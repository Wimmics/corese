package fr.inria.edelweiss.kgraph.query;

import java.util.Iterator;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;

/**
 * Iterator of Entity that perform match.match()
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 *
 */
public class MatchIterator implements Iterable<Entity>, Iterator <Entity> {
	
	Iterable<Entity> ii;
	Iterator<Entity> it;
 	Matcher match;
 	Environment env;
	Edge edge;
	
	MatchIterator(Iterable <Entity> it, Edge e, Environment env, Matcher m) {
		ii = it;
		match = m;
		edge = e;
		this.env = env;
	}

	@Override
	public Iterator<Entity> iterator() {
		it = ii.iterator();
		return this;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Entity next() {
		while (it.hasNext()){
			Entity ent = it.next();
			if (ent != null && match.match(edge, ent.getEdge(), env)){
				return ent;
			}
		}
		return null;
	}

	@Override
	public void remove() {		
	}
	
	
	

}
