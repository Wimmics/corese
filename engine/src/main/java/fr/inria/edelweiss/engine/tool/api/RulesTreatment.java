package fr.inria.edelweiss.engine.tool.api;

import java.util.List;

import fr.inria.edelweiss.engine.model.api.RuleBase;

public interface RulesTreatment {

	/**
	 * create a ruleBase with a set of rules
	 */
	//public RuleBase createRules(IEngine server,List<String[]> namespaces);
	public RuleBase createRules(List<String> ruleFiles);
}
