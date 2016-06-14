package fr.inria.edelweiss.kgraph.core.edge;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * Graph Edge for the defaultGraph
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public class EdgeTriple extends EdgeTop 
    implements Edge, Entity {
    public static boolean displayGraph = true;
    protected Node predicate, subject, object;

    public EdgeTriple() {
    }

  
    EdgeTriple(Node pred, Node subject, Node object) {
        this.predicate = pred;
        this.subject = subject;
        this.object = object;
    }
       
    public static EdgeTriple create(Node subject, Node pred, Node object) {
        return new EdgeTriple(pred, subject, object);
    }


    @Override
    public EdgeTriple copy() {
        return create(predicate, subject, object);
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

    public void setEdgeNode(Node node) {
        predicate = node;
    }

    @Override
    public int getIndex() {
        return -1;
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
    public void setIndex(int n) {
        
    }

    @Override
    public Edge getEdge() {
        return this;
    }

    @Override
    public Node getGraph(){
        return ((Graph) subject.getGraphStore()).getDefaultGraphNode();
    }

    @Override
    public void setGraph(Node gNode) {
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
        return null;
    }
    
    @Override    
    public void setProvenance(Object obj) {         
    }
    
}

