package junit;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.api.Tagger;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rdf.EdgeExtend;

/**
 * Implement GraphListener to listen to edge insert and delete
 * and broadcast to peers (cf crdt)
 * Implement Tagger to generate unique ID for triples
 * 
 * @author Olivier Corby, Wimmics INRIA 2012
 */
public class GListener implements GraphListener, Tagger {
	private static final String INSERT_DATA = "insert data";
	private static final String DELETE_DATA = "delete data";

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
		for (Graph gg : list){
			gg.copy(ent);
		}
	}
	
	
	/**
	 * Generate and process an Update query
	 */
	void update(Graph g, Entity ent, boolean insert){
		String query = toQuery(ent, insert);
		
		if (query != null){
			System.out.println(query);
			QueryProcess exec = QueryProcess.create(g);
			try {
				exec.update(query);
			} catch (EngineException e) {
				e.printStackTrace();
			}

		}
	}
	
	
	String toQuery(Entity ent, boolean insert){
		if (ent instanceof EdgeExtend){
			String str = ((EdgeExtend)ent).toParse();
			StringBuffer sb = new StringBuffer();
			sb.append((insert) ? INSERT_DATA : DELETE_DATA);
			sb.append(" { ");
			sb.append(str);
			sb.append(" }");
			return sb.toString();
		}
		return  null;
	}

	/*
	 * Graph declare that a delete has been successfully performed
	 * Broadcast to peers
	 * The triple has a unique tag
	 */
	public void delete(Graph g, Entity ent) {
		System.out.println(id + " delete: " + ent);
		for (Graph gg : list){	
			//update(gg, ent, false);
			gg.delete(ent);
		}
	}
	

	/**
	 * Generate a unique tag
	 */
	public String tag() {
		return key + tag++;
	}

	public boolean onInsert(Graph g, Entity ent) {
		return true;
	}

    @Override
    public void start(Graph g, Query q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finish(Graph g, Query q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
