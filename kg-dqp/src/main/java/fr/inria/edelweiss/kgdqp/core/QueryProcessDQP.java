package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgdqp.strategies.ServiceGrouper;
import fr.inria.edelweiss.kgram.api.query.DQPFactory;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.BgpGenerator;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgenv.eval.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.MatcherImpl;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Extension of the KGRAM SPARQL query processor, that handles several
 * concurrent producers through a parallel meta producer.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class QueryProcessDQP extends QueryProcess {

    private static Logger logger = LogManager.getLogger(QueryProcessDQP.class);
    private final int parallelWaitMP = 0;
    private final int parallelLessWaitMP = 1;
    private final int pipelinedMP = 2;

    private boolean provEnabled = false;
    private boolean groupingEnabled = false;

//    private static Graph provGraph = Graph.create();
//    public static QueryProcess provQP = QueryProcess.create(provGraph);
    // several cost criterions
    // for each sent query, record the number of invocations
    public static ConcurrentHashMap<String, Long> queryCounter = new ConcurrentHashMap<String, Long>();
    // for each sent query, record the number of transmetted results
    public static ConcurrentHashMap<String, Long> queryVolumeCounter = new ConcurrentHashMap<String, Long>();
    // for each source, record the number of sent queries
    public static ConcurrentHashMap<String, Long> sourceCounter = new ConcurrentHashMap<String, Long>();
    // for each source, record the number of sent queries
    public static ConcurrentHashMap<String, Long> sourceVolumeCounter = new ConcurrentHashMap<String, Long>();
    
    static {
        Query.setFactory(new DQPFactory() {

            @Override
            public BgpGenerator instance() {
                return new BgpGeneratorImpl();
            }
        });
    }

    public QueryProcessDQP(boolean provEnabled) {
        super();
        this.provEnabled = provEnabled;
    }

    public QueryProcessDQP(Producer p, Evaluator e, Matcher m, boolean provEnabled) {
        super(p, e, m);
        this.provEnabled = provEnabled;
    }

    public void setProvEnabled(boolean provEnabled) {
        this.provEnabled = provEnabled;
        if (producer instanceof MetaProducer) {
            MetaProducer meta = (MetaProducer) producer;
            for (Producer p : meta.getProducers()) {
                if (p instanceof RemoteProducerWSImpl) {
                    RemoteProducerWSImpl rp = (RemoteProducerWSImpl) p;
                    rp.setProvEnabled(provEnabled);
                }
            }
        }
    }

    public boolean isProvEnabled() {
        return provEnabled;
    }

    public boolean isGroupingEnabled() {
        return groupingEnabled;
    }

    /**
     *
     * @param groupingEnabled
     * @return
     */
    public ServiceGrouper setGroupingEnabled(boolean groupingEnabled) {
        this.groupingEnabled = groupingEnabled;
        if (groupingEnabled) {
            ServiceGrouper sg = new ServiceGrouper(this);
            this.setVisitor(new ServiceGrouper(this));
            return sg;
        } else {
            return null;
        }
    }

    public void addRemote(URL url, WSImplem implem) {
        if (implem.equals(WSImplem.REST)) {
            add(new RemoteProducerWSImpl(url, implem, this.isProvEnabled()));
        } else {
            logger.error("SOAP web services not supported anymore for remote communications with "+url);
        }
    }

    public void addRemoteSQL(String url, String driver, String login, String password) {
        add(new RemoteSqlProducerImpl(url, driver, login, password));
    }

    public static QueryProcessDQP create(Graph g) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = create(p);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, boolean provEnabled) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = create(p, provEnabled);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, Provider serviceProvider) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = create(p);
        exec.set(serviceProvider);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, Provider serviceProvider, boolean provEnabled) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = create(p, provEnabled);
        exec.set(serviceProvider);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, Graph g2) {
        QueryProcessDQP qp = create(g);
        qp.add(g2);
        return qp;
    }

    public static QueryProcessDQP create(ProducerImpl prod) {
        Matcher match = MatcherImpl.create(prod.getGraph());
        QueryProcessDQP exec = create(prod, match);
        return exec;
    }

    public static QueryProcessDQP create(ProducerImpl prod, boolean provEnabled) {
        Matcher match = MatcherImpl.create(prod.getGraph());
        QueryProcessDQP exec = create(prod, match, provEnabled);
        return exec;
    }

    public static QueryProcessDQP create(Producer prod, Matcher match) {
        Interpreter eval = createInterpreter(prod, match);
        QueryProcessDQP exec = new QueryProcessDQP(prod, eval, match, false);
        return exec;
    }

    public static QueryProcessDQP create(Producer prod, Matcher match, boolean provEnabled) {
        Interpreter eval = createInterpreter(prod, match);
        QueryProcessDQP exec = new QueryProcessDQP(prod, eval, match, provEnabled);
        return exec;
    }

    public static QueryProcessDQP create(Producer prod, Evaluator ev, Matcher match, boolean provEnabled) {
        QueryProcessDQP exec = new QueryProcessDQP(prod, ev, match, provEnabled);
        return exec;
    }

    @Override
    public void add(Producer prod) {
//        int implem = parallelWaitMP;
        int implem = parallelLessWaitMP;
//        int implem = pipelinedMP;

        if (implem == parallelWaitMP) {
            ParallelMetaProducer meta;
            if (producer instanceof MetaProducer) {
                meta = (ParallelMetaProducer) producer;
            } else {
                meta = ParallelMetaProducer.create();
                if (producer != null) {
                    meta.add(producer);
                }
                producer = meta;
            }
            meta.add(prod);
        } else if (implem == parallelLessWaitMP) {
            ParallelMetaProducerLessBlocking meta;
            if (producer instanceof MetaProducer) {
                meta = (ParallelMetaProducerLessBlocking) producer;
            } else {
                meta = ParallelMetaProducerLessBlocking.create();
                if (producer != null) {
                    meta.add(producer);
                }
                producer = meta;
            }
            meta.add(prod);
        } else if (implem == pipelinedMP) {
            PipelinedMetaProducer meta;
            if (producer instanceof MetaProducer) {
                meta = (PipelinedMetaProducer) producer;
            } else {
                meta = PipelinedMetaProducer.create();
                if (producer != null) {
                    meta.add(producer);
                }
                producer = meta;
            }
            meta.add(prod);
        }
    }

    
    public synchronized  static void  updateCounters(String query, String endpoint, boolean notEmptyResult, Long size){
                //count number of queries
                if (queryCounter.containsKey(query)) {
                    Long n = queryCounter.get(query);
                    queryCounter.put(query, n + 1L);
                } else {
                    queryCounter.put(query, 1L);
                }
//                 count number of source access
                if (sourceCounter.containsKey(endpoint)) {
                    Long n = sourceCounter.get(endpoint);
                    sourceCounter.put(endpoint, n + 1L);
                } else {
                    sourceCounter.put(endpoint, 1L);
                }

                if (notEmptyResult) {
                    if (queryVolumeCounter.containsKey(query)) {
                        Long n = queryVolumeCounter.get(query);
                        queryVolumeCounter.put(query, n + size);
                    } else {
                        queryVolumeCounter.put(query,  size);
                    }
                    if (sourceVolumeCounter.containsKey(endpoint)) {
                        Long n = sourceVolumeCounter.get(endpoint);
                        sourceVolumeCounter.put(endpoint, n +  size);
                    } else {
                        sourceVolumeCounter.put(endpoint,  size);
                    }
                }
    }
}
