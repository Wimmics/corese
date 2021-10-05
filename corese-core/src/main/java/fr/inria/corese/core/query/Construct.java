package fr.inria.corese.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.query.update.GraphManager;
import fr.inria.corese.core.rule.Rule;
import fr.inria.corese.core.util.Duplicate;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import java.util.TreeMap;

/**
 * query  construct describe  
 * update insert delete 
 * rule   construct where
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Construct
        implements Comparator<Node> {

   private static Logger logger = LoggerFactory.getLogger(Construct.class);
    private static boolean allEntailment = false;
    static final String BLANK = "_:b_";
    static final String DOT = ".";
    int count = 0, ruleIndex = 0, index = 0;
    String root;
    Query query;
    ASTQuery ast;
    GraphManager graphManager;
    Node defaultGraph;
    //IDatatype dtDefaultGraph;
    List<Edge> lInsert, lDelete;
    private Dataset ds;
    private boolean detail = false;
    boolean isDebug = false,
            isDelete = false;
    private boolean construct = false;
    private boolean isRule = false;
    private boolean isInsert = false;
    boolean isBuffer = false;
    private boolean test = false;
    Rule rule;
    HashMap<Node, Node> table;
    private TreeNode literalMap;
    private HashMap<String, Node> labelMap;
    Duplicate duplicate;
    private int loopIndex = -1;
    private Node provenance;
    private ProcessVisitor visitor;
    private AccessRight accessRight;
    

    Construct(Query q) {
        query = q;
        ast =  query.getAST();
        table = new HashMap<>();
        labelMap = new HashMap<>();
        literalMap = new TreeNode();
        count = 0;
        duplicate =  Duplicate.create();
    }

    private static Construct create(Query q) {
        Construct cons = new Construct(q);
        if (q.isDetail()) {
            cons.setDetail(true);
        }
        return cons;
    }
    
    private static Construct create(Query q, GraphManager g) {
        return create(q).set(g);
    }
    
    public static Construct createConstruct(Query q, GraphManager g) {
        return create(q, g).setConstruct(true);
    }
    
    public static Construct createRule(Query q, GraphManager g) {
        return create(q, g).setRule(true);
    }
    
    public static Construct createInsert(Query q, GraphManager g) {
        return create(q, g).setInsert(true);
    }
    
    public static Construct createDelete(Query q, GraphManager g) {
        return create(q, g).setDelete(true);
    }

//    public static Construct create(Query q, Dataset ds) {
//        return new Construct(q, ds);
//    }

    public void setDefaultGraph(Node n) {
        defaultGraph = n;
    }

    public void setBuffer(boolean b) {
        isBuffer = b;
    }    
    
    public Construct set(GraphManager g){
       graphManager = g; 
       return this;
    }
    
    public GraphManager getGraphManager() {
        return graphManager;
    }
    
     public boolean isBuffer() {
        return isBuffer;
    }

    public Construct setDelete(boolean b) {
        isDelete = b;
        return this;
    }

    public Construct setInsert(boolean b) {
        isInsert = b;
        return this;
    }

    public void setRule(Rule r, int n, Node prov) {
        //setRule(true);
        rule = r;
        root = BLANK + n + DOT;
        setProvenance(prov);
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public void setInsertList(List<Edge> l) {
        lInsert = l;
    }

    public List<Edge> getInsertList() {
        return lInsert;
    }

    public void setDeleteList(List<Edge> l) {
        lDelete = l;
    }

    public List<Edge> getDeleteList() {
        return lDelete;
    }
    
    Event event() {
        return (isDelete) ? Event.Delete : (isInsert()) ? Event.Insert : Event.Construct ;
    }
    
    // when external named graph, use specific GraphManager
    void setGraphManager(Exp exp) {
        if (exp.first().isGraph()) {
            // draft: graph kg:system { }
            // in GraphStore
            Node gNode = exp.first().getGraphName();
            if (gNode.isConstant()) {
                GraphManager m = graphManager.getNamedGraph(gNode.getLabel());
                if (m != null) {
                    set(m);
                }
            }
        }
    }
    
    
    public void delete(Mappings map, Dataset ds) {
        if (AccessRight.isActive() && getAccessRight() != null) {
            if (! getAccessRight().isDelete()){
                return;
            } 
        }
        //setDelete(true);
        if (ds != null && ds.isUpdate()) {
            setDataset(ds);
        }
        if (isDetail()){
            setDeleteList(new ArrayList<>());
        }
        process(map, null);
    }
    
    public void insert(Mappings map, Dataset ds) {
        if (AccessRight.isActive() && getAccessRight() != null) {
            if (! getAccessRight().isInsert()){
                return;
            } 
        }
        //setInsert(true);
        if (ds != null && ds.isUpdate()) {
            setDataset(ds);
        }
        if (isDetail()){
            setInsertList(new ArrayList<>());
        }
        process(map, null);
    }
       
    public void construct(Mappings map) {
         process(map, null);
    }
    
    public void entailment(Mappings map) {
        insert(map, null);
    }

    public void entailment(Mappings map, Environment env) {  
        process(map, env);
    }
    
    void process(Mappings map, Environment env) {  
        graphManager.start(event());
        Exp exp = query.getConstruct();
        if (isDelete) {
            exp = query.getDelete();
        }

        // when external named graph, use specific GraphManager
        setGraphManager(exp);

        // can be set to kg:rule or kg:constraint
        Node gNode = defaultGraph;
        if (gNode == null) {
            // kg:default
            gNode = graphManager.getDefaultGraphNode();
        }

        if (isDelete) {
            gNode = null;
        }

        if (lInsert != null) {
            map.setInsert(lInsert);
        }
        if (lDelete != null) {
            map.setDelete(lDelete);
        }
                
        if (env != null) {
            // rule entailment
            // construct one solution from env
            process(gNode, exp, map, env);
        } else {
            for (Mapping m : map) {       
                process(gNode, exp, map, m);
            }
        }

        graphManager.finish(event());     
    }
    
    void process(Node gNode, Exp exp, Mappings map, Environment env) {
        List<Edge> edgeList = null;
        clear();
        if (isRule()) {
            edgeList = new ArrayList<>();
        }
        construct(gNode, exp, map, env, edgeList);
        record(edgeList, env.getEdges());
    }
    
    void record(List<Edge> construct, Edge[] where) {
        if (isRule() && ! construct.isEmpty() && 
                getVisitor() != null && getVisitor().entailment()) {
            List<Edge> whereList = new ArrayList<>();
            if (where != null) {
                for (Edge e : where) {
                    if (e != null) {
                        whereList.add(graphManager.getGraph().getEdgeFactory().copy(e));
                    }
                }
            }
            getVisitor().entailment(query, construct, whereList);
        }
    }

    /**
     * Recursive construct of exp with map Able to process construct graph ?g
     * {exp}
     * env: current solution mapping to be processed
     */
    void construct(Node gNode, Exp exp, Mappings map, Environment env, List<Edge> edgeList) {
        if (exp.isGraph()) {
            gNode = exp.getGraphName();
            exp = exp.rest();
        }

        for (Exp ee : exp.getExpList()) {
            if (ee.isEdge()) {
                Edge ent = construct(gNode, ee.getEdge(), env);              
                if (ent != null) {
                    // RuleEngine loop index
                    ent.setIndex(loopIndex);
                    if (isDelete) {
                        boolean accept = true;
                        if (AccessRight.isActive()&&getAccessRight() != null) {
                            accept = getAccessRight().setDelete(ent);
                        }
                        if (accept) {
                            if (isDebug) {
                                logger.debug("** Delete: " + ent);
                            }
                            List<Edge> list = null;
                            if (gNode == null && getDataset() != null && getDataset().hasFrom()) {
                                // delete in default graph
                                list = graphManager.delete(ent, getDataset().getFrom());
                            } else {
                                // delete in all named graph
                                list = graphManager.delete(ent);
                            }
                            if (list != null) {
                                map.setNbDelete(map.nbDelete() + list.size());

                                if (lDelete != null) {
                                    //lDelete.addAll(list);
                                    lDelete.add(ent);
                                }
                            }
                        }

                    } else { // insert/construt
                        if (isDebug) {
                            logger.debug("** Construct: " + ent);
                        }
                        boolean accept = true;
                        if (AccessRight.isActive()&&getAccessRight() != null) {
                            accept = getAccessRight().setInsert(ent);
                        }
                        if (accept) {
                            if (isRule() && isAllEntailment()) {
                                edgeList.add(ent);
                            }
                            if (!isBuffer) {
                                // isBuffer means: bufferise edges in a list 
                                // that will be processed later by RuleEngine
                                // otherwise, store edge in graph rigth now
                                ent = graphManager.insert(ent);
                            }
                            if (ent != null) {
                                if (isRule() && ! isAllEntailment()) {
                                    edgeList.add(ent);
                                }
                                map.setNbInsert(map.nbInsert() + 1);
                                if (lInsert != null) {
                                    // buffer where to store edges for RuleEngine process() cons.getInsertList()
                                    lInsert.add(ent);
                                }

                                if (isInsert()) {
                                    // When insert in new graph g, update dataset named += g
                                    if (getDataset() != null) {
                                        String name = ent.getGraph().getLabel();
                                        if (!graphManager.isDefaultGraphNode(name)) {
                                            getDataset().addNamed(name);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                construct(gNode, ee, map, env, edgeList);
            }
        }
    }

    /**
     * Clear blank node table
     */
    void clear() {
        table.clear();
        getLiteralMap().clear();
        getLabelMap().clear();
    }

    void init() {
        clear();
        count = 0;
    }

    /**
     * Construct target edge from query edge and map
     * Edge ready to be inserted/deleted into/from target graph
     * Edge nodes are searched in target graph 
     * If an insert node does not exist in the graph, it is added in the graph now
     */
    Edge construct(Node gNode, Edge edge, Environment env) {

        Node pred = edge.getEdgeVariable();
        if (pred == null) {
            pred = edge.getEdgeNode();
        }

        Node source = null;
        if (gNode != null) {
            source = construct(gNode, env);
        }
        Node property = construct(pred, env);

        Node subject = construct(source, edge.getNode(0), env);
        Node object  = construct(source, edge.getNode(1), env);

        if ((source == null && !isDelete) || subject == null || property == null || object == null) {
            return null;
        }
        
        if (isBuffer){
            // optimized mode for Rule Engine
            if (graphManager.exist(property, subject, object)){
                // edge already exists: skip it
                return null;
            } 
            if (duplicate.exist(property, subject, object)){
                // edge already recorded: skip it
                return null;
            }
        }

        if (isDelete) {
            if (gNode == null) {
                source = null;
            }
        } else {
            // insert edge node into target graph
            graphManager.add(subject);
            graphManager.add(object);
            graphManager.addPropertyNode(property);
            //source = 
            graphManager.addGraphNode(source);
        }

        Edge ee;
        if (edge.nbNode() > 2) {
            // tuple()
            ArrayList<Node> list = new ArrayList<>();
            list.add(subject);
            list.add(object);

            for (int i = 2; i < edge.nbNode(); i++) {
                Node n = construct(source, edge.getNode(i), env);
                if (n != null) {
                    graphManager.add(n, i);
                    list.add(n);
                }
            }

            ee = create(source, property, list);
        } else {
            ee = create(source, subject, property, object);
        }
        if (getProvenance() != null){
            ee.setProvenance(getProvenance());
        }
        return ee;
    }

    Edge create(Node source, Node property, List<Node> list) {
        if (isDelete) {
            return graphManager.createDelete(source, property, list);
        } else {
            return graphManager.create(source, property, list);
        }
    }

    Edge create(Node source, Node subject, Node property, Node object) {
        if (isDelete) {
            return graphManager.createDelete(source, subject, property, object);
        } else {
            return graphManager.create(source, subject, property, object);
        }
    }

    /**
     * Construct target node from query node and map
     */
    Node construct(Node qNode, Environment map) {
        return construct(null, qNode, map);
    }
    
    /**
     * Record value of query node in order to reuse it during solution processing
     * qNode: variable | bnode | URI | Literal
     * bnode in construct/insert is not a variable, it means create a new bnode
     * and reuse the same new bnode in one solution
     * switch targetNode:
     * case var bnode URI -> put queryNode.getLabel() -> targetNode
     * case Literal: TreeNode with sameTerm
     */
    void put1(Node queryNode, Node targetNode) {
        table.put(queryNode, targetNode);
    }
        
    Node get1(Node queryNode) {
        return table.get(queryNode);
    }
    
    void put(Node queryNode, Node targetNode) {
        if (isLabel(queryNode)) {
            getLabelMap().put(queryNode.getLabel(), targetNode);
        }
        else {
            getLiteralMap().put(value(queryNode), targetNode);
        }
    }
    
    Node get(Node queryNode) {
        if (isLabel(queryNode)) {
            // queryNode = var bnode uri ; use getLabel() as key
            return getLabelMap().get(queryNode.getLabel());
        }
        else {
            // queryNode = Literal ; use IDatatype literal value as key
            return getLiteralMap().get(value(queryNode));
        }
    }
    
    Node uniqueNode1(Node gNode, IDatatype dt) {
        return graphManager.getNode(gNode, dt);
    }

    /**
     * Return unique Node for every occurrence of same datatype dt in one solution
     */
    Node uniqueNode(Node gNode, IDatatype dt) {
        Node node = get(dt);
        if (node == null) {
            // check if node already exist in target graph
            node = graphManager.getNode(gNode, dt);
            // record dt target value, later it can be reused
            put(dt, node);
        }
        return node;
    }
    
    boolean isLabel(Node queryNode) {
        return queryNode.isVariable() || queryNode.isBlank() || queryNode.getDatatypeValue().isURI();
    }
    

    Node construct(Node gNode, Node qNode, Environment map) {
        // search target node
        Node node = get(qNode);
        if (node == null) {
            // target node not yet created
            // search map node
            Node nn = map.getNode(qNode.getLabel());
            IDatatype value = null;
            if (nn != null) {
                value = nn.getDatatypeValue();
            }
            IDatatype dt = null;

            if (value == null) {
                if (qNode.isBlank()) {
                    dt = blank(qNode, map);
                } else if (qNode.isConstant()) {
                    // constant
                    dt = getValue(qNode);
                } else {
                    // unbound variable
                    return null;
                }
            } else {
                dt =  value;
            }
            node = uniqueNode(gNode, dt);
            put(qNode, node);
        }

        return node;
    }
       
    IDatatype value(Node node) {
        return  node.getDatatypeValue();
    }
    

    IDatatype blank(Node qNode, Environment map) {
        String str;
        if (isRule()) {
            str = blankRule(qNode, map);
        } else {
            str = graphManager.newBlankID();
        }
        IDatatype dt = graphManager.createBlank(str);
        return dt;
    }

    /**
     * Create a unique BN ID according to (Rule, qNode & Mapping) If the rule
     * runs twice on same mapping, it will create same BN graph will detect it,
     * hence engine will not loop
     *
     */
    String blankRule(Node qNode, Environment map) {
        // _:b + rule ID + "." + qNode ID
        StringBuilder sb = new StringBuilder(root);
        sb.append(getIndex(qNode));

        for (Node qnode : map.getQueryNodes()) {
            if (qnode != null && qnode.isVariable() && !qnode.isBlank()) {
                // node value ID   
                sb.append(DOT);
                sb.append(map.getNode(qnode).getIndex());
            }
        }

        return sb.toString();
    }

    /**
     * Generate an index for construct Node
     */
    int getIndex(Node qNode) {
        int n = qNode.getIndex();
        if (n == -1) {
            n = index++;
            qNode.setIndex(n);
        }
        return n;
    }

    IDatatype getValue(Node node) {
        return  node.getDatatypeValue();
    }

    String getID(Mapping map) {
        String str = "";
        List<Node> list = new ArrayList<Node>();
        for (Node node : map.getQueryNodes()) {
            list.add(node);
        }
        Collections.sort(list, this);
        int n = 0;
        for (Node qNode : list) {
            IDatatype value = map.getValue(qNode);
            n++;
            if (value != null && !qNode.isConstant()) {
                IDatatype dt =  value;
                str += qNode.getLabel() + "." + dt.toSparql() + ".";
            }
        }
        return str;
    }

    public int compare(Node n1, Node n2) {
        return n1.compare(n2);
    }

    public void setLoopIndex(int n) {
        loopIndex = n;
    }

   
    public boolean isTest() {
        return test;
    }

   
    public void setTest(boolean test) {
        this.test = test;
    }

    
    public boolean isDetail() {
        return detail;
    }

   
    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    
    public static boolean isAllEntailment() {
        return allEntailment;
    }

    
    public static void setAllEntailment(boolean aAllEntailment) {
        allEntailment = aAllEntailment;
    }

    
    public AccessRight getAccessRight() {
        return accessRight;
    }

   
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }

    public boolean isRule() {
        return isRule;
    }

    public Construct setRule(boolean isRule) {
        this.isRule = isRule;
        return this;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public boolean isConstruct() {
        return construct;
    }

    public Construct setConstruct(boolean construct) {
        this.construct = construct;
        return this;
    }
    
    public Dataset getDataset() {
        return ds;
    }

    public void setDataset(Dataset ds) {
        this.ds = ds;
    }

    public Node getProvenance() {
        return provenance;
    }

    public void setProvenance(Node provenance) {
        this.provenance = provenance;
    }

    public TreeNode getLiteralMap() {
        return literalMap;
    }

    public void setLiteralMap(TreeNode literalMap) {
        this.literalMap = literalMap;
    }

    public HashMap<String, Node> getLabelMap() {
        return labelMap;
    }

    public void setLabelMap(HashMap<String, Node> labelMap) {
        this.labelMap = labelMap;
    }
    
    public class TreeNode extends TreeMap<IDatatype, Node> {

        public TreeNode() {
            super(new CompareNode());
        }

        void put(Node node) {
            put(node.getDatatypeValue(), node);
        }

        boolean contains(Node node) {
            return containsKey( node.getDatatypeValue());
        }
    }
     
     /**
     * This Comparator enables to retrieve an occurrence of a given Literal
     * already existing in graph in such a way that two occurrences of same
     * Literal be represented by same Node 
     * It represents (1 integer) and (01 integer) as two different nodes
     * that will be assigned the same node index in order to join in SPARQL
     */
    class CompareNode implements Comparator<IDatatype> {

        CompareNode() {
        }

        @Override
        public int compare(IDatatype dt1, IDatatype dt2) {
            int res = dt1.compareTo(dt2);
            return res;
        }
    }

 
}
