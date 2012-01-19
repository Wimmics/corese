package fr.inria.edelweiss.kgtool.print;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;

public class TripleFormat extends RDFFormat {
	
	static final String PREFIX 	= "@prefix";
	static final String PV 		= " ;";
	static final String DOT 	= " .";
	static final String OPEN 	= "<";
	static final String CLOSE 	= ">";


	TripleFormat(Graph g, NSManager n) {
		super(g, n);
	}
	
	
	public static TripleFormat create(Graph g, NSManager n){
		return new TripleFormat(g, n);
	}
	
	public static TripleFormat create(Graph g){
		return new TripleFormat(g, NSManager.create());
	}

	
	public StringBuilder getStringBuilder(){
		if (graph == null && map == null){
			return null;
		}
		
		for (Entity ent : getNodes()){
			Node node = ent.getNode();
			print(node);
		}
		
		StringBuilder bb = new StringBuilder();
		
		header(bb);
		
		bb.append(NL);
		bb.append(NL);

		bb.append(sb); 
		
		return bb;
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
			bb.append(PREFIX + SPACE + p + ": <" + toXML(ns) + "> .");
		}
	}
	
	
	
	void print(Node node){
		boolean first = true;
		
		for (Entity ent : getEdges(node)){
			
			if (ent!=null && accept(ent)){
				
				if (first){
					first = false;
					subject(ent);
				}
				else {
					sdisplay(PV);
					sdisplay(NL);
				}
				
				edge(ent);
			}
		}
		
		if (! first){
			sdisplay(DOT);
			sdisplay(NL);
			sdisplay(NL);
		}
	}
	
	
	void subject(Entity ent){
		
		IDatatype dt0 = getValue(ent.getNode(0));
		
		if (dt0.isBlank()){
			String sub = dt0.getLabel();
			sdisplay(sub);
		}
		else {
			uri(dt0.getLabel());
		}
		
		sdisplay(SPACE);
	}
	
	void uri(String label){
		String qname = nsm.toPrefix(label, true);
		if (qname.equals(label)){
			sdisplay(OPEN);
			sdisplay(label);
			sdisplay(CLOSE);
		}
		else {
			sdisplay(qname);
		}
	}
	
	
	void edge(Entity ent){
		Edge edge = ent.getEdge();
		
		String pred = nsm.toPrefix(edge.getEdgeNode().getLabel());
		
		sdisplay(pred);
		sdisplay(SPACE);
		
		String obj;
		
		IDatatype dt1 = getValue(edge.getNode(1));
		
		if (dt1.isLiteral()){
			obj = dt1.toSparql();
			sdisplay(obj);
		}
		else if (dt1.isBlank()){
			obj = dt1.getLabel();
			sdisplay(obj);
		}
		else {
			uri(dt1.getLabel());
		}

	}
	
	

}
