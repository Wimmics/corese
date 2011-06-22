package fr.inria.edelweiss.rif.ast;

/** Expression uniterm */
public class Function extends Term {
	
	private boolean external = false ;
	
	private Const ident ;
	
	protected Function(boolean external, Const ident) {
		this.external = external ;
		this.ident = ident ;
	}
	
	protected Function(Const ident) {
		this.ident = ident ;
	}
	
	public static Function create(Const ident) {
		return new Function(ident) ;
	}
	
	public static Function create(boolean external, Const ident) {
		return new Function(external, ident) ;
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
