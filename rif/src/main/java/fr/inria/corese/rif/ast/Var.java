package fr.inria.corese.rif.ast;

public class Var extends Term {
	private String name ;
	
	private Var() {}
	
	private Var(String name) {
		this.name = name ;
	}

	public static Var create(String name) {
		return new Var(name) ;
	}
	
	public String getName() {
		return this.name ;
	}
}
