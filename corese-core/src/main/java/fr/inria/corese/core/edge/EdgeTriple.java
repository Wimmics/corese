package fr.inria.corese.core.edge;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;

/**
 * Graph Edge for the defaultGraph
 *
 * @author Olivier Corby, Wimmics, INRIA I3S, 2014
 *
 */
public class EdgeTriple extends EdgeBinary  {
    protected Node predicate;

    public EdgeTriple() {
    }

  
    public EdgeTriple(Node pred, Node subject, Node object) {
        this.predicate = pred;
        this.subject = subject;
        this.object = object;
    }
       
    public static EdgeTriple create(Node source, Node subject, Node pred, Node object) {
        return new EdgeTriple(pred, subject, object);
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
    public Node getEdgeNode() {
        return predicate;
    }

    @Override
    public void setEdgeNode(Node node) {
        predicate = node;
    }

    @Override
    public Node getGraph(){
        return subject.getTripleStore().getNode(Graph.DEFAULT_INDEX);
    }

    @Override
    public void setGraph(Node gNode) {
    }
}

