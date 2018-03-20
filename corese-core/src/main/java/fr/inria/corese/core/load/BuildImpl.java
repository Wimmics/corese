package fr.inria.corese.core.load;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;

/**
 * Graph creation
 * Methods are public, Design to be refined
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 * 
 */
public class BuildImpl implements Build {
	static final String STAR = "*";
	
	Graph graph;
	Node gnode;
	ArrayList<String> exclude;
	Hashtable<String, String> blank;
	boolean skip = false;
        private int limit = Integer.MAX_VALUE;
        
    private String resource, source;
    private Node node;

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
	
        @Override
	public void statement(AResource subj, AResource pred, ALiteral lit) {
		if (accept(pred.getURI())){
			Node subject 	= getSubject(subj);
			Node predicate 	= getProperty(pred);
			Node value 	= getLiteral(pred, lit);
			if (value == null) return;
			Entity edge 	= getEdge(gnode, subject, predicate, value);
			process(gnode, edge);
		}
	}
	
        @Override
	public void statement(AResource subj, AResource pred, AResource obj) {
		if (accept(pred.getURI())){						
			Node subject 	= getSubject(subj);
			Node predicate 	= getProperty(pred);
			Node value 	= getNode(obj);
			Entity edge 	= getEdge(gnode, subject, predicate, value);
			process(gnode, edge);
		}
	}
	
        @Override
	public void setSource(String src){
            if (source == null || ! src.equals(source)){
                source = src;
		gnode = graph.addGraph(src);
            }
	}
	
	public void setSkip(boolean b){
		skip = b;
	}
	
        @Override
	public void exclude(String ns){
		if (ns == null){
			exclude.clear();
		}
		else if (ns.equals(STAR)){
			skip = true;
		}
		else {
			exclude.add(ns);
		}
	}
	
        @Override
	public void start(){
		//graph.setUpdate(true);
                graph.getEventManager().start(Event.LoadAPI);
		blank.clear();
	}
        
        @Override
        public void finish(){
                graph.getEventManager().finish(Event.LoadAPI);
            
        }
	
	public boolean accept(String pred){
		if (skip || graph.size() > limit) {
                    return false;
                }
		if (exclude.size() == 0) return true;
		for (String ns : exclude){
			if (pred.startsWith(ns)){
				return false;
			}
		}
		return true;
	}
	
	
	public void process(Node gNode, Entity edge){
		Entity ent = graph.addEdge(edge);
	}
	

	public Entity getEdge(Node source, Node subject, Node predicate, Node value){
		if (source == null) source = graph.addDefaultGraphNode();
		
		return graph.create(source, subject, predicate, value);
		
	}
	
	
	public Node getLiteral(AResource pred, ALiteral lit){
		String lang = lit.getLang();
		String datatype = lit.getDatatypeURI();
		if (lang == ""){
                    lang = null;
                }
		return graph.addLiteral(pred.getURI(), lit.toString(), datatype, lang);
	}
               	
	public Node getProperty(AResource res){
		return graph.addProperty(res.getURI());		
	}
	
       Node getSubject(AResource res) {
           if (res.isAnonymous()) {
               return graph.addBlank(getID(res.getAnonymousID()));
           } else {
               return getResource(res.getURI());
           }
        }
        
	public Node getNode(AResource res){
		if (res.isAnonymous()){
			return graph.addBlank(getID(res.getAnonymousID()));
		}
		else {
			return graph.addResource(res.getURI());
		}		
	}
        
        Node getResource(String uri){
            if (resource == null || ! resource.equals(uri)){
                resource = uri;
                node = graph.addResource(uri);
            }
            return node;
        }
	
	public String getID(String b){
		String id = blank.get(b);
		if (id == null){
			id = graph.newBlankID();
			blank.put(b, id);
		}
		return id;
	}

    /**
     * @return the limit
     */
        @Override
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
        @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int nbBlank() {
        return blank.size();
    }

}
