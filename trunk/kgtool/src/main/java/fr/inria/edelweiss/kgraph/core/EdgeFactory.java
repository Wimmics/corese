package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
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
         
         boolean hasTime(){
             return false;
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
                else if (hasTime()){
			return timeCreate(source, subject, predicate, value);
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
		return stdCreate(source, subject, predicate, value);
	}
	
		
	public EdgeImpl stdCreate (Node source, Node subject, Node predicate, Node value){
		EdgeImpl edge =  EdgeImpl.create(source, subject, predicate, value);
		return edge;
	}
	
        // with time stamp
	public EdgeImpl timeCreate (Node source, Node subject, Node predicate, Node value){ 
            Node time = graph.getNode(DatatypeMap.newDate(), true, true);
	    EdgeImpl edge =  new EdgeImpl(source, predicate,  subject,  value, time);
	    return edge;
	}
	
			

}
