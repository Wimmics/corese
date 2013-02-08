package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgenv.parser.NodeImpl;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Distance;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.PPrinter;


/**
 * Plugin for filter evaluator 
 * Compute semantic similarity of classes and solutions for KGRAPH
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl extends ProxyImpl {
	

	static Logger logger = Logger.getLogger(PluginImpl.class);
	
	static String DEF_PPRINTER = PPrinter.PPRINTER;
	String PPRINTER = DEF_PPRINTER;
	
	// for storing Node setProperty() (cf Nicolas Marie store propagation values in nodes)
	// idem for setObject()
	static Table table;
	
	Graph graph;
	MatcherImpl match;
	Loader ld;
	
	PluginImpl(Graph g, Matcher m){
		graph = g;
		if (table == null){
			table = new Table();
		}
		if (m instanceof MatcherImpl){
			match = (MatcherImpl) m;
		}
	}
	
	
	public static PluginImpl create(Graph g, Matcher m){
		return new PluginImpl(g, m);
	}
	
	
	
	public Object function(Expr exp, Environment env) {
		
		switch (exp.oper()){
		
		case PPRINT:
			return pprint(env);
		
		case GRAPH:
			return graph();	
			
		case LEVEL:
			return getLevel(env);	
			
		case INDENT:
			return indent(env);	
			
		case SIM:						
			// solution similarity
			return similarity(env);						
		}
		
		return null;
	}
	
	
	
	public Object function(Expr exp, Environment env, Object o) {
		
		switch (exp.oper()){
		
		case PPRINT:
			return pprint(exp, datatype(o), env);
			
		case TURTLE:
			return turtle(datatype(o), env);	
			
		case PPURI:
			return uri(exp, datatype(o), env);	
			
		case INDENT:
			return indent(o);		
		
		case KGRAM:
			return kgram(o);
			
		case NODE:
			return node(o);
			
		case GET:
			return getObject(o);	
			
		case SET:
			 return setObject(o, null);	
			 
		case LOAD:
			return load(o);
		
		case DEPTH:
			return depth(o);
			
		case QNAME:
			return qname(o, env);
			 						
		}
		return null;
	}
	
	public Object function(Expr exp, Environment env, Object o1, Object o2) {
		
		switch (exp.oper()){
		
		case GETP:
			return getProperty(o1, ((IDatatype) o2).intValue());
			
		case SETP:
			return setProperty(o1, ((IDatatype) o2).intValue(), null);		
		
		case SET:
			return setObject(o1, o2);	
			
		case SIM:
				// class similarity
			return similarity((IDatatype) o1, (IDatatype) o2);
			
		case PSIM:
			// prop similarity
			return pSimilarity((IDatatype) o1, (IDatatype) o2);
			
			
		case ANCESTOR:
			// common ancestor
			return ancestor((IDatatype) o1, (IDatatype) o2);
			
		case PPRINT:
			return pprint(exp, (IDatatype) o1, (IDatatype) o2, env);	
						
		}
		
		return null;
	}
	
	
	public Object eval(Expr exp, Environment env, Object[] args) {
		
		Object o1 = args[0];
		Object o2 = args[1];
		Object o3 = args[2];

		switch (exp.oper()){
		
		case SETP:
			return setProperty(o1, ((IDatatype) o2).intValue(), o3);	
		
		}
		
		return null;
	}
	
	
	
	IDatatype similarity(IDatatype dt1, IDatatype dt2 ){		
		Node n1 = graph.getNode(dt1.getLabel());
		Node n2 = graph.getNode(dt2.getLabel());
		if (n1 == null || n2 == null) return null;
		
		Distance distance = graph.setClassDistance();
		double dd = distance.similarity(n1, n2);
		return getValue(dd);
	}
	
	IDatatype ancestor(IDatatype dt1, IDatatype dt2 ){		
		Node n1 = graph.getNode(dt1.getLabel());
		Node n2 = graph.getNode(dt2.getLabel());
		if (n1 == null || n2 == null) return null;
		
		Distance distance = graph.setClassDistance();
		Node n = distance.ancestor(n1, n2);
		return (IDatatype) n.getValue();
	}
	
	IDatatype pSimilarity(IDatatype dt1, IDatatype dt2 ){		
		Node n1 = graph.getNode(dt1.getLabel());
		Node n2 = graph.getNode(dt2.getLabel());
		if (n1 == null || n2 == null) return null;
		
		Distance distance = graph.setPropertyDistance();
		double dd = distance.similarity(n1, n2);
		return getValue(dd);
	}
	
	
	/**
	 * Similarity of a solution with Corese method
	 * Sum distance of approximate types
	 * Divide by number of nodes and edge
	 * 
	 * TODO: cache distance in Environment during query proc
	 */
	public IDatatype similarity(Environment env){
		if (! (env instanceof Memory)) return getValue(0);
		Memory memory = (Memory) env;
		Hashtable<Node, Boolean> visit = new Hashtable<Node, Boolean>();
		Distance distance = graph.setClassDistance();

		// number of node + edge in the answer
		int count = 0;
		float dd = 0;
		
		for (Edge qEdge : memory.getQueryEdges()){

			if (qEdge != null){
				Edge edge = memory.getEdge(qEdge);
				
				if (edge != null){
					count += 1 ;
					
					for (int i=0; i<edge.nbNode(); i++){
						// count nodes only once
						Node n = edge.getNode(i);
						if (! visit.containsKey(n)){
							count += 1;
							visit.put(n, true);
						}
					}
					
					if ((graph.isType(qEdge) || env.getQuery().isRelax(qEdge)) && 
							qEdge.getNode(1).isConstant()){

						Node qtype = graph.getNode(qEdge.getNode(1).getLabel());
						Node ttype = graph.getNode(edge.getNode(1).getLabel());

						if (qtype == null){
							// query type is undefined in ontology
							qtype = qEdge.getNode(1);
						}
						if (ttype == null){
							// target type is undefined in ontology
							ttype = edge.getNode(1);
						}
						
						if (! subClassOf(ttype, qtype, env)){
							dd += distance.distance(ttype, qtype);
						}						
					}
				}
			}
		}
		
		if (dd == 0) return getValue(1);
		
		double sim = distance.similarity(dd , count);
		
		return getValue(sim);
		
	}
	
	boolean subClassOf(Node n1, Node n2, Environment env){
		if (match != null){
			return match.isSubClassOf(n1, n2, env);
		}
		return graph.isSubClassOf(n1, n2);
	}
	
	
