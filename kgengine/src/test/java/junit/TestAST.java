package junit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.CoreseFloat;
import fr.inria.acacia.corese.cg.datatype.CoreseInteger;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Query;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;

/**
 * 
 * Test AST
 */
public class TestAST {


	@Test
	public void test1(){

		ASTQuery ast = ASTQuery.create();

		NSManager nsm = NSManager.create();
		nsm.definePrefix("ns", "http://ns.inria.fr/schema/");
		ast.setNSM(nsm);

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

		List<Variable> lVar = new ArrayList<Variable>();
		lVar.add(Variable.create("?a"));

		ast.setVariableBindings(lVar);

		List<Constant> lValue = new ArrayList<Constant>();
		lValue.add(ast.createConstant("a"));

		ast.setValueBindings(lValue);
		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		try {
			String query = 
				"prefix ns: <http://ns.inria.fr/schema/>" +
				"insert data {<a> rdf:type <Person> ; ns:test (1 2)}";
			exec.query(query);

			Mappings map = exec.query(ast);
			
			assertEquals("Result", map.size(), 2);

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}

	
	
	@Test
	public void test2(){
		IDatatype dt1 = DatatypeMap.newInstance((float)1.5);
		IDatatype dt2 = DatatypeMap.newInstance(2);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", dt.getDatatypeURI(), RDFS.xsdfloat);
	}
	
	@Test
	public void test3(){
		IDatatype dt1 = DatatypeMap.newInstance((double)1.5);
		IDatatype dt2 = DatatypeMap.newInstance(2);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", dt.getDatatypeURI(), RDFS.xsddouble);
	}
	
	@Test
	public void test4(){
		IDatatype dt1 = DatatypeMap.newInstance(1.5, RDFS.xsddecimal);
		IDatatype dt2 = DatatypeMap.newInstance(2);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", dt.getDatatypeURI(), RDFS.xsddecimal);
	}
	
	
	@Test
	public void test5(){
		IDatatype dt1 = DatatypeMap.newInstance(1.5, RDFS.xsddecimal);
		IDatatype dt2 = DatatypeMap.newInstance((float)2);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", dt.getDatatypeURI(), RDFS.xsdfloat);
	}
	
	
	@Test
	public void test6(){
		IDatatype dt1 = DatatypeMap.newInstance(3);
		IDatatype dt2 = DatatypeMap.newInstance(2, RDFS.xsddecimal);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", RDFS.xsddecimal, dt.getDatatypeURI());
	}
	
	@Test
	public void test7(){
		IDatatype dt1 = DatatypeMap.newInstance(3);
		IDatatype dt2 = DatatypeMap.newInstance(2);
		IDatatype dt = dt1.div(dt2);
		assertEquals("Result", 1.5, dt.getDoubleValue(), 0);
	}
	
	
	
	
	


}
