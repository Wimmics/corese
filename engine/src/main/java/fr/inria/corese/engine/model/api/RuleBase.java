package fr.inria.corese.engine.model.api;

import java.util.List;

public interface RuleBase extends Iterable<Rule> {
	
	/**
	 * getter and setter of the instance ruleBaseInstance
	 */
	public List<Rule> getRuleBaseInstance();
	public void setRuleBaseInstance(List<Rule> ruleBaseInstance);
	
	/**
	 * add a rule to the base of rules
	 */
	public void add(Rule rule);

}
