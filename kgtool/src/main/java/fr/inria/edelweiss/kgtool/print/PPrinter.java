package fr.inria.edelweiss.kgtool.print;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;

/**
 * SPARQL-based RDF AST Pretty Printer
 * Use case: 
 * pprint SPIN RDF in SPARQL concrete syntax
 * pprint OWL 2 RDF in functional syntax
 * Use a list of templates : 
 *   template { presentation } where { pattern }
 * Templates are loaded from a directory or from a file in .rul format (same as rules)
 * Templates can also be defined using defTemplate()
 * If called with kg:pprint(?x) :    execute one template on ?x
 * If called with kg:pprintAll(?x) : execute all templates on ?x
 * 
 * Olivier Corby, Wimmics INRIA I3S - 2012
 */
public class PPrinter {
	
	private static final String NULL = "";
	public  static final String PPRINTER = "/home/corby/workspace/kgengine/src/test/resources/data/pprint/asttemplate";
	private static final String OUT = ASTQuery.OUT;
	private static final String IN  = ASTQuery.IN;
	private static String NL = System.getProperty("line.separator");

	Graph graph, fake;
	QueryEngine qe;
	Query query;
	NSManager nsm;
	QueryProcess exec;
	
	Stack stack;
	
	String pp = PPRINTER;
	String separator = NL;
	boolean isDebug = false;
	private IDatatype EMPTY;
	boolean isTurtle = false;
	int nbt = 0, max = 0;
	private String START = Exp.KGRAM + "start";
	HashMap<Query,Integer> tcount;
	private boolean isHide = false;
	public boolean stat = !true;

	/**
	 * 
	 * Keep track of nodes already printed to prevent loop
	 * Variant: check the pair (dt, template) 
	 *   do not to loop on the same template ; may use several templates on same node dt
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
			ArrayList<Query> qlist = map.get(dt);
			return qlist != null && qlist.contains(q);
		}
		
		boolean contains2(IDatatype dt, Query q){
			boolean b = list.contains(dt);
			if (b && multi){
				ArrayList<Query> qlist = map.get(dt);
				return qlist.contains(q);
			}
			return b;
		}
		
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (IDatatype dt : list){
				sb.append(i++);
				sb.append(" ");
				sb.append(dt);
				sb.append(": ");
				for (Query q : map.get(dt)){
					sb.append(name(q));
					sb.append(" ");
				}
				sb.append("\n");
			}
			return sb.toString();
		}
		
		
	}
	
	
	
	PPrinter(Graph g, String p){
		graph = g;
		fake = Graph.create();
		pp = p;
		exec = QueryProcess.create(g, true);		
		tune(exec);				
		init();
		nsm = NSManager.create();
		stack = new Stack(true);
		EMPTY = DatatypeMap.createLiteral(NULL);
		tcount = new HashMap<Query,Integer> ();
	}
	
	
	private void tune(QueryProcess exec) {
		// do not use Thread in Property Path
		// compute all path nodes and put them in a list
		// it is faster
		exec.setListPath(true);
		Producer prod = exec.getProducer();
		if (prod instanceof ProducerImpl){
			// return value as is for kg:pprint()
			// no need to create a graph node in Producer
			ProducerImpl pi = (ProducerImpl) prod;
			pi.setSelfValue(true);
		}		
	}


	public static PPrinter create(Graph g){
		return new PPrinter(g, null);
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
	
	public void setTurtle(boolean b){
		isTurtle = b;
	}
	
	public void setSeparator(String s){
		separator = s;
	}
	
	public int nbTemplates(){
		return nbt;
	}
	
	public String toString(){
		IDatatype dt = pprint();
		return dt.getLabel();
	}
	
	public StringBuilder toStringBuilder(){
		IDatatype dt = pprint();
		return dt.getStringBuilder();
	}
	
	public void write(String name) throws IOException {				
		FileWriter fw = new FileWriter(name);
		String str = toString();
		fw.write(str);
		fw.flush();
		fw.close();
	}
	
	public void defTemplate(String t){
		try {
			qe.defQuery(t);
		} catch (EngineException e) {
			e.printStackTrace();
		}
	}
	
		
	/**
	 * Pretty print the graph.
	 * Apply the first query that matches without bindings.
	 * Hence it may pprint a subpart of the graph, 
	 * the subpart that matches the first query
	 */
	public IDatatype pprint(){
		return pprint(false);
	}
	
	
	public IDatatype pprint(boolean all){
		query = null;
		ArrayList<IDatatype> result = new ArrayList<IDatatype>();
		
		List<Query> list = getTemplate(START);
		if (list.size() == 0){
			list = qe.getTemplates();
		}		
		
		for (Query qq : list){
			
			qq.setPPrinter(this);
			// remember start with qq for function pprint below
			query = qq;
			Mappings map = exec.query(qq);
			
			query = null;
			Node res = map.getNode(OUT);
			if (res != null){
				if (all){
					result.add((IDatatype) res.getValue());
				}
				else {
					return  (IDatatype) res.getValue();
				}
			}
		}
		
		query = null;
		
		if (all){
			IDatatype res = result(result);
			return res;
		}
		
		return EMPTY;
	}
	
