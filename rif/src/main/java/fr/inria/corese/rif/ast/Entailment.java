package fr.inria.corese.rif.ast;

import fr.inria.corese.rif.api.IClause;
import fr.inria.corese.rif.api.IConclusion;
import fr.inria.corese.rif.api.IFormula;

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
