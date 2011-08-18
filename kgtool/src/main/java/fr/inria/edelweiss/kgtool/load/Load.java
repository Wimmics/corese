package fr.inria.edelweiss.kgtool.load;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.ARP;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.RDFListener;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.edelweiss.kgraph.api.Loader;
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
 */
public class Load 
	implements	StatementHandler, RDFListener, org.xml.sax.ErrorHandler,
	Loader {
	private static Logger logger = Logger.getLogger(Load.class);	

	static final String RULE 	= ".rul";
	static final String QUERY 	= ".rq";
	static final String UPDATE 	= ".ru";
	static final String[] SUFFIX = {".rdf", ".rdfs", ".owl", RULE, QUERY, UPDATE};
	static final String HTTP = "http://";
	static final String FTP  = "ftp://";
	static final String FILE = "file://";
	static final String[] protocols	= {HTTP, FTP, FILE};
	static final String OWL = "http://www.w3.org/2002/07/owl#";
	static final String IMPORTS = OWL + "imports";

	Graph graph;
	RuleEngine engine;
	QueryEngine qengine;
	Hashtable<String, String>  loaded;
	LoadPlugin plugin;
	Build build;
	
	String source;
	
	boolean debug = false,
	hasPlugin = false;
	
	int nb = 0;
	
	Load(Graph g){
		graph = g;
		loaded = new Hashtable<String, String> ();
		build = BuildImpl.create(g);
	}
	
	public static Load create(Graph g){
		return new Load(g);
	}
	
	public void reset(){
		//COUNT = 0;
	}
	
	public void exclude(String ns){
		build.exclude(ns);
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
	
	public void setPlugin(LoadPlugin p){
		if (p != null){
			plugin = p;
			hasPlugin = true;
		}
	}
	
	public void setBuild(Build b){
		if (b != null){
			build = b;
		}
	}
	
	public Build getBuild(){
		return build;
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
	
	public void load(String path){
		load(path, null);
	}
	
	public void loadWE(String path) throws LoadException{
		loadWE(path, null);
	}
	
	public void load(String path, String src){
		File file=new File(path);
		if (file.isDirectory()){
			path += File.separator;
			for (String f : file.list()){
				if (! suffix(f)) continue ;
				String name = path + f;
				load(name, src);
			}
		}
		else {
			if (debug){
				logger.debug("** Load: " + nb + " " + path);
			}
			try {
				load(path, src, null);
			} catch (LoadException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void loadWE(String path, String src) throws LoadException{
		File file=new File(path);
		if (file.isDirectory()){
			path += File.separator;
			
			for (String f : file.list()){
				if (! suffix(f)) continue ;
				String name = path + f;
				loadWE(name, src);
			}
		}
		else {
			if (debug){
				logger.debug("** Load: " + nb + " " + path);
			}
			load(path, src, null);
		}
	}
	
	
	
	public void load(String path, String base, String source) throws LoadException {
		//if (! suffix(path)) return ;
		
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
			throw LoadException.create(e);
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
		
		load(read, path, base, source);
	}
	
	void error(Object mes){
		logger.error(mes);
	}
	
	
	// not for rules
	public void load(InputStream stream, String source) throws LoadException{
		if (source == null) source = Entailment.DEFAULT;
		Reader read = new InputStreamReader(stream);
		load(read, source, source, source);
	}
	
	
	void load(Reader read,  String path, String base, String src) throws LoadException {
		if (hasPlugin){
			src  = plugin.statSource(src);
			base = plugin.statBase(base);
		}

		source = src;
		build.setSource(source);
		build.start();
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
			throw LoadException.create(e, arp.getLocator());
		} 
		catch (IOException e) {
			throw LoadException.create(e, arp.getLocator());
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
		//if (isURL(path)) return true;
		
		for (String suf : SUFFIX){
			if (path.endsWith(suf)){
				return true;
			}
		}
		return false;
	}
	
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		build.statement(subj, pred, lit);
	}
	
	public void statement(AResource subj, AResource pred, AResource obj) {
		build.statement(subj, pred, obj);
		if (pred.getURI().equals(IMPORTS)){
			imports(obj.getURI());
		}
	}
	
	void imports(String uri){
		if (! loaded.contains(uri)){
			loaded.put(uri, uri);
			load(uri);
		}
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
			//cur = src;
			build.setSource(source);
			return;
		}
		
		if (hasPlugin){
			s = plugin.dynSource(s);
		}
		build.setSource(s);
//		if (! cur.getLabel().equals(s)){
//			cur = build.getGraph(s);
//		}
	}
	
	
	

	public String getSource() {
		return source;
	}
	

}
