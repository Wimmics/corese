package fr.inria.corese.core.api;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

public interface GraphListener {
	
	void addSource(Graph g);
	
	boolean onInsert(Graph g, Edge ent);

	void insert(Graph g, Edge ent);

	void delete(Graph g, Edge ent);
        
        void start(Graph g, Query q);
	
	void finish(Graph g, Query q, Mappings m);
        
        void load(String path);

}
