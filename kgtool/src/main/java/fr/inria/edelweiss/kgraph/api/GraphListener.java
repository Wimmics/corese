package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.Graph;

public interface GraphListener {
	
	void addSource(Graph g);
	
	boolean onInsert(Graph g, Entity ent);

	void insert(Graph g, Entity ent);

	void delete(Graph g, Entity ent);
	
	
}
