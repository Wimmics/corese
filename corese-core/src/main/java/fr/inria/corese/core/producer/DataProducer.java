package fr.inria.corese.core.producer;

import fr.inria.corese.sparql.api.IDatatype;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.tool.MetaIterator;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphObject;
import fr.inria.corese.core.edge.EdgeTop;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.PRODUCER;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;

/**
 * Transient Dataset over graph in order to iterate edges 
 * Use case: Producer getEdges()
 * default or named graphs, from or from named
 * Default graph: eliminate duplicate edges during iteration
 * May take edge level into account for RuleEngine optimization
 * Example: 
 * graph.getDefault().from(g1).iterate(foaf:knows)
 * graph.getNamed().minus(list(g1 g2)).iterate(us:John, 0)
 * graph.getDefault().iterate().filter(ExprType.GE, 100) -- filter (?object >= 100)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 */
public class DataProducer extends GraphObject implements Iterable<Edge>, Iterator<Edge> {

    static final List<Edge> empty     = new ArrayList<Edge>(0);
    
    Iterable<Edge> iter;
    Iterator<Edge> it;
    EdgeTop glast;
    Edge last;
    Graph graph;
    private DataFilter filter;
    DataFrom from;
    boolean isNamedGraph;

    public DataProducer(Graph g) {
        graph = g;
        isNamedGraph = false;
    }

    public static DataProducer create(Graph g) {
        DataProducer ei = new DataProducer(g);
        return ei;
    }
      
    public DataProducer iterate() {
        return iterate(graph.getTopProperty());
    }

    public DataProducer iterate(Node predicate) {
        setIterable(graph.getEdges(predicate));
        return this;
    }

    public DataProducer iterate(Node predicate, Node node) {
        return iterate(predicate, node, 0);
    }
    
    public DataProducer iterate(Node node, int n) {
        return iterate(graph.getTopProperty(), node, n);
    }

     public DataProducer iterate(Node predicate, Node node, int n) {
        // optimize special cases
        if (isNamedGraph) {
            if (node == null && from != null && !from.isEmpty()) {
                // exploit IGraph Index to focus on from
                setIterable(getEdgesFromNamed(from.getFrom(), predicate));
                return this;
            } 
        } 
        // default graph
        else if (node == null && from != null && !from.isEmpty()) {
            // no query node has a value, there is a from           
            if (! from.isFromOK(from.getFrom())) {
                // from URIs are unknown in current graph
                setIterable( empty);
                return this;
            }
        } 

        // general case
        setIterable(graph.properGetEdges(predicate, node, n));
        return this;
    }
     
    DataProducer empty(){
        setIterable(new ArrayList<Edge>(0));
        return this; 
    } 
     
    /**
     * if arg is bnode and bnode is in target graph, bnode is considered as bnode of target graph
     * if arg is bnode and bnode is not in target graph, it is considered as a joker (a variable) in the triple pattern
     */
    public DataProducer iterate(IDatatype s, IDatatype p, IDatatype o) {
        Node ns = null, np, no = null;
        if (p == null || p.isBlank()){
            np = graph.getTopProperty();
        }
        else {
            np = graph.getPropertyNode(p);
            if (np == null){
                return empty();
            }
        }
        if (s != null){
            ns = graph.getNode(s);
            if (ns == null && ! s.isBlank()){
                return empty();
            }
        }        
        if (o != null){
            no = graph.getNode(o);
            if (no == null && ! o.isBlank()){
                 return empty();
            }
        }
        
        if (ns == null) {
            if (no == null) {
                return iterate(np);
            } else {
                return iterate(np, no, 1);
            }
        } else if (no == null) {
            return iterate(np, ns, 0);
        } else {
            return iterate(np, ns, 0).filter(new DataFilterFactory().filter(ExprType.EQ, o));
        }
    } 
    
    /**
     * Iterate predicate from named
     */
    Iterable<Edge> getEdgesFromNamed(List<Node> from, Node predicate) {
        MetaIterator<Edge> meta = new MetaIterator<Edge>();

        for (Node src : from) {
            Node tfrom = graph.getGraphNode(src.getLabel());
            if (tfrom != null) {
                Iterable<Edge> it = graph.getEdges(predicate, tfrom, Graph.IGRAPH);
                if (it != null) {
                    meta.next(it);
                }
            }
        }

        if (meta.isEmpty()) {
            return empty;
        } else {
            return meta;
        }
    }
   
    
    public void setIterable(Iterable<Edge> it){
        if (iter == null){
            iter = it;
        }
        else if (iter instanceof MetaIterator){
            ((MetaIterator) iter).next(it);
        }
        else {
            MetaIterator m = new MetaIterator<>();
            m.next(iter);
            m.next(it);
            iter = m;
        }
    }
    
    /**
     * RuleEngine require Edge with getIndex() >= n
     * 
     */
    public DataProducer level(int n){
        setFilter(new DataFilter(ExprType.EDGE_LEVEL, n));
        return  this;
    }
    
