package fr.inria.edelweiss.kgraph.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.api.IGraph;
import fr.inria.edelweiss.kgraph.api.Log;
import fr.inria.edelweiss.kgraph.logic.*;

/**
 * Graph Manager
 * Edges are stored in an index
 * An index is a table: predicate -> List<Edge>	
 * Edge List are sorted 
 * Join on a Node is computed by dichotomy
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Graph //implements IGraph 
{
	private static Logger logger = Logger.getLogger(Graph.class);	
	
	public static final String TOPREL = 
		fr.inria.acacia.corese.triple.cst.RDFS.RootPropertyURI;
	
	public static final int IGRAPH = -1;
	// NB of Index (subject, object, graph)
	public static final int LENGTH = 3;
	
	static final int COPY 	= 0;
	static final int MOVE 	= 1;
	static final int ADD  	= 2;
	static final int CLEAR  = 3;
	
	static long blankid = 0;
	static final String BLANK = "_:b";
	
	/**
	 * Synchronization:
	 * 
	 * several read in // ; only one write
	 * lock read:  Query (QueryProcess)
	 * lock write: Load (Load), Update (QueryProcess), Rule (RuleEngine)
	 * synchronized: Entailment synchronized in read, hence only one entailment can occur
	 * synchronized: indexNode (index of nodes for path)
	 * synchronized: synGetCheck (EdgeIndex) may generate index of nth arg during read
	 * see occurrences of graph.readLock() graph.writeLock()
	 **/
	 
	ReentrantReadWriteLock lock;
	
	ArrayList<Index> tables; 
	// default graph (deprecated)
	Index[] dtables;
	// Table of index 0
	Index table, 
	// for rdf:type, no named graph to speed up type test
	dtable;
	// resource and blank nodes
	Hashtable<String, Entity> individual, blank;
	SortedMap<IDatatype, Entity> literal;
	// graph property nodes
	Hashtable<String, Node> graph, property;
	NodeIndex gindex;
	Log log;
	Entailment inference, proxy;
	EdgeFactory fac;
	private Distance classDistance, propertyDistance;
	// true when graph is modified and need index()
	boolean 
	isUpdate = false, 
	isDelete = false,
	// any delete occurred ?
	isDeletion = false,
	isIndex  = true, 
	// automatic entailment when init()
	isEntail = true,
	isDebug  = !true,
	hasDefault = !true;
	// number of edges
	int size = 0;
	
	// SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...))
	

	
	class TreeNode extends TreeMap<IDatatype, Entity>  {
		
		TreeNode(){
			super(new Compare());
		}
						
	}
	
	class Compare implements Comparator<IDatatype> {
		
		public int compare(IDatatype dt1, IDatatype dt2){
			
			// xsd:integer differ from xsd:decimal 
			// same node for same datatype 
			if (dt1.getDatatypeURI() != null && dt2.getDatatypeURI() != null){
				int cmp = dt1.getDatatypeURI().compareTo(dt2.getDatatypeURI());
				if (cmp != 0){
					return cmp;
				}
			}
									
			int res =  dt1.compareTo(dt2);
			return res;
		}
	}
			
	
	Graph(){
		lock = new ReentrantReadWriteLock();
		
		tables  = new ArrayList<Index>(LENGTH);
		//dtables = new Index[LENGTH];
		for (int i=0; i<LENGTH; i++){
			// One table per node index
			// edges are sorted according to ith Node
			int j = i;
			if (i == LENGTH - 1){
				j = IGRAPH;
			}
			setIndex(i, new EdgeIndex(this, j));	
			//dtables[i] = new EdgeIndex(this, j, true);
		}
		table 		= getIndex(0);
		//dtable 		= dtables[0];
		literal 	= Collections.synchronizedSortedMap(new TreeNode());
		individual 	= new Hashtable<String, Entity>();
		blank 		= new Hashtable<String, Entity>();
		graph 		= new Hashtable<String, Node>();
		property 	= new Hashtable<String, Node>();
		gindex 		= new NodeIndex();	
		fac 		= new EdgeFactory(this);
	}
	
	public static Graph create(){
		return new Graph();
	}
	
	public static Graph create(boolean b){
		Graph g = new Graph();
		if (b) g.setEntailment();
		return g;
	}
	
	public EdgeFactory getEdgeFactory(){
		return fac;
	}
	
	public void setOptimize(boolean b){
		fac.setOptimize(b);
	}
	
	public boolean isLog(){
		return log != null;
	}
	
	public Log getLog(){
		return log;
	}
	
	public void setLog(Log l){
		log = l;
	}
	
	public void log(int type, Object obj){
		if (log != null){
			log.log(type, obj);
		}
	}
	
	public void log(int type, Object obj1, Object obj2){
		if (log != null){
			log.log(type, obj1, obj2);
		}
	}
	
	public Lock readLock(){
		return lock.readLock();
	}
	
	public Lock writeLock(){
		return lock.writeLock();
	}
	
	public ReentrantReadWriteLock getLock(){
		return lock;
	}
	
	
	void clearDistance(){
		setClassDistance(null);
		setPropertyDistance(null);
	}
	
	public Entailment getEntailment(){
		return inference;
	}
	
	public void setEntailment(Entailment i){
		inference = i;
	}
	
	public void setEntailment(){
		Entailment entail = Entailment.create(this);
		setEntailment(entail);
		
		if (size()>0){
			entail.define();
		}
	}
	
	public void set(String property, boolean value){
		localSet(property, value);
		if (inference!=null){
			inference.set(property, value);
		}
	}
	
	void localSet(String property, boolean value){
		if (property.equals(Entailment.DUPLICATE_INFERENCE)){
			for (Index t : tables){
				t.setDuplicateEntailment(value);
			}
		}
	}
	
	public void entail(){
		if (inference!=null){
			inference.entail();
		}
	}
	
	public void entail(List<Entity> list){
		if (isEntail && inference!=null){
			inference.entail(list);
		}
	}
	
	
	public void setDefault(boolean b){
		hasDefault = b;
	}	
	
	public boolean hasDefault(){
		return hasDefault;
	}
	
