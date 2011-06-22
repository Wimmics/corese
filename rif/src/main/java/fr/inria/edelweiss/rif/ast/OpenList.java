package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

public class OpenList extends List {

	private Term tail ;
	
	private OpenList(Vector<Term> terms, Term tail) {
		super(terms) ;
		this.tail = tail ;
	}
	
	public static OpenList create(Vector<Term> terms, Term tail) {
		return new OpenList(terms, tail) ;
	}
	
	@Override
	public boolean isEmptyList() {
		return false ;
	}
	
	public Term getTail() {
		return this.tail ;
	}
	
	
}
