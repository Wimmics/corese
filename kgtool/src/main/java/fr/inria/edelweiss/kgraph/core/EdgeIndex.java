package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import java.util.HashMap;

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
@Deprecated
public class EdgeIndex //extends HashMap<Node, ArrayList<Entity>> 
implements Index {
    private static final String NL = System.getProperty("line.separator");
	static final int IGRAPH = Graph.IGRAPH;
	static final int ILIST  = Graph.ILIST;
	private static Logger logger = Logger.getLogger(EdgeIndex.class);	
        static boolean byKey = Graph.valueOut;
        private boolean byIndex = true;
        
	int index = 0, other = 1;
	int count = 0;
        int scoIndex = -1, typeIndex = -1;
	boolean isDebug = !true,
                isUpdate = true,
	isIndexer = false,
	// do not create entailed edge in kg:entailment if it already exist in another graph
	isOptim = false;
	
	Comparator<Entity>  comp, skipTag, skipGraph, compList;
	Graph graph;
        List<Node> sortedProperties;
        HashMap<Node, ArrayList<Entity>> table ;

	EdgeIndex(Graph g, boolean bi, int n){
		init(g, bi, n);
		comp        = getComparator(n);
		skipTag     = getComparator(true, true);
		skipGraph   = getComparator(false, false);
                table = new HashMap();
	}
	
	
	void init(Graph g, boolean bi, int n){
		graph = g;
		index = n;
                byIndex = bi;
		switch (index){
			case 0:  other = 1; break;
			default: other = 0; break;
		}
	}
        
	public int size(){
            return table.size();
        }
        
        void put(Node n, ArrayList<Entity> l){
            table.put(n, l);
        }
        
        ArrayList<Entity> get(Node n){
            return table.get(n);
        }
	
	/**
	 * Compare edges for dichotomy
	 * edges are ordered according to index node
	 * check all arguments for arity n
	 */
	
	Comparator<Entity> getComparator(int n){
            switch (n){
                case ILIST: return getListComparator();
            }
            return getComparator(false, true);
	}
	
	
     int nodeCompare(Node n1, Node n2){
            if (isByIndex()){
                return compare(n1.getIndex(), n2.getIndex());
            }
            if (byKey){
		return n1.getKey().compareTo(n2.getKey());
            }

            return n1.compare(n2);
	}
        
      int compare(int n1, int n2){
            if (n1 < n2){
                return -1;
            }
            else if (n1 == n2){
                return 0;               
            }
            else {
                return +1;
            }
        }
	
        public boolean same(Node n1, Node n2){
            if (isByIndex()){
                return n1.getIndex() == n2.getIndex();
            }
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
	Comparator<Entity> getComparator(final boolean skipTag, final boolean duplicate){
		            
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
                                    if (! duplicate){
                                        return 0;
                                    }
                                    return  nodeCompare(o1.getGraph(), o2.getGraph());
				}
				
				if (skipTag){
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
        
        
        // sort in reverse order of edge index
        Comparator<Entity> getListComparator(){
		
		return new Comparator<Entity>(){
									
			public int compare(Entity o1, Entity o2) {
                            int i1 = o1.getEdge().getIndex();
                            int i2 = o2.getEdge().getIndex();
                            if (i1 > i2){
                                return -1;
                            }
                            else if (i1 == i2){
                                return 0;
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
		return table.keySet();
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
		return toRDF();
	}
        
       public String toRDF() {
           Serializer sb = new Serializer();
            sb.open("kg:Index");
            sb.appendPNL("kg:index ", index);
            int total = 0;
            for (Node pred : getSortedProperties()) {
                int i = get(pred).size();
                if (i > 0){
                    total += i;               
                    sb.append("kg:item [ ");
                    sb.appendP("rdf:predicate ", pred);
                    sb.append("rdf:value ", i);
                    sb.appendNL("] ;");
                }
            }
            sb.appendNL("kg:total ", total);
            sb.close();
            return sb.toString();
        }
         
       public int cardinality() {
            int total = 0;
            for (Node pred : getProperties()) {
                total += get(pred).size();
            }
            return total;
       }
               
        /**
         * Clean the content of the Index but keep the properties
         * Hence Index can be reused
         */
        public void clean(){
            for (Node p : getProperties()){
                List l = get(p);
                l.clear();
            }
        }
	
	public void clear(){
            isUpdate = true;
		if (index == 0){
			logClear();
		}
		table.clear();
	}
        
       public void clearIndex(Node pred) {
            List<Entity> l = get(pred);
            if (l != null && l.size() > 0) {
                l.clear();
            }
        }

        
        public void clearCache(){
            for (Node pred : getProperties()){
               clearIndex(pred);
            }
        }
	
	/** 
	 * Add a property in the table
	 */
        public Entity add(Entity edge){
            return add(edge, false);
        }
        
        /**
         * noGraph == true => if edge already exists in another graph, do not add edge        
         */
    public Entity add(Entity edge, boolean duplicate) {
        if (index != IGRAPH && edge.nbNode() <= index) {
            // use case:  additional node is not present, do not index on this node
            // never happens for subject object and graph
            return null;
        }

        List<Entity> list = define(edge.getEdge().getEdgeNode());

        Comparator cc = comp;

        boolean needTag = index == 0 && graph.needTag(edge);
        if (needTag) {
            // this edge has no tag yet and it will need onefind
            // let's see if there is the same triple (with another tag)
            // in this case, we skip the insertion
            // otherwise we tag the triple and we insert it 
            cc = skipTag;
        } else if (!duplicate) {
            cc = skipGraph;
        }

        if (isSort(edge)) {
            // edges are sorted, check presence by dichotomy
            int i = find(cc, list, edge, 0, list.size());
            int res = 0;

            if (i >= list.size()) {
                i = list.size();
            } 
            else {
                if (index == 0) {
                    res = cc.compare(edge, list.get(i));
                    if (res == 0) {
                        // eliminate duplicate at load time for index 0
                        count++;
                        return null;
                    }
                }
            }

            if (needTag) {
                tag(edge);
            }

            if (onInsert(edge)) {
                list.add(i, edge);
                logInsert(edge);
            } 
            else {
                return null;
            }


        } 
        else {
            // edges are not already sorted (load time)
            // add all edges, duplicates will be removed later when first query occurs
            if (onInsert(edge)) {
                list.add(edge);
                logInsert(edge);
            } else {
                return null;
            }
        }

        //complete(edge);
        return edge;
    }
        
        /**
         * PRAGMA:
         * All Edge in list have p as predicate
         * Use case: Rule Engine
         */
        public void add(Node p, List<Entity> list){
            ArrayList<Entity> l = define(p);
            if (index == 0 || l.size() > 0){
                l.ensureCapacity(l.size() + list.size());
                l.addAll(list);
            }
            if (index == 0){
                isUpdate = true;
            }
        }
        
	Entity tag(Entity ent){
            graph.tag(ent);
            return ent;
	}
	
        // TBD
	boolean same(Entity e1, Entity e2){
		return e1.getNode(0).same(e2.getNode(0)) &&
		       e1.getNode(1).same(e2.getNode(1));
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
            declare(edge, true);
        }
                       
	public void declare(Entity edge, boolean duplicate){
		List<Entity> list = define(edge.getEdge().getEdgeNode());
		if (list.size()>0){
			add(edge, duplicate);
		}
	}
        
        
	
	/**
	 * Create and store an empty list if needed
	 */
	private ArrayList<Entity> define(Node predicate){
		ArrayList<Entity> list = get(predicate);
		if (list == null){
			list = new ArrayList<Entity>(0);
			put(predicate, list);
		}
		return list;
	}

	/**
	 * Sort edges by values of first Node and then by value of second Node
	 */
	public void index(){
            index(true);
        }
        
        public void index(boolean reduce){
		for (Node pred : getProperties()){
			index(pred);
		}
		if (reduce && index == 0){
			reduce();
		}
	}
        
        public void index(Node pred, boolean reduce){
            index(pred);
            if (reduce){
                reduce(pred);
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
        
        private void reduce(Node pred) {
            ArrayList<Entity> l1 = get(pred);
            ArrayList<Entity> l2 = reduce(l1);
            put(pred, l2);
            if (l1.size() != l2.size()) {
                graph.setSize(graph.size() - (l1.size() - l2.size()));
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
				index(pred);
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
		int n = find(pred, list, node, node2);

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
        
        int find(Node p, List<Entity> list, Node n1, Node n2) {
            if (n2 == null) {
                if (isByIndex()){                    
                    return find(list, n1.getIndex(), 0, list.size());
                }
                return find(list, n1, 0, list.size());
            } else {
                if (isByIndex()){
                   return find(list, n1.getIndex(), n2.getIndex(), 0, list.size());
                }
                return find(list, n1, n2, 0, list.size());
            }
        }
        
      
        /**
         * Written for index = 0
         */
       public boolean exist(Node pred, Node n1, Node n2) {
            List<Entity> list = checkGet(pred);
            if (list == null) {
                return false;
            }
            int n;
            if (isByIndex()){
                n = find(list, n1.getIndex(), n2.getIndex(), 0, list.size());
            }
            else {
                n = find(list, n1, n2, 0, list.size());
            }
            if (n>=0 && n<list.size()){
                Entity ent = list.get(n);
                if (same(n1, ent.getNode(0)) && same(n2, ent.getNode(1))){
                    return true;
                }
            }
            return false;
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
     * @return the byIndex
     */
    public boolean isByIndex() {
        return byIndex;
    }

    /**
     * @param byIndex the byIndex to set
     */
    public void setByIndex(boolean byIndex) {
        this.byIndex = byIndex;
        index(false);
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
			if (compare(list.get(mid), node) >= 0) {
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
			if (compare(list.get(mid), node, node2) >= 0) {
				return find(list, node, node2, first, mid);
			}
			else {
				return find(list, node, node2, mid+1, last); 
			}
		}		
	}
        
        // by index
        int find(List<Entity> list, int n1, int n2, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			if (compare(list.get(mid), n1, n2) >= 0) {
				return find(list, n1, n2, first, mid);
			}
			else {
				return find(list, n1, n2, mid+1, last); 
			}
		}		
	}
        
        int find(List<Entity> list, int n1, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = compare(getNode(list.get(mid), index).getIndex(), n1);
			if (res >= 0) {
				return find(list, n1, first, mid);
			}
			else {
				return find(list, n1, mid+1, last); 
			}
		}		
	}
                          	
	int compare(Entity ent, Node n1, Node n2){
		int res = nodeCompare(getNode(ent, index), n1);
		if (res == 0 && n2 != null){
			res = nodeCompare(ent.getNode(other), n2);
		}
		return res;

	}
        
        int compare(Entity ent, int n1, int n2){
		int res = compare(getNode(ent, index).getIndex(), n1);
		if (res == 0){
			res = compare(ent.getNode(other).getIndex(), n2);
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
			return skipTag;
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
                boolean isBefore = false;
		if (g2!=null && nodeCompare(g1, g2) < 0){
                    isBefore = true;
                }
		for (int i = n; i<list.size(); ){
			Entity ent = list.get(i), ee;
			if (same(getNode(ent, index), g1)){
				
				switch (mode){

				case Graph.CLEAR:
					clear(ent);
					list.remove(i);
					break;
					
				case Graph.MOVE:
					clear(ent);
					list.remove(i);
					ee = copy(g2, ent);
                                        if (isBefore){}
                                        else if (ee != null){
                                            i++;
                                        }
					break;

				case Graph.COPY:
					ee = copy(g2, ent);
					// get next ent
					i++;
					// g2 is before g1 hence ent was added before hence incr i again
                                        if (isBefore){}
                                        else if (ee != null){
                                            i++;
                                        }
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
		return graph.copy(gNode, ent);
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
