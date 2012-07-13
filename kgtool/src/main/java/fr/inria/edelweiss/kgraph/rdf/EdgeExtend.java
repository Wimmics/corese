package fr.inria.edelweiss.kgraph.rdf;

import java.util.ArrayList;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeCore;

/**
 * Edge class where the  property node is in the class
 * e.g. rdfs:label
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeExtend extends EdgeCore {

	ArrayList<Node> nodes;
	
	public EdgeExtend(){
		nodes = new ArrayList<Node>();
	}
	
	public String toString(){
		String str = "";
		if (displayGraph) str += getGraph() + " " ;
		str +=  getEdgeNode() + " " ;
		for (Node n : nodes){
			str += n + " ";
		}
		return str;
	}
	
	public void setNode(int i , Node node){
		super.setNode(i, node);
		nodes.add(i, node);	
	}
	
	public Node getNode(int n) {
		switch (n) {
		case -1: return getGraph();
		default: return nodes.get(n);
		}
	}
	
	public int nbNode() {
		return nodes.size();
	}
	
}
