package fr.inria.acacia.corese.api;

import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;

public interface IBRuleEngine {
	
	void setEngine(IEngine server);
	
	IResults query(String query);
	
	IResults query(ASTQuery ast);
	
	void load(String path);
	
	void load(List<String> lpath);
	
}
