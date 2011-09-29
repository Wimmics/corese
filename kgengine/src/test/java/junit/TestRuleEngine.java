package junit;


import static org.junit.Assert.assertEquals;


import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.exceptions.*;

import fr.inria.edelweiss.engine.core.Engine;
import fr.inria.edelweiss.engine.model.api.LBind;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgpipe.Pipe;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;


/**
 * Test rule engines and pipeline
 *
 */
public class TestRuleEngine {
	
	static String data = "/user/corby/home/workspace/coreseV2/src/test/resources/data/";
	static String root = "/user/corby/home/workspace/kgengine/src/test/resources/data/";

	static Graph graph;
	static Engine rengine;
	static RuleEngine fengine;

	
	@BeforeClass
	public static void init() throws EngineException {	
		
		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");	

		graph = Graph.create(true);
		Loader load = Load.create(graph);

		load.load(data + "engine/ontology/test.rdfs");
		load.load(data + "engine/data/test.rdf");
		
		load.load(data + "engine/rule/test2.brul");
		load.load(data + "engine/rule/meta.brul");
		fengine = load.getRuleEngine();

		QueryProcess exec = QueryProcess.create(graph);
		rengine = Engine.create(exec);

		rengine.load(data + "engine/rule/test2.brul");
		rengine.load(data + "engine/rule/meta.brul");
	}
	
	
	@Test
	public void test1(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select ?x ?y where { " +
			"?y c:hasSister ?z" +
			"?x c:hasBrother ?y " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 13, bind.size());
	}
	
	
	@Test
	public void test2(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 4, bind.size());
	}
	
	
	@Test
	public void test3(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre ?x c:hasID ?id " +
			"}";

		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 0, bind.size());
	}
	
	
	@Test
	public void test4(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";

		fengine.process();
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
	}
	
	
	
	@Test
	public void test5(){

		Graph g = Graph.create();
		Pipe pipe = Pipe.create(g);
		pipe.load(root + "pipe/pipe.rdf");
		pipe.process();
		
		QueryProcess exec = QueryProcess.create(g);
		String query = "select * where {graph ?g {?x ?p ?y}}";
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 9, map.size());
		} catch (EngineException e) {
			assertEquals("Result", 9, e);
		}
		
	}
	
	
	
	
	
	
	
	


}
		
		
		
		
		
		
		