//	Object getObject(Object o){
//		Node n = node(o);
//		if (n == null) return null;
//		return n.getObject();
//	}
//	
//	IDatatype setObject(Object o, Object v){
//		Node n = node(o);
//		if (n == null) return null;
//		n.setObject(v);
//		return TRUE;
//	}
//	
//	IDatatype setProperty(Object o, IDatatype dt, Object v){
//		Node n = node(o);
//		if (n == null) return null;
//		
//		int i = dt.intValue();
//		n.setProperty(i+Node.PSIZE, v);
//		return TRUE;
//	}
//	
//
//	Object getProperty(Object o, IDatatype dt){
//		Node n = node(o);
//		if (n == null) return null;
//		
//		int i = dt.intValue();
//		return n.getProperty(i+Node.PSIZE);
//	}
	
	
	class Table extends Hashtable<Integer, PTable>{}
	
	class PTable extends Hashtable<Object, Object>{}
	
	PTable getPTable(Integer n){
		PTable t = table.get(n);
		if (t == null){
			t = new PTable();
			table.put(n, t);
		}
		return t;
	}
	
	Object getObject(Object o){
		return getProperty(o, Node.OBJECT);
	}
	
	IDatatype setObject(Object o, Object v){
		setProperty(o, Node.OBJECT, v);
		return TRUE;
	}
	
	IDatatype setProperty(Object o, Integer n, Object v){
		PTable t = getPTable(n);
		t.put(o, v);
		return TRUE;
	}
	

	Object getProperty(Object o, Integer n){
		PTable t = getPTable(n);
		return t.get(o);
	}

	
	
	Node node(Object o){
		IDatatype dt = (IDatatype) o;
		Node n = graph.getNode(dt, false, false);
		return n;
	}
	
	IDatatype depth(Object o){
		Node n = node(o);
		if (n == null || graph.getClassDistance() == null) return null;
		Integer d = graph.getClassDistance().getDepth(n);
		if (d == null){
			return  null;
		}
		return getValue(d);
	}
	
