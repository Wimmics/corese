package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.api.Tagger;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.rdf.*;

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
	
	private static boolean isAllocated = false;
	
	Table table1, table2;
	
	Graph graph;
	
	boolean 
		isOptim = false,
		isTag = false,
		isGraph = false;
	
	int count = 0;
	String key;
		
	class Table extends Hashtable<String, Class<? extends EdgeImpl>> {}
	
	
	EdgeFactory(Graph g){
		graph = g;
		key = hashCode() + ".";
	}
	
	static synchronized boolean isAllocated(){
		return isAllocated;
	}
	
	public void setOptimize(boolean b){
		if (b){
			if (! isAllocated()){
				isAllocated = true;
				isOptim = true;
				init();
			}
		}
		else {
			isOptim = false;
		}
	}	
	
	 boolean hasTag(){
		return graph.hasTag();
	}
	
	// std edge: property is static
	public void define(String name, Class<? extends EdgeImpl> cl){
		if (table1 == null){
			create();
		}
		table1.put(name, cl);
	}
	
	// not with rules
	public void setGraph(boolean b){
		isGraph = b;
	}
	
	// entailed edge: graph and property are static
	public void define(String name, Class<? extends EdgeImpl> cl, boolean entail){
		if (table2 == null){
			create();
		}
		table2.put(name, cl);
	}
	
	
	void create(){
		table1 = new Table();
		table2 = new Table();
	}
	
	void init(){
		create();
		define(RDF.TYPE, 			EdgeType.class);
		define(RDFS.SUBCLASSOF, 	EdgeSubClass.class);
		define(RDFS.LABEL,			EdgeLabel.class);
		define(RDFS.COMMENT, 		EdgeComment.class);	
		
		// entailed
		define(RDF.TYPE, 			EdgeTypeEntail.class, true);
	}
	
	Entailment proxy(){
		return graph.getProxy();
	}
	
	
	public EdgeImpl create(Node source, Node subject, Node predicate, Node value){
		if (isOptim){
			return optimCreate(source, subject, predicate, value);
		}
		else if (hasTag()){
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
		EdgeExtend ee = EdgeExtend.create(source, predicate, list);
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
	
	public EdgeImpl optimCreate(Node source, Node subject, Node predicate, Node value){
		
		EdgeImpl ee;
		
		if (source != null && proxy().isEntailment(source)){
			// Entailment Edge
			
			Class<? extends EdgeImpl> cl = table2.get(predicate.getLabel());

			if (cl != null){
				ee =  create(cl, source, subject, predicate, value);
			}
			else {
				ee = EdgeEntail.create(source, subject, predicate, value);
			}
			return ee;
		}
		else {
			Class<? extends EdgeImpl> cl = getClass(predicate.getLabel());

			if (cl != null){
				ee =  create(cl, source, subject, predicate, value);
				return ee;
			}
		}

		return stdCreate(source, subject, predicate, value);
	}
	
	Class<? extends EdgeImpl> getClass(String name){
		if (isGraph) return EdgeSameGraph.class;
		return table1.get(name);
	}
	
	
	public EdgeImpl stdCreate (Node source, Node subject, Node predicate, Node value){
		EdgeCore edge =  EdgeCore.create(source, subject, predicate, value);
		return edge;
	}
	
	public EdgeImpl extCreate (Node source, Node subject, Node predicate, Node value){
		EdgeImpl ee = new EdgeExtend();
		ee.setGraph(source);
		ee.setEdgeNode(predicate);
		ee.setNode(0, subject);
		ee.setNode(1, value);
		Node date = graph.getNode(DatatypeMap.newDate(), true, true);
		ee.setNode(2, date);
		return ee;
	}
	
	
	public EdgeImpl create(Class<? extends EdgeImpl> cl,
			Node source, Node subject, Node predicate, Node value){
		try {
			EdgeImpl  edge = cl.newInstance();
			edge.setGraph(source);
			edge.setEdgeNode(predicate);
			edge.setNode(0, subject);
			edge.setNode(1, value);
			return edge;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
