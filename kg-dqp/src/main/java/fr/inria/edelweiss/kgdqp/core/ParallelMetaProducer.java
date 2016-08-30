package fr.inria.edelweiss.kgdqp.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Meta Producer that handles concurent accesses to several Producers Uses a
 * generic MetaIterator that iterates over Producer iterators
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class ParallelMetaProducer extends MetaProducer {

    private final Logger logger = LogManager.getLogger(ParallelMetaProducer.class);

    protected ParallelMetaProducer() {
        super();
    }

    public static ParallelMetaProducer create() {
        return new ParallelMetaProducer();
    }

    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env) {
        ExecutorService exec = Executors.newCachedThreadPool();
        MetaIterator<Entity> meta = null;
        List<Future<Iterable<Entity>>> results = new ArrayList<Future<Iterable<Entity>>>();
        int count = 0;

        //TODO source ordering/selectivity

        //TODO subquery pipelining 

        //TODO parallelization of disjoint edges (one subquery for each BasicGraphPattern BGP)
        //BGPs are sequences of adjacent triple patterns. 
        boolean testBGPs = false;
        if (testBGPs) {
            ArrayList<BasicGraphPattern> ConnexeBGPs = new ArrayList<BasicGraphPattern>();
            Query fullQuery = env.getQuery();
            for (Exp exp : fullQuery.getBody().getExpList()) {
                BasicGraphPattern bgp = new BasicGraphPattern();
                if (exp.getEdge() != null) {
                    bgp.getEdges().add(exp.getEdge());
                }
                ArrayList<String> vars = new ArrayList<String>();
                exp.getVariables(vars);
                bgp.getVars().addAll(vars);
                ConnexeBGPs.add(bgp);
            }

            boolean activity = true;
            while (activity) {
                activity = false;
                for (BasicGraphPattern startingBgp : ConnexeBGPs) {
                    logger.debug("filling " + startingBgp.toString());
                    for (BasicGraphPattern bgp : ConnexeBGPs) {
                        for (String var : bgp.getVars()) {
                            if (startingBgp.getVars().contains(var)) {
                                for (Edge e : bgp.getEdges()) {
                                    //adding edge
                                    if (!startingBgp.getEdges().contains(e)) {
                                        logger.debug("with " + e.toString());
                                        startingBgp.getEdges().add(e);
                                        startingBgp.setProcessed(true);
                                        activity = true;
                                    }
                                }
                            }
                        }
                    }
                    logger.debug("filled " + startingBgp.toString());
                }

                for (BasicGraphPattern bgp : ConnexeBGPs) {
                    bgp.setProcessed(false);
                    for (Edge e : bgp.getEdges()) {
                        for (int i = 0; i < 2; i++) {
                            if ((e.getNode(i) != null) && e.getNode(i).isVariable()) {
                                if (!bgp.getVars().contains(e.getNode(i).getLabel())) {
                                    bgp.getVars().add(e.getNode(i).getLabel());
                                    bgp.setProcessed(true);
                                    activity = true;
                                    logger.debug("added variable : " + bgp.toString());
                                }
                            }
                        }
                    }
                }
            }

            //cleaning
            ArrayList<BasicGraphPattern> cleanedBGPs = new ArrayList<BasicGraphPattern>();
            for (BasicGraphPattern bgp : ConnexeBGPs) {
                if (!cleanedBGPs.contains(bgp)) {
                    cleanedBGPs.add(bgp);
                }
            }
            ConnexeBGPs = cleanedBGPs;
        }

//        logger.info("Current mappings: ");
//        if (env instanceof Memory) {
//            Memory mem = (Memory) env;
//            if (mem.current() != null) {
//                Mappings maps = mem.current();
//                for (Mapping m : maps) {
//                    for (int i = 0; i < m.getQueryNodes().length; i++) {
//                        String variable = m.getQueryNodes()[i].toString();
//                        String value = m.getNodes()[i].toString();
//                        logger.info("\t [" + variable + ":" + value + "]");
//                    }
//                }
//            }
//        }

        logger.info("Searching for edge : " + edge.toString());
        // iteration over 
        for (Producer p : this.getProducers()) {
            //TODO Check index
            CallableGetEdges getEdges = new CallableGetEdges(p, gNode, from, edge, env);
            results.add(exec.submit(getEdges));
            count++;
        }

        StopWatch sw = new StopWatch();
        sw.start();
        exec.shutdown();

        logger.info("Waiting for results");
        //synchronization barrier
        while (!exec.isTerminated()) {
        }

        //retrieving results
        for (Future<Iterable<Entity>> f : results) {
            try {
                if (f.get() != null) {
                    meta = add(meta, f.get());
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }
        sw.stop();
        logger.info("Global results retrieved in " + sw.getTime() + "ms.");

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
}
