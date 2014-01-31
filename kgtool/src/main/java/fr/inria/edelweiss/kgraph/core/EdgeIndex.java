package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
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
public class EdgeIndex extends Hashtable<Node, ArrayList<Entity>> 
implements Index {
	static final int IGRAPH = Graph.IGRAPH;
	private static Logger logger = Logger.getLogger(EdgeIndex.class);	
        static boolean byKey = Graph.valueOut;

	int index = 0, other = 1;
	int count = 0;
	boolean isDebug = !true,
                isUpdate = true,
	isIndexer = false,
	// do not create entailed edge in kg:entailment if it already exist in another graph
	isOptim = false;
	
	Comparator<Entity>  comp, skip;
	Graph graph;
	Hashtable<Node, Node> types;
        List<Node> sortedProperties;

	EdgeIndex(Graph g, int n){
		init(g, n);
		comp  = getComparator();
		skip  = getComparator(true);
		types = new Hashtable<Node, Node>();
	}
	
	
	void init(Graph g, int n){
		graph = g;
		index = n;
		switch (index){
			case 0: other = 1; break;
			case 1: other = 0; break;
			case Graph.IGRAPH: other = 0; break;
		}
	}
	
	
	/**
	 * Compare edges for dichotomy
	 * edges are ordered according to index node
	 * check all arguments for arity n
	 */
	
	Comparator<Entity> getComparator(){
		return getComparator(false);
	}
	
	
	static int nodeCompare(Node n1, Node n2){
            if (byKey){
		return n1.getKey().compareTo(n2.getKey());
            }
            return n1.compare(n2);
	}
	
	static boolean same(Node n1, Node n2){
            if (byKey){
		return n1.getKey().equals(n2.getKey());
            }
            return n1.same(n2);
	}
        
    
	/**
	 * skip = true means:
	 * if arity is different but common arity nodes and graph are equals 
	 * then return equal
	 * This is used to retrieve edge with tag for delete 
	 * in this case the query edge have no tag, it matches all target edge with tag
	 * 
	 */
	Comparator<Entity> getComparator(final boolean skip){
		
		return new Comparator<Entity>(){
									
			public int compare(Entity o1, Entity o2) {
				
				// first check the index node
				int res = nodeCompare(getNode(o1, index), getNode(o2, index));
				
				if (res != 0){
					return res;
				}
				
				int min = Math.min(o1.nbNode(), o2.nbNode());
				
				for (int i=0; i<min; i++){
					// check other common arity nodes
					if (i != index){
						res = nodeCompare(o1.getNode(i), o2.getNode(i));
						if (res != 0){
							return res;
						}
					}
				}
				
				if (o1.nbNode() == o2.nbNode()){
					// same arity, common arity nodes are equal
					// check graph
					return nodeCompare(o1.getGraph(), o2.getGraph());
				}
				
				if (skip){
					// use case: delete
					// skip tag node
					return nodeCompare(o1.getGraph(), o2.getGraph());
				}
				else if (o1.nbNode() < o2.nbNode()){
					// smaller arity edge is before
					return -1;
				}
				else {
					return 1;
				}				
				
			}
		};
	}
        
        Node getNode(Entity ent, int n){
            if (n == IGRAPH){
                return ent.getGraph();
            }
            return ent.getNode(n);
        }
	
	/**
	 * Ordered list of properties
	 * For pprint
         * TODO: optimize it
	 */
	public List<Node> getSortedProperties(){
            if (isUpdate){
                sortProperties();
                isUpdate = false;
            }
            return sortedProperties;
        }
        
        
       synchronized void sortProperties(){
		sortedProperties = new ArrayList<Node>();
		for (Node pred : getProperties()){
			sortedProperties.add(pred);
		}
		Collections.sort(sortedProperties, new Comparator<Node>(){
			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				return o1.compare(o2);
			}
		});
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
	
	public void clear(){
            isUpdate = true;
		if (index == 0){
			logClear();
		}
		super.clear();
	}
        
        
        public void clearCache(){
            for (Node pred : getProperties()){
                List<Entity> l = get(pred);
                if (l != null){
                    l.clear();
                }
            }
        }
	
	/** 
	 * Add a property in the table
	 */
	public Entity add(Entity edge){
            if (index != IGRAPH && edge.nbNode() <= index){
                // use case:  additional node is not present, do not index on this node
                // never happens for subject object and graph
                return null;
            }
		List<Entity> list = define(edge.getEdge().getEdgeNode());
		
		Comparator cc = comp;
		boolean needTag = index == 0 && graph.needTag(edge);
		if (needTag){
			// this edge has no tag yet and it will need onefind
			// let's see if there is the same triple (with another tag)
			// in this case, we skip the insertion
			// otherwise we tag the triple and we insert it 
			cc = skip;
		}
		
		if (isSort(edge)){
			// edges are sorted, check presence by dichotomy
			int i = find(cc, list, edge, 0, list.size());
			int res = 0;
			
			if (i>=list.size()){
				if (isOptim && i>0 && graph.getProxy().isEntailment(edge.getGraph())){
					// eliminate entailed edge if already exist in another graph
					if (same(edge, list.get(i-1))){
						count++;
						return null;
					}
				}
							
				if (needTag){
					tag(edge);
				}
				
				if (onInsert(edge)){
					list.add(edge);
					logInsert(edge);
				}
				else {
					return null;
				}
			}
			else {
				if (index == 0){
					res = cc.compare(edge, list.get(i));
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

				if (needTag){
					tag(edge);
				}
				
				if (onInsert(edge)){
					list.add(i, edge);
					logInsert(edge);
				}
				else {
					return null;
				}
					
			}
		}
		else {
			// edges are not already sorted (load time)
			// add all edges, duplicates will be removed later when first query occurs
			if (onInsert(edge)){
				list.add(edge);
				logInsert(edge);
			}
			else {
				return null;
			}
		}

		//complete(edge);
		return edge;
	}
	
	
	
	Entity tag(Entity ent){
		EdgeImpl ee = (EdgeImpl) ent;
		ee.setTag(graph.tag());
		return ent;
	}
	
        // TBD
	boolean same(Entity e1, Entity e2){
		return e1.getNode(0).same(e2.getNode(0)) &&
			   e1.getNode(1).same(e2.getNode(1));
	}
	
	void complete(Entity ent){
		if (index == 0 && graph.isType(ent.getEdge())){
			types.put(ent.getNode(1), ent.getNode(1));
		}
	}
	
	public Iterable<Node> getTypes(){
		return types.values();
	}

	
	/**
	 * delete/exist
	 */
	List<Entity> getByLabel(Entity e){
		Node pred = graph.getPropertyNode(e.getEdge().getEdgeNode().getLabel());
		if (pred == null) return null;
		List<Entity> list = get(pred);
		return list;
	}
	
	public boolean exist(Entity edge){
            if (index != IGRAPH && edge.nbNode() <= index){
                // use case:  additional node is not present, do not index on this node
                // never happens for subject object and graph
                return false;
            }
		List<Entity> list = getByLabel(edge); 
		if (list==null) return false;
		int i = find(list, edge, 0, list.size());
		
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
		List<Entity> list = define(edge.getEdge().getEdgeNode());
		if (list.size()>0){
			add(edge);
		}
	}
	
	/**
	 * Create and store an empty list if needed
	 */
	private List<Entity> define(Node predicate){
		ArrayList<Entity> list = get(predicate);
		if (list == null){
			list = new ArrayList<Entity>(0);
			put(predicate, list);
		}
		return list;
	}
	
	void set(Node predicate, ArrayList<Entity> list){
		put(predicate, list);
	}

	/**
	 * Sort edges by values of first Node and then by value of second Node
	 */
	public void index(){
		for (Node pred : getProperties()){
			index(pred);
		}
		if (index == 0){
			reduce();
		}
	}
        
        public void index(Node pred){
            List<Entity> list = get(pred);
            Collections.sort(list, comp);
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
	private void reduce(){
		for (Node pred : getProperties()){
			reduce(pred);
		}
	}
        
        private void reduce(Node pred){
            ArrayList<Entity> l1 = get(pred);
			ArrayList<Entity> l2 = reduce(l1);
			put(pred, l2);
			if (l1.size()!=l2.size()){
				graph.setSize(graph.size() - (l1.size()-l2.size()));
			}
        }
	
	private ArrayList<Entity> reduce(List<Entity> list){
		ArrayList<Entity> l = new ArrayList<Entity>();
		Entity pred = null;
		for (Entity ent : list){
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
	
	public int duplicate(){
		return count;
	}
	
	
	/**
	 * Check that this table has been built and initialized (sorted) for this predicate
	 * If not, copy edges from table of index 0 and then sort these edges
	 */
	List<Entity> checkGet(Node pred){
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
	private List<Entity> synCheckGet(Node pred){
		synchronized (pred){
			ArrayList<Entity> list = get(pred);
			if (list != null && list.size()==0){
				List<Entity> std = (List<Entity>) graph.getIndex().getEdges(pred, null);
                                list.ensureCapacity(std.size());
                                if (index < 2){
                                    // we are sure that there are at least 2 nodes
                                   list.addAll(std);
                                }
                                else for (Entity ent : std){
                                    // if additional node is missing: do not index this edge
                                    if (ent.nbNode() > index){
                                        list.add(ent);
                                    }
                                }
				Collections.sort(list, comp);
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
		List<Entity> list = checkGet(pred);
		if (list == null) return 0;
		return list.size();
	}
	

	public Iterable<Entity> getEdges(Node pred, Node node, Node node2){
		List<Entity> list = checkGet(pred); 
		if (list == null || node == null){
			return list;
		}
		// node is bound, enumerate edges where node = edge.getNode(index)
		int n = 0;
		
		if (node2 == null){
			n = find(list, node, 0, list.size());
		}
		else {
			n = find(list, node, node2, 0, list.size());
		}

		if (n>=0 && n<list.size()){
			Node tNode = getNode(list.get(n), index);
			if (same(tNode, node)){
				if (node2 != null){
					Node tNode2 = list.get(n).getNode(other);
					if (! same(tNode2, node2)){
						return null; 
					}
				}			
				
				Iterate it = new Iterate(list, n);
				return it;
			}
		}
		return null; 
	}
	
	void trace(List<Entity> list){
		Node nn = list.get(0).getNode(1);
		for (int i=0; i<list.size(); i++){
			if (! list.get(i).getNode(1).same(nn)){
				nn = list.get(i).getNode(1);
				logger.debug(nn);
			}
		}
	}
	
	
	/**
	 * 
	 * Return an iterator of edges with node as element
	 */
	class Iterate implements Iterable<Entity>, Iterator<Entity> {
		List<Entity> list;
		Node node;
		int ind, start; 
		
		Iterate(List<Entity> l, int n){
			list = l;
			node = getNode(list.get(n), index);
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
			boolean b = ind<list.size() && same(getNode(list.get(ind), index), node);
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

	int find(List<Entity> list, Node node){
		int res = find(list, node, null, 0, list.size());
		if (res>= 0 && res<list.size() && 
			same(getNode(list.get(res), index), node)){
			return res;
		}
		return -1;
	}

	/**
	 * Find the index of node in list of Edge by dichotomy
	 */
	int find(List<Entity> list, Node node, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = compare(list.get(mid), node);
			if (res >= 0) {
				return find(list, node, first, mid);
			}
			else {
				return find(list, node,mid+1, last); 
			}
		}		
	}
	
	int find(List<Entity> list, Node node, Node node2, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = compare(list.get(mid), node, node2);
			if (res >= 0) {
				return find(list, node, node2, first, mid);
			}
			else {
				return find(list, node, node2, mid+1, last); 
			}
		}		
	}
	
	int compare(Entity ent, Node n1, Node n2){
		Node tNode = getNode(ent, index);
		int res = nodeCompare(tNode, n1);
		if (res == 0 && n2 != null){
			res = nodeCompare(ent.getNode(other), n2);
		}
		return res;

	}
	
	int compare(Entity ent, Node n1){
		return nodeCompare(getNode(ent, index), n1);		
	}
	
	int find(List<Entity> list, Entity edge, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = comp.compare(list.get(mid), edge); 
			if (res >= 0) {
				return find(list, edge, first, mid);
			}
			else {
				return find(list, edge, mid+1, last); 
			}
		}		
	}
	
	int find(Comparator<Entity> c, List<Entity> list, Entity edge, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = c.compare(list.get(mid), edge); 
			if (res >= 0) {
				return find(c, list, edge, first, mid);
			}
			else {
				return find(c, list, edge, mid+1, last); 
			}
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
	
	/**
	 * DRAFT:
	 * Comparator used by delete to retrieve an edge to be deleted
	 * If graph manage tag for triple:
	 * if delete query triple has no tag, delete all triples
	 * if delete query triple has a tag, delete only triple with this tag
	 */
	Comparator<Entity> getComp(){
		if (graph.hasTag()){
			return skip;
		}
		else {
			return comp;
		}
	}
	
	public Entity delete(Entity edge){
            if (index != IGRAPH && edge.nbNode() <= index){
                // use case:  additional node is not present, do not index on this node
                // never happens for subject object and graph
                return null;
            }
		List<Entity> list = getByLabel(edge);
		if (list == null) return null;
		
		Comparator<Entity> cmp = getComp();
	
		int i = find(cmp, list, edge, 0, list.size());
		
		if (i >= list.size()){
			return null;
		}
                
                if (graph.hasTag()) {
                    // if edge have no tag:
                    // dichotomy may have jump in the middle of the tag list
                    // find first edge of the list
                    while (i > 0 && cmp.compare(list.get(i - 1), edge) == 0) {
                        i--;
                    }
                }

		Entity ent = delete(cmp, edge, list, i);
				
		logDelete(ent);

		if (graph.hasTag()){
			// delete all occurrences of triple that match edge
			// with different tags
			Entity res = ent;
			while (i < list.size() && res != null){
				res = delete(cmp, edge, list, i);
				logDelete(res);
			}
		}
		return ent;
		
	}
	
	boolean onInsert(Entity ent){
		return graph.onInsert(ent);
	}

	void logDelete(Entity ent){		
		if (ent != null){
                    isUpdate = true;
                    if (getIndex() == 0){
			graph.logDelete(ent);
                    }
		}
	}
	
	void logInsert(Entity ent){
            isUpdate = true;
		if (getIndex() == 0){
			graph.logInsert(ent);
		}
	}
	
	void logClear(){
		for (Node node : getSortedProperties()){
			for (Entity ent : get(node)){
				logDelete(ent);
			}
		}
	}
	
	
	/**
	 * Delete entity at index i, if it is the same as edge
	 * tags may differ according to comparator
	 */
	Entity delete (Comparator<Entity> cmp, Entity edge, List<Entity> list, int i){
		Entity ent = null;
		int res = cmp.compare(list.get(i), edge);

		if (res == 0){
			ent = list.get(i);
			list.remove(i);

			if (getIndex() == 0){
				graph.setSize(graph.size() -1 );
			}
		}
		
		return ent;
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
			
			List<Entity> list = checkGet(pred);
			if (list == null){
				continue ;
			}
			
			if (isDebug){
				for (Entity ee : list) logger.debug("** EI: " + ee);
			}

			int n = find(list, g1);
			if (isDebug) logger.debug("** EI find: " + g1 + " " + (n != -1));
			
			if (n == -1) continue;
			
			update(g1, g2, list, n, mode);			
			
		}
	}
	
	
	private void update(Node g1, Node g2, List<Entity> list, int n, int mode){
		boolean incr = false;
		if (g2!=null && g2.compare(g1) == -1) incr = true;
		
		for (int i = n; i<list.size(); ){

			Entity ent = list.get(i), ee;

			if (getNode(ent, index).same(g1)){
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
                
                if (graph.hasTag() && e.nbNode() == 3){
                    // edge has a tag
                    // copy must have a new tag
                    tag(ee);
                }
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

	public void delete(Node pred) {
		
	}
	
}
