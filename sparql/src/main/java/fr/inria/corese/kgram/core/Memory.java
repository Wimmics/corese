package fr.inria.corese.kgram.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.kgram.event.EventImpl;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.path.Path;
import fr.inria.corese.kgram.tool.ApproximateSearchEnv;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import java.util.ArrayList;

/**
 * Node and Edge binding stacks for KGRAM evaluator
 *
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class Memory extends PointerObject implements Environment {

    public static boolean DEBUG_DEFAULT = false;
    public static boolean IS_EDGE = !true;
    static final Edge[] emptyEdges = new Edge[0];
    static final Edge[] emptyEntities = new Edge[0];
    // number of times nodes are bound by Stack
    // decrease with backtrack
    int[] nbNodes, nbEdges,
            // stackIndex[n] = index in Eval Exp stack where nth node is bound first
            // enable to compute where to backjump
            stackIndex;
    Edge[] qEdges;
    Edge[] result;
    Node[] qNodes, nodes;
    Evaluator eval;
    Matcher match;
    Eval kgram;
    Stack stack;
    Exp exp;
    Object object;
    // bnode(label) must return same bnode in same solution, different otherwise.
    // hence must clear bnodes after each solution
    Map<String, IDatatype> bnode;
    //  query or sub query
    Query query;
    Node gNode;
    // to evaluate aggregates such as count(?x)
    Mappings results, group; //, join;
    Mapping mapping;
    // service evaluation detail report
    private IDatatype detail;
    // true when processing aggregate at the end 
    boolean isAggregate = false;
    private boolean isFake = false,
            isEdge = IS_EDGE;
    EventManager manager;
    boolean hasEvent = false;
    int nbEdge = 0, nbNode = 0;
    private Binding bind;
    private ApproximateSearchEnv appxSearchEnv;
    boolean debug = DEBUG_DEFAULT;

    public Memory() {}
    
    public Memory(Matcher m, Evaluator e) {
        match = m;
        eval = e;
        bnode = new HashMap();
        this.appxSearchEnv = new ApproximateSearchEnv();
    }

    public Memory setResults(Mappings r) {
        results = r;
        return this;
    }

    void setEventManager(EventManager man) {
        manager = man;
        hasEvent = true;
    }

    @Override
    public EventManager getEventManager() {
        if (manager == null) {
            kgram.createManager();
        }
        return manager;
    }

    @Override
    public boolean hasEventManager() {
        return (manager != null);
    }

    @Override
    public void setEval(Eval e) {
        kgram = e;
    }

    @Override
    public Eval getEval() {
        return kgram;
    }
    
    @Override
    public ProcessVisitor getVisitor() {
        return getEval().getVisitor();
    }

    void setGroup(Mappings lm) {
        group = lm;
    }

    Mappings getResults() {
        return results;
    }

    @Override
    public Query getQuery() {
        return query;
    }

    public Matcher getMatcher() {
        return match;
    }

    public void setGraphNode(Node g) {
        gNode = g;
    }

    @Override
    public Node getGraphNode() {
        return gNode;
    }

    void setStack(Stack s) {
        stack = s;
    }

    public Stack getStack() {
        return stack;
    }

    @Override
    public void setExp(Exp ee) {
        exp = ee;
    }

    @Override
    public Exp getExp() {
        return exp;
    }

    void setAggregate(boolean b) {
        isAggregate = b;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public void init(Memory memory) {
        setGraphNode(memory.getGraphNode());
        setEval(memory.getEval());
    }

    public Memory init(Query q) {
        // store (sub) query
        query = q;
        if (q.isSubQuery()) {
            // we need the outer query to get the max nb of nodes
            // because index may vary from 0 to max in any sub query
            q = q.getGlobalQuery();
        }
        if (q.isRecordEdge()) {
            // rule record edge
            isEdge = true;
        }
        int nmax = q.nbNodes();
        int emax = q.nbEdges();
        nbNodes = new int[nmax];
        stackIndex = new int[nmax];
        if (!isEdge) {
            emax = 0;
        }
        nbEdges = new int[emax];
        result = new Edge[emax];
        qEdges = new Edge[emax];

        nodes = new Node[nmax];
        qNodes = new Node[nmax];

        start();
        return this;
    }

    void start() {
        nbEdge = 0;
        nbNode = 0;
        for (int i = 0; i < nbNodes.length; i++) {
            qNodes[i] = null;
            nodes[i] = null;
            nbNodes[i] = 0;
            stackIndex[i] = -1;
        }
        if (isEdge) {
            for (int i = 0; i < nbEdges.length; i++) {
                nbEdges[i] = 0;
                result[i] = null;
                qEdges[i] = null;
            }
        }
    }

    @Override
     public String toString() {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (Node qNode : qNodes) {
            if (qNode != null) {
                if (n++ > 0) {
                    sb.append("\n");
                }
                sb.append("(").append(qNode.getIndex()).append(") ");
                //sb.append(nbNodes[qNode.getIndex()]).append(" ");
                sb.append(qNode).append(" = ").append(getNode(qNode));
            }
        }
        if (getBind() != null && getBind().hasBind()) {
            sb.append("\n").append(getBind());
        }
        return sb.toString();
    }
          
    Node getNode(String name, List<Node> list){
        for (Node node : list){
            if (node.getLabel().equals(name)){
                return node;
            }
        }
        return null;
    }

    /**
     * mem is a fresh new Memory, init() has been done Copy this memory into mem
     * Use case: exists {} , sub query Can bind all Memory nodes or bind only
     * subquery select nodes (2 different semantics) 
     * TODO: let ( .., exists {}),
     * MUST push BGP solution and then push Bind
     *
     */
    Memory copyInto(Query sub, Memory mem, Exp exp) {
        int n = 0;
        if (sub == null) {
            // exists {}
           copyInto(mem, exp);
        } // subquery
        else if (eval.getMode() == Evaluator.SPARQL_MODE){ 
            // SPARQL does not bind args
        } else {
            // bind subquery select nodes
            // take only from this memory the nodes
            // that are select nodes of sub query
            // hence sub query memory share only select node bindings
            // with outer query memory
            // use case: ?x :p ?z  {select ?z where {?x :q ?z}}
            // ?x in sub query is not the same as ?x in outer query (it is not bound here)
            // only ?z is the same
            for (Node subNode : sub.getSelect()) {
                copyInto(subNode, mem, n);
                n++;
            }
            mem.share(this);
        }
        return mem;
    }
    
     /**
     * Use case: exists {} in aggregate Copy Mapping into this fresh Memory
     * similar to copyInto 
     */
    void copy(Mapping map, Exp exp) {
        if (map.hasBind()) {
            copy(map.getBind(), exp);
        } 
        share(getBind(), map.getBind());
        push(map, -1);
    }
    
    /**
     * exists { }  
     * PRAGMA: when exists is in function, this memory is empty
     */
    void copyInto(Memory mem, Exp exp) {
        if (hasBind()) {
            // bind ldscript variables as sparql 
            // pattern matching variables
            mem.copy(getBind(), exp);
        } 
        mem.share(this);
        copyInto(mem);
    }
    
    /**
     * Share global variable, context, etc.
     */
    public void share(Binding target, Binding source) {
        if (source != null && target != null) {
            target.share(source);
        }
    }
    
    void share(Memory source) {
        share(getBind(), source.getBind());
    }
    
    /**
     * Copy this Bind local variable stack into this memory 
     * Use case: function xt:foo(?x) { exists { ?x ex:pp ?y } }
     */
    void copy(Binding bind, Exp exp) {
        List<Node> list = exp.getNodes();
        for (Expr var : bind.getVariables()) {
            Node qn = getNode(var.getLabel(), list);
            if (qn == null) {
            } else {
                push(qn, bind.get(var));
            }
        }
    }
    
    void copyInto(Memory mem) {
        int n = 0;
        // bind all nodes
        // use case: inpath copy the memory
        for (Node qNode : qNodes) {
            copyInto(qNode, mem, n);
            n++;
        }
    }
    
    void copyInto(Node qNode, Memory mem, int n) {
        if (qNode != null) {
            Node tNode = getNode(qNode);
            if (tNode != null) {
                mem.push(qNode, tNode, -1);
            }
        }
    }
              
    /**
     * Store a new result: take a picture of the stack as a Mapping
     */
    Mapping store(Query q, Producer p) throws SparqlException {
        return store(query, p, false, false);
    }

    Mapping store(Query q, Producer p, boolean subEval) throws SparqlException {
        return store(query, p, subEval, false);
    }

    /**
     * subEval = true : result of statement such as minus/optional etc
     * in this case: no select exp, no order by, no group by, etc
     * subEval = false: main or nested select query.
     */
    Mapping store(Query q, Producer p, boolean subEval, boolean func) throws SparqlException {
        boolean complete = ! q.getGlobalQuery().isAlgebra();
        
        Node detailNode = null;
        if (getReport() != null) {
            // draft: set service report as variable value
            // use case: xt:sparql() return map with report
            // PluginImpl sparql() record report in Environment
            // detailNode is defined by ASTParser with @report metadata
            detailNode = getQuery().getSelectNode(Binding.SERVICE_REPORT_ZERO);
            if (detailNode != null) {
                push(detailNode, getReport());
            }
        }
                
        int nb = nbNode;
        if (!subEval && complete) {            
             // select (exp as var) it may happen that var is already bound in
             // memory (bindings, subquery), so we should not allocate a
             // supplementary cell for var in Mapping node array             
            for (Exp exp : q.getSelectFun()) { //q.getSelectWithExp()) {
                if (exp.getFilter() != null && !isBound(exp.getNode())) {
                    nb++;
                }
            }
        }
        Edge[] qedge = emptyEdges;
        Edge[] tedge = emptyEntities;
        Node[] qnode = new Node[nb], tnode = new Node[nb];
        // order by
        Node[] snode = new Node[q.getOrderBy().size()],
               gnode = new Node[q.getGroupBy().size()];

        int n = 0, i = 0;
        if (isEdge) {
            qedge = new Edge[nbEdge];
            tedge = new Edge[nbEdge];
            for (Edge edge : qEdges) {
                if (edge != null) {
                    qedge[n] = edge;
                    //tedge[n] = result[i];
                    tedge[n] = p.copy(result[i]);
                    n++;
                }
                i++;
            }
        }
                          
        n = 0;
        i = 0;
        for (Node node : qNodes) {
            if (node != null) {
                qnode[n] = node;
                tnode[n] = nodes[i];
                n++;
            }
            i++;
        }
        
        Mapping map = null;
        
        if (complete) {                        
            if (subEval) {
                // statement e.g. minus/optional/union
                if (func) {                    
                    orderGroup(q.getOrderBy(), snode, p);
                    orderGroup(q.getGroupBy(), gnode, p);
                }
            } else {
                // main query or nested query
                int count = 0;
                for (Exp e : q.getSelectFun()) {

                    Filter f = e.getFilter();

                    if (f != null) {
                        // select fun(?x) as ?y
                        Node node = null;
                        boolean isBound = isBound(e.getNode());

                        if (e.isAggregate()) {
                            // do nothing
                        }
                        else {
                            //node = eval.eval(f, this, p);
                            node = kgram.eval(f, this, p);
                            kgram.getVisitor().select(kgram, f.getExp(), node==null?null:node.getDatatypeValue());
                            // bind fun(?x) as ?y
                            boolean success = push(e.getNode(), node);
                            if (success) {
                                count++;
                            } else {
                                // use case: var was already bound and there is a select (exp as var)
                                // and the two values of var are different
                                // pop previous exp nodes and return null
                                int j = 0;

                                for (Exp ee : q.getSelectFun()) {
                                    // pop previous exp nodes if any
                                    if (j >= count) {
                                        // we have poped all exp nodes
                                        return null;
                                    }

                                    if (ee.getFilter() != null) {
                                        pop(ee.getNode());
                                        j++;
                                    }
                                }
                            }
                        }

                        if (!isBound) {
                            // use case: select (exp as var) where var is already bound
                            qnode[n] = e.getNode();
                            tnode[n] = node;
                            n++;
                        }
                    }
                }

                map = new Mapping(qedge, tedge, qnode, tnode);
                mapping = map;
                map.init();   
                // order/group by may access mapping with xt:result()
                orderGroup(q.getOrderBy(), snode, p);
                orderGroup(q.getGroupBy(), gnode, p);

                for (Exp e : q.getSelectFun()) {
                    Filter f = e.getFilter();
                    if (f != null && !e.isAggregate()) {
                        // pop fun(?x) as ?y
                        pop(e.getNode());
                    }
                }
            }           
        }
        
        if (map == null) {
            map = new Mapping(qedge, tedge, qnode, tnode);
            mapping = map;
        }
        
        if (detailNode != null) {
            pop(detailNode);
            setReport(null);
        }

        map.setOrderBy(snode);
        map.setGroupBy(gnode);
        clear();
        mapping = null;

        return map;
    }
    
    @Override
    public Mapping getMapping() {
        return mapping;
    }
    
    @Override
    public int size() {
        return nbNode;
    }

    /**
     * BNode table cleared for new solution
     */
    public void clear() {
        bnode.clear();
    }

    @Override
    public Map<String, IDatatype> getMap() {
        return bnode;
    }

    void setMap(Map<String, IDatatype> m) {
        bnode = m;
    }

    Mapping store(Query q, Mapping map, Producer p) throws SparqlException {
        Node[] gnode = new Node[q.getGroupBy().size()];
        orderGroup(q.getGroupBy(), gnode, p);
        map.setGroupBy(gnode);
        return map;
    }

    void orderGroup(List<Exp> lExp, Node[] nodes, Producer p) throws SparqlException {
        int n = 0;
        for (Exp e : lExp) {
            Node qNode = e.getNode();
            if (qNode != null) {
                nodes[n] = getNode(qNode);
            }
            if (nodes[n] == null) {
                Filter f = e.getFilter();
                if (f != null && !e.isAggregate()) {
                    //nodes[n] = eval.eval(f, this, p);
                    nodes[n] = kgram.eval(f, this, p);
                }

            }
            n++;
        }
    }
    
    boolean pushNodeList(Producer p, Node node, Edge edge, int i) {
        if (node.isMatchCardinality()) {
            return pushCardinality(p, node, edge, i);
        }
        return pushList(p, node, edge, i);
    }
    
    boolean pushList(Producer p, Node node, Edge edge, int i) {
        ArrayList<Node> list = new ArrayList<>();
        for (int j = i; j<edge.nbNode(); j++) {
            list.add(edge.getNode(j));
        }
        Node target = (p == null) ? node : p.getDatatypeValueFactory().nodeList(list);       
        return push(node, target, i);
    }
    
    boolean pushCardinality(Producer p, Node node, Edge edge, int i) {
        int n = edge.nbNode() - i;
        Node target = (p == null) ? node : p.getDatatypeValueFactory().nodeValue(n);       
        return push(node, target, i);
    }

    /**
     * pop nodes when fail
     */
    boolean push(Edge q, Edge ent, int n) {
        return push(null, q, ent, n);
    }
    
    void trace(String mes, Object... obj){
        if (getQuery().isDebug()) {
            System.out.println(String.format(mes, obj));
        }
    }
    
    boolean push(Producer p, Edge q, Edge ent, int n) {
        boolean success = true;
        int max = q.nbNode();
        
        for (int i = 0; i < max; i++) {
            Node node = q.getNode(i);
            if (node != null) {
                if (node.isMatchNodeList()) {
                    success = pushNodeList(p, node, ent, i);
                } else {
                    success = push(node, ent.getNode(i), n);
                    if (!success) trace("push: %s=%s success: %s", node, ent.getNode(i), success);
                }

                if (!success) {
                    // it fail: pop right now
                    pop(q, i);
                    // stop pushing as ith node failed
                    break;
                }
            }
        }

        if (success) {
            // explicit edge node
            // e.g. the node that represents the property/relation
            Node pNode = q.getEdgeVariable();
            if (pNode != null) {
                success = push(pNode, ent.getEdgeNode(), n);

                if (!success) {
                    // it fail: pop nodes
                    pop(q, q.nbNode());
                }
            }
        }

        if (isEdge && success) {
            int index = q.getEdgeIndex();
            if (nbEdges[index] == 0) {
                nbEdge++;
            }
            nbEdges[index]++;
            qEdges[index] = q;
            result[index] = ent;
        }
        return success;
    }

    void pop(Edge q, int length) {
        for (int j = 0; j < length; j++) {
            Node qNode = q.getNode(j);
            if (qNode != null) {
                pop(qNode);
            }
        }
    }

    void event(Edge q) {
        for (int i = 0; i < q.nbNode(); i++) {
            Node node = q.getNode(i);
            if (node != null) {
                if (nbNodes[node.getIndex()] == 1) {
                    send(Event.BIND, node, nodes[node.getIndex()]);
                }
            }
        }
    }

    void send(int type, Object obj, Object arg) {
        Event e = EventImpl.create(type, obj, arg);
        manager.send(e);
    }

    /**
     * Push a target node in the stack only if the binding is correct: same
     * query/ same target
     */
    public boolean push(Node node, Node target) {
        return push(node, target, -1);
    }

    /**
     * n is the index in Exp stack where Node is bound
     */
    boolean push(Node node, Node target, int n) {
        if (node.isConstant()) {
            return true;
        }
        int index = node.getIndex();
        if (nodes[index] == null) { // (nbNodes[index] > 0){
            nodes[index] = target;
            qNodes[index] = node;

            nbNode++;
            nbNodes[index]++;
            // exp stack index where node is bound
            stackIndex[index] = n;
            return true;
        } else if (target == null) {
            // may happen with aggregate or subquery
            return false;
        } else if (match.same(node, nodes[index], target, this)) {
            nbNodes[index]++;
            return true;
        }

        // Query node already bound but target not equal to binding
        // also process use case: ?x ?p ?p
        return false;
    }

    public void pop(Node node) {
        if (node.isVariable()) {
            int index = node.getIndex();
            if (nbNodes[index] > 0) {
                nbNodes[index]--;
                if (nbNodes[index] == 0) {
                    nbNode--;
                    nodes[index] = null;
                    qNodes[index] = null;
                    stackIndex[index] = -1;
                }
            }
        }
    }

    /*
     * max index where edge nodes are bound first
     * hence where to backjump when edge fails
     */
    int getIndex(Node gNode, Edge edge) {
        int[] stack = stackIndex;
        int max = -1;
        int length = edge.nbNode();
        for (int i = 0; i < length; i++) {
            Node qNode = edge.getNode(i);
            if (qNode != null) {
                int n = qNode.getIndex();
                if (stack[n] > max) {
                    max = stack[n];
                }
            }
        }
        Node pNode = edge.getEdgeVariable();
        if (pNode != null) {
            int n = pNode.getIndex();
            if (stack[n] > max) {
                max = stack[n];
            }
        }
        if (gNode != null && gNode.isVariable()) {
            int n = gNode.getIndex();
            if (stack[n] > max) {
                max = stack[n];
            }
        }
        return max;
    }

    int getIndex(List<Node> lNodes) {
        int max = -1;
        for (Node node : lNodes) {
            int index = getIndex(node);
            if (index == -1) {
                return -1;
            }
            max = Math.max(max, index);
        }
        return max;
    }

    int getIndex(Node node) {
        return stackIndex[node.getIndex()];
    }

    void pop(Edge q, Edge r) {
        popNode(q, r);
        if (isEdge) {
            popEdge(q, r);
        }
    }

    void popNode(Edge q, Edge r) {
        if (q != null) {
            int max = q.nbNode();
            for (int i = 0; i < max; i++) {
                Node node = q.getNode(i);
                if (node != null) {
                    pop(node);
                }
            }

            // the edge node if any
            // use case: ?x ?p ?y
            Node pNode = q.getEdgeVariable();
            if (pNode != null) {
                // it was pushed only if it is a variable
                pop(pNode);
            }
        }
    }

    void popEdge(Edge q, Edge r) {
        int index = q.getEdgeIndex();
        if (nbEdges[index] > 0) {
            nbEdges[index]--;
            if (nbEdges[index] == 0) {
                nbEdge--;
                qEdges[index] = null;
                result[index] = null;
            }
        }
    }

    /**
     * Push elementary result in the memory
     */
    boolean push(Mapping res, int n) {
        return push(res, n, isEdge);
    }

    /**
     * Bind Mapping in order to compute aggregate on one group Create a fresh
     * new bnode table for the solution of this group use case: select
     * (count(?x) as ?c) (bnode(?c) as ?bn)
     *
     */
    void aggregate(Mapping map) {
        push(map, -1);
        Map<String, IDatatype> bnode = map.getMap();
        if (bnode == null) {
            bnode = new HashMap();
            map.setMap(bnode);
        }
        setMap(bnode);
    }

    boolean push(Mapping res, int n, boolean isEdge) {
        return push(res, n, isEdge, true);
    }
    
    boolean push(Mapping res, int n, boolean isEdge, boolean isBlank) {
        int k = 0;
        for (Node qNode : res.getQueryNodes()) {
            if (qNode != null && qNode.getIndex() >= 0) {
                // use case: skip select fun() as var
                // when var has no index
                if (!qNode.isBlank() || isBlank) {
                    // do not push service bnode
                    Node node = res.getNode(k);
                    if (push(qNode, node, n)) {
                    } else {
                        for (int i = 0; i < k; i++) {
                            pop(res.getQueryNode(i));
                        }
                        return false;
                    }
                }
            }
            k++;
        }

        if (isEdge) {
            k = 0;
            for (Edge qEdge : res.getQueryEdges()) {
                Edge edge = res.getEdge(k);
                if (!push(qEdge, edge, n)) {
                    for (int i = 0; i < k; i++) {
                        pop(res.getQueryEdge(i), res.getEdge(i));
                    }
                    // TODO: pop the nodes
                    System.out.println("**** MEMORY: push mapping fail on edges");
                    return false;
                }
                k++;
            }
        }       
        return true;
    }

    /**
     * values (?x ?y) { unnest(exp) } exp returns Mappings map push ?x and ?y
     */
    boolean push(HashMap<String, Node> list, Mapping map, int n) {
        int k = 0;
        for (Node qNode : map.getQueryNodes()) {
            if (qNode != null) {
                Node tNode = list.get(qNode.getLabel());
                if (tNode != null) {
                    Node node = map.getNodeProtect(k);
                    if (push(tNode, node, n)) {                       
                    } else {
                        // pop
                        for (int i = 0; i < k; i++) {
                            Node qq = map.getQueryNode(i);
                            if (qq != null) {
                                Node tt = list.get(qq.getLabel());
                                if (tt != null) {
                                    pop(tt);                                   
                                }
                            }
                        }
                        return false;
                    }
                }
            }
            k++;
        }
        return true;
    }

    void pop(HashMap<String, Node> list, Mapping map) {
        int n = 0;
        for (Node qNode : map.getQueryNodes()) {
            if (qNode != null) {
                Node tNode = list.get(qNode.getLabel());
                if (tNode != null) {
                    pop(tNode);                  
                }
            }
            n++;
        }
    }

    /**
     * Pop elementary result
     */
    void pop(Mapping res) {
        pop(res, true);
    }

    void pop(Mapping res, boolean isEdge) {
        int n = 0;
        for (Node qNode : res.getQueryNodes()) {
            if (qNode != null && qNode.getIndex() >= 0) {
                pop(qNode);
            }
            n++;
        }

        if (isEdge) {
            n = 0;
            for (Edge qEdge : res.getQueryEdges()) {
                pop(qEdge, res.getEdge(n++));
            }
        }
    }

    public void pushPath(Node qNode, Path path) {
    }

    public void popPath(Node qNode) {
    }

    Node getNode(int n) {
        return nodes[n];
    }

    @Override
    public Node getQueryNode(int n) {
        return qNodes[n];
    }

    @Override
    public Node[] getQueryNodes() {
        return qNodes;
    }

    @Override
    public boolean isBound(Node qNode) {
        return getNode(qNode) != null;
    }

    public Edge getEdge(Edge qEdge) {
        return result[qEdge.getEdgeIndex()];
    }

    public Edge getEdge(int n) {
        return result[n];
    }

    public Edge[] getQueryEdges() {
        return qEdges;
    }

    @Override
    public Edge[] getEdges() {
        return result;
    }

    public Edge getEdge(Node qnode) {
        for (Edge e : getQueryEdges()) {
            if (e.getEdgeVariable() != null
                    && e.getEdgeVariable().equals(qnode)) {
                return getEdge(e);
            }
        }
        return null;
    }

    public Edge getEdge(String var) {
        for (Edge e : getQueryEdges()) {
            if (e != null && e.getEdgeVariable() != null
                    && e.getEdgeVariable().getLabel().equals(var)) {
                return getEdge(e);
            }
        }
        return null;
    }

    @Override
    public Node[] getNodes() {
        return nodes;
    }

    /**
     * The target Node of a query Node in the stack
     */
    @Override
    public Node getNode(Node node) {
        if (node.isConstant()) {
            return node;
        }
        int n = node.getIndex();
        if (n == -1) {
            return null;
        }       
        return nodes[n];
    }

    @Override
    public Node getNode(String name) {
        int index = getIndex(name);
        if (index == ExprType.UNBOUND) {
            return null;
        }
        return getNode(index);
    }

    /**
     * Used by aggregate, stack is empty, search in query go also into subquery
     * select because outer query may reference an inner variable in an outer
     * aggregate use case: select count(?x) as ?count where { {select ?x where
     * {...}} }
     */
    @Override
    public Node getQueryNode(String name) {
        Node node = query.getProperAndSubSelectNode(name); 
        return node;
    }

    /**
     * Index of a Node in the stack given its name For filter variable
     * evaluation We start at the end to get the latest bound node use case:
     * graph ?g {{select where {?g ?p ?y}}} outer ?g is the graph node inner ?g
     * is another value gNode = outer ?g
     */
    int getIndex(String name) {
        for (int i = qNodes.length - 1; i >= 0; i--) {
            Node node = qNodes[i];
            if (node != null && node.getLabel().equals(name)) {
                return i;
            }
        }
        return ExprType.UNBOUND;
    }
    
    @Override
    public Node getNode(Expr var) {
        int index = var.getIndex();
        switch (var.subtype()) {
            // ldscript variable
            // normally we do not get here because ldscript variable is 
            // instance of VariableLocal and eval() call Binding directly
            // however, it is not a bug, it is just less efficient to be here
            case ExprType.LOCAL: 
                return get(var);

            case ExprType.UNDEF:
                if (debug) {
                    System.out.println("Memory UNDEF: Unbound variable: " + var);
                }
                return null;
            // sparql bgp
            case ExprType.GLOBAL:
                index = getIndex(var.getLabel());
                var.setIndex(index);
                if (index == ExprType.UNBOUND) {
                    if (debug) {
                        System.out.println("Memory GLOBAL: Unbound variable: " + var);
                    }
                    return null;
                }

        }
        Node node = getNode(index);
        if (node == null && debug) {
            System.out.println("Memory DEFAULT: Unbound variable: " + var);
            if (var.getLabel().equals("?value")) {
                System.out.println(query.getAST());
                System.out.println(this);
            }
        }
        return node;
    }

    // Filter evaluator
    public Evaluator getEvaluator() {
        return eval;
    }

    /**
     * *************************************
     *
     * Aggregates and system functions
     */
    @Override
    public int count() {
        return current().size();
    }

    public int sum(Node qNode) {
        return -1;
    }

    // sum(?x)
