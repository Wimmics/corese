/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLRestEndpointClient;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgdqp.strategies.SourceSelectorWS;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizer;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizerFactory;
import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import fr.inria.corese.kgram.api.core.*;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.SPARQLResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

/**
 * Implementation of the remote producer, acting as web service client for a
 * KGRAM endpoint (kgserver web service).
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class RemoteProducerWSImpl implements Producer {

    private final static Logger logger = LogManager.getLogger(RemoteProducerWSImpl.class);
    private final SparqlEndpointInterface rp;
    private final HashMap<String, Boolean> cacheIndex = new HashMap<String, Boolean>();
    private boolean provEnabled = false;

    public RemoteProducerWSImpl(URL url, WSImplem implem, boolean provEnabled) {
        this.provEnabled = provEnabled;
        if (implem == WSImplem.REST) {
            rp = new SPARQLRestEndpointClient(url);
            logger.debug("REST endpoint instanciated " + url);
        } else {
//            rp = new SPARQLSoapEndpointClient(url);
            logger.error("SOAP endpoint not supported anymore " + url);
            rp = null;
        }
    }

    public boolean isProvEnabled() {
        return provEnabled;
    }

    public void setProvEnabled(boolean provEnabled) {
        this.provEnabled = provEnabled;
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
                    if (this.isProvEnabled()) {
                        //TODO does it make sense ?
//                        annotateResultsWithProv((Graph) maps.getGraph(), gNode);
                    }
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
            return namespaceMgr.toString();
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

//        logger.debug("gNode = " + gNode);
//        logger.debug("from = " + from);

//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createSimpleOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFilterOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createBindingOptimizer();
        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();
        String rwSparql = qo.getSparqlQuery(gNode, from, qEdge, env);

        Graph g = Graph.create(false);
        g.setTuple(true);
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();

            if (SourceSelectorWS.ask(qEdge, this, env)) {
                logger.debug("sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
                String sparqlRes = rp.getEdges(rwSparql);
//                logger.debug(sparqlRes);

                if (env.getQuery() != null && env.getQuery().isDebug()) {
                    System.out.println("Query:\n" + rwSparql + "\n" + rp.getEndpoint());
                    System.out.println("Result:\n" + sparqlRes);
                }

                boolean isNotEmpty = sparqlRes != null;
                if (isNotEmpty) {
                    Load l = Load.create(g);
                    is = new ByteArrayInputStream(sparqlRes.getBytes());
//                    l.load(is, ".ttl");
                    l.parse(is);
                    logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms from " + rp.getEndpoint());
                }
                
//                synchronized(this){
//                   logger.info(g+"  number of triple EDGE ???  "+g.size()); 
//                }
                QueryProcessDQP.updateCounters(qEdge.toString(), rp.getEndpoint(), isNotEmpty, new Long(g.size()));
                
                if(isNotEmpty){
                    if (this.isProvEnabled()) {
                        this.annotateResultsWithProv(g, qEdge);
                    }
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
            for (String p : namespaceMgr.getPrefixes()) {
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

        String rwSparql = sparqlPrefixes;
        if (gNode == null) {
            rwSparql += "CONSTRUCT  { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " } " + fromClauses + "\n WHERE { \n";
            rwSparql += "\t " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + " .\n ";
            rwSparql += "}";
        } else {
            rwSparql += "CONSTRUCT { GRAPH " + gNode.toString() + " { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " }} " + fromClauses + "\n WHERE { \n";
            rwSparql += "\t GRAPH " + gNode.toString() + "{ " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + "} .\n ";
            rwSparql += "}";
        }

        Graph g = Graph.create();
        g.setTuple(true);

        logger.debug("sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());

        // count number of queries
        if (QueryProcessDQP.queryCounter.containsKey(qEdge.toString())) {
            Long n = QueryProcessDQP.queryCounter.get(qEdge.toString());
            QueryProcessDQP.queryCounter.put(qEdge.toString(), n + 1L);
        } else {
            QueryProcessDQP.queryCounter.put(qEdge.toString(), 1L);
        }
        // count number of source access
        String endpoint = rp.getEndpoint();
        if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
            Long n = QueryProcessDQP.sourceCounter.get(endpoint);
            QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
        } else {
            QueryProcessDQP.sourceCounter.put(endpoint, 1L);
        }
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(rwSparql);
            logger.debug("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
//                l.load(is, ".ttl");
                l.load(is);
                logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
                if (QueryProcessDQP.queryVolumeCounter.containsKey(qEdge.toString())) {
                    Long n = QueryProcessDQP.queryVolumeCounter.get(qEdge.toString());
                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), n + (long) g.size());
                } else {
                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), (long) g.size());
                }
                if (QueryProcessDQP.sourceVolumeCounter.containsKey(endpoint)) {
                    Long n = QueryProcessDQP.sourceVolumeCounter.get(endpoint);
                    QueryProcessDQP.sourceVolumeCounter.put(endpoint, n + (long) g.size());
                } else {
                    QueryProcessDQP.sourceVolumeCounter.put(endpoint, (long) g.size());
                }
                if (this.isProvEnabled()) {
                    annotateResultsWithProv(g, qEdge);
                }
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
            for (String p : namespaceMgr.getPrefixes()) {
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
        String rwSparql = sparqlPrefixes;

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
                rwSparql += "CONSTRUCT  { " + object + " <" + exp.getName() + "> " + subject + " } " + fromClauses + "\n WHERE { \n";
                rwSparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
            } else {
                rwSparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{" + object + " <" + exp.getName() + "> " + subject + " }} " + fromClauses + "\n WHERE { \n";
                rwSparql += "\t GRAPH " + gNode.toString() + "{" + subject + " " + transformedExp + " " + object + "} .\n ";
            }
        } else if (exp.isNot()) {
            boolean valid = checkNeg(exp);
            if (!valid) {
                logger.warn("Invalid negation in path expression " + exp.toString());
                return new ArrayList<Entity>();
            } else {
                List<String> negProps = getFlatRegEx(exp);
                if (gNode == null) {
                    rwSparql += "CONSTRUCT  { " + subject + " ?_p " + object + " } " + fromClauses + "\n WHERE { \n";
                    rwSparql += "\t " + subject + " ?_p " + object + " .\n ";
                } else {
                    rwSparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{ " + subject + " ?_p " + object + " }} " + fromClauses + "\n WHERE { \n";
                    rwSparql += "\t GRAPH " + gNode.toString() + "{" + subject + " ?_p " + object + "} .\n ";
                }
                rwSparql += "\tFILTER ( \n";
                for (String negP : negProps) {
                    rwSparql += "\t\t (?_p != " + negP + " ) && \n ";
                }
                rwSparql = rwSparql.substring(0, rwSparql.lastIndexOf("&&"));
                rwSparql += " )";

            }
        } else {
            if (gNode == null) {
                rwSparql += "CONSTRUCT  { " + subject + " <" + exp.getName() + "> " + object + " } " + fromClauses + "\n WHERE { \n";
                rwSparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";
            } else {
                rwSparql += "CONSTRUCT  { GRAPH " + gNode.toString() + "{ " + subject + " <" + exp.getName() + "> " + object + " }} " + fromClauses + "\n WHERE { \n";
                rwSparql += "\t GRAPH " + gNode.toString() + "{" + subject + " " + transformedExp + " " + object + "} .\n ";
            }
        }
        rwSparql += "\n}";

        // Remote query processing
        Graph g = Graph.create();
        g.setTuple(true);

//        logger.debug("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());
        if (QueryProcessDQP.queryCounter.containsKey(qEdge.toString())) {
            Long n = QueryProcessDQP.queryCounter.get(qEdge.toString());
            QueryProcessDQP.queryCounter.put(qEdge.toString(), n + 1L);
        } else {
            QueryProcessDQP.queryCounter.put(qEdge.toString(), 1L);
        }
        // count number of source access
        String endpoint = rp.getEndpoint();
        if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
            Long n = QueryProcessDQP.sourceCounter.get(endpoint);
            QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
        } else {
            QueryProcessDQP.sourceCounter.put(endpoint, 1L);
        }
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(rwSparql);
            logger.debug("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
//                l.load(is, ".ttl");
                l.load(is);
                logger.debug("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
                if (QueryProcessDQP.queryVolumeCounter.containsKey(qEdge.toString())) {
                    Long n = QueryProcessDQP.queryVolumeCounter.get(qEdge.toString());
                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), n + (long) g.size());
                } else {
                    QueryProcessDQP.queryVolumeCounter.put(qEdge.toString(), (long) g.size());
                }
                if (QueryProcessDQP.sourceVolumeCounter.containsKey(endpoint)) {
                    Long n = QueryProcessDQP.sourceVolumeCounter.get(endpoint);
                    QueryProcessDQP.sourceVolumeCounter.put(endpoint, n + (long) g.size());
                } else {
                    QueryProcessDQP.sourceVolumeCounter.put(endpoint, (long) g.size());
                }
                if (this.isProvEnabled()) {
                    annotateResultsWithProv(g, qEdge);
                }
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
        return false;
    }

    @Override
    public Producer getProducer(Node node, Environment env) {
        return null;
    }

    private void annotateResultsWithProv(Graph g, Edge qEdge) {
//        logger.info("Tracking provenance of " + qEdge.toString());
        Iterator<Entity> it = g.getEdges().iterator();

        // Annotate the invocation only once, Then iterate over edge results.
        String preInsertProv = "PREFIX prov:<" + Util.provPrefix + "> insert data {\n"
                // Sparql processing activity 
                + " _:b1 rdf:type prov:Activity . \n"
                + " _:b1 prov:qualifiedAssociation _:b2 . \n"
                // Association to a software agent through a plan (i.e. a sparql query) 
                + " _:b2 rdf:type prov:Association . \n"
                + " _:b2 prov:hadPlan _:b3 . \n"
                + " _:b2 prov:agent <" + rp.getEndpoint() + "> . \n"
                // The plan corresponding to the "recipe" 
                + " _:b3 rdf:type prov:Plan . \n"
                + " _:b3 rdfs:comment \"" + Constant.addEscapes(qEdge.toString()) + "\". \n";
//                                + " _:b3 rdfs:comment \"" + rwSparql.replaceAll("\"", "'").replaceAll("\n", " ").replaceAll("\t", " ") + "\". \n";

        while (it.hasNext()) {
            Entity entity = it.next();
            Edge e = entity.getEdge();
            if (e != null) {
                //TODO provenance for node result ; associated rwSparql ; associated endpoint
                //TODO TimeStamping
                //TODO duration ?

                // Resulting entity 
                String insertProv = preInsertProv + "[ rdf:type prov:Entity ; \n"
                        + " prov:wasGeneratedBy _:b1 ; \n"
                        + " rdf:type prov:Entity ; \n"
                        + " prov:wasAttributedTo <" + rp.getEndpoint() + "> ; \n"
                        + " rdf:predicate " + e.getEdgeNode().toString() + " ; \n"
                        + " rdf:subject " + e.getNode(0).toString() + " ; \n"
                        + " rdf:object " + e.getNode(1).toString() + " ; \n"
                        + " rdfs:comment \"" + qEdge.toString() + "\" ] \n"
                        + "}";
                try {
//                    logger.info(insertProv);
                    Graph provG = Graph.create();
                    QueryProcess qp = QueryProcess.create(provG);
                    qp.query(insertProv);
                    entity.setProvenance(provG);
                } catch (EngineException ex) {
                    logger.error("Error while inserting provenance:\n" + insertProv);
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public Object getGraph() {
        return null;
    }

    @Override
    public int getMode() {
        return Producer.DEFAULT;
    }

    @Override
    public void setGraphNode(Node n) {
    }

    @Override
    public Node getGraphNode() {
        return null;
    }

    @Override
    public Query getQuery() {
        return null;
    }

    @Override
    public Object getValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp bgp, Environment env) {

        Mappings mappings = new Mappings();

        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();
        String rwSparql = qo.getSparqlQueryBGP(gNode, from, bgp, env);

        Graph g = Graph.create(false);
        g.setTuple(true);
        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
//            logger.info("ASK FOR BGP "+SourceSelectorWS.ask(bgp, this, env));
            if (SourceSelectorWS.ask(bgp, this, env)) {
//                logger.debug("sending query \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
                String sparqlRes = rp.query(rwSparql);
//                logger.info("Result: from "+ rp.getEndpoint() +"\n ---->  "+sparqlRes);
                mappings = SPARQLResult.create(ProducerImpl.create(g)).parseString(sparqlRes);
                SPARQLResult.create(g).parseString(sparqlRes);
//                logger.info("SPARQL => Mappings result: \n"+mappings.toString());

                if (mappings.size() != 0) {
                    logger.debug(" results found \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
                } else {
                    logger.debug(" no result \n" + rwSparql + "\n" + "to " + rp.getEndpoint());
                }
                logger.debug(sparqlRes);

                if (env.getQuery() != null && env.getQuery().isDebug()) {
                    logger.info("Query:\n" + rwSparql + "\n" + rp.getEndpoint());
                    logger.info("Result:\n" + sparqlRes);
                }
                boolean isNotEmpty = sparqlRes != null;
                
//                synchronized(this){
//                    for(Mapping m : mappings)
//                   logger.info(m+"  number of triple BGP???  "+m.size()+"  ??? "+new Long(mappings.size() * mappings.get(0).size())); 
//                }
                
                QueryProcessDQP.updateCounters(bgp.toString(), rp.getEndpoint(), isNotEmpty, new Long(mappings.size()));

                if (isNotEmpty) {
                    logger.debug("Results (cardinality " + mappings.size() + ") merged in  " + sw.getTime() + " ms from " + rp.getEndpoint());
                    if (this.isProvEnabled()) {
                        for (int i = 0; i < bgp.getExpList().size(); i++) {
                            this.annotateResultsWithProv(g, bgp.getExpList().get(i).getEdge());
                        }
                    }
                }
            } else {
                logger.debug("negative ASK (" + bgp + ") -> pruning data source " + rp.getEndpoint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mappings;
    }

    public boolean checkEdge(Edge edge) {
        boolean result = false;
        if (edge instanceof EdgeImpl) {
            EdgeImpl e = (EdgeImpl) edge;
//            if(getCacheIndex().get(e.getTriple().getPredicate().toSparql())!=null){
            result = this.getCacheIndex().get(e.getTriple().getPredicate().toSparql());
//            }
        }
        return result;
    }

    public boolean checkBGP(Exp bgp) {
        boolean result = true;
        for (int i = 0; i < bgp.getExpList().size() && result; i++) {
//            if(bgp.getExpList().get(i).isEdge()){
            result = checkEdge(bgp.getExpList().get(i).getEdge());
//            }
        }
        return result;
    }

    @Override
    public Entity copy(Entity ent) {
        return ent;
    }

    @Override
    public void close() {

    }

}
