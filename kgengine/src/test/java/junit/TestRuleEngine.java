package junit;


import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


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
import fr.inria.edelweiss.kgraph.query.QueryEngine;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.RuleLoad;


/**
 * Test rule engines and pipeline
 *
 */
public class TestRuleEngine {
	
//	static String data = "/user/corby/home/workspace/coreseV2/src/test/resources/data/";
        static String data = TestRuleEngine.class.getClassLoader().getResource("data").getPath()+"/";
//	static String root = "/user/corby/home/workspace/kgengine/src/test/resources/data/";
        static String root = TestRuleEngine.class.getClassLoader().getResource("data").getPath()+"/";

	static Graph graph;
	static Engine rengine;
	static RuleEngine fengine;

	
	@BeforeClass
	public static void init() throws EngineException {	
		
		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");	

		graph = Graph.create(true);
		Load load = Load.create(graph);

		load.load(data + "engine/ontology/test.rdfs");
		load.load(data + "engine/data/test.rdf");
		
		try {
			load.loadWE(data + "engine/rule/test2.brul");
			load.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	public void test44(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		String ent = "select * where {graph kg:entailment {?x ?p ?y}}";

		graph.process(fengine);
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
			
			map = exec.query(ent);
			System.out.println(map);
			
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
	
	
	
	/**
	 * Rule engine with QueryExec on two graphs
	 */
	@Test
	public void test6(){
		QuerySolver.definePrefix("c", "http://www.inria.fr/acacia/comma#");	

		Graph g1 = Graph.create(true);
		Graph g2 = Graph.create(true);

		Load load1 = Load.create(g1);
		Load load2 = Load.create(g2);
		
		load1.load(data + "engine/ontology/test.rdfs");
		load2.load(data + "engine/data/test.rdf");

		QueryProcess exec = QueryProcess.create(g1);
		exec.add(g2);
		RuleEngine re = RuleEngine.create(g2, exec);
		//re.setOptimize(true);
		
		load2.setEngine(re);
		
		try {
			load2.loadWE(data + "engine/rule/test2.brul");
			load2.load(new FileInputStream(data + "engine/rule/meta.brul"), "meta.brul");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Engine rengine = Engine.create(exec);

		rengine.load(data + "engine/rule/test2.brul");
		rengine.load(data + "engine/rule/meta.brul");
		
		
		
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		
		LBind bind = rengine.SPARQLProve(query);
		assertEquals("Result", 4, bind.size());
		System.out.println(bind);
		

		re.process();
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 4, map.size());
			System.out.println(map);
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
		
	}
	
	
	
	
	@Test
	public void test7(){
		Graph g1 = Graph.create(true);
		Load load1 = Load.create(g1);
		load1.load(root + "sdk/sdk.rdf");
		
		String init = "load <" + root + "rule/server.rul> into graph kg:rule";
		String query = "select * where {?x a ?class}";
		QueryProcess exec = QueryProcess.create(g1);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 6, map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Test
	public void test8(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		QueryEngine qe = QueryEngine.create(g);
		String query = "insert data { <John> rdfs:label 'John' }";
		qe.addQuery(query);
		
		qe.process();
		
		assertEquals("Result", 1, g.size());


	}

	
	
	
	
	@Test
	public void testWF(){

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select     * where {" +
			"?x c:hasGrandParent c:Pierre " +
			"}";
		
		String ent = "select * where {graph kg:entailment {?x ?p ?y}}";

		graph.addEngine(fengine);
		
		QueryProcess exec = QueryProcess.create(graph);
		Mappings map;
		try {
			map = exec.query(query);
			assertEquals("Result", 4, map.size());
			
			map = exec.query(ent);
			System.out.println(map);
			
		} catch (EngineException e) {
			assertEquals("Result", 4, e);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
		
		
		
		
		
		
		
