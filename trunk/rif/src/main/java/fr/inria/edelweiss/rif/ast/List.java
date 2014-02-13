package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

public class List extends Term {

	private Vector<Term> listedTerms = new Vector<Term>() ;
	
	protected List(Vector<Term> terms) {
		this.listedTerms = terms ;
	}
	
	public static List create(Vector<Term> terms) {
		return new List(terms) ;
	}
	
	public boolean isEmptyList() {
		return listedTerms.isEmpty() ;
	}
	
	
}
