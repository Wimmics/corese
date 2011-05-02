package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.NodeImpl;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * construct where 
 * describe where
 * delete where
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */


public class Construct 
	implements Comparator<Node>{
	static final String BLANK = "_:b";

	int count = 0;
	
	Query query;
	Graph graph;
	Node defaultGraph;
	List<Entity> list;
	List<String> from;
	
	boolean isDebug = false, isDelete = false, isInsert = false;
	
	Hashtable<Node, Node> table;
	
	Construct(Query q){
		this(q, Entailment.DEFAULT);
	}
	
	Construct(Query q, String src){
		query = q;
		table = new Hashtable<Node, Node>();
		count = 0;
		IDatatype dt;
		dt = DatatypeMap.createResource(src);
		defaultGraph = new NodeImpl(dt);
	}
	
	
	public static Construct create(Query q){
		Construct cons = new Construct(q);
		return cons;
	}
	
	public static Construct create(Query q, String src){
		Construct cons = new Construct(q, src);
		return cons;
	}
	
	void setDelete(boolean b){
		isDelete = b;
	}
	
	void setInsert(boolean b){
		isInsert = b;
	}
	
	void setDebug(boolean b){
		isDebug = b;
	}
	
	
	public void setList(List<Entity> l){
		list = l;
	}
	
	public List<Entity> getList(){
		return list;
	}
	
	public Graph construct(Mappings lMap){
		return construct(lMap, Graph.create());
	}
	
	public Graph delete(Mappings lMap, Graph g, 
			List<String> from, List<String> named){
		setDelete(true);
		this.from = from;
		return construct(lMap, g);
	}
	
	public Graph insert(Mappings lMap, Graph g){
		setInsert(true);
		return construct(lMap, g);
	}
	
	
	/**
	 * Construct graph according to query and mapping 
	 */
	public Graph construct(Mappings lMap, Graph g){
		graph = g;
		init();
		Node gNode = defaultGraph;
		if (isDelete) gNode = null;
		Exp exp = query.getConstruct();
		if (isDelete){
			exp = query.getDelete();
		}
		
		for (Mapping map : lMap){
			// each map has its own blank nodes:
			clear();
			construct(gNode, exp, map);
			
			//System.out.println(getID(map));
		}
		
		//System.out.println(graph);
		graph.index();
		return graph;
	}
	
	
	
	
	
	/**
	 * Recursive construct of exp with map
	 * Able to process construct graph ?g {exp}
	 */
	void construct(Node gNode, Exp exp, Mapping map){
		if (exp.isGraph()){
			gNode = exp.getGraphName();
			exp = exp.rest();
		}
		for (Exp ee : exp.getExpList()){
			if (ee.isEdge()){
				EdgeImpl edge = construct(gNode, ee.getEdge(), map);
				//if (isDebug) System.out.println("** CD: " + edge);

				if (edge != null){
					if (isDelete){
						if (isDebug) System.out.println("** Delete: " + edge);
						if (gNode == null && from!=null && from.size()>0){
							// delete in default graph
							graph.delete(edge, from);
						}
						else {
							// delete in all named graph
							graph.delete(edge);
						}
					}
					else {
						if (isDebug) System.out.println("** Construct: " + edge);
						Entity ent = graph.addEdge(edge);
						if (ent != null && list != null){
							list.add(ent);
						}
					}
				}
			}
			else {
				construct(gNode, ee, map);
			}
		}
	}
	
	
	/**
	 * Clear blank node table
	 */
	void clear(){
		table.clear();
	}
	
	void init(){
		table.clear();
		count = 0;
	}
	
	
	
	/**
	 * Construct target edge from query edge and map
	 * TODO: refactor this wrt Node/Property/Graph Node
	 */
	EdgeImpl construct(Node gNode, Edge edge, Mapping map){
		Node pred = edge.getEdgeVariable();
		if (pred == null){
			pred = edge.getEdgeNode();
		}
		
		Node source   = null;
		if (gNode!=null) source = construct(gNode, map);
		Node property = construct(pred, map);
		
		Node subject = construct(source, edge.getNode(0), map);
		Node object  = construct(source, edge.getNode(1), map);
		
//		if (isDebug){
//			System.out.println(source);
//			System.out.println(subject);
//			System.out.println(property);
//			System.out.println(object);
//		}
		
		if ((source == null && ! isDelete) || subject == null || property == null || object == null){
			return null;
		}
		
		if (isDelete){
			if (gNode == null){
				source = null;
			}
		}
		else {
			graph.add(subject);
			graph.add(object);
			graph.addPropertyNode(property);
			graph.addGraphNode(source);
		}

		EdgeImpl ee =  EdgeImpl.create(source, subject, property, object);
		return ee;
	}
	
	
	/**
	 * Construct target node from query node and map
	 */
	Node construct(Node qNode, Mapping map){
		return construct(null, qNode, map);
	}
		
	Node construct(Node gNode, Node qNode, Mapping map){

		// search target node
		Node node = table.get(qNode);
		
		if (node == null){
			// target node not yet created
			// search map node
			node = map.getNode(qNode);
			IDatatype dt = null;

			if (node == null){
				if (qNode.isBlank()){
					dt = blank(map);
				}
				else if (qNode.isConstant()){
					// constant
					dt = getValue(qNode);
				}
				else {
					// unbound variable
					return null;
				}
			}
			else {
				dt = getValue(node);
			}
			
			node = graph.getNode(gNode, dt, true, false);
			table.put(qNode, node);
		}
		
		return node;
	}
	
	//TODO: check blank wrt target graph
	IDatatype blank(Mapping map){
		IDatatype dt = DatatypeMap.createBlank(blankID());
		return dt;
	}
	
	String blankID(){
		if (isInsert){
			return graph.newBlankID();
		}
		else {
			return BLANK + count++ ;
		}
	}
	
	
	IDatatype getValue(Node node){
		return (IDatatype)node.getValue();
	}

	
	String getID(Mapping map){
		String str = "";
		List<Node> list = new ArrayList<Node>();
		for (Node node : map.getQueryNodes()){
			list.add(node);
		}
		Collections.sort(list, this);
		int n = 0;
		for (Node qNode : list){
			Node node = map.getNode(qNode);
			n++;
			if (node != null && ! qNode.isConstant()){
				str += qNode.getLabel() + "." + getValue(node).toSparql() + ".";
			}
		}
		return str;
	}
	
	public int compare(Node n1, Node n2){
		return n1.compare(n2);
	}


	
}
