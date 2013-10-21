package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * Create specific Edge for specific property
 * Property Node is static 
 * rdf:type -> EdgeType
 * 
 * Graph Node is static:
 * Entailed edge -> EdgeEntail
 * Entailed type edge -> EdgeTypeEntail
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class EdgeFactory {
		
	
	Graph graph;
	
	boolean 
		isOptim = false,
		isTag = false,
		isGraph = false;
	
	int count = 0;
	String key;
			
	
	EdgeFactory(Graph g){
		graph = g;
		key = hashCode() + ".";
	}
	
	
	 boolean hasTag(){
		return graph.hasTag();
	}
		
	
	// not with rules
	public void setGraph(boolean b){
		isGraph = b;
	}
	
	

	Entailment proxy(){
		return graph.getProxy();
	}
	
	
	public EdgeImpl create(Node source, Node subject, Node predicate, Node value){
		if (hasTag()){
			return tagCreate(source, subject, predicate, value);
		}
		else {
			return stdCreate(source, subject, predicate, value);
		}
	}
	
	public EdgeImpl createDelete(Node source, Node subject, Node predicate, Node value){
		return stdCreate(source, subject, predicate, value);
	}

	
	
	/**
	 * Tuple
	 */
	public EdgeImpl createDelete(Node source, Node predicate, List<Node> list){
		return create(source, predicate, list);
	}

	public EdgeImpl create(Node source, Node predicate, List<Node> list){
		//EdgeExtend ee = EdgeExtend.create(source, predicate, list);
		EdgeImpl ee = EdgeImpl.create(source, predicate, list);
		return ee;
	}
	
	/**
	 * Generate a unique tag for each triple
	 */
	Node tag(){
		return graph.tag();
	}
	
	/**
	 * Draft: prepare add a unique tag to each edge
         * The tag is added by EdgeIndex.add(edge)              
         * 
	 */
	public EdgeImpl tagCreate(Node source, Node subject, Node predicate, Node value){
		ArrayList<Node> list = new ArrayList<Node>(3);
		list.add(subject);
		list.add(value);
		return create(source, predicate, list);
	}
	
		
	public EdgeImpl stdCreate (Node source, Node subject, Node predicate, Node value){
		//EdgeCore edge =  EdgeCore.create(source, subject, predicate, value);
		EdgeImpl edge =  EdgeImpl.create(source, subject, predicate, value);
		return edge;
	}
	
	
			

}
