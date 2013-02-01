package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;

/**
 * SPARQL-based RDF AST Pretty Printer
 * Use case: pprint SPIN RDF graph in SPARQL concrete syntax
 * 
 * 
 * Olivier Corby, Wimmics INRIA 2012
 */
public class PPrinter {
	
	private static final String NULL = "";
	public  static final String PPRINTER = "/home/corby/workspace/kgengine/src/test/resources/data/pprint/template";
	private static final String OUT = ASTQuery.OUT;
	private static final String IN  = ASTQuery.IN;
	
	Graph graph, fake;
	QueryEngine qe;
	Query query;
	NSManager nsm;
	QueryProcess exec;
	
	Stack stack;
	
	String pp = PPRINTER;
	
	boolean isDebug = ! true;
	private IDatatype EMPTY;
	boolean isTurtle = false;

	/**
	 * 
	 * Keep track of nodes already printed to prevent loop
	 * Variant: may check the pair (dt, query) 
	 *   do not to loop on the same query but may use several queries on same node dt
	 * 
	 */
	class Stack {
		ArrayList<IDatatype> list;
		HashMap<IDatatype, ArrayList<Query>> map;
		boolean multi = true;
		
		Stack(boolean b){
			list = new ArrayList<IDatatype>();
			map  = new HashMap<IDatatype, ArrayList<Query>>();
			multi = b;
		}
		
		int size(){
			return list.size();
		}
		
		void push(IDatatype dt){
			list.add(dt);
		}
		
		void push(IDatatype dt, Query q){
			list.add(dt);
			if (multi){
				ArrayList<Query> qlist = map.get(dt);
				if (qlist == null){
					qlist = new ArrayList<Query>();
					map.put(dt, qlist);
				}
				qlist.add(q);
			}
		}
		
		IDatatype pop(){
			if (list.size() > 0){
				int last = list.size() - 1;
				IDatatype dt = list.get(last);
				list.remove(last);
				if (multi){
					ArrayList<Query> qlist = map.get(dt);
					qlist.remove(qlist.size()-1);
				}
				return dt;
			}
			return null;			
		}
		
		boolean contains(IDatatype dt){
			return list.contains(dt);
		}
		
		boolean contains(IDatatype dt, Query q){
			boolean b = list.contains(dt);
			if (b && multi){
				ArrayList<Query> qlist = map.get(dt);
				return qlist.contains(q);
			}
			return b;
		}
	}
	
	
	
	PPrinter(Graph g, String p){
		graph = g;
		fake = Graph.create();
		pp = p;
		init(graph, p);
		nsm = NSManager.create();
		stack = new Stack(true);
		exec = QueryProcess.create(g, true);
		EMPTY = DatatypeMap.createLiteral(NULL);
	}
	
	
	public static PPrinter create(Graph g){
		return new PPrinter(g, PPRINTER);
	}
	
	public static PPrinter create(Graph g, String p){
		if (p == null){
			p = PPRINTER;
		}
		return new PPrinter(g, p);
	}
	
	public void setNSM(NSManager n){
		nsm = n;
	}
	
	public void setDebug(boolean b){
		isDebug = b;
	}
	
	public void setTurtle(boolean b){
		isTurtle = b;
	}
	
	public String toString(){
		IDatatype dt = pprint();
		return dt.getLabel();
	}
	
	public void write(String name) throws IOException {				
		FileWriter fw = new FileWriter(name);
		String str = toString();
		fw.write(str);
		fw.flush();
		fw.close();
	}
		
	/**
	 * Pretty print the graph.
	 * Apply the first query that matches without bindings.
	 * Hence it may pprint a subpart of the graph, 
	 * the subpart that matches the first query
	 */
	public IDatatype pprint(){
		query = null;
		for (Query qq : qe.getQueries()){
			
			qq.setPPrinter(this);
			// remember start with qq for function pprint below
			query = qq;
			Mappings map = exec.query(qq);
			query = null;
			Node res = map.getNode(OUT);
			if (res != null){
				return  (IDatatype) res.getValue();
			}
		}
		query = null;
		return EMPTY;
	}
	
	public int level(){
		return stack.size();
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
	 * One rule is applied only once on one node, 
	 * hence we store in a stack : node -> rule
	 */
	public IDatatype pprint(Expr exp, IDatatype dt){		
		if (dt == null){
			return EMPTY;
		}
		
		boolean start = false;
		if (query != null && stack.size() == 0){
			// just started with query in pprint() above
			if (exp != null  && exp.getLabel().equals(IN)){
				// current variable is ?in, 
				// push dt -> query in the stack
				// query is the first query that started pprint (see function above)
				// and at that time ?in was not bound
				// use case:
				// template {"subClassOf(" ?in " " ?y ")"} where {?in rdfs:subClassOf ?y}
				start = true;
				stack.push(dt, query);
			}
		}
		
		if (isDebug){
			System.out.println("pprint: " + dt);
		}
		
		// to prevent infinite loop in the case where the graph is cyclic
		// should not happen with RDF AST
		
		Graph g = graph;									
		QueryProcess exec = this.exec;

		Node qn = NodeImpl.createVariable(IN);
		Node n  = g.getNode(dt, false, false);
		if (n == null){
			// use case: kg:pprint("header")
			n = fake.getNode(dt, true, true);
		}
		Mapping m = Mapping.create(qn, n);

		for (Query qq : qe.getQueries()){
			
			// Tricky: All queries of this PPrinter share the same query base (see PluginImpl)
			qq.setPPrinter(this);
			
			if (! stack.contains(dt, qq)){

				stack.push(dt, qq);
				Mappings map = exec.query(qq, m);
				stack.pop();
				Node res = map.getNode(OUT);

				if (res != null){
					if (start){
						stack.pop();
					}
					return  (IDatatype) res.getValue();
				}
			}
		}
		
		if (start){
			stack.pop();
		}	
		// no query match dt; it may be a constant		
		return display(dt); 
	}
	
	
	IDatatype display(IDatatype dt){
		if (isTurtle){
			return turtle(dt);
		}
		else {
			return dt;
		}
	}
	
	/**
	 * pprint a URI, Literal, blank node
	 * in its Turtle syntax
	 */
	public IDatatype turtle(IDatatype dt){

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
		
		if (qe == null){
			RuleEngine re = ld.getRuleEngine();
			qe = QueryEngine.create(g);
			for (Rule r : re.getRules()){
				Query q = r.getQuery();
				qe.defQuery(q);
			}
		}
		
		qe.sort();
		
//		for (Query q : qe.getQueries()){
//			System.out.println(q.getPragma(Pragma.FILE));
//			ASTQuery ast = (ASTQuery) q.getAST();
//			System.out.println(ast);
//		}
	}

}
