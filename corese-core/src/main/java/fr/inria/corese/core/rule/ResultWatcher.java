package fr.inria.corese.core.rule;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Distinct;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.logic.RDFS;
import fr.inria.corese.core.query.Construct;
import java.util.ArrayList;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Watch kgram query solutions of rules for RuleEngine 1 Check that a solution
 * at loop n contains at least one edge deduced at loop n-1 (that is a new edge)
 * 2 Eliminate duplicate solution by a select distinct * on construct variables
 * 3 Create Edge directly (without Mapping created) Does not optimize with:
 * exists {} Property Path subquery
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class ResultWatcher implements ResultListener, GraphListener {

    // proportion of new edges under which we may consider only new edges
    public static double LIMIT = 0.2;
    // max sum of proportion of new edges
    public static double TOTAL = 0.3;

    int loop = 0, timestamp = 0;
    int cpos = 0, cneg = 0;
    int cnode = 0;
    // check that result have new target edge
    boolean selectNewResult = true;
    // check that (specific) query edge have new target edge
    boolean selectNewEdge = false;
    boolean start = true;
    private boolean isDistinct = true;
    //private boolean isSkipPath = false;
    private boolean test = false;
    int queryNewEdgeIndex = -1;

    // Factory to construct edge directly without intermediate Mapping
    private Construct construct;
    Mappings map;
    private Rule rule;
    private Graph graph;
    Distinct distinct;
    ArrayList<Edge> insertEdgeList;
    private boolean trace;
    private boolean performSelectNewEdge;
    boolean localSelectNewEdge = false;
    private boolean optimizeRuleDataManager = false;
    
    ResultWatcher(Graph g) {
        graph = g;
        insertEdgeList = new ArrayList<>();
    }

    public Distinct getDistinct() {
        return distinct;
    }

    void setConstruct(Construct c) {
        construct = c;
    }

    void setMappings(Mappings m) {
        map = m;
    }

    void setTimestamp(int n) {
        timestamp = n;
    }

    void start(int n) {
        loop = n;
    }

    void start(Rule r) {
        setRule(r);
        selectNewResult = true;
        selectNewEdge = false;
        start = true;
        performSelectNewEdge = false;
        localSelectNewEdge = false;
        init(r);
    }
    
    Query getQuery() {
        return getRule().getQuery();
    }

    /**
     * (1) If there is only one predicate in where with new edges (Only one
     * occurrence of this predicate in where) We can focus on these new edge
     * using listen(Edge, Entity) (2) If there are two predicates and the
     * proportion of new edges (wrt new edges in previous record) 
     * is small enough < 0.3 we will focus on new
     * triples using a specific Index of edges sorted by timestamp (see union())
     */
    void start(Record oldRecord, Record newRecord) {
        setTimestamp(oldRecord.getTimestamp());

        if (newRecord.nbNewPredicate() == 1
                && getQuery().nbPredicate(newRecord.getPredicate()) == 1) {
            queryNewEdgeIndex = getQuery().getEdge(newRecord.getPredicate()).getEdgeIndex();
            if (queryNewEdgeIndex != -1) {
                selectNewEdge = true;
            }
        } else if (loop > 0 && getQuery().getEdgeList() == null) {
            int n = 0;
            boolean ok = true;
            double tt = 0.0;
            // new edges <= 30% edges
            for (Node predicate : getRule().getPredicates()) {
                if (newRecord.get(predicate) > oldRecord.get(predicate)) {
                    n++;
                    // proportion of new edges for predicate wrt new edges in previous record
                    double dd = ((double) (newRecord.get(predicate) - oldRecord.get(predicate))) / (double) newRecord.get(predicate);
                    // sum of proportion of new edges
                    tt += dd;
                }
            }

            // 2 predicates with new edges and may be 1 a filter
            if (n <= 2 && tt < TOTAL) {
                Exp body = getQuery().getBody();
                int ne = 0, nf = 0;

                for (Exp exp : body) {
                    if (exp.isEdge() && exp.getEdge().getEdgeVariable() == null) {
                        ne++;
                    } else if (exp.isFilter()) {
                        nf++;
                    }
                }

                if (ne == 2 && nf <= 1) {
                    performSelectNewEdge = doit(true);
                }
            }
        }
    }

    /**
     * set up a distinct * on construct variables hence do not apply rule twice
     * on same solution
     */
    void init(Rule r) {
        r.getQuery().setEdgeList(null);
        List<Node> list = r.getQuery().getConstructNodes();
        if (list != null && !list.isEmpty()) {
            distinct = Distinct.create(list);
        }
    }

    void finish(Rule r) {
        distinct = null;
    }

    /**
     * sparql interpreter find a solution in env and call function process
     * Check that env contains
     * at least one new edge from preceding RuleEngine loop Check that this
     * solution is not duplicate: select distinct * on construct variables This
     * function is called by sparql just before returning a solution
     */
    @Override
    public boolean process(Environment env) {
        if (!selectNewResult) {
            return store(env);
        }

        if (loop == 0 || selectNewEdge) {
            return store(env);
        }

        for (Edge ent : env.getEdges()) {
            if (ent != null && ent.getEdgeIndex() >= timestamp) {
                return store(env);
            }
        }

        cneg += 1;
        return false;
    }

    // return false to skip optimization
    boolean doit(boolean b) {
        return b;
    }

    boolean store(Environment env) {
        if (isDistinct && distinct != null) {
            // select distinct * on construct variables
            if (!distinct.isDistinct(env)) {
                cneg += 1;
                return false;
            }
        }
        cpos += 1;
        if (getConstruct() == null) {
            // Mapping created by kgram
            return true;
        } else {
            // Construct create Edge directly
            // no intermediate Mapping created by sparql
            getConstruct().entailment(map, env);
            return false;
        }
    }

    @Override
    public boolean process(Path path) {
        return true;
    }

    @Override
    public boolean enter(Edge ent, Regex exp, int size) {
        return true;
    }

    @Override
    public boolean leave(Edge ent, Regex exp, int size) {
        return true;
    }

    /**
     * sparql interpreter call listen(exp, n) and may get an optimized
     * version of exp to evaluate
     * 
     */
    @Override
    public Exp listen(Exp exp, int n) {
        switch (exp.type()) {

            case Exp.PATH:
            case Exp.QUERY:
                selectNewResult = false;
                break;

            case Exp.UNION:
            case Exp.OPTION:
                // because we may not go through the branch with new edges
                // check new at the end as usual
                selectNewEdge = false;
                break;
        }

        if (n == 0 && exp.type() == Exp.AND) { // && ! isOptimizeRuleDataManager()) {

            if (getRule().isGTransitive() && getRule().getQuery().getEdgeList() != null) {
                // rule exp = where { ?p a owl:TransitiveProperty . ?x ?p ?y . ?y ?p ?z }
                // there is a list of new candidates for ?x ?p ?y
                // sparql skip first query edge: skip exp.get(0)
                exp = Exp.create(Exp.AND, exp.get(1), exp.get(2));
            } 
            else if (performSelectNewEdge) {
                performSelectNewEdge = false;
                if (isOptimizeRuleDataManager()) {
//                    exp = union(exp);
//                    RuleEngine.logger.info("Watcher local select new edge: "+exp);
//                    // local listen take care of it
//                    localSelectNewEdge = true;
                }
                else 
                    if (getGraph().hasRuleEdgeList()) {
                    // return an expression with 
                    // focus on new edges (in a specific graph Index sorted by edge timestamp)
                    exp = union(exp);
                    // ProducerImpl take care of it
                    localSelectNewEdge = false;
                }
            }
        }

        return exp;
    }
  
    /**
     * sparql check whether targetEdge is new
     */
    @Override
    public boolean listen(Exp exp, Edge queryEdge, Edge targetEdge) {
        if (selectNewEdge
                && queryEdge.getEdgeIndex() == queryNewEdgeIndex
                && targetEdge.getEdgeIndex() < timestamp) {
            return false;
        }
//        else if (localSelectNewEdge && exp.getLevel()!=-1) {
//            return targetEdge.getEdgeIndex() >= exp.getLevel();
//        }
        return true;
    }
    
    @Override
    public void listen(Expr exp) {
        switch (exp.oper()) {
            case ExprType.EXIST:
            case ExprType.UNNEST:
                selectNewResult = false;
        }
    }


    /**
     * rule exp = 2 edge (and may be 1 filter) 
     * return { getNew(e1) e2 } union {getNew(e2) e1 } 
     * where getNew(e) is a directive to Producer to focus on
     * new edges only new edges are those with level(e) >= ruleLoop There is a
     * Graph Index where edges are sorted by level
     */
    Exp union(Exp exp) {
        int fst = 0, snd = 1, flt = 2;
        if (exp.size() == 3 && exp.get(1).isFilter()) {
            snd = 2;
            flt = 1;
        }

        Exp e1 = Exp.create(Exp.EDGE, exp.get(fst).getEdge());
        e1.setLevel(timestamp);

        Exp a1 = Exp.create(Exp.AND, e1, exp.get(snd));
        if (exp.size() == 3) {
            a1.add(exp.get(flt));
        }

        Exp e2 = Exp.create(Exp.EDGE, exp.get(snd).getEdge());
        e2.setLevel(timestamp);

        Exp a2 = Exp.create(Exp.AND, e2, exp.get(fst));
        if (exp.size() == 3) {
            a2.add(exp.get(flt));
        }
        Exp ee = Exp.create(Exp.UNION, a1, a2);

        if (trace) {
            System.out.println("Compile: " + ee);
        }

        return ee;
    }

    public String toString() {
        return "positive: " + cpos + "\n"
                + "negative: " + cneg;
    }

    /**
     * *******************************************************************
     *
     *
     */
    Exp compile(Exp exp) {
        Exp e1 = Exp.create(Exp.EDGE, exp.get(1).getEdge());
        Exp e2 = Exp.create(Exp.EDGE, exp.get(0).getEdge());
        Exp ee = Exp.create(Exp.AND, e1, e2);
        exp.get(0).setIndex(timestamp);
        e1.setIndex(timestamp);
        Exp union = Exp.create(Exp.UNION, exp, ee);
        return union;
    }

    /**
     * ************************************************************
     *
     * GraphListener: deprecated, not used
     *
     ************************************************************
     */
    public void clear() {
        insertEdgeList.clear();
    }

    public List<Edge> getInsertEdgeList() {
        return insertEdgeList;
    }

    @Override
    public void addSource(Graph g) {
    }

    @Override
    public boolean onInsert(Graph g, Edge ent) {
        return true;
    }

    @Override
    public void insert(Graph g, Edge ent) {
        if (ent.getEdgeLabel().equals(RDFS.SUBCLASSOF)) {
            getInsertEdgeList().add(ent);
        }
    }

    @Override
    public void delete(Graph g, Edge ent) {
    }

    @Override
    public void start(Graph g, Query q) {
    }

    @Override
    public void finish(Graph g, Query q, Mappings m) {
    }

    @Override
    public void load(String path) {
    }

    
//    public boolean isSkipPath() {
//        return isSkipPath;
//    }
//
//   
//    public void setSkipPath(boolean isSkipPath) {
//        this.isSkipPath = isSkipPath;
//    }

    
    public boolean isDistinct() {
        return isDistinct;
    }

    
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
    }

   
    public boolean isTest() {
        return test;
    }

    
    public void setTest(boolean test) {
        this.test = test;
    }

   
    public boolean isTrace() {
        return trace;
    }

    
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean isNew() {
        return selectNewEdge;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Construct getConstruct() {
        return construct;
    }

    public boolean isOptimizeRuleDataManager() {
        return optimizeRuleDataManager;
    }

    public void setOptimizeRuleDataManager(boolean optimizeRuleDataManager) {
        this.optimizeRuleDataManager = optimizeRuleDataManager;
    }

}
