package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * Graph Edge as Quad
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public class EdgeQuad extends EdgeTop 
    implements Edge, Entity {
    static int pcount = 0;
    public static boolean displayGraph = true;
    int index = -1;
    protected Node graph, predicate, subject, object;
    private Object prov;

    public EdgeQuad() {
    }


    EdgeQuad(Node g, Node p){
        graph = g;
        predicate = p;
    }
    
    EdgeQuad(Node g, Node pred, Node subject, Node object) {
        this(g, pred);
        this.subject = subject;
        this.object = object;
    }
    
    
    EdgeQuad(Node g, Node pred, Node subject, Node object, Node arg1) {
        this(g, pred, subject, object);
   }
    
     EdgeQuad(Node g, Node p, Node[] args) {
        this(g, p);
    }
     
    public static EdgeQuad create(Node g, Node subject, Node pred, Node object) {
        return new EdgeQuad(g, pred, subject, object);
    }

    public void add(Node node){
        
    }
    
    public EdgeQuad copy() {
        return new EdgeQuad(graph, predicate, subject, object);
    }

 
    public void setTag(Node node) {           
    }

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

