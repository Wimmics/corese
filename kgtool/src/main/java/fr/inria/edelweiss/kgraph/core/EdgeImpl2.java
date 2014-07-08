package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.List;

/**
 * Graph Edge with n nodes (not only triple)
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeImpl2 implements Edge, Entity {

    public static boolean displayGraph = true;
    int index = -1;
    protected Node graph, predicate, subject, object;

    public EdgeImpl2() {
    }


    EdgeImpl2(Node g, Node p){
        graph = g;
        predicate = p;
    }
    
    EdgeImpl2(Node g, Node pred, Node subject, Node object) {
        this(g, pred);
        this.subject = subject;
        this.object = object;
    }
    
    EdgeImpl2(Node g, Node pred, Node subject, Node object, Node arg1) {
        this(g, pred, subject, object);
   }
    
     EdgeImpl2(Node g, Node p, Node[] args) {
        this(g, p);
    }

    public static EdgeImpl2 create(Node g, Node subject, Node pred, Node object) {
        return new EdgeImpl2(g, pred, subject, object);
    }

    public static EdgeImpl2 create(Node g, Node pred, List<Node> list) {
        Node[] nodes = new Node[list.size()];
        list.toArray(nodes);
        EdgeImpl2 e = new EdgeImpl2(g, pred, nodes);
        return e;
    }
    
    public static EdgeImpl2 create(Node g, Node pred, Node[] nodes) {
        return new EdgeImpl2(g, pred, nodes);
    }

    public void add(Node node){
        
    }
    
    public EdgeImpl2 copy() {
        return new EdgeImpl2(getGraph(), getEdgeNode(), subject, object);
    }
    
    public void setNodes(Node[] args){
        
    }
    
    public Node[] getNodes(){
        return null;    
    }

    public void setNode(int i, Node node) {
             
    }
    
     public void setTag(Node node) {           
    }

    public String toString() {
        if (nbNode()>2){
            return tuple();
        }
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
        str += getNode(0) + " " + getEdgeNode() + " " + getNode(1);
        return str;
    }
    
    public String tuple() {
        String str = "";
        if (displayGraph) {
            str += getGraph() + " ";
        }
       str += toParse();
       
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
        return subject.same(node) || object.same(node);
    }

    @Override
    public Node getEdgeNode() {
        return predicate;
    }

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
           case 0: return subject;
           case 1: return object;
       }
       return null;
    }

    @Override
    public int nbNode() {
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

    public void setGraph(Node gNode) {
        graph = gNode;
    }

    @Override
    public Node getNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node getEdgeVariable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProvenance() {              
        return null;    
    }
    
    /**
     * Draft 
     */
    public void setProvenance(Object obj) {
        if (!(obj instanceof Node)) {
            Node prov = DatatypeMap.createObject("provenance");
            prov.setObject(obj);
            obj = prov;
        }
        add((Node) obj);
    }
}
