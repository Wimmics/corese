package fr.inria.edelweiss.kgtool.load;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.acacia.corese.triple.api.Creator;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * 
 * Create Edge on the fly for Turtle parser
 * 
 * @author Olivier Corby, INRIA 2012
 * 
 */
public class CreateImpl implements Creator {
	
	Hashtable<String, String> blank;
	NSManager nsm;
	Graph graph;
	Node source;
	Stack stack;
	String base;
	private boolean renameBlankNode = true;
	int limit = Integer.MAX_VALUE;
    private String resource;
    private Node node;
	
	class Stack extends ArrayList<Node> {
		
		Node pop(){
			if (size()>0){
				return remove(size()-1);
			}
			return null;
		}
		
	}
	
	CreateImpl(Graph g){
		graph = g;
		blank = new Hashtable<String, String>();
		nsm = NSManager.create();
		stack = new Stack();
	}

	public static CreateImpl create(Graph g){
		return new CreateImpl(g);
	}
        
        public void start(){
            graph.setUpdate(true);
        }
	
	public void setLimit(int max){
		limit = max;
	}
	
	// init
	// TODO: check 
	public void graph(String src){
		source = graph.addGraph(src);
	}
	
	
	public void graph(Atom src){
		stack.add(source);
		source = graph.addGraph(src.getLabel());
	}
	
	public void endGraph(Atom src){
		source = stack.pop();
	}
	
	public boolean accept(Atom subject, Atom property, Atom object){
//		if (graph.size() / 10000 == graph.size()/ 10000.0){
//			System.out.println(graph.size());
//		}
		if (graph.size() < limit){
			return true;
		}
		return false;
	}

	public void triple(Atom subject, Atom property, Atom object) {		
		if (source == null){
			source = graph.addGraph(Entailment.DEFAULT);
		}
		Node s = getSubject(subject);
		Node p = getProperty(property);
		Node o;
		if (object.isLiteral()){
			o = getLiteral(property, object);
		}
		else {
			o = getNode(object);
		}
		
		EdgeImpl e = graph.create(source, s, p, o);
		graph.addEdge(e);
	}
	
	public void triple(Atom property, List<Atom> l) {
		if (source == null){
			source = graph.addGraph(Entailment.DEFAULT);
		}
		
		Node p = getProperty(property);
		
		ArrayList<Node> list = new ArrayList<Node>();
		for (Atom at : l){
			Node n = getObject(at);
			list.add(n);
		}
		
		EdgeImpl e = graph.create(source, p, list);
		graph.addEdge(e);
	}
	
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
			o = getLiteral(object);
		}
		else {
			o = getNode(object);
		}
		return o;
	}

	
	Node getLiteral(Atom pred, Atom lit){
		String lang = lit.getLang();
		String datatype = nsm.toNamespace(lit.getDatatype());
		if (lang == "") lang = null;
		return graph.addLiteral(pred.getLabel(), lit.getLabel(), datatype, lang);
	}
	
	Node getLiteral(Atom lit){
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

	public boolean isRenameBlankNode() {
		return renameBlankNode;
	}

	public  void setRenameBlankNode(boolean renameBlankNode) {
		this.renameBlankNode = renameBlankNode;
	}

}
