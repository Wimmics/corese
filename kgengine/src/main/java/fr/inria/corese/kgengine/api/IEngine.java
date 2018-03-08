package fr.inria.corese.kgengine.api;

import java.io.InputStream;


import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 * This class encapsulate the Corese class, which manages the Corese RDF engine<br />
 * @author Virginie Bottollier
 */
public interface IEngine {
	
	/**
	 * Predefined prefix associated with a namespace<br />
	 * Example: "a http://www.inria.fr/acacia# i http://www.inria.fr#"
	 */
	public static final String ENGINE_GUI_NAMESPACE = "corese.gui.namespace";
	
	/**
	 * Maximum number of result returned after possibly grouping projections<br />
	 * Values: Integer
	 */
	public static final String ENGINE_RESULT_MAX = "corese.result.max";
	/**
	 * Maximum number of projections computed to answer a query<br />
	 * Values: Integer
	 */
	public static final String ENGINE_PROJECTION_MAX = "corese.result.projection.max";

	public static final String ENGINE_LENGTH_MAX = "corese.length.max";

	
	/**
	 * When this property is set to true, Corese groups projections that share the same first concept into one result.<br />
	 * Values: true or false
	 */
	public static final String ENGINE_RESULT_JOIN = "corese.result.join";
	
	/**
	 * Load a file or a whole directory (that can contain RDF (.rdf), RDFS (.rdfs), OWL (.owl) or 
	 * RULE (.rul) files) into Corese.<br />
	 * <i>Note</i>: files will be loaded in any order, whereas we should load 1) rdfs or owl 
	 * 2) rul 3) rdf<br />
	 * <i>Note</i>: files that do not end with .rdf, .owl, .rdf or .rul are ignored.<br /> 
	 * It would be better to have different directories for each kind of file.<br />
	 * Path can be absolute or relative.
	 *
	 * @param path where to find the document to load
	 * @throws <code>EngineException<code>
	 */
	public void load(String path) throws EngineException;
	
	public boolean validate(String path) ;

	

	/**
	 * Load a file or a whole directory (that can contain RDF (.rdf), RDFS (.rdfs), OWL (.owl) or 
	 * RULE (.rul) files) into Corese.<br />
	 * <i>Note</i>: files will be loaded in any order, whereas we should load 1) rdfs or owl 
	 * 2) rul 3) rdf<br />
	 * <i>Note</i>: files that do not end with .rdf, .owl, .rdf or .rul are ignored.<br /> 
	 * It would be better to have different directories for each kind of file.<br />
	 * Path can be absolute or relative.
	 *
	 * @param path where to find the document to load
	 * @throws <code>EngineException<code>
	 * @deprecated use load(String path) instead
	 */
	public void loadDir(String path) throws EngineException;
	
	/**
	 * As load(String path), load a whole directory but avoid to load files/directories specified in path2
	 * @param path where to find the document to load
	 * @param exlude: exclusion list (regex)
	 * @param include: inclusion list (regex)
	 */
	public void load(String path, String exclude) throws EngineException;

	public void load(String path, String include, String exclude) throws EngineException;
	
	/**
	 * bexclude=true:
	 * load path but reject exclude except if it match include
	 * bexclude=false:
	 * load path that match include and that not match exclude
	 * default is bexclude=false
	 */	
	public void load(String path, String include, String exclude, boolean bexclude) throws EngineException;

	/**
	 * load from rdf/xml string
	 * source may be null (default source will be chosen)
	 */ 
	public void loadRDF(String rdf, String source) throws EngineException;
	
	public void loadRDFRule(String rdf, String source) throws EngineException;
	
	public void load (InputStream rdf, String source) throws EngineException;

	/**
	 * Load a triple written in N-Triples
	 * ex: engine.loadTriple("<http://example.org/resource123> <http://example.org/property> <http://example.org/resource2>");
	 * @param triple
	 * @throws EngineException
	 */
	public void loadTriple(String triple) throws EngineException;
	
	/**
	 * Create triples from the RDF file/directory (located at pathToRDF) and print them as NTriple 
	 * in the file/directory located at pathToTriples to save them<br />
	 * .rdf will be transformed into .nt<br />
	 * .rdfs will be transformed into .nts
	 * @param pathToRDF The RDF file/directory that contains triples to save
	 * @param pathToTriples The NTriples file/directory where NTriples will be printed
	 * @throws EngineException
	 */
	public void translate(String pathToRDF, String pathToTriples) throws EngineException;

	/**
	 * Apply rule base on graph base<br />
	 * It should be called only when rules have been previously loaded 
	 */
	public boolean runRuleEngine();
	
	public boolean runRuleEngine(boolean rdf, boolean owl);
	
	public void runQueryEngine();

