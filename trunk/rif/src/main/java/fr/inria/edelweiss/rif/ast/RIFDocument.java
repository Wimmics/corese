package fr.inria.edelweiss.rif.ast;

import java.io.File;
import java.util.HashMap;


/** This class embeds the abstract syntax tree of a parsed RIF document.
 * It also stores several document-relative data like imported namespaces...etc. 
 * @author cfollenf */
public abstract class RIFDocument {

	/** Concrete AST, starting with root rules group. This field is <code>null</code> until compiled with 
	 * {@link #compilePS() compilePS()} or {@link #compileXML() compileXML()} */
	protected Group payload ;
	
	/** Source text of a RIF-PS document */
	protected String rifDocStr ;
	
	/** Source file of a RIF document (PS or XML) */
	protected File rifDocFile ;
	
	/** Proxy to the application namespaces manager */
	private NSManager NSMParser ;
	
	/** App-specific namespaces */
	private String defaultNamespaces ;

	/** Imported RDF/OWL/DL graphs */
	private HashMap<String, String> imports ;
	
	/** Metadata with global document scope */
	private Annotation meta ;
	
	/** Constructor for reading input from a string */
	protected RIFDocument(String doc) {
		this.rifDocStr = doc ;
	}

	/** Constructor for reading input from a file */
	protected RIFDocument(File doc) {
		this.rifDocFile = doc ;
	}
	
	public Group getPayload() {
		return this.payload ;
	}
	
	/** Use this method to bind a manually created RIF AST (i.e. not built by JavaCC or XML syntactic tree).<br>
	 *  Don't forget to add prefixes and base declarations when relevant, using respectively
	 *  {@link #addPrefixedNamespace(String, String) addPrefixedNamespace(String, String)}
	 *   and {@link #setBaseNamespace(String) setBaseNamespace(String)}.
	 * @param g Root of the AST to bind */
	public void setPayload(Group g) {
		this.payload = g ;
	}
	
	public NSManager getNSMParser() {
		return this.NSMParser == null ? this.NSMParser = NSManager.create(defaultNamespaces) : this.NSMParser ;
	}
	
	public void setDefaultNamespaces(String defaultNamespaces) {
		this.defaultNamespaces = defaultNamespaces ;
	}
	
	/** Adds a new prefix to be used in the document's QNames, expanded to the specified IRI.
	 * Here are a few examples of (prefix, expansion) pairs :
	 * <ul><li>(<code>bks</code>, <code>http://example.org/books#</code>)</li>
	 * <li>(<code>auth</code>, <code>http://example.org/authors#</code>)</li>
	 * <li>(<code>rdf</code>, <code>http://www.w3.org/1999/02/22-rdf-syntax-ns#</code>)</li>
	 * </ul>
	 * @param prefix the new prefix, usually an alphanumeric string
	 * @param namespace the expanded namespace IRI, ending up with locator */
	public void addPrefixedNamespace(String prefix, String namespace) {
		this.getNSMParser().defNamespace(prefix, namespace) ;
	}
	
	
	/** Sets the base namespace to be used in the document (for expanding relatives IRIs into full IRIs in the document)
	 * @param namespace An absolute IRI */
	public void setBaseNamespace(String namespace) {
		this.getNSMParser().setBase(namespace) ;
	}
	
	/** When RIF document has import directive(s), all the remote graph must be
	 * added into the facts base when processing the document 
	 * @see <a href="http://www.w3.org/TR/rif-bld/#def-bld-imported-doc">Imported Documents definition</a> */
	public void addImportedDocument(String location, String profile) {
		if(this.imports == null) this.imports = new HashMap<String, String>() ;
		imports.put(location, profile) ;
	}

	public abstract void compile() ;
	
	/** Used when there is a document-scope annotation */
	public void setMeta(Annotation meta) {
		this.meta = meta;
	}

	public Annotation getMeta() {
		return meta;
	}
	
}
