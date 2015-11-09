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
import fr.inria.edelweiss.kgram.core.Memory;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * Helper class to handle the retrieveing of results when getEdges() are
 * parallelized.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
public class CallableGetEdges implements Callable<Iterable<Entity>> {

    private final Logger logger = Logger.getLogger(CallableGetEdges.class);
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
//        StopWatch sw = new StopWatch();
//        sw.start();
        Iterable<Entity> res =  new ArrayList<Entity>();
        if (p instanceof RemoteProducerWSImpl) {
            RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
            if(!rp.isAlreadyProcessed(qEdge))
              res = p.getEdges(gNode, from, qEdge, env);
        }
//        sw.stop();
//        logger.info("Finished CallableGetEdge in " + sw.getTime() + " ms.");
        return res;
    }

}
