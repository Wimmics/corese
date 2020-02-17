package fr.inria.corese.core.load;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, INRIA 2020
 */
public class CreateTriple {

    private QueryProcess queryProcess;
    Load load;
    Graph graph;
    private String path;
    IDatatype dtpath;
    
    CreateTriple(){}

    CreateTriple(Graph g, Load ld) {
        load = ld;
        queryProcess = load.getQueryProcess();
        graph = g;
    }
    
    public void start() {
        declare();
        graph.getEventManager().start(Event.LoadAPI);
    }
    
    void declare() {
        dtpath = DatatypeMap.newResource(getPath()==null?Entailment.DEFAULT:getPath()); 
    }

    public void finish() {
        graph.getEventManager().finish(Event.LoadAPI);
    }

    void declare(Edge edge) {
        if (getQueryProcess() != null) {
            getQueryProcess().getCurrentVisitor().insert(dtpath, edge);
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

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
