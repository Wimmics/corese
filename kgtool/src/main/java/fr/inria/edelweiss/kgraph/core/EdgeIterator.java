package fr.inria.edelweiss.kgraph.core;

import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;

/**
 * 
 * Eliminate successive similar edges (when no graph ?g {})
 * Check from & from named
 * Check graph ?g
 */
public class EdgeIterator implements Iterable<Entity>, Iterator<Entity> {
        static final List<Entity> empty = new ArrayList<Entity>(0);
        
	Iterable<Entity> iter;
	Iterator<Entity> it;
	Edge last;
        EdgeQuad res;
	Node graph;
        Graph gg;
	List<Node> from;
        Node fromNode;
	boolean hasGraph, hasFrom, hasOneFrom;
	private boolean hasTag = false;
        private int level = -1;
        boolean hasLevel = false;
        private boolean isSpeedUp = false;
	
	EdgeIterator(Graph g){
            gg = g;
            init();
	}
	
	// eliminate duplicate edges due to same source
	EdgeIterator(Graph g, Iterable<Entity> i){
            gg = g;
            iter = i;
            hasGraph = false;
            hasFrom = false;
            hasOneFrom = false;
            init();
	}
	
	public static EdgeIterator create(Graph g){
		EdgeIterator ei =   new EdgeIterator(g);
		ei.setTag(g.hasTag());		
		return ei;
	}

	
	public static EdgeIterator create(Graph g, Iterable<Entity> i){
		EdgeIterator ei =  new EdgeIterator(g, i);
		ei.setTag(g.hasTag());
		return ei;
	}
        
        void init(){
            res = EdgeQuad.create(null, null, null, null);
        }

	
	public EdgeIterator(Graph g, Iterable<Entity> i, List<Node> list, boolean hasGraph){
		iter = i;
		from = list;
		this.hasGraph = hasGraph;
		hasFrom = from.size()>0;
                hasOneFrom = from.size() == 1;
                if (hasOneFrom){
                    fromNode = g.getGraphNode(from.get(0).getLabel());
                    if (fromNode == null){
                        iter = empty;
                    }
                }
		setTag(g.hasTag());
                gg = g;
                init();
	}
	
	void setTag(boolean b){
		hasTag = b;
	}
	
	void setGraph(Node g){
		hasGraph = true;
		graph = g;
	}

	@Override
	public Iterator<Entity> iterator() {
		// TODO Auto-generated method stub
		it = iter.iterator();
		last = null;
		return this;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return it.hasNext();
	}
	
	boolean same(Node n1, Node n2){
            return gg.getIndex().same(n1, n2);
	}

	@Override
	public Entity next() {
		
		while (hasNext()){
			Entity ent = it.next();
			boolean ok = true;
			
			if (hasGraph){
				if (graph == null){
					// keep duplicates
					ok = true;
				}
				else {
					// check same graph node
					if (! same(ent.getGraph(), graph)){
						ok = false;
					}
				}
			}
//			else if (isSpeedUp){
//                            // rule engine takes care of duplicates
//                        }                           
                        else if (last != null){
				// eliminate successive duplicates
				if (! same(last.getEdgeNode(), ent.getEdge().getEdgeNode())){
					// different properties: ok
					ok = true;
				}
				else {
					int size = last.nbNode();
					if (size == ent.nbNode()){
						ok = false;
						
						// draft: third argument is a tag, skip it
						if (hasTag  && size == 3){
							size = 2;
						}
						
						for (int i=0; i<size; i++){
							if (! same(last.getNode(i), ent.getNode(i))){
								ok = true;
								break;
							}
						}
					}
				}
			}
			
			
			if (ok && hasFrom){
				ok = isFrom(ent, from);
			}
			
			if (ok){
				last = ent.getEdge();
                               // last = getResult(ent, res);
                                if (hasLevel && last.getIndex() < level){
                                    // use case: Rule Engine requires edges with level >= this.level
                                    it = empty.iterator();
                                    return null;
                                }
                                return ent;
			}
		}
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
        
        // simulate creation of only one edge for this iterator
        Edge getResult(Entity ent, EdgeQuad res) {
            res.setGraph(ent.getGraph());
            res.setEdgeNode(ent.getEdge().getEdgeNode());
            res.setNode(0, ent.getNode(0));
            res.setNode(1, ent.getNode(1));
            return res;
        }
	
	/**
	 * 
	 * Check if entity graph node is member of from by dichotomy
	 */
	boolean isFrom(Entity ent, List<Node> from){
            if (hasOneFrom){
                return same(fromNode, ent.getGraph());
            }
            else {
		Node g = ent.getGraph();
		int res = find(from, g);
		return res != -1;
            }
	}
	
	public boolean isFrom(List<Node> from, Node node){
		int res = find(from, node);
		return res != -1;
	}

	
	int find(List<Node> list, Node node){
		int res = find(list, node,  0, list.size());
		if (res>= 0 && res<list.size() && 
			list.get(res).same(node)){
			return res;
		}
		return -1;
	}

	/**
	 * Find the index of node in list of Node by dichotomy
	 */
	int find(List<Node> list, Node node, int first, int last){
		if (first >= last) {
			return first;
		}
		else {
			int mid = (first + last) / 2;
			int res = list.get(mid).compare(node);
			if (res >= 0) {
				return find(list, node, first, mid);
			}
			else {
				return find(list, node,mid+1, last); 
			}
		}		
	}
	
	boolean isFrom2(Entity ent, List<Node> from){
		Node g = ent.getGraph();
		for (Node node : from){
			if (g.same(node)){
				return true;
			}
		}
		return false;
	}

        // draft unused
    private void provenance(Entity ent) {
        ent.setProvenance(graph(ent));
    }
	
    Graph graph(Entity ent) {
        Graph g = Graph.create();
        g.copy(ent);
        return g;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
        hasLevel = level!= -1;
    }

    /**
     * @return the isSpeedUp
     */
    public boolean isSpeedUp() {
        return isSpeedUp;
    }

    /**
     * @param isSpeedUp the isSpeedUp to set
     */
    public void setSpeedUp(boolean isSpeedUp) {
        this.isSpeedUp = isSpeedUp;
    }
}
