package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

public class PositionalFunction extends Function {
	
	private Vector<Term> arguments = new Vector<Term>() ;
	
	private PositionalFunction(boolean external, Const ident) {
		super(external, ident) ;
	}
	
	private PositionalFunction(Const ident) {
		super(ident) ;
	}
	
	private PositionalFunction(boolean external, Const ident, Vector<Term> args) {
		super(external, ident) ;
		this.arguments = args ;
	}
	
	public static PositionalFunction create(Const ident) {
		return new PositionalFunction(ident) ;
	}
	
	public static PositionalFunction create(boolean external, Const ident) {
		return new PositionalFunction(external, ident) ;
	}
	
	public static PositionalFunction create(boolean external, Const ident, Vector<Term> args) {
		return new PositionalFunction(external, ident, args) ;
	}
	
	public void addArgument(Term arg) {
		this.arguments.add(arg) ;
	}
	
}