	/**
	 * Prove by backward chaining inference rules (suffix .brul)
	 */
	public IResults SPARQLProve(String query) throws EngineException;
	
	public IResults SPARQLProve(ASTQuery ast) throws EngineException;
	

	/**
	 * Query Corese in the SPARQL syntax <br />
	 * 
	 * @param query The query string to test
	 * @return the IResults which corresponds to the Result of the query
	 * @throws EngineException
	 */
	
	// query & update
	public IResults SPARQLQuery(String query) throws EngineException;
	
	// query only
	public IResults query(String query) throws EngineException;

	// update 
	public IResults update(String query) throws EngineException;

	public IResults SPARQLQuery(ASTQuery ast) throws EngineException;

	
	/**
	 * Return first value of first variable
	 */
	public IResultValue SimpleQuery(String query) throws EngineException;

	/**
	 * Query Corese in the SPARQL syntax with a model<br />
	 * 
	 * @param query The query string to test
	 * @param model A model to give the answers to get:gui values
	 * @return the IResults which corresponds to the Result of the query
	 * @throws EngineException
	 */
	public IResults SPARQLQuery(String query, IModel model) throws EngineException;
	
	public IResults SPARQLQuery(String query, String[] from, String[] named) 
	throws EngineException;
	
	public ASTQuery parse(String query) throws EngineException;
	//public QueryGraph compile(ASTQuery ast) throws EngineException;
	
	public IResults SPARQLQueryLoad(String query) 
	throws EngineException;

	/**
	 * Validate the query. <br />
	 * @param query The query string to test
	 * @return true if the query is well written (no parsing exception, no compilation exception)
	 * @throws EngineException
	 */
	public boolean SPARQLValidate(String query) throws EngineException;

	/**
	 * Set the value of a corese property<br />
	 * Examples:<br />
	 * <code>setProperty(ENGINE_DATA, "annot/data annot/data2 annot/testrdf.rdf")</code><br />
	 * <code>setProperty(ENGINE_DEBUG, "false")</code><br />
	 * For more informations about corese properties, see <a href="http://www-sop.inria.fr/acacia/soft/corese/manual/#coreseproperties">http://www-sop.inria.fr/acacia/soft/corese/manual/#coreseproperties</a>
	 * @param name the name of the property we want to set
	 * @param value the value (String) that we want for this property in Corese
	 */
	public void setProperty(String name, String value);
	
	/**
	 * Get the value of a corese property if it exists
	 * @param name the name of the property we want to get
	 * @return the value of this property in Corese if it exists (otherwise null)
	 */
	public String getProperty(String name);

	/**
	 * Print an empty result in the SPARQL result format
	 * @param res the result of the query
	 * @return an empty result in the SPARQL result format
	 */
	public String emptyResult(IResults res);

	/**
	 * Create a typed resource in the current graph (create a triple "resourceId rdf:type type")
	 * @param type the URI of the type of the resource (ex: "http://www.inria.fr/acacia/corese#Person")
	 * @param resourceId the resource Id (ex: "http://www.inria.fr/acacia/corese#John") 
	 * Note: it must be a URI 
	 * @param source the source of the resource, can be null (then the default source will be used)
	 * @throws EngineInitException
	 */
	//public IResource createResource(String type, String resourceId, String source) throws EngineInitException;
	
	/**
	 * Create a relation in the current graph<br/>
	 * Note: if a blank node with the same name is created in different sources, only relations created in the first source will be taken  
	 * @param resource must be a URI or a BlankNode (ex: "http://www.inria.fr/acacia/corese#John" or "_:b1")
	 * @param property must be a URI, note: the property must already exists (ex: "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
	 * @param value must be a URI or a BlankNode (ex: "http://www.inria.fr/acacia/corese#Person" or "_:b1")
	 * @param source the source of the relation, can be null (then the default source will be used)
	 * @throws EngineInitException
	 */
	//public IRelation createRelation(String resource, String property, String value, String source) throws EngineInitException;
	
	/**
	 * Create a literal relation in the current graph
	 * Note: if a blank node with the same name is created in different sources, only relations created in the first source will be taken
	 * @param resource must be a URI or a BlankNode (ex: "http://www.inria.fr/acacia/corese#John" or "_:b1")
	 * @param property must be a URI, note: the property must already exists (ex: "http://www.w3.org/2000/01/rdf-schema#label")
	 * @param valueLiteral must be a literal (examples: "My name is John", "Person"@en", "123"^^xsd:integer)
	 * @param source the source of the relation, can be null (then the default source will be used)
	 * @throws EngineInitException
	 */	
//	public IRelation createAttribute(String resource, String property, String literalValue, String source) throws EngineInitException;

	public String getQuery(String uri);
	
	// type check event listener
	

	
	void start();

	public void runPipeline(String name);

	
}
