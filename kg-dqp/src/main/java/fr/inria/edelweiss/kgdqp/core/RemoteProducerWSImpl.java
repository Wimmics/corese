/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLRestEndpointClient;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLSoapEndpointClient;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgdqp.strategies.SourceSelectorWS;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizer;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizerFactory;
import fr.inria.edelweiss.kgram.api.core.*;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Implementation of the remote producer, acting as web service client for a
 * KGRAM endpoint (kgserver web service).
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteProducerWSImpl implements Producer {

    private static Logger logger = Logger.getLogger(RemoteProducerWSImpl.class);
    private SparqlEndpointInterface rp;
    private HashMap<String, Boolean> cacheIndex = new HashMap<String, Boolean>();

    public RemoteProducerWSImpl(URL url, WSImplem implem) {
        if (implem == WSImplem.REST) {
            rp = new SPARQLRestEndpointClient(url);
            logger.debug("REST endpoint instanciated " + url);
        } else {
            rp = new SPARQLSoapEndpointClient(url);
            logger.debug("SOAP endpoint instanciated " + url);
        }
    }

    @Override
    public void init(int nbNodes, int nbEdges) {
    }

    @Override
    /**
     * propagate to the server ? nothing
     */
    public void setMode(int n) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return graph nodes from the remote SPARQL endpoint through a SELECT
     * query. Results are serialized with
     */
    @Override
    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
        try {
            Node node = env.getNode(gNode);
            String rwSparql = getSparqlForGetGN(gNode, from, env);

            logger.debug("Sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
            String sparqlRes = rp.query(rwSparql);
            Mappings maps = SPARQLResult.create(ProducerImpl.create(Graph.create())).parseString(sparqlRes);
            ArrayList<Node> resNodes = new ArrayList<Node>();

            for (Mapping m : maps) {
                logger.debug(m);
                for (Node n : m.getNodes()) {
                    resNodes.add(n);
                }
            }

            return resNodes;
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        logger.debug("Is Graph Node ? " + gNode.toString());
        Node node = env.getNode(gNode);

//        if (gNode.isVariable()) {
//
//        } else if (gNode.isConstant()) {
//
//        }

        String rwSparql = getSparqlForIsGN(gNode, from, env);

        logger.debug("Sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
        String sparqlRes = rp.query(rwSparql);
        boolean res = sparqlRes.contains("<boolean>true</boolean>");
        return res;
    }

    private String getSparqlForGetGN(Node gNode, List<Node> from, Environment env) {
        String selectFrom = "SELECT DISTINCT * ";
        String sparql = getSparqlPrefixes(env);
        if (!from.isEmpty()) {
            for (Node f : from) {
                selectFrom += "FROM NAMED " + f + " ";
            }
        }
        sparql += selectFrom + " WHERE \n\t { GRAPH " + gNode.toString() + "{} }";

        return sparql;
    }

    private String getSparqlForIsGN(Node gNode, List<Node> from, Environment env) {
        String askFrom = "ASK ";
        String sparql = getSparqlPrefixes(env);

        if (!from.isEmpty()) {
            for (Node f : from) {
                askFrom += "FROM NAMED " + f + " ";
            }
//            askFrom = askFrom.substring(0, askFrom.lastIndexOf(","));
        }
        sparql += askFrom + "\n\t { GRAPH " + gNode.toString() + "{} }";

        return sparql;
    }

    private String getSparqlPrefixes(Environment env) {
        String sparqlPrefixes = "";
        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }
        return sparqlPrefixes;
    }

    /**
     * Transforms an EDGE request into a simple SPARQL query pushed to the
     * remote producer. Results are returned through standard web services
     * protocol.
     *
     * @param gNode graph variable if it exists, null otherwise
     * @param from "from named <g>" list
     * @param qEdge edge searched for
     * @param env query execution context (current variable values, etc.)
     * @return an iterator over graph entities
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env) {

        logger.debug("gNode = " + gNode);
        logger.debug("from = " + from);

//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createSimpleOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFilterOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createBindingOptimizer();
        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();
        String rwSparql = qo.getSparqlQuery(gNode, from, qEdge, env);

        Graph g = Graph.create(false);
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();

            if (SourceSelectorWS.ask(qEdge, this, env)) {
                logger.debug("sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
                String sparqlRes = rp.getEdges(rwSparql);
                logger.debug(sparqlRes);

                // count number of queries
//                if (QueryProcessDQP.queryCounter.containsKey(qEdge.toString())) {
//                    Long n = QueryProcessDQP.queryCounter.get(qEdge.toString());
//                    QueryProcessDQP.queryCounter.put(qEdge.toString(), n + 1L);
//                } else {
//                    QueryProcessDQP.queryCounter.put(qEdge.toString(), 1L);
//                }

                // count number of source access
//                String endpoint = rp.getEndpoint();
//                if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
//                    Long n = QueryProcessDQP.sourceCounter.get(endpoint);
//                    QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
//                } else {
//                    QueryProcessDQP.sourceCounter.put(endpoint, 1L);
//                }

                if (sparqlRes != null) {
                    Load l = Load.create(g);
                    is = new ByteArrayInputStream(sparqlRes.getBytes());
                    l.load(is, ".ttl");
                    logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms from " + rp.getEndpoint());
//                    if (QueryProcessDQP.queryVolumeCounter.containsKey(qEdge.toString())) {
//                        Long n = QueryProcessDQP.queryVolumeCounter.get(qEdge.toString());
//                        QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), n + (long) g.size());
//                    } else {
//                        QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), (long) g.size());
//                    }
                }

            } else {
                logger.debug("negative ASK (" + qEdge + ") -> pruning data source " + rp.getEndpoint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return g.getEdges();
    }

    /**
     * UNUSED with kgram (pas de manipulation des sommets)
     *
     * @param gNode
     * @param from
     * @param qNode
     * @param env
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Node qNode, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * a priori inutile de le propager cote serveur car il sera declenche par le
     * moteur local (rien en pratique).
     *
     * @param qEdge
     * @param index
     */
    @Override
    public void initPath(Edge qEdge, int index) {
    }

    /**
     *
     * @param gNode
     * @param from
     * @param env
     * @param exp inutile dans ce cas la
     * @param index sert au sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp,
            int index) {

        String sparqlPrefixes = "";

        //prefix handling
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }
        ArrayList<String> filters = new ArrayList<String>();

        //from handling
        String fromClauses = "";
        if ((from != null) && (!from.isEmpty())) {
            for (Node f : from) {
                if (gNode == null) {
                    fromClauses += "FROM ";
                } else {
                    fromClauses += "FROM NAMED ";
                }
                fromClauses += f + " ";
            }
        }

        String sparql = sparqlPrefixes;
        if (gNode == null) {
            sparql += "CONSTRUCT  { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " } " + fromClauses + "\n WHERE { \n";
            sparql += "\t " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + " .\n ";
            sparql += "}";
        } else {
            sparql += "CONSTRUCT { GRAPH " + gNode.toString() + " { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " }} " + fromClauses + "\n WHERE { \n";
            sparql += "\t GRAPH " + gNode.toString() + "{ " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + "} .\n ";
            sparql += "}";
        }

        Graph g = Graph.create();
        logger.debug("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());

        // count number of queries
//        if (QueryProcessDQP.queryCounter.containsKey(qEdge.toString())) {
//            Long n = QueryProcessDQP.queryCounter.get(qEdge.toString());
//            QueryProcessDQP.queryCounter.put(qEdge.toString(), n + 1L);
//        } else {
//            QueryProcessDQP.queryCounter.put(qEdge.toString(), 1L);
//        }

        // count number of source access
//        String endpoint = rp.getEndpoint();
//        if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
//            Long n = QueryProcessDQP.sourceCounter.get(endpoint);
//            QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
//        } else {
//            QueryProcessDQP.sourceCounter.put(endpoint, 1L);
//        }

        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
            logger.debug("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is, ".ttl");
                logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
//                if (QueryProcessDQP.queryVolumeCounter.containsKey(qEdge.toString())) {
//                    Long n = QueryProcessDQP.queryVolumeCounter.get(qEdge.toString());
//                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), n + (long) g.size());
//                } else {
//                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), (long) g.size());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return g.getAllNodes();
    }

    /**
     * Specific to SPARQL 1.1 property path expressions
     *
     * @param gNode
     * @param from
     * @param qEdge
     * @param env ne pas prendre en compte l'env dans le calcul des chemins
     * @param exp
     * @param src l'ensemble des
     * @param start noeud courant
     * @param index sens du parcours
     * @return
     */
    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start, int index) {

        //prefix handling
        String sparqlPrefixes = "";
        if (env.getQuery().getAST() instanceof ASTQuery) {
            ASTQuery ast = (ASTQuery) env.getQuery().getAST();
            NSManager namespaceMgr = ast.getNSM();
            Enumeration<String> prefixes = namespaceMgr.getPrefixes();
            while (prefixes.hasMoreElements()) {
                String p = prefixes.nextElement();
                sparqlPrefixes += "PREFIX " + p + ": " + "<" + namespaceMgr.getNamespace(p) + ">\n";
            }
        }

        //Specific to path processing
        Node subject = null;
        Node object = null;
        if (start == null) {
            subject = qEdge.getNode(0);
            object = qEdge.getNode(1);
        } else if (index == 0) {
            //normal order
            subject = start;
            object = qEdge.getNode(1);
        } else {
            //reverse order
            subject = qEdge.getNode(1);
            object = start;
        }

        // Query rewriting
        String transformedExp = exp.toRegex();
        String sparql = sparqlPrefixes;

        //from handling
        String fromClauses = "";
        if ((from != null) && (!from.isEmpty())) {
            for (Node f : from) {
                if (gNode == null) {
                    fromClauses += "FROM ";
                } else {
                    fromClauses += "FROM NAMED ";
                }
                fromClauses += f + " ";
            }
        }

        if (exp.isReverse()) {
            if (gNode == null) {
                sparql += "CONSTRUCT  { " + object + " <" + exp.getName() + "> " + subject + " } " + fromClauses + "\n WHERE { \n";
                sparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
            } else {
                sparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + object + " <" + exp.getName() + "> " + subject + " }} " + fromClauses + "\n WHERE { \n";
                sparql += "\t GRAPH " + gNode.toString() + "{" + subject + " " + transformedExp + " " + object + "} .\n ";
            }
        } else if (exp.isNot()) {
            boolean valid = checkNeg(exp);
            if (!valid) {
                logger.warn("Invalid negation in path expression " + exp.toString());
                return new ArrayList<Entity>();
            } else {
                List<String> negProps = getFlatRegEx(exp);
                if (gNode == null) {
                    sparql += "CONSTRUCT  { " + subject + " ?_p " + object + " } " + fromClauses + "\n WHERE { \n";
                    sparql += "\t " + subject + " ?_p " + object + " .\n ";
                } else {
                    sparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{ " + subject + " ?_p " + object + " }} " + fromClauses + "\n WHERE { \n";
                    sparql += "\t GRAPH " + gNode.toString() + "{" + subject + " ?_p " + object + "} .\n ";
                }
                sparql += "\tFILTER ( \n";
                for (String negP : negProps) {
                    sparql += "\t\t (?_p != " + negP + " ) && \n ";
                }
                sparql = sparql.substring(0, sparql.lastIndexOf("&&"));
                sparql += " )";

            }
        } else {
            if (gNode == null) {
                sparql += "CONSTRUCT  { " + subject + " <" + exp.getName() + "> " + object + " } " + fromClauses + "\n WHERE { \n";
                sparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
            } else {
                sparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{ " + subject + " <" + exp.getName() + "> " + object + " }} " + fromClauses + "\n WHERE { \n";
                sparql += "\t GRAPH " + gNode.toString() + "{" + subject + " " + transformedExp + " " + object + "} .\n ";
            }
        }
        sparql += "\n}";

        // Remote query processing
        Graph g = Graph.create();
//        logger.debug("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());

//        if (QueryProcessDQP.queryCounter.containsKey(qEdge.toString())) {
//            Long n = QueryProcessDQP.queryCounter.get(qEdge.toString());
//            QueryProcessDQP.queryCounter.put(qEdge.toString(), n + 1L);
//        } else {
//            QueryProcessDQP.queryCounter.put(qEdge.toString(), 1L);
//        }

        // count number of source access
//        String endpoint = rp.getEndpoint();
//        if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
//            Long n = QueryProcessDQP.sourceCounter.get(endpoint);
//            QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
//        } else {
//            QueryProcessDQP.sourceCounter.put(endpoint, 1L);
//        }


        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
            logger.debug("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is, ".ttl");
                logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
//                if (QueryProcessDQP.queryVolumeCounter.containsKey(qEdge.toString())) {
//                    Long n = QueryProcessDQP.queryVolumeCounter.get(qEdge.toString());
//                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), n + (long) g.size());
//                } else {
//                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), (long) g.size());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return g.getEdges();
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Node getNode(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public boolean isBindable(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public List<Node> toNodeList(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appele sur un remoteProducer
     *
     * @param value
     * @return
     */
    @Override
    public Mappings map(List<Node> qNodes, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms a regular expression eventually containing nested expressions
     * into a flat list of expressions.
     *
     * @param exp a regular expression eventually containing nested expressions.
     * @return a flat list of initially nested expressions.
     */
    private List<String> getFlatRegEx(Regex exp) {
        ArrayList<String> res = new ArrayList<String>();
        if (exp.getArity() == -1) {
            res.add(exp.getName());
        } else if (exp.getArity() == 0) {
            res.add(exp.getName());
        } else {
            for (int i = 0; i < exp.getArity(); i++) {
                res.addAll(getFlatRegEx(exp.getArg(i)));
            }
        }
        return res;
    }

    /**
     * Checks that a negation only covers | operators.
     *
     * @param exp a regular exression.
     * @return true if the negation expresison is valid.
     */
    private boolean checkNeg(Regex exp) {
        boolean res = true;
        if (exp.getArity() == -1) {
            return true;
        } else if (exp.getArity() == 0) {
            return true;
        } else {
            if (!(exp.getName().equals("!") || exp.getName().equals("|"))) {
                return false;
            }
            for (int i = 0; i < exp.getArity(); i++) {
                res = res && checkNeg(exp.getArg(i));
            }
        }
        return res;
    }

    public synchronized HashMap<String, Boolean> getCacheIndex() {
        return cacheIndex;
    }

    public SparqlEndpointInterface getEndpoint() {
        return rp;
    }

    @Override
    public boolean isProducer(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Producer getProducer(Node node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
