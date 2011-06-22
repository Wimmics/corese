package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

import fr.inria.edelweiss.rif.api.IConnectible;
import fr.inria.edelweiss.rif.api.IFormula;

/** Connective (AND | OR) */
public abstract class Connective<C extends IConnectible> extends Statement implements IFormula {
	protected Vector<C> connectedStatements = new Vector<C>() ;
	
	public void add(C statement) {
		connectedStatements.add(statement) ;
	}
	
}
