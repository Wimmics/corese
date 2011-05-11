package fr.inria.edelweiss.engine.tool.core;

import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;
import fr.inria.edelweiss.engine.tool.core.ParserImpl;
import fr.inria.edelweiss.engine.tool.api.Parser;
import fr.inria.edelweiss.engine.model.api.Rule;
import fr.inria.edelweiss.engine.model.api.RuleBase;
import fr.inria.edelweiss.engine.model.core.RuleBaseImpl;
import fr.inria.edelweiss.engine.model.core.RuleImpl;
import fr.inria.edelweiss.engine.tool.api.RulesTreatment;

public class RulesTreatmentImpl implements RulesTreatment {

	/**
	 * create a ruleBase with a set of rules
	 */
	public RuleBase createRules(List<String> ruleFiles) {
		return createRules(null,  ruleFiles);
	}
		
		
	public RuleBase createRules(RuleBase ruleBase, List<String> ruleFiles) {

		//the rule base
		if (ruleBase == null) ruleBase = new RuleBaseImpl();	

		//the xpath query to access to the rule
		String xPath="/rdf:RDF/rule:rule/rule:value";

		//parameter to access to the value
		String parameter=".";
		
		//the astQuery to contain the SPARQLQuery parsed
		//ASTQuery ast=null;
		
		//parser to parse the files containing rules to get the list of rules
		Parser parser=new ParserImpl();
		
		//iterate the list of files of rules
		for(String ruleFile:ruleFiles){
			
			//parsing the file containing rules to get the list of rules
			List<String> rules=parser.extractSPARQLQuery(ruleFile, xPath, parameter);
			
			//iterate the list of rules
			for(String ruleString:rules){
				//System.out.println("** Rule: " + ruleString);
				try {
					//parse a rule
					//ASTQuery ast=server.parse(ruleString);
					
					ASTQuery ast = ASTQuery.create(ruleString);
					ast.setSPARQL1(true);
					ast = ParserSparql1.create(ast).parse();
					
					ast = ast.expand();

					//create an object rule with the instance of rule as ASTQuery
					Rule rule=new RuleImpl();
					rule.setRuleInstance(ast);
					
					//add the rule to the base of rules
					ruleBase.add(rule);
				} 
				catch (EngineException e) {
					System.out.println(e.getMessage());
					System.out.println("can't parse the rule : "+ruleString);
				}
				
			}
		}
		
		return ruleBase;
		
	}

}
