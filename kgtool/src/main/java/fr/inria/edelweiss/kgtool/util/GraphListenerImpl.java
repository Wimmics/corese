package fr.inria.edelweiss.kgtool.util;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.api.GraphListener;

public class GraphListenerImpl implements GraphListener {
	
	public static GraphListenerImpl create(){
		return new GraphListenerImpl();
	}

	public void insert(Entity ent) {
		System.out.println("insert: " + ent);
	}

	public void delete(Entity ent) {
		System.out.println("delete: " + ent);
	}
	
	

}
