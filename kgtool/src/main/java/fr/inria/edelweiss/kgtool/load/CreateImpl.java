package fr.inria.edelweiss.kgtool.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.acacia.corese.triple.api.Creator;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;

/**
 * 
 * Create Edge on the fly for Turtle parser
 * 
 * @author Olivier Corby, INRIA 2012
 * 
 */
public class CreateImpl implements Creator {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CreateImpl.class);
    
    HashMap<String, String> blank;
    NSManager nsm;
    Graph graph;
    Node source;
    Stack stack;
    String base;
    private boolean renameBlankNode = true;
    int limit = Integer.MAX_VALUE;
    private String resource;
    private Node node;
    Load load;
	
	class Stack extends ArrayList<Node> {
		
		Node pop(){
			if (size()>0){
				return remove(size()-1);
			}
			return null;
		}
		
	}
	
	CreateImpl(Graph g, Load ld){
		graph = g;
                load = ld;
		blank = new HashMap<String, String>();
		nsm = NSManager.create();
		stack = new Stack();
	}

	public static CreateImpl create(Graph g, Load ld){
		return new CreateImpl(g, ld);
	}
        
    @Override
        public void start(){
            graph.setUpdate(true);
        }
        
    @Override
        public void finish(){
        }
	
    @Override
	public void setLimit(int max){
		limit = max;
	}
	
	// init
	// TODO: check 
	public void graph(String src){
		source = graph.addGraph(src);
	}
	
	
    @Override
	public void graph(Atom src){
		stack.add(source);
		source = graph.addGraph(src.getLabel());
	}
	
    @Override
	public void endGraph(Atom src){
		source = stack.pop();
	}
	
    @Override
	public boolean accept(Atom subject, Atom property, Atom object){
//		if (graph.size() / 10000 == graph.size()/ 10000.0){
//			System.out.println(graph.size());
//		}
		if (graph.size() < limit){
			return true;
		}
		return false;
	}

    @Override
	public void triple(Atom subject, Atom property, Atom object) {		
		if (source == null){
			source = graph.addDefaultGraphNode();                      
		}
		Node s = getSubject(subject);
		Node p = getProperty(property);
		Node o;
		if (object.isLiteral()){
			o = getLiteral(property, object.getConstant());
		}
		else {
			o = getNode(object);
		}
		
		Entity e = graph.create(source, s, p, o);
		graph.addEdge(e);
                parseImport(property, object);              
	}
    
    void parseImport(Atom property, Atom object){
        if (property.getLongName().equals(Load.IMPORTS)){
            try {
                load.parseImport(object.getLongName());
            } catch (LoadException ex) {
                logger.error(ex);
            }
        }
    }
	
    @Override
	public void triple(Atom property, List<Atom> l) {
		if (source == null){
			source = graph.addDefaultGraphNode();
		}
		
		Node p = getProperty(property);
		
		ArrayList<Node> list = new ArrayList<Node>();
		for (Atom at : l){
			Node n = getObject(at);
			list.add(n);
		}
		
		Entity e = graph.create(source, p, list);
		graph.addEdge(e);
	}
	
    @Override
	public void list(RDFList l){
		for (Exp exp : l.getBody()){
			if (exp.isTriple()){
				Triple t = exp.getTriple();
				triple(t.getSubject(), t.getProperty(), t.getObject());
			}
		}
	}
	
	
	
	Node getObject(Atom object){
		Node o;
		if (object.isLiteral()){
			o = getLiteral(object.getConstant());
		}
		else {
			o = getNode(object);
		}
		return o;
	}

	
	Node getLiteral(Atom pred, Constant lit){
		String lang = lit.getLang();
		String datatype = nsm.toNamespace(lit.getDatatype());
		if (lang == "") lang = null;
		return graph.addLiteral(pred.getLabel(), lit.getLabel(), datatype, lang);
	}
	
	Node getLiteral(Constant lit){
		String lang = lit.getLang();
		String datatype = nsm.toNamespace(lit.getDatatype());
		if (lang == "") lang = null;
		return graph.addLiteral(lit.getLabel(), datatype, lang);
	}
	
	Node getProperty(Atom res){
		return graph.addProperty(res.getLabel());		
	}
	
	Node getNode(Atom c){
		if (c.isBlankNode()){
			return graph.addBlank(getID(c.getLabel()));
		}
		else {
			return graph.addResource(c.getLabel());
		}
	}
        
        Node getSubject(Atom c){
            if (c.isBlankNode()) {
                return graph.addBlank(getID(c.getLabel()));
            } else {
                if (resource == null || !resource.equals(c.getLabel())) {
                    resource = c.getLabel();
                    node = graph.addResource(resource);
                }
                return node;
            }
        }

	
	
	String getID(String b){
		String id = b;
		if (isRenameBlankNode() ){
			id = blank.get(b);
			if (id == null){
				id = graph.newBlankID();
				blank.put(b, id);
			}
		}
		return id;
	}

    @Override
	public boolean isRenameBlankNode() {
		return renameBlankNode;
	}

    @Override
	public  void setRenameBlankNode(boolean renameBlankNode) {
		this.renameBlankNode = renameBlankNode;
	}

}
