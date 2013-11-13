package fr.inria.edelweiss.kgraph.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
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
	private static Logger logger = Logger.getLogger(Construct.class);	

	static final String BLANK = "_:b_";
	static final String DOT   = ".";

	int count = 0, ruleIndex = 0, index = 0;
	String root;
	
	Query query;
	Graph graph;
	Node defaultGraph;
        IDatatype dtDefaultGraph;
	List<Entity> lInsert, lDelete;
	//List<String> from;
	Dataset ds;
	
	boolean isDebug = false, 
	isDelete = false,
	isRule = false,
	isInsert = false;
	
	Object rule;
	
	Hashtable<Node, Node> table;
	
	Construct(Query q){
		this(q, Entailment.DEFAULT);
	}
	
	Construct(Query q, Dataset ds){
		this(q, Entailment.DEFAULT);
		this.ds = ds;
	}
	
	Construct(Query q, String src){
		query = q;
		table = new Hashtable<Node, Node>();
		count = 0;
		dtDefaultGraph = DatatypeMap.createResource(src);
	}
	
	
	public static Construct create(Query q){
		Construct cons = new Construct(q);
                if (q.isDetail()){
                    if (q.isConstruct()){
			cons.setInsertList(new ArrayList<Entity>());
                    }
                    else {
                        cons.setDeleteList(new ArrayList<Entity>());                    
                    }
                }
		return cons;
	}
	
	public static Construct create(Query q, Dataset ds){
		Construct cons = new Construct(q, ds);
		return cons;
	}
	
	public static Construct create(Query q, String src){
		Construct cons = new Construct(q, src);
		return cons;
	}
	
	public void setDelete(boolean b){
		isDelete = b;
	}
	
	public void setInsert(boolean b){
		isInsert = b;
	}
	
	public void setRule(Object r, int n){
		isRule = true;
		rule = r;
		root = BLANK + n + DOT;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	
	public void setInsertList(List<Entity> l){
		lInsert = l;
	}
	
	public List<Entity> getInsertList(){
		return lInsert;
	}
	
	public void setDeleteList(List<Entity> l){
		lDelete = l;
	}
	
	public List<Entity> getDeleteList(){
		return lDelete;
	}
	
	public Graph construct(Mappings lMap){
		return construct(lMap, Graph.create());
	}
	
	public Graph delete(Mappings lMap, Graph g, Dataset ds){
		setDelete(true);
		if (ds!=null && ds.isUpdate()){
			this.ds = ds;
		}
		return construct(lMap, g);
	}
	
	public Graph insert(Mappings lMap, Graph g, Dataset ds){
		setInsert(true);
		if (ds!=null && ds.isUpdate()){
			this.ds = ds;
		}
		return construct(lMap, g);
	}
	
	
	/**
	 * Construct graph according to query and mapping 
	 */
	public Graph construct(Mappings lMap, Graph g){
            	Exp exp = query.getConstruct();
		if (isDelete){
			exp = query.getDelete();
		}
                
                graph = g;
                if (exp.first().isGraph()){
                    // draft: graph kg:system { }
                    // in GraphStore
			Node gNode = exp.first().getGraphName();
                      if (gNode.isConstant() 
                                && graph.getNamedGraph(gNode.getLabel()) != null){                         
                            graph = graph.getNamedGraph(gNode.getLabel());
                       }
                }
                             
                               
		init();
		//Node gNode = defaultGraph;
                Node gNode = graph.getResourceNode(dtDefaultGraph, true, false);

		if (isDelete) gNode = null;
		
		for (Mapping map : lMap){
			// each map has its own blank nodes:
			clear();
			construct(gNode, exp, lMap, map);
			
		}
		
		graph.index();
		return graph;
	}
	
	
	
	
	
	/**
	 * Recursive construct of exp with map
	 * Able to process construct graph ?g {exp}
	 */
	void construct(Node gNode, Exp exp, Mappings lMap, Mapping map){
		if (lInsert != null) lMap.setInsert(lInsert);
		if (lDelete != null) lMap.setDelete(lDelete);
		
		if (exp.isGraph()){
			gNode = exp.getGraphName();
			exp = exp.rest();
		}
                
		for (Exp ee : exp.getExpList()){
			if (ee.isEdge()){
				EdgeImpl edge = construct(gNode, ee.getEdge(), map);

                                if (edge != null){
					if (isDelete){
						if (isDebug) logger.debug("** Delete: " + edge);
						List<Entity> list = null;
						if (gNode == null && ds!=null && ds.hasFrom()){
							// delete in default graph
							list = graph.delete(edge, ds.getFrom());
						}
						else {
							// delete in all named graph
							list = graph.delete(edge);
						}
						if (list != null){
							lMap.setNbDelete(lMap.nbDelete() + list.size());

							if (lDelete != null){
								lDelete.addAll(list);
							}
						}
						
					}
					else {
						if (isDebug) logger.debug("** Construct: " + edge);
						Entity ent = graph.addEdge(edge);
						
						if (ent != null){
							lMap.setNbInsert(lMap.nbInsert() + 1);
							if (lInsert != null){
								lInsert.add(ent);
							}
							
							if (isInsert){
								// When insert in new graph g, update dataset named += g
								if (ds!=null){
									String name = ent.getGraph().getLabel();
									if (! name.equals(Entailment.DEFAULT)){
										ds.addNamed(ent.getGraph().getLabel());
									}
								}
							}
						}
					}
				}
			}
			else {
				construct(gNode, ee, lMap, map);
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
		
		EdgeImpl ee;
		
		if (edge.nbNode() > 2){
			// tuple()
			 ArrayList<Node> list = new ArrayList<Node>();
			 list.add(subject);
			 list.add(object);
			 
			 for (int i=2; i<edge.nbNode(); i++){
				 Node n = construct(source, edge.getNode(i), map);
				 if (n != null){
					 graph.add(n);
					 list.add(n);
				 }
			 }
			 
			 ee =  create(source, property, list);
		}
		else {
			 ee =  create(source, subject, property, object);
		}

		return ee;
	}
	
	
	EdgeImpl create(Node source, Node property, List<Node> list){
		if (isDelete){
			return graph.createDelete(source, property, list);
		}
		else {
			return graph.create(source, property, list);
		}
	}
	
	EdgeImpl create(Node source, Node subject, Node property, Node object){
		if (isDelete){
			return graph.createDelete(source, subject, property, object);
		}
		else {
			return graph.create(source, subject, property, object);
		}
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
			Object value = map.getValue(qNode);
			IDatatype dt = null;

			if (value == null){
				if (qNode.isBlank()){
					dt = blank(qNode, map);
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
				dt = (IDatatype) value;
			}
			
			node = graph.getNode(gNode, dt, true, false);
			table.put(qNode, node);
		}
		
		return node;
	}
	
	IDatatype blank(Node qNode, Mapping map){
		String str;
		if (isRule){
			str = blankRule(qNode, map);
		}
		else {
			str = graph.newBlankID();
		}
		IDatatype dt = graph.createBlank(str);
		return dt;
	}


	
	/**
	 * Create a unique BN ID according to (Rule, qNode & Mapping)
	 * If the rule runs twice on same mapping, it will create same BN
	 * graph will detect it, hence engine will not loop
	 * 
	 */
	String blankRule(Node qNode, Mapping map){
		// _:b + rule ID + "." + qNode ID
		StringBuffer sb = new StringBuffer(root);
		sb.append(getIndex(qNode));
		
		for (Node node : map.getQueryNodes()){
			if (node.isVariable() && ! node.isBlank()){
				// node value ID
				sb.append(DOT);
				sb.append(map.getNode(node).getIndex());
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Generate an index for construct Node
	 */
	int getIndex(Node qNode){
		int n = qNode.getIndex();
		if (n == -1){
			n = index++;
			qNode.setIndex(n);
		}
		return n;
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
			Object value = map.getValue(qNode);
			n++;
			if (value != null && ! qNode.isConstant()){
                            IDatatype dt = (IDatatype) value;
                            str += qNode.getLabel() + "." + dt.toSparql() + ".";
			}
		}
		return str;
	}
	
	public int compare(Node n1, Node n2){
		return n1.compare(n2);
	}


	
}
