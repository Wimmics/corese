/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgram.tool;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author gaignard
 */
public class CallableGetEdges implements Callable<Iterable<Entity>> {
    
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
        return p.getEdges(gNode, from, qEdge, env);
    }
    
}
