package fr.inria.corese.rif.ast;

public class Const extends Term {
	
	private String name, datatype, language ;
	
	private Const(String name, String datatype, String language) {
		this.name = name ;
		this.datatype = datatype ;
		this.language = language ;
		System.out.println("New const : " + name + ", " + datatype + ", " + language) ;
	}
	
	private Const(String name, String datatype) {
		this(name, datatype, null) ;
	}
	
	public static Const create(String name, String datatype, String language) {
		return new Const(name, datatype, language) ;
	}
	
	public static Const create(String name, String datatype) {
		return new Const(name, datatype) ;
	}
	
	
	
}
