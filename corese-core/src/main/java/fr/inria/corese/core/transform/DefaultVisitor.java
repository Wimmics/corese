package fr.inria.corese.core.transform;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.core.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transformer Visitor to be used in a transformation
 * Store and/or log visited nodes, enumerate visited nodes
 * 
 * st:visit(st:start, st:trace)
 * st:visit(st:silent, false)
 * st:visit(st:exp, ?x)
 * bind (st:visited() as ?node)
 * 
 * Olivier Corby, Wimmics INRIA I3S - 2015
 *
 */
public class DefaultVisitor implements TemplateVisitor {
    static final String STL         = NSManager.STL;
    static final String TRACE       = STL + "trace";
    static final String GRAPH       = STL + "graph";
    static final String START       = STL + "start";
    static final String TRANSFORM   = STL + "transform";
    static final String SILENT      = STL + "silent";
    static final String ACCEPT      = STL + "accept";
    static final String DEFAULT     = STL + "default";
    static final String SET         = STL + "set";
    static final String GET         = STL + "get";
   
    Graph graph, visitedGraph;
    IDatatype visitedNode;
    HashMap <String, Boolean> map;
    HashMap <IDatatype, List<IDatatype>> errors;
    private ArrayList<IDatatype> visitedList;
    private HashMap<IDatatype, IDatatype> distinct, value;
    
    private String transform = Transformer.TURTLE;
    private boolean silent = true;
    private String NL = System.getProperty("line.separator");
    // boolean value (if any) that means that visitor must consider visited node
    // use case: st:visit(st:exp, ?x, ?suc)
    // if (?suc = acceptValue) node ?x is considered
    private boolean acceptValue = false;
    // by default accept (or not) all exp such as st:subexp
    private boolean defaultAccept = true;
    boolean isDistinct = true;
    
    
    public DefaultVisitor(){
        map      = new HashMap();
        distinct = new HashMap();
        value    = new HashMap();
        visitedList     = new ArrayList();
        errors   = new HashMap<>();
        initVisitedGraph();
    }
    
    public DefaultVisitor(Graph g) {
        this();
        setGraph(g);
    }

    
    void initVisitedGraph() {
        visitedGraph = Graph.create();
        visitedNode = DatatypeMap.createObject(visitedGraph);
    }
      
    
    @Override
    public void visit(IDatatype name, IDatatype obj, IDatatype arg) {
        if (name.getLabel().equals(START)) {
            initVisitedGraph();
            define(name, obj.getLabel(), arg);
        } else if (name.getLabel().equals(TRACE)) {
            define(name, obj.getLabel(), arg);
        } else {
            process(name, obj, arg);
        }
    }
    
    /**
     * st:visit(st:trace, st:subexp, true)
     */
    void define(IDatatype name, String obj, IDatatype arg){
        if (arg == null){
            return;
        }
        if (obj.equals(GRAPH)){
            addGraph((Graph) arg.getPointerObject());
        }
        else if (obj.equals(TRACE)){
            silent = ! getValue(arg);
        }
        else if (obj.equals(TRANSFORM)){
            setTransform(arg.getLabel());
        }
        else if (obj.equals(SILENT)){
            silent = getValue(arg);
        }
        else if (obj.equals(ACCEPT)){
            // accept node when boolean value is arg
            // e.g. trace node with ?suc = false
            acceptValue = getValue(arg);
        }       
        else if (obj.equals(DEFAULT)){
            // by default accept (or not) all exp such as st:subexp
            defaultAccept = getValue(arg);
        }     
        else { 
            //st:visit(st:trace, st:subexp, true)
            map.put(obj, getValue(arg));
        }
    }
 
    void process(IDatatype name, IDatatype obj, IDatatype arg) {
       if (accept(name) && accept(arg)){
            store(name, obj);
            if (! silent){
                trace(name, obj);
            }
        }
    }
    
