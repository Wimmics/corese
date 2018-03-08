package fr.inria.corese.rif.ast;

import java.util.HashMap;

public class NamedArgsFunction extends Function {

	private HashMap<String, Term> arguments = new HashMap<String, Term>() ;

	private NamedArgsFunction(boolean external, Const ident) {
		super(external, ident) ;
	}

	private NamedArgsFunction(Const ident) {
		super(ident) ;
	}

	private NamedArgsFunction(boolean external, Const ident, HashMap<String, Term> args) {
		super(external, ident) ;
		this.arguments = args ;
	}

	public static NamedArgsFunction create(Const ident) {
		return new NamedArgsFunction(ident) ;
	}

	public static NamedArgsFunction create(boolean external, Const ident, HashMap<String, Term> args) {
		return new NamedArgsFunction(external, ident, args) ;
	}
	
	public static NamedArgsFunction create(boolean external, Const ident) {
		return new NamedArgsFunction(external, ident) ;
	}

	public void addArgument(String argName, Term arg) {
		if(this.arguments.put(argName, arg) != null)
			;
		System.out.println("named arg expr was " + argName) ;
	}

}
