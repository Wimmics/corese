package fr.inria.edelweiss.engine.tool.core;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.engine.tool.core.ParserImpl;
import fr.inria.edelweiss.engine.tool.api.Parser;
import fr.inria.edelweiss.engine.model.api.Query;
import fr.inria.edelweiss.engine.model.core.QueryImpl;
import fr.inria.edelweiss.engine.tool.api.QueriesTreatment;

public class QueriesTreatmentImpl implements QueriesTreatment {

	/**
	 * create a query with a set of clauses
	 */
	public List<Query> createQuery(IEngine server,List<String> queryFiles) {

		//create the list of queries to return
		List<Query> queries=new ArrayList<Query>();
		
		//the xpath query to access to the query
		String xPath="/rdf:RDF/query:query/cos:value";
		
		//parameter to access to the value
		String parameter=".";
		
		//the astQuery to contain the SPARQLQuery parsed
		ASTQuery ast=null;
		
		//parser to parse the file containing queries to get the list of queries
		Parser parser=new ParserImpl();
		
		//iterate the list of files of queries
		for(String queryFile:queryFiles){
			
			//parsing the file containing queries to get the list of queries
			List<String> queriesString=parser.extractSPARQLQuery(queryFile, xPath, parameter);
			
			//iterate the list of queries
			for(String queryString: queriesString){
				
				try {
					//parse a query
					ast = server.parse(queryString);
					
					//expansion of prefix
					ast = ast.expand();
					
				} catch (EngineException e) {
					System.out.println("can't parse the query : "+queryString);
				}
					
				//create the query containing the body of the SPARQL query
				Query query=new QueryImpl(ast);
				
				//add the object query to the list of queries to return
				queries.add(query);
			}
		}
		
		return queries;
	}

}
