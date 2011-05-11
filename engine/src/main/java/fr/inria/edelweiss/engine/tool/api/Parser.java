package fr.inria.edelweiss.engine.tool.api;

import java.util.List;

public interface Parser {

	/**
	 * Extract the rules to the rdf file 
	 */
	public List<String> extractSPARQLQuery(String ruleFile,String xPath,String parameter);
}