//    @Override
//    public void aggregate(Evaluator eval, Producer p, Filter f) {
//        current().aggregate(eval, f, this, p);
//    }

    public Node max(Node qNode) {
        return current().max(qNode);
    }

    public Node min(Node qNode) {
        return current().min(qNode);
    }

    /**
     * Current group is set by Mappings aggregate function
     */
    public Mappings current() {
        if (group != null) {
            return group;
        } else {
            return results;
        }
    }

    @Override
    public Mappings getMappings() {
        return current();
    }
    
//    Mappings getJoinMappings() {
//        return join;
//    }
//    
//    Mappings getResetJoinMappings() {
//        Mappings map = getJoinMappings();
//        setJoinMappings(null);
//        return map;
//    }
//    
//    void setJoinMappings(Mappings m) {
//        join = m;
//    }
    
    /**
     * Iterate Mappings for aggregate
     * @return 
     */
    @Override
    public Iterable<Mapping> getAggregate() {
        if (current().isFake()) {
            return new ArrayList<>(0);
        }
        return current();
    }
    
    /**
     * Prepare Mapping for aggregate
     * @param map
     * @param n 
     */
    @Override
    public void aggregate(Mapping map, int n) {
        current().prepareAggregate(map, getQuery(), getMap(), n);
    }
      
    @Override
    public int pathLength(Node qNode) {
        Path path = getPath(qNode);
        if (path == null) {
            return 0;
        }
        return path.length();
    }

    @Override
    public int pathWeight(Node qNode) {
        Path path = getPath(qNode);
        if (path == null) {
            return 0;
        }
        return path.weight();
    }

    @Override
    public Path getPath(Node qNode) {
        Node node =  getNode(qNode);
        if (node == null) {
            return null;
        }
        return node.getPath();
    }

    boolean isPath(Node qNode) {
        return getPath(qNode) != null;
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public void setObject(Object o) {
        object = o;
    }

    public void setFake(boolean isFake) {
        this.isFake = isFake;
    }

    boolean isFake() {
        return isFake;
    }

    @Override
    public ASTExtension getExtension() {
        return query.getActualExtension();
    }

    @Override
    public Node get(Expr var) {
        return bind.get(var);
    }


    @Override
    public void setBind(Binding b) {
        bind = b;
    }
    
    public Memory setBinding(Binding b) {
        setBind(b);
        return this;
    }

    @Override
    public Binding getBind() {
        return bind;
    }

    @Override
    public boolean hasBind() {
        return bind != null && bind.hasBind();
    }
    
    @Override
    public ApproximateSearchEnv getAppxSearchEnv() {
        return this.appxSearchEnv;
    }

    public void setAppxSearchEnv(ApproximateSearchEnv appxEnv) {
        this.appxSearchEnv = appxEnv;
    }
    
    
    
       /**
     * List of variable binding
     * @return 
     */
    @Override
    public Iterable<List<IDatatype>> getLoop() {
        return getList();
    }
        
    List<List<IDatatype>> getList() {    
        ArrayList<List<IDatatype>> list = new ArrayList<>();
        int i = 0;
        for (Node n : getQueryNodes()) {
            Node val = getNode(i++);
            if (n!= null && val != null){
                ArrayList<IDatatype> l = new ArrayList<>(2);
                l.add(n.getDatatypeValue());
                l.add(val.getDatatypeValue());
                list.add(l);
            }
        }
        return list;
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
        Node node = getNode(var);
        if (node == null) {
            return null;
        }
        return node.getDatatypeValue();
    }

    List<IDatatype> getBinding(int n){
        List<List<IDatatype>> l = getList();
        if (n < l.size()){
            return l.get(n);
        }
        return null;
    }
    
    public static void recordEdge(boolean b) {
        IS_EDGE = b;
    }

    public IDatatype getReport() {
        return detail;
    }

    @Override
    public void setReport(IDatatype detail) {
        this.detail = detail;
    }
    
}
