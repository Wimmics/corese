/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizer;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizerFactory;
import fr.inria.edelweiss.kgram.api.core.*;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 * Implementation of the remote producer, acting as web service client for a
 * KGRAM endpoint (kgserver web service).
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class RemoteProducerImpl implements Producer {

    private static Logger logger = Logger.getLogger(RemoteProducerImpl.class);
    private RemoteProducer rp;

    public RemoteProducerImpl(URL url) {
        rp = RemoteProducerServiceClient.getPort(url);
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
     * @return
     */
    @Override
    public Iterable<Node> getGraphNodes(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param gNode
     * @param from
     * @param env
     * @return
     */
    @Override
    public boolean isGraphNode(Node gNode, List<Node> from, Environment env) {
        throw new UnsupportedOperationException("Not supported yet.");
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

        // si gNode != null et from non vide, alors "from named"
        // si gNode == null et from non vide alors "from"

        ArrayList<Entity> results = new ArrayList<Entity>();
        Iterator it = null;

//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createSimpleOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFilterOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createBindingOptimizer();
        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();
        String query = qo.getSparqlQuery(qEdge, env);
        Graph g = Graph.create();

        logger.debug("sending query \n" + query + "\n" + "to " + rp.getEndpoint());

        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(query);
            logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is);
                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
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
    public void initPath(Edge qEdge,
            int index) {
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

        String sparql = sparqlPrefixes;
        sparql += "construct  { " + qEdge.getNode(0) + qEdge.getEdgeNode() + qEdge.getNode(1) + " } \n where { \n";
        sparql += "\t " + qEdge.getNode(0) + " " + qEdge.getEdgeNode() + "{0}" + " " + qEdge.getNode(1) + " .\n ";

        if (filters.size() > 0) {
            sparql += "\t  FILTER (\n";
            int i = 0;
            for (String filter : filters) {
                if (i == (filters.size() - 1)) {
                    sparql += "\t\t " + filter + "\n";
                } else {
                    sparql += "\t\t " + filter + "&&\n";
                }
                i++;
            }
            sparql += "\t  )\n";
        }
        sparql += "}";

        Graph g = Graph.create();
        logger.info("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());

        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
            logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is);
                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
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

        //filter handling
        ArrayList<String> filters = new ArrayList<String>();

        //Specifc to path processing
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

        String negProp = null;
        String transformedExp = exp.toRegex();
        if (exp.isReverse()) {
            transformedExp = "^(" + exp.toRegex() + ")";
            System.out.println(transformedExp);
        } else if (exp.isNot()) {
            negProp = transformedExp.substring(2, transformedExp.lastIndexOf(")"));
            System.out.println(negProp);
//            throw new UnsupportedOperationException("Unsupported negation of properties in paths");
            String propSparql = sparqlPrefixes;
            propSparql += "SELECT DISTINCT ?p WHERE {?x ?p ?y MINUS{?x " + negProp + " ?y}}";
            Graph g = Graph.create();
            InputStream is = null;
            try {
                String sparqlRes = rp.getEdges(propSparql);
                if (sparqlRes != null) {
                    Load l = Load.create(g);
                    is = new ByteArrayInputStream(sparqlRes.getBytes());
                    l.load(is);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String ex = exp.toRegex();
            transformedExp = "(";

            Iterator<Entity> it = g.getEdges().iterator();
            while (it.hasNext()) {
                transformedExp += "<" + it.next().getEdge().getLabel() + "> | ";
            }
            transformedExp = transformedExp.substring(0, transformedExp.lastIndexOf("|"));
            transformedExp += ")";
        }

        String sparql = sparqlPrefixes;
        if (exp.isReverse()) {
            sparql += "construct  { " + object + " <" + exp.getName() + "> " + subject + " } \n where { \n";
        } else if (exp.isNot()) {
            sparql += "construct  { " + subject + " " + exp.toRegex() + " " + object + " } \n where { \n";
        } else {
            sparql += "construct  { " + subject + " <" + exp.getName() + "> " + object + " } \n where { \n";
        }
        sparql += "\t " + subject + " " + transformedExp + " " + object + " .\n ";

        if (filters.size() > 0) {
            sparql += "\t  FILTER (\n";
            int i = 0;
            for (String filter : filters) {
                if (i == (filters.size() - 1)) {
                    sparql += "\t\t " + filter + "\n";
                } else {
                    sparql += "\t\t " + filter + "&&\n";
                }
                i++;
            }
            sparql += "\t  )\n";
        }
        sparql += "}";

        Graph g = Graph.create();

        logger.info("sending query \n" + sparql + "\n" + "to " + rp.getEndpoint());

        InputStream is = null;
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            String sparqlRes = rp.getEdges(sparql);
            logger.info("Received results in " + sw.getTime() + " ms from " + rp.getEndpoint());
            sw.reset();
            sw.start();
            if (sparqlRes != null) {
                Load l = Load.create(g);
                is = new ByteArrayInputStream(sparqlRes.getBytes());
                l.load(is);
                logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
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

    /*
     * ?x p ?y FILTER ((?x > 10) && (?z > 10))
     *
     */
    public boolean bound(Edge edge, Filter filter) {
        List<String> vars = new ArrayList<String>();
        if (edge.getNode(0).isVariable()) {
            vars.add(edge.getNode(0).toString());
        }
        if (edge.getNode(1).isVariable()) {
            vars.add(edge.getNode(1).toString());
        }

        List<String> varsFilter = filter.getVariables();
        for (String var : varsFilter) {
            if (!vars.contains(var)) {
                return false;
            }
        }
        return true;
    }
}
