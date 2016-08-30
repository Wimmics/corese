package fr.inria.edelweiss.kgdqp.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Meta Producer that handles concurent accesses to several Producers
 * Uses a generic MetaIterator that iterates over Producer iterators
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class PipelinedMetaProducer extends MetaProducer {

    private final Logger logger = LogManager.getLogger(PipelinedMetaProducer.class);
    private ArrayList<SyncEdgeBuffer> buffers = new ArrayList<SyncEdgeBuffer>();

    protected PipelinedMetaProducer() {
        super();
    }

    public static PipelinedMetaProducer create() {
        return new PipelinedMetaProducer();
    }

    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env) {

        ExecutorService executorS = Executors.newCachedThreadPool();
        CompletionService<Iterable<Entity>> completionS = new ExecutorCompletionService<Iterable<Entity>>(executorS);
        List<Future<Iterable<Entity>>> futures = new ArrayList<Future<Iterable<Entity>>>();
        
        int pending = 0;
        for (Producer p : this.getProducers()) {
            if ((p instanceof RemoteProducerWSImpl) || ((p instanceof RemoteSqlProducerImpl))) {
                pending++;
            }
        }
        
        SyncEdgeBuffer buffer = new SyncEdgeBuffer(pending, executorS);
        buffers.add(buffer);
//        SyncEdgeBufferV2 buffer = new SyncEdgeBufferV2(futures);
//        logger.info("added new buffer for " + edge);
        // iteration over 
        for (Producer p : this.getProducers()) {
            //TODO Check index
            if ((p instanceof RemoteProducerWSImpl) || ((p instanceof RemoteSqlProducerImpl))) {
                CallableBufferedGetEdges getEdges = new CallableBufferedGetEdges(p, gNode, from, edge, env, buffer);
                futures.add(completionS.submit(getEdges));
            }
        }

        return buffer;
    }

    MetaIterator<Entity> add(MetaIterator<Entity> meta, Iterable<Entity> it) {
        MetaIterator<Entity> m = new MetaIterator<Entity>(it);
        if (meta == null) {
            meta = m;
        } else {
            meta.next(m);
        }
        return meta;
    }
}
