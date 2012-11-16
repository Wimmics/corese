package fr.inria.edelweiss.kgdqp.core;

import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.filter.Interpreter;
import fr.inria.edelweiss.kgram.tool.MetaProducer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import java.net.URL;

/**
 * Extension of the KGRAM SPARQL query processor, that handles several 
 * concurrent producers through a parallel meta producer.
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
public class QueryProcessDQP extends QueryProcess {

    private final int parallelWaitMP = 0;
    private final int parallelLessWaitMP = 1;
    private final int pipelinedMP = 2;
    
    public QueryProcessDQP() {
        super();
    }

    public QueryProcessDQP(Producer p, Evaluator e, Matcher m) {
        super(p, e, m);
    }

    public void addRemote(URL url) {
//        add(new RemoteProducerHTTPImpl(url));
        add(new RemoteProducerWSImpl(url));
    }
    
    public void addRemoteSQL(String url, String driver, String login, String password) {
        add(new RemoteSqlProducerImpl(url, driver, login, password));
    }

    public static QueryProcessDQP create(Graph g) {
        ProducerImpl p = ProducerImpl.create(g);
        QueryProcessDQP exec = QueryProcessDQP.create(p);
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

    public static QueryProcessDQP create(Producer prod, Matcher match) {
        Interpreter eval = createInterpreter(prod, match);
        QueryProcessDQP exec = new QueryProcessDQP(prod, eval, match);
        return exec;
    }

    public static QueryProcessDQP create(Producer prod, Evaluator ev, Matcher match) {
        QueryProcessDQP exec = new QueryProcessDQP(prod, ev, match);
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
