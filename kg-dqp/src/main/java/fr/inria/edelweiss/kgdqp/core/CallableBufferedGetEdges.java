/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * Helper class to handle the retrieveing of results 
 * when getEdges() are parallelized. 
 *
 * This version push resuts as soon as they are available in a shared BlockingQueue 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class CallableBufferedGetEdges implements Callable<Iterable<Entity>> {
    
    private final Logger logger = Logger.getLogger(CallableBufferedGetEdges.class);
    private Producer p = null;
    private Node gNode = null; 
    private List<Node> from = null;
    private Edge qEdge = null; 
    private Environment env = null;
    private SyncEdgeBuffer buffer = null;

    public CallableBufferedGetEdges(Producer p, Node gNode, List<Node> from, Edge qEdge, Environment env, SyncEdgeBuffer buf) {
        this.p = p;
        this.gNode = gNode;
        this.from = from;
        this.qEdge = qEdge;
        this.env = env;
        this.buffer = buf;
    }    
    
    @Override 
    public Iterable<Entity> call() {
        StopWatch sw = new StopWatch();
        sw.start();
        Iterable<Entity> res =  p.getEdges(gNode, from, qEdge, env);
        Iterator<Entity> it = res.iterator();
        while (it.hasNext()) {
            buffer.put(it.next());
        }
        buffer.put(new Stop());
        sw.stop();
//        logger.info("Finished CallableGetEdge in "+sw.getTime()+" ms.");
        return res;
    }
}