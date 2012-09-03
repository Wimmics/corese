package fr.inria.edelweiss.kgtool.util;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.Graph;

public class GraphListenerImpl implements GraphListener {
	
	public static GraphListenerImpl create(){
		return new GraphListenerImpl();
	}

	public void insert(Graph g, Entity ent) {
		System.out.println("insert: " + ent);
	}

	public void delete(Graph g, Entity ent) {
		System.out.println("delete: " + ent);
	}

	public void addSource(Graph g) {
		
	}
	
	

}
