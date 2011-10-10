package fr.inria.edelweiss.kgtool.print;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
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
	static final String ID 			= " rdf:about='";
	static final String NODEID 		= " rdf:NodeID='";
	static final String RESOURCE 	= " rdf:resource='";
	static final String LANG 		= " xml:lang='";
	static final String DATATYPE 	= " rdf:datatype='";
	static final String RDFSCLASS 	= "rdfs:Class";
	static final String RDFPROPERTY	= "rdf:Property";
	static final String OWLCLASS 	= "owl:Class";
	static final String SPACE 		= "   ";
	static final String NL 			= System.getProperty("line.separator");
	private static final String OCOM = "<!--";
	private static final String CCOM = "--!>";

	
	Graph graph;
	Mapper map;
	NSManager nsm;
	StringBuilder sb;
	Query query;
	ASTQuery ast;
	
	List<String> with, without;
	
	RDFFormat(NSManager n){
		with = new ArrayList<String>();
		without = new ArrayList<String>();
		nsm = n;
		sb = new StringBuilder();
	}
	
	
	RDFFormat(Graph g, NSManager n){
		this(n);
		if (g!=null){
			graph = g;
			graph.prepare();
		}
	}
	
	RDFFormat(Graph g, Query q){
		this(getAST(q).getNSM());
		if (g!=null){
			graph = g;
			graph.prepare();
		}
		ast = getAST(q);
		query = q;
	}
	
	static ASTQuery getAST(Query q){
		return (ASTQuery) q.getAST();
	}
	
	RDFFormat(Mapping m, NSManager n){
		this(n);
		add(m);
	}
	
	public static RDFFormat create(Graph g, NSManager n){
		return new RDFFormat(g, n);
	}
	
	public static RDFFormat create(Graph g, Query q){
		return new RDFFormat(g, q);
	}
	
	public static RDFFormat create(Mappings map){
		Graph g = (Graph) map.getGraph();
		if (g!=null){
			return create(g, map.getQuery());
		}
		return create(map, NSManager.create());
	}
	
	public static RDFFormat create(Graph g){
		return new RDFFormat(g, NSManager.create());
	}
	
	public static RDFFormat create(Mappings lm, NSManager m){
		RDFFormat f = RDFFormat.create(m);
		for (Mapping map : lm){
			f.add(map);
		}
		return f;
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
	
	public void with(String name){
		with.add(name);
	}
	
	public void without(String name){
		without.add(name);
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
		if (graph == null && map == null){
			return null;
		}
		
		for (Entity ent : getNodes()){
			Node node = ent.getNode();
			print(node);
		}
		
		StringBuilder bb = new StringBuilder();
		
		bb.append("<rdf:RDF"); 
		bb.append(NL);
		header(bb);
		bb.append(">");
		bb.append(NL);
		error();
		bb.append(NL);
		bb.append(sb); 
		bb.append("</rdf:RDF>");
		
		return bb.toString();
	}
	
	
	void header(StringBuilder bb){
		boolean first = true;
		for (String p : nsm.getPrefixSet()){
			
			if (first){
				first = false;
			}
			else {
				bb.append(NL);
			}
			
			String ns = nsm.getNamespace(p);
			bb.append("xmlns:" + p + "='" + ns + "'");
		}
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
				if (ent!=null){
					wprint(ent);
				}
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
	

	void wprint(Entity ent){
		Node gname = ent.getGraph();
		
		if (without.contains(gname.getLabel())){
			return;
		}

		if (with.size()>0){
			if (! with.contains(gname.getLabel())){
				return;
			}
		}
		print(ent);
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
		sb.append(mes);
		sb.append(obj);
		sb.append(NL);
	}

	void display(Object obj){
		sb.append(obj);
		sb.append(NL);
	}
	
	void display(){
		sb.append(NL);
	}
	
	
	void error(){
		boolean b1 = ast!=null     && ast.getErrors()!=null;
		boolean b2 = query != null && query.getErrors()!=null;
		
		if (b1 || b2){
			
			display(OCOM);
			if (ast.getText()!=null){
				display(ast.getText());
			}
			display();
			
			if (b1){
				for (String mes : ast.getErrors()){
					display(mes);
				}
			}
			if (b2){
				for (String mes : query.getErrors()){
					display(mes);
				}
			}
			display(CCOM);
		}
	}
	
	

}
