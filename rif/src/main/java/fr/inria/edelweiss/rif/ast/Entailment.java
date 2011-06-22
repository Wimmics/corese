package fr.inria.edelweiss.rif.ast;

import fr.inria.edelweiss.rif.api.IClause;
import fr.inria.edelweiss.rif.api.IConclusion;
import fr.inria.edelweiss.rif.api.IFormula;

/** Conclusion :- Premise */
public class Entailment extends Statement implements IClause {

	private IConclusion conslusion ;
	private IFormula premise ;
	
	private Entailment() {}
	
	private Entailment(IConclusion conclusion, IFormula condition) {
		super() ;
		this.conslusion = conclusion ;
		this.premise = condition ;
	}
	
	public static Entailment create(IConclusion conclusion, IFormula condition) {
		return new Entailment(conclusion, condition) ;
	}
	
	public IConclusion getConclusion() {
		return this.conslusion ;
	}
	
	public IFormula getCondition() {
		return this.premise ;
	}
	
}
