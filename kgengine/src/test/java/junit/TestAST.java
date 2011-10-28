package junit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.CoreseFloat;
import fr.inria.acacia.corese.cg.datatype.CoreseInteger;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
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
import fr.inria.edelweiss.kgtool.print.RDFFormat;

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
	
	
	@Test
	public void test8(){
		IDatatype dt1 = DatatypeMap.createLiteral("toto", null, "en");
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "fr");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", -1, res);
		try {
			boolean b = dt1.less(dt2);
			assertEquals("Result", false, b);
			b = dt1.equals(dt2);
			assertEquals("Result", false, b);
			b = dt1.greater(dt2);
			assertEquals("Result", false, b);

		} catch (CoreseDatatypeException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", false, false);
		}
	}
	
	@Test
	public void test9(){
		IDatatype dt1 = DatatypeMap.createLiteral("toto", null, "fr");
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "en");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", 1, res);
		res = dt2.compareTo(dt1);
		assertEquals("Result", -1, res);
		
	}
	
	@Test
	public void test10(){
		IDatatype dt1 = DatatypeMap.createLiteral("tata", null, "en");
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "fr");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test11(){
		IDatatype dt1 = DatatypeMap.createLiteral("tata", null, "fr");
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "en");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test12(){
		IDatatype dt1 = DatatypeMap.createLiteral("tutu", null, "fr");
		IDatatype dt2 = DatatypeMap.createLiteral("toto");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res>0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res<0));
	}

	@Test
	public void test13(){
		IDatatype dt1 = DatatypeMap.createLiteral("toto");
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "fr");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test14(){
		IDatatype dt1 = DatatypeMap.createLiteral("toto", RDFS.xsdstring);
		IDatatype dt2 = DatatypeMap.createLiteral("toto", null, "fr");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	
	@Test
	public void test15(){
		IDatatype dt1 = DatatypeMap.createLiteral("2009-10-11", RDFS.xsddate);
		IDatatype dt2 = DatatypeMap.createLiteral("2009-11-11", RDFS.xsddate);
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test16(){
		IDatatype dt1 = DatatypeMap.createLiteral("2009-10-11", RDFS.xsddate);
		IDatatype dt2 = DatatypeMap.createLiteral("2009-10-11", RDFS.xsddate);
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res==0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res==0));
	}
	
	@Test
	public void test17(){
		IDatatype dt1 = DatatypeMap.createLiteral("2009-12-11", RDFS.xsddate);
		IDatatype dt2 = DatatypeMap.createLiteral("2009-11-11", RDFS.xsddate);
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test18(){
		IDatatype dt1 = DatatypeMap.createLiteral("2009-12-11");
		IDatatype dt2 = DatatypeMap.createLiteral("2009-11-11", RDFS.xsddate);
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res>0));
	}
	
	@Test
	public void test19(){

		IDatatype dt1 = DatatypeMap.createBlank("b");
		IDatatype dt2 = DatatypeMap.createResource("a");
		IDatatype dt3 = DatatypeMap.createLiteral("c");
		IDatatype dt4 = DatatypeMap.newInstance(1);
		
		IDatatype dt5 = DatatypeMap.createLiteral("b", "undef1");
		IDatatype dt6 = DatatypeMap.createLiteral("b", "undef1");
		IDatatype dt7 = DatatypeMap.createLiteral("a", "undef2");


		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res<0));
		res = dt2.compareTo(dt3);
		assertEquals("Result", true, (res<0));
		res = dt1.compareTo(dt3);
		assertEquals("Result", true, (res<0));
		res = dt1.compareTo(dt4);
		assertEquals("Result", true, (res<0));
		res = dt3.compareTo(dt4);
		assertEquals("Result", true, (res<0));
		
		res = dt5.compareTo(dt6);
		assertEquals("Result", true, (res==0));
		res = dt5.compareTo(dt7);
		assertEquals("Result", true, (res<0));
		
		
	}
	
	
@Test
public void test20() throws EngineException{
	Graph g = Graph.create();
	QueryProcess exec = QueryProcess.create(g);
	
	String query = "insert data {" +
			"<John>  rdf:value 1 " +
			"<James> rdf:value '1.0'^^xsd:decimal " +
			"}";
	
	exec.query(query);
	
	RDFFormat f = RDFFormat.create(g);
	System.out.println(f);
}
	
	
	

}
