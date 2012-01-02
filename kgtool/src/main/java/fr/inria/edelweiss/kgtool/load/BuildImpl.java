package fr.inria.edelweiss.kgtool.load;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeCore;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * Graph creation
 * Methods are public, Design to be refined
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 * 
 */
public class BuildImpl implements Build {
	
	Graph graph;
	Node source;
	ArrayList<String> exclude;
	Hashtable<String, String> blank;

	public BuildImpl(){
	}
	
	public BuildImpl(Graph g){
		graph = g;
		blank = new Hashtable<String, String> ();
		exclude = new ArrayList<String>();
	}
	
	public static BuildImpl create(Graph g){
		return new BuildImpl(g);
	}
	
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		if (accept(pred.getURI())){
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getLiteral(pred, lit);
			if (value == null) return;
			EdgeImpl edge 	= getEdge(source, subject, predicate, value);
			process(source, edge);
		}
	}
	
	public void statement(AResource subj, AResource pred, AResource obj) {
		if (accept(pred.getURI())){			
			
			Node subject 	= getNode(subj);
			Node predicate 	= getProperty(pred);
			Node value 		= getNode(obj);
			EdgeImpl edge 	= getEdge(source, subject, predicate, value);
			process(source, edge);
		}
	}
	
	public void setSource(String src){
		source = graph.addGraph(src);
	}
	
	public void exclude(String ns){
		exclude.add(ns);
	}
	
	public void start(){
		graph.setUpdate(true);
		blank.clear();
	}
	
	public boolean accept(String pred){
		if (exclude.size() == 0) return true;
		for (String ns : exclude){
			if (pred.startsWith(ns)){
				return false;
			}
		}
		return true;
	}
	
	
	public void process(Node gNode, EdgeImpl edge){
		Entity ent = graph.addEdge(edge);
	}
	

	public EdgeImpl getEdge(Node source, Node subject, Node predicate, Node value){
		if (source == null) source = graph.addGraph(Entailment.DEFAULT);
		
		return graph.create(source, subject, predicate, value);
		
	}
	
	
	public Node getLiteral(AResource pred, ALiteral lit){
		String lang = lit.getLang();
		String datatype = lit.getDatatypeURI();
		if (lang == "") lang = null;
		return graph.addLiteral(pred.getURI(), lit.toString(), datatype, lang);
	}
	
	public Node getProperty(AResource res){
		return graph.addProperty(res.getURI());		
	}
	
	public Node getNode(AResource res){
		if (res.isAnonymous()){
			return graph.addBlank(getID(res.getAnonymousID()));
		}
		else {
			return graph.addResource(res.getURI());
		}		
	}
	
	public String getID(String b){
		String id = blank.get(b);
		if (id == null){
			id = graph.newBlankID();
			blank.put(b, id);
		}
		return id;
	}
	
	

}
