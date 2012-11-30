package fr.inria.edelweiss.kgtool.print;

import java.util.ArrayList;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

/**
 * SPARQL-based RDF AST Pretty Printer
 * Use case: pprint SPIN RDF graph in SPARQL concrete syntax
 * 
 * Olivier Corby, Wimmics INRIA 2012
 */
public class PPrinter {
	
	public static final String PPRINTER = "/home/corby/workspace/kgengine/src/test/resources/data/pprint/template";
	private static final String OUT = ASTQuery.OUT;
	private static final String IN  = ASTQuery.IN;
	
	Graph graph;
	QueryEngine qe;
	NSManager nsm;
	QueryProcess exec;
	
	ArrayList<IDatatype> list;
	
	String pp = PPRINTER;
	
	boolean isDebug = ! true;
	private IDatatype EMPTY;

	
	
	PPrinter(Graph g, String p){
		graph = g;
		pp = p;
		init(graph, p);
		nsm = NSManager.create();
		list = new ArrayList<IDatatype>();
		exec = QueryProcess.create(g, true);
		EMPTY = DatatypeMap.createLiteral("");
	}
	
	
	public static PPrinter create(Graph g){
		return new PPrinter(g, PPRINTER);
	}
	
	public static PPrinter create(Graph g, String p){
		return new PPrinter(g, p);
	}
	
	public void setNSM(NSManager n){
		nsm = n;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
		
	/**
	 * Pretty print the graph.
	 * Apply the first query that matches without bindings.
	 * Hence it may pprint a subpart of the graph, 
	 * the subpart that matches the first query
	 */
	public IDatatype pprint(){
		for (Query qq : qe.getQueries()){

			// Tricky:
			// All queries of this PPrinter share the same query base
			// use case: kg:pprint() call the same PPrinter for all queries of this base
			qq.setPPrinter(this);
			if (isDebug){
				qq.setDebug(true);
				System.out.println(qq.getAST());
			}
			Mappings map = exec.query(qq);
			Node res = map.getNode(OUT);
			if (res != null){
				return  (IDatatype) res.getValue();
			}
		}
		return EMPTY;
	}
	

	/**
	 * Object o is the node where to apply the query (with a Mapping)
	 * use case: 
	 * select (kg:pprint(?w) as ?pw) where {?q ast:where ?w}
	 * Search a query that matches ?w
	 * By convention, ?w is bound to ?in, all queries have ?in as input node
	 * and ?out as output node
	 * Compute the first query that matches ?w
	 * Queries are sorted  more "specific" first: construct/select/ask/describe
	 * They are sorted using a pragma { kg:query kg:priority n }
	 */
	public IDatatype pprint(IDatatype dt){		
		if (dt == null){
			return EMPTY;
		}
		
		if (isDebug){
			System.out.println("pprint: " + dt);
		}
		
		// to prevent infinite loop in the case where the graph is cyclic
		// should not happen with RDF AST

		if (list.contains(dt)){
			return print(dt);
		}
		else {
			list.add(dt);
		}
		
		Graph g = graph;									
		QueryProcess exec = this.exec;

		Node qn = NodeImpl.createVariable(IN);
		Mapping m = Mapping.create(qn, g.getNode(dt, false, false));

		for (Query qq : qe.getQueries()){
			
			// Tricky:
			// All queries of this PPrinter share the same query base
			// use case: kg:pprint() call the same PPrinter for all queries of this base
			qq.setPPrinter(this);
			if (isDebug){
				qq.setDebug(true);
				System.out.println(qq.getAST());
			}
			
			Mappings map = exec.query(qq, m);
			Node res = map.getNode(OUT);
			
			if (res != null){
				list.remove(dt);
				return  (IDatatype) res.getValue();
			}
		}
			
		// no query match dt; it may be a constant		
		list.remove(dt);
		//System.out.println("return: " + dt);
		return print(dt);
	}
	
	
	/**
	 * pprint a URI, Literal, blank node
	 * in its Turtle syntax
	 */
	IDatatype print(IDatatype dt){

		if (dt.isURI()){
			String qname = nsm.toPrefix(dt.getLabel(), true);
			if (dt.getLabel().equals(qname)){
				// no namespace, return <uri>
				dt = DatatypeMap.newInstance(dt.toString());
			}
			else {
				// return qname
				dt = DatatypeMap.newInstance(qname);
			}
		}
		else if (dt.isLiteral()){
			if (dt.isNumber() || dt.getCode() == IDatatype.BOOLEAN){
				// print as is
			}
			else {
				// add quotes around string, add lang tag if any
				dt = DatatypeMap.newInstance(dt.toString());
			}
		}
		
		return dt;	
	}
	
	
	void init(Graph g, String str){
		Load ld = Load.create(g);
		ld.load(str);
		qe = ld.getQueryEngine();
		qe.sort();
//		for (Query q : qe.getQueries()){
//			System.out.println(q.getAST());
//		}
	}

}
