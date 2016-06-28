package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Processor;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.core.edge.EdgeGeneric;
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
 */
public class DataProducer implements Iterable<Entity>, Iterator<Entity> {

    static final List<Entity> empty     = new ArrayList<Entity>(0);
    static final List<Node> emptyNode   = new ArrayList<Node>(0);
    
    Iterable<Entity> iter;
    Iterator<Entity> it;
    EdgeGeneric glast;
    Edge last;
    Graph graph;
    DataFilter filter;
    DataFrom from;
    boolean isNamedGraph;
    private int level;

    DataProducer(Graph g) {
        graph = g;
        isNamedGraph = false;
        level = -1;
    }

    public static DataProducer create(Graph g) {
        DataProducer ei = new DataProducer(g);
        return ei;
    }

    @Deprecated
    public static DataProducer create(Graph g, Iterable<Entity> i) {
        DataProducer ei = new DataProducer(g);
        ei.setIterable(i);
        return ei;
    }
    
    @Deprecated
     public DataProducer(Graph g, List<Node> list) {
        this(g);
        from(list);
    }

    @Deprecated 
    public DataProducer(Graph g, Iterable<Entity> i, List<Node> list) {
        this(g, list);
        setIterable(i);
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
    
    

    /**
     * Iterate predicate from named
     */
    Iterable<Entity> getEdgesFromNamed(List<Node> from, Node predicate) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();

        for (Node src : from) {
            Node tfrom = graph.getGraphNode(src.getLabel());
            if (tfrom != null) {
                Iterable<Entity> it = graph.getEdges(predicate, tfrom, Graph.IGRAPH);
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
   
    
    public void setIterable(Iterable<Entity> it){
        iter = it;
    }
    
    public DataProducer level(int n){
        setLevel(n);
        return  this;
    }
    
    public DataProducer named(){
        this.isNamedGraph = true;
        return this;
    }
    
    public DataFrom getCreateDataFrom(){
        if (from == null){
            from = new DataFrom(graph);
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
    
    public DataProducer not(){
        if (filter != null){
            filter.not();
        }
        return this;
    }
    
    @Override
    public Iterator<Entity> iterator() {
        if (from != null && from.isOneFrom() && from.getFromNode() == null) {
            return empty.iterator();
        }
        it = iter.iterator();
        last = null;
        glast = null;
        return this;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex();
    }

    @Override
    public Entity next() {

        while (hasNext()) {
            Entity ent = it.next();
            

            if (isNamedGraph) {
                // ok
            } 
            else if (last != null && ! different(last, ent.getEdge())){
                continue;
            }

            if (from != null && ! from.eval(ent)){
                continue;
            }
            
            if (filter != null&& ! filter.eval(ent)){
                continue;
            }
            
            if (level != -1 && ent.getEdge().getIndex() < level) {
                    // use case: Rule Engine requires edges with level >= this.level
                 it = empty.iterator();
                 break;
            }

            record(ent);
            return ent;
        }
        return null;
    }
      
    // eliminate successive duplicates
    boolean different(Edge last, Edge edge) {       
        if (edge.getEdgeNode() == null || ! same(last.getEdgeNode(), edge.getEdgeNode())) {
            // different properties: ok
            return true;
        } else {
            int size = last.nbNode();
            if (size == edge.nbNode()) {               
                for (int i = 0; i < size; i++) {
                    if (!same(last.getNode(i), edge.getNode(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
        
    void record(Entity ent) {
        if (EdgeIndexer.test){
            record2(ent);
        }
        else {
            last = ent.getEdge();
        }
    }

    // record a copy of ent for last
    void record2(Entity ent) {
        if (glast == null) {
            glast = new EdgeGeneric();
            last = glast;
        }
        glast.duplicate(ent);
    }

    @Override
    public void remove() {
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
    
    
    /********************************************************
     * 
     * 
     *******************************************************/
    
    
    public DataProducer filter(int test){
        filter = new DataFilter(test);
        return this;
    }
    
    public DataProducer filter(int test, IDatatype dt){
        filter = new DataFilter(test, dt);
        return this;
    }
    
    public DataProducer filter(int test, IDatatype dt, int index){
        filter = new DataFilter(test, dt, index);
        return this;
    }
       
    public DataProducer filter(int test, int value){
         return filter(test, DatatypeMap.newInstance(value));
    }
    
    public DataProducer filter(int test, double value){
         return filter(test, DatatypeMap.newInstance(value));
    }

    public DataProducer filter(int test, String value){
         return filter(test, DatatypeMap.newInstance(value));
    }
    
    
    
    
    public DataProducer filter(String test){
        filter = new DataFilter(oper(test));
        return this;
    }
    
    public DataProducer filter(String test, IDatatype dt){
        filter = new DataFilter(oper(test), dt);
        return this;
    }
    
    public DataProducer filter(String test, IDatatype dt, int index){
        filter = new DataFilter(oper(test), dt, index);
        return this;
    }
    
    public DataProducer filter(String test, int value){
         return filter(oper(test), DatatypeMap.newInstance(value));
    }
    
    public DataProducer filter(String test, double value){
         return filter(oper(test), DatatypeMap.newInstance(value));
    }

    public DataProducer filter(String test, String value){
         return filter(oper(test), DatatypeMap.newInstance(value));
    }
    
    int oper(String str){
        return Processor.getOper(str);
    }
    

}
