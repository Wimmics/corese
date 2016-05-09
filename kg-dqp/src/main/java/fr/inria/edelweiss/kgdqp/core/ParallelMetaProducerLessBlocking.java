package fr.inria.edelweiss.kgdqp.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import static fr.inria.edelweiss.kgram.api.core.ExpType.EDGE;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import java.util.HashMap;
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
 * @author Abdoul Macina, macina@i3s.unice.fr
 */
public class ParallelMetaProducerLessBlocking extends MetaProducer {

    private final Logger logger = Logger.getLogger(ParallelMetaProducerLessBlocking.class);
    private final HashMap<String, ArrayList<Producer>> bookKeeping = new HashMap<String, ArrayList<Producer>>();
    private final ArrayList<Exp> processedBGP = new ArrayList<Exp>();
    private ArrayList<Producer> sameProducers = new ArrayList<Producer>();

    protected ParallelMetaProducerLessBlocking() {
        super();
    }

    public static ParallelMetaProducerLessBlocking create() {
        return new ParallelMetaProducerLessBlocking();
    }

    @Override
    public Iterable<Entity> getEdges(Node gNode, List<Node> from, Edge edge, Environment env) {
        ExecutorService executors = Executors.newCachedThreadPool();
        CompletionService<Result> completions = new ExecutorCompletionService<Result>(executors);

        MetaIterator<Entity> meta = new MetaIterator<Entity>(new ArrayList<Entity>());
        List<Future<Result>> futures = new ArrayList<Future<Result>>();

        logger.info("Searching for edge : " + edge.toString());

        Memory memory = (Memory) env;
        //BGP mode
//        if (memory.getCurrentAndLockExpression() != null) {
         if (env.getQuery().getEdgeAndContext().containsKey(edge)) {
//            boolean isLastEdge = env.getExp().equals(memory.getCurrentAndLockExpression().getExpList().get(memory.getCurrentAndLockExpression().getExpList().size() - 1));
             Exp currentAnd = env.getQuery().getEdgeAndContext().get(edge);
             boolean isLastEdge = env.getExp().equals(currentAnd.getExpList().get(currentAnd.getExpList().size() - 1));

             //to handle previous AND already  processed by BGP
            boolean edgesFromSameSources = false;
            if (isLastEdge) {
                edgesFromSameSources = sameSource(env);
            }

            for (Producer p : this.getProducers()) {
                if (p instanceof RemoteProducerWSImpl) {
                    RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                    if (rp.checkEdge(edge)) {
                        //Not yet the last edge of AND Lock, AND does not have <=> BGP, previous edges are not from the same source
                        if ((!isLastEdge) || (!isAlreadyProcessed(env,currentAnd)) || (!edgesFromSameSources)) {
                            CallableResult getEdges = new CallableResult(p, gNode, from, Exp.create(EDGE, edge), env);
                            futures.add(completions.submit(getEdges));
                        } 
                        else {
                            //check if the current producer is in the list of sameProduers then no need to send this edge
                            //bbecause already done by the equivalent BGP
                            if (!sameProducers.contains(p)) {
                                CallableResult getEdges = new CallableResult(p, gNode, from, Exp.create(EDGE, edge), env);
                                futures.add(completions.submit(getEdges));
                            }
                        }
                    }
                }
            }
            sameProducers.clear();
        } //Edges mode
        else {
            for (Producer p : this.getProducers()) {
                if (p instanceof RemoteProducerWSImpl) {
                    RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                    if (rp.checkEdge(edge)) {
                        CallableResult getEdges = new CallableResult(p, gNode, from, Exp.create(EDGE, edge), env);
                        futures.add(completions.submit(getEdges));
                    }
                }

            }
        }

        StopWatch sw = new StopWatch();
        sw.start();

        //retrieving results
        for (Future<Result> future : futures) {
            try {
                Result res = completions.take().get();
                Iterable<Entity> resultFromProducer = res.getEntities();

                //delete duplicates
                Iterator<Entity> it = resultFromProducer.iterator();
                ArrayList<Entity> cleanedResults = new ArrayList<Entity>();
                boolean duplicated = false;
                while (it.hasNext()) {
                    Entity ent = it.next();
                    if (ent != null) {
                        Edge ed = ent.getEdge();
                        if (meta != null) {
                            Iterator<Entity> itt = meta.iterator();
                            while (itt.hasNext() && !duplicated) {
                                Entity entt = itt.next();
                                Edge edd = entt.getEdge();
                                if (ed.getNode().equals(edd.getNode())) {
                                    duplicated = true;
                                }
                            }

                        }

                        //save in book-keeping
                        for (int i = 0; i < ent.nbNode(); i++) {
                            if (bookKeeping.containsKey(ent.getNode(i).getValue().toString())) {
                                ArrayList<Producer> p = bookKeeping.get(ent.getNode(i).getValue().toString());
                                if (!p.contains(res.getProducer())) {
                                    p.add(res.getProducer());
                                }
                                bookKeeping.put(ent.getNode(i).getValue().toString(), p);

                            } else {
                                ArrayList<Producer> p = new ArrayList<Producer>();
                                p.add(res.getProducer());
                                bookKeeping.put(ent.getNode(i).getValue().toString(), p);
                            }
                        }
                        
                        //When the predicate is a variable add its values too
                         if(edge.getEdgeVariable()!=null){
                            if (bookKeeping.containsKey(ed.getEdgeNode().toString())) {
                                ArrayList<Producer> p = bookKeeping.get(ed.getEdgeNode().toString());
                                if (!p.contains(res.getProducer())) {
                                    p.add(res.getProducer());
                                }
                                bookKeeping.put(ed.getEdgeNode().toString(), p);

                            } else {
                                ArrayList<Producer> p = new ArrayList<Producer>();
                                p.add(res.getProducer());
                                bookKeeping.put(ed.getEdgeNode().toString(), p);
                            } 
                         }
                        
                        //delete the already obtained results to avoid redundancy in the final result
                        if (!duplicated) {
                            cleanedResults.add(ent);
                        } else {
                            duplicated = false;
                        }
                    }
                }

                meta = add(meta, cleanedResults);
//                 meta = add(meta, resultFromProducer);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (ExecutionException ex) {
                ex.printStackTrace();
            }
        }
        executors.shutdown();
        sw.stop();
        logger.info("Global results retrieved in getEdges " + sw.getTime() + "ms.");
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
        CompletionService<Result> completions = new ExecutorCompletionService<Result>(executors);
        List<Future<Result>> futures = new ArrayList<Future<Result>>();

        Mappings results = new Mappings();
        logger.info("Searching for BGP : " + bgp);

        // sending BGP to relevant producers 
        for (Producer p : this.getProducers()) {
            if (p instanceof RemoteProducerWSImpl) {
                RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                //Checking index
                if (rp.checkBGP(bgp)) {
                    CallableResult getBGP = new CallableResult(p, gNode, from, bgp, env);
                    futures.add(completions.submit(getBGP));

                    //save into processed BGP
                    if (!processedBGP.contains(bgp)) {
                        processedBGP.add(bgp);
                    }
                }
            }
        }

        StopWatch sw = new StopWatch();
        sw.start();

        //retrieving results
        for (Future<Result> future : futures) {
            try {
                Result res = completions.take().get();
                Mappings resFromProd = res.getMappings();

                //delete duplicates
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

    /**
     * 
     * 
     * @param env
     * @return 
     */
    public boolean isAlreadyProcessed(Environment env, Exp currentAnd) {
//        Memory memory = (Memory) env;
//        Exp and = memory.getCurrentAndLockExpression();
        Exp and = currentAnd;
        for (Exp e : processedBGP) {
             if (compareCurrentANDBGP(e, and)) {
                    return true;
             }
        }
        return false;
    }

    /**
     * 
     * @param bgp
     * @param and
     * @return 
     */
    public boolean compareCurrentANDBGP(Exp bgp, Exp and) {
        boolean result = true;
        if (((bgp.getExpList().size() == and.getExpList().size()))) {
            for (int i = 0; i < bgp.getExpList().size() && result; i++) {
                if(bgp.getExpList().get(i).isEdge()){
                    result = result && bgp.getExpList().get(i).getEdge().equals(and.getExpList().get(i).getEdge());
                }
            }
            return result;
        }
        return false;
    }

    /**
     * 
     * @param env
     * @return 
     */
    public boolean sameSource(Environment env) {
        boolean result = true;
        Memory memory = (Memory) env;
        Node[] nodes = memory.getNodes();
        ArrayList<Producer> tmp = new ArrayList<Producer>();
        int j = 0;
        for (int i = 0; nodes[i] != null && nextBoundNode(i, nodes)  < nodes.length && result; i=j) {
            j = nextBoundNode(i, nodes);
            ArrayList<Producer> p1 = bookKeeping.get(nodes[i].getValue().toString());
            ArrayList<Producer> p2 = bookKeeping.get(nodes[j].getValue().toString());

            if (tmp.isEmpty()) {
                tmp = (ArrayList<Producer>) p1.clone();
            }
            tmp = intersection(tmp, p2);
            result = !tmp.isEmpty();
        }
        
        if (!tmp.isEmpty()) {
            sameProducers = tmp;
        }
        return result;
    }

    /**
     * 
     * @param previous
     * @param next
     * @return 
     */
    public ArrayList<Producer> intersection(ArrayList<Producer> previous, ArrayList<Producer> next) {
        ArrayList<Producer> tmp = new ArrayList<Producer>();
        for (Producer p : previous) {
            if (next.contains(p)) {
                tmp.add(p);
            }
        }
        return tmp;
    }
    /**
     * 
     * @param i
     * @param nodes
     * @return 
     */
    private int nextBoundNode(int i, Node[] nodes) {
        while(i+1<nodes.length && nodes[i+1]==null)
            i++;
        return i+1;
    }
}