//	IDatatype depth(Object o){
//		Node n = node(o);
//		if (n == null || n.getProperty(Node.DEPTH)== null) return null;
//		IDatatype d = getValue((Integer) n.getProperty(Node.DEPTH));
//		return d;
//	}
	
	Graph graph(){
		return graph;
	}
	
	IDatatype load(Object o){
		loader();
		IDatatype dt = (IDatatype) o;
		try {
			ld.loadWE(dt.getLabel());
		} catch (LoadException e) {
			logger.error(e);
			return FALSE;
		}
		return TRUE;
	}
	
	void loader(){
		if (ld == null){
			ld = ManagerImpl.getLoader();
			ld.init(graph);
		}
	}
	
	Mappings kgram(Object o){
		IDatatype dt = (IDatatype) o;
		String query = dt.getLabel();
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.sparqlQuery(query);
			return map;
		} catch (EngineException e) {
			return new Mappings();
		}
	}
	
	IDatatype qname(Object o, Environment env){
		IDatatype dt = (IDatatype) o;
		if (! dt.isURI()){
			return dt;
		}
		Query q = env.getQuery();
		if (q == null){
			return dt;
		}
		ASTQuery ast = (ASTQuery) q.getAST();
		NSManager nsm = ast.getNSM();
		String qname = nsm.toPrefix(dt.getLabel(), true);
		if (qname.equals(dt.getLabel())){
			return dt;
		}
		return getValue(qname);
	}
	
	
	/**
	 * Draft:
	 * Recursive pprinter by means of SPARQL queries
	 */
	IDatatype pprint(Environment env){
		PPrinter p = getPPrinter(env); 
		return p.pprint();
	}

	/**
	 * Object o is the node where to apply the query (with a Mapping)
	 * exp = kg:pprint(arg);
	 */
	IDatatype pprint(Expr exp, IDatatype o, Environment env){
		return pprint(exp, o, null, env);
	}
	
	/**
	 * t is the template to be used, it may be null
	 * exp = kg:pprint()
	 */
	IDatatype pprint(Expr exp, IDatatype o, IDatatype t, Environment env){
		String tt = null;
		if (t != null){
			tt = t.getLabel();
		}
		PPrinter p = getPPrinter(env, tt); 
		Expr arg = exp.getExp(0);
		if (! arg.isVariable()){
			arg = null;
		}
		IDatatype dt = p.pprint(arg, o);
		return dt;
	}

	IDatatype turtle(IDatatype o, Environment env){
		PPrinter p = getPPrinter(env); 
		IDatatype dt = p.turtle(o);
		return dt;
	}
	
	IDatatype uri(Expr exp, IDatatype dt, Environment env){
		if (dt.isURI()){
			return turtle(dt, env);
		}
		else {
			return pprint(exp, dt, env);
		}
	}
	

	IDatatype getLevel(Environment env){
		return getValue(level(env));
	}

	int level(Environment env){
		PPrinter p = getPPrinter(env); 
		return p.level();
	}
	
	IDatatype indent(Object o){
		return indent(datatype(o).intValue());
	}
	
	IDatatype indent(int n){
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<n; i++){
			sb.append(" ");
		}
		return getValue(sb.toString());
	}
	
	IDatatype indent(Environment env){
		return indent(level(env));
	}

	
	/**
	 * 
	 */
	PPrinter getPPrinter(Environment env){	
		return getPPrinter(env, null);
	}
	
	PPrinter getPPrinter(Environment env, String t){		
		Query q  = env.getQuery();
		Object o = q.getPP();

		if (o != null){
			return (PPrinter) o;
		}
		else {
			String p = PPRINTER;
			if (t != null){
				p = t;
			}
			else if (q.hasPragma(Pragma.TEMPLATE)){
				p = (String) q.getPragma(Pragma.TEMPLATE);
			}
			PPrinter pp = PPrinter.create(graph, p);
			ASTQuery ast = (ASTQuery) q.getAST();
			pp.setNSM(ast.getNSM());
			q.setPPrinter(pp);
			return pp;
		}
	}

	
	public void setPPrinter(String str){
		PPRINTER = str;
	}
	

}
