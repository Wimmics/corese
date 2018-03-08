/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.core;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Helper class to handle the retrieveing of results when getEdges() or
 * getMappings() are parallelized.
 *
 * Merging of CallableGetEdges() and CallableGetMappings()
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class CallableResult implements Callable<Result> {

    private final Logger logger = LogManager.getLogger(CallableResult.class);
    private Producer producer = null;
    private Node gNode = null;
    private List<Node> from = null;
    private Exp exp = null;
    private Environment env = null;

    public CallableResult(Producer p, Node gNode, List<Node> from, Exp exp, Environment env) {
        this.producer = p;
        this.gNode = gNode;
        this.from = from;
        this.exp = exp;
        this.env = env;
    }

    @Override
    public Result call() {
        StopWatch sw = new StopWatch();
        sw.start();
        Result result = new Result((RemoteProducerWSImpl) producer);

        if (exp.isEdge()) {
            logger.info("CallableResult for GetEdge");
            Iterable<Entity> res = producer.getEdges(gNode, from, exp.getEdge(), env);
            result.setEntities(res);
        } else {
            logger.info("CallableResult for GetMappings");
            Mappings mappings = producer.getMappings(gNode, from, exp, env);
            RemoteProducerWSImpl rp = (RemoteProducerWSImpl) producer;
            logger.info("RESULTS: "+mappings.size() +" FROM "+rp.getEndpoint().getEndpoint());
            result.setMappings(mappings);
        }
        sw.stop();
        logger.info("Finished CallableResult in " + sw.getTime() + " ms.");
        return result;
    }

}
