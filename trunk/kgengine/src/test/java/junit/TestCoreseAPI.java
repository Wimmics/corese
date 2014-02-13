package junit;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;

public class TestCoreseAPI {
	
//	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        static String data = TestCoreseAPI.class.getClassLoader().getResource("data").getPath()+"/";

	static IEngine engine;
	
	@BeforeClass
	static public void init(){
		engine = new EngineFactory().newInstance();
		GraphEngine ge = (GraphEngine) engine;
		ge.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		ge.setListGroup(true);
		try {
			engine.load(data + "comma/comma.rdfs");
			engine.load(data + "comma/model.rdf");
			engine.load(data + "comma/data");
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void test1(){
		
		String query = "select * where {" +
				"?x c:hasCreated ?doc" +
				"}";
		
		try {
			IResults res = engine.SPARQLQuery(query);
			
			for (IResult rr : res){
				IDatatype dt = rr.getDatatypeValue("?x");
				assertEquals("Result", dt, dt);
			}
			
			assertEquals("Result", 4, res.size());
		} catch (EngineException e) {
			System.out.println(e);
			assertEquals("Result", 4, e);
		}
		
	}
	
	@Test
	public void test2(){
		
		String query = "select * where {" +
				"?x c:hasCreated ?doc" +
				"}" +
				"group by ?x" ;
		
		try {
			IResults res = engine.SPARQLQuery(query);
			System.out.println(res);

			for (IResult rr : res){
				IResultValue[] val = rr.getResultValues("?doc");
				assertEquals("Result", true, val.length>0);
			}
			
			assertEquals("Result", 3, res.size());
		} catch (EngineException e) {
			System.out.println(e);
			assertEquals("Result", 3, e);
		}
		
	}
	
	
	
	
	
	@Test
	public void test3(){
		
		String query = "construct {?x c:hasCreated ?doc} where {" +
				"?x c:hasCreated ?doc" +
				"}";
		
		try {
			IResults res = engine.SPARQLQuery(query);
						
			System.out.println(res);
			
			assertEquals("Result", true, res.size()>0);
		} catch (EngineException e) {
			System.out.println(e);
			assertEquals("Result", true, e);
		}
		
	}
	
	@Test
	public void test4(){
		EngineFactory ef = new EngineFactory(); 
		IEngine semengine1 = ef.newInstance(); 
		IEngine semengine2 = ef.newInstance(); 

	
		QueryExec queryAgent = QueryExec.create(); 
		queryAgent.setListGroup(false); 
		queryAgent.add( semengine1 ); 
		queryAgent.add( semengine2 ); 
		
		String init = "insert data {graph <test> {<John> <name> 'John'}}";
		try {
			queryAgent.update(init);
			
			GraphEngine e = (GraphEngine) semengine1;
			Graph g = e.getGraph();
			g.init();
			
//			RDFFormat f = RDFFormat.create(g);
//			System.out.println(f);
												
			assertEquals("Result", 3, g.size());
			
			
			String query = "select * where {graph <test> {?x ?p ?y}}";
			IResults res = queryAgent.SPARQLQuery(query);
			assertEquals("Result", 1, res.size());

			
			query = "select * where {?p rdf:type rdf:Property}";
			
			res = queryAgent.SPARQLQuery(query);
//			System.out.println("** Res: " );
//			System.out.println(res);
			assertEquals("Result", 2, res.size());
			
			
			String update = "delete {?x ?p ?y} where {?x ?p ?y}";
			queryAgent.update(update);
						
			
			res = queryAgent.SPARQLQuery(query);
			System.out.println(res);
			assertEquals("Result", 0, res.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	

}
