package fr.inria.corese.kgraph.api;

import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;

public interface GraphListener {
	
	void addSource(Graph g);
	
	boolean onInsert(Graph g, Entity ent);

	void insert(Graph g, Entity ent);

	void delete(Graph g, Entity ent);
        
        void start(Graph g, Query q);
	
	void finish(Graph g, Query q, Mappings m);
        
        void load(String path);

}
