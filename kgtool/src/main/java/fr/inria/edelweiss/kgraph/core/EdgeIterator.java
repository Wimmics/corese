package fr.inria.edelweiss.kgraph.core;

import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;

/**
 * 
 * Eliminate successive similar edges (when no graph ?g {})
 * Check from & from named
 * Check graph ?g
 */
public class EdgeIterator implements Iterable<Entity>, Iterator<Entity> {
	Iterable<Entity> iter;
	Iterator<Entity> it;
	Edge last;
	Node graph;
	List<Node> from;
	boolean hasGraph, hasFrom;
	
	// eliminate duplicate edges due to same source
	EdgeIterator(Iterable<Entity> i){
		iter = i;
		hasGraph = false;
		hasFrom = false;
	}
	
	public static EdgeIterator create(Iterable<Entity> i){
		return new EdgeIterator(i);
	}
	
	public EdgeIterator(Iterable<Entity> i, List<Node> list, boolean hasGraph){
		iter = i;
		from = list;
		this.hasGraph = hasGraph;
		hasFrom = from.size()>0;
	}
	
	void setGraph(Node g){
		hasGraph = true;
		graph = g;
	}

	@Override
	public Iterator<Entity> iterator() {
		// TODO Auto-generated method stub
		it = iter.iterator();
		return this;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return it.hasNext();
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
					if (! ent.getGraph().same(graph)){
						ok = false;
					}
				}
			}
			else if (last != null){
				// eliminate successive duplicates
				if (last.getNode(0).same(ent.getNode(0)) &&
					last.getNode(1).same(ent.getNode(1)) &&
					last.getLabel().equals(ent.getEdge().getLabel())){
					ok = false;
				}
			}
			
			if (ok && hasFrom){
				ok = isFrom(ent, from);
			}
			
			if (ok){
				last = ent.getEdge();
				return ent;
			}
		}
		return null;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
	boolean isFrom(Entity ent, List<Node> from){
		for (Node node : from){
			if (ent.getGraph().same(node)){
				return true;
			}
		}
		return false;
	}
	
}
