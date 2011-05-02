package fr.inria.edelweiss.kgraph.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

class GraphEdgeIndex 
implements Iterable<Entity> 
{
	
	static final int IGRAPH = Graph.IGRAPH;
	int index = 0, other = 1;
	int count = 0, size = 0;
	Graph graph;
	Comparator<Entity> comp;
	TreeMap<Node, List<Entity>> tree;

	GraphEdgeIndex(Graph g, int n){
		graph = g;
		index = n;
		switch (index){
		case 0: other = 1; break;
		case 1: other = 0; break;
		case Graph.IGRAPH: other = 0; break;
		}
		
		comp = getComparator();
		tree = new 	TreeMap<Node, List<Entity>>(getComp());
	}
	
	
	
	Comparator<Node> getComp(){
		return new Comparator<Node>(){
			public int compare(Node o1, Node o2) {
				int res = o1.compare(o2);								
				return res;
			}
		};
	}
	
	Comparator<Entity> getComparator(){
		return new Comparator<Entity>(){
			public int compare(Entity o1, Entity o2) {
				int res = getNode(o1, index).compare(getNode(o2, index));
				if (res == 0){
					res = getNode(o1, other).compare(getNode(o2, other));
					if (res == 0 && index == 0){
						res = o1.getGraph().compare(o2.getGraph());
					}
				}
				return res;
			}
		};
	}
	
	int duplicate(){
		return count;
	}
	
	Node getNode(Entity ent, int n){
		if (n == IGRAPH) return ent.getGraph();
		else return ent.getEdge().getNode(n);
	}
	
	public Entity add(Entity ent) {
		List<Entity> list = declare(ent);
		Entity res = add(ent, list);
		if (res != null) size++;
		return res;
	}
	
	List<Entity> declare(Entity ent){
		Node node = ent.getEdge().getNode(index);
		List<Entity> list = tree.get(node);
		if (list == null){
			list = new ArrayList<Entity>();
			tree.put(node, list);
		}
		return list;
	}
	
	
	public int size(){
		return size;
	}
	
	public Entity add(Entity edge, List<Entity> list){
		
		if (true){
			int i = find(list, edge, 0, list.size());
			int res = 0;
			
			if (i>=list.size()){
				list.add(edge);
				return edge;
			}
			
			res = comp.compare(edge, list.get(i));
			if (res == 0 && index == 0){
				// eliminate duplicate at load time for index 0
				count++;
				return null;
			}
			
			list.add(i, edge);
		}
		else {
			list.add(edge);
		}
		
		return edge;
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

	public Iterable<Entity> getEdges(Node node) {
		List<Entity> list = tree.get(node);
		return list;
	}

	public Iterable<Entity> getEdges() {
		return this;
	}
	
	
	public Iterator<Entity> iterator(){

		return new Iterator<Entity>(){

			List<Entity> list = new ArrayList<Entity>();
			Iterator<Entity> ite = list.iterator();
			Iterator<Node> itn = tree.keySet().iterator();
			
			
			public boolean hasNext() {
				
				if (ite.hasNext()) return true;
				
				while (itn.hasNext()){
					Node node = itn.next();
					list = tree.get(node);
					ite = list.iterator();
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
	
	
	
	
}
