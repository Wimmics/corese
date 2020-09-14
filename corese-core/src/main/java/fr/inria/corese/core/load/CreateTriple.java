package fr.inria.corese.core.load;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, INRIA 2020
 */
public class CreateTriple {
    static final String STAR = "*";

    private QueryProcess queryProcess;
    Load load;
    Graph graph;
    private String path;
    IDatatype dtpath;
    ArrayList<String> exclude;
    private boolean skip = false;
    private int limit = Integer.MAX_VALUE;
    int count = 1;

    
    CreateTriple(){}

    CreateTriple(Graph g, Load ld) {
        load = ld;
        queryProcess = load.getCreateQueryProcess();
        graph = g;
        exclude = new ArrayList<>();
    }
    
    public void start() {
        declare();
        graph.getEventManager().start(Event.LoadAPI);
    }
    
    Graph getGraph() {
        return graph;
    }
    
    void declare() {
        dtpath = DatatypeMap.newResource(getPath()==null?Entailment.DEFAULT:getPath()); 
    }

    public void finish() {
        graph.getEventManager().finish(Event.LoadAPI);
    }
    
    void add(Edge e) {
        if (AccessRight.isActive()) { 
            if (! getAccessRight().setInsert(e)) {
                return ;
            }
        }
        Edge edge = graph.addEdge(e);
        if (edge != null) {
            declare(edge);
        }
    }

    void declare(Edge edge) {
        if (load.isEvent() && getQueryProcess() != null) {
            getQueryProcess().getCurrentVisitor().insert(dtpath, edge);
        }
    }
    
//    void access(Edge edge) {
//        if (AccessRight.isActive()) {
//            getAccessRight().setInsert(edge);
//        }
//    }
    
    AccessRight getAccessRight() {
        return load.getAccessRight();
    }
    
    public void exclude(String ns) {
        if (ns == null) {
            exclude.clear();
        } else if (ns.equals(STAR)) {
            setSkip(true);
        } else {
            exclude.add(ns);
        }
    }
    
    public void exclude(List<String> list) {
        for (String name : list) {
            exclude(name);
        }
    }



    public boolean accept(String pred) {
        if (count > 100000) {
            graph.getEventManager().process(Event.LoadStep);
            count = 2;
        } else {
            count++;
        }
        if (isSkip() || graph.size() > limit) {
            return false;
        }
        if (exclude.isEmpty()) {
            return true;
        }
        for (String ns : exclude) {
            if (pred.startsWith(ns)) {
                return false;
            }
        }
        return true;
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

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * @param skip the skip to set
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }
    
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

}
