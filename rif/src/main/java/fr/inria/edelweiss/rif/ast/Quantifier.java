package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

import fr.inria.edelweiss.rif.api.IFormula;

/** (Exists | Forall) Var+ (CLAUSE | FORMULA) */
public abstract class Quantifier extends Statement implements IFormula {
	protected Vector<Var> boundVars ;
	
	protected Quantifier(Vector<Var> vars) {
		this.boundVars = vars ;
	}
	
}
