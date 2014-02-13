package fr.inria.edelweiss.rif.ast;

/** 
 * @deprecated multiple inheritance failure */
public abstract class Uniterm extends Symbol {
	
	private boolean external = false ;
	
	private Const ident ;
	
	protected Uniterm(boolean external, Const ident) {
		this.external = external ;
		this.ident = ident ;
	}
	
	public boolean isExternal() {
		return this.external ;
	}
	
	public Const getIdent() {
		return this.ident ;
	}
	
}