	public int level(){
		return stack.size();
	}
	
	public int maxLevel(){
		return max;
	}

	public IDatatype pprint(Expr exp, IDatatype dt){	
		return pprint(exp, dt, null, false);
	}
	
	
	/**
	 * IDatatype dt is the focus node to be printed
	 * use case: 
	 * template { ... ?w ... } where {?in ast:where ?w}
	 * Search a template that matches ?w
	 * By convention, ?w is bound to ?in, all templates use variable ?in as focus node
	 * and ?out as output node
	 * Execute the first template that matches ?w (all templates if all = true)
	 * Templates are sorted  more "specific" first
	 * They are sorted using a pragma { kg:query kg:priority n }
	 * A template is applied only once on one node, 
	 * hence we store in a stack : node -> template
	 */
	public IDatatype pprint(Expr exp, IDatatype dt, String temp, boolean all){	
		
		if (dt == null){
			return EMPTY;
		}
		
		ArrayList<IDatatype> result = null;
		if (all) {
			result = new ArrayList<IDatatype>();
		}
		boolean start = false;
		
		if (query != null && stack.size() == 0){
			// just started with query in pprint() above
			// without focus node at that time
			if (exp != null){ //  && exp.getLabel().equals(IN)){
				// current variable is ?in, 
				// push dt -> query in the stack
				// query is the first template that started pprint (see function above)
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
				
		Graph g = graph;									
		QueryProcess exec = this.exec;

		Node qn = NodeImpl.createVariable(IN);
		Node n  = g.getNode(dt, false, false);
		if (n == null){
			// use case: kg:pprint("header")
			n = fake.getNode(dt, true, true);
		}
		Mapping m = Mapping.create(qn, n);

		int count = 0;
		for (Query qq : getTemplates(temp)){
						
			// Tricky: All templates of this PPrinter share the same PPrinter (see PluginImpl)
			qq.setPPrinter(this);
			
			if (! qq.isFail() && ! stack.contains(dt, qq)){
				
				nbt++;

				if (all) {
					count ++;
				}
				stack.push(dt, qq);
				if (stack.size() > max){
					max = stack.size();
				}
				
				if (stat ){
					incr(qq);
				}
				
				Mappings map = exec.query(qq, m);				
				
//				if (! qq.isAggregate()){
//					System.out.println(map.size());
//				}
				
				if (! all){
					// if execute all templates, keep them in the stack 
					// to prevent loop same template on same focus node
					stack.pop();
				}
				
				Node res = map.getNode(OUT);
								
				if (res != null){

					if (stat){
						succ(qq);
					}
					
					if (isDebug){
						trace(qq, res);
					}
					
					if (all){
						result.add((IDatatype) res.getValue());
					}
					else {
						if (start){
							stack.pop();
						}
						return  (IDatatype) res.getValue();
					}
				}
			}
		}
		
		if (all){
			// gather results of several templates
			
			for (int i=0; i<count; i++){
				// pop the templates that have been executed
				stack.pop();
			}
			
			if (result.size() > 0){
				
				if (start){
					stack.pop();
				}
				
				IDatatype res = result(result);
				return res;
			}
		}
		
		if (start){
			stack.pop();
		}	
		
		// no template match dt
		
		if (temp != null){
			// named template fail
			return EMPTY;
		}
		
		// default: display dt as is
		return display(dt); 
	}
	
	

	private List<Query> getTemplates(String temp) {
		if (temp == null){
			return qe.getTemplates();
		}
		return getTemplate(temp);
	}


	private List<Query> getTemplate(String temp) {
		Query q =  qe.getTemplate(temp);
		ArrayList<Query> l = new ArrayList<Query>(1);
		if (q != null){
			l.add(q);
		}
		return l;	
	}


	/**
	 * Concat results of several templates executed on same focus node
	 * kg:pprintAll(?x)
	 */
	IDatatype result(List<IDatatype> result){
		StringBuilder sb = new StringBuilder();

		for (IDatatype d : result){
			StringBuilder b = d.getStringBuilder();
			
			if (b!=null){
				if (b.length()>0){
					sb.append(b);
					sb.append(separator) ;
				}
			}
			else if (d.getLabel().length() > 0){
				sb.append(d.getLabel());
				sb.append(separator) ;
			}
		}
		
		IDatatype res = DatatypeMap.newStringBuilder(sb);
		return res;
	}
	
	
	
	
	IDatatype display(IDatatype dt){
		if (isTurtle){
			return turtle(dt);
		}
		else if (isHide){
			return EMPTY;
		}
		else {
			// the label of dt as in SPARQL concat()
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
				dt = DatatypeMap.newStringBuilder(dt.toString());
			}
			else {
				// return qname
				dt = DatatypeMap.newStringBuilder(qname);
			}
		}
		else if (dt.isLiteral()){
			if (dt.isNumber() || dt.getCode() == IDatatype.BOOLEAN){
				// print as is
			}
			else {
				// add quotes around string, add lang tag if any
				dt = DatatypeMap.newStringBuilder(dt.toString());
			}
		}
		
		return dt;	
	}
	
	/**
	 * Load templates from directory (.rq) or from a file (.rul)
	 */
	void init(){
		if (pp == null){
			qe = QueryEngine.create(graph);
		}
		else {
			Load ld = Load.create(graph);
			try {
				ld.loadWE(pp);
			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			qe = ld.getQueryEngine();

			if (qe == null && ld.getRuleEngine() != null){

				RuleEngine re = ld.getRuleEngine();
				qe = QueryEngine.create(graph);
				for (Rule r : re.getRules()){
					Query q = r.getQuery();
					qe.defQuery(q);
				}
			}
			if (qe == null)  {
				qe = QueryEngine.create(graph);
			}
			
			qe.sort();	
		}
		//trace();
		
//		if (isCheck()){
//			check();
//		}
	}
	
	/***************************************************************
	 * 
	 * Check templates that would never succeed
	 * 
	 ***************************************************************/
	
	
	/**
	 * Check if a template edges not exist in graph
	 * remove those templates from the list to speed up
	 * PRAGMA: does not take RDFS entailments into account
	 */
	public void check(){
		for (Query q : qe.getQueries()){
			boolean b = graph.check(q);
			if (! b){
				q.setFail(true);
			}
		}
		qe.clean();
		if (stat) {
			trace();
		}
	}
	
	

	
	public void trace(){
		for (Query q : qe.getQueries()){
			if (q.hasPragma(Pragma.FILE)) {
				System.out.println(name(q));
			}
			ASTQuery ast = (ASTQuery) q.getAST();
			System.out.println(ast);
		}
	}
	
	String name(Query qq){
		String f =  qq.getStringPragma(Pragma.FILE);
		if (f != null){
			int index = f.lastIndexOf("/");
			if (index != -1){
				f = f.substring(index + 1);
			}
		}
		return f;
	}
	
	void trace(Query qq, Node res){
		System.out.println();
		System.out.println("query:  " + name(qq));
		System.out.println("result: " + res);
	}
	
	public void nbcall(){
		for (Query q : qe.getQueries()){
			System.out.println(q.getNumber() + " " + name(q) + " " + tcount.get(q));
		}
	}
	
	private void succ(Query q) {
		Integer c = tcount.get(q);
		if (c == null){
			tcount.put(q, 1);
		}
		else {
			tcount.put(q, c+1);
		}
	}


	private void incr(Query qq) {
		 qq.setNumber(qq.getNumber()+1);
	}


	public boolean isHide() {
		return isHide;
	}


	public void setHide(boolean isHide) {
		this.isHide = isHide;
	}

}
