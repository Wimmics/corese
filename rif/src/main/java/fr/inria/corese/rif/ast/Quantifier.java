package fr.inria.corese.rif.ast;

import java.util.Vector;

import fr.inria.corese.rif.api.IFormula;

/** (Exists | Forall) Var+ (CLAUSE | FORMULA) */
public abstract class Quantifier extends Statement implements IFormula {
	protected Vector<Var> boundVars ;
	
	protected Quantifier(Vector<Var> vars) {
		this.boundVars = vars ;
	}
	
}
