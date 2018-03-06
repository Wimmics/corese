/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Helper class to handle the retrieveing of results when getEdges() are
 * parallelized.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class CallableGetEdges implements Callable<Iterable<Entity>> {

    private final Logger logger = LogManager.getLogger(CallableGetEdges.class);
    private Producer p = null;
    private Node gNode = null;
    private List<Node> from = null;
    private Edge qEdge = null;
    private Environment env = null;

    public CallableGetEdges(Producer p, Node gNode, List<Node> from, Edge qEdge, Environment env) {
        this.p = p;
        this.gNode = gNode;
        this.from = from;
        this.qEdge = qEdge;
        this.env = env;
    }

    @Override
    public Iterable<Entity> call() {
        Iterable<Entity> res  = p.getEdges(gNode, from, qEdge, env);
        return res;
    }

}
