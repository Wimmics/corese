package fr.inria.corese.kgtool.print;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;

public class TripleFormat extends RDFFormat {
	
	static final String PREFIX 	= "@prefix";
	static final String PV 		= " ;";
	static final String DOT 	= " .";
	static final String OPEN 	= "<";
	static final String CLOSE 	= ">";
	static final String GRAPH 	= "graph";
	static final String OGRAPH 	= "{";
	static final String CGRAPH 	= "}";

	boolean isGraph = false;


	TripleFormat(Graph g, NSManager n) {
		super(g, n);
	}
	
	
	public static TripleFormat create(Graph g, NSManager n){
		return new TripleFormat(g, n);
	}
	
	public static TripleFormat create(Mappings map){
		Graph g = (Graph) map.getGraph();
		if (g != null){
			Query q = map.getQuery();
			NSManager nsm = ((ASTQuery) q.getAST()).getNSM();
			return create(g, nsm);
		}
		return create(Graph.create());
	}
	
	public static TripleFormat create(Graph g){
		return new TripleFormat(g, NSManager.create());
	}
	
        public static TripleFormat create(Mappings map, boolean isGraph){
		Graph g = (Graph) map.getGraph();
		if (g != null){
			Query q = map.getQuery();
			NSManager nsm = ((ASTQuery) q.getAST()).getNSM();
                        TripleFormat t = new TripleFormat(g, nsm);
                        t.setGraph(isGraph);
			return t;
		}
		return create(Graph.create());
	}
        
	public static TripleFormat create(Graph g, boolean isGraph){
		TripleFormat t = new TripleFormat(g, NSManager.create());
		t.setGraph(isGraph);
		return t;
	}

	public void setGraph(boolean b){
		isGraph = b;
	}
	
	public StringBuilder getStringBuilder(){
		sb 	= new StringBuilder();
		if (graph == null && map == null){
			return sb;
		}
		
		if (isGraph){
			graphNodes();
		}
		else {
			nodes();
		}
		
		StringBuilder bb = new StringBuilder();
		
		header(bb);
		
		bb.append(NL);
		bb.append(NL);

		bb.append(sb); 
		
		return bb;
	}
	
	
	void nodes(){
		for (Entity ent : getNodes()){
			Node node = ent.getNode();
			print(null, node);
		}
	}
	
	
	void graphNodes(){
		for (Node gNode : graph.getGraphNodes()){
			if (accept(gNode)){
				sdisplay(GRAPH);
				sdisplay(SPACE);
				subject(gNode);
				sdisplay(OGRAPH);
				for (Entity ent : graph.getNodes(gNode)){
					Node node = ent.getNode();
					print(gNode, node);
				}
				display(CGRAPH);
			}
		}
	}
	
	void header2(StringBuilder bb){
		boolean first = true;
		for (String p : nsm.getPrefixSet()){
			
			if (first){
				first = false;
			}
			else {
				bb.append(NL);
			}
			
			String ns = nsm.getNamespace(p);
			//bb.append(PREFIX + SPACE + p + ": <" + toXML(ns) + "> .");
                        bb.append(String.format("@prefix %s: <%s>", p, toXML(ns)));
		}
	}
        
       @Override
	void header(StringBuilder bb){
            bb.append(nsm.toString(PREFIX, false, false));
        }
	
	
	void print(Node gNode, Node node){
		boolean first = true;
		
		for (Entity ent : getEdges(gNode, node)){
			
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
	
	Iterable<Entity> getEdges(Node gNode, Node node){
		if (isGraph){
			return graph.getNodeEdges(gNode, node);	
		}
		else {
			return graph.getNodeEdges(node);
		}
	}
	
	
	
	void subject(Entity ent){
		subject(ent.getNode(0));
	}
		
		
		
	void subject(Node  node){
		IDatatype dt0 = getValue(node);
		
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
            String str = nsm.toPrefixURI(label);
            sdisplay(str);
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
