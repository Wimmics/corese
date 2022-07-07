package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPING;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.core.TripleStore;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Result;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.kgram.tool.ApproximateSearchEnv;
import fr.inria.corese.kgram.tool.EnvironmentImpl;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.IDatatypeList;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

/*
 * An elementary result of a query or a subquery
 * Store query/target nodes and edges 
 * Store path edges in case of path node
 * Store order by nodes
 * Store nodes for select fun() as ?var
 * 
 * Implements Environment to enable evaluate having (?count>50)
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2009
 */
public class Mapping
        extends EnvironmentImpl
        implements Result, Environment, Pointerable {


    public static boolean DEBUG_DEFAULT = false; 
    static final Edge[] emptyEdge = new Edge[0];
    static final Node[] emptyNode = new Node[0];
    private Edge[] queryEdges;
    private Edge[] targetEdges;
    private Node[] queryNodes;
    private Node[] targetNodes;
    private Node[] selectNodes;
    private Node[] orderByNodes;
    private Node[] groupByNodes;
    private Node[] distinctNodes;
    private Node[] groupAlterNodes;
    // record group Mappings when group by
    Mappings lMap;
    // var -> Node
    HashMap<String, Node> values;
    // aggregate may need to share bnode map 
    Map<String, IDatatype> bnode;
    // Mapping as Environment
    Query query;
    // current named graph URI for eval filter
    private Node graphNode;
    // value of graph ?g variable when eval named graph pattern
    private Node targetGraphNode;
    private Binding bind;
    private Eval eval;
    private IDatatype report;
    boolean debug = DEBUG_DEFAULT;
    
    public Mapping() {
        init(emptyEdge, emptyEdge);
        init(emptyNode, emptyNode);
    }

    Mapping(Edge[] query, Edge[] result, Node[] qnodes, Node[] nodes) {
        init(query, result);
        init(qnodes, nodes);
    }

    Mapping(Node[] qnodes, Node[] nodes) {
        init(emptyEdge, emptyEdge);
        init(qnodes, nodes);
    }

    public Mapping(List<Node> q, List<Node> t) {
        this();
        init(q, t);
    }

    static Mapping fake(Query q) {
        Mapping m = new Mapping();
        m.setOrderBy(new Node[q.getOrderBy().size()]);
        m.setGroupBy(new Node[q.getGroupBy().size()]);
        return m;
    }

    public static Mapping create(List<Node> q, List<Node> t) {
        return new Mapping(q, t);
    }

    public static Mapping create() {
        return new Mapping();
    }
    
    public static Mapping create(Binding b) {
        Mapping m = new Mapping();
        m.setBind(b);
        return m;
    }
    
    public static Mapping create(Node[] qnodes, Node[] nodes) {
        return simpleCreate(qnodes, nodes);
    }

    static Mapping simpleCreate(Node[] qnodes, Node[] nodes) {
        return new Mapping(qnodes, nodes);
    }

    public static Mapping safeCreate(Node[] qnodes, Node[] nodes) {
        for (Node node : nodes) {
            if (node == null) {
                return cleanCreate(qnodes, nodes);
            }
        }
        return simpleCreate(qnodes, nodes);
    }

    static Mapping cleanCreate(Node[] qnodes, Node[] nodes) {
        ArrayList<Node> query = new ArrayList<>();
        ArrayList<Node> value = new ArrayList<>();
        int i = 0;
        for (Node node : nodes) {
            if (node != null) {
                query.add(qnodes[i]);
                value.add(nodes[i]);
            }
            i++;
        }
        return create(query, value);
    }

    public static Mapping create(Node qnode, Node node) {
        Node[] qnodes = new Node[1],
                nodes = new Node[1];
        qnodes[0] = qnode;
        nodes[0] = node;
        return new Mapping(qnodes, nodes);
    }

//    public static Mapping create(Node q1, Node n1, Node q2, Node n2) {
//        Node[] qnodes = new Node[2],
//                nodes = new Node[2];
//        qnodes[0] = q1;
//        nodes[0] = n1;
//        qnodes[1] = q2;
//        nodes[1] = n2;
//        return new Mapping(qnodes, nodes);
//    }

//    public static Mapping create(Edge query, Edge result) {
//        ArrayList<Node> qNodes = new ArrayList<>(),
//                tNodes = new ArrayList<>();
//        for (int i = 0; i < query.nbNode(); i++) {
//            Node node = query.getNode(i);
//            if (node.isVariable()) {
//                qNodes.add(node);
//                tNodes.add(result.getNode(i));
//            }
//        }
//        if (query.getEdgeVariable() != null) {
//            qNodes.add(query.getEdgeVariable());
//            tNodes.add(result.getEdgeNode());
//        }
//        return new Mapping(qNodes, tNodes);
//    }

//    public static Mapping create(Edge[] query, Edge[] result,
//            Node[] qnodes, Node[] nodes) {
//        return new Mapping(query, result, qnodes, nodes);
//    }
    
    
     /**
     * TODO: remove duplicates in getVariables()
     * use case:
     * function us:fun(?x){let (select ?x where {}) {}}
     * variable ?x appears twice in the stack because it is redefined in the let clause
     */
    public static Mapping create(Query q, Binding b) {
        ArrayList<Node> lvar = new ArrayList();
        ArrayList<Node> lval = new ArrayList();
        for (Expr var : b.getVariables()) {
            Node node = q.getProperAndSubSelectNode(var.getLabel());
            if (node != null && !lvar.contains(node)) {
                lvar.add(node);
                lval.add(b.get(var));
            }
        }
        Mapping m = Mapping.create(lvar, lval);
        return m;
    }
    
    

    void init(List<Node> q, List<Node> t) {
        Node[] qn = new Node[q.size()];
        Node[] tn = new Node[t.size()];
        qn = q.toArray(qn);
        tn = t.toArray(tn);
        init(qn, tn);
    }

    /**
     * Complete Mapping with select (exp as var) pragma: setNodeValue already
     * done
     *
     *
     */
    void complete(List<Node> q, List<Node> t) {
        Node[] qn = new Node[getQueryNodes().length + q.size()];
        Node[] tn = new Node[getTargetNodes().length + t.size()];
        System.arraycopy(getQueryNodes(), 0, qn, 0, getQueryNodes().length);
        System.arraycopy(getTargetNodes(), 0, tn, 0, getTargetNodes().length);
        int j = 0;
        for (int i = getQueryNodes().length; i < qn.length; i++) {
            qn[i] = q.get(j);
            tn[i] = t.get(j);
            j++;
        }
        init(qn, tn);
    }

    void init(Node[] qnodes, Node[] nodes) {
        this.setQueryNodes(qnodes);
        this.setTargetNodes(nodes);
        initValues();
    }
    
    void init(Edge[] query, Edge[] result) {
        setQueryEdges(query);
        setTargetEdges(result);
    }

    public void initValues() {
        if (values == null) {
            // use case: select (exp as var), values already exists
            values = new HashMap<>();
        }
        int i = 0;
        for (Node q : getQueryNodes()) {
            if (i < getTargetNodes().length) {
                if (q != null && q.isVariable() && getTargetNodes()[i] != null) {
                    setNodeValue(q, getTargetNodes()[i]);
                }
            }
            i++;
        }
    }

    @Deprecated
    public void bind(Node qNode, Node tNode) {
        Node[] qq = new Node[getQueryNodes().length + 1];
        Node[] tt = new Node[getTargetNodes().length + 1];
        int i = 0;
        for (Node q : getQueryNodes()) {
            qq[i] = q;
            tt[i] = getTargetNodes()[i];
            i++;
        }
        qq[i] = qNode;
        tt[i] = tNode;
        setQueryNodes(qq);
        setTargetNodes(tt);
    }

    @Override
    public int count() {
        if (lMap == null) {
            return 0;
        }
        return lMap.count();
    }

    @Override
    public int size() {
        return getQueryNodes().length;
    }

    /**
     * Project on select variables of query Modify this Mapping
     */
    public void project(Query q) {
        ArrayList<Node> lqNodes = new ArrayList<>();
        ArrayList<Node> ltNodes = new ArrayList<>();

        for (Node qNode : q.getSelect()) {
            Node tNode = getNode(qNode);
            if (tNode != null) {
                lqNodes.add(qNode);
                ltNodes.add(tNode);
            }
        }
        init(lqNodes, ltNodes);
    }

    void setOrderBy(Node[] nodes) {
        setOrderByNodes(nodes);
    }

    void setOrderBy(Node node) {
        setOrderByNodes(new Node[1]);
        getOrderByNodes()[0] = node;
    }

    void setGroupBy(Node[] nodes) {
        setGroupByNodes(nodes);
    }
    
    public void dispose() {
        setMappings(null);
    }

    @Override
    public Mappings getMappings() {
        return lMap;
    }

    void setMappings(Mappings l) {
        lMap = l;
    }

    void setQuery(Query q) {
        query = q;
    }

    @Override
    public Query getQuery() {
        return query;
    }

    void setMap(Map<String, IDatatype> m) {
        bnode = m;
    }

    @Override
    public Map<String, IDatatype> getMap() {
        return bnode;
    }

    public Node[] getOrderBy() {
        return getOrderByNodes();
    }

    public Node[] getGroupBy() {
        return getGroupByNodes();
    }

    public void setSelect(Node[] nodes) {
        setSelectNodes(nodes);
    }

    public Node[] getSelect() {
        return getSelectNodes();
    }

    @Deprecated
    public void rename(Node oName, Node nName) {
        int i = 0;
        for (Node qn : getQueryNodes()) {
            if (qn != null && qn.getLabel().equals(oName.getLabel())) {
                getQueryNodes()[i] = nName;
                return;
            }
            i++;
        }
    }

    @Override
    public Path getPath(Node qNode) {
        Node node = getNode(qNode);
        if (node == null) {
            return null;
        }
        return node.getPath();
    }

    public Path getPath(String name) {
        Node qNode = getQueryNode(name);
        if (qNode == null) {
            return null;
        }
        return getPath(qNode);
    }

    /**
     * Index of qNode in mapping (not in stack)
     */
    int getIndex(Node qNode) {
        int i = 0;
        for (Node node : getQueryNodes()) {
            if (qNode == node) {
                return i;
            }
            i++;
        }
        return i;
    }

    @Override
    public int pathLength(Node qNode) {
        Path path = getPath(qNode);
        if (path == null){
            return -1;
        }
        return path.length();
    }

    @Override
    public int pathWeight(Node qNode) {
        Path path = getPath(qNode);
        if (path == null){
            return -1;
        }
        return path.weight();
    }

    boolean isPath(int n) {
        return  getPath(n) != null;
    }

    public Path getPath(int n) {
        if (getTargetNodes()[n] == null) {
            return null;
        }
        return getTargetNodes()[n].getPath();
    }

    boolean isPath(Node qNode) {
        return  isPath(getIndex(qNode));
    }
    
    @Override
    public String getDatatypeLabel() {
        return toString(" ");
    }

    @Override
    public String toString() {
        return toString("\n");
    }
    
    @Override
    public Object getObject() {
        return this;
    }
    
    @Override
    public Object getPointerObject() {
        return this;
    }
     
    String toString(String sep) {    
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Node e : getTargetNodes()) {
            sb.append(getQueryNodes()[i]); //.append("[").append(qNodes[i].getIndex()).append("]");
            sb.append(" = ").append(e).append(sep);
            if (e != null && e.getNodeObject() != null && e.getNodeObject() != this) {
                if ((e.getNodeObject() instanceof TripleStore)) { 
                }
                else {
                    sb.append(sep).append(e.getNodeObject()).append(sep);
                }
            } 
            i++;
        }

        return sb.toString();
    }

    public List<Node> getNodes(Node var) {
        return getNodes(var.getLabel());
    }

    public List<Node> getNodes(String var) {
        return getNodes(var, false);
    }

    public List<Node> getNodes(String var, boolean distinct) {
        List<Node> list = new ArrayList<>();
        if (getMappings() != null) {
            for (Mapping map : getMappings()) {
                Node n = map.getNode(var);
                if (n != null) {
                    if (distinct && list.contains(n)) {
                    } else {
                        list.add(n);
                    }
                }
            }
        }
        return list;
    }

    void init() {
    }

    /**
     * min(?l, groupBy(?x, ?y)) store value of ?x ?y in an array
     */
    void setGroup(List<Node> list) {
        setGroupAlter(new Node[list.size()]);
        set(list, getGroupAlter());
    }

    void computeDistinct(List<Node> list) {
        setDistinctNodes(new Node[list.size()]);
        set(list, getDistinctNodes());
    }
    
    void prepareModify(Query q) {
        if (!q.getOrderBy().isEmpty()) {
            setOrderBy(new Node[q.getOrderBy().size()]);
        }
        if (!q.getGroupBy().isEmpty()) {
            // group by node retrieved in variable hashmap 
            setGroupBy(new Node[0]);
        }
    }

    void set(List<Node> list, Node[] array) {
        int i = 0;
        for (Node qNode : list) {
            Node node = getNode(qNode);
            array[i++] = node;
        }      
    }

    /**
     * min(?l, groupBy(?x, ?y)) retrieve value of ?x ?y in an array
     */
    Node getGroupNode(int n) {
        return getGroupAlter()[n];
    }

    Node[] getGroupNodes() {
        return getGroupAlter();
    }

    Node getDistinctNode(int n) {
        return getDistinctNodes()[n];
    }

    public Node[] getDistinct() {
        return getDistinctNodes();
    }

    public Node getTNode(Node node) {
        return getNode(node);
    }

    public Node getGroupBy(int n) {
        return getGroupByNodes()[n];
    }

    public Node getGroupBy(Node qNode, int n) {
        if (getGroupByNodes().length == 0) {
            return getNode(qNode);
        }
        return getGroupByNodes()[n];
    }

    public void setNode(Node qNode, Node node) {
        int n = 0;
        for (Node qrNode : getQueryNodes()) {
            if (qNode.same(qrNode)) {
                // overload variable value
                setNode(qNode, node, n);
                return;
            }
            n++;
        }
        addNode(qNode, node);
    }
    
    void setNode(Node qNode, Node node, int n) {
        getTargetNodes()[n] = node;
        if (qNode.isVariable()) {
            setNodeValue(qNode, node);
        }
    }
    
    public void setNode(Node node, int n) {
       setNode(getQueryNode(n), node, n);
    }

    public Mapping project(Node q) {
        Node value = getNodeValue(q);
        if (value == null) {
            return null;
        }
        return create(q, value);
    }

    /**
     * rename query nodes Used by Producer.map() to return Mappings
     */
    public void setNodes(List<Node> lNodes) {
        int n = 0;
        for (Node qNode : lNodes) {
            if (n < getQueryNodes().length) {
                getQueryNodes()[n++] = qNode;
            }
        }
    }

    /**
     * use case: bind(sparql('select ?x ?y where { ... }') as (?z, ?t)) rename
     * ?x as ?z and ?y as ?t in all Mapping as well as in Mappings select
     *
     */
    public void rename(List<Node> oselect, List<Node> nselect) {
        int size = Math.min(oselect.size(), nselect.size());
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < getQueryNodes().length; j++) {
                if (oselect.get(i).equals(getQueryNodes()[j])) {
                    getQueryNodes()[j] = nselect.get(i);
                    break;
                }
            }
        }
    }

    // TODO: manage Node isPath
    public void fixQueryNodes(Query q) {
        for (int i = 0; i < getQueryNodes().length; i++) {
            Node node = getQueryNodes()[i];
            Node qnode = q.getOuterNodeSelf(node);
            getQueryNodes()[i] = qnode;
        }
        setQueryEdges(emptyEdge);
        setTargetEdges(emptyEdge);
    }

    public void addNode(Node qNode, Node node) {
        Node[] q = new Node[getQueryNodes().length + 1];
        Node[] t = new Node[getTargetNodes().length + 1];
        System.arraycopy(getQueryNodes(), 0, q, 0, getQueryNodes().length);
        System.arraycopy(getTargetNodes(), 0, t, 0, getTargetNodes().length);
        q[q.length - 1] = qNode;
        t[t.length - 1] = node;
        setQueryNodes(q);
        setTargetNodes(t);
        setNodeValue(qNode, node);
    }

    public void setOrderBy(int n, Node node) {
        getOrderByNodes()[n] = node;
    }

    public void setGroupBy(int n, Node node) {
        getGroupByNodes()[n] = node;
    }

    public Node getNode(int n) {
        return getTargetNodes()[n];
    }
    
    public Node getNodeProtect(int n) {
        if (n < getTargetNodes().length) {
            return getTargetNodes()[n];
        }
        return null;
    }
    

    @Override
    public Node getQueryNode(int n) {
        return getQueryNodes()[n];
    }

    public Object getNodeObject(String name) {
        Node node = getNode(name);
        if (node == null) {
            return null;
        }
        return node.getNodeObject();
    }

    public HashMap<String, Node> getNodeValues() {
        return values;
    }

    // variable name only
    public Node getNodeValue(String name) {
        return values.get(name);
    }

    public Node getNodeValue(Node q) {
        if (q.isVariable()) {
            return getNodeValue(q.getLabel());
        }
        return null;
    }

    public void setNodeValue(Node q, Node t) {
        if (q.isVariable()) {
            setNodeValue(q.getLabel(), t);
        }
    }

    public void setNodeValue(String q, Node t) {
        if (t == null) {
            values.remove(q);
        }
        else {
            values.put(q, t);
        }
    }

    public Set<String> getVariableNames() {
        return values.keySet();
    }

    public IDatatype getValue(String name) {
        Node n = getNode(name);
        if (n == null) {
            return null;
        }
        return n.getDatatypeValue();
    }

    public IDatatype getValue(Node qn) {
        Node n = getNode(qn);
        if (n == null) {
            return null;
        }
        return n.getDatatypeValue();
    }
    
    @Override
    public Node getNode(Node node) {
        if (node.isVariable()) {
            return getNodeValue(node.getLabel());
        }
        return getNodeBasic(node);
    }
    
    @Override
    public Node getNode(String label) {
        return getNodeValue(label);
    }
    
    Node getNodeBasic(Node node) {
        int n = 0;
        for (Node qnode : getQueryNodes()) {
            if (node.same(qnode)) {
                return getTargetNodes()[n];
            }
            n++;
        }
        return null;
    }

    Node getNodeBasic(String label) {
        int n = 0;
        for (Node qnode : getQueryNodes()) {
            if (qnode.getLabel().equals(label)) {
                return getTargetNodes()[n];
            }
            n++;
        }
        return null;
    }
    
    /**
     * Use case:
     * let (((?var, ?val)) = ?m)
     * let ((?x, ?y) = ?m) 
     */
    @Override
    public Object getValue(String var, int n) {
        if (var == null){
            // let (((?var, ?val)) = ?m)  -- ?m : Mapping
            // compiled as: let (?vv = xt:get(?m, 0), (?var, ?val) = ?vv)
            // xt:get(?m, 0) evaluated as xt:gget(?m, null, 0)
            // hence var == null
            return getBinding(n);
        }
        // let ((?x, ?y) = ?m) -- ?m : Mapping
        return getValue(var);
    }

    List<IDatatype> getBinding(int n){
        List<List<IDatatype>> l = getList();
        if (n < l.size()){
            return l.get(n);
        }
        return null;
    }

    /**
     * List of variable binding
     * @return 
     */
    @Override
    public Iterable getLoop() {
        return getList();
    }
        
    public List<List<IDatatype>> getList() {    
        ArrayList<List<IDatatype>> list = new ArrayList<>();
        int i = 0;
        for (Node n : getQueryNodes()) {
            Node val = getNode(i++);
            if (val != null){
                ArrayList<IDatatype> l = new ArrayList<>(2);
                l.add(n.getDatatypeValue());
                l.add(val.getDatatypeValue());
                list.add(l);
            }
        }
        return list;
    }
    
    public IDatatype getDatatypeList() {
        IDatatypeList dt = DatatypeMap.newList();
        for (List<IDatatype> list : getList()) {
            dt.add(DatatypeMap.newList(list));
        }
        return dt;
    }


    @Override
    public Node[] getQueryNodes() {
        return queryNodes;
    }

    public List<Node> getQueryNodeList() {
        return Arrays.asList(getQueryNodes());
    }

    @Override
    public Node[] getNodes() {
        return getTargetNodes();
    }
    
    public Edge[] getQueryEdges() {
        return queryEdges;
    }

    @Override
    public Edge[] getEdges() {
        return getTargetEdges();
    }

    Edge getEdge(int n) {
        return getTargetEdges()[n];
    }

    Edge getQueryEdge(int n) {
        return getQueryEdges()[n];
    }
    
     /**
     * minus compatible
     * varList is the list of common variables between Mappings map1 and map2
     * Focus on varList but we are not sure that they are bound in these particular Mapping
     * If no common variable : compatible = false
     * If all common variables have same values : compatible = true
     * else compatible = false
     * */
    boolean minusCompatible(Mapping map, List<String> varList) {
        return compatible(map, varList, false);
    }
    
    boolean optionalCompatible(Mapping map, List<String> varList) {
        return compatible(map, varList, true);
    }
    
    boolean compatible(Mapping map, List<String> varList, boolean compatibleWithoutCommonVariable) {
        boolean success = compatibleWithoutCommonVariable;
        for (String var : varList) {
            Node val1 = getNodeValue(var);
            Node val2 = map.getNodeValue(var);
            if (val1 == null || val2 == null) {
                // do nothing as if variable were not in Mapping
                // use case: select count(*) as ?c
                // ?c is in QueryNodes but has no value
                // use case: minus {option{}}
            } else if (val1.match(val2)) { 
                success = true;
            } else {
                return false;
            }
        }
        return success;
    }

    /**
     * Compatible imply remove minus if all shared variables have same value
     * return true if no shared variable return false
     */
    public boolean compatible(Mapping minus) {
        return compatible(minus, false);
    }
    
    boolean compatible(Mapping map, boolean defaultValue) {
        if (map.getSelect() == null){
            return compatible1(map, defaultValue);
        }
        else {
            return compatible2(map, defaultValue);            
        }
    }
    
        // common variables have compatible values
    boolean isMergeAble(Mapping m) {
        for (String var : getVariableNames()) {
            Node v1 = getNodeValue(var);
            Node v2 = m.getNodeValue(var);
            if (v2 != null && !v2.match(v1)) { // was equal
                return false;
            }
        }
        return true;
    }
        
        
    boolean compatible1(Mapping map, boolean defaultValue) {
        boolean sameVarValue = defaultValue;
        for (Node node : getSelectQueryNodes()) {
            if (node.isVariable()) {
                Node val1 = getNodeValue(node);
                Node val2 = map.getNodeValue(node);
                if (val1 == null || val2 == null) {
                    // do nothing as if variable were not in Mapping
                    // use case: select count(*) as ?c
                    // ?c is in QueryNodes but has no value
                    // use case: minus {option{}}
                } else if (!val1.match(val2)) { // was same
                    return false;
                } else {
                    sameVarValue = true;
                }
            }
        }
        return sameVarValue;
    }
    
    boolean compatible2(Mapping map, boolean defaultValue) {
        boolean sameVarValue = defaultValue;
        for (Node node1 : getSelectQueryNodes()) {
            if (node1.isVariable()) {
                Node node2 = map.getSelectQueryNode(node1.getLabel());
                if (node2 != null) {
                    Node val1 = getNodeValue(node1);
                    Node val2 = map.getNodeValue(node2);
                    if (val1 == null || val2 == null) {
                        // do nothing as if variable were not in Mapping
                        // use case: select count(*) as ?c
                        // ?c is in QueryNodes but has no value
                        // use case: minus {option{}}
                    } else if (!val1.match(val2)) { // was same
                        return false;
                    } else {
                        sameVarValue = true;
                    }
                }
            }
        }
        return sameVarValue;
    }
   
     
    
