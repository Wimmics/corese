package junit;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.IDatatype;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;

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
	public void test7() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		Load load1 = Load.create(g1);
		//load1.load(root + "sdk/sdk.rdf");
		
		String init = 
			"load rdfs:";
		
		//String query = "select * (count(?y) as ?c) where {?x ?p ?y} group by ?x order by desc(?c)";
		//String query = "select *  where {?x a ?y ; ?p ?y} having (exists {?p ?p ?z})";
		//String query = "construct {graph ?g {?x  ?p ?y}}  where {graph ?g {?x  ?p ?y} } ";
		String query = "select * where {" +
				"?p ?p ?r " +
				"service <http://localhost:8080/corese/sparql> {?x ?p ?y} " +
				"?x a ?r} ";
		//String query = "ask {?x <p> ?y filter(isLiteral(?y)) } ";


		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		
		try {
			exec.query(init);
			
			ProviderImpl p = ProviderImpl.create();
			//p.add(g1);
			exec.set(p);
			
			//exec.set(new VisitorImpl());
			
			Mappings map = exec.query(query);
			//System.out.println(map);
			//System.out.println(map);
			System.out.println(map.size());

			
			
			
//			XMLFormat f = XMLFormat.create(map);
//			String str = f.toString();
//			
//			XMLResult p = XMLResult.create(ProducerImpl.create(Graph.create()));
//			Mappings maps = p.parse(new ByteArrayInputStream(str.getBytes()));
//			System.out.println(maps);
//			System.out.println(maps.size());

			
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void test8(){
		Graph g = Graph.create(true);
		QueryProcess exec = QueryProcess.create(g);
		
		String init = "insert data {" +
		"<a> foaf:knows <e> " +
				"<a> foaf:knows <c> " +
				"<a> foaf:knows <b> <b> foaf:knows <c>}";
		
		String query = 
			//"construct {?x ?p ?y}" +
			"select * (min(?l) as ?min)" +				
				"where {" +
				"{select * (pathLength($path) as ?l)  where {" +
				"?a short(foaf:knows+) :: $path ?b } }" +
				"graph $path {?x ?p ?y}" +
				"} " +
				//"having (?l = min(?l))" +
				"bindings ?a ?b {" +
				"(<a> <c>)" +
				"}";
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}
	
	
	
	
	

}
