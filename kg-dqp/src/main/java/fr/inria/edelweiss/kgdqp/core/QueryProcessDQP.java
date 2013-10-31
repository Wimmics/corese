package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
 * Extension of the KGRAM SPARQL query processor, that handles several
 * concurrent producers through a parallel meta producer.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class QueryProcessDQP extends QueryProcess {

    private static Logger logger = Logger.getLogger(QueryProcessDQP.class);
    private final int parallelWaitMP = 0;
    private final int parallelLessWaitMP = 1;
    private final int pipelinedMP = 2;

    private boolean provEnabled = false;

//    private static Graph provGraph = Graph.create();
//    public static QueryProcess provQP = QueryProcess.create(provGraph);

    // several cost criterions
    // for each sent query, record the number of invocations
    public static ConcurrentHashMap<String, Long> queryCounter = new ConcurrentHashMap<String, Long>();
    // for each sent query, record the number of transmetted results
    public static ConcurrentHashMap<String, Long> queryVolumeCounter = new ConcurrentHashMap<String, Long>();
    // for each source, record the number of sent queries
    public static ConcurrentHashMap<String, Long> sourceCounter = new ConcurrentHashMap<String, Long>();

    public QueryProcessDQP(boolean provEnabled) {
        super();
        this.provEnabled = provEnabled;
    }

    public QueryProcessDQP(Producer p, Evaluator e, Matcher m, boolean provEnabled) {
        super(p, e, m);
        this.provEnabled = provEnabled;
    }

    public boolean isProvEnabled() {
        return provEnabled;
    }

    public void addRemote(URL url, WSImplem implem) {
        add(new RemoteProducerWSImpl(url, implem, this.isProvEnabled()));

        //provenance annotation
//        if (this.isProvEnabled()) {
//            String insertRemote = "PREFIX prov: <" + Util.provPrefix + "> insert data { <" + url.toString() + "> rdf:type prov:SoftwareAgent}";
//            try {
//                provQP.query(insertRemote);
//            } catch (EngineException ex) {
//                logger.error("Eror while inserting provenance: \n" + insertRemote);
//            }
//        }
    }

    public void addRemoteSQL(String url, String driver, String login, String password) {
        add(new RemoteSqlProducerImpl(url, driver, login, password));

        //provenance annotation
//        if (this.isProvEnabled()) {
//            String insertRemote = "PREFIX prov: <" + Util.provPrefix + "> insert data { <" + url.toString() + "> rdf:type prov:SoftwareAgent}";
//            try {
//                provQP.query(insertRemote);
//            } catch (EngineException ex) {
//                logger.error("Eror while inserting provenance: \n" + insertRemote);
//            }
//        }
    }

    public static QueryProcessDQP create(Graph g) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = QueryProcessDQP.create(p);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, boolean provEnabled) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = QueryProcessDQP.create(p, provEnabled);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, Provider serviceProvider) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = QueryProcessDQP.create(p);
        exec.set(serviceProvider);
        return exec;
    }
    
    public static QueryProcessDQP create(Graph g, Provider serviceProvider, boolean provEnabled) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = QueryProcessDQP.create(p, provEnabled);
        exec.set(serviceProvider);
        return exec;
    }

    public static QueryProcessDQP create(Graph g, Graph g2) {
        QueryProcessDQP qp = QueryProcessDQP.create(g);
        qp.add(g2);
        return qp;
    }

    public static QueryProcessDQP create(ProducerImpl prod) {
        Matcher match = MatcherImpl.create(prod.getGraph());
        QueryProcessDQP exec = QueryProcessDQP.create(prod, match);
        return exec;
    }

    public static QueryProcessDQP create(ProducerImpl prod, boolean provEnabled) {
        Matcher match = MatcherImpl.create(prod.getGraph());
        QueryProcessDQP exec = QueryProcessDQP.create(prod, match, provEnabled);
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

}
