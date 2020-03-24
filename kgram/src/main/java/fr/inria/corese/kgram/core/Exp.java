package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpPattern;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.STATEMENT;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.HashMap;

/**
 * KGRAM expressions
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Exp extends PointerObject
        implements ExpType, ExpPattern, Iterable<Exp> {

    /**
     * @return the mappings
     */
    public boolean isMappings() {
        return mappings;
    }

    /**
     * @param mappings the mappings to set
     */
    public void setMappings(boolean mappings) {
        this.mappings = mappings;
    }

    /**
     * @return the generated
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * @param generated the generated to set
     */
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    /**
     * @return the simpleNodeList
     */
    public List<Node> getInScopeNodeList() {
        return simpleNodeList;
    }

    /**
     * @param simpleNodeList the simpleNodeList to set
     */
    public void setInScopeNodeList(List<Node> simpleNodeList) {
        this.simpleNodeList = simpleNodeList;
    }

    /**
     * @return the externQuery
     */
    public Query getExternQuery() {
        return externQuery;
    }

    /**
     * @param externQuery the externQuery to set
     */
    public void setExternQuery(Query externQuery) {
        this.externQuery = externQuery;
    }
    
    public static final int ANY        = -1;
    public static final int SUBJECT    = 0;
    public static final int OBJECT     = 1;
    public static final int PREDICATE  = 2;
    public static final int GRAPH_NAME = 3;
    
    static final String NL = System.getProperty("line.separator");
    static final String SP = " ";
    static Exp empty = new Exp(EMPTY);
    // group edge even if there is a disconnected filter
    public static boolean groupEdge = true;
    int type, index = -1;
    // optional success
    boolean // default status must be false (for order by desc())
            status = false,
            // for WATCH: skip exp if reach CONTINUE/BACKJUMP
            skip,
            isFail = false,
            isPath = false,
            isFree = false,
            isAggregate = false,
            isBGP = false,
            lock = false,
            isSilent = false;
    boolean isDebug = false;
    private boolean isPostpone = false;
    private boolean BGPAble = false;
    private boolean isFunctional = false;
    private boolean generated = false;
    VExp args;
    Edge edge;
    Node node;
    private Node arg;
    List<Node> lNodes;
    //  service with several URI:
    private List<Node> nodeSet, 
            // in-scope variables except those that are only in right arg of optional
            simpleNodeList;
    Filter filter;
    List<Filter> lFilter;
    // min(?l, expGroupBy(?x, ?y))
    List<Exp> expGroupBy;
    // for UNION
    Stack stack;
    // for EXTERN 
    Object object;
    Producer producer;
    Regex regex;
    Exp next;
    private Exp postpone;
    private Exp path;
    private Exp bind;
    private Exp values;
    private Query externQuery;
    Mappings map, templateMap;
    HashMap<Node, Mappings> cache;
    int min = -1, max = -1;
    private int level = -1;
    private boolean isSystem = false;
    private boolean mappings = false;

    /**
     * @return the bind
     */
    public Exp getBind() {
        return bind;
    }

    /**
     * @param bind the bind to set
     */
    public void setBind(Exp bind) {
        this.bind = bind;
    }

    /**
     * @return the isFunctional
     */
    public boolean isFunctional() {
        return isFunctional;
    }

    /**
     * @param isFunctional the isFunctional to set
     */
    public void setFunctional(boolean isFunctional) {
        this.isFunctional = isFunctional;
    }

    /**
     * @return the path
     */
    public Exp getPath() {
        return path;
    }

    public boolean hasPath() {
        return path != null;
    }

    /**
     * @param path the path to set
     */
    public void setPath(Exp path) {
        this.path = path;
    }

    public void setSystem(boolean b) {
        isSystem = b;
    }

    public boolean isSystem() {
        return isSystem;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isBGP() {
        return type == BGP;
    }

    public boolean isBGPAnd() {
        return type == AND || type == BGP;
    }

    /**
     * @return the BGPAble
     */
    public boolean isBGPAble() {
        return BGPAble;
    }

    /**
     * @param BGPAble the BGPAble to set
     */
    public void setBGPAble(boolean BGPAble) {
        this.BGPAble = BGPAble;
    }

    /**
     * @return the arg
     */
    public Node getCacheNode() {
        return arg;
    }

    /**
     * @param arg the arg to set
     */
    public void setCacheNode(Node arg) {
        this.arg = arg;
    }

    /**
     * @return the values
     */
    public Exp getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(Exp values) {
        this.values = values;
    }

    /**
     * @return the postpone
     */
    public boolean isPostpone() {
        return isPostpone;
    }

    /**
     * @param postpone the postpone to set
     */
    public void setPostpone(boolean postpone) {
        this.isPostpone = postpone;
    }

    /**
     * @param postpone the postpone to set
     */
    public void setPostpone(Exp postpone) {
        this.postpone = postpone;
    }

    /**
     * @return the nodeSet
     */
    public List<Node> getNodeSet() {
        return nodeSet;
    }

    /**
     * @param nodeSet the nodeSet to set
     */
    public void setNodeSet(List<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    class VExp extends ArrayList<Exp> {
    }

    Exp() {
    }

    Exp(int t) {
        type = t;
        args = new VExp();
        lFilter = new ArrayList<Filter>();
    }

//    Exp(int t, Exp e1, Exp e2) {
//        this(t);
//        args.add(e1);
//        args.add(e2);
//    }
//
//    Exp(int t, Exp e) {
//        this(t);
//        args.add(e);
//    }

    public static Exp create(int t) {
        switch (t){
            case PATH:
            case EDGE: return new ExpEdge(t);
        }
        return new Exp(t);
    }

    public static Exp create(int t, Exp e1, Exp e2) {
        Exp e = create(t);
        e.add(e1);
        e.add(e2);
        return e;
    }

    public static Exp create(int t, Exp e1, Exp e2, Exp e3) {
        Exp e = create(t);
        e.add(e1);
        e.add(e2);
        e.add(e3);
        return e;
    }

    public static Exp create(int t, Exp e1) {
        Exp e = create(t);
        e.add(e1);
        return e;
    }

    public static Exp create(int t, Node n) {
        Exp exp = create(t);
        exp.setNode(n);
        return exp;
    }

    public static Exp create(int t, Edge e) {
        Exp exp = create(t);
        exp.setEdge(e);
        return exp;
    }

    public static Exp create(int t, Filter e) {
        Exp exp = create(t);
        exp.setFilter(e);
        return exp;
    }
    
    // draft for BGP
    public Exp duplicate() {
        Exp exp = Exp.create(type());
        for (Exp e : this) {
            exp.add(e);
        }
        return exp;
    }
    
    public static Exp createValues(List<Node> list, Mappings map){
        Exp exp = create(VALUES);
        exp.setNodeList(list);
        exp.setMappings(map);
        return exp;
    }

    public boolean hasArg() {
        return args.size() > 0;
    }

    @Override
    public int size() {
        return args.size();
    }

    public void add(Exp e) {
        args.add(e);
    }

    public void add(Edge e) {
        args.add(create(EDGE, e));
    }

    public void add(Node n) {
        args.add(create(NODE, n));
    }

    public void add(Filter f) {
        args.add(create(FILTER, f));
    }

    public void set(int n, Exp e) {
        args.set(n, e);
    }

    @Override
    public Query getQuery() {
        return null;
    }

    public void insert(Exp e) {
        if (type() == AND && e.type() == AND) {
            for (Exp ee : e) {
                insert(ee);
            }
        } else {
            args.add(e);
        }
    }

    /**
     * Add Exp rst into this exp 
     * If this exp is BGP{e1 .. en}
     * return: BGP{JOIN{BGP{e1 .. en} rst}}
     */
    public void join(Exp rst, int bgp) {
        if (isBGPAnd() && size() > 0) { // type() == AND
            Exp fst = get(0);
            if (size() == 1) {
                if (!fst.isBGPAnd()) { //first.type() != AND
                    fst = Exp.create(bgp, fst);
                }
            } else {
                fst = Exp.create(bgp);
                for (Exp ee : this) {
                    fst.add(ee);
                }
            }
            //BGP ???
            // first.setBGP(true);
            // e.setBGP(true);
            Exp body = Exp.create(JOIN, fst, rst);
            getExpList().clear();
            args.add(body);
        } 
        else {
            args.add(rst);
        }
    }
    
    /**
     * This is a BGP
     * if it contains several statements (union, minus, optional, graph, query, bgp), JOIN them
     * if it contains statement and basic (eg triple/path/filter/values/bind) 
     * crate BGP for basics and JOIN them
     * otherwise leave as is
     */
    public void dispatch(){
        if (size() == 0){
            return;
        }
        int nb = 0, ns = 0;
        for (Exp exp : this){
            if (exp.isStatement()){
                ns++;
            }
            else {
                nb++;
            }
        }
        if (ns == 0 || (ns == 1 && nb == 0)){
            return;
        }
        doDispatch();
    }
    
    void doDispatch(){
        Exp join  = Exp.create(JOIN);
        Exp basic = Exp.create(BGP);
        for (Exp exp : this){
            if (exp.isStatement()){
                if (basic.size() > 0){
                    join.add(basic);
                    basic = Exp.create(BGP);
                }
                join.add(exp);
            }
            else {
                basic.add(exp);
            }
        }
        if (basic.size() > 0){
            join.add(basic);
        }
        Exp body = join.dispatch(0);
        getExpList().clear();
        add(body);
    }
    
    /**
     * create binary JOIN from nary
     */
    Exp dispatch(int i){
        if (i == size() - 1){
            return last();
        }
        else {
            return Exp.create(JOIN, get(i), dispatch(i+1));
        }
    }
    
    public void add(int n, Exp e) {
        args.add(n, e);
    }

    public boolean remove(Exp e) {
        return args.remove(e);
    }

    public Exp remove(int n) {
        return args.remove(n);
    }

    /**
     * Add a bind exp at the beginning this exp qnode will be bound with node in
     * the stack before processing this exp
     */
    public Exp bind(Node qnode, Node node) {
        Exp bind = create(NODE, qnode);
        Exp value = create(NODE, node);
        bind.add(value);
        Exp and = this;
        if (this.type() != AND) {
            and = create(AND, this);
        }
        and.add(0, bind);
        return and;
    }

    @Override
    public String toString() {    
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
    
    public StringBuilder toString(StringBuilder sb) {
        return toString(sb, 0);
    }
       
    StringBuilder toString(StringBuilder sb, int n) {
//        if (isValues() || isOptional() || isMinus() || isGraph() || isUnion() || isService() || isQuery()){
//             nl(sb, n);
//        }
        sb.append(title()).append(SP);

        if (type() == VALUES) {
            sb.append(getNodeList());
            sb.append(SP);
        }
        
        sb.append("{");
        
        if (edge != null) {
            sb.append(edge);
            if (size() > 0) {
                sb.append(SP);
            }
            if (getBind() != null) {
                sb.append(SP).append(getBind()).append(SP);
            }
        }
        if (node != null) {
            sb.append(node).append(SP);
            if (size() > 0) {
                sb.append(SP);
            }
        }
        if (filter != null) {
            sb.append(filter);
            if (size() > 0) {
                sb.append(SP);
            }
        }

        if (type() == VALUES) {
            nl(sb, 0);
            sb.append(getMappings().toString(true));
        } else if (type() == WATCH || type() == CONTINUE || type() == BACKJUMP) {
            // skip because loop
        } else {
            if (isOptional() && isPostpone()){
                sb.append("POSTPONE ");
                getPostpone().toString(sb);
                nl(sb, n);
            }
            int i = 0;
            for (Exp e : this) {
                nl(sb, n);
                e.toString(sb, n+1).append(SP);
//                if (type() == JOIN && i == 0) {
//                    nl(sb, n);
//                }
                i++;
            }
        }
        
        if (type() == VALUES){
            indent(sb, n);
        }
        sb.append("}");
        return sb;
    }
     
     StringBuilder nl(StringBuilder sb, int n) {
         sb.append(NL);
         indent(sb, n);
         return sb;
     }
     
    void indent(StringBuilder sb, int n) {
        for (int i = 0; i < n; i++) {
            sb.append(SP).append(SP);
        }
    }
        
    String title() {
        return TITLE[type];
    }

    public void skip(boolean b) {
        // TODO Auto-generated method stub
        skip = b;
    }

    public boolean skip() {
        // TODO Auto-generated method stub
        return skip;
    }

    public void status(boolean b) {
        // TODO Auto-generated method stub
        status = b;
    }

    public boolean status() {
        // TODO Auto-generated method stub
        return status;
    }

    public void setFree(boolean b) {
        isFree = b;
    }

    public boolean isFree() {
        return isFree;
    }

    public int type() {
        // TODO Auto-generated method stub
        return type;
    }
    
    public boolean isStatement() {
        switch (type()) {
            case BGP:
            case JOIN:
            case UNION:
            case OPTIONAL:
            case MINUS:
            case GRAPH:
            case QUERY:
                return true;

            default:
                return false;
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int n) {
        index = n;
    }
    
    public boolean isBGPFilter(){
        return isBGP() && isOnly(FILTER);
    }
    
    boolean isOnly(int type){
        for (Exp exp : this){
            if (exp.type() != type){
                return false;
            }
        }
        return true;
    }

    public boolean isFilter() {
        return type == FILTER;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public void setAggregate(boolean b) {
        isAggregate = b;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean b) {
        isSilent = b;
    }

    public boolean isNode() {
        return type == NODE;
    }

    public boolean isEdge() {
        return type == EDGE;
    }
    
    public boolean isEdgePath() {
        return isEdge() || isPath();
    }

    public boolean isEdgesOrFilter() {
        boolean result = true;
        for (Exp e : args) {
            if (!(e.isEdge() || e.isFilter())) {
                return false;
            }
        }
        return ((result) && (args.size() > 1));
    }

    public boolean isOption() {
        return type == OPTION;
    }

    public boolean isOptional() {
        return type == OPTIONAL;
    }

    public boolean isJoin() {
        return type == JOIN;
    }
    
    public boolean isAndJoin() {
        return isJoin() || (isBGPAnd() && size() == 1 && get(0).isJoin());
    }
    
    public boolean isAnd() {
        return type == AND;
    }
    
    public boolean isBinary() {
        return isUnion() || isOptional() || isMinus() || isJoin();
    }

    public boolean isGraph() {
        return type == GRAPH;
    }

    public boolean isUnion() {
        return type == UNION;
    }
    
    public boolean isMinus() {
        return type == MINUS;
    }

    public boolean isQuery() {
        return type == QUERY;
    }
    
    public boolean isService() {
        return type == SERVICE;
    }

    public boolean isAtomic() {
        return type == FILTER || type == EDGE || type == NODE
                || type == ACCEPT;
    }

    public void setType(int n) {
        type = n;
    }

    Exp getNext() {
        return next;
    }

    void setNext(Exp e) {
        next = e;
    }

    public List<Exp> getExpList() {
        return args;
    }

    void getEdgeList(List<Edge> list) {
        for (Exp exp : getExpList()) {
            if (exp.isEdge()) {
                list.add(exp.getEdge());
            } else {
                exp.getEdgeList(list);
            }
        }
    }

    public Exp first() {
        if (args.size() > 0) {
            return args.get(0);
        } else {
            return empty;
        }
    }

    public Exp rest() {
        if (args.size() > 1) {
            return args.get(1);
        } else {
            return null;
        }
    }

    public Exp last() {
        if (args.size() > 0) {
            return args.get(args.size() -1);
        } else {
            return null;
        }
    }

    @Override
    public Iterator<Exp> iterator() {
        return args.iterator();
    }

    public Exp get(int n) {
        return args.get(n);
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge e) {
        edge = e;
    }

    public Regex getRegex() {
        return regex;
    }

    public void setRegex(Regex f) {
        regex = f;
    }

    @Override
    public Mappings getMappings() {
        return map;
    }

    public void setMappings(Mappings m) {
        map = m;
    }
    
    public Mappings getActualMappings() {
        if (getValues() == null){
            return null;
        }
        return getValues().getMappings();
    }
   
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter f) {
        filter = f;
        if (f.isRecAggregate()) {
            setAggregate(true);
        }
    }

    public void addFilter(Filter f) {
        lFilter.add(f);
    }

    public List<Filter> getFilters() {
        return lFilter;
    }
    
    public List<Filter> getFilters(int n, int t) {
        return new ArrayList<Filter>(0);
    }
          
    public boolean isHaving() {
        return getHavingFilter() != null;
    }

    public Filter getHavingFilter() {
        Expr e = getFilter().getExp();
        if (e.arity() >= 3) {
            return e.getExp(2).getFilter();
        }
        return null;
    }

    public boolean isExpGroupBy() {
        return expGroupBy != null;
    }

    public void setExpGroupBy(List<Exp> l) {
        expGroupBy = l;
    }

    public List<Exp> getExpGroupBy() {
        return expGroupBy;
    }

    public boolean isFail() {
        return isFail;
    }

    public void setFail(boolean b) {
        isFail = b;
    }

    public boolean isPath() {
        return type == PATH;
    }
    
     public boolean isValues() {
        return type == VALUES;
    }
     
     public boolean isBind() {
        return type == BIND;
    }

    public void setPath(boolean b) {
        isPath = b;
    }

    public Node getGraphName() {
        return first().first().getNode();
    }
    
    public Node getServiceNode() {
        return first().getNode();
    }

    public Node getGraphNode() {
        return node;
    }

    @Override
    public Node getNode() {
        return node;
    }

    public void setNode(Node n) {
        node = n;
    }

    public List<Node> getNodeList() {
        return lNodes;
    }

    public void setNodeList(List<Node> l) {
        lNodes = l;
    }

    public boolean hasNodeList() {
        return getNodeList() != null && !getNodeList().isEmpty();
    }

    public void addNode(Node n) {
        if (lNodes == null) {
            lNodes = new ArrayList<Node>();
        }
        lNodes.add(n);
    }

    public void setObject(Object o) {
        object = o;
    }

    public Object getObject() {
        return object;
    }

    public void setProducer(Producer p) {
        producer = p;
    }
    
    public Producer getProducer() {
        return producer;
    }

    public Exp getRestore() {
        return (Exp) object;
    }

    public void setRestore(Object o) {
        object = o;
    }  

    public List<Object> getObjectValues() {
        if (object instanceof List) {
            return (List<Object>) object;
        } else {
            return new ArrayList<Object>();
        }
    }

    public void setMin(int i) {
        min = i;
    }

    public int getMin() {
        return min;
    }

    public void setMax(int i) {
        max = i;
    }

    public int getMax() {
        return max;
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack st) {
        stack = st;
    }

    /**
     * use case: select distinct ?x where add an ACCEPT ?x statement to check
     * that ?x is new
     */
    boolean distinct(Node qNode) {
        switch (type()) {

            case UNION:
                //case OPTION:
                boolean success = false;
                for (Exp ee : this) {
                    success = ee.distinct(qNode) || success;
                }
                if (success) {
                    return true;
                }
                break;

            case AND:
            case BGP:
            case GRAPH:

                for (int i = 0; i < size(); i++) {
                    Exp exp = get(i);
                    switch (exp.type()) {
                        case EDGE:
                        case PATH:
                        case XPATH:
                        case EVAL:
                            //Edge edge = exp.getEdge();
                            if (exp.contains(qNode)) {
                                add(i + 1, Exp.create(ACCEPT, qNode));
                                return true;
                            }
                            break;

                        case AND:
                        case GRAPH:
                        case UNION:
                            //case OPTION:
                            if (exp.distinct(qNode)) {
                                return true;
                            }

                            break;

                    }
                }
        }

        return false;
    }

    boolean isSortable() {
        return isEdge() || isPath() || isGraph() || type == OPT_BIND;
    }

    boolean isSimple() {
        switch (type) {
            case EDGE:
            case PATH:
            case EVAL:
            case OPT_BIND:
                return true;
            default:
                return false;
        }
    }

    /**
     * Does edge e have a node bound by map (bindings)
     */
    boolean bind(Mapping map) {
        if (!isEdge()) {
            return false;
        }

        for (int i = 0; i < nbNode(); i++) {
            Node node = getNode(i);
            if (node.isVariable() && map.getNode(node) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * check special case: e1: ?x path ?y e2: graph path {} e2 cannot be moved
     * before e1
     */
    boolean isGraphPath(Exp e1, Exp e2) {
        if (e1.isPath() && e2.isGraph()) {
            Node var1 = e1.getEdge().getEdgeVariable();
            Node var2 = e2.getGraphName();
            if (var1 != null && var2.isVariable() && var1.same(var2)) {
                return true;
            }
        }
        return false;
    }

    public int nBind(List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        if (isSimple()) {
            return count(lNode, lVar, lBind);
        } else {
            return gCount(lNode, lVar, lBind);
        }
    }

    int gCount(List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        int n = 0;
        List<Node> list = getNodes();
        for (Node node : list) {
            n += member(node, lNode, lVar, lBind);
        }
        return n;
    }

    int count(List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        int n = 0;
        for (int i = 0; i < nbNode(); i++) {
            n += member(getNode(i), lNode, lVar, lBind);
        }
        return n;
    }

    int member(Node node, List<Node> lNode, List<String> lVar, List<Exp> lBind) {
        if (node.isConstant()) {
            return 1;
        }
        if (member(node, lBind)) {
            return 1;
        }
        if (lNode.contains(node) || lVar.contains(node.getLabel())) {
            return 2;
        }
        return 0;
    }

    boolean member(Node node, List<Exp> lBind) {
        for (Exp exp : lBind) {
            if (node.same(exp.first().getNode())) {
                return true;
            }
        }
        return false;

    }

    /**
     * list of nodes that are bound by this exp no minus and no exists
     *
     */
    void bind(List<Node> lNode) {
        if (isSimple()) {
            for (int i = 0; i < nbNode(); i++) {
                Node node = getNode(i);
                if (node != null) {
                    bind(node, lNode);
                }
            }
        } else {
            // TODO: check
            List<Node> list = getNodes();
            for (Node node : list) {
                bind(node, lNode);
            }
        }
    }

    void bind(Node node, List<Node> lNode) {
        if (!lNode.contains(node)) {
            lNode.add(node);
        }
    }

    void addBind(List<String> lVar) {
        switch (type()) {
            case EDGE:
            case PATH:
                for (int i = 0; i < nbNode(); i++) {
                    Node node = getNode(i);
                    addBind(node, lVar);
                }
        }
    }

    void addBind(Node node, List<String> lVar) {
        if (node.isVariable()) {
            lVar.add(node.getLabel());
        }
    }

    /**
     * for EDGE exp nodes + edgeNode
     */
    int nbNode() {
        switch (type) {

            case EDGE:
            case PATH:
            case EVAL:
                if (edge.getEdgeVariable() == null) {
                    return edge.nbNode();
                } else {
                    return edge.nbNode() + 1;
                }

            case OPT_BIND:
                return size();
        }

        return 0;
    }

    /**
     * for EDGE exp nodes + edgeNode
     */
    Node getNode(int n) {
        switch (type) {

            case EDGE:
            case PATH:
            case EVAL:

                if (n < edge.nbNode()) {
                    return edge.getNode(n);
                } else {
                    return edge.getEdgeVariable();
                }

            case OPT_BIND:
                return get(n).getNode();
        }
        return null;
    }

    /**
     * for EDGE exp nodes + edgeNode
     */
    public boolean contains(Node node) {
        if (edge.contains(node)) {
            return true;
        }
        Node pNode = edge.getEdgeVariable();
        if (pNode == null) {
            return false;
        }
        return pNode == node;
    }

    /**
     *
     * @param filterVar: variables of a filter
     * @param expVar: list of variables bound by expressions Add in expVar the
     * variables bound by this expression that are in filterVar bound means no
     * optional, no union
     */
    public void share(List<String> filterVar, List<String> expVar) {
        switch (type()) {

            case FILTER:
            case OPT_BIND:
                break;

            case OPTION:
                break;

            case OPTIONAL:
                first().share(filterVar, expVar);
                break;

            case UNION:
                // must be bound in both branches 
                ArrayList<String> lVar1 = new ArrayList<String>();
                ArrayList<String> lVar2 = new ArrayList<String>();
                first().share(filterVar, lVar1);
                rest().share(filterVar, lVar2);
                for (String var : lVar1) {
                    if (lVar2.contains(var) && !expVar.contains(var)) {
                        expVar.add(var);
                    }
                }

                break;

            case QUERY:
                ArrayList<String> lVar = new ArrayList<String>();
                getQuery().getBody().share(filterVar, lVar);

                for (Exp exp : getQuery().getSelectFun()) {
                    String name = exp.getNode().getLabel();
                    if ((lVar.contains(name) || exp.getFilter() != null) && !expVar.contains(name)) {
                        expVar.add(name);
                    }
                }
                break;

            case BIND:
                // bind may not bind the variable (in case of error) 
                // hence variable cannot be considered as bound for filter
                //share(getNode(), filterVar, expVar);
                break;

            case EDGE:
            case PATH:
                for (int i = 0; i < nbNode(); i++) {
                    Node node = getNode(i);
                    share(node, filterVar, expVar);
                }
                break;

            case NODE:
                share(getNode(), filterVar, expVar);
                break;

            case MINUS:
                first().share(filterVar, expVar);
                break;

            default:

                for (Exp exp : this) {
                    exp.share(filterVar, expVar);
                }

        }

    }

    void share(Node node, List<String> fVar, List<String> eVar) {
        if (node != null && node.isVariable()
                && fVar.contains(node.getLabel())
                && !eVar.contains(node.getLabel())) {
            eVar.add(node.getLabel());
        }
    }

    public boolean bound(List<String> fvec, List<String> evec) {
        for (String var : fvec) {
            if (!evec.contains(var)) {
                return false;
            }
        }
        return true;
    }

    public boolean bind(Filter f) {
        List<String> lVar = f.getVariables();
        List<String> lVarExp = new ArrayList<String>();
        share(lVar, lVarExp);
        return bound(lVar, lVarExp);
    }

    /**
     * Return variable nodes of this exp use case: find the variables for select
     * * PRAGMA: subquery : return only the nodes of the select return only
     * variables (no cst, no blanks) minus: return only nodes of first argument
     * inSubScope = true : collect nodes of left of optional and surely bound by union
     * optional = true :  we are inside an optional
     */
    void getNodes(ExpHandler h) {

        switch (type()) {

            case FILTER:
                // get exists {} nodes
                // draft
                getExistNodes(getFilter().getExp(), h.getExistNodeList());
                break;

            case NODE:
                h.add(getNode());
                break;

            case EDGE:
            case PATH:
                Edge edge = getEdge();
                h.add(edge.getNode(0));
                if (edge.getEdgeVariable() != null) {
                    h.add(edge.getEdgeVariable());
                }
                h.add(edge.getNode(1));
                
                for (int i = 2; i < edge.nbNode(); i++) {
                    h.add(edge.getNode(i));
                }
                break;
                
            case XPATH:
            case EVAL:
                for (int i = 0; i < nbNode(); i++) {
                    Node node = getNode(i);
                    h.add(node);
                }
                break;

            case ACCEPT:
                //use case: join() check connection, need all variables
                h.add(getNode());
                break;

            case VALUES:
                for (Node var : getNodeList()) {
                    h.add(var);
                }
                break;

            case MINUS:
                // second argument does not bind anything: skip it
                if (first() != null) {
                    first().getNodes(h);
                }
                break;
                
            case OPTIONAL:
                boolean b = h.isOptional();
                first().getNodes(h.setOptional(true));
                if (! h.isInSubScope()){               
                    rest().getNodes(h);   
                }
                h.setOptional(b);
                break;
                
            case GRAPH: 
                h.add(getGraphName());
                if (size() > 1) {
                    rest().getNodes(h);
                }
                break;
                
            case UNION:
                if (h.isInSubScope()) {
                    // in-subscope record nodes that are bound in both branches of union
                    List<Node> left  = first().getTheNodes(h.copy());
                    List<Node> right =  rest().getTheNodes(h.copy());
                    for (Node node : left) {
                        if (right.contains(node)) {
                            h.add(node);
                        }
                    }
                }
                else {
                   for (Exp ee : this) {
                       ee.getNodes(h);
                   }
                }
                break;
                
            case BIND:
                if (h.isBind()) {
                    if (getNodeList() == null) {
                        h.add(getNode());
                    } else {
                        for (Node node : getNodeList()) {
                            h.add(node);
                        }
                    }
                }
                
                break;
                           
            case QUERY: 
                queryNodeList(h.getSelectNodeList(), h.isInSubScope());
                break;
               
            default:
                // BGP, service, union, named graph pattern
                for (Exp ee : this) {
                    ee.getNodes(h);
                    if (h.isInSubScopeLimited() && (ee.isMinus() || ee.isOptional() || ee.isUnion() || ee.isGraph())) {
                        // skip statements after optional/minus/union for in-subscope nodes
                        break;
                    }                    
                }
        }

    }
    
  
    void queryNodeList(List<Node> selectList, boolean inSubScope) {
        List<Node> subSelectList = getQuery().getSelectNodeList();
        if (inSubScope) {
            // focus on left optional in query body 
            // because otherwise select * includes right optional
            List<Node> scopeList = getQuery().getBody().getInScopeNodes();
            for (Node node : scopeList) {
                if (subSelectList.contains(node)) {
                    add(selectList, node);
                }
            }
        } else {
            for (Node node : subSelectList) {
                add(selectList, node);
            }
        }
    }

    /**
     * For modularity reasons, Pattern is stored as ExpPattern interface
     */
    public Exp getPattern(Expr exp) {
        return (Exp) exp.getPattern();
    }

    /**
     * This is a filter get exists{} nodes if any
     */
    void getExistNodes(Expr exp, List<Node> lExistNode) {
        switch (exp.oper()) {

            case ExprType.EXIST:
                Exp pat = getPattern(exp);
                List<Node> lNode = pat.getTheNodes(handler(true, false, true, false));
                for (Node node : lNode) {
                    add(lExistNode, node);
                }
                break;

            default:
                for (Expr ee : exp.getExpList()) {
                    getExistNodes(ee, lExistNode);
                }

        }
    }

    /**
     * select * return nodes that (may) bind variables, do not return exists and
     * minus nodes
     */
    public List<Node> getNodes() {
        List<Node> list = getTheNodes(handler(false, false, true, false));
        return list;
    }
    
    public List<Node> getRecordInScopeNodes() {
        if (getInScopeNodeList() == null) {
            setInScopeNodeList(getInScopeNodes());
        }
        return getInScopeNodeList();
    }
   
    public List<Node> getRecordInScopeNodesForService() {
        return getRecordInScopeNodes(false);
    }

    public List<Node> getRecordInScopeNodes(boolean bind) {
        if (getInScopeNodeList() == null) {
            setInScopeNodeList(getInScopeNodes(bind));
        }
        return getInScopeNodeList();
    }
    
    /**
     * in-scope nodes minus nodes in right optional (as well as in subquery right optional)
     * @return 
     */
    public List<Node> getInScopeNodes() {
        return getInScopeNodes(true);
    }
    
    /**
     * 
     */
    public List<Node> getAllInScopeNodes() {
        return getTheNodes(handler(false, true, true, false).setAll(true));
    }
       
    List<Node> getInScopeNodes(boolean bind) {
        return getTheNodes(handler(false, true, bind, false));
    }

    public List<Node> getAllNodes() {
        return getTheNodes(handler(false, false, true, true));
    }
    
    ExpHandler handler(boolean exist, boolean inSubScope, boolean bind, boolean blank) {
        return new ExpHandler(exist, inSubScope, bind, blank);
    }
   
    
    public List<Node> getTheNodes(ExpHandler h){
        getNodes(h);
        // add select nodes that are not in lNode
        for (Node selectNode : h.getSelectNodeList()) {
            if (!h.getNodeList().contains(selectNode)) {                                                 
                h.getNodeList().add(overloadSelectNodeByExistNode(h.getExistNodeList(), selectNode));
            }
        }

        if (h.isExist()) {
            // collect exists {} nodes
            for (Node existNode : h.getExistNodeList()) {
                if (!h.getNodeList().contains(existNode)) {
                    h.getNodeList().add(existNode);
                }
            }
        }
        
        return h.getNodeList();
    }
    
  
    /**
    * use case: 
    * select * where { 
    * {select * where {?x rdf:rest * / rdf:first ?y}} 
    * filter(! exists {?x rdf:first ?y}) } 
    * lNode = {} lSelNode = {?x, ?y} lExistNode = {?x, ?y} 
    * select nodes of subquery are overloaded by exists nodes
    */    
    Node overloadSelectNodeByExistNode(List<Node> list, Node node) {
        if (list.contains(node)) {            
            return get(list, node);
        } else {
            return node;
        }
    }

    void add(List<Node> lNode, Node node) {
        add(lNode, node, false);
    }

    void add(List<Node> lNode, Node node, boolean blank) {
        if (node != null
                && (blank || (node.isVariable() && !node.isBlank()))
                && !lNode.contains(node)) {
            lNode.add(node);
        }
    }

    boolean contain(List<Exp> lExp, Node node) {
        for (Exp exp : lExp) {
            if (exp.getNode().equals(node)) {
                return true;
            }
        }
        return false;
    }

    Node get(List<Node> lNode, Node node) {
        for (Node qNode : lNode) {
            if (qNode.equals(node)) {
                return qNode;
            }
        }
        return null;
    }

    /**
     * compute the variable list use case: filter(exists {?x ?p ?y}) no minus
     */
    @Override
    public void getVariables(List<String> list) {
        getVariables(list, false);
    }

    @Override
    public void getVariables(List<String> list, boolean excludeLocal) {
        List<Node> lNode = getNodes();
        for (Node node : lNode) {
            String name = node.getLabel();
            if (!list.contains(name)) {
                list.add(name);
            }
        }
        // go into filters if any
        getFilterVar(list, excludeLocal);
    }

    public void getFilterVar(List<String> list, boolean excludeLocal) {
        switch (type) {

            case FILTER:
                List<String> lVar = getFilter().getVariables(excludeLocal);
                for (String var : lVar) {
                    if (!list.contains(var)) {
                        list.add(var);
                    }
                }
                break;

            default:
                for (Exp exp : getExpList()) {
                    exp.getFilterVar(list, excludeLocal);
                }

        }
    }

    boolean isBindCst() {
        return type() == OPT_BIND && size() == 1;
    }

    boolean isBindVar() {
        return type() == OPT_BIND && size() == 2;
    }

    /**
     * Add BIND ?x = ?y
     */
    List<Exp> varBind() {
        List<Exp> lBind = new ArrayList<Exp>();
        for (int i = 0; i < size(); i++) {
            Exp f = get(i);
            if ((f.type == VALUES) || (f.isFilter() && f.size() > 0)) {
                Exp bind = f.first();
                if (bind.type() == OPT_BIND) {
                    if (bind.isBindCst()) {
                        // ?x = cst
                        lBind.add(bind);
                    } else {
                        // filter has BIND
                        // TODO: when there is a UNION, variables of BIND may not be all bound 
                        // so we cannot bind here
                        //add(i, bind);
                        //i++;
                    }
                }
            }
        }
        return lBind;
    }

    /**
     * If a filter carry a bind, set the bind into its edge or path
     */
    void setBind() {
        for (int i = 1; i < size(); i++) {
            Exp f = get(i);
            if (f.isFilter() && f.size() > 0) {
                Exp bind = f.first();
                if (bind.type() == OPT_BIND
                        // no bind (?x = ?y) in case of JOIN
                        && (!Query.testJoin || bind.isBindCst())) {
                    int j = i - 1;
                    while (j > 0 && get(j).isFilter()) {
                        j--;
                    }
                    if (j >= 0) {
                        Exp g = get(j);
                        if ((g.isEdge() || g.isPath())
                                && (bind.isBindCst() ? g.bind(bind.first().getNode()) : true)) {
                            bind.status(true);
                            g.setBind(bind);
                        }
                    }
                }
            }
        }
    }

    /**
     * Edge bind node
     */
    boolean bind(Node node) {
        for (int i = 0; i < nbNode(); i++) {
            if (getNode(i).equals(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * graph ?g {} filter(f(?g)) add filter to GRAPHNODE(?g, FILTER(f(?g))
     *
     */
    void graphFilter() {
        for (int i = 0; i < size(); i++) {
            Exp f = get(i);
            if (f.isFilter() && i >= 1 && get(i - 1).isGraph()) {
                Exp graph = get(i - 1);
                Node gNode = graph.getGraphName();
                if (match(gNode, f.getFilter())) {
                    graph.first().add(f);
                }
            }
        }
    }

    boolean match(Node node, Filter f) {
        if (!node.isVariable() || f.getExp().isRecExist()) {
            return false;
        }
        List<String> lVar = f.getVariables();
        if (lVar.size() != 1) {
            return false;
        }
        return lVar.get(0).equals(node.getLabel());
    }

    /**
     * use case:
     *
     * ?x c:FirstName ?n filter(?n < 'B')
     *
     * ?x c:FirstName ?n filter(?n < ?n1)
     */
    void edgeFilter() {
        for (int i = 0; i < size(); i++) {
            Exp f = get(i);
            if (f.isFilter() && f.size() > 0 && f.get(0).type() == TEST
                    && i >= 1 && get(i - 1).isEdge()) {
                Exp edge = get(i - 1);
                if (match(edge, f)) {
                    edge.add(f);
                }
            }
        }
    }

    /**
     * ?x c:FirstName ?n ?n < ?n1
     */
    boolean match(Exp edge, Exp filter) {
        Exp test = filter.get(0);
        for (Exp exp : test) {
            Node node = exp.getNode();
            if (edge.contains(node)) {
                int indexNode = edge.indexNode(node);
                int indexVar = filter.indexVar(node);
                test.setIndex(indexNode);
                test.setObject(indexVar);
                test.setNode(node);
//				if (check(filter, indexVar)){
//					filter.status(true);
//				}
                filter.status(true);
                test.status(order(filter, indexVar));

                return true;
            }
        }
        return false;
    }

    /**
     * this is FILTER with TEST ?x < ?y
     */
    public int oper() {
        int ope = getFilter().getExp().oper();
        return ope;
    }

    boolean check(Exp filter, int index) {
        int oper = filter.oper();
        if (oper == ExprType.LT || oper == ExprType.LE) {
            return index == 0;
        } else if (oper == ExprType.GT || oper == ExprType.GE) {
            return index == 1;
        }
        return false;
    }

    boolean order(Exp filter, int index) {
        int oper = filter.oper();
        if (oper == ExprType.LT || oper == ExprType.LE) {
            return index == 0;
        } else if (oper == ExprType.GT || oper == ExprType.GE) {
            return index == 1;
        }
        return true;
    }

    /**
     * index of Node in Edge
     */
    public int indexNode(Node node) {
        if (!isEdge()) {
            return -1;
        }
        for (int i = 0; i < nbNode(); i++) {
            if (node.same(getNode(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * index of node in FILTER ?x < ?y
     */
    public int indexVar(Node node) {
        Expr ee = getFilter().getExp();
        String name = node.getLabel();
        for (int i = 0; i < 2; i++) {
            if (ee.getExp(i).type() == ExprType.VARIABLE
                    && ee.getExp(i).getLabel().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * *****************************************************************
     *
     * Draft Join
     *
     */
    /**
     * If content is disconnected, generate join(e1, e2).
     */
    Exp join() {
        List<Node> connectedNode = null;
        Exp connectedExp = Exp.create(AND);
        List<Exp> disconnectedExp = new ArrayList<Exp>();
        boolean disconnectedFilter = false;

        for (int i = 0; i < size(); i++) {
            Exp e = get(i);

            switch (e.type()) {

                case FILTER:
                    Filter f = e.getFilter();
                    List<String> lvar = f.getVariables();

                    if (connectedNode == null || isBound(lvar, connectedNode)) {
                        // filter is first 
                        // or filter is bound by current exp : add it to exp
                        connectedExp.add(e);
                    } else {
                        // filter not bound by current exp
                        if (!disconnectedFilter) {
                            add(disconnectedExp, connectedExp);
                            disconnectedFilter = true;
                        }
                        add(disconnectedExp, e);
                    }
                    continue;

                case OPTION:
                    if (connectedNode == null) {
                        connectedNode = e.getAllNodes();
                    } else {
                        connectedNode.addAll(e.getAllNodes());
                    }

                    break;

                default:
                    // TODO: UNION 
                    List<Node> nodes = null;
                    if (type() == MINUS || type() == OPTIONAL) {
                        nodes = e.first().getAllNodes();
                    } else {
                        nodes = e.getAllNodes();
                    }

                    if (disconnectedFilter) {
                        if (!groupEdge) {
                            connectedExp = Exp.create(AND);
                            connectedNode = null;
                        }
                        disconnectedFilter = false;
                    }

                    if (connectedNode == null) {
                        connectedNode = nodes;
                    } else if (intersect(nodes, connectedNode)) {
                        connectedNode.addAll(nodes);
                    } else {
                        add(disconnectedExp, connectedExp);
                        connectedExp = Exp.create(AND);
                        connectedNode = nodes;
                    }
            }

            connectedExp.add(e);
        }

        if (connectedExp.size() > 0) {
            add(disconnectedExp, connectedExp);
        }

        if (disconnectedExp.size() <= 1) {
            return this;
        } else {
            Exp res = join(disconnectedExp);
            //System.out.println("E: " + res);
            return res;
        }
    }

    void add(List<Exp> list, Exp exp) {
        if (!list.contains(exp)) {
            list.add(exp);
        }
    }

    /**
     * JOIN the exp of the list, except filters which are in a BGP with
     * preceding exp list = e1 e2 f1 e3 return JOIN(AND(JOIN(e1, e2) f1), e3 ).
     */
    Exp join(List<Exp> list) {
        Exp exp = list.get(0);

        for (int i = 1; i < list.size(); i++) {

            Exp cur = list.get(i);

            if (cur.type() == FILTER || exp.type() == FILTER) {
                // and
                if (exp.type() == AND) {
                    exp.add(cur);
                } else {
                    exp = Exp.create(AND, exp, cur);
                }
            } else {
                // variables that may be bound from environment (e.g. values)
//                exp.setNodeList(exp.getNodes());
//                cur.setNodeList(cur.getNodes());
                exp = Exp.create(JOIN, exp, cur);
                exp.bindNodes();
            }
        }

        return exp;
    }

    /**
     * Nodes that may be bound by previous clause or by environment except
     * minus, etc.
     * use case:  join
     */
    void bindNodes() {
        for (Exp exp : getExpList()) {
            exp.setNodeList(exp.getInScopeNodes());
        }
    }

    boolean isBound(List<String> lvar, List<Node> lnode) {
        for (String var : lvar) {
            if (!isBound(var, lnode)) {
                return false;
            }
        }
        return true;
    }

    boolean isBound(String var, List<Node> lnode) {
        for (Node node : lnode) {
            if (node.isVariable() && node.getLabel().equals(var)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersect(List<Node> nodes, List<Node> list) {
        for (Node node : nodes) {
            if (list.contains(node)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public void cache(Node n) {
        setCacheNode(n);
        cache = new HashMap<Node, Mappings>();
    }

    boolean hasCache() {
        return cache != null;
    }

    void cache(Node n, Mappings m) {
        cache.put(n, m);
    }

    Mappings getMappings(Node n) {
        Mappings m = cache.get(n);
        return m;
    }
    
    /**
     * {?x ex:p ?y} optional {?y ex:q ?z  filter(?z != ?x) optional {?z ?p ?x}}
     * filter ?x is not bound in optional ...
     * this postponed filter must be processed at the end of optional after join occurred
     */
    public void optional() {
        Exp p = Exp.create(BGP);
        Exp rest = rest();
        for (Exp exp : rest) { 
            if (exp.isFilter() && ! rest.simpleBind(exp.getFilter())) {
                p.add(exp);
                exp.setPostpone(true);
            } 
            else if (exp.isOptional() || exp.isMinus()) {
                Exp first = exp.first();                
                for (Exp e : first) {   
                    if (e.isFilter() && ! first.simpleBind(e.getFilter())) {
                        p.add(e);
                        e.setPostpone(true);
                    }
                }
            }
        }
        if (p.size() > 0) {
            setPostpone(p);
            setPostpone(true);
       }
    }
    
    /**
     * Filter variables of f are bound by triple, path, values or bind, locally in this Exp
     */
    boolean simpleBind(Filter f){
        List<String> varList = f.getVariables();
        List<String> nodeList = getNodeVariables();
        
        for (String var : varList) {
            if (! nodeList.contains(var)){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Node variables of edge, path, bind, values
     */
    List<String> getNodeVariables() {
        List<String> list = new ArrayList<>();
        for (Exp exp : this) {
            switch (exp.type()) {
                case EDGE:
                case PATH:
                    exp.getEdgeVariables(list);
                    break;
                case BIND: 
                    exp.getBindVariables(list);
                    break;
                case VALUES:
                    exp.getValuesVariables(list);
                    break;
            }
        }
        return list;
    }

    void getBindVariables(List<String> list) {
        if (getNodeList() == null) {
            addVariable(list, getNode());
        }
        else {
            getValuesVariables(list);
        }
    }
    
    void getValuesVariables(List<String> list) {
        for (Node node : getNodeList()) {
            addVariable(list, node);
        }
    }

    void getEdgeVariables(List<String> list) {
        addVariable(list, getEdge().getNode(0));
        addVariable(list, getEdge().getNode(1));
        if (! isPath()) {
            addVariable(list, getEdge().getPredicate());
        }
    }
    
    void addVariable(List<String> list, Node node) {
        if (node.isVariable() && ! list.contains(node.getLabel())) {
            list.add(node.getLabel());
        }
    }
    
    // optional postponed filters
    public Exp getPostpone(){
        return postpone;
    }
    
    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }
    
    @Override
    public PointerType pointerType(){
        return STATEMENT;
    }
    
     @Override
    public String getDatatypeLabel() {
        return String.format("[Statement]");
    }
    
    @Override
    public Exp getStatement() {
        return this;
    }
    
    @Override
    public Object getValue(String var, int n) {
        if (n < size()) {
            return get(n);
        }
        return null;
    }
    
}
