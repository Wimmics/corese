package fr.inria.edelweiss.kgtool.print;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;

/**
 * 
 * RDF Format for Graph construct-where result
 * RDF Format for Mapping edges
 * 
 * Olivier Corby, Edelweiss INRIA 2011
 * 
 */
public class RDFFormat {
	static final String DESCRIPTION = "rdf:Description";
	static final String ID 		= " rdf:ID='";
	static final String NODEID 	= " rdf:NodeID='";
	static final String RESOURCE = " rdf:resource='";
	static final String LANG 	= " xml:lang='";
	static final String DATATYPE = " rdf:datatype='";
	static final String SPACE 	= "   ";
	static final String RDFSCLASS 	= "rdfs:Class";
	static final String RDFPROPERTY	= "rdf:Property";
	static final String OWLCLASS 	= "owl:Class";
	
	Graph graph;
	Mapper map;
	NSManager nsm;
	StringWriter w;
	PrintWriter pw;
	
	RDFFormat(NSManager n){
		nsm = n;
		w = new StringWriter();
		pw = new PrintWriter(w);
	}
	
	
	RDFFormat(Graph g, NSManager n){
		this(n);
		graph = g;
	}
	
	RDFFormat(Mapping m, NSManager n){
		this(n);
		add(m);
	}
	
	public static RDFFormat create(Graph g, NSManager n){
		return new RDFFormat(g, n);
	}
	
	public static RDFFormat create(Graph g){
		return new RDFFormat(g, NSManager.create());
	}
	
	public static RDFFormat create(Mapping m){
		return new RDFFormat(m, NSManager.create());
	}
	
	public static RDFFormat create(Mapping m, NSManager n){
		if (n == null) return create(m); 
		return new RDFFormat(m, n);
	}
	
	public static RDFFormat create(NSManager n){
		return new RDFFormat(n);
	}
	
	public void add(Mapping m){
		if (map == null) map = Mapper.create();
		map.add(m);
	}
	
	Iterable<Entity> getNodes(){
		if (map != null) return map.getMapNodes();
		return graph.getNodes();
	}
	
	Iterable<Entity> getEdges(Node node){
		if (map != null) return map.getMapEdges(node);
		return graph.getNodeEdges(node);
	}
	
	Node getType(Node node){
		if (map != null) return map.getMapType(node);
		Node type = graph.getPropertyNode(Entailment.RDFTYPE);
		if (type == null) return null;
		Edge edge = graph.getEdge(type, node, 0);
		if (edge == null) return null;
		return edge.getNode(1);
	}
	
	

	
	public String toString(){
		for (Entity ent : getNodes()){
			Node node = ent.getNode();
			print(node);
		}
		String str = w.toString();
		str = "<rdf:RDF " + 
		header() +   ">\n\n" + str + 
		"</rdf:RDF>";
		return str;
	}
	
	String header(){
		String str = "";
		for (String p : nsm.getPrefixSet()){
			String ns = nsm.getNamespace(p);
			str += "xmlns:" + p + "='" + ns + "'\n";
		}
		return str;
	}
	
	
	void print(Node node){

		Iterator<Entity> it = getEdges(node).iterator();
		
		if (it.hasNext()){
			
			IDatatype dt = getValue(node);

			String id = ID;
			if (dt.isBlank())id = NODEID;
			String type = type(node);	
			
			String open  = "<" + type;
			String close = "</" + type + ">";

			display(open + id + node.getLabel() + "'>");

			for (; it.hasNext();){
				Entity ent = it.next();
				if (ent!=null) print(ent);
			}

			display(close);
			display();
		}
	}
	
	
	String type(Node node){
		String open  = DESCRIPTION;
		Node type = getType(node);
		if (type!=null){
			String name = type.getLabel();
			if (name.equals(Entailment.RDFSCLASS)){
				open = RDFSCLASS;
			}
			else if (name.equals(Entailment.OWLCLASS)){
				open = OWLCLASS;
			}
			else if (name.equals(Entailment.RDFPROPERTY)){
				open = RDFPROPERTY;
			}
		}
		return open;
	}
	

	
	void print(Entity ent){
		Edge edge = ent.getEdge();
		String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());
		IDatatype dt = getValue(edge.getNode(1));
		String open  = "<" + pred;
		String close = "</" + pred +">";

		if (dt.isLiteral()){
			if (dt.hasLang()){
				display(SPACE + open + LANG + dt.getLang()+ "'>" + dt + close);
			}
			else if (dt.getDatatype()!=null){
				display(SPACE + open + DATATYPE + dt.getDatatypeURI() + "'>" + dt + close);
			}
			else {
				display(SPACE + open + ">" + dt + close);
			}
		}
		else {
			String id = RESOURCE;
			if (dt.isBlank()) id = NODEID ;
			display(SPACE + open + id + dt + "'/>");
		}
	}
	

	IDatatype getValue(Node node){
		return (IDatatype) node.getValue();
	}
	
	
	void display(String mes, Object obj){
		pw.println(mes + obj);
	}

	void display(Object obj){
		pw.println(obj);
	}
	
	void display(){
		pw.println();
	}

}
