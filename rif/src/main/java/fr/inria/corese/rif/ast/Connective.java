package fr.inria.corese.rif.ast;

import java.util.Vector;

import fr.inria.corese.rif.api.IConnectible;
import fr.inria.corese.rif.api.IFormula;

/** Connective (AND | OR) */
public abstract class Connective<C extends IConnectible> extends Statement implements IFormula {
	protected Vector<C> connectedStatements = new Vector<C>() ;
	
	public void add(C statement) {
		connectedStatements.add(statement) ;
	}
	
}
