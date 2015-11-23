package fr.inria.edelweiss.kgram.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Loopable;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.filter.Compile;
import fr.inria.edelweiss.kgram.filter.Extension;
import fr.inria.edelweiss.kgram.tool.Message;

/**
 * KGRAM Query
 * also used for subquery
 *
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class Query extends Exp implements Graphable, Loopable {

    public static final int QP_T0 = 0; //No QP settings
    public static final int QP_DEFAULT = 1; //Default Corese QP
    public static final int QP_HEURISTICS_BASED = 2;//Heuristics based QP
    public static final int QP_BGP = 3;//BGP based QP

    //used to set the default query plan method 
    public static int STD_PLAN = QP_DEFAULT;

    public static final int STD_PROFILE = -1;
    public static final int COUNT_PROFILE = 1;
    public static final int DEFAULT_SLICE = 20;

	
    private static Logger logger = Logger.getLogger(Exp.class);

    public static final String PATHNODE = "pathNode";
    public static final String BPATH = "_:_path_";

    public static boolean test = true;
    public static boolean testJoin = false;
    public static boolean isOptional = true;

    int limit = Integer.MAX_VALUE, offset = 0,
            // if slice > 0 : service gets mappings from previous pattern by slices
            slice = 20;

    private int number = 0;
    boolean distinct = false;
    int iNode = 0, iEdge = 0, iPath = 0;
    private int edgeIndex = -1;
    List<Node> from, named, selectNode;
    // all nodes (on demand)
    List<Node> // std patterns (including bindings) but  minus and exists (no select)
            patternNodes,
            // minus + exists (no select)
            queryNodes,
            // select nodes in std pattern
            patternSelectNodes,
            //  select nodes in minus and exists
            querySelectNodes,
            // final query bindings nodes
            bindingNodes;
    private List<Node> constructNodes;
    List<Node> relaxEdges;
    List<Exp> selectExp, selectWithExp, orderBy, groupBy;
    List<Filter> failure, pathFilter, funList;
    List<String> errors, info;
    Exp having, construct, delete;
	// gNode is a local graph node when subquery has no ?g in its select 
    // use case: graph ?g {{select where {}}}
    Node gNode, pathNode;
    // outer main query that contains this (when subquery)
    private Node provenance;
    // SPIN graph
    private Object graph;
    Query query, outerQuery;
    // for templates
    private Query templateProfile;
    Graphable ast;
    Object object;

    private Object pprinter;
    HashMap<String, Object> tprinter;
    Compile compiler;
    private QuerySorter querySorter;

    HashMap<String, Object> pragma;
    // Extended filters: pathNode()
    HashMap<String, Filter> ftable;
	// Extended queries for type check
    // nb occurrences of predicates in where
    HashMap<String, Integer> ptable;
    HashMap<String, Edge> etable;
    Hashtable<Edge, Query> table;
    // Extended queries for additional group by
    List<Query> queries;
    private Extension extension;

    private boolean isCompiled = false;
    private boolean hasFunctional = false;

    boolean isDebug = false, isCheck = false;
    private boolean isUseBind = true;

    boolean 
            isAggregate = false, isFunctional = false, isRelax = false,
            isDistribute = false,
            isOptimize = false,
            isTest = false,
            // sort edges to be connected
            isSort = true, isConstruct = false,
            isDelete = false, isUpdate = false, // true:  path do not loop on node
            isCheckLoop = false, isPipe = false,
            isListGroup = false, // select/aggregate/group by SPARQL 1.1 rules
            // PathFinder list path instead of thread buffer: 50% faster but enumerate all path
            isListPath = false;
    private boolean isFun = false;
    private boolean isPathType = false;
    // store the list of edges of the path
    private boolean isStorePath = true;
    // cache PP result in PathFinder
    private boolean isCachePath = false;

    boolean isCountPath = false,
            isCorrect = true, isConnect = false,
            // join service send Mappings from first pattern to service
            isMap = true,
            // construct where as a rule
            isRule = false, isDetail = false;
    private boolean isMatch = false;
    private int id = -1;
    int mode = Matcher.UNDEF;

    int planner = STD_PLAN;
    private int queryProfile = STD_PROFILE;

    private boolean isService = false;

    private boolean isBind = false;

    private boolean isSynchronized = false;

    private boolean isTemplate = false;

    // member of a set of templates of a pprinter (not a single query that is a template)
    private boolean isPrinterTemplate = false;

    private boolean isAllResult = false;

    private Exp templateGroup, templateNL;
    private List<Node> argList;
    private List<Entity> edgeList;
    private String name;
    private String profile;
    private boolean isNumbering;
    private boolean isExtension = false;

    
    private GenerateBGP generateBGP;
    private List<Edge> queryEdgeList;

    
    
	
	Query(){
        super(QUERY);
		from 		= new ArrayList<Node>();
		named 		= new ArrayList<Node>();
		selectExp 	= new ArrayList<Exp>();
		selectWithExp 	= new ArrayList<Exp>();
		orderBy 	= new ArrayList<Exp>();
		groupBy 	= new ArrayList<Exp>();
		failure 	= new ArrayList<Filter>();
		pathFilter 	= new ArrayList<Filter>();
		funList 	= new ArrayList<Filter>();

		compiler 	= new Compile(this);
		table 		= new Hashtable<Edge, Query>();
		ftable 		= new HashMap<String, Filter>();
		pragma 		= new HashMap<String, Object>(); 
		tprinter 	= new HashMap<String, Object> (); 
                ptable          = new HashMap<String, Integer>();
                etable          = new HashMap<String, Edge>();
		queries 	= new ArrayList<Query>();

		patternNodes 		= new ArrayList<Node>();
		queryNodes 		= new ArrayList<Node>();
		patternSelectNodes 	= new ArrayList<Node>();
		querySelectNodes 	= new ArrayList<Node>();
		bindingNodes 		= new ArrayList<Node>();
		relaxEdges 		= new ArrayList<Node>();
		argList 		= new ArrayList<Node>();
                queryEdgeList           = new ArrayList<Edge>();
                generateBGP             = new GenerateBGP();

        querySorter = new QuerySorter(this);

    }

    Query(Exp e) {
        this();
        add(e);
    }

    public static Query create(Exp e) {
        return new Query(e);
    }

    public static Query create(int type) {
        return new Query();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (selectExp.size() > 0) {
            sb.append("select ");
            sb.append(selectExp);
            sb.append("\n");
        }
        sb.append(super.toString());
        if (!getOrderBy().isEmpty()) {
            sb.append("\n");
            sb.append("order by ");
            sb.append(getOrderBy());
        }
        if (!getGroupBy().isEmpty()) {
            sb.append("\n");
            sb.append("group by ");
            sb.append(getGroupBy());
        }
        if (getHaving() != null) {
            sb.append("\n");
            sb.append("having ");
            sb.append(getHaving());
        }
        if (getMappings() != null && getMappings().size() > 0) {
            sb.append("\n");
            sb.append("values");
            sb.append(getBindingNodes());
            sb.append("{");
            sb.append(getMappings());
            sb.append("}");
        }

        return sb.toString();
    }

    public void set(Sorter s) {
        querySorter.setSorter(s);
    }

    public void set(Edge e, Query q) {
        table.put(e, q);
    }

    Query get(Edge e) {
        return table.get(e);
    }

    public void addQuery(Query q) {
        queries.add(q);
    }

    public List<Query> getQueries() {
        return queries;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object o) {
        object = o;
    }

    public Graphable getAST() {
        return ast;
    }

    public void setAST(Graphable o) {
        ast = o;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int m) {
        mode = m;
    }

    public void setPlanProfile(int n) {
        planner = n;
    }

    public int getPlanProfile() {
        return planner;
    }
    
    /**
     * @return the hasFunctional
     */
    public boolean hasFunctional() {
        return  // bind functional
                hasFunctional || 
                // query functional
                isFunctional();
    }

    /**
     * @param hasFunctional the hasFunctional to set
     */
    public void setHasFunctional(boolean hasFunctional) {
        this.hasFunctional = hasFunctional;
    }

    public void addError(String mes, Object obj) {
        getGlobalQuery().setError(mes, obj);
    }

    public void addError(String mes, Object obj, boolean duplicate) {
        getGlobalQuery().setError(mes, obj, duplicate);
    }

    void setError(String mes, Object obj) {
        setError(mes, obj, true);
    }

    void setError(String mes, Object obj, boolean duplicate) {
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        String str = mes;
        if (obj != null) {
            str += obj;
        }

        if (!errors.contains(str)) {

            if (!duplicate) {
                for (String m : errors) {
                    if (m.startsWith(mes)) {
                        return;
                    }
                }
            }
            logger.error(str);
            errors.add(str);
        }
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addInfo(String mes, Object obj) {
        if (info == null) {
            info = new ArrayList<String>();
        }
        if (obj != null) {
            mes += obj;
        }
        if (!info.contains(mes)) {
            info.add(mes);
        }
    }

    public List<String> getInfo() {
        return info;
    }

    public Exp getBody() {
        return first();
    }

    void setGlobalQuery(Query q) {
        query = q;
        inherit(q);
    }

    /**
     * inherit from and from named
     */
    void inherit(Query q) {
        if (!isService()) {
            setFrom(q.getFrom());
            setNamed(q.getNamed());
        }
    }

    public Query getGlobalQuery() {
        if (query != null) {
            return query;
        }
        return this;
    }

    public void setOuterQuery(Query q) {
        outerQuery = q;
    }

    public Query getOuterQuery() {
        if (outerQuery == null) {
            return this;
        }
        return outerQuery;
    }

    boolean isSubQuery() {
        return query != null;
    }
    
    /**
     * Select Query is empty and does nothing
     */
    boolean isEmpty(){
        return isSelect()
                && getSelectFun().isEmpty()
                && getBody().size() == 0
                && getMappings() == null;            
    }

    boolean isCheckLoop() {
        return isCheckLoop;
    }

    public void setCheckLoop(boolean b) {
        isCheckLoop = b;
    }

    boolean isPipe() {
        return isPipe;
    }

    public void setPipe(boolean b) {
        isPipe = b;
    }

    /**
     * Fake local graph node
     */
    public Node getGraphNode() {
        return gNode;
    }

    public void setGraphNode(Node n) {
        gNode = n;
    }

    public Node getPathNode() {
        return pathNode;
    }

    public void setPathNode(Node n) {
        pathNode = n;
    }

    public void addPathFilter(Filter f) {
        pathFilter.add(f);
    }

    /**
     * constraint in property path: ?x ex:prop @[?this != <John>] ?y
     */
    List<Filter> getPathFilter() {
        return pathFilter;
    }
    
    public void defineFunction(Filter f){
        funList.add(f);
    }
    
    List<Filter> getFunList(){
        return funList;
    }

    /**
     * Return equivalent local node for graph node it may be a local node with
     * same variable name it may be a fake node
     */
    public Node getGraphNode(Node g) {
        Node node = getSelectNode(g.getLabel());
        if (node != null) {
            return node;
        } else {
            return getGraphNode();
        }
    }

    public Exp getFunction() {
        for (Exp exp : getSelectFun()) {
            if (exp.getFilter() != null && exp.getFilter().isFunctional()) {
                return exp;
            }
        }
        return null;
    }

    public static boolean isSPARQL2() {
        return true;
    }

    List<Node> getFrom(Node gNode) {
        if (gNode == null) {
            return from;
        } else {
            return named;
        }
    }

    public List<Node> getFrom() {
        return from;
    }

    public List<Node> getNamed() {
        return named;
    }

    public void setFrom(List<Node> l) {
        from = l;
    }

    public void setNamed(List<Node> l) {
        named = l;
    }

    public List<Node> getPatternNodes() {
        return patternNodes;
    }

    public List<Node> getQueryNodes() {
        return queryNodes;
    }

    public List<Node> getPatternSelectNodes() {
        return patternSelectNodes;
    }

    public List<Node> getQuerySelectNodes() {
        return querySelectNodes;
    }

    public List<Node> getBindingNodes() {
        return bindingNodes;
    }

    public void setBindingNodes(List<Node> l) {
        bindingNodes = l;
    }

    /**
     * use case: select * list of nodes that are exposed as select *
     */
    public List<Node> getSelectNodes() {
        List<Node> list = new ArrayList<Node>();
        for (Node node : patternNodes) {
            list.add(node);
        }
        for (Node node : patternSelectNodes) {
            if (!list.contains(node)) {
                Node ext = getExtNode(node);
                add(list, ext);
            }
        }
        return list;
    }

    /**
     *
     * use case: select ?x
     */
    public Node getSelectNodes(String name) {
        Node node = get(patternNodes, name);
        if (node != null) {
            return node;
        }
        node = get(patternSelectNodes, name);
        if (node != null) {
            return getExtNode(node);
        }
        return node;
    }

    public List<Exp> getSelectNodesExp() {
        List<Node> list = getSelectNodes();
        return toExp(list);
    }

    public Node getPatternNode(String name) {
        return get(patternNodes, name);
    }

    public Node getQueryNode(String name) {
        return get(queryNodes, name);
    }

    public Node getPatternSelectNode(String name) {
        return get(patternSelectNodes, name);
    }

    public Node getQuerySelectNode(String name) {
        return get(querySelectNodes, name);
    }

    Node get(List<Node> list, String name) {
        for (Node node : list) {
            if (name.equals(node.getLabel())) {
                return node;
            }
        }
        return null;
    }

    public void setSlice(int n) {
        slice = n;
    }

    public int getSlice() {
        return slice;
    }

    public void setMap(boolean b) {
        isMap = b;
    }

    public boolean isMap() {
        return isMap;
    }

    public void setLimit(int n) {
        limit = n;
    }

    public int getLimit() {
        return limit;
    }

    public int getLimitOffset() {
        // when order by/group by/count(), return all results, group sort agg, and then apply offset/limit
        if (!isConstruct()
                && (isOrderBy() || hasGroupBy() || isAggregate())) {
            return Integer.MAX_VALUE;
        }
        if (limit < Integer.MAX_VALUE - offset) {
            return limit + offset;
        } else {
            return limit;
        }
    }

    public void setOffset(int n) {
        offset = n;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean b) {
        distinct = b;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean b) {
        isCheck = b;
    }

    public void setSort(boolean b) {
        isSort = b;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public void setAggregate(boolean b) {
        isAggregate = b;
    }

    public boolean isRelax() {
        return isRelax;
    }

    public void setRelax(boolean b) {
        isRelax = b;
    }

    /**
     * To relax types on other property than rdf:type
     */
    public boolean isRelax(Edge q) {
        boolean b = isRelax && relaxEdges.contains(q.getEdgeNode());
        return b;
    }

    public void addRelax(Node n) {
        relaxEdges.add(n);
    }

    public boolean isDistribute() {
        if (query != null) {
            return query.isDistribute();
        } else {
            return isDistribute;
        }
    }

    public void setDistribute(boolean b) {
        isDistribute = b;
    }
    
    public boolean isSelect(){
        return ! (isConstruct() || isUpdate() || isDelete());
    }

    public boolean isConstruct() {
        return isConstruct;
    }

    public void setConstruct(boolean b) {
        isConstruct = b;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean b) {
        isDelete = b;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean b) {
        isUpdate = b;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean b) {
        isTest = b;
    }

    public boolean isOptimize() {
        return isOptimize;
    }

    public void setOptimize(boolean b) {
        isOptimize = b;
    }

    public void setAggregate() {
        for (Exp exp : getSelectFun()) {
            if (exp.getFilter() != null) {
                if (exp.isAggregate() && !exp.isExpGroupBy()) {
                    setAggregate(true);
                } else if (exp.getFilter().isFunctional()) {
                    setFunctional(true);
                    getOuterQuery().setHasFunctional(true);                   
                }
            }
        }
        for (Exp exp : getOrderBy()) {
            if (exp.getFilter() != null && exp.getFilter().isAggregate()) {
                setAggregate(true);
            }
        }
    }

    public void addFailure(Filter exp) {
        failure.add(exp);
    }

    public List<Filter> getFailures() {
        return failure;
    }

    public void setSelectFun(List<Exp> s) {
        selectExp = s;
    }

    public List<Exp> getSelectFun() {
        return selectExp;
    }

    public List<Exp> getSelectWithExp() {
        return selectWithExp;
    }

    /**
     * Copy select (exp as var) in a sublist for optimization purpose
     */
    public void setSelectWithExp(List<Exp> s) {
        for (Exp exp : s) {
            if (exp.getFilter() != null) {
                selectWithExp.add(exp);
            }
        }
    }

    public void addSelect(Exp exp) {
        selectExp.add(exp);
    }

    public void addSelect(Node node) {
        selectExp.add(Exp.create(NODE, node));
    }

    public void addOrderBy(Exp exp) {
        orderBy.add(exp);
    }

    public void addOrderBy(Node node) {
        orderBy.add(Exp.create(NODE, node));
    }

    public void addGroupBy(Exp exp) {
        groupBy.add(exp);
    }

    public void addGroupBy(Node node) {
        groupBy.add(Exp.create(NODE, node));
    }

    public void setOrderBy(List<Exp> s) {
        orderBy = s;
    }

    public boolean isOrderBy() {
        return orderBy.size() > 0;
    }

    public List<Exp> getOrderBy() {
        return orderBy;
    }

    public void setGroupBy(List<Exp> s) {
        groupBy = s;
    }

    public List<Exp> getGroupBy() {
        return groupBy;
    }

    public boolean isGroupBy() {
        return groupBy.size() > 0;
    }

    public boolean hasGroupBy() {
        return isGroupBy() || isConnect();
    }

    public boolean isListGroup() {
        return isListGroup;
    }

    public void setListGroup(boolean b) {
        isListGroup = b;
    }

    public void setListPath(boolean b) {
        isListPath = b;
    }

    public boolean isListPath() {
        return isListPath;
    }

    public void setCountPath(boolean b) {
        isCountPath = b;
    }

    public boolean isCountPath() {
        return isCountPath;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean b) {
        isCorrect = b;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean b) {
        isConnect = b;
    }

    public void setHaving(Exp f) {
        having = f;
    }

    public Exp getHaving() {
        return having;
    }

    public void setConstruct(Exp c) {
        construct = c;
    }

    public Exp getConstruct() {
        return construct;
    }

    public Exp getInsert() {
        return construct;
    }

    public void setDelete(Exp c) {
        delete = c;
    }

    public Exp getDelete() {
        return delete;
    }

    public int nbFun() {
        int nbfun = 0;
        for (Exp e : getSelectFun()) {
            if (e.getFilter() != null) {
                nbfun++;
            }
        }
        return nbfun;
    }

    /**
     * Check that select variables and expressions are compatible with group by
     * & aggregates use case:
     *
     * SELECT ?P (COUNT(?O) AS ?C) WHERE { ?S ?P ?O }
     *
     * SELECT ((?O1 + ?O2) AS ?O12) (COUNT(?O1) AS ?C) WHERE { ?S :p ?O1; :q ?O2
     * } GROUP BY (?S)
     *
     *
     */
    public boolean check() {
        if (getGroupBy().size() > 0) {
            for (Exp exp : getSelectFun()) {
                if (!checkGroupBy(exp)) {
                    return false;
                }
            }
        } else {
            return checkAggregate();
        }
        return true;
    }

    /**
     * If there is an aggregate, there should be no variable in select
     *
     * SELECT ?P (COUNT(?O) AS ?C) WHERE { ?S ?P ?O }
     */
    boolean checkAggregate() {
        boolean hasVariable = false, hasAggregate = false;

        for (Exp exp : getSelectFun()) {
            if (exp.getFilter() == null) {
                if (!exp.status()) {
					// special case, status=true means 
                    // exp was generated by transformer as input arg of another select exp
                    // see checkFilterVariables
                    hasVariable = true;
                }
            } else if (exp.getFilter().isRecAggregate()) {
                hasAggregate = true;
            }
        }
        return !(hasAggregate && hasVariable);
    }

    /**
     *
     * Check that select variables and expressions are compatible with group by
     *
     * SELECT ((?O1 + ?O2) AS ?O12) (COUNT(?O1) AS ?C) WHERE { ?S :p ?O1; :q ?O2
     * } GROUP BY (?S)
     *
     *
     */
    boolean checkGroupBy(Exp exp) {
        if (exp.getFilter() != null) {
			// variables should be in group by or in aggregate 
            // (fun(count(?x)) as y
            if (exp.getFilter().isRecAggregate()) {
                return true;
            }
            if (member(exp.getNode(), getGroupBy())) {
                // use case: select ?x where {} group by (f(?y) as ?x)
                return true;
            }

            List<String> list = exp.getFilter().getVariables();
            for (String var : list) {
                if (!member(var, getGroupBy())) {
                    return false;
                }
            }
        } else if (!member(exp.getNode(), getGroupBy())) {
            return false;
        }
        return true;
    }

    boolean member(Node node, List<Exp> lExp) {
        return member(node.getLabel(), lExp);
    }

    boolean member(String var, List<Exp> lExp) {
        for (Exp exp : lExp) {
            if (var.equals(exp.getNode().getLabel())) {
                return true;
            }
        }
        return false;
    }

    /**
     * PRAGMA: do not use at compile time (use getSelectFun())
     *
     * @return all var in select, including fun() as ?var
     */
    public List<Node> getSelect() {
        if (selectNode == null) {
            selectNode = new ArrayList<Node>();
            if (selectExp != null) {
                for (Exp exp : selectExp) {
                    selectNode.add(exp.getNode());
                }
            }
        }
        return selectNode;
    }

    public List<String> getVariables() {
        List<String> list = new ArrayList<String>();
        for (Node node : getSelect()) {
            list.add(node.getLabel());
        }
        return list;
    }

    public Exp getSelectExp(String label) {
        for (Exp exp : getSelectFun()) {
            Node node = exp.getNode();
            if (node.getLabel().equals(label)) {
                return exp;
            }
        }
        return null;
    }

    public Node getSelectNode(String label) {
        Exp exp = getSelectExp(label);
        if (exp != null) {
            return exp.getNode();
        }
        return null;
    }

    public int nbNodes() {
        return iNode;
    }

    public int nbEdges() {
        return iEdge;
    }

    public synchronized int nbPath() {
        return iPath++;
    }

    /**
     * Only on global query, not on subquery
     */
    public void complete(Producer prod) {
        if (isCompiled()) {
            return;
        } else {
            setCompiled(true);
        }

		// sort edges according to var connexity, assign filters
        // recurse on subquery
        querySorter.compile(prod);
        setAggregate();
        // recurse on subquery
        index(this, getBody(), true, false, -1);

        for (Exp ee : getSelectFun()) {
            // use case: query node created to hold fun result
            Node snode = ee.getNode();
            index(snode);
            // use case: select (exists{?x :p ?y} as ?b)
            if (ee.getFilter() != null) {
                index(this, ee.getFilter());
            }
        }

        complete2();

        for (Query q : getQueries()) {
            q.complete(prod);
        }
    }

    /**
     *
     * index(node) use global query index
     */
    void complete2() {
        for (Filter f : getPathFilter()) {
            index(this, f);
        }
        index(getOrderBy());
        index(getGroupBy());

        if (getHaving() != null) {
            index(this, getHaving().getFilter());
        }
        
        for (Filter f :getFunList()){
            index(this, f);
        }

        for (Node node : getBindingNodes()) {
            index(node);
        }

        for (Node node : getArgList()) {
            index(node);
        }

        if (getGraphNode() != null) {
            index(getGraphNode());
        }
        if (getPathNode() != null) {
            index(getPathNode());
        }
    }

    void compile(Filter f) {
        querySorter.compile(f);
    }

    void index(List<Exp> list) {
        for (Exp ee : list) {
			// use case: group by (exists{?x :p ?y} as ?b)
            // use case: order by exists{?x :p ?y} 
            if (ee.getFilter() != null) {
                index(this, ee.getFilter());
            }
        }
    }

    public Query getQuery() {
        return this;
    }

    void trace() {
        System.out.println("patternNodes: " + patternNodes);
        System.out.println("queryNodes: " + queryNodes);
        System.out.println("patternSelectNodes: " + patternSelectNodes);
        System.out.println("querySelectNodes: " + querySelectNodes);
    }

    /**
     * TODO: must also join: {select ?x where {?x rdf:first ?y}} filter (exists
     * {?x}) minus {?x} and also {minus {select ?x where }} filter exists
     * {{select ?x where }}
     *
     * embedding select * must evolve as well
     */
    Node getOuterNode(Node subNode) {
        return getExtNode(subNode.getLabel());
    }

    Node getOuterNodeSelf(Node subNode) {
        Node n = getExtNode(subNode.getLabel());
        if (n == null) {
            return subNode;
        }
        return n;
    }

    public Node getExtNode(Node qNode) {
        return getExtNode(qNode.getLabel());
    }

    public Node getNode(String name) {
        return getExtNode(name);
    }

    /**
     * get node with going in select sub query go into its own select because
     * order by may reuse a select variable use case: transformer find node for
     * select & group by
     */
    public Node getProperAndSubSelectNode(String name) {
        return getExtNode(name, true);
    }

    public Node getExtNode(String name) {
        return getExtNode(name, false);
    }

    public Node getExtNode(String name, boolean select) {
        Node node = getPatternNode(name);
        if (node != null) {
            return node;
        }
        if (select) {
            node = getSelectNode(name);
            if (node != null) {
                return node;
            }
        }
        node = getQueryNode(name);
        if (node != null) {
            return node;
        }
        node = getPatternSelectNode(name);
        if (node != null) {
            return node;
        }
        node = getQuerySelectNode(name);
        return node;
    }

    void store(Node node, boolean exist, boolean select) {
        if (select) {
            if (exist) {
                add(querySelectNodes, node);
            } else {
                add(patternSelectNodes, node);
            }
        } else {
            if (exist) {
                add(queryNodes, node);
            } else {
                add(patternNodes, node);
            }
        }
    }

    public void collect() {
        if (getPathNode() != null) {
            /**
             * use case: ?x ex:prop @[?this != <John>] + ?y collect ?this first
             * because it may be within @[exists {?this ?p ?y}} and even worse
             * within @[exists {select ?this where {?this ?p ?y}}]
             */
            store(getPathNode(), false, false);
        }

        for (Exp ee : this) {
            collect(ee, false);
        }

        for (Node node : getBindingNodes()) {
            store(node, false, false);
        }

        for (Node node : getArgList()) {
            store(node, false, false);
        }

        for (Filter ff : getPathFilter()) {
            collectExist(ff.getExp());
        }
        
        for (Filter f : getFunList()){
             collectExist(f.getExp());
        }
    }

    void collect(Exp exp, boolean exist) {

        switch (exp.type()) {

            case FILTER:
			// get exists {} nodes
                // draft
                collectExist(exp.getFilter().getExp());
                break;

            case NODE:
                store(exp.getNode(), exist, false);
                break;

            case VALUES:
                for (Node node : exp.getNodeList()) {
                    store(node, exist, false);
                }
                break;

            case EDGE:
            case PATH:
            case XPATH:
            case EVAL:
                for (int i = 0; i < exp.nbNode(); i++) {
                    Node node = exp.getNode(i);
                    store(node, exist, false);
                }
                break;

            case MINUS:
                // second argument does not bind anything: skip it
                if (exp.first() != null) {
                    collect(exp.first(), exist);
                }
                if (exp.rest() != null) {
                    collect(exp.rest(), true);
                }

                break;

            case QUERY:

			// use case: select * where { {select ?y fun() as ?var where {}} }
                // we want ?y & ?var for select *			
                for (Exp ee : exp.getQuery().getSelectFun()) {
                    store(ee.getNode(), exist, true);
                }

                break;

            case BIND:
                collectExist(exp.getFilter().getExp());
                store(exp.getNode(), exist, true);
                break;

            default:
                for (Exp ee : exp) {
                    collect(ee, exist);
                }
        }

    }

    void collectExist(Expr exp) {
        switch (exp.oper()) {

            case ExprType.EXIST:
                Exp pat = getPattern(exp);
                collect(pat, true);
                break;

            default:
                for (Expr ee : exp.getExpList()) {
                    collectExist(ee);
                }

        }
    }

    /**
     * set Index for EDGE & NODE check if subquery is aggregate in case of
     * UNION, start is the start index for both branches if exp is free (no
     * already bound variable, it is tagged as free) return the min of index of
     * exp Nodes query is (sub)query this is global query
     */
    int index(Query query, Exp exp, boolean hasFree, boolean isExist, int start) {
        int min = Integer.MAX_VALUE, n;
        int type = exp.type();

        switch (type) {

            case EDGE:
            case PATH:
            case XPATH:
            case EVAL:
                Edge edge = exp.getEdge();
                edge.setIndex(iEdge++);
                for (int i = 0; i < exp.nbNode(); i++) {
                    Node node = exp.getNode(i);
                    n = qIndex(query, node);
                    min = Math.min(min, n);
                }

                if (exp.hasPath()) {
                            // x rdf:type t
                    // x rdf:type/rdfs:subClassOf* t
                    Exp ep = exp.getPath();
                    ep.getEdge().setIndex(edge.getIndex());
                    for (int i = 0; i < ep.nbNode(); i++) {
                        Node node = ep.getNode(i);
                        n = qIndex(query, node);
                        min = Math.min(min, n);
                    }
                }
                break;

            case VALUES:
                for (Node node : exp.getNodeList()) {
                    n = qIndex(query, node);
                    min = Math.min(min, n);
                }
                break;

            case NODE:
                Node node = exp.getNode();
                min = qIndex(query, node);
                break;

            case BIND:
                Node qn = exp.getNode();
                min = qIndex(query, qn);
                        // continue on filter below:

            case FILTER:
                // use case: filter(exists {?x ?p ?y})
                boolean hasExist = index(query, exp.getFilter());

                List<String> lVar = exp.getFilter().getVariables(true);
                for (String var : lVar) {
                    Node qNode = query.getProperAndSubSelectNode(var);
                    if (qNode != null) {
                        n = qIndex(query, qNode);
                        min = Math.min(min, n);
                    } else if (!isExist && !hasExist) {
					// TODO: does not work with filter in exists{}
                        // because getProperAndSubSelectNode does not go into exists{}
                        Message.log(Message.UNDEF_VAR, var);
                        addError(Message.get(Message.UNDEF_VAR), var);
                    }
                }
                if (hasExist) {
				// by safety, outer exp will not be free
                    // use case:
                    // exists {?x p ?y filter(?x != ?z)}
                    min = -1;
                }
                break;

            case QUERY:

                Query qq = exp.getQuery();
                qq.setCompiled(true);
                qq.setGlobalQuery(this);
                qq.setOuterQuery(query);
                qq.setAggregate();

                for (Exp e : exp) {
                    // for subquery, do not consider index here
                    index(qq, e, hasFree, isExist, -1);
                }

                for (Exp ee : qq.getSelectFun()) {
				// use case: query node created to hold fun result
                    // see Transformer compiler.compileSelectFun()
                    Node sNode = ee.getNode();
				// get the outer node for this select sNode
                    // use case:
                    // ?x ?p ?y 
                    // {select * where {?y ?r ?t}}
                    // get the index of outer sNode ?y 

                    n = qIndex(qq, sNode);

                    min = Math.min(min, n);

                    if (ee.getFilter() != null) {
                        index(qq, ee.getFilter());
                    }

                }

                qq.complete2();

                break;

            case OPT_BIND:

            case ACCEPT:
                break;

            default:
                // AND UNION OPTION GRAPH BIND
                int startIndex = globalNodeIndex(),
                 ind = -1;
                if (start >= 0) {
                    startIndex = start;
                }
                if (exp.isUnion()) {
                    ind = startIndex;
                }
                for (Exp e : exp) {
                    n = index(query, e, hasFree && !exp.isGraph(), isExist, ind);
                    min = Math.min(min, n);
                }

                switch (exp.type()) {

                    case GRAPHNODE:
                        break;

                    default:
                        if (startIndex > 0 && min >= startIndex && hasFree) {
					// this exp has no variable in common with preceding exp
                            // hasFree = false : 
                            // except graph ?g {ei ej} because ek in graph share ?g implicitly !!!
                            // pragma {kg:kgram kg:test true}
                            if (isOptimize()) {
                                exp.setFree(true);
                            }
                            if (isDebug()) {
                                //Message.log(Message.FREE, exp);
                            }
                        }
                }

        }

        // index the fake graph node (select/minus)
        if (exp.getGraphNode() != null) {
            index(exp.getGraphNode());
        }

        return min;

    }

    boolean inSelect(Node qNode) {
        for (Exp exp : getSelectFun()) {
            Node node = exp.getNode();
            if (node == qNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate or retrieve index of node If node is in a sub query, return the
     * index of the outer node corresponding to node and rec.
     */
    int qIndex(Query query, Node node) {
        int n = index(node);
        if (query != this && query.inSelect(node)) {
            // get the outer node for this sub select sNode
            Node oNode = query.getOuterQuery().getProperAndSubSelectNode(node.getLabel());
            if (oNode != null) {
                n = qIndex(query.getOuterQuery(), oNode);
            }
        }
        return n;
    }

    /**
     */
    public int index(Node node) {
        if (node.getIndex() == -1) {
            node.setIndex(newGlobalNodeIndex());
        }
        //System.out.println("** Q: " + node + " " + node.getIndex());
        return node.getIndex();
    }

    /**
     * Use outer query node index for all (sub) queries
     */
    int newGlobalNodeIndex() {
        return getOuterQuery().newNodeIndex();
    }

    int newNodeIndex() {
        return iNode++;
    }

    int globalNodeIndex() {
        return getOuterQuery().getNodeIndex();
    }

    int getNodeIndex() {
        return iNode;
    }

    void setNodeIndex(int n) {
        iNode = n;
    }

    /**
     *
     * @return nodes for select * where BODY go into BODY
     */
//	public List<Exp> getNodesExp(){
//		List<Node> lNode = getNodes();
//		return toExp(lNode);
//	}
    public List<Exp> toExp(List<Node> lNode) {
        List<Exp> lExp = new ArrayList<Exp>();
        for (Node node : lNode) {
            lExp.add(Exp.create(NODE, node));
        }
        return lExp;
    }

    /**
     * use case: select distinct ?x where add an ACCEPT ?x statement to check
     * that ?x is new
     */
    public void distinct() {
        if (testJoin) {
                // in case of JOIN() the ACCEPT(?x) cannot be set
            // because evaluation occurs in kgram subEval
            // where there is no Group to compute accept()
            return;
        }
        if (isDistinct() && getSelectFun().size() == 1) {
            Node qNode = getSelectFun().get(0).getNode();
            for (Exp exp : this) {
                if (exp.distinct(qNode)) {
                    return;
                }
            }
        }
    }

    /**
     * *************************************************************
     * Compile before exec
     */
    /**
     * search for: path(?from path ?to) . filter(?from = cst || ?to = cst)
     * compile to: {path(?from path ?to) . filter(?from = cst)} UNION
     * {path(?from path ?to) . filter(?to = cst)}
     *
     * By safety consider only path at index 0 to avoid unnecessary duplication
     * of code (if ?x is already bound)
     */
    void checkPath(Exp exp, boolean compile) {
        int i = 0;
        boolean b = false;

        for (Exp ee : exp) {
            if ((ee.isPath() || ee.isEdge()) && i < exp.size() - 1 && exp.get(i + 1).isFilter()) {
                b = checkPath(ee, exp.get(i + 1));
                if (b) {
                    break;
                }
            }
            i++;
        }

        if (b) {
            // found path + filter
            Exp union = createUnion(exp, i);
            if (compile) {
                // compile to UNION on both filters
                exp.set(i, union);
            } else {
                addInfo("Expensive pattern: \n", exp.get(i) + "\n" + exp.get(i + 1));
                addInfo("Alternative: \n", union);
            }
        }
    }

    boolean checkPath(Exp exp, Exp filter) {
        Edge edge = exp.getEdge();
        Node n1 = edge.getNode(0);
        Node n2 = edge.getNode(1);

        boolean b = false;
        if (n1.isVariable() && n2.isVariable()) {
            b = compiler.check(n1.getLabel(), n2.getLabel(), filter);
        }
        return b;
    }

    /**
     * path(?from path ?to) . filter(?from = cst || ?to = cst) create
     * {path(?from path ?to) . filter(?from = cst)} UNION {path(?from path ?to)
     * . filter(?to = cst)}
     */
    Exp createUnion(Exp exp, int i) {
        Edge ee = exp.get(i).getEdge();
        Node n1 = ee.getNode(0);

        Expr ff = exp.get(i + 1).getFilter().getExp();
        Filter f1 = ff.getExp(0).getFilter();
        Filter f2 = ff.getExp(1).getFilter();
        List<String> list = f1.getVariables();

        if (!list.get(0).equals(n1.getLabel())) {
            Filter tmp = f1;
            f1 = f2;
            f2 = tmp;
        }

        Exp e1 = Exp.create(AND, exp.get(i), Exp.create(FILTER, f1));
        Exp e2 = Exp.create(AND, exp.get(i), Exp.create(FILTER, f2));
        Exp e3 = Exp.create(UNION, e1, e2);
        return e3;
    }

    void processFilter(Exp exp, boolean option) {
        boolean correct = checkFilter(exp);

        if (!correct) {
            if (option) {
                // in case of option, exp is the inner AND
                exp.setFail(true);
            } else {
                this.setFail(true);
            }
        }

    }

    /**
     * if exist EDGE with NODE n and exist also NODE n by itself then remove
     * NODE n because it is redundant
     */
    void cleanNode(Exp exp) {
        ArrayList<Exp> nodes = new ArrayList<Exp>();
        for (Exp eNode : exp) {
            if (eNode.isNode()) {
                for (Exp eEdge : exp) {
                    if (eEdge.type() == OPTION) {
						// option may bind the node
                        // hence cannot remove mandatory node
                        // use case: ?x rdf:type CC  optional {?x p ?y}
                        return;
                    } else if (eEdge.isEdge() && !eEdge.isPath()
                            && eEdge.contains(eNode.getNode())) {
                        nodes.add(eNode);
                    }
                }
            }
        }
        for (Exp node : nodes) {
            exp.remove(node);
        }
    }

    /**
     * @return the isPrinterTemplate
     */
    public boolean isPrinterTemplate() {
        return isPrinterTemplate;
    }

    /**
     * @param isPrinterTemplate the isPrinterTemplate to set
     */
    public void setPrinterTemplate(boolean isPrinterTemplate) {
        this.isPrinterTemplate = isPrinterTemplate;
    }

    public boolean isMatchBlank() {
        return isMatch;
    }

    /**
     * @param match the match to set
     */
    public void setMatchBlank(boolean match) {
        this.isMatch = match;
    }

    public List<Node> getArgList() {
        return argList;
    }

    public void defArg(Node n) {
        argList.add(n);
    }

    /**
     * @return the edgeList
     */
    public List<Entity> getEdgeList() {
        return edgeList;
    }

    /**
     * @param edgeList the edgeList to set
     */
    public void setEdgeList(List<Entity> edgeList) {
        this.edgeList = edgeList;
    }

    /**
     * @return the constructNodes
     */
    public List<Node> getConstructNodes() {
        return constructNodes;
    }

    /**
     * @param constructNodes the constructNodes to set
     */
    public void setConstructNodes(List<Node> constructNodes) {
        this.constructNodes = constructNodes;
    }

    /**
     * @return the edgeIndex
     */
    public int getEdgeIndex() {
        return edgeIndex;
    }

    /**
     * @param edgeIndex the edgeIndex to set
     */
    public void setEdgeIndex(int edgeIndex) {
        this.edgeIndex = edgeIndex;
    }

    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public void setProfile(String uri) {
        profile = uri;
    }

    public String getProfile() {
        return profile;
    }

    public void setNumbering(boolean b) {
        isNumbering = b;
    }

    public boolean isNumbering() {
        return isNumbering;
    }

    /**
     * @return the templateProfile
     */
    public Query getTemplateProfile() {
        return templateProfile;
    }

    /**
     * @param templateProfile the templateProfile to set
     */
    public void setTemplateProfile(Query templateProfile) {
        this.templateProfile = templateProfile;
    }

    /**
     * Check always true and always false filters return false if there is a
     * always false filter
     */
    boolean checkFilter(Exp exp) {
        boolean b = true;
        for (int i = 0; i < exp.size(); i++) {
            Exp e1 = exp.get(i);
            if (e1.isFilter()) {
                b = compiler.check(e1) && b;

                for (int j = i + 1; j < exp.size(); j++) {
                    Exp e2 = exp.get(j);
                    if (e2.isFilter()) {
                        b = compiler.check(e1, e2) && b;
                    }
                }
            }
        }
        return b;
    }

    /**
     * Compute node list for filter variables use case: Pattern compiler (?x =
     * cst) TODO: does not dive into minus {PAT}
     */
    public List<Node> getNodes(Exp exp) {
        return getNodes(exp.getFilter());
    }

    public List<Node> getNodes(Filter f) {
        List<String> lVar = f.getVariables();
        ArrayList<Node> lNode = new ArrayList<Node>();
        for (String var : lVar) {
            Node node = getProperAndSubSelectNode(var);
            if (node != null && !lNode.contains(node)) {
                lNode.add(node);
            }
        }
        return lNode;
    }

    /**
     * use case: select count(distinct ?x)
     */
    public List<Node> getAggNodes(Filter f) {
        ArrayList<Node> lNode = new ArrayList<Node>();
        getAggNodes(f.getExp(), lNode);
        return lNode;
    }

    void getAggNodes(Expr exp, ArrayList<Node> lNode) {
        if (exp.type() == ExprType.VARIABLE) {
            Node node = getProperAndSubSelectNode(exp.getLabel());
            if (node != null && !lNode.contains(node)) {
                lNode.add(node);
            }
        } else {
            for (Expr ee : exp.getExpList()) {
                getAggNodes(ee, lNode);
            }
        }
    }

    /**
     * ******************************************************************
     *
     * Dependency with filter exp for tracking filter(exists {PAT})
     *
     */
    boolean index(Query query, Filter f) {
        return index(query, f.getExp());
    }

    /**
     * Looking for filter(exist {})
     */
    boolean index(Query query, Expr exp) {
        boolean b = false;
        if (exp.oper() == ExprType.EXIST) {
            index(query, getPattern(exp), false, true, -1);
            b = true;
        } else {
            for (Expr ee : exp.getExpList()) {
                b = index(query, ee) || b;
            }
        }
        return b;
    }

    /**
     * ********************************************************************
     *
     * Pipeline using operators on queries: union/and/optional/minus
     * q1.union(q2).and(q3).optional(q4).minus(q5)
     *
     *********************************************************************
     */
    public Query union(Query q2) {
        Query q1 = this;
        Exp exp = Exp.create(UNION, q1, q2);
        Query q = Query.create(exp).complete(q1, q2);
        return q;
    }

    public Query and(Query q2) {
        Query q1 = this;
        Exp exp = Exp.create(AND, q1, q2);
        Query q = Query.create(exp).complete(q1, q2);
        return q;
    }

    public Query minus(Query q2) {
        Query q1 = this;
        Exp exp = Exp.create(MINUS, q1, Exp.create(AND, q2));
        Query q = Query.create(exp).complete(q1, q2);
        return q;
    }

    public Query optional(Query q2) {
        Query q1 = this;
        Exp exp = Exp.create(AND, q1, Exp.create(OPTION, Exp.create(AND, q2)));
        Query q = Query.create(exp).complete(q1, q2);
        return q;
    }

    public Query ifthen(Query q1, Query q2) {

        return this;
    }

    public Query orderBy(Node node) {
        if (node != null && !contain(getOrderBy(), node)) {
            addOrderBy(node);
        }
        return this;
    }

    public Query orderBy(String n) {
        return orderBy(getNode(n));
    }

    public Query groupBy(Node node) {
        if (node != null && !contain(getGroupBy(), node)) {
            addGroupBy(node);
        }
        return this;
    }

    public Query groupBy(String n) {
        return groupBy(getNode(n));
    }

    public Query select(Node node) {
        if (node != null && !contain(getSelectFun(), node)) {
            addSelect(node);
        }
        return this;
    }

    public Query select(String n) {
        return select(getNode(n));
    }

    Query complete(Query q1, Query q2) {
        q1.setOuterQuery(this);
        q2.setOuterQuery(this);
        setGlobalQuery(getBody());
        setSelect(q1, q2);
        collect();
        setAST(q2.getAST());
        return this;
    }

    void setSelect(Query q1, Query q2) {
        List<Exp> list = new ArrayList<Exp>();
        list.addAll(q1.getSelectFun());
        for (Exp exp : q2.getSelectFun()) {
            if (!contain(list, exp.getNode())) {
                list.add(exp);
            }
        }
        setSelectFun(list);
    }

    // rec set global query
    void setGlobalQuery(Exp exp) {
        for (Exp q : exp) {
            if (q.isQuery()) {
                q.getQuery().setGlobalQuery(this);
                setGlobalQuery(q);
            }
        }
    }

    public void setBind(boolean isBind) {
        this.isBind = isBind;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setService(boolean isService) {
        this.isService = isService;
    }

    boolean isService() {
        return isService;
    }

    void setCompiled(boolean isCompiled) {
        this.isCompiled = isCompiled;
    }

    boolean isCompiled() {
        return isCompiled;
    }

    public Filter getFilter(String name) {
        return ftable.get(name);
    }

    public Expr getProfile(String name) {
        if (templateProfile == null) {
            return null;
        }
        Filter f = templateProfile.getFilter(name);
        if (f == null) {
            return null;
        }
        return f.getExp();
    }

    public Filter getGlobalFilter(String name) {
        return getGlobalQuery().getFilter(name);
    }

    public void setFilter(String name, Filter filter) {
        ftable.put(name, filter);
    }

    public Iterable<String> getFilterNames() {
        return ftable.keySet();
    }

    public Object getPragma(String name) {
        return pragma.get(name);
    }

    public String getStringPragma(String name) {
        return (String) pragma.get(name);
    }

    public boolean hasPragma(String name) {
        return pragma.get(name) != null;

    }

    public boolean isPragma(String name) {
        Object obj = pragma.get(name);
        if (obj == null || !(obj instanceof Boolean)) {
            return false;
        }
        Boolean b = (Boolean) obj;
        return b;
    }

    public void setPragma(String name, Object value) {
        pragma.put(name, value);
    }

    public void setRule(boolean rule) {
        isRule = rule;
    }

    public boolean isRule() {
        return isRule;
    }

    public void setDetail(boolean b) {
        isDetail = b;
    }

    public boolean isDetail() {
        return isDetail;
    }

    public void setSynchronized(boolean b) {
        isSynchronized = b;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public Object getTransformer(String p) {
        return getOuterQuery().getPPrinter(p);
    }

    public Object getTransformer() {
        return getOuterQuery().getPPrinter(null);
    }

    public void setTransformer(String p, Object pprinter) {
        getOuterQuery().setPPrinter(p, pprinter);
    }

    public Object getPPrinter(String p) {
        if (p == null) {
            return pprinter;
        }
        return tprinter.get(p);
    }

    public void setPPrinter(String p, Object pprinter) {
        if (p == null) {
            // next kg:pprint() will use this one
            this.pprinter = pprinter;
        } else {
            if (this.pprinter == null) {
                // next kg:pprint() will use this one
                this.pprinter = pprinter;
            }
            tprinter.put(p, pprinter);
        }
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isAllResult() {
        return true; //isAllResult;
    }

    public void setAllResult(boolean isAllResult) {
        this.isAllResult = isAllResult;
    }

    public Exp getTemplateGroup() {
        return templateGroup;
    }

    public void setTemplateGroup(Exp templateGroup) {
        this.templateGroup = templateGroup;
    }

    public Exp getTemplateNL() {
        return templateNL;
    }

    public void setTemplateNL(Exp nl) {
        this.templateNL = nl;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void recordPredicate(Node p, Edge edge) {
        Integer i = ptable.get(p.getLabel());
        if (i == null) {
            i = 0;
        }
        ptable.put(p.getLabel(), i + 1);
        etable.put(p.getLabel(), edge);
    }

    public int nbPredicate(Node p) {
        Integer n = ptable.get(p.getLabel());
        if (n == null) {
            return 0;
        }
        return n;
    }

    public Edge getEdge(Node p) {
        return etable.get(p.getLabel());
    }

    /**
     * @return the queryProfile
     */
    public int getQueryProfile() {
        return queryProfile;
    }

    /**
     * @param queryProfile the queryProfile to set
     */
    public void setQueryProfile(int queryProfile) {
        this.queryProfile = queryProfile;
    }

    /**
     * @return the isPathType
     */
    public boolean isPathType() {
        return isPathType;
    }

    /**
     * @param isPathType the isPathType to set
     */
    public void setPathType(boolean isPathType) {
        this.isPathType = isPathType;
    }

    /**
     * @return the isStorePath
     */
    public boolean isStorePath() {
        return isStorePath;
    }

    /**
     * @param isStorePath the isStorePath to set
     */
    public void setStorePath(boolean isStorePath) {
        this.isStorePath = isStorePath;
    }

    /**
     * @return the isCachePath
     */
    public boolean isCachePath() {
        return isCachePath;
    }

    /**
     * @param isCachePath the isCachePath to set
     */
    public void setCachePath(boolean isCachePath) {
        this.isCachePath = isCachePath;
    }

    /**
     * @return the id
     */
    public int getID() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * @return the provenance
     */
    public Node getProvenance() {
        return provenance;
    }

    /**
     * @param provenance the provenance to set
     */
    public void setProvenance(Node provenance) {
        this.provenance = provenance;
    }

    /**
     * @return the graph
     */
    public Object getGraph() {
        return graph;
    }

    /**
     * @param graph the graph to set
     */
    public void setGraph(Object graph) {
        this.graph = graph;
    }

    @Override
    public String toGraph() {
        return getAST().toGraph();
    }

    public void setExtension(boolean b) {
        isExtension = b;
    }

    public boolean isExtension() {
        return isExtension;
    }

    /**
     * @return the extention
     */
    public Extension getExtension() {
        return extension;
    }

    /**
     * @param extention the extention to set
     */
    public void setExtension(Extension ext) {
        this.extension = ext;
    }
    
    public Expr getExpression(String name){
        if (getExtension() != null){
            Expr exp = getExtension().get(name);
            if (exp != null){
                return exp.getFunction(); 
            }
        }
        return null;
    }
    
    public void addExtension(Extension ext){
        if (ext == null){
            return;
        }
        if (extension == null){
            extension = ext;
        }
        else {
            extension.add(ext);
        }
    }

    @Override
    public Iterable getLoop() {
        ArrayList<Edge> list = new ArrayList();
        for (Exp exp : getBody()){
            if (exp.isEdge()){
                list.add(exp.getEdge());
            }
        }
        return list;
    }

    /**
     * @return the isUseBind
     */
    public boolean isUseBind() {
        return isUseBind;
    }

    /**
     * @param isUseBind the isUseBind to set
     */
    public void setUseBind(boolean isUseBind) {
        this.isUseBind = isUseBind;
    }

    
    public GenerateBGP getGenerateBGP() {
        return generateBGP;
    }
    
    public void setGenerateBGP(GenerateBGP generateBGP) {
        this.generateBGP = generateBGP;
    }

    public List<Edge> getQueryEdgeList() {
        return queryEdgeList;
    }

    public void setQueryEdgeList(List<Edge> queryEdgeList) {
        this.queryEdgeList = queryEdgeList;
    }

    /**
     * @return the isFun
     */
    public boolean isFun() {
        return isFun;
    }

    /**
     * @param isFun the isFun to set
     */
    public void setFun(boolean isFun) {
        this.isFun = isFun;
    }
	
}
