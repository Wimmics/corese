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
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgenv.parser.ExpandPath;
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
	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

	
	
	public void test60(){			
		DatatypeMap.setLiteralAsString(false);
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init, query;
		
		init = "" +
		"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
				"insert data {" +
				"<John> foaf:knows <Jack> ; foaf:name 'John' " +
				"<Jack> foaf:knows <Jim> ; foaf:name 'Jack'" +
				"<John> foaf:knows <James>" +
				"<James> foaf:knows <Jim> ; foaf:name 'James' " +
				"<Jim> foaf:knows <Jules>  " +
				"<Jim> foaf:name 'Jim' " +
				"<James> foaf:knows <John>  " +
				"}";
		
		query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"select * where {" +
			"?x foaf:knows+ ?y " +
			"filter(isURI(?x))" +
			"}" +
			"order by ?x ?y";
		
		
//		init = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"insert data {" +
//				"<John> foaf:knows (<Jack> <Jack> <Jim> <Jack>) " +
//				"" +
//				"}";				
//		
//		query = 
//			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
//			"select * where {" +
//			"?x foaf:knows/rdf:rest+/rdf:first ?y " +
//			"filter(isURI(?y))" +
//			"}";
//		
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());

			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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
	
	
	public void test7() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		Load load1 = Load.create(g1);
		load1.load(data + "comma/model.rdf");
		
		String init = 
			"load rdfs:";
		
//		init = "select * where {" +
//				//"service <http://localhost:8080/corese/sparql> " +
//				"{" +
//				"select (kg:sparql('load rdfs:') as ?x) where {}" +
//				"}" +
//				"}";
//		
		
		
		//String query = "select * (count(?y) as ?c) where {?x ?p ?y} group by ?x order by desc(?c)";
		//String query = "select *  where {?x a ?y ; ?p ?y} having (exists {?p ?p ?z})";
		//String query = "construct {graph ?g {?x  ?p ?y}}  where {graph ?g {?x  ?p ?y} } ";

		String query = "" +
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select debug * where {" +
				"service <http://localhost:8080/corese/sparql> {" +
				"?x c:FirstName 'Olivier'} " +
				"} ";
		
		query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>"+
			
			"select debug *  where {" +
		
//				"{select (strlang(?n, 'fr') as ?name) where { " +
//				"?y c:FirstName ?n ; c:FirstName 'Olivier'}" +
//				"}"+
		
		"service <http://fr.dbpedia.org/sparql> {" +
			//"select * where {" +
			"<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y " +
			//"} limit 5"+
		"}" +		"" +
		"}" +
		"pragma {kg:path kg:expand 10}";
		
		
	

		//String query = "ask {?x <p> ?y filter(isLiteral(?y)) } ";
		
		
		//query = "select * where {?x ?p ?y}";


		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		
		try {
			exec.query(init);
			
			ProviderImpl p = ProviderImpl.create();
			//p.add(g1);
			//exec.set(p);
			
			//exec.set(new VisitorImpl());
			
			Mappings map = exec.query(query);
			//System.out.println(map);
			//System.out.println(map);
			System.out.println(map);
			System.out.println(map.size());

			
			
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
	
	
	public void ttest2(){
		Graph g = Graph.create(true);
		QueryProcess exec = QueryProcess.create(g);
		
		String init = "insert data {" +
				"graph <g1> {<John> foaf:knows <Jim>} " +
				"graph <g2> {<John> foaf:knows <James> }" +
				"}";
		
		String del = "delete data {" +
		"<John> foaf:knows <James> " +
		"}";

		
		String query = 
			"select * " +
			"from kg:entailment " +
			"where {" +
			"?x ?p ?y " +
			//"?p rdf:type rdf:Property"+
			"}";
		
		
		query = 
			"select * " +
			"where {" +
			"?x foaf:knows* ?y"+
			"}";
		
		
		try {
			exec.query(init);
			exec.query(del);
			Mappings map = exec.query(query);
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			
//			RDFFormat ff = RDFFormat.create(g);
//			System.out.println(ff);

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Random graph creation
	 */
	@Test
	public void testUpdate(){
		int nbnode = 100000;
		int nbedge = nbnode;

		
		Graph g = Graph.create(true);
		
		Node pred = g.addProperty("foaf:knows");
		
		String init = "" +
				"insert data {" +
				"foaf:knows rdfs:domain foaf:Person " +
				"foaf:knows rdfs:range  foaf:Person" +
				"}";

		Date d1 = new Date();
		
		for (int j=0; j<10; j++){
			System.out.println(j);
			for (int i= 0; i<nbedge; i++){
				long sd = Math.round(Math.random() * nbnode);
				long od = Math.round(Math.random() * nbnode);

				Node sub = g.addResource(Long.toString(sd));
				Node obj = g.addResource(Long.toString(od));

				g.addEdge(sub, pred, obj);
			}
		}
		
		System.out.println("Size: " + g.size());

		Date d2 = new Date();
		System.out.println("Create Time : " + (d2.getTime()-d1.getTime()) / 1000.0);

		g.init();
		
		Date d3 = new Date();

		
		System.out.println("Index Time : " + (d3.getTime()-d2.getTime()) / 1000.0);

		
		String query = "select * where {" +
				"?x foaf:knows ?y " +
				"?z foaf:knows ?x" +
				"?y foaf:knows ?z " +
				"}" +
				"limit 5";
		
		String update = 
		"delete {" +
			"?x foaf:knows ?y " +
			"?y foaf:knows ?z " +
			"?z foaf:knows ?x" +		
		"}" +
		"where {" +
			"{select * where {" +
			"?x foaf:knows ?y " +
			"?z foaf:knows ?x" +
			"?y foaf:knows ?z " +
			"}" +
			"limit 5}" +
		"}";
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			d2 = new Date();
			g.init();
			d3 = new Date();
			System.out.println("Infer Time : " + (d3.getTime()-d2.getTime()) / 1000.0);


			exec.query(query);
			Date d4 = new Date();
			System.out.println("Query Time : " + (d4.getTime()-d3.getTime()) / 1000.0);

			
			Mappings map = exec.query(update);
			Date d5 = new Date();
			System.out.println("Update Time : " + (d5.getTime()-d4.getTime()) / 1000.0);

			g.init();
			
			Date d6 = new Date();
			System.out.println("Infer Time : " + (d6.getTime()-d5.getTime()) / 1000.0);

			System.out.println(g.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Size: 1000000
Create Time : 3.102
Index Time : 4.844
fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372) Undefined prefix: foaf:knows
fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372) Undefined prefix: foaf:Person
Infer Time : 7.345
fr.inria.acacia.corese.triple.parser.ASTQuery.setError(ASTQuery.java:372) Undefined prefix: foaf:knows

Query Time : 1.468
Query Time : 4.767

Infer Time : 5.18
1099947
	 */
	

}
