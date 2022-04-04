package fr.inria.corese.core.api;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

public interface GraphListener {
	
    default void addSource(Graph g) {}
	
    default boolean onInsert(Graph g, Edge ent) { return true; }

    default void insert(Graph g, Edge ent) {}

    default void delete(Graph g, Edge ent) {}
        
    default void start(Graph g, Query q) {}
	
    default void finish(Graph g, Query q, Mappings m) {}
        
    default void load(String path) {}

}
