package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;

public interface Loader {
	
	void init(Object o);
	
	void load(String path);
	
	void load(String path, String source);
	
	void loadWE(String path) throws LoadException;
	
	void loadWE(String path, String source) throws LoadException;
	
	RuleEngine getRuleEngine();


}
