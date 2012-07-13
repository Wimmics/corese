package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgram.api.core.Entity;

public interface GraphListener {
	
	void insert(Entity ent);

	void delete(Entity ent);
	
	
}
