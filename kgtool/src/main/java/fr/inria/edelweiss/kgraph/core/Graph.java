package fr.inria.edelweiss.kgraph.core;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.Entailment;

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
public class Graph {
	private static Logger logger = Logger.getLogger(Graph.class);	
	
	public static final String TOPREL = RDFS.RootPropertyURI;
	public static final int IGRAPH = 2;
	public static final int MAX = 3;
	
	static final int COPY = 0;
	static final int MOVE = 1;
	static final int ADD  = 2;
	static final int CLEAR  = 3;
	
	static long blankid = 0;
	static final String BLANK = "_:b";
	
	Index[] tables;
	// Table of index 0
	Index table;
	// resource and blank nodes
	Hashtable<String, Entity> individual, literal;
	// graph property nodes
	Hashtable<String, Node> graph, property;
	NodeIndex gindex;
	Entailment inference, proxy;
	private Distance distance;
	// true when graph is modified and need index()
	boolean 
	isUpdate = false, 
	isDelete = false, 
	isIndex  = false, 
	// automatic entailment when init()
	isEntail = true,
	isDebug  = !true;
	// number of edges
	int size = 0;
			
	
	Graph(){
		tables = new Index[MAX];
		for (int i=0; i<MAX; i++){
			// One table per node index
			// edges are sorted according to ith Node
			tables[i] = new EdgeIndex(this, i);
			//tables[i] = new GraphIndex(this, i);

		}
		table = tables[0];
		individual 	= new Hashtable<String, Entity>();
		literal 	= new Hashtable<String, Entity>();
		graph 		= new Hashtable<String, Node>();
		property 	= new Hashtable<String, Node>();
		gindex 		= new NodeIndex();
	}
	
	public static Graph create(){
		return new Graph();
	}
	
	public static Graph create(boolean b){
		Graph g = new Graph();
		if (b) g.setEntailment();
		return g;
	}
	
	public void setUpdate(boolean b){
		isUpdate = b;
		if (distance!=null) distance.setUpdate(b);
	}
	
	void setDelete(boolean b){
		setUpdate(b);
		isDelete = b;
	}
	
	public Entailment getInference(){
		return inference;
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
		if (inference!=null){
			inference.set(property, value);
		}
	}
	
	/**
	 * If true, entailment is done by init() before query processing
	 */
	public void setEntailment(boolean b){
		isEntail = b;
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
	
	public Index[] getTables(){
		return tables;
	}
	
	public String toString(){
		String str = "";
		str += "Edge: " + size() + "\n";
		str += "Node: " + (individual.size() + literal.size()) + "\n";
		str += "Property: " + table.size() + "\n";
		str += "Duplicate: " + table.duplicate() + "\n";
		str += "Individual: " + (individual.size()) + "\n";
		str += "Literal: " + (literal.size());
		return str;
	}
	
	// true when index is sorted 
	boolean isIndex(){
		return isIndex;
	}
	
	public boolean isUpdate(){
		return isUpdate;
	}
	
	public boolean isEntail(){
		return isEntail;
	}
	
	void setProxy(){
		proxy = inference;
		if (proxy == null){
			proxy = Entailment.create(this);
		}
	}
	
	public boolean isType(Edge edge){
		if (proxy == null){
			setProxy();
		}

		return proxy.isType(edge);
	}
	
	public boolean isType(Node pred){
		if (proxy == null){
			setProxy();
		}

		return proxy.isType(pred);
	}
	
	public boolean isSubClassOf(Node pred){
		if (proxy == null){
			setProxy();
		}

		return proxy.isSubClassOf(pred);
	}
	
	public void setIndex(boolean b){
		isIndex = b;
	}
	
	/**
	 * send e.g. by kgram eval() before every query execution
	 * restore consistency if updates have been done
	 */
	
	public void init(){
		
		if (! isIndex){
			isIndex = true;
			index();
		}
		if (isUpdate){
			update();
			if (isEntail) entail();
		}
	}
	
	
	/**
	 * An edge has been added or deleted
	 * Restore consistency wrt indexes and entailment
	 * PRAGMA:
	 * when delete is performed, it is the user responsibility
	 * to delete the entailments that depend on it
	 * 
	 * it can be done using: 
	 * delete where {graph kg:default {?x ?p ?y}}
	 * but all that was inserted in default graph is also removed ...
	 */
	void update(){
		isUpdate = false;
		// node index
		clearIndex();
		if (isDelete){
			delete();
		}
	}
	
	/**
	 * use case: a meta triple was deleted previously
	 * remove it from entailment tables
	 */
	void delete(){
		isDelete = false;
		if (inference!=null){
			inference.reset();
		}
	}
	
	
	public Index getIndex(){
		return table;
	}
	
	/**
	 * When load is finished,  sort edges
	 */
	public void index(){
		for (int i=0; i<MAX; i++){
			tables[i].index();
		}
		isIndex = true;
	}
	
	public void prepare(){
		if (! isIndex){
			index();
		}
	}
	
	void clearIndex(){
		gindex.clear();
	}
	
	void indexNode(){
		table.indexNode();
	}
	
	void define(Entity ent){
		gindex.add(ent);
	}
	
	public Iterable<Node> getProperties(){
		return table.getProperties();
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
			for (int i=1; i<MAX; i++){
				tables[i].declare(edge);
			}
			size++;
		}
		return ent;
	}
	
