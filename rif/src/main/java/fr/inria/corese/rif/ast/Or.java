package fr.inria.corese.rif.ast;

import fr.inria.corese.rif.api.IFormula;

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
