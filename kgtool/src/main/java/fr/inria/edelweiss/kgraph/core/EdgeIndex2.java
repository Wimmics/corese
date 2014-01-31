package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;

/**
 * Table property node -> List<Edge>
 * Sorted by getNode(index), getNode(other)
 * At the beginning, only table of index 0 is fed with edges
 * Other index are built at runtime only if needed 
 * 
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class EdgeIndex2  
implements Index {
	static final int IGRAPH = Graph.IGRAPH;
	private static Logger logger = Logger.getLogger(EdgeIndex2.class);	


	int index = 0, other = 1;
	int count = 0;
	boolean isDebug = !true,
	isIndexer = false,
	// do not create entailed edge in kg:entailment if it already exist in another graph
	isOptim = false;
	
	Comparator<Entity>  comp, comp2;
	Graph graph;
	Hashtable<Node, Node> types;
	Hashtable<Node, Table> table;

    @Override
    public void clearCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
	
	/**
	 * List of properties of one property node
	 *
	 */
	class Table implements Iterable<Entity> {
		Node predicate;
		ArrayList<Entity> list;
		
		Table(){
			list = new ArrayList<Entity>();
		}
		
		Table(Node p){
			this();
			predicate = p;
		}

		public Node getEdgeNode(){
			return predicate;
		}
		
		public Iterator<Entity> iterator() {
			return list.iterator();
		}
		
		int size(){
			return list.size();
		}
		
		void add(Entity e){
			list.add(e);
		}
		
		void add(int i, Entity e){
			list.add(i, e);
		}
		
		 Entity get(int i){
			return list.get(i);
		}
		
		void addAll(Table t){
			list.addAll(t.getList());
		}
		
		ArrayList<Entity> getList(){
			return list;
		}
		
		void sort(Comparator<Entity> c){
			Collections.sort(list, c);
		}
		
		void clear(){
			list.clear();
		}
		
		void remove(int i){
			list.remove(i);
		}
		
		
		
		
		
		
		int find(Node node){
			int res = find(node, null, 0, list.size());
			if (res>= 0 && res<list.size() && 
				list.get(res).getEdge().getNode(index).same(node)){
				return res;
			}
			return -1;
		}

		/**
		 * Find the index of node in list of Edge by dichotomy
		 */
		int find(Node node, int first, int last){
			if (first >= last) {
				return first;
			}
			else {
				int mid = (first + last) / 2;
				int res = compare(list.get(mid), node);
				if (res >= 0) {
					return find(node, first, mid);
				}
				else {
					return find(node,mid+1, last); 
				}
			}		
		}
		
		int find(Node node, Node node2, int first, int last){
			if (first >= last) {
				return first;
			}
			else {
				int mid = (first + last) / 2;
				int res = compare(list.get(mid), node, node2);
				if (res >= 0) {
					return find(node, node2, first, mid);
				}
				else {
					return find(node, node2, mid+1, last); 
				}
			}		
		}
		
		int compare(Entity ent, Node n1, Node n2){
			Node tNode = ent.getNode(index);
			int res = tNode.compare(n1);
			if (res == 0 && n2 != null){
				res = ent.getNode(other).compare(n2);
			}
			return res;

		}
		
		int compare(Entity ent, Node n1){
			return ent.getNode(index).compare(n1);		
		}
		
		int find(Entity edge, int first, int last){
			if (first >= last) {
				return first;
			}
			else {
				int mid = (first + last) / 2;
				int res = comp.compare(list.get(mid), edge); 
				if (res >= 0) {
					return find(edge, first, mid);
				}
				else {
					return find(edge, mid+1, last); 
				}
			}		
		}
		
		
		
		Entity insert(Entity edge){
			int i = find(edge, 0, list.size());
			int res = 0;

			if (i>=list.size()){
				if (isOptim && i>0 && graph.getProxy().isEntailment(edge.getGraph())){
					// eliminate entailed edge if already exist in another graph
					if (same(edge, list.get(i-1))){
						count++;
						return null;
					}
				}
				list.add(edge);
			}
			else {
				if (index == 0){
					res = comp.compare(edge, list.get(i));
					if (res == 0){
						// eliminate duplicate at load time for index 0
						count++;
						return null;
					}
					else if (isOptim && graph.getProxy().isEntailment(edge.getGraph())){
						// eliminate entailed edge if already exist in another graph
						if (i > 0 && same(edge, list.get(i-1))){
							count++;
							return null;
						}
						if (same(edge, list.get(i))){
							count++;
							return null;
						}
					}
				}

				list.add(i, edge);
			}

			return edge;

		}
		
		
		
		Table reduce(){
			Table l = new Table(getEdgeNode());
			Entity pred = null;
			for (Entity ent : this){
				if (pred == null){
					l.add(ent);
				}
				else if (comp.compare(ent, pred) != 0){
					l.add(ent);
				}
				else {
					count++;
				}
				pred = ent;
			}
			return l;
		}
		
		
		public Iterable<Entity> getEdges(Node pred, Node node, Node node2){
			// node is bound, enumerate edges where node = edge.getNode(index)
			int n = 0;
			
			if (node2 == null){
				n = find(node, 0, list.size());
			}
			else {
				n = find(node, node2, 0, list.size());
			}

			if (n>=0 && n<list.size()){
				Node tNode = list.get(n).getNode(index);
				if (tNode.same(node)){
					if (node2 != null){
						Node tNode2 = list.get(n).getNode(other);
						if (! tNode2.same(node2)){
							return null; 
						}
					}			
					
					Iterate it = new Iterate(this, n);
					return it;
				}
			}
			return null; 
		}
		
		
		
		
		
	}
	
	

	EdgeIndex2(Graph g, int n){
		init(g, n);
		comp  = getComparator();
		// skip graph (for entailed edge):
		//comp2 = getComparator2();
		types = new Hashtable<Node, Node>();
		table = new Hashtable<Node, Table>();
	}
	
