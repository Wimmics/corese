package fr.inria.corese.kgraph.core;

import java.util.Iterator;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;

public class NodeIterator implements Iterable<Node>, Iterator<Node> {
	
	Iterable<Entity> ie;
	Iterator<Entity> it;
	int index;
	
	NodeIterator(Iterable<Entity> it, int n){
		ie = it;
		index = n;
	}
	
	public static NodeIterator create(Iterable<Entity> it, int n){
		return new NodeIterator(it, n);
	}

	public Iterator<Node> iterator() {
		it = ie.iterator();
		return this;
	}

	public boolean hasNext() {
		return it.hasNext();
	}

	public Node next() {
		Entity ent = it.next();
		if (ent == null) return null;
		return ent.getEdge().getNode(index);
	}

	public void remove() {		
	}
	
	

}
