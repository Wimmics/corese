package fr.inria.corese.core.load;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * @author Olivier Corby, INRIA 2020
 */
public class CreateTriple {

    private QueryProcess queryProcess;
    Load load;
    Graph graph;
    
    CreateTriple(){}

    CreateTriple(Graph g, Load ld) {
        load = ld;
        queryProcess = load.getQueryProcess();
        graph = g;
    }
    
    public void start() {
        graph.getEventManager().start(Event.LoadAPI);
    }

    public void finish() {
        graph.getEventManager().finish(Event.LoadAPI);
    }

    void declare(Edge edge) {
        if (getQueryProcess() != null) {
            getQueryProcess().getCurrentVisitor().insert(edge);
        }
    }

    /**
     * @return the queryProcess
     */
    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    /**
     * @param queryProcess the queryProcess to set
     */
    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

}