//	EdgeIndex2(Graph g, int n, boolean skip){
//		init(g, n);
//		comp = getComparator2();
//		isIndexer = true;
//	}
	
	void init(Graph g, int n){
		graph = g;
		index = n;
		switch (index){
			case 0: other = 1; break;
			case 1: other = 0; break;
			case Graph.IGRAPH: other = 0; break;
		}
	}
	
	public int size(){
		return table.size();
	}
	
	public void clear(){
		table.clear();
	}
	
	Table get(Node n){
		return table.get(n);
	}
	
	void put(Node n, Table l){
		table.put(n, l);
	}
	
	Collection<Node> keySet(){
		return table.keySet();
	}
	
	Comparator<Entity> getComparator(){
		
		return new Comparator<Entity>(){
			
			public int compare(Entity o1, Entity o2) {
				int res = o1.getNode(index).compare(o2.getNode(index));
				if (res == 0){
					res = o1.getNode(other).compare(o2.getNode(other));
					if (res == 0 && index == 0){
						if (o1.getGraph() == null || o2.getGraph() == null) res = 0;
						else res = o1.getGraph().compare(o2.getGraph());
					}
				}
				return res;
			}
		};
	}
	
	
	/**
	 * declare equal when N0 N1 are equal but in different graphs
	 * optimize rdf:type test
	 */
	Comparator<Entity> getComparator2(){
		
		return new Comparator<Entity>(){
			
			public int compare(Entity o1, Entity o2) {
				int res = o1.getNode(index).compare(o2.getNode(index));
				if (res == 0){
					res = o1.getNode(other).compare(o2.getNode(other));
				}
				return res;
			}
		};
	}
	
	/**
	 * Ordered list of properties
	 * For pprint
	 */
	public List<Node> getSortedProperties(){
		List<Node> list = new ArrayList<Node>();
		for (Node pred : getProperties()){
			list.add(pred);
		}
		Collections.sort(list, new Comparator<Node>(){
			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				return o1.compare(o2);
			}
		});

		return list;
	}
	
	public Iterable<Node> getProperties(){
		return keySet();
	}
	
	public void setDuplicateEntailment(boolean b){
		isOptim = ! b;
	}
	
	public int getIndex(){
		return index;
	}
	
	public Iterable<Entity> getEdges(){
		MetaIterator<Entity> meta = null;
		for (Node pred : getProperties()){
			Iterable<Entity> it = get(pred);
			if (meta == null) meta = new MetaIterator<Entity>(it);
			else meta.next(it);
		}
		return meta;
	}
	
	public String toString(){
		String str = "Edges:\n";
		int total = 0;
		for (Node pred : getSortedProperties()){
			total += get(pred).size();
			str += pred + ": " + get(pred).size() +"\n";
		}
		str += "Total: " + total + "\n";
		return str;
	}
	
	/** 
	 * Add a property in the table
	 */
	public Entity add(Entity edge){
		Table list = define(edge.getEdge().getEdgeNode());
		
		if (isSort(edge)){
			return list.insert(edge);
		}
		else {
			list.add(edge);
		}

		return edge;
	}
	
	boolean same(Entity e1, Entity e2){
		return e1.getNode(0).same(e2.getNode(0)) &&
			   e1.getNode(1).same(e2.getNode(1));
	}
	
	void complete(Entity ent){
		if (index == 0 && graph.isType(ent.getEdge())){
			types.put(ent.getEdge().getNode(1), ent.getEdge().getNode(1));
		}
	}
	
	public Iterable<Node> getTypes(){
		return types.values();
	}

	
	/**
	 * delete/exist
	 */
	Table getByLabel(Entity e){
		return getByLabel(e.getEdge().getEdgeNode());
	}
	
	Table getByLabel(Node node){
		Node pred = graph.getPropertyNode(node.getLabel());
		if (pred == null) return null;
		Table list = get(pred);
		return list;
	}
	
	
	public boolean exist(Entity edge){
		Table list = getByLabel(edge); 
		if (list==null) return false;
		int i = list.find(edge, 0, list.size());
		
		if (i>=list.size()){
			return false;
		}
		
		int res = comp.compare(edge, list.get(i));
		return (res == 0);
	}
	
	
	boolean isSort(Entity edge){
		return ! graph.isIndex();
	}
	
	/**
	 * Store that the property exist by creating an empty list
	 * It may be fed later if we need a join at getNode(index)
	 * If the list already contains edges, we add it now. 
	 */
	public void declare(Entity edge){
		Table list = define(edge.getEdge().getEdgeNode());
		if (list.size()>0){
			add(edge);
		}
	}
	
	/**
	 * Create and store an empty list if needed
	 */
	private Table define(Node predicate){
		Table list = get(predicate);
		if (list == null){
			list = new Table(predicate);
			put(predicate, list);
		}
		return list;
	}
	