//    boolean compatible2(Mapping map, boolean defaultValue) {
//        boolean sameVarValue = defaultValue;
//        for (Node node2 : map.getSelectQueryNodes()) {
//            if (node2.isVariable()) {
//                Node node1 = getSelectQueryNode(node2.getLabel());
//                if (node1 != null) {
//                    Node val1 = getNodeValue(node1);
//                    Node val2 = map.getNodeValue(node2);
//                    if (val1 == null || val2 == null) {
//                        // do nothing as if variable were not in Mapping
//                        // use case: select count(*) as ?c
//                        // ?c is in QueryNodes but has no value
//                        // use case: minus {option{}}
//                    } else if (!val1.match(val2)) { // was same
//                        return false;
//                    } else {
//                        sameVarValue = true;
//                    }
//                }
//            }
//        }
//        return sameVarValue;
//    }

    /**
     * Environment
     */
    /**
     * Warning: do not cache this index because index may vary between mappings
     */
    int getIndex(String label) {
        // TODO Auto-generated method stub
        int n = 0;
        for (Node qNode : getQueryNodes()) {
            if (qNode.isVariable() && qNode.getLabel().equals(label)) {
                return n;
            }
            n++;
        }
        return -1;
    }

    @Override
    public Node getNode(Expr var) {
        switch (var.subtype()) {
            case ExprType.LOCAL: {               
                Node node = get(var);
                if (debug && node == null) {
                    System.out.println("Mapping: Unbound variable: " + var);
                }
                return node;
            }
        }
        Node node = getNodeValue(var.getLabel());
        if (debug && node == null) {
            System.out.println("Mapping: Unbound variable: " + var);
        }
        return node;
//            int i = getIndex(var.getLabel());
//            if (i == -1){
//                return null;
//            }                  
//            return nodes[i];
    }

    @Override
    public Node getQueryNode(String label) {
        for (Node qNode : getQueryNodes()) {
            if (qNode.getLabel().equals(label)) {
                return qNode;
            }
        }
        return null;
    }
    
    Node getQueryNode(Node node) {
        return getQueryNode(node.getLabel());
    }
    
     Node getCommonNode(Mapping m) {
        for (Node q1 : getQueryNodes()) {
            if (q1.isVariable()) {
                Node q2 = m.getQueryNode(q1);
                if (q2 != null && q2.isVariable()) {
                    return q2;
                }
            }
        }
        return null;
    }

    public Node getSelectNode(String label) {
        if (getSelectNodes() == null) {
            return null;
        }
        for (Node qNode : getSelectNodes()) {
            if (qNode.getLabel().equals(label)) {
                return qNode;
            }
        }
        return null;
    }

    public Node getSelectQueryNode(String label) {
        if (getSelect() != null) {
            return getSelectNode(label);
        } else {
            return getQueryNode(label);
        }
    }

    @Override
    public boolean isBound(Node qNode) {
        // TODO Auto-generated method stub
        int n = getIndex(qNode.getLabel());
        return n != -1 && getTargetNodes()[n] != null;
    }

    /**
     * *******************************************************************
     *
     * Pipeline Solutions implementation
     *
     *
     ********************************************************************
     */
    Node[] getSelectQueryNodes() {
        if (getSelect() != null) {
            return getSelect();
        } else {
            return getQueryNodes();
        }
    }
    
    Mapping join(Mapping m) {
        List<Node> qNodes = new ArrayList<>();
        List<Node> tNodes = new ArrayList<>();

        for (Node q1 : getSelectQueryNodes()) {
            Node n1 = getNodeValue(q1);
            Node q2 = m.getSelectQueryNode(q1.getLabel());
            if (q2 != null) {
                Node n2 = m.getNodeValue(q2);
                if (! n1.match(n2)) { // was same
                    return null;
                }
            }
            qNodes.add(q1);
            tNodes.add(n1);
        }

        // nodes in m not in this
        for (Node q2 : m.getSelectQueryNodes()) {
            Node q1 = getSelectQueryNode(q2.getLabel());
            if (q1 == null) {
                Node n2 = m.getNode(q2);
                qNodes.add(q2);
                tNodes.add(n2);
            }
        }

        Mapping map = new Mapping(qNodes, tNodes);
        return map;
    }


    Mapping merge(Mapping m) {
        if (!isMergeAble(m)) { // ! compatible(m, true);
            return null;
        }

        List<Node> q = new ArrayList<Node>();
        List<Node> t = new ArrayList<Node>();

        List<Path> p = null;
//        boolean isPath = isPath() || m.isPath();
//        if (isPath) {
//            p = new ArrayList<Path>();
//        }

        int n = 0;
        for (Node qn : getQueryNodes()) {
            if (qn.isVariable()) {
                Node tn = getNodeValue(qn.getLabel());
                if (tn != null) {
                    q.add(qn);
                    t.add(tn);
//                    if (isPath) {
//                        p.add(getPath(n));
//                    }
                }
            }
            n++;
        }

        n = 0;
        for (Node qn : m.getQueryNodes()) {
            if (qn.isVariable()) {
                Node tn = m.getNodeValue(qn.getLabel());
                if (tn != null && getNodeValue(qn.getLabel()) == null) {
                    q.add(qn);
                    t.add(tn);
//                    if (isPath) {
//                        p.add(m.getPath(n));
//                    }
                }
            }
            n++;
        }

        Mapping map = new Mapping(q, t);
//        if (isPath) {
//            map.init(p);
//        }
        return map;
    }


    Mapping project(List<Exp> lExp) {

        return this;
    }

    Mapping rename(List<Exp> lExp) {
        if (getSelect() != null) {
            rename(lExp, getSelect());
        }
        rename(lExp, getQueryNodes());
        return this;
    }

    Node[] rename(List<Exp> lExp, Node[] qNodes) {
        int i = 0;
        for (Node node : qNodes) {
            Node tNode = get(lExp, node);
            if (tNode != null) {
                qNodes[i] = tNode;
            }
            i++;
        }
        return qNodes;
    }

    Node get(List<Exp> lExp, Node node) {
        for (Exp exp : lExp) {
            Filter f = exp.getFilter();
            if (f != null
                    && f.getExp().type() == ExprType.VARIABLE
                    && f.getExp().getLabel().equals(node.getLabel())) {
                return exp.getNode();
            }
        }
        return null;
    }

    /**
     * Share one target node (independently of query node)
     */
    boolean match(Mapping map) {
        int i = 0;
        for (Node node : getNodes()) {
            // skip path that cannot be shared
            if (!isPath(i++) && node != null && map.contains(node)) {
                return true;
            }
        }
        return false;
    }

    boolean contains(Node node) {
        for (Node n : getNodes()) {
            if (n != null && node.match(n)) { // was same
                return true;
            }
        }
        return false;
    }

    /**
     * Mapping as Environment may compute aggregates see same function in Memory
     */
