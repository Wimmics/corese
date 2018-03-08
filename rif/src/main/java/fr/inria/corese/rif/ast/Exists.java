package fr.inria.corese.rif.ast;

import java.util.Vector;

import fr.inria.corese.rif.api.IFormula;

public class Exists extends Quantifier implements IFormula {
	
	private IFormula formula ;
	
	private Exists(Vector<Var> vars, IFormula formula) {
		super(vars);
		this.formula = formula ;
	}
	
	public IFormula getFormula() {
		return this.formula ;
	}
	
	public static Exists create(Vector<Var> vars, IFormula formula) {
		return new Exists(vars, formula) ;
	}


}
