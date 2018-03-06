package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.corese.kgram.event.EventListener;
import fr.inria.corese.kgramenv.util.QueryExec;
import java.net.URL;

/**
 * Extension of the KGRAM SPARQL query evaluator, that handles several remote
 * KGRAM producers.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 *
 */
@Deprecated
public class QueryExecDQP extends QueryExec {

    public QueryExecDQP() {
        super();
    }

    public static QueryExecDQP create() {
        return new QueryExecDQP();
    }

    /**
     * Corese implementation
     */
    public static QueryExecDQP create(IEngine eng) {
        GraphEngine engine = (GraphEngine) eng;
        QueryExecDQP qe = new QueryExecDQP();
        qe.add(engine);
        return qe;
    }

    /**
     * Draft with several engine
     *
     * TODO: add is done in first engine (see constructor in set() )
     */
    @Override
    public void add(IEngine eng) {
        GraphEngine engine = (GraphEngine) eng;
        if (exec == null) {
            exec = QueryProcessDQP.create(engine.getGraph());
//                        exec.setListGroup(isListGroup);
            exec.setDebug(isDebug);
            for (EventListener el : list) {
                exec.addEventListener(el);
            }
        } else {
            exec.add(engine.getGraph());
        }
    }

    /**
     * Draft with several engine
     *
     * TODO: add is done in first engine (see constructor in set() )
     */
    public void addRemote(URL producerURL, WSImplem implem) {
        if (exec == null) {
            exec = new QueryProcessDQP(false);
            exec.setListGroup(isListGroup);
            exec.setDebug(isDebug);
            for (EventListener el : list) {
                exec.addEventListener(el);
            }
        }
        ((QueryProcessDQP) exec).addRemote(producerURL, implem);
    }

    public void addRemoteSQL(String producerURL, String driver, String login, String password) {
        if (exec == null) {
            exec = new QueryProcessDQP(false);
            exec.setListGroup(isListGroup);
            exec.setDebug(isDebug);
            for (EventListener el : list) {
                exec.addEventListener(el);
            }
        }
        ((QueryProcessDQP) exec).addRemoteSQL(producerURL, driver, login, password);
    }
}
