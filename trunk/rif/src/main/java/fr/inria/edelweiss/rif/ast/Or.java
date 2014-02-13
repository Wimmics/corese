package fr.inria.edelweiss.rif.ast;

import fr.inria.edelweiss.rif.api.IFormula;

/** Disjunction of formulas */
public class Or extends Connective<IFormula> {
	private Or() {}
	
	public static Or create() {
		return new Or() ;
	}
	
	public void compile() {
		for(IFormula f : connectedStatements) f.toString() ;
	}
	
}