    public DataProducer named(){
        this.isNamedGraph = true;
        return this;
    }
    
    public DataFrom getCreateDataFrom(){
        if (from == null){
            from = new DataFrom(graph);
            setFilter(from);
        }
        return from;
    }
    
    public DataProducer from(List<Node> list) {  
        if (list != null && ! list.isEmpty()){
            getCreateDataFrom().from(list);
        }
        return this;
    }
    
    public DataProducer from(Node g) {
        if (g != null) {          
            getCreateDataFrom().from(g);
        }
        return this;
    }
    
    
    public DataProducer from(List<Node> list, Node source) {
        if (source == null){
            return from(list);
        }
        return from(source);
    }
    
    /**
     * The from clause is taken as skip from
     * the graphs are skipped to answer query
     */    
    public DataProducer minus(List<Node> list){
        getCreateDataFrom().minus(list);
        return this;
    }
    
    public DataProducer minus(Node node){
        getCreateDataFrom().minus(node);
        return this;
    }
    
    @Override
    public Iterator<Edge> iterator() {
        if (from != null && from.isOneFrom() && from.getFromNode() == null) {
            return empty.iterator();
        }
        it = iter.iterator();
        last = null;
        glast = null;
        return this;
    }
    
    @Override
    public Iterable getLoop() {
        return this;
    }
    
    @Override
    public PointerType pointerType() {
        return PRODUCER;
    }
   
    public IDatatype getList() {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : this) {
            if (edge != null) {
                list.add(DatatypeMap.createObject(graph.getEdgeFactory().copy(edge)));
            }
        }
        return DatatypeMap.newInstance(list);
    }
    
    @Override
    public IDatatype getValue(String var, int n){
        if (n >= 0) {
            int i = 0;
            for (Edge edge : this) {
                if (i++ == n) {
                    if (edge != null) {
                        IDatatype dt = DatatypeMap.createObject(graph.getEdgeFactory().copy(edge));
                        return dt;
                    } 
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex() && n1.same(n2);
    }

    /**
     * Main function iterate Edges.
     */
    @Override
    public Edge next() {

        while (hasNext()) {
            Edge ent = it.next();
            

            if (isNamedGraph) {
                // ok
            } 
            else if (last != null && ! different(last, ent)){
                continue;
            }
            if (filter != null && ! filter.eval(ent)) {
                // filter process from() clause
                if (filter.fail()) {
                    // RuleEngine edge level may fail
                    it = empty.iterator();
                    return null;
                }
                continue;
            }
                    
            record(ent);
            return ent;
        }
        return null;
    }
      
    /**
     * Eliminate successive duplicates
     * 
     **/
    boolean different(Edge last, Edge edge) {
        if (edge.getEdgeNode() == null || !same(last.getEdgeNode(), edge.getEdgeNode())) {
            // different properties: ok
            return true;
        }
        if (graph.isMetadata()) {
            return metadataDifferent(last, edge);
        }
        int size = last.nbNode();
        if (size == edge.nbNode()) {
            for (int i = 0; i < size; i++) {
                if (!same(last.getNode(i), edge.getNode(i))) {
                    return true;
                }
            }
            return false;
        } else {
            // different nb of nodes => different
            return true;
        }
    }
    
    boolean metadataDifferent(Edge last, Edge edge) {
        for (int i = 0; i < 2; i++) {
            if (!same(last.getNode(i), edge.getNode(i))) {
                return true;
            }
        }
        return false;
    }
        
    void record(Edge ent) {
        if (ent.nbNode() == 2){
            record2(ent);
        }
        else {
            last = ent;
        }
    }

    // record a copy of ent for last
    void record2(Edge ent) {
        if (glast == null) {
            glast = graph.getEdgeFactory().createDuplicate(ent);
            last = glast;
        }
        glast.duplicate(ent);
    }

    @Override
    public void remove() {
    }
   
    
    /********************************************************
     * 
     * API to add filters to iterate()
     * Use case:
     * 
     * g.getDefault().iterate(foaf:age).filter(new DataFilterFactory().object(ExprType.GE, 50))   -- object >= 50
     * g.getNamed().from(g1).iterate().filter(new DataFilterFactory().compare(ExprType.EQ, 0, 1)) -- subject = object
     * g.getDefault().iterate().filter(new DataFilterFactory().and().subject(AA).object(BB)) -- and/or are binary
     * g.getDefault().iterate().filter(new DataFilterFactory().not().or().subject(AA).object(BB)) -- not is unary
     * 
     *******************************************************/
        
    public DataProducer filter(DataFilter f){
        setFilter(f);
        return this;
    }
     
    public DataProducer filter(DataFilterFactory f){
        setFilter(f.getFilter());
        return this;
    }

    /**
     * @return the filter
     */
    public DataFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(DataFilter f) {
        if (filter == null){
            filter = f;
        }
        else if (filter.isBoolean()){
           filter.setFilter(f);
        }
        else {
            // use case: filter = from(g1)
            filter = new DataFilterAnd(filter, f);
        }
    }
    
}
