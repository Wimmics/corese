package fr.inria.corese.test.engine;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.RDFFormat;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.RDFList;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.parser.Atom;

/**
 * 
 * Test AST
 */
public class TestAST {

    
        @Test
        public void testNumber(){
            IDatatype dt1 = DatatypeMap.newInstance(10);
            IDatatype res = dt1.div(DatatypeMap.newInstance(2));
            assertEquals(5, res.intValue());
            assertEquals(res.getCode(), IDatatype.DECIMAL);
            
            
        }

//	@Test
//	public void test1(){
//
//		ASTQuery ast = ASTQuery.create();
//
//		NSManager nsm = NSManager.create();
//		nsm.definePrefix("ns", "http://ns.inria.fr/schema/");
//		ast.setNSM(nsm);
//
//		Expression regex = ast.createOperator("/", ast.createConstant("rdf:type"), 
//				ast.createOperator("0", "1", ast.createConstant("rdfs:subClassOf")));
//		Constant pp = ast.createProperty(regex);
//		Exp tt  = ast.createTriple(Variable.create("?x"), pp, Variable.create("?class"));
//
//
//		Exp exp1 = BasicGraphPattern.create();
//		exp1.add(tt);
//
//		Source gg = Source.create(Variable.create("?g"), exp1);
//
//		Exp body = BasicGraphPattern.create();
//		body.add(gg);
//
//
//		ast.setSelectAll(true);
//		ast.setSelect(Variable.create("?xx"), Term.function("self", Variable.create("?x")));
//
//		// subquery
//
//		ASTQuery sub = ast.subCreate();
//		sub.setSelectAll(true);
//
//		Triple st = Triple.create( Variable.create("?x"), Variable.create("?p"), Variable.create("?z"));
//
//		Expression f = Term.create("=", Variable.create("?x"), Variable.create("?x"));
//		//Triple tf = Triple.create(f);
//
//		Exp sexp1 = BasicGraphPattern.create();
//		sexp1.add(st);
//		sexp1.add(f);
//
//		sub.setBody(sexp1);
//		Query q = Query.create(sub);
//
//		body.add(q);
//
//
//		List<Atom> ll = new ArrayList<Atom>();
//		ll.add(ast.createConstant("1", "xsd:integer"));
//		ll.add(ast.createConstant("2", "xsd:integer"));
//		RDFList l = ast.createRDFList(ll);
//
//		Triple tl = Triple.create( Variable.create("?a"), ast.createConstant("ns:test"), l.head());
//
//		body.add(tl);
//		body.add(l);
//
//		ast.setBody(body);	
//
//		List<Variable> lVar = new ArrayList<Variable>();
//		lVar.add(Variable.create("?a"));
//
//		Values values = Values.create();
//			
//		values.setVariables(lVar);
//
//		List<Constant> lValue = new ArrayList<Constant>();
//		lValue.add(ast.createConstant("a"));
//
//		values.addValues(lValue);
//		
//		ast.setValues(values);
//		
//		//System.out.println(ast);
//
//		Graph g = Graph.create();
//		QueryProcess exec = QueryProcess.create(g);
//
//		try {
//			String init = 
//				"prefix ns: <http://ns.inria.fr/schema/>" +
//				"insert data {<a> rdf:type <Person> ; ns:test (1 2)}";
//			exec.query(init);
//			
//			RDFFormat ff = RDFFormat.create(g);
//			//System.out.println(ff);
//
//			Mappings map = exec.query(ast);
//			//System.out.println(map);
//		
//			assertEquals("Result", 2, map.size());
//
//
//		} catch (EngineException e) {
//			// TODO Auto-generated catch block
//			assertEquals("Result", true, e);
//		}
//	}
//
//	
	
	
	
	
	
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
		assertEquals("Result", 1.5, dt.doubleValue(), 0);
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
			b = dt1.equalsWE(dt2);
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
		IDatatype dt2 = DatatypeMap.newLiteral("toto");
		int res = dt1.compareTo(dt2);
		assertEquals("Result", true, (res>0));
		res = dt2.compareTo(dt1);
		assertEquals("Result", true, (res<0));
	}

	@Test
	public void test13(){
		IDatatype dt1 = DatatypeMap.newLiteral("toto");
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
		IDatatype dt1 = DatatypeMap.newLiteral("2009-12-11");
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
		IDatatype dt3 = DatatypeMap.newLiteral("c");
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
	//System.out.println(f);
}
	
	
@Test
public void test21(){
	IDatatype dt1 = DatatypeMap.createLiteral("test", RDF.rdflangString, "en");
	IDatatype dt2 = DatatypeMap.createLiteral("test", RDF.rdflangString, "fr");
	IDatatype dt3 = DatatypeMap.newLiteral("test");

	try {
		assertEquals("Result", true, (dt1.equalsWE(dt1)));
		assertEquals("Result", false, (dt1.sameTerm(dt2)));
		assertEquals("Result", false, (dt1.sameTerm(dt3)));
		assertEquals("Result", RDF.rdflangString, dt1.getDatatypeURI());
		
		Constant cst = Constant.create(dt1);

		assertEquals("Result", true, (dt1.equalsWE(cst.getDatatypeValue())));
		
		cst = Constant.create("test", RDF.rdflangString, "en");
		
		assertEquals("Result", true, (dt1.equalsWE(cst.getDatatypeValue())));


	} catch (CoreseDatatypeException e) {
		// TODO Auto-generated catch block
		assertEquals("Result", true, e);
	}

	
}


	
	public void test22(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)1);
		IDatatype dt3 = DatatypeMap.newInstance((double)1);
		IDatatype dt4 = DatatypeMap.newInstance((float)1);

		
		
		assertEquals("Result", true, dt1.sameTerm(dt1));
		assertEquals("Result", true, dt1.sameTerm(dt2));
		assertEquals("Result", true, dt1.sameTerm(dt3));
		assertEquals("Result", true, dt1.sameTerm(dt4));

		assertEquals("Result", true, dt2.sameTerm(dt1));
		assertEquals("Result", true, dt2.sameTerm(dt2));
		assertEquals("Result", true, dt2.sameTerm(dt3));
		assertEquals("Result", true, dt2.sameTerm(dt4));
		
		assertEquals("Result", true, dt3.sameTerm(dt1));
		assertEquals("Result", true, dt3.sameTerm(dt2));
		assertEquals("Result", true, dt3.sameTerm(dt3));
		assertEquals("Result", true, dt3.sameTerm(dt4));

		
		assertEquals("Result", true, dt4.sameTerm(dt1));
		assertEquals("Result", true, dt4.sameTerm(dt2));
		assertEquals("Result", true, dt4.sameTerm(dt3));
		assertEquals("Result", true, dt4.sameTerm(dt4));
		
	}




	@Test
	public void test23(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)2);
		IDatatype dt3 = DatatypeMap.newInstance((double)3);
		IDatatype dt4 = DatatypeMap.newInstance((float)4);
		IDatatype dt5 = DatatypeMap.createLiteral("5", RDFS.xsddecimal);


		
		try {
			
			assertEquals("Result", false, dt1.less(dt1));
			assertEquals("Result", true, dt1.less(dt2));
			assertEquals("Result", true, dt1.less(dt3));
			assertEquals("Result", true, dt1.less(dt4));
			assertEquals("Result", true, dt1.less(dt5));

			
			assertEquals("Result", false, dt2.less(dt1));
			assertEquals("Result", false, dt2.less(dt2));
			assertEquals("Result", true, dt2.less(dt3));
			assertEquals("Result", true, dt2.less(dt4));
			assertEquals("Result", true, dt2.less(dt5));
			
			assertEquals("Result", false, dt3.less(dt1));
			assertEquals("Result", false, dt3.less(dt2));
			assertEquals("Result", false, dt3.less(dt3));
			assertEquals("Result", true, dt3.less(dt4));
			assertEquals("Result", true, dt3.less(dt5));
			
			
			assertEquals("Result", false, dt4.less(dt1));
			assertEquals("Result", false, dt4.less(dt2));
			assertEquals("Result", false, dt4.less(dt3));
			assertEquals("Result", false, dt4.less(dt4));
			assertEquals("Result", true, dt4.less(dt5));
			
			assertEquals("Result", false, dt5.less(dt1));
			assertEquals("Result", false, dt5.less(dt2));
			assertEquals("Result", false, dt5.less(dt3));
			assertEquals("Result", false, dt5.less(dt4));
			assertEquals("Result", false, dt5.less(dt5));
			
			
			
			
			
			
			
		}
		catch (CoreseDatatypeException e){
			assertEquals("Result", true, e);

		}
		
	}




	@Test
	public void test24(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)2);
		IDatatype dt3 = DatatypeMap.newInstance((double)3);
		IDatatype dt4 = DatatypeMap.newInstance((float)4);
		IDatatype dt5 = DatatypeMap.createLiteral("5", RDFS.xsddecimal);


		
		try {
			
			assertEquals("Result", true, dt1.lessOrEqual(dt1));
			assertEquals("Result", true, dt1.lessOrEqual(dt2));
			assertEquals("Result", true, dt1.lessOrEqual(dt3));
			assertEquals("Result", true, dt1.lessOrEqual(dt4));
			assertEquals("Result", true, dt1.lessOrEqual(dt5));

			
			assertEquals("Result", false, dt2.lessOrEqual(dt1));
			assertEquals("Result", true, dt2.lessOrEqual(dt2));
			assertEquals("Result", true, dt2.lessOrEqual(dt3));
			assertEquals("Result", true, dt2.lessOrEqual(dt4));
			assertEquals("Result", true, dt2.lessOrEqual(dt5));
			
			assertEquals("Result", false, dt3.lessOrEqual(dt1));
			assertEquals("Result", false, dt3.lessOrEqual(dt2));
			assertEquals("Result", true, dt3.lessOrEqual(dt3));
			assertEquals("Result", true, dt3.lessOrEqual(dt4));
			assertEquals("Result", true, dt3.lessOrEqual(dt5));
			
			
			assertEquals("Result", false, dt4.lessOrEqual(dt1));
			assertEquals("Result", false, dt4.lessOrEqual(dt2));
			assertEquals("Result", false, dt4.lessOrEqual(dt3));
			assertEquals("Result", true, dt4.lessOrEqual(dt4));
			assertEquals("Result", true, dt4.lessOrEqual(dt5));
			
			assertEquals("Result", false, dt5.lessOrEqual(dt1));
			assertEquals("Result", false, dt5.lessOrEqual(dt2));
			assertEquals("Result", false, dt5.lessOrEqual(dt3));
			assertEquals("Result", false, dt5.lessOrEqual(dt4));
			assertEquals("Result", true, dt5.lessOrEqual(dt5));
		
		}
		catch (CoreseDatatypeException e){
			assertEquals("Result", true, e);

		}
		
	}


	@Test
	public void test25(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)2);
		IDatatype dt3 = DatatypeMap.newInstance((double)3);
		IDatatype dt4 = DatatypeMap.newInstance((float)4);
		IDatatype dt5 = DatatypeMap.createLiteral("5", RDFS.xsddecimal);


		
		try {
			
			assertEquals("Result", false, dt1.greater(dt1));
			assertEquals("Result", false, dt1.greater(dt2));
			assertEquals("Result", false, dt1.greater(dt3));
			assertEquals("Result", false, dt1.greater(dt4));
			assertEquals("Result", false, dt1.greater(dt5));

			
			assertEquals("Result", true, dt2.greater(dt1));
			assertEquals("Result", false, dt2.greater(dt2));
			assertEquals("Result", false, dt2.greater(dt3));
			assertEquals("Result", false, dt2.greater(dt4));
			assertEquals("Result", false, dt2.greater(dt5));
			
			assertEquals("Result", true, dt3.greater(dt1));
			assertEquals("Result", true, dt3.greater(dt2));
			assertEquals("Result", false, dt3.greater(dt3));
			assertEquals("Result", false, dt3.greater(dt4));
			assertEquals("Result", false, dt3.greater(dt5));
			
			
			assertEquals("Result", true, dt4.greater(dt1));
			assertEquals("Result", true, dt4.greater(dt2));
			assertEquals("Result", true, dt4.greater(dt3));
			assertEquals("Result", false, dt4.greater(dt4));
			assertEquals("Result", false, dt4.greater(dt5));
			
			assertEquals("Result", true, dt5.greater(dt1));
			assertEquals("Result", true, dt5.greater(dt2));
			assertEquals("Result", true, dt5.greater(dt3));
			assertEquals("Result", true, dt5.greater(dt4));
			assertEquals("Result", false, dt5.greater(dt5));
		
		}
		catch (CoreseDatatypeException e){
			assertEquals("Result", true, e);

		}
		
	}



	@Test
	public void test26(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)2);
		IDatatype dt3 = DatatypeMap.newInstance((double)3);
		IDatatype dt4 = DatatypeMap.newInstance((float)4);
		IDatatype dt5 = DatatypeMap.createLiteral("5", RDFS.xsddecimal);


		
		try {
			
			assertEquals("Result", true, dt1.greaterOrEqual(dt1));
			assertEquals("Result", false, dt1.greaterOrEqual(dt2));
			assertEquals("Result", false, dt1.greaterOrEqual(dt3));
			assertEquals("Result", false, dt1.greaterOrEqual(dt4));
			assertEquals("Result", false, dt1.greaterOrEqual(dt5));

			
			assertEquals("Result", true, dt2.greaterOrEqual(dt1));
			assertEquals("Result", true, dt2.greaterOrEqual(dt2));
			assertEquals("Result", false, dt2.greaterOrEqual(dt3));
			assertEquals("Result", false, dt2.greaterOrEqual(dt4));
			assertEquals("Result", false, dt2.greaterOrEqual(dt5));
			
			assertEquals("Result", true, dt3.greaterOrEqual(dt1));
			assertEquals("Result", true, dt3.greaterOrEqual(dt2));
			assertEquals("Result", true, dt3.greaterOrEqual(dt3));
			assertEquals("Result", false, dt3.greaterOrEqual(dt4));
			assertEquals("Result", false, dt3.greaterOrEqual(dt5));
			
			
			assertEquals("Result", true, dt4.greaterOrEqual(dt1));
			assertEquals("Result", true, dt4.greaterOrEqual(dt2));
			assertEquals("Result", true, dt4.greaterOrEqual(dt3));
			assertEquals("Result", true, dt4.greaterOrEqual(dt4));
			assertEquals("Result", false, dt4.greaterOrEqual(dt5));
			
			assertEquals("Result", true, dt5.greaterOrEqual(dt1));
			assertEquals("Result", true, dt5.greaterOrEqual(dt2));
			assertEquals("Result", true, dt5.greaterOrEqual(dt3));
			assertEquals("Result", true, dt5.greaterOrEqual(dt4));
			assertEquals("Result", true, dt5.greaterOrEqual(dt5));
		
		}
		catch (CoreseDatatypeException e){
			assertEquals("Result", true, e);

		}
		
	}
	
	
	
	@Test
	public void test27(){
		IDatatype dt1 = DatatypeMap.newInstance((int)1);
		IDatatype dt2 = DatatypeMap.newInstance((long)2);
		IDatatype dt3 = DatatypeMap.newInstance((double)3);
		IDatatype dt4 = DatatypeMap.newInstance((float)4);
		IDatatype dt5 = DatatypeMap.createLiteral("5", RDFS.xsddecimal);


		
		try {
			
			assertEquals("Result", true, dt1.greaterOrEqual(dt1));
			assertEquals("Result", false, dt1.greaterOrEqual(dt2));
			assertEquals("Result", false, dt1.greaterOrEqual(dt3));
			assertEquals("Result", false, dt1.greaterOrEqual(dt4));
			assertEquals("Result", false, dt1.greaterOrEqual(dt5));

			
			assertEquals("Result", true, dt2.greaterOrEqual(dt1));
			assertEquals("Result", true, dt2.greaterOrEqual(dt2));
			assertEquals("Result", false, dt2.greaterOrEqual(dt3));
			assertEquals("Result", false, dt2.greaterOrEqual(dt4));
			assertEquals("Result", false, dt2.greaterOrEqual(dt5));
			
			assertEquals("Result", true, dt3.greaterOrEqual(dt1));
			assertEquals("Result", true, dt3.greaterOrEqual(dt2));
			assertEquals("Result", true, dt3.greaterOrEqual(dt3));
			assertEquals("Result", false, dt3.greaterOrEqual(dt4));
			assertEquals("Result", false, dt3.greaterOrEqual(dt5));
			
			
			assertEquals("Result", true, dt4.greaterOrEqual(dt1));
			assertEquals("Result", true, dt4.greaterOrEqual(dt2));
			assertEquals("Result", true, dt4.greaterOrEqual(dt3));
			assertEquals("Result", true, dt4.greaterOrEqual(dt4));
			assertEquals("Result", false, dt4.greaterOrEqual(dt5));
			
			assertEquals("Result", true, dt5.greaterOrEqual(dt1));
			assertEquals("Result", true, dt5.greaterOrEqual(dt2));
			assertEquals("Result", true, dt5.greaterOrEqual(dt3));
			assertEquals("Result", true, dt5.greaterOrEqual(dt4));
			assertEquals("Result", true, dt5.greaterOrEqual(dt5));
		
		}
		catch (CoreseDatatypeException e){
			assertEquals("Result", true, e);

		}
		
	}

	
	@Test
	public void test28(){
		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		String init = "insert data {" +
				"[rdf:value '1'^^xsd:short, '1'^^xsd:int, 1.0, 1]" +
				"}";
		
		String query = 
			"select * where {" +
			"?x rdf:value ?y filter(datatype(?y) = xsd:integer)}";
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			assertEquals("Result", 1, map.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	@Test
	public void test29(){

		NSManager nsm = NSManager.create();
		nsm.definePrefix("foaf", "http://foaf.org/");
		

		ASTQuery ast = ASTQuery.create();
		ast.setNSM(nsm);

		Triple t1 = Triple.create(Variable.create("?x"), ast.createQName("foaf:knows"), Variable.create("?y"));
		Triple t2 = Triple.create(Variable.create("?y"), ast.createQName("foaf:knows"), Variable.create("?x"));

		ast.setBody(BasicGraphPattern.create(t1));
		ast.setConstruct(BasicGraphPattern.create(t2));


		String init = 
			"prefix foaf: <http://foaf.org/>" +
			"insert data {<John> foaf:knows <Jim>" +
			"<John> owl:sameAs <Johnny>}";
		
		String query = 
			"prefix foaf: <http://foaf.org/>" +
			"construct {?y foaf:knows ?x}" +
			"where {?x foaf:knows ?y}";
		

		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		try {
			exec.query(init);
			Mappings map =  exec.query(ast);
			RDFFormat f = RDFFormat.create(map);
			
//			System.out.println(ast);
//			System.out.println(map);
//			System.out.println(f);
			assertEquals("Result", map.size(), 1);
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}


	@Test
	public void test291(){

		NSManager nsm = NSManager.create();
		nsm.definePrefix("foaf", "http://foaf.org/");
		

		ASTQuery ast = ASTQuery.create();
		ast.setNSM(nsm);

		Triple t1 = Triple.create(Constant.createBlank("_:b2"), ast.createQName("rdfs:seeAlso"), Variable.create("?z"));
		Triple t2 = Triple.create(Variable.create("?z"), ast.createQName("foaf:knows"), Variable.create("?x"));

		ast.setBody(BasicGraphPattern.create(t2));
		ast.setConstruct(BasicGraphPattern.create(t1));


		String init = 
			"prefix foaf: <http://foaf.org/>" +
			"insert data {<John> foaf:knows <Jim>" +
			"<John> owl:sameAs <Johnny>}";
		
		String query = 
			"prefix foaf: <http://foaf.org/>" +
			"construct {?y foaf:knows ?x}" +
			"where {?x foaf:knows ?y}";
		

		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		try {
			exec.query(init);
			Mappings map =  exec.query(ast);
			RDFFormat f = RDFFormat.create(map);
			
//			System.out.println(ast);
//			System.out.println(map);
//			System.out.println(f);
			assertEquals("Result", 1, map.size());
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}

	
	
	@Test
	public void test30(){

		NSManager nsm = NSManager.create();
		nsm.definePrefix("foaf", "http://foaf.org/");
		

		ASTQuery ast = ASTQuery.create();
		ast.setNSM(nsm);

		Triple t1 = Triple.create(Variable.create("?x"), ast.createQName("foaf:knows"), Variable.create("?y"));

		ast.setBody(BasicGraphPattern.create(t1));
		
		ast.setDescribe(Variable.create("?x"));

		String init = 
			"prefix foaf: <http://foaf.org/>" +
			"insert data {<John> foaf:knows <Jim>" +
			"<John> owl:sameAs <Johnny>}";
		
		String query = 
			"prefix foaf: <http://foaf.org/>" +
			"construct {?y foaf:knows ?x}" +
			"where {?x foaf:knows ?y}";
		

		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		try {
			exec.query(init);
			Mappings map =  exec.query(ast);
			RDFFormat f = RDFFormat.create(map);
			
//			System.out.println(ast);
//			System.out.println(map);
//			System.out.println(f);
			assertEquals("Result", map.size(), 2);
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}

	
	
	@Test
	public void test31(){

		NSManager nsm = NSManager.create();
		nsm.definePrefix("foaf", "http://foaf.org/");
		

		ASTQuery ast = ASTQuery.create();
		ast.setNSM(nsm);

		Triple t1 = Triple.create(Variable.create("?x"), ast.createQName("foaf:knows"), Variable.create("?y"));

		ast.setBody(BasicGraphPattern.create(t1));
		
		ast.setAsk(true);

		String init = 
			"prefix foaf: <http://foaf.org/>" +
			"insert data {<John> foaf:knows <Jim>" +
			"<John> owl:sameAs <Johnny>}";

		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);

		try {
			exec.query(init);
			Mappings map =  exec.query(ast);
			ResultFormat f = ResultFormat.create(map);
			
//			System.out.println(ast);
//			System.out.println(map);
//			System.out.println(f);
			assertEquals("Result", map.size(), 1);
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}
	
	
	
	@Test
	public void test32(){
		
		ASTQuery ast = ASTQuery.create();
		ast.definePrefix("foaf", "http://foaf.org/");

		Triple t1 = Triple.create(Constant.createBlank("_:b1"), ast.createQName("foaf:knows"), Variable.create("?y"));
		Triple t2 = Triple.create(Constant.createBlank("_:b2"), ast.createQName("rdfs:seeAlso"), Variable.create("?y"));
				
		ast.setConstruct(BasicGraphPattern.create(t2));
		ast.setBody(BasicGraphPattern.create(t1));
		ast.setRule(true);
		
		//System.out.println(ast);

		String init = 
			"prefix foaf: <http://foaf.org/>" +
			"insert data {<John> foaf:knows <Jim>" +
			"<John> owl:sameAs <Johnny>}";
		
		String query = "select * where {?x rdfs:seeAlso ?y}";

		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		RuleEngine re = RuleEngine.create(g);
		//re.setDebug(true);

		re.addRule(ast.toString());
		
		try {
			exec.query(init);
			re.process();
			
			Mappings map =  exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			
//			System.out.println(map);
//			System.out.println(f);
			assertEquals("Result", 1, map.size());
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}
	
	public Object fun (Object obj){
		return DatatypeMap.TRUE;
	}
	
	
	
	
	
	
	
	
	
}