//	public List<Index> getTables(){
//		return tables;
//	}
	
	public String toString(){
		String str = "";
		str += "Edge:       " 	+ size() + "\n";
		str += "Node:       " 	+ (individual.size() + blank.size() + literal.size()) + "\n";
		str += "Graph:      "	+ graph.size() + "\n";
		str += "Property:   " + table.size() + "\n";
		str += "Literal:    " 	+ literal.size() + "\n";
		str += "Individual: " + individual.size() + "\n";
		str += "Blank: " 	  + blank.size() + "\n";
		str += "Duplicate:  "+ table.duplicate() + "\n";
		return str;
	}
	
	public String toString2(){
		String str = "";
		int uri = 0, blank = 0, string = 0, lit = 0, date = 0, num = 0;
		
		for (Entity e : getNodes()){
			uri++;
		}
		
		for (Entity e : getBlankNodes()){
			blank++;
		}
		
		for (Entity e : getLiteralNodes()){
			IDatatype dt = (IDatatype) e.getNode().getValue();
			if (dt.isNumber()) num++;
			else if (dt.getCode() == IDatatype.STRING) 	string++;
			else if (dt.getCode() == IDatatype.LITERAL) lit++;
			else if (dt.getCode() == IDatatype.DATE) 	date++;

		}
		
		str += "uri: " + uri;
		str += "\nblank: " + blank; 
		
		str += "\nnum: " + num; 
		str += "\nstring: " + string; 
		str += "\nliteral: " + lit; 
		str += "\ndate: " + date; 

		return str;
	}
	

	public Entailment getProxy(){
		if (proxy == null){
			proxy = inference;
			if (proxy == null){
				proxy = Entailment.create(this);
			}
		}
		return proxy;
	}
	
	public boolean isType(Edge edge){
		return getProxy().isType(edge);
	}
	
	public boolean isType(Node pred){
		return getProxy().isType(pred);
	}
	
	public boolean isSubClassOf(Node pred){
		return getProxy().isSubClassOf(pred);
	}
	
	public boolean isSubClassOf(Node node, Node sup){
		return getProxy().isSubClassOf(node, sup);
	}
	
	
	/**************************************************************
	 * 
	 * Consistency Management
	 * 
	 **************************************************************/
	
	/**
	 * send e.g. by kgram eval() before every query execution
	 * restore consistency if updates have been done, perform entailment
	 * when delete is performed, it is the user responsibility
	 * to delete the entailments that depend on it
	 * it can be done using:  drop graph kg:entailment 
	 * Rules are not automatically run, use re.process()
	 */
	
	public synchronized void init(){
		
		if (isIndex){
			index();
		}
		if (isUpdate){
			// use case: previously load or sparql update
			// entailment: 
			// clean meta properties 
			// redefine meta properties
			// perform entailments
			update();
			if (isEntail){
				entail();
			}
		}
	}

	private void update(){
		isUpdate = false;
		// node index
		clearIndex();
		clearDistance();
		
		if (isDelete){
			isDelete = false;
			
			if (inference!=null){
				// use case: a meta triple was deleted previously
				// remove it from entailment tables
				inference.reset();
			}		
		}
	}
	
	public void setUpdate(boolean b){
		isUpdate = b;
	}
	
	private void setDelete(boolean b){
		setUpdate(b);
		isDelete = b;
		isDeletion = true;
	}
	
	/**
	 * @deprecated
	 */
	public boolean isEntailment(){
		return isEntail;
	}
	
	public boolean hasEntailment(){
		return inference!=null;
	}
	
	/**
	 * If true, entailment is done by init() before query processing
	 */
	public void setEntailment(boolean b){
		isEntail = b;
	}
	
	
	// true when index must be sorted 
	boolean isIndex(){
		return isIndex;
	}

	/**
	 * Property Path start a new shortest path
	 * Only with one user (no thread here)
	 * @deprecated
	 */
	public void initPath(){
		for (Entity ent : individual.values()){
			ent.getNode().setProperty(Node.LENGTH, null);
			ent.getNode().setProperty(Node.REGEX, null);
		}
		for (Entity ent : blank.values()){
			ent.getNode().setProperty(Node.LENGTH, null);
			ent.getNode().setProperty(Node.REGEX, null);
		}
		for (Entity ent : literal.values()){
			ent.getNode().setProperty(Node.LENGTH, null);
			ent.getNode().setProperty(Node.REGEX, null);
		}
	}
	
	
	
	/*************************************************************************/
	
	
	
	
	public Index getIndex(){
		return table;
	}
	
	/**
	 * When load is finished,  sort edges
	 */
	public void index(){
		for (Index ei : getIndexList()){
			ei.index();
		}
//		if (hasDefault){
//			for (int i=0; i<tables.size(); i++){
//				dtables[i].index();
//			}
//		}
		isIndex = false;
	}
	
	public void prepare(){
		if (isIndex){
			index();
		}
	}
	
	void clearIndex(){
		gindex.clear();
	}
	
	synchronized void indexNode(){
		if (gindex.size() == 0){
			table.indexNode();
		}
	}
	
	void define(Entity ent){
		gindex.add(ent);
	}
	
	public Iterable<Node> getProperties(){
		return table.getProperties();
	}
	
	public Iterable<Node> getSortedProperties(){
		return table.getSortedProperties();
	}
	
	/**
	 * Pragma: nodes are already nodes of the graph
	 */
	public Entity add(EdgeImpl edge){
		// store edge in index 0
		Entity ent = table.add(edge);
		// tell other index that predicate has instances
		if (ent != null){
			addGraphNode(edge.getGraph());
			addPropertyNode(edge.getEdgeNode());
			
			for (Index ei : getIndexList()){
				if (ei.getIndex() != 0){
					ei.declare(edge);
				}
			}
			
//			if (hasDefault){
//				dtable.add(edge);
//				for (int i=0; i<tables.size(); i++){
//					if (i != 0){
//						dtables[i].declare(edge);
//					}
//				}
//			}
			
			size++;
		}
		return ent;
	}
	
	public boolean exist(EdgeImpl edge){
		return table.exist(edge);
	}
	
	public Entity addEdge(Edge edge){
		if (edge instanceof EdgeImpl){
			return addEdge((EdgeImpl) edge);
		}
		return null;
	}

	
	public Entity addEdge(EdgeImpl edge){
		Entity ent = add(edge);
		if (ent != null){
			setUpdate(true);
			if (inference!=null){
				inference.define(ent.getGraph(), edge);
			}
		}
		return ent;
	}
	


	
	public EdgeImpl create(Node source, Node subject, Node predicate, Node value){
		return fac.create(source, subject, predicate, value);
	}
	
	public EdgeImpl create(Node source, Node predicate, List<Node> list){
		return fac.create(source, predicate, list);
	}
		
	public EdgeImpl create(IDatatype source, IDatatype subject, IDatatype predicate, IDatatype value){
		return null;
	}
	
	public int size(){
		return size;
	}
	
	public int nbIndividuals(){
		return individual.size();
	}
	
	public int nbBlanks(){
		return blank.size();
	}
	
	public int nbLiterals(){
		return literal.size();
	}
	
	void setSize(int n){
		size = n;
	}
	
	IDatatype datatype(Node node){
		return (IDatatype) node.getValue();
	}
	

	
	public Node copy(Node node){
		return getNode(datatype(node), true, false);
	}
	
	
	public Node getTopClass(){
		Node n = getNode(OWL.THING);
		if (n == null){
			n = getNode(RDFS.RESOURCE);
		}
		if (n == null){
			n = createNode(RDFS.RESOURCE);
		}
		return n;
	}
	
	public Node getTopProperty(){
		Node n = getNode(TOPREL);
		if (n == null){
			n = createNode(TOPREL);
		}
		return n;
	}
	
	
	public List<Node> getTopProperties(){
		List<Node> nl = new ArrayList<Node>();
		Node n;
		
//		n = getNode(OWL.TOPOBJECTPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
//		n = getNode(OWL.TOPDATAPROPERTY);
//		if (n != null){
//			nl.add(n);
//		}
		
		if (nl.size() == 0){
			n = getTopProperty();
			nl.add(n);
		}
		
		return nl;
	}
	
		
	// all nodes
	// TODO: check producer
	public Node addNode(IDatatype dt){
		return getNode(dt, true, true);
	}
	
	// used by construct
	public Node getNode(Node gNode, IDatatype dt, boolean create, boolean add){
		if (dt.isBlank()){
			// check that blank is not already in another graph than gNode
			
		}
		return getNode(dt, create, add);
	}
	
	// used by construct
	public Node getNode(IDatatype dt, boolean create, boolean add){
		if (dt.isLiteral()){
			return getLiteralNode(dt, create, add);
		}
		else if (dt.isBlank()){
			return getBlankNode(dt, create, add);
		}
		else {
			return getResourceNode(dt, create, add);
		}
	}
	
	
	public Node getResourceNode(IDatatype dt, boolean create, boolean add){
		Node node = getNode(dt.getLabel());
		if (node != null) return node;
		node = getResource(dt.getLabel());
		if (node == null && create){
			node = createNode(dt);
		}
		if (add) add(node);
		return node;
	}
	
	
	public Node getBlankNode(IDatatype dt, boolean create, boolean add){
		Node node = getBlankNode(dt.getLabel());
		if (node != null) return node;
		if (node == null && create){
			node = createNode(dt);
		}
		if (add) add(node);
		return node;
	}
	
	public Node getLiteralNode(IDatatype dt, boolean create, boolean add){
		Node node = getLiteralNode(dt);
		if (node != null) return node;
		if (create){ 
			node = createNode(dt);
			if (add) addLiteralNode(dt, node);
		}
		return node;
	}
	


	/**
	 * Retrieve Node or create it (but not add it into graph)
	 */
	public Node getCreateResource(String name){
		Node node = getResource(name);
		if (node == null){ 
			node = createNode(name);
		}
		return node;
	}
	
	/**
	 * Retrieve a node/graph node/property node 
	 */
	public Node getResource(String name){
		Node node = getNode(name);
		if (node == null) node = getGraphNode(name);
		if (node == null){
			node = getPropertyNode(name);
		}
		return node;
	}
	

	// resource or blank
	public boolean isIndividual(Node node){
		return individual.containsKey(node.getLabel()) ||
		blank.containsKey(node.getLabel());
	}
	
	// resource node
	public Node getNode(String name){
		return (Node) individual.get(name);
	}
	
	public Node getBlankNode(String name){
		return (Node) blank.get(name);
	}
	
	
	Node basicAddGraph(String name){
		Node node = getGraphNode(name);
		if (node != null) return node;
		node = getCreateResource(name);
		addGraphNode(node);
		return node;
	}

	Node basicAddResource(String label){
		Node node = getNode(label);
		if (node != null) return node;
		node = getCreateResource(label);
		add(node);
		return node;
	}


	Node basicAddProperty(String label){
		Node node = getNode(label);
		if (node != null){
			addPropertyNode(node);
			return node;
		}
		node = getCreateResource(label);
		addPropertyNode(node);
		return node;
	}

	Node basicAddBlank(String label){
		Node node = getBlankNode(label);
		if (node == null){
			IDatatype dt = DatatypeMap.createBlank(label);
			if (dt!=null){
				node = addNode(dt);
			}
		}
		return node;
	}
	
	// resource node
	public void add(Node node){
		IDatatype  dt = datatype(node);
		if (dt.isLiteral()){
			addLiteralNode(dt, node);
		}
		else if (dt.isBlank()){
			blank.put(node.getLabel(), (Entity)node);
		}
		else {
			individual.put(node.getLabel(), (Entity) node);
		}
	}

	public void addLiteralNode(IDatatype dt, Node node){
		literal.put(dt, (Entity) node);
	}
	
	public Node getLiteralNode(IDatatype dt){
		return (Node) literal.get(dt);
	}	
	
	public Node getGraphNode(String label){
		return graph.get(label);
	}
	
	public void addGraphNode(Node gNode){
		if (! isGraphNode(gNode)){
			graph.put(gNode.getLabel(), gNode);
		}
	}
	
	public boolean isGraphNode(Node node){
		return graph.containsKey(node.getLabel());
	}
	
	public Node getPropertyNode(String label){
		return property.get(label);
	}
	
	public void addPropertyNode(Node pNode){
		if (! property.containsKey(pNode.getLabel())){
			property.put(pNode.getLabel(), pNode);
		}
	}
	

	

	
	public Iterable<Entity> getEdges(){
		Iterable<Entity> ie = table.getEdges();
		if (ie == null) return new ArrayList<Entity>();
		return ie;
	}
	
	public Edge getEdge(Node pred, Node node, int index){
		Iterable<Entity> it = getEdges(pred, node, index);
		if (it == null) return null;
 		for (Entity ent : it){
			return ent.getEdge();
		}
		return null;
	}
	
	public Edge getEdge(String name, Node node, int index){
		Node pred = getPropertyNode(name);		
		return getEdge(pred, node, index);
	}

	
	public Edge getEdge(String name, String arg, int index){
		Node pred = getPropertyNode(name);		
		Node node = getNode(arg);
		if (pred==null || node==null) return null;
		Edge edge = getEdge(pred, node, index);
		return edge;
	}
	
	public Iterable<Node> getNodes(Node pred, Node node, int n){
		Iterable<Entity> it = getEdges(pred, node, n);
		if (it == null){
			return new ArrayList<Node>();
		}
		int index = (n == 0) ? 1 : 0;
		return NodeIterator.create(it, index);
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, int n){
		return getEdges(predicate, node, null, n);
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, Node node2, int n){
		if (isTopRelation(predicate)){
			return getEdges(node, n);
		}
		else {
			return basicEdges(predicate, node, node2, n);
		}
	}
	
	public Iterable<Entity> basicEdges(Node predicate, Node node, Node node2, int n){
		return getIndex(n).getEdges(predicate, node, node2);
	}

	
	/**
	 * with rdfs:subPropertyOf
	 */
	public Iterable<Entity> getAllEdges(Node predicate, Node node, Node node2, int n){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		
		for (Node pred : getProperties(predicate)){
			Iterable<Entity> it = getIndex(n).getEdges(pred, node);
			if (it != null){
				meta.next(it);
			}
		}
		if (meta.isEmpty()) return new ArrayList<Entity>();
		return meta;
	}
	
	public Iterable<Node> getProperties(Node p){
		ArrayList<Node> list = new ArrayList<Node>();
		for (Node n : getProperties()){
			if (getEntailment().isSubPropertyOf(n, p)){
				list.add(n);
			}
		}
		return list;
	}
	
	
	public Iterable<Entity> getDefaultEdges(Node predicate, Node node, Node node2, int n){
		if (isTopRelation(predicate)){
			return getEdges(node, n, hasDefault);
		}
		return getIndex(n, hasDefault).getEdges(predicate, node, node2);
	}
	
	public List<Node> getList(Node node){
		List<Node> list = new ArrayList<Node>();
		list(node, list);
		return list;
	}

	/**
	 * node is a list Node
	 * compute the list of elements
	 */
	void list(Node node, List<Node> list){
		if (node.getLabel().equals(RDF.NIL)){
		}
		else {
			Edge first = getEdge(RDF.FIRST, node, 0);
			list.add(first.getNode(1));
			Edge rest  = getEdge(RDF.REST, node, 0);
			list(rest.getNode(1), list);
		}
	}
	
	
	boolean isTopRelation(Node predicate){
		return predicate.getLabel().equals(TOPREL);
	}
	
	// without duplicates 
	public Iterable<Entity> getNodeEdges(Node node){
		return EdgeIterator.create(getEdges(node, 0));
	}
	
	public Iterable<Entity> getNodeEdges(Node gNode, Node node){
		EdgeIterator it = EdgeIterator.create(getEdges(node, 0));
		it.setGraph(gNode);
		return it;
	}
	

	Index getIndex(int n, boolean def){
		if (def){
			return dtables[n];
		}
		return getIndex(n);
	}	
	
	public List<Index> getIndexList(){
		return tables;
	}
	
	// synchronized
	Index getIndex(int n){
		if (n == IGRAPH){
			return tables.get(tables.size()-1);
		}
		if (n+1 >= tables.size() ){
			//setIndex(n, new EdgeIndex(this, n));	
		}
		return tables.get(n);
	}
	
	void setIndex(int n, Index e){
		tables.add(n, e);
	}
	
	public Iterable<Entity> getEdges(Node node, int n){
		return getEdges(node, n, false);
	}
	
	public Iterable<Entity> getEdges(Node node, int n, boolean def){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		
		for (Node pred : table.getProperties()){
			Iterable<Entity> it = getIndex(n, def).getEdges(pred, node);
			if (it != null){
				meta.next(it);
			}
		}
		if (meta.isEmpty()) return new ArrayList<Entity>();
		return meta;
	}
	
	public Iterable<Entity> getEdges(Node predicate){
		Iterable<Entity> it = getEdges(predicate, null, 0);
		if (it == null) it = new ArrayList<Entity>();
		return it;
	}
	
	public int size (Node predicate){
		if (isTopRelation(predicate)) return size();
		Node pred = getPropertyNode(predicate.getLabel());
		if (pred == null) return 0;
		return table.size(pred);
	}
	
	public Iterable<Node> getGraphNodes(){
		return graph.values();
	}
	
	public Iterable<Node> getTypeNodes(){
		return table.getTypes();
	}
	
	public Iterable<Entity> getNodes(){
		return individual.values();
	}
	
	public Iterable<Entity> getBlankNodes(){
		return blank.values();
	}
	
	/**
	 *  resource & blank
	 *  TODO: a node may have been deleted (by a delete triple)
	 *  but still be in the table
	 */
	public Iterable<Entity> getRBNodes(){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		meta.next(getNodes());
		meta.next(getBlankNodes());
		return meta;
	}
		
	public Iterable<Entity> getLiteralNodes(){
		return literal.values();
	}
		
	public Iterable<Entity> getAllNodes(){
		if (isDeletion){
			// recompute existing nodes
			return getAllNodesIndex();
		}
		else {
			// get nodes from tables
			return getAllNodesDirect();
		}
	}
	
	/**
	 * 	TODO: a node may have been deleted (by a delete triple)
	 *  but still be in the table
	 */
	public Iterable<Entity> getAllNodesDirect(){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		meta.next(getNodes());
		meta.next(getBlankNodes());
		meta.next(getLiteralNodes());
		return meta;
	}
	
	/**
	 * Prepare an index of nodes for each graph, enumerate all nodes
	 * TODO: there are duplicates (same node in several graphs) 
	 */
	public Iterable<Entity> getAllNodesIndex(){
		indexNode();
		return gindex.getNodes();
	}
	
	
	public Iterable<Entity> getNodes(Node gNode){
		indexNode();
		return gindex.getNodes(gNode);
	}

	
	
	/**
	 * May infer datatype from property range
	 */
	public Node addLiteral(String pred, String label, String datatype, String lang){
		if (datatype == null && lang == null && 
				inference!=null && inference.isDatatypeInference()){
			String range = inference.getRange(pred);
			if (range != null && range.startsWith(Entailment.XSD)){
				datatype = range;
			}
		}
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
		if (dt == null) return null;
		return addNode(dt);
	}
	

	
	public String newBlankID(){
		return BLANK + blankid++;
	}
	

	

	
	public void deleteGraph(String name){
		Node node = getGraphNode(name);
		if (node != null){
			graph.remove(node);
		}
	}
	
	

	
	Node createNode(IDatatype dt){
		return  NodeImpl.create(dt);
	}
	
	// resource nodes
	public Node createNode(String name){
		IDatatype dt = DatatypeMap.createResource(name);
		if (dt == null) return null;
		return createNode(dt);
	}
	
	
	
	public boolean compare(Graph g){
		return compare(g, false);
	}
	
	public boolean compare(Graph g2, boolean isGraph){
		Graph g1 = this;
		if (isDebug){
			logger.debug(g1.getIndex());
			logger.debug(g2.getIndex());
		}
		if (g1.isIndex()) index();
		if (g2.isIndex()) g2.index();
		if (g1.size()!=g2.size()){
			if (isDebug) logger.debug("** Graph Size: " + size() + " " + g2.size());
			return false;
		}

		for (Node pred1  : g1.getProperties()){
			
			Iterable<Entity> l1 = g1.getEdges(pred1);
			
			Node pred2 = g2.getPropertyNode(pred1.getLabel());
			
			if (pred2 == null){
				if (l1.iterator().hasNext()){
					if (isDebug)  logger.debug("Not found: " + pred1);
					return false;
				}
			}
			else {
				Iterable<Entity> l2 = g2.getEdges(pred2);

				Iterator<Entity> it = l2.iterator();

				for (Entity ent1 : l1){

					if (! it.hasNext()) return false;

					Entity ent2 = it.next();
					if (! compare(ent1, ent2, isGraph)){
						if (isDebug){
							logger.debug(ent1);
							logger.debug(ent2);
						}
						return false;
					}
				}
			}

		}
		return true;
	}

	
	boolean compare(Entity ent1, Entity ent2, boolean isGraph){
		for (int j=0; j<ent1.getEdge().nbNode(); j++){
			if (! ent1.getEdge().getNode(j).same(ent2.getEdge().getNode(j))){
				return false;
			}
		}
		return true;
	}
	
	
	/*****************************************************************
	 * 
	 * Update
	 * 
	 *****************************************************************/
	

	public Entity delete(EdgeImpl edge){
		Entity res = null;
		
		if (edge.getGraph() == null){
			res = deleteAll(edge);
		}
		else {
			res = basicDelete(edge);
		}
		
		if (res != null){
			deleted(res);
		}
		return res;
	}

	
	public Entity delete(EdgeImpl edge, List<String> from){
		Entity res = null;	
		
		for (String str : from){
			Node node = getGraphNode(str);

			if (node != null){
				edge.setGraph(node);
				Entity ent = basicDelete(edge);
				if (res == null){
					if (ent != null) setDelete(true);
					res = ent;
				}
			}
		}
		
		if (res != null){
			deleted(res);
		}
		return res;
	}
	
	
	/**
	 * Does not delete nodes
	 */
	Entity basicDelete(EdgeImpl edge){
		Entity res = null;
		
		for (Index ie : tables){
				Entity ent = ie.delete(edge);
				if (isDebug){
					logger.debug("delete: " + ie.getIndex() + " " + edge);
					logger.debug("delete: " + ie.getIndex() + " " + ent);
				}
				if (ent != null){
					setDelete(true);
					res = ent;
				}
		}
		return res;
	}
	
	
	
	/**
	 * delete occurrences of this edge in all graphs
	 */
	Entity deleteAll(EdgeImpl edge){
		Entity res = null;
		
		for (Node graph : getGraphNodes()){
			edge.setGraph(graph);
			Entity ent = basicDelete(edge);
			if (res == null){
				if (ent != null) setDelete(true);
				res = ent;
			}
		}

		return res;
	}
	
	/**
	 * This edge has been deleted
	 * TODO: Delete its nodes from tables if needed
	 */
	void deleted(Entity ent){
		Edge edge = ent.getEdge();
		for (int i=0; i<edge.nbNode(); i++){
			delete(edge.getNode(i));
		}
	}
	
	void delete(Node node){
		
	}
	
	// clear all except graph names.
	// they must be cleared explicitely
	void clear(){
		clearIndex();
		individual.clear();
		blank.clear();
		literal.clear();
		property.clear();
		for (Index t : tables){
			t.clear();
		}
		if (inference!=null){
			inference.clear();
		}
		clearDistance();
		isIndex = true;
		isUpdate = false; 
		isDelete = false; 
		size = 0;
	}
	
	public boolean clearDefault(){
		clear();
		return true;
	}
	
	public boolean clearNamed(){
		clear();
		return true;
	}
	
	public boolean dropGraphNames(){
		graph.clear();
		return true;
	}
	
	public boolean clear(String uri, boolean isSilent){
		if (uri != null){
			Node gg = getGraphNode(uri);
			if (isDebug) logger.debug("** clear: " + gg);
			if (gg != null){
				setDelete(true);
				getIndex(IGRAPH).clear(gg);
			}
		}
			
		return true;
	}
	
	public boolean update(String source, String target, boolean isSilent, int mode){
		Node g1 = getGraphNode(source);
		Node g2 = getGraphNode(target);
		
		if (g1 == null){
			return false;
		}
		setUpdate(true);

		if (g2 == null){
			g2 = addGraph(target);
		}
		
		switch (mode){
		case ADD:  getIndex(IGRAPH).add(g1, g2); break;
		case MOVE: getIndex(IGRAPH).move(g1, g2); break;
		case COPY: getIndex(IGRAPH).copy(g1, g2); break;

		}
		
		return true;
	}
	
	public boolean add(String source, String target, boolean isSilent){
		return update(source, target, isSilent, ADD);
	}
	
	public boolean move(String source, String target, boolean isSilent){
		return update(source, target, isSilent, MOVE);
	}
	
	public boolean copy(String source, String target, boolean isSilent){
		return update(source, target, isSilent, COPY);
	}

	
	/*********************************************************
	 * 
	 * Distance
	 * 
	 ********************************************************/
	
	
	public void setClassDistance(Distance distance) {
		this.classDistance = distance;
	}
	
	synchronized public Distance setClassDistance(){
		if (classDistance != null){
			return classDistance;
		}
		setClassDistance(Distance.classDistance(this));
		return classDistance;
	}

	public Distance getClassDistance() {
		return classDistance;
	}
	
	public void setPropertyDistance(Distance distance) {
		this.propertyDistance = distance;
	}
	
	synchronized public Distance setPropertyDistance(){
		if (propertyDistance != null){
			return propertyDistance;
		}
		setPropertyDistance(Distance.propertyDistance(this));
		return propertyDistance;
	}

	public Distance getPropertyDistance() {
		return propertyDistance;
	}
	
	
	/****************************************************
	 * 
	 *                  User API
	 *                  
	 *  TODO: 
	 *  no code here, use call to basic methods
	 *  secure addEdge wrt addGraph/addProperty
	 *  api with IDatatype
	 *  
	 * **************************************************/
	
	public Edge addEdge(Node source, Node subject, Node predicate, Node value){
		EdgeImpl e = fac.create(source, subject, predicate, value);
		Entity ee = addEdge(e);
		if (ee != null){
			return ee.getEdge();
		}
		return null;
	}
	
	public Edge addEdge(Node subject, Node predicate, Node value){
		Node g = addGraph(Entailment.DEFAULT);
		return addEdge(g, subject, predicate, value);
	}
	
	// tuple
	public Edge addEdge(Node source, Node predicate, List<Node> list){
		EdgeImpl e = fac.create(source, predicate, list);
		Entity ee = addEdge(e);
		if (ee != null){
			return ee.getEdge();
		}
		return null;
	}
	
	
	/**
	 * Graph in itself is not considered as a graph node for SPARQL path
	 * unless explicitely referenced as a subject or object
	 * Hence ?x :p* ?y does not return graph nodes
	 */
	public Node addGraph(String name){
		return basicAddGraph(name);
	}

	public Node addResource(String label){
		return basicAddResource(label);
	}

	/**
	 * Property in itself is not considered as a graph node for SPARQL path
	 * unless explicitely referenced as a subject or object
	 * Hence ?x :p* ?y does not return property nodes
	 */
	public Node addProperty(String label){
		return basicAddProperty(label);
	}

	public Node addBlank(String label){
		return basicAddBlank(label);
	}


	public Node addBlank(){
		return basicAddBlank(newBlankID());
	}


	public Node addLiteral(String label, String datatype, String lang){
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
		if (dt == null) return null;
		return addNode(dt);
	}
	
	public Node addLiteral(String label, String datatype){
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, null);
		if (dt == null) return null;
		return addNode(dt);
	}
	
	public Node addLiteral(String label){
		return addLiteral(label, null, null);
	}
	
	public Node addLiteral(int n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(long n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(double n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(float n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	public Node addLiteral(boolean n){
		return addNode(DatatypeMap.newInstance(n));
	}
	
	
	
	
	
}
