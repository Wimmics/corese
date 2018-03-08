package fr.inria.corese.kgengine.api;

import java.util.Enumeration;

import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 * This interface wraps query results<br />
 * <ul>
 * <li>
 * With a query result, the most common and practical thing to do is to just print it. Results will 
 * be printed as asked by the DISPLAY statement. <br />
 * Results for query with ASK or SELECT clauses will be printed in XML by default, but it is possible to print 
 * them in RDF format by using the keyword DISPLAY RDF. (more info in the <a href="http://www-sop.inria.fr/acacia/soft/corese/manual/">user manual</a>).<br />
 * Results for query with CONSTRUCT or DESCRIBE clauses will be printed in RDF format.<br />
 * <br />
 * <i>Example:</i>
 * <code><pre>
 * try {
 * 	IResults res = engine.SPARQLQuery(queryString);
 * 	System.out.println(res);
 * } catch (EngineException e) {
 * 	e.printStackTrace();
 * }</pre></code>
 * </li>
 * <li>
 * When using a SELECT clause, it is also possible to get details to manage results in another way:<br />
 * get each result (IResult) with an enumeration; then, for each result, get the name of the 
 * selected variable and its corresponding value.<br />
 * <br />
 * <i>Example:</i> 
 * <code><pre>
 * try {
 * 	// get the results of the query
 * 	IResults res = engine.SPARQLQuery(queryString);
 * 	// get the list of all the selected variables
 * 	String[] variables = res.getVariables();
 * 	// go through all results
 * 	for (IResult r : res) {
 *
 * 		for (String var : variables) {
 * 			// get result values for each selected variable 
 * 			IResultValue[] values = r.getResultValues(var);
 * 			if (r.isBound(var))
 * 				for (int j = 0; j < values.length; j++)
 * 					System.out.println(var + " = " + values[j].getStringValue());
 * 			else
 * 				System.out.println(var + " = Not bound");
 * 		}
 * 	}
 * } catch (EngineException e) {
 * 	e.printStackTrace();
 * }</pre></code>
 * <br />
 * </li>
 * <li>
 * It is possible to know easily if a query has a result or if it fails with the function <code><pre>getSuccess()</pre></code>
 * </li></ul>
 * <br />
 * @author Olivier Corby
 */

public interface IResults extends Iterable<IResult> {
	
	/** Query with a SELECT clause */
	public final static int CL_SELECT =  ASTQuery.QT_SELECT;
	
	/** Query with a ASK clause */
	public final static int CL_ASK =  ASTQuery.QT_ASK;
	
	/** Query with a CONSTRUCT clause */
	public final static int CL_CONSTRUCT =  ASTQuery.QT_CONSTRUCT;
	
	/** Query with a DESCRIBE clause */
	public final static int CL_DESCRIBE = ASTQuery.QT_DESCRIBE;
	
	/**
	 * 
	 * @return boolean to say if result is successful: return true if there is one or more results, false otherwise.
	 */
	public boolean getSuccess();

	/**
	 * 
	 * @return enumerations&lt;IResult&gt; of arrays of IResult
	 */
	public Enumeration<IResult> getResults();
	
	public boolean includes(IResult r);
	
	public IResults union(IResults r);
	
	public IResults inter(IResults r);
	
	public IResults minus(IResults r);
	
	/**
	 * 
	 * @return an array of String that contains selected variables
	 */
	public String[] getVariables();
	
	// additional variables from e.g. Listener
	public void defVariable(String name);
	

	/**
	 * @return the number of graphs in the result
	 */
	public int size();
	
	public IResult get(int i);
	
	public void remove(IResult r);
	
	public void add(IResult r);
	
	public void add(int n, IResult r);

	/**
	 * 
	 * @return a String that represents the SPARQL result and some information provided by Corese<br />
	 * exemple:<br />
	 * <pre><code>
	 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
	 * &lt;cos:result xmlns:e='http://example/of/ontology#'&gt;
	 * 	&lt;cos:tquery&gt;&lt;![CDATA[select * where { ?x c:FamilyName ?n . filter (?n ~ 'pont'^^xsd:string) ?x c:age ?age . }]]&gt;&lt;/cos:tquery&gt;
	 * 	&lt;cos:info&gt;&lt;![CDATA[0.00 s for 1 projections]]&gt;&lt;/cos:info&gt;
	 * 	&lt;sparql xmlns='http://www.w3.org/2005/sparql-results#' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' &gt;
	 * 		&lt;head&gt;
	 * 			&lt;var name='x'/&gt;
	 * 			&lt;var name='n'/&gt;
	 * 			&lt;var name='age'/&gt;
	 * 		&lt;/head&gt;
	 * 		&lt;results distinct='false' sorted='false' &gt;
	 * 			&lt;result&gt;
	 * 				&lt;binding name='x'&gt;&lt;uri&gt;http://www-sop.inria.fr/personnel/dupont/&lt;/uri&gt;&lt;/binding&gt;
	 * 				&lt;binding name='n'&gt;&lt;literal datatype='http://www.w3.org/2001/XMLSchema#string'&gt;Dupont&lt;/literal&gt;&lt;/binding&gt;
	 * 				&lt;binding name='age'&gt;&lt;literal datatype='http://www.w3.org/2001/XMLSchema#integer'&gt;30&lt;/literal&gt;&lt;/binding&gt;
	 * 			&lt;/result&gt;
	 * 		&lt;/results&gt;
	 * 	&lt;/sparql&gt;
	 * &lt;/cos:result&gt;
	 * </code></pre>
	 */
	public String toCoreseResult();
	
	/**
	 * 
	 * @return a String that represents the result, with compliance to http://www.w3.org/TR/rdf-sparql-XMLres/<br /><br />
	 * exemple:<br />
	 * <pre><code>
	 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
	 * &lt;sparql xmlns='http://www.w3.org/2005/sparql-results#' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' &gt;
	 * 	&lt;head&gt;
	 * 		&lt;var name='x'/&gt;
	 * 		&lt;var name='n'/&gt;
	 * 		&lt;var name='age'/&gt;
	 * 		&lt;link href="http://where/to/find/more/info/about/the/query/2007-02-22-18717163.txt"/&gt;
	 * 	&lt;/head&gt;
	 * 	&lt;results distinct='false' sorted='false' &gt;
	 * 		&lt;result&gt;
	 * 			&lt;binding name='x'&gt;&lt;uri&gt;http://www-sop.inria.fr/personnel/dupont/&lt;/uri&gt;&lt;/binding&gt;
	 * 			&lt;binding name='n'&gt;&lt;literal datatype='http://www.w3.org/2001/XMLSchema#string'&gt;Dupont&lt;/literal&gt;&lt;/binding&gt;
	 * 			&lt;binding name='age'&gt;&lt;literal datatype='http://www.w3.org/2001/XMLSchema#integer'&gt;30&lt;/literal&gt;&lt;/binding&gt;
	 * 		&lt;/result&gt;
	 * 	&lt;/results&gt;
	 * &lt;/sparql&gt;
	 * </code></pre>
	 */
	public String toSPARQLResult();
	
	public String toJSON();
		
	/**
	 * Returns the clause of the query.<br />
	 * @return if the query was a SELECT, ASK, CONSTRUCT or DESCRIBE clause<br /> 
	 */
	public int getClause();	
	
	public boolean isSelect();
	
	public boolean isConstruct();
	
	public boolean isDescribe();
	
	public boolean isAsk();
	
	// construct where graph
	public Object getGraph();
}