//	void set(Node predicate, Table list){
//		put(predicate, list);
//	}

	/**
	 * Sort edges by values of first Node and then by value of second Node
	 */
	public void index(){
		for (Node pred : getProperties()){
			Table list = get(pred);
			list.sort(comp);
		}
		if (index == 0){
			reduce(! isIndexer);
		}
	}
	
	public void indexNode(){
		for (Node pred : getProperties()){
			for (Entity ent : get(pred)){
				graph.define(ent);
			}
		}
	}
	
	/** 
	 * eliminate duplicate edges
	 */
	private void reduce(boolean bsize){
		for (Node pred : getProperties()){
			Table l1 = get(pred);
			Table l2 = l1.reduce();
			put(pred, l2);
			if (bsize && l1.size()!=l2.size()){
				graph.setSize(graph.size() - (l1.size()-l2.size()));
			}
		}
	}
	
	
	
	public int duplicate(){
		return count;
	}
	
	
	/**
	 * Check that this table has been built and initialized (sorted) for this predicate
	 * If not, copy edges from table of index 0 and then sort these edges
	 */
	Table checkGet(Node pred){
		if (index == 0){
			return get(pred);
		}
		else {
			return synCheckGet(pred);
		}
	}
	
	
	/**
	 * Create the index of property pred on nth argument
	 * It is synchronized on pred hence several synGetCheck can occur in parallel on different pred
	 */
	private Table synCheckGet(Node pred){
		synchronized (pred){
			Table list = get(pred);
			if (list != null && list.size()==0){
				Table std = (Table) graph.getIndex().getEdges(pred, null);
				list.addAll(std);
				list.sort(comp);
			}
			return list;
		}
	}
	
	
	/**
	 * Return iterator of Edge with possibly node as element
	 */
	
	public Iterable<Entity> getEdges(Node pred, Node node){
		return getEdges(pred, node, null);
	}
	
	public int size(Node pred){
		Table list = checkGet(pred);
		if (list == null) return 0;
		return list.size();
	}
	

	public Iterable<Entity> getEdges(Node pred, Node node, Node node2){
		Table list = checkGet(pred); 
		if (list == null || node == null){
			return list;
		}
		return list.getEdges(pred, node, node2);
	}
	
	void trace(Table list){
		Node nn = list.get(0).getEdge().getNode(1);
		for (int i=0; i<list.size(); i++){
			if (! list.get(i).getEdge().getNode(1).same(nn)){
				nn = list.get(i).getEdge().getNode(1);
				logger.debug(nn);
			}
		}
	}
	
	
	/**
	 * 
	 * Return an iterator of edges with node as element
	 */
	class Iterate implements Iterable<Entity>, Iterator<Entity> {
		Table list;
		Node node;
		int ind, start; 
		
		Iterate(Table l, int n){
			list = l;
			node = list.get(n).getNode(index);
			start = n;
		}

		@Override
		public Iterator<Entity> iterator() {
			ind = start;
			return this;
		}


		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			boolean b = ind<list.size() && list.get(ind).getNode(index).same(node);
			return b;
		}

		@Override
		public Entity next() {
			// TODO Auto-generated method stub
			Entity ent = list.get(ind++);
			return ent;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}


	}

	
	
	public void delete(Node pred){
		Table list = getByLabel(pred);
		if (list != null){
			list.clear();
		}
	}
	
