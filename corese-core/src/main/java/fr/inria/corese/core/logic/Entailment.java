package fr.inria.corese.core.logic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 * RDFS Entailment
 *
 * rdfs:domain rdfs:range rdfs:subPropertyOf rdfs:subClassOf
 * owl:SymmetricProperty owl:inverseOf
 *
 * subPropertyOf & subClassOf are not transitive in the graph but their
 * instances are typed according to transitivity
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Entailment implements Engine {

    private static Logger logger = LoggerFactory.getLogger(Entailment.class);
    private static final String S_TYPE = RDF.TYPE;
    private static final String S_BLI = RDF.BLI;
    private static final String S_PROPERTY = RDF.PROPERTY;
    private static final String S_RDFS = RDFS.RDFS;
    private static final String S_RESOURCE = RDFS.RESOURCE;
    private static final String S_SUBCLASSOF = RDFS.SUBCLASSOF;
    private static final String S_SUBPROPERTYOF = RDFS.SUBPROPERTYOF;
    private static final String S_MEMBER = RDFS.MEMBER;
    private static final String S_MEMBERSHIP = RDFS.MEMBERSHIP;
    private static final String S_THING = OWL.THING;
    static final String W3C = "http://www.w3.org";
    public static final String KGRAPH2 = "http://ns.inria.fr/edelweiss/2010/kgraph#";
    public static final String KGRAPH = ExpType.KGRAM;
    public static String DEFAULT = ExpType.DEFAULT_GRAPH;
    public static String ENTAIL = KGRAPH + "entailment";
    public static String RULE = KGRAPH + "rule";
    public static String CONSTRAINT = KGRAPH + "constraint";
    public static String[] GRAPHS = {DEFAULT, ENTAIL, RULE};
    public static final String XSD = "http://www.w3.org/2001/XMLSchema#";
    // take literal range into account in loader
    public static final String DATATYPE_INFERENCE = KGRAPH + "datatype";
    // false: do not duplicate RDFS entailment in kg:entailment graph
    public static final String DUPLICATE_INFERENCE = KGRAPH + "duplicate";
    static final int UNDEF = -1;
    static final int SUBCLASSOF = 0;
    static final int SUBPROPERTYOF = 1;
    static final int DOMAIN = 2;
    static final int RANGE = 3;
    static final int TYPE = 4;
    static final int MEMBER = 5;
    static final int INVERSEOF = 6;
    static final int RDF_ENTAIL = 7;
    static final int SYMMETRIC = 30;
    public static boolean trace = false;
    Signature domain, range, inverse, symetric, subproperty;
    Graph graph; 
    List<Edge> targetList;
    Node hasType, subClassOf, graphNode;
    Edge last, current;
    Hashtable<Node, Integer> count;
    Hashtable<String, Integer> keyword;
    boolean // generate rdf:type wrt rdfs:subClassOf
            isSubClassOf = !true,
            isSubPropertyOf = true,
            // entailments in default graph
            isDefaultGraph = true,
            // infer datatype from property range for literal (à la corese)
            isDatatypeInference = false,
            isDomain = true,
            isRange = true,
            isRDF = true,
            isMember = true,
            isActivate = true;
    // deprecated
    boolean recurse = false,
            isDebug = false;

    class Signature extends Hashtable<Node, List<Node>> {

        void define(Node pred, Node value) {
            List<Node> list = get(pred);
            if (list == null) {
                list = new ArrayList<Node>();
                put(pred, list);
            }
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

    public static Entailment create(Graph g) {
        return new Entailment(g);
    }

    Entailment(Graph g) {
        graph = g;
        symetric = new Signature();
        inverse = new Signature();
        domain = new Signature();
        range = new Signature();
        subproperty = new Signature();
        keyword = new Hashtable<String, Integer>();
        count = new Hashtable<Node, Integer>();
        hasType = graph.addProperty(S_TYPE);
        defProperty();
    }

    public void onClear() {
        clear();
    }

    void clear() {
        symetric.clear();
        inverse.clear();
        domain.clear();
        range.clear();
        subproperty.clear();
        hasType = graph.addProperty(S_TYPE);
    }

    // use RDFS metamodel
    void defProperty() {
        defEntity(RDF.RDF, RDF_ENTAIL);
        defEntity(RDF.TYPE, TYPE);
        defEntity(RDFS.SUBCLASSOF, SUBCLASSOF);
        defEntity(RDFS.SUBPROPERTYOF, SUBPROPERTYOF);
        defEntity(RDFS.DOMAIN, DOMAIN);
        defEntity(RDFS.RANGE, RANGE);
        defEntity(RDFS.MEMBER, MEMBER);
        defEntity(OWL.INVERSEOF, INVERSEOF);
        defEntity(OWL.SYMMETRIC, SYMMETRIC);

    }

    public void defEntity(String name, int type) {
        keyword.put(name, type);
    }

    Integer getType(String name) {
        Integer type = keyword.get(name);
        if (type == null) {
            type = UNDEF;
        }
        return type;
    }

    public void set(String name, boolean b) {

        switch (getType(name)) {
            case RDF_ENTAIL:
                rdfEntailment();
                break;
            case SUBCLASSOF:
                isSubClassOf = b;
                break;
            case SUBPROPERTYOF:
                isSubPropertyOf = b;
                break;
            case DOMAIN:
                isDomain = b;
                break;
            case RANGE:
                isRange = b;
                break;
            case MEMBER:
                isMember = b;
                break;

            default:
                if (name.equals(DATATYPE_INFERENCE)) {
                    isDatatypeInference = b;
                } else if (name.equals(ENTAIL)) {
                    isDefaultGraph = b;
                }
        }
    }

    void rdfEntailment() {
        set(RDFS.SUBCLASSOF, false);
        set(RDFS.SUBPROPERTYOF, false);
        set(RDFS.DOMAIN, false);
        set(RDFS.RANGE, false);
        set(RDFS.MEMBER, false);
    }

    public boolean isDatatypeInference() {
        return isDatatypeInference;
    }

    public boolean isSubClassOfInference() {
        return isSubClassOf;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    /**
     * clear tables of meta statements (domain, range, etc.) fill these tables
     * with current graph
     */
    @Override
    public void onDelete() {
        reset();
    }

    void reset() {
        clear();
        define();
    }

    /**
     * Record definitions corresponding to ontological edges from graph: pp
     * rdfs:range rr use case: add Entailment on existing graph use case:
     * redefine after delete
     */
    @Override
    public void init() {
        define();
    }

    void define() {

        for (Node pred : graph.getSortedProperties()) {
            boolean isType = isType(pred);

            for (Edge ent : graph.getEdges(pred)) {
                Edge edge = ent;
                boolean isMeta = define(ent.getGraph(), ent);
                if (!isMeta) {
                    if (isType) {
                        // continue for rdf:type owl:Symmetric
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean process() {
        if (graph.size() == 0) {
            return false;
        }
        graph.getEventManager().start(Event.InferenceEngine, getClass().getName());
        int size = graph.size();
        entail();
        graph.getEventManager().finish(Event.InferenceEngine, getClass().getName());
        return graph.size() > size;
    }

    int entail() {
        int nb = inference();
        return nb;
    }

    /**
     * Internal process of entailed edge
     */
    void recordWithoutEntailment(Node gNode, Edge ee, Edge edge) {
        if (! graph.exist(edge)){
            targetList.add(edge);
        }       
    }

    void recordWithEntailment(Node gNode, Edge ee, Edge edge) {
        recordWithoutEntailment(gNode, ee, edge);
        define(gNode, edge);
    }

    Edge create(Node src, Node sub, Node pred, Node obj) {
        return graph.createForInsert(src, sub, pred, obj);
    }

    /**
     * Store property domain, range, subPropertyOf, symmetric, inverse
     */
    @Override
    public void onInsert(Node gNode, Edge edge) {
        define(gNode, edge);
    }

    boolean define(Node gNode, Edge edge) {
        //if (! edge.getLabel().startsWith(W3C)) return;
        boolean isMeta = true;

        switch (getType(edge.getEdgeLabel())) {

            case TYPE:
                if (getType(edge.getNode(1).getLabel()) == SYMMETRIC) {
                    symetric.define(edge.getNode(0), edge.getNode(0));
                }
                break;

            case DOMAIN:
                domain.define(edge.getNode(0), edge.getNode(1));
                break;

            case RANGE:
                range.define(edge.getNode(0), edge.getNode(1));
                break;

            case SUBPROPERTYOF:
                subproperty.define(edge.getNode(0), edge.getNode(1));
                break;

            case SUBCLASSOF:
                subClassOf = edge.getEdgeNode();
                break;

            case INVERSEOF:
                inverse.define(edge.getNode(0), edge.getNode(1));
                inverse.define(edge.getNode(1), edge.getNode(0));
                break;

            default:
                isMeta = false;


        }


        return isMeta;
    }

    /**
     * Add RDFS entailment to the graph, given edge and RDFS Schema
     *
     * Entail domain, range, subPropertyOf, symmetric, inverse
     *
     */
    public void entail(Node gNode, Edge edge) {
        property(gNode, edge);
        signature(gNode, edge);
        subsume(gNode, edge);
    }

    /**
     * graph creates new property pNode infer: pNode rdf:type rdf:Property TODO:
     * BUG: concurrent modification while entailment TODO: move at entailment
     * time
     */
    void defProperty(Node pNode) {
        Node gNode = graph.addGraph(ENTAIL);
        Node tNode = graph.addResource(S_PROPERTY);
        graph.add(pNode);
        Edge ee = create(gNode, pNode, hasType, tNode);
        recordWithoutEntailment(gNode, null, ee);

        if (isMember && pNode.getLabel().startsWith(S_BLI)) {
            // rdf:_i rdfs:subPropertyOf rdfs:member
            tNode = graph.addResource(S_MEMBER);
            Node sub = graph.addProperty(S_SUBPROPERTYOF);
            ee = create(gNode, pNode, sub, tNode);
            recordWithEntailment(gNode, null, ee);

            // rdf:_i rdf:type rdfs:ContainerMembershipProperty
            Node mem = graph.addResource(S_MEMBERSHIP);
            ee = create(gNode, pNode, hasType, mem);
            recordWithoutEntailment(gNode, null, ee);
        }
    }

    void property(Node gNode, Edge edge) {
        inverse(gNode, edge, symetric);
        inverse(gNode, edge, inverse);

        subproperty(gNode, edge);
    }

    void inverse(Node gNode, Edge edge, Signature table) {
        Node pred = edge.getEdgeNode();
        List<Node> list = table.get(pred);
        if (list != null) {
            for (Node type : list) {
                Edge ee = create(gNode, edge.getNode(1), type, edge.getNode(0));
                recordWithoutEntailment(gNode, edge, ee);
            }
        }
    }

    void subproperty(Node gNode, Edge edge) {
        if (!isSubPropertyOf) {
            return;
        }

        Node pred = edge.getEdgeNode();
        List<Node> list = subproperty.get(pred);
        if (list != null) {
            for (Node sup : list) {
                Edge ee = create(gNode, edge.getNode(0), sup, edge.getNode(1));
                recordWithoutEntailment(gNode, edge, ee);
                if (isMeta(sup)) {
                    define(gNode, ee);
                }
            }
        }
    }

    /**
     * Man intersectionOf (Human Male) Human unionOf (Man Woman) edge: Man
     * intersectionOf _:b
     */
    void interunion(Node gNode, Edge edge) {
        if (edge.getNode(0).isBlank()) {
            return;
        }

        if (hasLabel(edge, OWL.INTERSECTIONOF)) {
            interunion(gNode, edge, false);
        } else if (hasLabel(edge, OWL.UNIONOF)) {
            interunion(gNode, edge, true);
        }
    }

    void interunion(Node gNode, Edge edge, boolean union) {
        Node node = edge.getNode(0);
        Node bnode = edge.getNode(1);
        List<Node> list = graph.getList(bnode);

        for (Node elem : list) {
            if (!elem.isBlank()) {
                Edge ee;
                if (union) {
                    ee = create(gNode, elem, subClassOf, node);
                } else {
                    ee = create(gNode, node, subClassOf, elem);
                }
                recordWithoutEntailment(gNode, edge, ee);
            }
        }
    }

    void signature(Node gNode, Edge edge) {
        domain(gNode, edge);
        range(gNode, edge);
    }

    void domain(Node gNode, Edge edge) {
        if (isDomain) {
            Node pred = edge.getEdgeNode();
            infer(gNode, edge, domain.get(pred), 0);
        }
    }

    void range(Node gNode, Edge edge) {
        if (isRange && graph.isIndividual(edge.getNode(1))) {
            Node pred = edge.getEdgeNode();
            infer(gNode, edge, range.get(pred), 1);
        }
    }

    void subsume(Node gNode, Edge edge) {
        // infer types using subClassOf
        if (isSubClassOf && isType(edge)) {
            infer(gNode, edge);
        }
    }

    boolean differ(Edge edge, Edge last) {
        if (last == null) {
            return true;
        }
        return !(edge.getNode(0).same(last.getNode(0))
                && edge.getEdgeNode().same(last.getEdgeNode()));
    }

    /**
     * signature
     */
    void infer(Node gNode, Edge edge, List<Node> list, int i) {
        Node node = edge.getNode(i);
        IDatatype dt =  node.getValue();

        if (i == 1 && dt.isLiteral()) {
            return;
        }
        
        if (list != null) {
            for (Node type : list) {
                Edge ee = create(gNode, node, hasType, type);
                recordWithoutEntailment(gNode, edge, ee);
            }
        }
    }

    /**
     * edge: in:aa rdf:type ex:Person infer super classes
     */
    void infer(Node gNode, Edge edge) {
        if (subClassOf == null) {
            return;
        }

        Iterable<Edge> list = graph.getEdges(subClassOf, edge.getNode(1), 0);

        if (list != null) {
            for (Edge type : list) {
                Edge ee =
                        create(gNode, edge.getNode(0), hasType, type.getNode(1));
                recordWithoutEntailment(gNode, edge, ee);
            }
        }
    }

    public List<Node> getSubClass(Node node) {
        ArrayList<Node> list = new ArrayList<Node>();
        getClasses(node, list, true);
        return list;
    }

    public List<Node> getSuperClass(Node node) {
        ArrayList<Node> list = new ArrayList<Node>();
        getClasses(node, list, false);
        return list;
    }

    /**
     * TODO: track loop
     */
    public void getClasses(Node node, List<Node> list, boolean isSubClass) {
        Iterable<Edge> it =
                graph.getEdges(graph.getPropertyNode(S_SUBCLASSOF), node, (isSubClass) ? 1 : 0);

        if (it == null) {
            return;
        }

        for (Edge ent : it) {
            Node nn = ent.getNode((isSubClass) ? 0 : 1);
            if (!list.contains(nn)) {
                list.add(nn);
                getClasses(nn, list, isSubClass);
            }
        }
    }

    class Table extends Hashtable<Node, Node> {

        boolean visited(Node node) {
            return containsKey(node);
        }

        void enter(Node node) {
            put(node, node);
        }

        void leave(Node node) {
            remove(node);
        }
    }

    public boolean isSubClassOf(Node node, Node sup) {
        if (node.same(sup)) {
            return true;
        }
        Node pred = graph.getPropertyNode(S_SUBCLASSOF);
        if (pred == null) {
            return false;
        }
        return isSubOf(pred, node, sup, new Table());
    }

    public boolean isSubPropertyOf(Node node, Node sup) {
        if (node.same(sup)) {
            return true;
        }
        Node pred = graph.getPropertyNode(S_SUBPROPERTYOF);
        if (pred == null) {
            return false;
        }
        return isSubOf(pred, node, sup, new Table());
    }

    /**
     * Take loop into account
     */
    boolean isSubOf(Node pred, Node node, Node sup, Table t) {
        Iterable<Edge> it = graph.getEdges(pred, node, 0);

        if (it == null) {
            return false;
        }

        t.enter(node);

        for (Edge ent : it) {
            Node nn = ent.getNode(1);
            if (nn.same(sup)) {
                return true;
            }
            if (nn.same(node)) {
                continue;
            }
            if (t.visited(nn)) {
                continue;
            }
            if (isSubOf(pred, nn, sup, t)) {
                return true;
            }
        }

        t.leave(node);

        return false;
    }

    public boolean isEntailed(Node source) {
        return isEntailment(source) || isRule(source);
    }

    public boolean isEntailment(Node source) {
        return hasLabel(source, ENTAIL);
    }

    public boolean isRule(Node source) {
        return hasLabel(source, RULE);
    }

    public boolean isRule(Edge e) {
        return hasLabel(e.getGraph(), RULE);
    }

    public boolean isType(Edge edge) {
        return getType(edge.getEdgeNode().getLabel()) == TYPE;
    }

    public boolean isType(Node pred) {
        return getType(pred.getLabel()) == TYPE;
    }

    public boolean isSubClassOf(Node pred) {
        return getType(pred.getLabel()) == SUBCLASSOF;
    }

    public boolean isSubClassOf(Edge edge) {
        return getType(edge.getEdgeNode().getLabel()) == SUBCLASSOF;
    }

    boolean hasLabel(Edge edge, String type) {
        return edge.getEdgeLabel().equals(type);
    }

    boolean hasLabel(Node node, String type) {
        return node.getLabel().equals(type);
    }

    public boolean isSymmetric(Edge edge) {
        return symetric.containsKey(edge.getEdgeNode());
    }

    public boolean isTopClass(Node node) {
        return node.getLabel().equals(S_RESOURCE)
                || node.getLabel().equals(S_THING);
    }

    /**
     * *******************
     *
     * Entailments
     *
     ********************
     */
    int inference() {
        targetList = new ArrayList<Edge>();
        int count = 0;

        // extension of metamodel
        meta();
        List<Edge> lDef = copy(targetList, graph);
        count += lDef.size();

        targetList = new ArrayList<Edge>();

        // first: entail for all edges in graph
        // and add infered edges in fresh target graph
        graphEntail();

        count += loop();

        return count;
    }

    /**
     * Complementary entailment from rules on new edge list
     */
    public int entail(List<Edge> list) {
        inference(list);
        return loop();
    }

    /**
     * second: loop on target to infer new edges until no new edges are infered
     * new edges are added in list
     */
    int loop() {
        int count = 0;

        boolean any = true;

        while (any) {

            any = false;

            // Try to add in graph the entailed edges
            // already existing edges are rejected
            // accepted edges are also put in list

            List<Edge> list = copy(targetList, graph);

            // loop on new infered edges
            if (list.size() > 0) {
                any = true;
                inference(list);
            }

            count += list.size();

        }

        return count;
    }

    void inference(List<Edge> list) {
        graph.getEventManager().start(Event.InferenceCycle);
        if (isDebug) {
            logger.info("Entail list: " + list.size());
        }
        targetList = new ArrayList<Edge>();
        
        Edge prev = null;

        for (Edge ent : list) {

            Edge edge = ent;
            Node gg = getGraph(ent);

            property(gg, edge);
            subsume(gg, edge);
            signature(gg, edge);

            if (prev == null) {
                prev = ent;
                defProperty(ent.getEdgeNode());
            } else if (prev.getEdgeNode() != ent.getEdgeNode()) {
                defProperty(ent.getEdgeNode());
            }
        }
        graph.getEventManager().finish(Event.InferenceCycle);
    }

    /**
     * Copy entailed edges into graph
     */
    List<Edge> copy(List<Edge> list, Graph to) {
        return to.copy(list);
    }

    /**
     * Graph where entailed edges are stored May be default or edge graph
     */
    Node getGraph(Edge ent) {
        if (isDefaultGraph) {
            if (graphNode == null) {
                graphNode = graph.addGraph(ENTAIL);
            }
            return graphNode;
        }
        return ent.getGraph();
    }

    /**
     * First loop on whole graph that was just loaded Entailed edges stored in
     * fresh target graph TODO: defProperty() for rule entail
     */
    void graphEntail() {
        if (isDebug) {
            //logger.info(graph.getIndex());
        }
        for (Node pred : graph.getProperties()) {
            if (isDebug) {
                logger.info("Entail: " + pred + " " + graph.size(pred));
            }
            Node pdomain = null, prange = null;
            boolean isFirst = true;
            
            for (Edge ent : graph.getEdges(pred)) {
                
                if (isFirst) {
                    // ?p rdf:type rdf:Property
                    defProperty(pred);
                }

                Edge edge = ent;
                Node gg = getGraph(ent);

                property(gg, edge);
                subsume(gg, edge);


                //signature(gg, edge);

                if (isFirst) {
                    isFirst = false;
                    signature(gg, edge);
                    pdomain = ent.getNode(0);
                    prange  = ent.getNode(1);                    
                } else {

                    if (pdomain != ent.getNode(0)
                            || !isDefaultGraph) {
                        domain(gg, edge);
                        pdomain = ent.getNode(0);                      
                    }
                    
                    if (prange != ent.getNode(1)
                            || !isDefaultGraph) {
                        range(gg, edge);
                        prange = ent.getNode(1);
                    }
                }


            }
        }
    }

    /**
     *
     * Meta model refinement
     *
     * Currently: wrt rdfs:subPropertyOf rdfs:property only direct subproperties
     * only
     *
     * codomain rdfs:subPropertyOf rdfs:range && pp codomain rr => pp range rr
     *
     * TODO: subProperty at depth more than 1 hasType inverseOf rdf:type
     * MySymmetric subClassOf owl:Symmetric
     *
     */
    void meta() {
        Node subprop = graph.getPropertyNode(S_SUBPROPERTYOF);
        if (subprop != null) {
            for (Edge ent : graph.getEdges(subprop)) {
                Edge edge = ent;
                if (isMeta(edge.getNode(1))) {
                    // codomain subPropertyOf rdfs:range
                    Node pred = edge.getNode(0);
                    for (Edge meta : graph.getEdges(pred)) {
                        // entail: pp codomain dd
                        subproperty(getGraph(meta), meta);
                    }
                }
            }
        }
    }

    boolean isMeta(Node pred) {
        return pred.getLabel().startsWith(S_RDFS);
    }

    public String getRange(String pred) {
        Node node = graph.getPropertyNode(pred);
        if (node == null) {
            return null;
        }
        List<Node> list = range.get(node);
        if (list == null) {
            return null;
        }
        return list.get(0).getLabel();
    }

    public boolean typeCheck() {
        boolean res = true;
        NSManager nsm = NSManager.create();
        for (Node prop : graph.getProperties()) {
            boolean isDatatype = false;
            String range = getRange(prop.getLabel());
            if (range != null) {
                isDatatype = DatatypeMap.isDatatype(range);
            }
            for (Edge ent : graph.getEdges(prop)) {
                IDatatype dt =  ent.getNode(1).getValue();
                if (range == null) {
                    if (DatatypeMap.isUndefined(dt)) {
                        logger.warn("Datatype error: " + dt);
                        res = false;
                    }
                } else {
                    boolean b = check(dt, range, isDatatype);
                    if (!b) {
                        logger.warn("Range error: " + dt + " " + nsm.toPrefix(range));
                    }
                    res = res && b;
                }
            }
        }
        return res;
    }

    boolean check(IDatatype dt, String range, boolean isDatatype) {
        if (DatatypeMap.isUndefined(dt)) {
            return false;
        }
        if (isDatatype) {
            if (dt.isLiteral()) {
                return DatatypeMap.check(dt, range);
            }
        } else if (dt.isLiteral()) {
            return false;
        }

        return true;
    }

    void reject(Edge edge) {
        Integer val = count.get(edge.getEdgeNode());
        if (val == null) {
            val = 0;
        }
        count.put(edge.getEdgeNode(), ++val);
    }

    public String display() {
        String str = "";
        for (Node pred : count.keySet()) {
            str += pred + ": " + count.get(pred) + "\n";
        }
        return str;
    }

    @Override
    public void setActivate(boolean b) {
        isActivate = b;
    }

    @Override
    public boolean isActivate() {
        return isActivate;
    }

    @Override
    public void remove() {
        graph.clear(ENTAIL, true);
    }

    @Override
    public int type() {
        return RDFS_ENGINE;
    }
}
