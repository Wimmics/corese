package fr.inria.corese.engine.model.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.corese.engine.model.api.Rule;
import fr.inria.corese.engine.model.api.RuleBase;

public class RuleBaseImpl implements RuleBase {

	private List<Rule> ruleBaseInstance;
	
	public List<Rule> getRuleBaseInstance() {
		return ruleBaseInstance;
	}

	public void setRuleBaseInstance(List<Rule> ruleBaseInstance) {
		this.ruleBaseInstance = ruleBaseInstance;
	}

	/**
	 * to iterate the set of rules
	 */
	public Iterator<Rule> iterator() {
		return ruleBaseInstance.iterator();
	}

	/**
	 * constructor which create an instance of the object ruleBaseInstance
	 */
	public RuleBaseImpl() {
		ruleBaseInstance = new ArrayList<Rule>();
	}

	public void add(Rule rule) {
		ruleBaseInstance.add(rule);
	}

}
