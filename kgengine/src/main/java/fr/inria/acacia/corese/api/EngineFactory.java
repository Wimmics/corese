package fr.inria.acacia.corese.api;

import java.util.HashMap;
import java.util.Vector;

import com.ibm.icu.util.StringTokenizer;

import fr.inria.edelweiss.kgengine.GraphEngine;

//import fr.inria.acacia.corese.Corese;
//import fr.inria.acacia.corese.event.EventListener;

/**
 * This interface create an instance of Corese.<br />
 * <ul>
 * 	<li>It is possible to create an engine and then load needed files: <br />
 * <code>EngineFactory ef = new EngineFactory();<br />
 * IEngine engine = ef.newInstance();<br /></code><br />
 * 	<ul>
 * 		<li>load an ontology (.rdfs or .owl): <code>engine.load(ontologyFilename);</code></li> 		
 * 		<li>load rules (.rul): <code>engine.load(rulesFilename);</code></li>
 * 		<li>apply rules: <code>engine.runRuleEngine();</code></li>
 * 		<li>load annotations (.rdf): <code>engine.load(annotationsFilename);</code></li> 		
 * 	</ul>
 * <i>Note:</i> It is also possible to load a whole directory instead of just a file with the function <code>loadDir(String directoryName)</code>
 * </li>
 * 	<li>It is also possible to set properties (propertyFile and datapath) and 
 * 	then create the engine, according to these properties:<br />
 * 	The "propertyFile" property is the name of the corese properties file.<br />
 * 	The "datapath" property is a datapath (same syntax as CLASSPATH); it contains a list of paths that will
 * 	be the directories where files declared in the corese properties will be searched (.rdfs, .owl, .rdf or .rul)<br />
 * 	The corese properties file has to be in one of these directories.<br />
 * 	Paths can be absolute or relative to the directory where Corese is launched. It cannot be a URI<br />
 * <code>
 * EngineFactory ef = new EngineFactory();<br />
 * ef.setPropetry("propertyFile", "corese.properties");<br />
 * ef.setPropetry("datapath", "/path/to/rdf;../path/to/prop/;/path/to/data");<br />
 * IEngine engine = ef.newInstance();</code></li>
 * </ul>
 * 
 * @author Virginie Bottollier
 */
public class EngineFactory {

	private String propertyFile = null;
	private String datapath = null;
	/** Vectors used to do "getURIFromPath()" in class RDFLoader: 
	 * if the regular expression (which corresponds to the first parameter of the hashtable) matches 
	 * the path given in parameter, we return the new path proposed in second argument of the Hashtable */
	private Vector<String> regexps = new Vector<String>();
	private Vector<String> uris = new Vector<String>();
	// to keep properties set before creating a corese
	private HashMap<String, String> propertiesHM = new HashMap<String, String>();	
	//Vector<EventListener> manager = new Vector<EventListener>();
	
	/** 
	 * Ontologies to load<br />
	 * example: "ontology/humans.rdfs owlOntology.owl" 
	 */
	public static final String ENGINE_SCHEMA = "corese.schema";    
	/** 
	 * Annotations to load<br />
	 * Example: "annot/data annot/data2 annot/testrdf.rdf" 
	 */
	public static final String ENGINE_DATA = "corese.data";
	/** 
	 * Rules to load<br />
	 * Example: rule/testrule.rul 
	 */
	public static final String ENGINE_RULE = "corese.rule";
	/** 
	 * If we have to run the rule Engine <br />
	 * Values: true or false
	 */
	public static final String ENGINE_RULE_RUN = "corese.rule.run";
	
	// create RDF/S metamodel in the RDF graph and 
	// create rdf:type relations from type inference (e.g. from rdfs:domain ...)
	public static final String ENGINE_METAMODEL = "corese.metamodel";
	

