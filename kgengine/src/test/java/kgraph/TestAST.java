package kgraph;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Or;
import fr.inria.acacia.corese.triple.parser.Query;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.MapperSQL;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;


	public class TestAST {

		public static void main(String[] args){
			new TestAST().process();
		}
		
		void process(){
			
		ASTQuery ast = ASTQuery.create();
		
		NSManager nsm = NSManager.create();
		nsm.definePrefix("ns", "http://ns.inria.fr/schema/");
		ast.setNSM(nsm);
		
		
		// construct
		Triple t2 = Triple.create( Variable.create("?x"), ast.createConstant("ns:test"), Variable.create("?y"));
		t2 = Triple.create( Variable.create("?x"), ast.createConstant("ns:test"), ast.createConstant("12", "xsd:integer", null));
		
		
		Exp exp2 = BasicGraphPattern.create(t2);
		//ast.setConstruct(exp2);
		
		
		Or.create();Constant.create(12);
		
		//ast.setNamed(ast.createConstant(Entailment.DEFAULT));
		

		// where
		Triple t1 = Triple.create( Variable.create("?x"), ast.createConstant("rdf:type"), Variable.create("?y"));
		
		
		Expression regex = ast.createOperator("/", ast.createConstant("rdf:type"), 
				ast.createOperator("0", "1", ast.createConstant("rdfs:subClassOf")));
		Constant pp = ast.createProperty(regex);
		Exp tt  = ast.createTriple(Variable.create("?x"), pp, Variable.create("?class"));
		
		
		
		
		
		
		Exp exp1 = BasicGraphPattern.create();
		exp1.add(tt);
		
		Source gg = Source.create(Variable.create("?g"), exp1);
		
		Exp body = BasicGraphPattern.create();
		body.add(gg);

		
		ast.setSelectAll(true);
		ast.setSelect(Variable.create("?xx"), Term.function("self", Variable.create("?x")));
		
		// subquery
		
		ASTQuery sub = ast.subCreate();
		sub.setSelectAll(true);
		
		Triple st = Triple.create( Variable.create("?x"), Variable.create("?p"), Variable.create("?z"));
		
		Expression f = Term.create("=", Variable.create("?x"), Variable.create("?x"));
		Triple tf = Triple.create(f);
		
		Exp sexp1 = BasicGraphPattern.create();
		sexp1.add(st);
		sexp1.add(tf);

		sub.setBody(sexp1);
		Query q = Query.create(sub);
		
		body.add(q);
		
		
		List<Expression> ll = new ArrayList<Expression>();
		ll.add(ast.createConstant("1", "xsd:integer"));
		ll.add(ast.createConstant("2", "xsd:integer"));
		RDFList l = ast.createRDFList(ll);
		
		Triple tl = Triple.create( Variable.create("?a"), ast.createConstant("ns:test"), l.head());

		body.add(tl);
		body.add(l);

		ast.setBody(body);	
		
//		List<Variable> lVar = new ArrayList<Variable>();
//		lVar.add(Variable.create("?a"));
//
//		ast.setVariableBindings(lVar);
//
//		List<Constant> lValue = new ArrayList<Constant>();
//		lValue.add(ast.createConstant("a"));
//
//		ast.setValueBindings(lValue);
		

		
//		System.out.println(ast);
//		
//		ast.setDescribe(Variable.create("?x"));
		
		//ast.setResultForm(ASTQuery.QT_ASK);

		//ast.setGroup(Variable.create("?x"));
		
		// check it:
		IEngine engine = new EngineFactory().newInstance();
		
		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		ProducerImpl p = (ProducerImpl) exec.getProducer();
		//p.set(new MapperImpl(p));
		
		try {
			String query = 
				"prefix ns: <http://ns.inria.fr/schema/>" +
				"insert data {<a> rdf:type <Person> ; ns:test (1 2)}";
			engine.SPARQLQuery(query);
			
			IResults res = engine.SPARQLQuery(ast);
			System.out.println(res);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
			
		}
	
	
	
}
