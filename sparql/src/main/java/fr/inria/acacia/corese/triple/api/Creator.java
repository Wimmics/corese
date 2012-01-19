package fr.inria.acacia.corese.triple.api;

import java.util.List;

import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.RDFList;

/**
* 
* Interface to create edges from Turtle parser
* @author Olivier Corby, INRIA 2012
* 
*/
public interface Creator {
	
	public void triple(Atom subject, Atom predicate, Atom object);
	
	public void triple(Atom predicate, List<Atom> l);

	public void graph(Atom graph);
	
	public void endGraph(Atom graph);

	public void list(RDFList l);

}
