package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.filter.Compile;
import java.util.ArrayList;
import java.util.List;

/**
 * Sort query edges to optimize query processing Including exists {} edges
 * Insert filters at place where variables are bound
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class QuerySorter implements ExpType {

    private boolean isSort = true;
    private boolean testJoin = false;

    private Sorter sort;
    private Query query;
    Compile compiler;
    private Producer prod;

    //todo assign sorter here
    QuerySorter(Query q) {
        query = q;
        compiler = new Compile(q);
        setSort(q);
    }
    
    void setSort(Query q) {
        switch (q.getPlanProfile()) {
            case Query.QP_HEURISTICS_BASED:
                sort = new SorterNew();
                break;
            default:
                sort = new Sorter();
        }
    }

    /**
     * Contract: For each BGP: Sort Query Edges Insert Filters at first place
     * where variables are bound This must be recursively done for exists {}
     * pattern in filter and bind including select, order by, group by and
     * having
     */
    void compile(Producer prod) {
        this.setProducer(prod);
        VString bound = new VString();
        compile(getQuery(), bound, false);
    }

    /**
     * Compile exists {} pattern when filter contains exists {}.
     */
    void compile(Filter f) {
        compile(f, new VString(), false);
    }

    /**
     * Compile additional filters that may contain exists {}.
     */
    void modifier(Query q) {
        compile(q.getSelectFun());
        compile(q.getOrderBy());
        compile(q.getGroupBy());
        if (q.getHaving() != null) {
            compile(q.getHaving().getFilter());
        }
        for (Filter f : q.getFunList()) {
            compile(f);
        }
    }

    /**
     * ***************************************************
     */
    /**
     * Recursively sort edges and filters edges are sorted wrt connection:
     * successive edges share variables if possible In each BGP, filters are
     * inserted at the earliest place where their variables are bound lVar is
     * the List of variables already bound
     */
    Exp compile(Exp exp, VString varList, boolean option) {
        int type = exp.type();
        switch (type) {

            case EDGE:
            case PATH:
            case XPATH:
            case EVAL:
            case NODE:
            case GRAPHNODE:
                break;

            case FILTER:
                // compile inner exists {} if any
                compile(exp.getFilter(), varList, option);
                break;

            case BIND:
                compile(exp.getFilter(), varList, option);
                break;

            case QUERY:
                // match query and subquery
                Query q = exp.getQuery();
                modifier(q);
                if (!varList.isEmpty()) {
                    // lVar = intersection(select variables,lVar)                   
                    varList = getSelectVariables(q, varList);
                }

            // continue with subquery body
            default:

                if (type == OPTIONAL || type == UNION || type == MINUS) {
                    option = true;
                }

               // Query planning
               if (isSort) {
                   exp = queryPlan(exp, varList);
               }

                int size = varList.size();

                // bind graph variable *after* exp sorting
                if (exp.isGraph() && exp.getGraphName().isVariable()) {
                    // GRAPH {GRAPHNODE NODE} {EXP}
                    Node gNode = exp.getGraphName();
                    varList.add(gNode.getLabel());
                }

                for (int i = 0; i < exp.size(); i++) {
                    Exp e = compile(exp.get(i), varList, option);
                    exp.set(i, e);
                    if (exp.isBGPAnd()) {
                        // add exp variable to list of bound variable
                        exp.get(i).addBind(varList);
                    }
                }

                varList.clear(size);

                if (testJoin && exp.type() == AND) {
                    // group in separate BGP statements that are not connected
                    // by variables
                    // generate a JOIN between BGP.
                    Exp res = exp.join();
                    if (res != exp) {
                        exp.getExpList().clear();
                        exp.add(res);
                    }
                }
        }
        InScopeNodes(exp);

        return exp;
    }
    
    VString getSelectVariables(Query q, VString lVar) {
        VString list = new VString();
        for (Exp ee : q.getSelectFun()) {
            Node node = ee.getNode();
            if (lVar.contains(node.getLabel())) {
                list.add(node.getLabel());
            }
        }
        return list;
    }

    /**
     * Sort exp statements
     */
    Exp queryPlan(Exp exp, VString lVar) {
        int num = exp.size();
        // identify remarkable filters such as ?x = <uri>
        // create OPT_BIND(?x = <uri>) store it in FILTER 
        List<Exp> lBind = findBindings(exp);
        if (exp.isBGPAnd()) {
            // sort edges wrt connection
            switch (getQuery().getPlanProfile()) {

                case Query.QP_T0:
                    sortFilter(exp, lVar);
                    break;

                case Query.QP_HEURISTICS_BASED:
                    sort = new SorterNew();
                    ((SorterNew) sort).sort(exp, lBind, getProducer(), getQuery().getPlanProfile());
                    setBind(getQuery(), exp);
                    break;

                case Query.QP_BGP:
                case Query.QP_DEFAULT:
                    // sort statements in connected order
                    sort.sort(getQuery(), exp, lVar, lBind);
                    // move filters
                    sortFilter(exp, lVar);
                    setBind(getQuery(), exp);
                    
                    if (getQuery().getPlanProfile() == Query.QP_BGP &&
                            getQuery().getBgpGenerator() != null) {
                        exp = getQuery().getBgpGenerator().process(exp);
                    }
                    break;
            }
            service(exp);
        }
        return exp;
    }
    /**
     * Compute and record list of inscope variables
     * that may be bound to evaluate exp in an optimized way
     */
    void InScopeNodes(Exp exp) {
         if (exp.isOptional()) {
            // A optional B
            // variables bound by A
            optional(exp);
        } 
        else if (exp.isMinus()) {
            minus(exp);
         }
        else if (exp.isUnion()) {
            union(exp);
        } else if (exp.isGraph()) {
            graph(exp);
        }
        else if (exp.isJoin()) {
            exp.bindNodes();
        }
    }
    
    void optional(Exp exp){
        exp.first().setNodeList(exp.getInScopeNodes());
        exp.optional();
    }
    
    // used by Eval subEval() bind()
    void union(Exp exp){
        exp.first().setNodeList(exp.first().getAllInScopeNodes());
        exp.rest().setNodeList(exp.rest().getAllInScopeNodes());
    }
    
    void minus(Exp exp){
        exp.first().setNodeList(exp.first().getInScopeNodes());
    }
    
    void graph(Exp exp) {
       exp.setNodeList(exp.getInScopeNodes());
    }
    
    void setBind(Query q, Exp exp){
        if (q.isUseBind()){
            exp.setBind();
        }
    }

    // put the binding variables to concerned edge
    void setBind(Exp exp, List<Exp> bindings) {
        for (Exp bid : bindings) {
            Node n = bid.get(0).getNode();
            if (bid.type() == OPT_BIND
                    // no bind (?x = ?y) in case of JOIN
                    && (!Query.testJoin || bid.isBindCst())) {

                for (Exp g : exp) {
                    if (((g.isEdge() || g.isPath()) && g.getEdge().contains(n))
                            && (bid.isBindCst() ? g.bind(bid.first().getNode()) : true)) {
                        if (g.getBind() == null) {
                            bid.status(true);
                            g.setBind(bid);
                        }
                    }
                }
            }
        }
    }

    boolean contains(Exp exp, Node n) {
        if (!exp.isEdge()) {
            return false;
        }
        return exp.getEdge().contains(n);
    }

    void compile(Filter f, VString lVar, boolean opt) {
        compile(f.getExp(), lVar, opt);
    }

    /**
     * Compile pattern of exists {} if any
     */
    void compile(Expr exp, VString lVar, boolean opt) {
        if (exp.oper() == ExprType.EXIST) {
            compile(getQuery().getPattern(exp), lVar, opt);
            if (getQuery().isValidate()) {
                System.out.println("QuerySorter exists: \n" + getQuery().getPattern(exp));
            }
        } else {
            for (Expr ee : exp.getExpList()) {
                compile(ee, lVar, opt);
            }
        }
    }

    void compile(List<Exp> list) {
        for (Exp ee : list) {
            // use case: group by (exists{?x :p ?y} as ?b)
            // use case: order by exists{?x :p ?y} 
            if (ee.getFilter() != null) {
                compile(ee.getFilter());
            }
        }
    }

    /**
     * Move filter at place where variables are bound in exp 
     * @varList: list of bound variables 
     * @todo: exists {} could be eval earlier
     */
    void sortFilter(Exp exp, VString varList) {
        int size = varList.size();
        List<String> filterVarList;
        List<Exp> done = new ArrayList<>();

        for (int indexFilter = exp.size() - 1; indexFilter >= 0; indexFilter--) {
            // reverse to get them in same order after placement

            Exp filterExp = exp.get(indexFilter);

            if (filterExp.isFilter() && !done.contains(filterExp)) {

                Filter filter = filterExp.getFilter();

                if (compiler.isLocal(filter)) {
                    // optional {} !bound()
                    // !bound() should not be moved
                    done.add(filterExp);
                    continue;
                }

                varList.clear(size);

                filterVarList = filter.getVariables();
                boolean isExist = filter.getExp().isRecExist();

                for (int indexExp = 0; indexExp < exp.size(); indexExp++) {
                    // search exp e after which filter f is bound
                    Exp e = exp.get(indexExp);

                    e.share(filterVarList, varList);
                    boolean bound = getQuery().bound(filterVarList, varList);

                    if (bound || (isExist && indexExp + 1 == exp.size())) {
                        // insert filter after exp
                        // an exist filter that is not bound is moved at the end because it 
                        // may bound its own variables.
                        if (bound && e.isOptional() && e.first().size() > 0) {
                            // add filter in first arg of optional
                            e.first().add(filter);
                        }
                        e.addFilter(filter);
                        done.add(filterExp);
                        if (indexFilter < indexExp) {
                            // filter is before, move it after
                            exp.remove(filterExp);
                            exp.add(indexExp, filterExp);
                        } else if (indexFilter > indexExp + 1) {
                            // put if just behind
                            exp.remove(filterExp);
                            exp.add(indexExp + 1, filterExp);
                            indexFilter++;
                        }

                        break;
                    }
                }
            }
        }
        varList.clear(size);
    }

    /**
     * Identify remarkable filter ?x < ?y ?x = ?y or ?x = cst or !bound() filter
     * is tagged
     */
    public List<Exp> findBindings(Exp exp) {
        for (Exp ee : exp) {
            if (ee.isFilter()) {
                compiler.process(getQuery(), ee);
            }
        }
        return exp.varBind();
    }

    
    public Sorter getSorter() {
        return sort;
    }

    
    public void setSorter(Sorter sort) {
        this.sort = sort;
    }

    /**
     * JOIN service
     */
    void service(Exp exp) {
        int hasService = 0;
        for (Exp ee : exp) {
            if (isService(ee)) {
                hasService += 1;
            }
        }
        if (hasService > 0) { 
            // replace: exp . service by: join(exp, service)
            service(exp, hasService);
        }
    }
    
    boolean isService(Exp exp) {
        switch (exp.type()) {
            case Exp.SERVICE: return true;
            case Exp.UNION:  return unionService(exp);
        }
        return false;
    }
    
    boolean unionService(Exp exp) {
        return bgpService(exp.get(0)) || bgpService(exp.get(1));
    }
    
     boolean bgpService(Exp exp) {
        for (Exp e : exp) {
            if (isService(e)) {
                return true;
            }                 
        }
        return false;
     }
    

    /**
     * For each service in exp 
     * replace: (ee service) by: join(ee, service) 
     */
    void service(Exp exp, int nbService) {

        if (nbService < 1 || (nbService == 1 && isService(exp.get(0)))) {
            // nothing to do
            return;
        }

        int count = 0;
        int i = 0;

        Exp and = Exp.create(Exp.AND);

        while (count < nbService) {
            // there are services

            while (!isService(exp.get(i))) {
                // find next service
                and.add(exp.get(i));
                exp.remove(i);
            }

            // exp.get(i) is a service
            count++;

            if (and.size() == 0) {
                and.add(exp.get(i));
            } else {
                Exp join = Exp.create(Exp.JOIN, and, exp.get(i));
                and = Exp.create(Exp.AND);
                and.add(join);
            }

            exp.remove(i);
        }

        while (exp.size() > 0) {
            // no more service
            and.add(exp.get(0));
            exp.remove(0);
        }
        
        for (Exp e : and) {
            exp.add(e);
        }
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Producer getProducer() {
        return prod;
    }

    public void setProducer(Producer prod) {
        this.prod = prod;
    }

    class VString extends ArrayList<String> {

        void clear(int size) {
            if (size == 0) {
                clear();
            } else {
                while (size() > size) {
                    remove(size() - 1);
                }
            }
        }

        @Override
        public boolean add(String var) {
            if (!contains(var)) {
                super.add(var);
            }
            return true;
        }
    }
}
