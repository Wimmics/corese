package fr.inria.edelweiss.engine.model.api;

import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;

public interface Rule {
	
	int getID();
	
	/**
	 * getter and setter of the instance ruleInstance
	 */
	public ASTQuery getRuleInstance();
	public void setRuleInstance(ASTQuery ruleInstance);
	
	/**
	 * get the body of the rule as a query
	 */
	public Query getBody();
	
	/**
	 * get the conclusion of the rule as a clause
	 */
	public List<Clause> getHead();
	
	/**
	 * the conclusion of the rule match the clause ?
	 */
	public List<Clause> match(Clause clause,Bind bind);

}
