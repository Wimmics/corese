package fr.inria.edelweiss.kgraph.api;

import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.InputStream;

public interface Loader {
	
	void init(Object o);
	
	boolean isRule(String path);
	
	void load(String path);
	
	void load(String path, String source);
        
        void load(InputStream stream, String str) throws LoadException;
	
	void loadWE(String path) throws LoadException;
	
	void loadWE(String path, String source) throws LoadException;
	
	RuleEngine getRuleEngine();


}
