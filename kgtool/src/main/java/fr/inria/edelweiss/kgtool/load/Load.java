package fr.inria.edelweiss.kgtool.load;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Hashtable;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.ARP;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;


/**
 * Translate an RDF/XML document into a Graph
 * use ARP
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class Load 
	implements	StatementHandler, org.xml.sax.ErrorHandler,
	Loader {
	
	static final String RULE = ".rul";
	static final String[] SUFFIX = {".rdf", ".rdfs", ".owl", RULE};
	static final String HTTP = "http://";
	static final String FTP  = "ftp://";
	static final String FILE = "file://";
	
	static final String OWL = "http://www.w3.org/2002/07/owl#";
	static final String IMPORTS = OWL + "imports";

	Graph graph;
	RuleEngine engine;
	Node src;
	Hashtable<String, String> blank, loaded;
	
	boolean debug = false;
	
	int nb = 0;
	
	Load(Graph g){
		graph = g;
		blank = new Hashtable<String, String> ();
		loaded = new Hashtable<String, String> ();
	}
	
	public static Load create(Graph g){
		return new Load(g);
	}
	
	public void reset(){
		//COUNT = 0;
	}
	
	public void setEngine(RuleEngine eng){
		engine = eng;
	}
	
	public void setDebug(boolean b){
		debug = b;
	}
	
		
	String uri(String name){
		if (! isURL(name)){
			name = FILE + name;
		}
		return name;
	}
	
	boolean isURL(String path){
		return path.startsWith(HTTP) || path.startsWith(FILE) || 
			   path.startsWith(FTP);
	}
	
	public void load(String path, String base, String source){
		if (! suffix(path)) return ;
		if (path.endsWith(RULE)){
			loadRule(path, base);
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
			System.out.println(e.getMessage());
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
		
		load(read, path, base, source);
	}
	
	// not for rules
	public void load(InputStream stream, String source){
		if (source == null) source = Entailment.DEFAULT;
		Reader read = new InputStreamReader(stream);
		load(read, source, source, source);
	}
	
	void load(Reader read,  String path, String base, String source){
		
		graph.setUpdate(true);
		src = graph.addGraph(source);
		blank.clear();
		ARP arp = new ARP();
		arp.setStatementHandler(this);
		try {
			arp.load(read, base);
			read.close();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage() + " " + path); 
			System.out.println(arp.getLocator());
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage() + " " + path); 
			System.out.println(arp.getLocator());
		}
	}
	
	
	void loadRule(String path, String src){
		if (engine == null){
			engine = RuleEngine.create(graph);
		}
		RuleLoad load = RuleLoad.create(engine);
		load.load(path);
	}
	
	
	boolean suffix(String path){
		if (path.startsWith(HTTP)) return true;
		
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
				System.out.println("** Load: " + nb + " " + path);
			}
			load(path, src, null);
		}
	}
	
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		if (true){
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getLiteral(pred, lit);
			if (value == null) return;
			EdgeImpl edge 	= EdgeImpl.create(src, subject, predicate, value);
			process(src, edge);
		}
	}
	
	public void statement(AResource subj, AResource pred, AResource obj) {
		if (true){
			
			if (pred.getURI().equals(IMPORTS)){
				imports(obj.getURI());
				return;
			}
			
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getNode(obj);
			EdgeImpl edge 	= EdgeImpl.create(src, subject, predicate, value);
			process(src, edge);
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

}
