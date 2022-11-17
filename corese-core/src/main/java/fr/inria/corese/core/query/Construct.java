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
    private Query query;
    private ASTQuery ast;
    GraphManager graphManager;
    Node defaultGraph;
    List<Edge> lInsert, lDelete, globalDeleteList;
    private Dataset ds;
    private boolean detail = false;
    boolean isDebug = false,
            isDelete = false;
    private boolean construct = false;
    private boolean isRule = false;
    private boolean isInsert = false;
    boolean isBuffer = false;
    private boolean test = false;
    public static boolean trace = false;
    Rule rule;
    HashMap<Node, Node> table;
    private TreeNode literalMap;
    private HashMap<String, Node> labelMap;
    Duplicate duplicate;
    private int loopIndex = -1;
    private Node provenance;
    private ProcessVisitor visitor;
    private AccessRight accessRight;
    List<Edge> emptyEdgeList;
    

    Construct(Query q) {
        query = q;
        ast =  query.getAST();
        table = new HashMap<>();
        labelMap = new HashMap<>();
        literalMap = new TreeNode();
        count = 0;
        duplicate =  Duplicate.create();
        emptyEdgeList = new ArrayList<>(0);
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
     
    boolean isLocal() {
        return ! isBuffer();
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
        if (getGraphManager().isRDFStar()) {
            globalDeleteList = new ArrayList<>();
        }
        process(map, null);
        if (getGraphManager().isRDFStar()) {
            // @hint: does not work with: dataset + delete { graph uri { rdfstar }}
            delete(null, map, globalDeleteList);
        }
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
    
    // rule engine
    public void entailment(Mappings map) {
        insert(map, null);
    }

    // rule engine
    public void entailment(Mappings map, Environment env) {  
        process(map, env);
    }
           
    void process(Mappings map, Environment env) {  
        graphManager.start(event());
        Exp exp = getQuery().getConstruct();
        if (isDelete) {
            exp = getQuery().getDelete();
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
            getVisitor().entailment(getQuery(), construct, whereList);
        }
    }

    /**
     * Recursive construct of exp with map Able to process construct graph ?g
     * {exp}
     * env: current solution mapping to be processed
     */
    void construct(Node gNode, Exp exp, Mappings map, Environment env, List<Edge> edgeList) {
        construct(gNode, exp, map, env, edgeList, false);
    }
    
    void construct(Node gNode, Exp exp, Mappings map, Environment env, List<Edge> edgeList, boolean rec) {
        if (exp.isGraph()) {
            gNode = exp.getGraphName();
            exp = exp.rest();
        }

        List<Edge> deleteList = null;
        
        if (isDelete) {
            deleteList = new ArrayList<> ();
        }
        
        for (Exp ee : exp.getExpList()) {
            if (ee.isEdge()) {
                List<Edge> insertEdgeList = 
                        (isInsertConstruct() || isRule()) ? new ArrayList<>() : emptyEdgeList;
                // construct edge may construct nested rdf star triples recursively
                // they are recorded in insertEdgeList
                Edge edge = construct(gNode, ee.getEdge(), env, insertEdgeList, rec);
                
                if (edge != null) {
                    // RuleEngine loop index
                    edge.setEdgeIndex(loopIndex);

                    if (isDelete) {
                        boolean accept = true;
                        if (AccessRight.isActive()&&getAccessRight() != null) {
                            accept = getAccessRight().setDelete(edge);
                        }
                        if (accept) {                           
                            deleteList.add(edge);                                                        
                        }
                    } else { // insert/construt
                        if (isDebug) {
                            logger.debug("** Construct: " + edge);
                        }
                        boolean accept = true;
                        if (AccessRight.isActive()&&getAccessRight() != null) {
                            accept = getAccessRight().setInsert(edge);
                        }
                        if (accept) {
                            if (isRule() && isAllEntailment()) {
                                //@todo: check insertEdgeList
                                edgeList.add(edge);
                            }
                            if (isLocal()) {
                                // store edge in graph rigth now
                                edge = graphManager.insert(edge);                                
                                for (Edge nestedEdge : insertEdgeList) {
                                    // insert { t q v } where { ... }
                                    // edge = t q v ; nestedEdge = <<s p o t>>
                                    // t is reference of <<s p o>>
                                    // insert <<s p o>> (and recursively s may be a reference)
                                    graphManager.insert(nestedEdge);
                                }                                
                            }
                            
                            if (edge != null) {
                                // edge has been inserted in  the graph with success
                                if (isRule() && ! isAllEntailment()) {
                                    edgeList.add(edge);
                                }
                                map.setNbInsert(map.nbInsert() + 1);
                                
                                if (lInsert != null) {
                                    // isBuffer() == true
                                    // store edges for RuleEngine process() cons.getInsertList()
                                    lInsert.add(edge);
                                    for (Edge nestedEdge : insertEdgeList) {
                                        lInsert.add(nestedEdge);
                                    }
                                }

                                if (isInsert()) {
                                    // When insert in new graph g, update dataset named += g
                                    if (getDataset() != null) {
                                        String name = edge.getGraph().getLabel();
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
                construct(gNode, ee, map, env, edgeList, rec);
            }                        
        }
        
        if (isDelete) {
            if (getGraphManager().isRDFStar()) {
                globalDeleteList.addAll(deleteList);
            }
            else {
                delete(gNode, map, deleteList);
            }
        }
    }
    
    // @todo: check access right
    void delete(Node gNode, Mappings map, List<Edge> deleteList) {
        for (Edge edge : deleteList) {
            if (isDebug) {
                logger.debug("** Delete: " + edge);
            }
            List<Edge> list = null;

            if (gNode == null && getDataset() != null && getDataset().hasFrom()) {
                // delete in default graph from dataset
                list = graphManager.delete(edge, getDataset().getFrom());
            } else {
                list = graphManager.delete(edge);
            }

            if (list != null) {
                map.setNbDelete(map.nbDelete() + list.size());

                if (lDelete != null) {
                    //lDelete.addAll(list);
                    lDelete.add(edge);
                }
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
    Edge construct(Node gNode, Edge edge, Environment env, List<Edge> insertEdgeList, boolean rec) {
        if (trace) {
            System.out.println("___");
            System.out.println("construct edge: " + edge);
        }
        Node pred = edge.getEdgeVariable();
        if (pred == null) {
            pred = edge.getEdgeNode();
        }

        Node source = null;
        if (gNode != null) {
            source = construct(gNode, env);
        }
        Node property = construct(pred, env);
        
        Node subject = construct(gNode, source, edge.getNode(0), env, insertEdgeList, rec);
        Node object  = construct(gNode, source, edge.getNode(1), env, insertEdgeList, rec);

        if ((source == null && !isDelete()) || subject == null || property == null || object == null) {
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

        if (isDelete()) {
            if (gNode == null) {
                source = null;
            }
        } else {
            // insert edge node into target graph
            graphManager.add(subject);
            graphManager.add(object);
            graphManager.addPropertyNode(property);
            graphManager.addGraphNode(source);
        }

        Edge ee;
        if (edge.nbNode() > 2) {
            // tuple()
            ArrayList<Node> list = new ArrayList<>();
            list.add(subject);
            list.add(object);

            for (int i = 2; i < edge.nbNode(); i++) {
                Node queryNode = edge.getNode(i);
                Node n;
                
                if (queryNode.isTriple()) {
                    n = reference(queryNode, subject, property, object);
                }
                else {
                    n = construct(gNode, source, queryNode, env, insertEdgeList, rec);
                }
                
                if (n != null) {
                    if (!isDelete()){
                        graphManager.add(n, i);
                    }
                    list.add(n);
                }
            }

            ee = create(source, property, list);
        } else {
            ee = create(source, subject, property, object);
        }

        if (getProvenance() != null && !getGraphManager().isRDFStar()){
            // @todo: check this
            ee.setProvenance(getProvenance());
        }
        ee.setNested(edge.isNested());
        return ee;
    }
    
    
    
    // refQueryNode is reference query node t of triple pattern s p o t
    Node reference(Node refQueryNode, Node s, Node p, Node o) {
        if (isDelete()) {
            // reference node useless in case of delete
            // do not screw up future binding of this node in case it would appear as subject
            return null;
        }
        return basicReference(refQueryNode, s, p, o);
    }

    /** 
     * refQueryNode is reference query node t of triple pattern s p o t
     * create target reference node in graph
     * s p o must be inserted in graph because we use node index
     */
    Node basicReference(Node refQueryNode, Node s, Node p, Node o) {
        Node refNode = get(refQueryNode);
        if (refNode == null) {
            refNode = graphManager.createTripleReference(s, p, o);
            put(refQueryNode, refNode);
        }
        return refNode;
    }
    
    IDatatype tripleReference(Node refQueryNode, Environment map) {
        return graphManager.createTripleReference();
    }
   
    Node tripleReference(Node refQueryNode, Node s, Node p, Node o) {
        return graphManager.createTripleReference(s, p, o);
    }
    
    Node construct(Node gNode, Node source, Node qNode, Environment map) {
        return construct(gNode, source, qNode, map, null, false);
    }

    /**
     * Given queryNode return target node in map
     * If node exist in graph, return graph node
     * rec == true: recursive call for nested triple
     * <<_:b p o t>> q v
     */
    Node construct(Node queryGraph, Node resultGraph, 
            Node queryNode, Environment map, 
            List<Edge> insertEdgeList, boolean rec) {        
        
        if (rec && queryNode.isBlank()) {
            // bnode in rec nested triple: 
            // bnode denote itself, it is not a variable
            return queryNode;
        }
        
        // search result node in table
        Node resultNode = get(queryNode);
        
        if (resultNode == null) {
            // result node not yet created
            // search map target node
            Node targetNode = map.getNode(queryNode.getLabel());
            IDatatype dt = null;
            
            if (targetNode == null) {
                trace("query node: %s %s %s", queryNode, queryNode.isTriple(), queryNode.isTripleWithEdge());
                if (queryNode.isTriple()) {
                    if (queryNode.getEdge() == null) {
                        return null;
                    }
                    // very specific case where queryTriple is a nested triple from target graph:
                    // construct {?t :p :v} where {?s ?p ?o bind (<<?s ?p ?o>> as ?t)}
                    // ?s match a nested triple <<a q b>>[s] p o
                    // construct ?t -> construct s and s isTriple() 
                    // queryNode = s and targetNode == null
                    return createReference(queryGraph, resultGraph, queryNode, targetNode, map, insertEdgeList, rec);
                }
                else if (queryNode.isBlank()) {
                    dt = blank(queryNode, map);
                }
                else if (queryNode.isConstant()) {
                    // uri, literal
                    dt = queryNode.getDatatypeValue();
                } else {
                    // unbound variable
                    return null;
                }
            }
            else {
                // targetNode != null
                // check case:
                // targetNode = value of ?t in bind(<<s p o>> as ?t)
                // query edge = ?t q r
                // ?t has a value in map result, but the value
                // is not a reference in target graph (?t is created by bind)
                // retrieve corresponding reference in graph
                if (targetNode.isTripleWithEdge()  
                        && (targetNode.getEdge().isCreated() || isConstruct())) {
                    
                    return createReference(queryGraph, resultGraph, queryNode, targetNode, 
                            map, insertEdgeList, rec);
                }
                else {
                    dt = targetNode.getDatatypeValue();
                }
            }
            
            resultNode = uniqueNode(resultGraph, dt);
            put(queryNode, resultNode);
        }

        return resultNode;
    }   
    
    
    /**
     * queryNode is triple reference
     */
    Node createReference(Node queryGraphNode, Node resultGraphNode, 
            Node queryNode, Node targetNode, Environment map, 
            List<Edge> insertEdgeList, boolean rec) {  
        
        Node resultNode = reference(queryGraphNode, resultGraphNode, queryNode, 
                    targetNode == null ? queryNode : targetNode , 
                    map,
                    isDelete() ? emptyEdgeList : insertEdgeList, rec);

        if (resultNode != null) {
            put(queryNode, resultNode);
        }
        return resultNode;
    }
    

    /**
     * refNode is a triple reference to edge: delete/insert/construct <<s p o t>> q v -> t q v .
     * refNode = t in t q v and t.edge = s p o instantiate edge s p o = query
     * case delete: find query edge in graph, get its reference ref if any return ref as t
     * case insert/construct: return query reference node 
     * in this case, ref node is graph ref node and it is inserted in graph by construction
     */     
    Node reference(Node gNode, Node resultGraphNode, Node queryNode, Node refNode, Environment map, List<Edge> insertEdgeList, 
            boolean rec) {
        // t.edge = s p o
        Edge edge = refNode.getEdge();
        trace1(gNode, refNode, edge);
        // instantiate s p o wrt to target graph 
        Edge query = construct(gNode, edge, map, insertEdgeList, true);
        if (query == null) {
            // when delete, we may have a null result when some node does not exist
            return null;
        }
        trace("ref node query edge: %s %s %s", query, 
                query.getReferenceNode(), query.getClass().getName());
        
       if (isDelete()) {
            Edge target = getGraphManager().find(query);
            trace2(query, target);
            if (target == null) {
                // edge does not exist in graph: skip this delete 
                return null;
            }
            return target.getReferenceNode();
        } else {
            if (isConstruct() || edge.isCreated()) {
                // intermediate query edge will inserted as cited triple
                // use case 1: construct {?t qq vv} where {<<s p o>> q v . ?t q v}
                // use case 2: insert    {?t qq vv} where { bind (<<s p o>> as ?t) }
                // in adition to t qq vv, insert query = <<s p o>> 
                query.setNested(true);
                insertEdgeList.add(query);
            }
            return query.getReferenceNode();
        }
    }
    

    
    void trace(String mes, Object... obj) {
        if (trace) {
            System.out.println(String.format(mes, obj));
        }
    }
    void trace1(Node gNode, Node refNode, Edge edge) {
        if (trace) {
            trace("graph node: %s", gNode);
            trace("ref node: %s [%s] edge: %s", refNode, refNode.getLabel(), edge);
        }
    }
    
    void trace2(Edge query, Edge target) {
        if (trace) {
            System.out.println("query edge:  " + query);
            System.out.println("target edge: " + target);
        }
    }
    
    void trace3(Node refNode, Node resultNode) {
        if (trace) {
            trace("ref node: %s=%s label: ",refNode, resultNode, resultNode.getLabel());
        }
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
        return construct(null, null, qNode, map);
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
            if (targetNode.isTriple()) {
                // for recursive calls
                getLabelMap().put(targetNode.getLabel(), targetNode);
            }
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
        return queryNode.isVariable() || queryNode.isTriple() || queryNode.isBlank() || queryNode.getDatatypeValue().isURI();
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
                sb.append(getGraphManager().reference(map.getNode(qnode)));
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
        List<Node> list = new ArrayList<>();
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
    
    public boolean isInsertConstruct() {
        return isInsert() || isConstruct();
    }

    public boolean isInsert() {
        return isInsert;
    }

    public boolean isConstruct() {
        return construct;
    }
    
    public boolean isDelete() {
        return isDelete;
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

    public ASTQuery getAst() {
        return ast;
    }

    public void setAst(ASTQuery ast) {
        this.ast = ast;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
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
    
    
//        @Deprecated
//    Node reference1(Node gNode, Node resultGraphNode, Node queryNode, Node refNode, Environment map, List<Edge> insertEdgeList, 
//            boolean rec) {
//        
//        Edge edge = refNode.getEdge();
//        trace1(gNode, refNode, edge);
//        Edge query = construct(gNode, edge, map, insertEdgeList, true);
//        trace("ref node query edge: %s %s %s", query, 
//                query.getReferenceNode(), query.getClass().getName());
//        // when isDelete() search target edge in graph
//        // @todo otherwise resultNode = query.getReferenceNode()
//        // because reference node = ref(s p o) and it is unique (no need to search)
//        Edge target = getGraphManager().find(query);
//        trace2(query, target);
//        
//        Node resultNode;
//        
//        if (target != null && target.hasReferenceNode()) {
//            // target edge exists in graph: return its reference
//            resultNode = target.getReferenceNode();
//            trace3(refNode, resultNode);
//        }         
//        else {
//            // there is no such reference in graph: create it
//            // if edge isCreated() : edge = <<s p o>> in 
//            // insert { ?t us:wrt ?x } where {bind (<<s p o>> as ?t)}
//            // insert <<s p o>> as well as triple
//            // @todo: when isDelete(), do not create reference, return null and skip this delete 
//            resultNode = tripleReference(refNode, query.getSubjectNode(), query.getPropertyNode(), query.getObjectNode());
//            trace3(refNode, resultNode);
//            if (isConstruct() || (edge.isCreated() && !isDelete())) {
//                // add nested triple <<s p o>> in <<s p o>> q v
//                // it must not be asserted here
//                query.setNested(true);
//                insertEdgeList.add(query);
//            }
//        }
//        
//        return resultNode;
//    }
//    
}
