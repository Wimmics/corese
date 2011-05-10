package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgraph.rule.RuleEngine;

public interface Loader {
	
	void load(String path);
	
	void load(String path, String source);
	
	RuleEngine getRuleEngine();


}
