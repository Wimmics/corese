package fr.inria.corese.core.edge;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Graph Edge as Quad
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public class EdgeGeneric extends EdgeTop 
    implements Edge {
    static int pcount = 0;
    public static boolean displayGraph = true;
    int index = -1;
    protected Node graph, predicate, subject, object;
    private Object prov;

    public EdgeGeneric() {
    }

    public EdgeGeneric(Node pred) {
        predicate = pred;
    }

    EdgeGeneric(Node g, Node p){
        graph = g;
        predicate = p;
    }
    
    EdgeGeneric(Node g, Node pred, Node subject, Node object) {
        this(g, pred);
        this.subject = subject;
        this.object = object;
    }
    
    
    EdgeGeneric(Node g, Node pred, Node subject, Node object, Node arg1) {
        this(g, pred, subject, object);
    }
    
    EdgeGeneric(Node g, Node p, Node[] args) {
        this(g, p);
    }
     
    public static EdgeGeneric create(Node g, Node subject, Node pred, Node object) {
        return new EdgeGeneric(g, pred, subject, object);
    }
    
    @Override
    public void replicate(Edge cur){
        setNode(0, cur.getNode(0));
        setNode(1, cur.getNode(1));
        setIndex(cur.getIndex());
        setLevel(cur.getLevel());
        setProvenance(cur.getProvenance());
    }
    
    @Override
    public void duplicate(Edge cur){
        setEdgeNode(cur.getEdgeNode());
        setGraph(cur.getGraph());
        replicate(cur);
    }

    public void add(Node node){
        
    }
    
    @Override
    public void setTag(Node node) {           
    }

    @Override
    public String toString() {       
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
        str += getNode(0) + " " + getEdgeNode() + " " + getNode(1);
        return str;
    }
      
    public String toParse(){
		StringBuilder sb = new StringBuilder();
		sb.append("tuple");
		sb.append("(");
		sb.append(getEdgeNode());
		sb.append(subject);
		sb.append(" ");
		sb.append(object);
		sb.append(")");
		return sb.toString();
	}
    

    @Override
    public boolean contains(Node node) {
        // TODO Auto-generated method stub
        return getNode(0).same(node) || getNode(1).same(node);
    }

    @Override
    public Node getEdgeNode() {
        return predicate;
    }

    @Override
    public void setEdgeNode(Node node) {
        predicate = node;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getLabel() {
        return getEdgeNode().getLabel();
    }

    @Override
    public Node getNode(int n) {
       switch(n){
           case Graph.IGRAPH: return getGraph();
           case 0: return subject;
           case 1: return object;
       }
       return null;
    }
    
    public void setNode(int i, Node n){
        switch (i){
            case 0: subject = n; break;
            case 1: object  = n; break;
        }
    }

    @Override
    public int nbNode() {
        return 2;
    }
    
    @Override
    public int nbGraphNode() {
        return 2;
    }

    @Override
    public void setIndex(int n) {
        index = n;
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public Node getGraph() {
        return graph;
    }

    @Override
    public void setGraph(Node gNode) {
        graph = gNode;
    }

    @Override
    public Node getNode() {
        // TODO Auto-generated method stub
        return DatatypeMap.createObject(this.toString(), this);
    }

    @Override
    public Node getEdgeVariable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProvenance() {
        if (prov != null && ! (prov instanceof Node)) {
            prov = DatatypeMap.createObject("p" + pcount++, prov);
        }
        return prov;
    }
    
    @Override    
    public void setProvenance(Object obj) {         
        prov = obj;
    }
}
