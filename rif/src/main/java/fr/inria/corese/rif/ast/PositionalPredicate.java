package fr.inria.corese.rif.ast;

import java.util.Vector;

public class PositionalPredicate extends Predicate {
	
	private Vector<Term> arguments = new Vector<Term>() ;
		
	private PositionalPredicate(boolean external, Const ident) {
		super(external, ident) ;
	}
	
	private PositionalPredicate(Const ident) {
		super(ident) ;
	}
	
	private PositionalPredicate(boolean external, Const ident, Vector<Term> args) {
		super(external, ident) ;
		this.arguments = args ;
	}
	
	public static PositionalPredicate create(Const ident) {
		return new PositionalPredicate(ident) ;
	}
	
	public static PositionalPredicate create(boolean external, Const ident) {
		return new PositionalPredicate(external, ident) ;
	}
	
	public static PositionalPredicate create(boolean external, Const ident, Vector<Term> args) {
		return new PositionalPredicate(external, ident, args) ;
	}
	
	public void addArgument(Term arg) {
		this.arguments.add(arg) ;
	}
	
}
