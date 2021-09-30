package fr.inria.corese.core.util;

import java.util.ArrayList;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;


/**
 * Graph Listener:
 *  survey the size of the graph and may skip insert above max size
 *  log insert and delete : kg:listen kg:insert true
 *  log insert that match a triple pattern
 *  graph kg:insert { ?x a c:Person }
 * 
 * @author Olivier Corby, Wimmics, INRIA 2012
 */
public class GraphListenerImpl implements GraphListener {
	
	int max = Integer.MAX_VALUE;
	boolean isInsert = false, isDelete = false;
	
	ArrayList<Triple> linsert;
	
	
	
	GraphListenerImpl(){
		linsert = new ArrayList<Triple>();
	}
	
	public static GraphListenerImpl create(){
		return new GraphListenerImpl();
	}

	
	public static GraphListenerImpl create(int n){
		GraphListenerImpl gl = new GraphListenerImpl();
		gl.setMax(n);
		return gl;
	}
	
	public void setMax(int n){
		max = n;
	}
	
	/**
	 * kg:insert  triple(<John> ?p ?y)
	 */
	public void setProperty(String name, Triple t){
		if (name.equals(Pragma.LISTEN_INSERT)){
			System.out.println("kg:insert " + t);
			linsert.add(t);
		}
		else if (name.equals(Pragma.LISTEN_DELETE)){
			
		}
	}
	
	public void setProperty(String name, IDatatype dt){
		if (name.equals(Pragma.SIZE)){
			setMax(dt.intValue());
		}
		else if (name.equals(Pragma.INSERT)){
			isInsert = isTrue(dt);			
		}
		else if (name.equals(Pragma.DELETE)){
			isDelete = isTrue(dt);			
		}
	}
	
	boolean isTrue(IDatatype dt){
		try {
			return dt.isTrue();
		} catch (CoreseDatatypeException e) {
			return false;
		}
	}
	
	public void insert(Graph g, Edge ent) {
		if (isInsert){
			System.out.println("Insert: " + ent);
		}
		else {
			linsert(g, ent);
		}
	}

	
	/**
	 * Test if edge matches a triple pattern 
	 */
	private void linsert(Graph g, Edge ent) {
		for (Triple t : linsert){
			if (match(t, ent)){
				System.out.println("insert: " + ent);
			}
		}
	}
	
	
	/**
	 * Test if edge matches triple pattern 
	 */
	boolean match(Triple t, Edge edge){
		boolean b = 
			match(t.getSubject(), edge.getNode(0)) &&
			match(t.getObject(), edge.getNode(1)) ;
		
		if (t.getVariable() == null){
			b = b && match(t.getProperty(), edge.getEdgeNode());
		}
		
		return b;   
	}
	
	
	boolean match(Atom at, Node n){
		if (at.isVariable() || at.isBlank() || at.isBlankNode()){
			return true;
		}
		IDatatype dt =  n.getValue();
		try {
			return at.getDatatypeValue().equalsWE(dt);
		} catch (CoreseDatatypeException e) {
			return false;
		}
	}

	public void delete(Graph g, Edge ent) {
		if (isDelete){
			
		}
	}

	public void addSource(Graph g) {
	}

	public boolean onInsert(Graph g, Edge ent) {
		return g.size() < max;
	}

    @Override
    public void start(Graph g, Query q) {
    }

    @Override
    public void finish(Graph g, Query q, Mappings m) {
    }

    @Override
    public void load(String path) {
    }
	
	

}
