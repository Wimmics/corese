package fr.inria.edelweiss.kgtool.load;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.ARP;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.RDFListener;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;


/**
 * Translate an RDF/XML document into a Graph
 * use ARP
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 * 
 * http://gfx.developpez.com/tutoriel/java/log4j/
 *
 */
public class Load 
	implements	StatementHandler, RDFListener, org.xml.sax.ErrorHandler,
	Loader {
	private static Logger logger = Logger.getLogger(Load.class);	

	static final String RULE = ".rul";
	static final String QUERY = ".rq";
	static final String[] SUFFIX = {".rdf", ".rdfs", ".owl", RULE, QUERY};
	static final String HTTP = "http://";
	static final String FTP  = "ftp://";
	static final String FILE = "file://";
	static final String[] protocols	= {HTTP, FTP, FILE};
	static final String OWL = "http://www.w3.org/2002/07/owl#";
	static final String IMPORTS = OWL + "imports";

	Graph graph;
	RuleEngine engine;
	QueryEngine qengine;
	Node src, cur;
	Hashtable<String, String> blank, loaded;
	ArrayList<String> exclude;
	String pattern, lbase;
	
	String source;
	
	boolean debug = false;
	
	int nb = 0;
	
	Load(Graph g){
		graph = g;
		blank = new Hashtable<String, String> ();
		loaded = new Hashtable<String, String> ();
		exclude = new ArrayList<String>();
	}
	
	public static Load create(Graph g){
		return new Load(g);
	}
	
	public void reset(){
		//COUNT = 0;
	}
	
	public void exclude(String ns){
		exclude.add(ns);
	}
	
	public void setEngine(RuleEngine eng){
		engine = eng;
	}
	
	public RuleEngine getRuleEngine(){
		return engine;
	}
	
	public void setEngine(QueryEngine eng){
		qengine = eng;
	}
	
	public void setPattern(String pat){
		pattern = pat;
	}
	
	public QueryEngine getQueryEngine(){
		return qengine;
	}
	
	public void setDebug(boolean b){
		debug = b;
	}
	
		
	String uri(String name){
		if (! isURL(name)){
			// use case: relative file name
			// otherwise URI is not correct (for ARP)
			name = new File(name).getAbsolutePath();
			// for windows
			name = name.replace('\\','/');
			name = FILE + name;
		}
		return name;
	}
	
	boolean isURL(String path){
		try {
			new URL(path);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
	
	public void load(String path, String base, String source){
		if (! suffix(path)) return ;
		
		if (path.endsWith(RULE)){
			loadRule(path, base);
			return;
		}
		if (path.endsWith(QUERY)){
			loadQuery(path, base);
			return;
		}
		
		Reader read = null;

		try {
			if (isURL(path)){
				URL url = new URL(path);
				read = new InputStreamReader(url.openStream());
				}
			else {
				read = new FileReader(path);
			}
		} 
		catch (Exception e){
			error(e.getMessage());
			error("URL/File load error: " + path);
			return;
		}
				
		if (base != null){
			// ARP needs an URI for base
			base = uri(base);
		}
		else {
			base = uri(path);
		}
		
		if (source == null){
			source = base;
		}
		
		if (pattern != null){
			base   = process(base);
			source = process(source);
		}
		
		load(read, path, base, source);
	}
	
	void error(Object mes){
		logger.error(mes);
	}
	
	
	// not for rules
	public void load(InputStream stream, String source){
		if (source == null) source = Entailment.DEFAULT;
		Reader read = new InputStreamReader(stream);
		load(read, source, source, source);
	}
	
	void load(Reader read,  String path, String base, String source){
		lbase = base;
		if (pattern != null) lbase = clean(base);
		
		graph.setUpdate(true);
		src = graph.addGraph(source);
		cur = src;
		blank.clear();
		ARP arp = new ARP();
		try {
			arp.setRDFListener(this);
		}
		catch (java.lang.NoSuchMethodError e){
			
		}
		arp.setStatementHandler(this);
		try {
			arp.load(read, base);
			read.close();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			error(e.getMessage() + " " + path); 
			error(arp.getLocator());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			error(e.getMessage() + " " + path); 
			error(arp.getLocator());
		}
	}
	
	
	void loadRule(String path, String src){
		if (engine == null){
			engine = RuleEngine.create(graph);
		}
		RuleLoad load = RuleLoad.create(engine);
		load.load(path);
	}
	
	void loadQuery(String path, String src){
		if (qengine == null){
			qengine = QueryEngine.create(graph);
		}
		QueryLoad load = QueryLoad.create(qengine);
		load.load(path);
	}
	
	
	boolean suffix(String path){
		if (isURL(path)) return true;
		
		for (String suf : SUFFIX){
			if (path.endsWith(suf)){
				return true;
			}
		}
		return false;
	}
	
	public void load(String path){
		load(path, null);
	}
	
	public void load(String path, String src){
		File file=new File(path);
		if (file.isDirectory()){
			path += File.separator;
			for (String f : file.list()){
				String name = path + f;
				load(name, src);
			}
		}
		else {
			if (debug){
				logger.debug("** Load: " + nb + " " + path);
			}
			load(path, src, null);
		}
	}
	
	
	boolean accept(String pred){
		if (exclude.size() == 0) return true;
		for (String ns : exclude){
			if (pred.startsWith(ns)){
				return false;
			}
		}
		return true;
	}
	
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		if (accept(pred.getURI())){
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getLiteral(pred, lit);
			if (value == null) return;
			EdgeImpl edge 	= EdgeImpl.create(cur, subject, predicate, value);
			process(cur, edge);
		}
	}
	
	public void statement(AResource subj, AResource pred, AResource obj) {
		if (accept(pred.getURI())){			
			
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getNode(obj);
			EdgeImpl edge 	= EdgeImpl.create(cur, subject, predicate, value);
			process(cur, edge);
			
			if (pred.getURI().equals(IMPORTS)){
				imports(obj.getURI());
			}
		}
	}
	
	void imports(String uri){
		if (! loaded.contains(uri)){
			loaded.put(uri, uri);
			load(uri);
		}
	}
	
	void process(Node gNode, EdgeImpl edge){
		Entity ent = graph.addEdge(edge);
	}
	
	
	Node getLiteral(AResource pred, ALiteral lit){
		String lang = lit.getLang();
		String datatype = lit.getDatatypeURI();
		if (lang == "") lang = null;
		return graph.addLiteral(pred.getURI(), lit.toString(), datatype, lang);
	}
	
	Node getProperty(AResource res){
		return graph.addProperty(res.getURI());		
	}
	
	Node getNode(AResource res){
		if (res.isAnonymous()){
			return graph.addBlank(getID(res.getAnonymousID()));
		}
		else {
			return graph.addResource(res.getURI());
		}		
	}
	
	String getID(String b){
		String id = blank.get(b);
		if (id == null){
			id = graph.newBlankID();
			blank.put(b, id);
		}
		return id;
	}
	

	@Override
	public void error(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Process cos:graph statement
	 * TODO:
	 * generate different blanks in different graphs
	 */
	public void setSource(String s) {
		if (s == null){
			cur = src;
			return;
		}
		
		if (pattern != null){
			s = lbase + s;
		}
		if (! cur.getLabel().equals(s)){
			cur = graph.addGraph(s);
		}
	}
	
	// remove file name after last "/"
	String clean(String str){
		if (str.endsWith(File.separator) || str.endsWith("#")) return str;
		int ind = str.lastIndexOf(File.separator);
		str = str.substring(0, ind+1);
		return str;
	}
	
	// remove pattern from str
	String process(String str){
		int index = str.indexOf(pattern);
		if (index != -1){
			str = str.substring(0, index) + str.substring(index + pattern.length());
		}
		return str;
	}

	public String getSource() {
		return source;
	}
	

}