    void trace(IDatatype name, IDatatype obj) {
        try {
            Transformer t = Transformer.create(graph, getTransform());
            IDatatype dt = t.process(obj);
            System.out.println(name);
            System.out.println((dt != null) ? dt.getLabel() : obj);
            System.out.println();
        } catch (EngineException ex) {
            Logger.getLogger(DefaultVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    boolean accept(String name){
        Boolean b = map.get(name);
        if (b == null){
            return defaultAccept;
        }
        return b;
    }
    
    boolean accept(IDatatype arg){
        if (arg == null){
            return true;
        }
        boolean b = getValue(arg);
        return b == isAcceptValue();
    }
    
 
    
    void store(IDatatype name, IDatatype obj){
    	storeErrors(name, obj);
        if (isDistinct){
            if (! distinct.containsKey(obj)){
                getVisitedList().add(obj);
                distinct.put(obj, obj);
                //storeGraph(name, obj);
            }
        }
        else {
            getVisitedList().add(obj);
        }
    }
    
    void storeErrors(IDatatype name, IDatatype obj){
        List<IDatatype> list = errors.get(obj);
        if (list == null) {
            list = new ArrayList<>();
            errors.put(obj, list);
        }
        if (! list.contains(name)) {
            list.add(name);
        }
    }
    
    void addGraph(Graph g){
        visitedGraph.copy(g);
    }
    
//    void storeGraph(String name, IDatatype obj){
//        Edge ent = visitedGraph.add(obj, DatatypeMap.newResource(RDF.TYPE), DatatypeMap.newResource(name));
//    }
    
    StringBuilder toSB() {
        StringBuilder sb = new StringBuilder();
        Transformer t = Transformer.create(graph, getTransform());
        for (IDatatype dt : getVisitedList()) {
            IDatatype res;
            try {
                res = t.process(dt);
                if (res != null) {
                    sb.append(res.getLabel());
                    sb.append(NL).append(NL);
                }
            } catch (EngineException ex) {
                Logger.getLogger(DefaultVisitor.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (visitedGraph.size() > 0) {
            sb.append(toStringGraph());
        }
        return sb;
    }
    
    String toStringGraph(){
        Transformer t = Transformer.create(visitedGraph, getTransform());
        return t.toString();
    }
    
    public IDatatype display(){
        return DatatypeMap.newStringBuilder(toSB());     
    }
    
    @Override
    public String toString(){
        return toSB().toString();
    }
    
    @Override
    public Collection<IDatatype> visited(){  
        return getVisitedList();
    }
    
    @Override
    public IDatatype visitedGraphNode(){
        visitedGraph.init();
        return visitedNode;
    }
    
    @Override
    public Graph visitedGraph(){
        visitedGraph.init();
        return visitedGraph;
    }
    
    @Override
    public boolean isVisited(IDatatype dt){
        if (isDistinct){
            return distinct.containsKey(dt);
        }
        return getVisitedList().contains(dt);
    }
     
     boolean getValue(IDatatype arg){
        try {
            return arg.isTrue();
        } catch (CoreseDatatypeException ex) {          
        }
        return false;
     }
     
    @Override
     public void setGraph(Graph g){
         graph = g;
     }

    /**
     * @return the transform
     */
    public String getTransform() {
        return transform;
    }
    
    /**
     * @param transform the transform to set
     */
    public void setTransform(String transform) {
        this.transform = transform;
    }

    /**
     * @return the acceptValue
     */
    public boolean isAcceptValue() {
        return acceptValue;
    }

    /**
     * @param acceptValue the acceptValue to set
     */
    public void setAcceptValue(boolean acceptValue) {
        this.acceptValue = acceptValue;
    }

    /**
     * @return the distinct
     */
    public HashMap<IDatatype, IDatatype> getDistinct() {
        return distinct;
    }

    /**
     * @param distinct the distinct to set
     */
    public void setDistinct(HashMap<IDatatype, IDatatype> distinct) {
        this.distinct = distinct;
    }

    @Override
    public IDatatype set(IDatatype obj, IDatatype prop, IDatatype arg) {
          value.put(obj, arg);
          return arg;
    }
    
    @Override
    public IDatatype get(IDatatype obj, IDatatype prop) {
          return value.get(obj);
    }
    
    /**
     * @return the errors of a node as an array
     */
    @Override
    public Collection<IDatatype> getErrors(IDatatype dt){
    	if (errors.containsKey(dt)){
            return errors.get(dt);
    	}
    	else return new ArrayList<>(0);
    }
    
    @Override
    public IDatatype errors() {
        IDatatype map = DatatypeMap.map();
        for (IDatatype key : errors.keySet()) {
            map.set(key, DatatypeMap.newList(errors.get(key)));
        }
        return map;
    }

    public ArrayList<IDatatype> getVisitedList() {
        return visitedList;
    }

    public void setVisitedList(ArrayList<IDatatype> visitedList) {
        this.visitedList = visitedList;
    }
}
