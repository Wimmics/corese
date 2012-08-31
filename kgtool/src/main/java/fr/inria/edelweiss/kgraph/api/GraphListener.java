package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.Graph;

public interface GraphListener {
	
	void addSource(Graph g);
	
	void insert(Graph g, Entity ent);

	void delete(Graph g, Entity ent);
	
	
}
