package fr.inria.edelweiss.rif.ast;

import java.util.Vector;

import fr.inria.edelweiss.rif.api.IClause;
import fr.inria.edelweiss.rif.api.IRule;

/** The only production in which a forall quantifier can be used is a root rule */
public class Forall extends Quantifier implements IRule {

	private IClause clause ;
	
	private Forall(Vector<Var> vars, IClause clause) {
		super(vars);
		this.clause = clause ;
	}
	
	public IClause getClause() {
		return this.clause ;
	}

	public static Forall create(Vector<Var> vars, IClause clause) {
		return new Forall(vars, clause) ;
	}
	
}