	public boolean exist(EdgeImpl edge){
		return table.exist(edge);
	}
	
	public Entity addEdge(EdgeImpl edge){
		Entity ent = add(edge);
		if (ent != null){
			setUpdate(true);
			if (inference!=null){
				inference.process(ent.getGraph(), edge);
			}
		}
		return ent;
	}
	
	public EdgeCore create(Node source, Node subject, Node predicate, Node value){
		return EdgeCore.create(source, subject, predicate, value);
	}

	
	public int size(){
		return size;
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
		else {
			Node node = getNode(dt.getLabel());
			if (node != null) return node;
			node = getResource(dt.getLabel());
			if (node == null && create){
				node = createNode(dt);
			}
			if (add) add(node);
			return node;
		}
	}
	
	public Node getLiteralNode(IDatatype dt, boolean create, boolean add){
		String name = dt.toSparql();
		Node node = getLiteralNode(name);
		if (node != null) return node;
		if (create){ 
			node = createNode(dt);
			if (add) addLiteralNode(name, node);
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
	

	
	public boolean isIndividual(Node node){
		return individual.containsKey(node.getLabel());
	}
	
	// resource node
	public Node getNode(String name){
		return (Node) individual.get(name);
	}
	

	
	// resource node
	public void add(Node node){
		if (datatype(node).isLiteral()){
			addLiteralNode(node.getLabel(), node);
		}
		else {
			individual.put(node.getLabel(), (Entity) node);
		}
	}
	
	// resource node
	public Node getLiteralNode(String name){
		return (Node) literal.get(name);
	}
	
	public void addLiteralNode(String name, Node node){
		literal.put(name, (Entity) node);
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
		if (! property.containsKey(pNode.getLabel()))
			property.put(pNode.getLabel(), pNode);
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
	
	public Edge getEdge(String name, String arg, int index){
		Node pred = getPropertyNode(name);		
		Node node = getNode(arg);
		if (pred==null || node==null) return null;
		Edge edge = getEdge(pred, node, 0);
		return edge;
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, int n){
		return getEdges(predicate, node, null, n);
	}
	
	public Iterable<Node> getNodes(Node pred, Node node, int n){
		Iterable<Entity> it = getEdges(pred, node, n);
		if (it == null){
			return new ArrayList<Node>();
		}
		int index = (n == 0) ? 1 : 0;
		return NodeIterator.create(it, index);
	}
	
	public Iterable<Entity> getEdges(Node predicate, Node node, Node node2, int n){
		if (isTopRelation(predicate)){
			return getEdges(node, n);
		}
		else {
			return tables[n].getEdges(predicate, node, node2);
		}
	}
	
	boolean isTopRelation(Node predicate){
		return predicate.getLabel().equals(TOPREL);
	}
	
	// without duplicates 
	public Iterable<Entity> getNodeEdges(Node node){
		return EdgeIterator.create(getEdges(node, 0));
	}
	
	public Iterable<Entity> getEdges(Node node, int n){
		MetaIterator<Entity> meta = new MetaIterator<Entity>();
		
		for (Node pred : table.getProperties()){
			Iterable<Entity> it = tables[n].getEdges(pred, node);
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
		if (isTopRelation(predicate)) return graph.size();
		Node pred = getPropertyNode(predicate.getLabel());
		if (pred == null) return 0;
		return table.size(pred);
	}
	
	public Iterable<Node> getGraphNodes(){
		return graph.values();
	}
	
	public Iterable<Entity> getNodes(){
		return individual.values();
	}
	
	public Iterable<Entity> getLiteralNodes(){
		return literal.values();
	}
	
	public Iterable<Entity> getAllNodes(){
		if (gindex.size() == 0){
			indexNode();
		}
		return gindex.getNodes();
	}
	
	public Iterable<Entity> getNodes(Node gNode){
		if (gindex.size() == 0){
			indexNode();
		}
		return gindex.getNodes(gNode);
	}

	public Node addLiteral(String label, String datatype, String lang){
		IDatatype dt = DatatypeMap.createLiteral(label, datatype, lang);
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
	
	public Node addBlank(String label){
		Node node = getNode(label);
		if (node == null){
			IDatatype dt = DatatypeMap.createBlank(label);
			if (dt!=null){
				node = addNode(dt);
			}
		}
		return node;
	}
	
	public Node addBlank(){
		return addBlank(newBlankID());
	}

	
	public String newBlankID(){
		return BLANK + blankid++;
	}
	
	public Node addProperty(String label){
		Node node = getNode(label);
		if (node != null){
			addPropertyNode(node);
			return node;
		}
		node = getCreateResource(label);
		addPropertyNode(node);
		return node;
	}
	
	// graph nodes
	public Node addGraph(String name){
		Node node = getGraphNode(name);
		if (node != null) return node;
		node = getCreateResource(name);
		addGraphNode(node);
		return node;
	}
	
	public void deleteGraph(String name){
		Node node = getGraphNode(name);
		if (node != null){
			graph.remove(node);
		}
	}
	
	
	public Node addResource(String label){
		Node node = getNode(label);
		if (node != null) return node;
		node = getCreateResource(label);
		add(node);
		return node;
	}
	
	Node createNode(IDatatype dt){
		return new NodeImpl(dt);
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
		if (! g1.isIndex()) index();
		if (! g2.isIndex()) g2.index();
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
	
	
	/*********************************************************************
	 * 
	 * Update
	 * 
	 */
	
	
	public Entity delete(EdgeImpl edge){
		if (edge.getGraph() == null){
			return deleteAll(edge);
		}
		
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
	
	public Entity delete(EdgeImpl edge, List<String> from){
		Entity res = null;	
		for (String str : from){
			Node node = getGraphNode(str);

			if (node != null){
				edge.setGraph(node);
				Entity ent = delete(edge);
				if (res == null){
					if (ent != null) setDelete(true);
					res = ent;
				}
			}
		}
		return res;
	}
	
	
	Entity deleteAll(EdgeImpl edge){
		Entity res = null;
		for (Node graph : getGraphNodes()){
			edge.setGraph(graph);
			Entity ent = delete(edge);
			if (res == null){
				if (ent != null) setDelete(true);
				res = ent;
			}
		}
		return res;
	}
	
	
	
	public boolean clear(String uri, boolean isSilent){
		if (uri != null){
			Node gg = getGraphNode(uri);
			if (isDebug) logger.debug("** clear: " + gg);
			if (gg != null){
				setDelete(true);
				tables[IGRAPH].clear(gg);
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
		case ADD:  tables[IGRAPH].add(g1, g2); break;
		case MOVE: tables[IGRAPH].move(g1, g2); break;
		case COPY: tables[IGRAPH].copy(g1, g2); break;

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

	void setDistance(Distance distance) {
		this.distance = distance;
	}
	
	public Distance setDistance(){
		if (distance != null){
			return distance;
		}
		setDistance(Distance.create(this));
		return distance;
	}

	public Distance getDistance() {
		return distance;
	}
}
