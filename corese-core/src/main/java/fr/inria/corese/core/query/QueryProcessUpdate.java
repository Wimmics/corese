package fr.inria.corese.core.query;

import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.core.query.update.UpdateProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.query.update.ManagerImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Update;
import java.util.List;

/**
 * SPARQL Update
 * 
 * @author Olivier Corby, INRIA, I3S 2020
 */
public class QueryProcessUpdate { 
    
    static boolean reentrant = false;
    private boolean debug = false;
    private QueryProcess exec;
    
    QueryProcessUpdate(QueryProcess e) {
        exec = e;
    }
    
    Graph getGraph() {
        return getQueryProcess().getGraph();
    }

    Mappings synUpdate(Query query, Mapping m, Dataset ds) throws EngineException {
        if (isReentrant()) {
            Mappings map = reentrant(query, m, ds);
            if (map != null) {
                return map;
            }
        }

        Graph g = getGraph();
        GraphListener gl = (GraphListener) query.getPragma(Pragma.LISTEN);

        try {
            getQueryProcess().syncWriteLock(query);
            if (gl != null) {
                g.addListener(gl);
            }
            if (query.isRule()) {
                return rule(query);
            } else {
                return update(query, m, ds);
            }
        } finally {
            if (gl != null) {
                g.removeListener(gl);
            }
            getQueryProcess().syncWriteUnlock(query);
        }
    }
    
    /**
     * reentrant mode enables an update query during a select query 
     * update performed on an external named graph, created if needed
     */
    Mappings reentrant(Query query, Mapping m, Dataset ds) throws EngineException {
        String name = getWithName(query);
        if (name != null && isExternal(name)) {
            // get/create a graph and store it as named graph
            // with name insert where
            // insert data { graph name }
            // drop graph name
            return overWrite(name, query, m, ds, true);
        }
        name = getDeleteInsertName(query);
        // insert { graph name {} } where {}
        // consider name as external graph
        // eval where {} on std graph
        if (name != null && isExternal(name)) {
            return overWrite(name, query, m, ds, false);
        }
        return null;
    }
    
    
     /**
     * from and named (if any) specify the Dataset over which update take place
     * where {} clause is computed on this Dataset delete {} clause is computed
     * on this Dataset insert {} take place in Entailment.DEFAULT, unless there
     * is a graph pattern or a with
     *
     * This explicit Dataset is introduced because Corese manages the default
     * graph as the union of named graphs whereas in some case (W3C test case,
     * protocol) there is a specific default graph hence, ds.getFrom()
     * represents the explicit default graph
     *
     */
    Mappings update(Query query, Mapping m, Dataset ds) throws EngineException {
        ASTQuery ast = getAST(query);
        // Mapping m may carry a specific Visitor (see Cleaner)
        // init() set this Visitor as current visitor
        getQueryProcess().init(query, m);
        getEventManager().start(Event.Update, ast);
        // record current Visitor because 
        // 1) it may contain event function definitions
        // 2) it may be changed by update subqueries event function e.g. @start
        ProcessVisitor vis = getCurrentEval().getVisitor();
        beforeUpdate(vis, query, isSynchronized());

        if (ds != null && ds.isUpdate()) {
            // TODO: check complete() -- W3C test case require += default + entailment + rule
            getQueryProcess().complete(ds);
        }
        UpdateProcess up = UpdateProcess.create(getQueryProcess(), createUpdateManager(getGraph()), ds);
        up.setDebug(isDebug());
        Mappings map = up.update(query, m);

        afterUpdate(vis, map, isSynchronized());
        getEventManager().finish(Event.Update, ast);
        return map;
    }


