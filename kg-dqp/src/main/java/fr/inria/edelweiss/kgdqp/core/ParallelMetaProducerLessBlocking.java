package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgenv.parser.EdgeImpl;
import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.Iterator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * Meta Producer that handles concurrent accesses to several Producers. Uses a
 * generic MetaIterator that iterates over Producer iterators
 *
 * This version uses a CompletionService that allows to exploit the first
 * available results.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class ParallelMetaProducerLessBlocking extends MetaProducer {

    private final Logger logger = Logger.getLogger(ParallelMetaProducerLessBlocking.class);

    protected ParallelMetaProducerLessBlocking() {
        super();
    }

    public static ParallelMetaProducerLessBlocking create() {
        return new ParallelMetaProducerLessBlocking();
    }

    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env) {
        ExecutorService executorS = Executors.newCachedThreadPool();
        CompletionService<Iterable<Entity>> completionS = new ExecutorCompletionService<Iterable<Entity>>(executorS);

        MetaIterator<Entity> meta = null;
        List<Future<Iterable<Entity>>> futures = new ArrayList<Future<Iterable<Entity>>>();

        logger.info("Searching for edge : " + edge.toString());
        // iteration over 
        for (Producer p : this.getProducers()) {
            if (p instanceof RemoteProducerWSImpl) {
                RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                if (rp.checkEdge(edge)) {
                    CallableGetEdges getEdges = new CallableGetEdges(p, gNode, from, edge, env);
                    futures.add(completionS.submit(getEdges));
                }
            }
        }

        StopWatch sw = new StopWatch();
        sw.start();

        //retrieving results
        for (Future<Iterable<Entity>> f : futures) {
            try {
                Iterable<Entity> resultFromProducer = completionS.take().get();
                //detele duplicates
                Iterator<Entity> it = resultFromProducer.iterator();
                boolean doubles = false;
                while (it.hasNext()) {
                    Entity ent = it.next();
                    if (ent != null) {
                        Edge ed = ent.getEdge();
                        if (meta != null) {
                            Iterator<Entity> itt = meta.iterator();
                            while (itt.hasNext()) {
                                Entity entt = itt.next();
                                Edge edd = entt.getEdge();
                                if (ed.getNode().equals(edd.getNode())) {
//                                    logger.info(" DUPLICATES " + ed.getNode().toString());
                                    doubles = true;
                                }
                            }

                        }
                    }
                }

                if (!doubles) {
                    meta = add(meta, resultFromProducer);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }
        executorS.shutdown();
        sw.stop();
        logger.info("Global results retrieved in getEDGES " + sw.getTime() + "ms.");

        return meta;
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

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp bgp, Environment env) {
        ExecutorService executors = Executors.newCachedThreadPool();
        CompletionService<Mappings> completions = new ExecutorCompletionService<Mappings>(executors);
        List<Future<Mappings>> futures = new ArrayList<Future<Mappings>>();

        Mappings results = new Mappings();

        logger.info("Searching for BGP : " + bgp.toString());
        // sending BGP to relevant producers 
        for (Producer p : this.getProducers()) {
            //Checking indexes
            if (p instanceof RemoteProducerWSImpl) {
                RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                if (rp.checkBGP(bgp)) {
                    CallableGetBasicGraphPattern getBGP = new CallableGetBasicGraphPattern(p, gNode, from, bgp, env);
                    futures.add(completions.submit(getBGP));
                }
            }
        }

        StopWatch sw = new StopWatch();
        sw.start();

        //retrieving results
        for (Future<Mappings> f : futures) {
            try {
                //delete duplicates
                Mappings resFromProd = completions.take().get();
                resFromProd = resFromProd.minus(results);
                results.add(resFromProd);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }

        executors.shutdown();
        sw.stop();
        logger.info("Global results retrieved in getMappings " + sw.getTime() + "ms.");

        return results;
    }
}
