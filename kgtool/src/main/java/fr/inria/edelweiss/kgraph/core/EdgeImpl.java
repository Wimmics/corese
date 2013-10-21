package fr.inria.edelweiss.kgraph.core;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.Arrays;
import java.util.List;

/**
 * Graph Edge with n nodes (not only triple)
 *
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeImpl implements Edge, Entity {

    public static boolean displayGraph = true;
    protected Node graph, predicate;
    Node[] nodes;

    public EdgeImpl() {
    }


    EdgeImpl(Node g, Node p){
        graph = g;
        predicate = p;
    }
    
    EdgeImpl(Node g, Node pred, Node subject, Node object) {
        this(g, pred);
        nodes = new Node[2];
        nodes[0] = subject;
        nodes[1] = object;
    }
    
     EdgeImpl(Node g, Node p, Node[] args) {
        this(g, p);
        nodes = args;
    }

    public static EdgeImpl create(Node g, Node subject, Node pred, Node object) {
        return new EdgeImpl(g, pred, subject, object);
    }

    public static EdgeImpl create(Node g, Node pred, List<Node> list) {
        Node[] nodes = new Node[list.size()];
        list.toArray(nodes);
        EdgeImpl e = new EdgeImpl(g, pred, nodes);
        return e;
    }
    
    public static EdgeImpl create(Node g, Node pred, Node[] nodes) {
        return new EdgeImpl(g, pred, nodes);
    }


    public EdgeImpl copy() {
        return new EdgeImpl(getGraph(), getEdgeNode(), Arrays.copyOf(getNodes(), nbNode()));
    }
    
    public void setNodes(Node[] args){
        nodes = args;
    }
    
    public Node[] getNodes(){
        return nodes;
    }

    public void setNode(int i, Node node) {
        nodes[i] = node;       
    }
    
     public void setTag(Node node) {
         if (nodes.length <= Graph.TAGINDEX){
             Node[] args = Arrays.copyOf(nodes, Graph.TAGINDEX+1);
             nodes = args;
         }
        nodes[Graph.TAGINDEX] = node;       
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
		for (Node n : nodes){
			sb.append(n);
			sb.append(" ");
		}
		sb.append(")");
		return sb.toString();
	}
    

    @Override
    public boolean contains(Node node) {
        // TODO Auto-generated method stub
        for (Node n : nodes) {
            if (n.same(node)) {
                return true;
            }
        }
        return false;
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
        return 0;
    }

    @Override
    public String getLabel() {
        return getEdgeNode().getLabel();
    }

    @Override
    public Node getNode(int n) {
        return nodes[n];
    }

    @Override
    public int nbNode() {
        return nodes.length;
    }

    @Override
    public void setIndex(int n) {
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
}
