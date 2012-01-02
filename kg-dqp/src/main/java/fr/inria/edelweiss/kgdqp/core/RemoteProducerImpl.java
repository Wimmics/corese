/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizer;
import fr.inria.edelweiss.kgdqp.strategies.RemoteQueryOptimizerFactory;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
     * remote producer. Results are returned through standard 
     * web services protocol. 
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

//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createSimpleOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFilterOptimizer();
//        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createBindingOptimizer();
        RemoteQueryOptimizer qo = RemoteQueryOptimizerFactory.createFullOptimizer();

        String query = qo.getSparqlQuery(qEdge, env);
        Graph g = Graph.create();
        Graph g1 = Graph.create();
        Node source = g1.addGraph("http://ns.inria.fr/edelweiss/2010/kgram/default");

        logger.debug("sending query \n" + query + "\n" + "to " + rp.getEndpoint());
        {
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
                    // bug unicode char encoding
//                    is = new ByteArrayInputStream(sparqlRes.getBytes());
//                    l.load(is);
                    File temp = File.createTempFile("pattern", ".rdf");
                    BufferedWriter out = new BufferedWriter(new FileWriter(temp));
                    out.write(sparqlRes);
                    out.close();

                    l.load(temp.getAbsolutePath());
                    temp.delete();

                    Iterator it = g.getEdges().iterator();
                    while (it.hasNext()) {
                        Edge e = (Edge) it.next();
                        //                    logger.info("From Graph " + e.toString());
                        results.add((Entity) e);
                    }
                    logger.info("Results (cardinality " + g.size() + ") merged in  " + sw.getTime() + " ms.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
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
     * a priori inutile de le propager cote serveur car il sera déclenche par le moteur local (rien en pratique).
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
     * @param exp inutile dans ce cas là
     * @param index sert au sens du parcours
     * @return 
     */
    @Override
    public Iterable<Entity> getNodes(Node gNode, List<Node> from, Edge qEdge, Environment env, List<Regex> exp,
            int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Dans le cas des chemins
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
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge qEdge, Environment env, Regex exp, Node src, Node start,
            int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appelé sur un remoteProducer
     * @param value
     * @return 
     */
    @Override
    public Node getNode(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appelé sur un remoteProducer
     * @param value
     * @return 
     */
    @Override
    public boolean isBindable(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appelé sur un remoteProducer
     * @param value
     * @return 
     */
    @Override
    public List<Node> toNodeList(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * inutile car jamais appelé sur un remoteProducer
     * @param value
     * @return 
     */
    @Override
    public Mappings map(List<Node> qNodes, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
