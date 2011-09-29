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
import fr.inria.edelweiss.kgraph.query.QueryProcess;

public class TestCoreseAPI {
	
	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

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
	
	
	
	
	

}