//    @Override
//    public void aggregate(Evaluator eval, Producer p, Filter f) {
//        if (!getMappings().isFake()) {
//            for (Mapping map : getMappings()) {
//                // in case there is a nested aggregate
//                eval.eval(f, map, p);
//            }
//        }
//    }
    
    @Override
    public Iterable<Mapping> getAggregate(){
        if (getMappings()==null) {
            return List.of(this);
        }
        if (getMappings().isFake()) {
            return new ArrayList<>(0);
        }
        return getMappings();
    }
    
    @Override
    public void aggregate(Mapping map, int n){
        getAggregateMappings().prepareAggregate(map, getQuery(), getMap(), n);
    }   
    
    Mappings getAggregateMappings() {
        if (getMappings() == null) {
            return new Mappings().add(this);
        }
        return getMappings();
    }
    
    @Override
    public ASTExtension getExtension() {
        return query.getActualExtension();
    }

    @Override
    public Binding getBind() {
        return bind;
    }
    
    @Override
    public void setBind(Binding b) {
        bind = b;
    }

    @Override
    public boolean hasBind() {
        return bind != null && bind.hasBind();
    }
    
    Binding getCreateBind(){
        return bind;
    }

    @Override
    public Node get(Expr var) {
        // return getCreateBind().get(var);
        if (getBind() == null) {
            Eval.logger.error("Mapping unbound ldscript variable: " + var);
            return null;
        }
        return getBind().get(var);
    }

    @Override
    public PointerType pointerType() {
        return MAPPING;
    }

    @Override
    public Mapping getMapping() {
        return this;
    }

    @Override
    public Edge getEdge() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApproximateSearchEnv getAppxSearchEnv() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TripleStore getTripleStore() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
   
    @Override
    public Node getGraphNode() {
        return graphNode;
    }
  
    public void setGraphNode(Node graphNode) {
        this.graphNode = graphNode;
    }
    
    public Node getNamedGraph() {
        return targetGraphNode;
    }
   
    public void setNamedGraph(Node targetGraphNode) {
        this.targetGraphNode = targetGraphNode;
    }   
    
    @Override
    public Eval getEval() {
        return eval;
    }
    
    @Override
    public ProcessVisitor getVisitor() {
        if (getEval() == null) {
            return null;
        }
        return getEval().getVisitor();
    }   
    @Override
    public void setEval(Eval eval) {
        this.eval = eval;
    }

    @Override
    public IDatatype getReport() {
        return report;
    }

    @Override
    public void setReport(IDatatype report) {
        this.report = report;
    }

    public void setQueryNodes(Node[] qNodes) {
        this.queryNodes = qNodes;
    }
    
    public Node[] getTargetNodes() {
        return targetNodes;
    }

    public void setTargetNodes(Node[] nodes) {
        this.targetNodes = nodes;
    }
    
    public Node[] getSelectNodes() {
        return selectNodes;
    }

    public void setSelectNodes(Node[] sNodes) {
        this.selectNodes = sNodes;
    }

    public Node[] getOrderByNodes() {
        return orderByNodes;
    }

    public void setOrderByNodes(Node[] oNodes) {
        this.orderByNodes = oNodes;
    }

    public Node[] getGroupByNodes() {
        return groupByNodes;
    }

    public void setGroupByNodes(Node[] gNodes) {
        this.groupByNodes = gNodes;
    }

    public Node[] getDistinctNodes() {
        return distinctNodes;
    }

    public void setDistinctNodes(Node[] distinct) {
        this.distinctNodes = distinct;
    }

    public Node[] getGroupAlter() {
        return groupAlterNodes;
    }

    public void setGroupAlter(Node[] group) {
        this.groupAlterNodes = group;
    }

    public void setQueryEdges(Edge[] qEdges) {
        this.queryEdges = qEdges;
    }

    public Edge[] getTargetEdges() {
        return targetEdges;
    }

    public void setTargetEdges(Edge[] edges) {
        this.targetEdges = edges;
    }

 
 
}
