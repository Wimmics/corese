package fr.inria.corese.rif.ast;

import java.util.HashMap;

public class NamedArgsPredicate extends Predicate {

	private HashMap<String, Term> arguments = new HashMap<String, Term>() ;

	private NamedArgsPredicate(boolean external, Const ident) {
		super(external, ident) ;
	}

	private NamedArgsPredicate(Const ident) {
		super(ident) ;
	}

	private NamedArgsPredicate(boolean external, Const ident, HashMap<String, Term> args) {
		super(external, ident) ;
		this.arguments = args ;
	}

	public static NamedArgsPredicate create(Const ident) {
		return new NamedArgsPredicate(ident) ;
	}

	public static NamedArgsPredicate create(boolean external, Const ident, HashMap<String, Term> args) {
		return new NamedArgsPredicate(external, ident, args) ;
	}
	
	public static NamedArgsPredicate create(boolean external, Const ident) {
		return new NamedArgsPredicate(external, ident) ;
	}

	public void addArgument(String argName, Term arg) {
		if(this.arguments.put(argName, arg) != null)
			;
		System.out.println("named arg predicate was " + argName) ;
	}

}
