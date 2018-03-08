package fr.inria.corese.engine.model.api;

import java.util.Iterator;

import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.api.ElementClause;

public interface Clause extends Iterable<ElementClause> {

	public static int SUBJECT 	= 0;
	public static int PROPERTY 	= 1;
	public static int OBJECT 	= 2;
	public static int GRAPH 	= 3;
	
	/**
	 * To iterate the elements of the triple (clause)
	 */
	public Iterator<ElementClause> iterator();
	
	public ElementClause get(int i);
		
	public Triple getTriple();
	
	public int size();
	
	public boolean isGround();
	
	public void setGround(boolean b);

}
