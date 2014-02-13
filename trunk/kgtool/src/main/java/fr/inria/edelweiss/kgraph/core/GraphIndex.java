package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;


/**
 * Table property name -> List<Edge>
 * Sorted by getNode(index)
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class GraphIndex 
implements Index, Iterable<Entity> {
	
	
	static final int IGRAPH = Graph.IGRAPH;
	int index = 0, other = 1;
	int size = 0;
	TreeMap<Node, GraphEdgeIndex> tree;
	Comparator<Node>  comp;
	Graph graph;
	
	GraphIndex(Graph g, int n){
		graph = g;
		index = n;
		switch (index){
		case 0: other = 1; break;
		case 1: other = 0; break;
		case Graph.IGRAPH: other = 0; break;
		}
		
		comp = getComparator();
		tree = new TreeMap<Node, GraphEdgeIndex>(comp);
	}
	
	
	Comparator<Node> getComparator(){
		return new Comparator<Node>(){
			public int compare(Node o1, Node o2) {
				int res = o1.compare(o2);								
				return res;
			}
		};
	}
	
	Node getNode(Entity ent, int n){
		if (n == IGRAPH) return ent.getGraph();
		else return ent.getEdge().getNode(n);
	}

	public boolean exist(Entity edge){
		return false;
	}
	
	public Entity delete(Entity edge){
		return edge;
	}
	
	
	public int size() {
		return tree.size();
	}

	
	public int duplicate() {
		int dup = 0;
		for (GraphEdgeIndex gei : tree.values()){
			dup += gei.duplicate();
		}
		return dup;
	}

	
	public void index() {
	}

	
	public Iterable<Node> getProperties() {
		return tree.keySet();
	}

	public Iterable<Node> getSortedProperties() {
		return tree.keySet();
	}
	
	public Entity add(Entity edge) {
		GraphEdgeIndex gei = getIndex(edge);
		Entity ent = gei.add(edge);
		if (ent != null) size++;
		return ent;
	}
	
	
	GraphEdgeIndex getIndex(Entity edge){
		Node pred = edge.getEdge().getEdgeNode();
		GraphEdgeIndex gei = tree.get(pred);
		if (gei == null){
			gei = new GraphEdgeIndex(graph, index);
			tree.put(pred, gei);
		}
		return gei;
	}

	
	public void declare(Entity edge) {
		GraphEdgeIndex gei = getIndex(edge);
		if (gei.size()>0){
			gei.add(edge);
		}
	}
	

	
	public Iterable<Entity> getEdges() {
		return this;
	}
	
	
	public Iterator<Entity> iterator(){

		return new Iterator<Entity>(){

			List<Entity> list = new ArrayList<Entity>();
			Iterator<Entity> ite = list.iterator();
			Iterator<Node> itn = tree.keySet().iterator();
			GraphEdgeIndex gei;
			
			public boolean hasNext() {
				
				if (ite.hasNext()) return true;
				
				while (itn.hasNext()){
					Node node = itn.next();
					gei = tree.get(node);
					ite = gei.iterator();
					if (ite.hasNext()) return true;
				}
				
				return false;
			}

			
			public Entity next() {
				return ite.next();
			}

			
			public void remove() {
				
				
			}
			
		};
	}
	
	
	
	
	
	
	public Iterable<Entity> getEdges(Node pred, Node node) {
		GraphEdgeIndex gei = tree.get(pred);
		if (gei == null) return null;
		if (node == null) return gei.getEdges();
		
		if (index > 0 && gei.size() == 0){
			Index gg = graph.getIndex();
			for (Entity ent : gg.getEdges(pred, null)){
				gei.add(ent);
			}
		}
		
		return gei.getEdges(node);
	}

	
	public Iterable<Entity> getEdges(Node pred, Node node, Node node2) {
		return getEdges(pred, node);
	}
	
	public int getIndex(){
		return index;
	}
	
	/*******************************************************************
	 * 
	 * Update
	 * 
	 */
	
	public void clear (Node gNode){
		
	}


	@Override
	public void copy(Node g1, Node g2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void add(Node g1, Node g2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void move(Node g1, Node g2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void indexNode() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int size(Node pred) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Iterable<Node> getTypes() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void setDuplicateEntailment(boolean value) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void delete(Node pred) {
		// TODO Auto-generated method stub
	}

    @Override
    public void clearCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