    /**
     * Create a new graph where to perform update Store it as named graph of
     * main dataset 
     * toName = true: execute whole query on external named graph
     * toName = false: execute where on std graph, execute insert/delete on
     * external graph in this case, Construct setGraphManager processes external
     * graph
     */
    Mappings overWrite(String name, Query query, Mapping m, Dataset ds, boolean toName) throws EngineException {
        Graph g = getGraph();
        Graph gg = g.getNamedGraph(name);
        if (gg == null) {
            gg = GraphStore.create();
            synchronized (g) {
                g.setNamedGraph(name, gg);
            }
            gg.setNamedGraph(Context.STL_DATASET, g);
            if (g.isVerbose()) {
                gg.setVerbose(true);
            }
        }

        synchronized (g) {
            gg.shareNamedGraph(g);
        }
        Graph targetGraph = (toName) ? gg : g;
        QueryProcess exec = QueryProcess.create(targetGraph);
        // bypass lock if any
        Mappings map = exec.getQueryProcessUpdate().update(query, m, ds);
        if (gg.isVerbose()) {
            System.out.println("reentrant named graph: " + name + " " + toName);
            System.out.println(gg.getNames());
            System.out.println(gg);
        }
        //g.setNamedGraph(name, gg);
        complete(exec.getAST(query).getUpdate(), g);
        return map;
    }

    void complete(ASTUpdate up, Graph g) {
        String name = up.getGraphName();
        for (Update act : up.getUpdates()) {
            switch (act.type()) {
                case Basic.DROP:
                    if (act.getBasic().getGraph() != null && act.getBasic().getGraph().equals(name)) {
                        g.setNamedGraph(name, null);
                        return;
                    }
            }
        }
    }
    
    EventManager getEventManager() {
        return getQueryProcess().getEventManager();
    }
    
    ASTQuery getAST(Query q) {
        return getQueryProcess().getAST(q);
    }
    
    boolean isSynchronized() {
        return getQueryProcess().isSynchronized();
    }
    
    void setSynchronized(boolean b) {
        getQueryProcess().setSynchronized(b);
    }
    
    Eval getCurrentEval() {
        return getQueryProcess().getCurrentEval();
    }
    
    ProcessVisitor getCurrentVisitor() {
        return getQueryProcess().getCurrentVisitor();
    }

    String getWithName(Query query) {
        String name = getAST(query).getUpdate().getGraphName();
        if (isDebug()) {
            System.out.println("QP: update reentrant with graph name: " + name);
        }
        return name;
    }

    String getDeleteInsertName(Query query) {
        String name = getAST(query).getUpdate().getGraphNameDeleteInsert();
        if (isDebug()) {
            System.out.println("QP: update reentrant del/ins graph name: " + name);
        }
        return name;
    }

    // place holder to have specific external URI
    boolean isExternal(String name) {
        return true;
    }

    /**
     * Compute a construct where query considered as a (unique) rule Syntax:
     * rule construct {} where {}
     */
    Mappings rule(Query q) {
        RuleEngine re = RuleEngine.create(getGraph());
        re.setDebug(isDebug());
        re.defRule(q);
        getGraph().process(re);
        return Mappings.create(q);
    }


    ManagerImpl createUpdateManager(Graph g) {
        GraphManager man = new GraphManager(g);
        man.setQueryProcess(getQueryProcess());
        return new ManagerImpl(man);
    }

   
    // bypass lock in case method beforeUpdate perform select query
    void beforeUpdate(ProcessVisitor vis, Query q, boolean b) {
        setSynchronized(true);
        vis.beforeUpdate(q);
        setSynchronized(b);
    }

    void afterUpdate(ProcessVisitor vis, Mappings m, boolean b) {
        setSynchronized(true);
        vis.afterUpdate(m);
        setSynchronized(b);
    }

    /**
     * Save and restore current eval because function beforeLoad() may execute a
     * query and hence create a new eval and change the Visitor
     */
    public void beforeLoad(IDatatype dt, boolean b) {
        setSynchronized(true);
        Eval eval = getCurrentEval();
        getCurrentVisitor().beforeLoad(dt);
        getQueryProcess().setEval(eval);
        setSynchronized(b);
    }

    public void afterLoad(IDatatype dt, boolean b) {
        setSynchronized(true);
        Eval eval = getCurrentEval();
        getCurrentVisitor().afterLoad(dt);
        getQueryProcess().setEval(eval);
        setSynchronized(b);
    }

//    public void update(Query q, List<Edge> del, List<Edge> ins) {
//        //System.out.println("QPU: " + getCurrentVisitor());
//        getCurrentVisitor().update(q, del, ins);
//    } 

    static boolean isReentrant() {
        return QueryProcess.isReentrant();
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the exec
     */
    public QueryProcess getQueryProcess() {
        return exec;
    }

    /**
     * @param exec the exec to set
     */
    public void setQueryProcess(QueryProcess exec) {
        this.exec = exec;
    }

}