	/**
	 * Used to define global prefix for namespaces 
	 * that can be used in queries and rules
	 */
	public static final String ENGINE_NAMESPACE = IEngine.ENGINE_GUI_NAMESPACE;
	
	
	// By default, index on first argument of relations at creation time
	// if fullIndex true, index all arguments at creation time
	public static final String ENGINE_FULLINDEX = "corese.index.full";

	/**
	 * The path (absolute or relative) to indicate where to find the log4j configuration file<br />
	 * Example: "data/log4j.properties"
	 */
	public static final String ENGINE_LOG4J = "corese.log4j.conf";
	
	public static final String[] PROPERTIES = {
		ENGINE_SCHEMA, ENGINE_DATA, ENGINE_RULE, ENGINE_RULE_RUN, ENGINE_METAMODEL, 
		ENGINE_LOG4J, ENGINE_NAMESPACE, ENGINE_FULLINDEX
	};
	
	/**
	 * propertyFile = the name of a Corese properties file (with or without .properties))
	 */ 
	public static final String PROPERTY_FILE = "propertyfile";
	/**
	 * datapath = the path to resolve the resource paths (RDF(S), properties, icon, etc), 
	 * same syntax as CLASSPATH; the path is relative to the directory where Corese has 
	 * been launched
	 */
	public static final String DATAPATH = "datapath";
	
	/**
	 * Create an empty engine
	 */
	public IEngine newInstance() {
		GraphEngine engine = GraphEngine.create();
		
		String namespace = propertiesHM.get(ENGINE_NAMESPACE);
		if (namespace != null){
			StringTokenizer stk = new StringTokenizer(namespace);
			while (stk.hasMoreTokens()){
				engine.definePrefix(stk.nextToken(), stk.nextToken());
			}
		}
		
		return engine;
	}
	
	/**
	 * @param prop The name of the property to set. It can be ENGINE_SCHEMA, ENGINE_DATA, 
	 * ENGINE_RULE, ENGINE_RULE_RUN, or PROPERTY_FILE, DATAPATH <br />
	 * @param value The value of the property to set
	 */
	public void setProperty(String prop, String value) {
		if (prop.equals(PROPERTY_FILE))
			propertyFile = value;
		else if (prop.equals(DATAPATH))
			datapath = value;		
		else saveProperty(prop, value);
	}
	
	/**
	 * Get the value of a corese property if it exists
	 * @param name the name of the property we want to get. It can be ENGINE_SCHEMA, ENGINE_DATA, 
	 * ENGINE_RULE, ENGINE_RULE_RUN, ENGINE_LOG4J, or PROPERTY_FILE, DATAPATH <br />
	 * @return the value of this property in Corese if it exists (otherwise null)
	 */
	public String getProperty(String name) {
		if (name.equals(PROPERTY_FILE)) return propertyFile;
		else if (name.equals(DATAPATH)) return datapath;
		else return propertiesHM.get(name);
	}
	
	/**
	 * save properties set in a HashMap to set them when an instance of Corese will be created
	 * @param prop
	 * @param value
	 */
	private void saveProperty(String prop, String value) {
		propertiesHM.put(prop, value);
	}
	
	/**
	 * If a URL of a document that contains a graph matches a given pattern, this graph can be assigned a new source URI. <br />
	 * In the following example, every graph whith a name that contained "data.Annotation_Added_The" will be given 
	 * the URI "http://corese/data/Annotations.rdf".<br />
	 * Example:<br />
	 * <code>engineFact.setURIFromPath(".*data.Annotation_Added_The.*", "http://corese/data/Annotations.rdf");</code><br />
	 * Note: see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html">the Java definition of Pattern</a>
	 * @param regex The pattern of all RDF Graphs we want to retrieve
	 * @param uri The unique source we want to give to all these graphs
	 */
	public void setURIFromPath(String regex, String uri) {
		regexps.add(regex);
		uris.add(uri);
	}
	
//	public void addEventListener(EventListener el){
//		manager.add(el);
//	}
	
}
