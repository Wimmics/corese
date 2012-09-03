package junit;

import java.util.ArrayList;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Tagger;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.rdf.EdgeExtend;

/**
 * Implement GraphListener to listen to edge insert and delete
 * and broadcast to peers (cf crdt)
 * Implement Tagger to generate unique ID for triples
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 */
public class GListener implements GraphListener, Tagger {
	private static final Object NL = System.getProperty("line.separator");

	static int count = 1;
	
	// unique ID for the Listener/Tagger
	int id;
	// counter for unique tags
	int tag;
	// string base to generate string tags
	String key;
	
	// The source graph that is listened to
	Graph graph;
	// The list of target peers to broadcast
	List<Graph> list;
	
	
	
	GListener(){
		list = new ArrayList<Graph>();
		id = count++;
		key = id + ".";
		tag = 0;
		}
	
	public static GListener create(){
		return new GListener();
	}
	
	int getID(){
		return id;
	}
	
	public String toString(){
		return Integer.toString(id);
	}

	/**
	 * A source graph where insert/delete happen
	 */
	public void addSource(Graph g){
		graph = g;
	}
	
	
	/**
	 * A target graph for broadcast
	 */
	public void addTarget(Graph g){
		list.add(g);
	}
	
	/*
	 * Graph declare that an insert has been successfully performed
	 * Broadcast to peers
	 * The triple has a unique tag
	 */
	public void insert(Graph g, Entity ent) {
		System.out.println(id + " insert: " + ent);
		update(g, ent);
		for (Graph gg : list){
			gg.copy(ent);
		}
	}
	
	
	void update(Graph g, Entity ent){
		if (ent instanceof EdgeExtend){
			String str = ((EdgeExtend)ent).toParse();
			StringBuffer sb = new StringBuffer();
			sb.append("insert data { ");
			sb.append(str);
			sb.append(" }");
			
			System.out.println(sb.toString());
		}
	}

	/*
	 * Graph declare that a delete has been successfully performed
	 * Broadcast to peers
	 * The triple has a unique tag
	 */
	public void delete(Graph g, Entity ent) {
		System.out.println(id + " delete: " + ent);
		for (Graph gg : list){						
			gg.delete(ent);
		}
	}
	

	/**
	 * Generate a unique tag
	 */
	public String tag() {
		return key + tag++;
	}

}
