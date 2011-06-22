package fr.inria.edelweiss.rif.ast;

import fr.inria.edelweiss.rif.api.IFormula;

/** Atom uniterm */
public class Predicate extends Atomic implements IFormula {

	private boolean external = false ;
	
	private Const ident ;
	
	protected Predicate(boolean external, Const ident) {
		this.external = external ;
		this.ident = ident ;
	}
	
	protected Predicate(Const ident) {
		this.ident = ident ;
	}
	
	public static Predicate create(Const ident) {
		return new Predicate(ident) ;
	}
	
	public static Predicate create(boolean external, Const ident) {
		return new Predicate(external, ident) ;
	}
	
	public void setExternal(boolean external) {
		this.external = external ;
	}
	
	public boolean isExternal() {
		return this.external ;
	}
	
	public Const getIdent() {
		return this.ident ;
	}

}