/************************************************************************
 * 
 * Update
 * 
 * TODO:
 * remove nodes from individual & literal if needed
 * 
 * TODO:
 * semantics of default graph (kgraph:default vs union of all graphs)
 * 
 */
	
	public Entity delete(Entity edge){
		Table list = getByLabel(edge);
		if (isDebug) logger.debug("EI: " + index + " " + list);
		if (list==null) return null;
		int i = list.find(edge, 0, list.size());
		if (isDebug) logger.debug("EI: " + i);
		if (i>=list.size()){
			return null;
		}
		
		int res = comp.compare(edge, list.get(i));
		if (isDebug) logger.debug("EI: " + res);
		if (res == 0){
			edge = list.get(i);
			list.remove(i);
			if (getIndex() == 0){
				graph.setSize(graph.size() -1 );
			}
			return edge;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * index = IGRAPH
	 */
	public void clear (Node gNode){
		update(gNode, null, Graph.CLEAR);
	}
	
	public void copy (Node g1, Node g2){
		update(g2, null, Graph.CLEAR);
		update(g1, g2, Graph.COPY);
	}
	
	public void move (Node g1, Node g2){
		update(g2, null, Graph.CLEAR);
		update(g1, g2, Graph.MOVE);
	}
	
	public void add (Node g1, Node g2){
		update(g1, g2, Graph.COPY);
	}
	
	
	
	
	
	
	
	
	
	
	
	private void update (Node g1, Node g2, int mode){
		for (Node pred : getProperties()){
			
			Table list = checkGet(pred);
			if (list == null){
				continue ;
			}
			
			if (isDebug){
				for (Entity ee : list) logger.debug("** EI: " + ee);
			}

			int n = list.find(g1);
			if (isDebug) logger.debug("** EI find: " + g1 + " " + (n != -1));
			
			if (n == -1) continue;
			
			update(g1, g2, list, n, mode);			
			
		}
	}
	
	
	private void update(Node g1, Node g2, Table list, int n, int mode){
		boolean incr = false;
		if (g2!=null && g2.compare(g1) == -1) incr = true;
		
		for (int i = n; i<list.size(); ){

			Entity ent = list.get(i), ee;

			if (ent.getEdge().getNode(index).same(g1)){
				if (isDebug) 
					logger.debug("** EI update: " + index + " " + ent);

				switch (mode){

				case Graph.CLEAR:
					clear(ent);
					list.remove(i);
					break;
					
				case Graph.MOVE:
					clear(ent);
					list.remove(i);
					ee = copy(g2, ent);
					if (incr && ee !=null) i++;
					break;

				case Graph.COPY:
					ee = copy(g2, ent);
					// get next ent
					i++;
					// g2 is before g1 hence ent was added before hence incr i again
					if (incr && ee!=null) i++;
					break;
				}
			}
			else {
				break;
			}
		}
	}
	
	/**
	 * TODO:  setUpdate(true)
	 */
	private Entity copy(Node gNode, Entity ent){
		EdgeImpl e = (EdgeImpl) ent;
		EdgeImpl ee = e.copy();
		ee.setGraph(gNode);
		Entity res = graph.add(ee);
		return res;
	}
	
	private void clear(Entity ent){
		for (Index ei : graph.getIndexList()){
			if (ei.getIndex()!= IGRAPH) {
				Entity rem = ei.delete(ent);
				if (isDebug && rem!=null)
					logger.debug("** EI clear: " + ei.getIndex() + " " + rem);
			}
		}
	}
	
}
