package junit;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.ResultFormat;

public class TestUnit {
	
	static String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
	static String text = "/home/corby/workspace/kgengine/src/test/resources/text/";

	
	
	/**
	 * select where { ?x c:FamilyName 'Corby'  ?x c:isMemberOf ?org  
	 * ?x c:FirstName ?name  filter  (?name = 'toto' || ?org ~ 'inria' )} 
	 */

	Graph init(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create();
		graph.set(Entailment.DATATYPE_INFERENCE, true);

		Load load = Load.create(graph);
		graph.setOptimize(true);
		System.out.println("load");

		load.load(data + "kgraph/rdf.rdf",  RDF.RDF);
		load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
		load.load(data + "comma/comma.rdfs");
		//load.load(data + "comma/commatest.rdfs");
		load.load(data + "comma/model.rdf");
		load.load(data + "comma/testrdf.rdf");
		load.load(data + "comma/data");
		//load.load(data + "comma/data2");
		
		try {
			load.loadWE(root + "rule/rdfs.rul");
			load.loadWE(root + "rule/owl.rul");
			
			//load.loadWE(root + "rule/tmp.rul");

		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("init");
		long t1 = new Date().getTime();
		graph.init();
		long t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");
		
		System.out.println("rule");

		 t1 = new Date().getTime();
		RuleEngine re = load.getRuleEngine();
		int nb = re.process();
		 t2 = new Date().getTime();
		System.out.println("** Time: " + (t2-t1) / 1000.0 + "s");
		System.out.println("** Size: " +graph.size());
		System.out.println("** Rule Entailment: " + nb);
//		
		return graph;
	}
	
	
	@Test
	public void test57(){
		Graph graph = Graph.create();
		QueryProcess.definePrefix("e", "htp://example.org/");
		QueryProcess exec = QueryProcess.create(graph);
		
		RuleEngine re = RuleEngine.create(graph);
		
		String rule = 
			"construct {[a e:Parent; e:term(?x ?y)]}" +
			"where     {[a e:Father; e:term(?x ?y)]}";
		
		String rule2 = 
			"construct {[a e:Father;   e:term(?x ?y)]}" +
			"where     {[a e:Parent;   e:term(?x ?y)]}";
		
		
		String rule3 = 
			"construct {[a e:Parent]}" +
			"where     {[a e:Father]}";
		
		String rule4 = 
			"construct {[a e:Father]}" +
			"where     {[a e:Parent]}";
		
		
		try {
			re.defRule(rule);
			re.defRule(rule2);
		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String init = "insert data {" +
				"[ a e:Father ; e:term(<John> <Jack>) ]" +
				"[ a e:Father ; e:term(<Jack> <Jim>) ]" +
				"[ a e:Father ; e:term(<John> <Jim>) ]" +
				"}";
		
		String query = "select  * where {" +
				//"?x foaf:knows ?z " +
				"[a e:Parent; e:term(?x ?y)]" +
				"}" ;	
		
		try {
			exec.query(init);
			re.setDebug(true);
			re.process();
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 1, map.size());

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	

}
