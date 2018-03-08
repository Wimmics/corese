package fr.inria.corese.kgengine.api;

import java.util.List;

import fr.inria.corese.sparql.triple.parser.ASTQuery;

public interface IBRuleEngine {
	
	void setEngine(IEngine server);
	
	IResults query(String query);
	
	IResults query(ASTQuery ast);
	
	void load(String path);
	
	void load(List<String> lpath);
	
}
