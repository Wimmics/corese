package junit;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.cg.datatype.CoreseDate;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;

import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Join;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.ParserSparql1;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgenv.eval.Dataset;
import fr.inria.edelweiss.kgenv.eval.ProxyImpl;
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
import fr.inria.edelweiss.kgraph.api.GraphListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.OWLRule;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.Construct;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.ProviderImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.query.QueryGraph;
import fr.inria.edelweiss.kgraph.rule.Rule;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import fr.inria.edelweiss.kgtool.util.GraphListenerImpl;

public class TestUnit {
	
	static String root  = "/home/corby/workspace/kgengine/src/test/resources/data/";
	static String text  = "/home/corby/workspace/kgengine/src/test/resources/text/";
	static String data  = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	static String ndata = "/home/corby/workspace/kgtool/src/test/resources/data/";

	
	
	
	public void test61(){			
		DatatypeMap.setLiteralAsString(false);
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>" + 
			"select  ?y where {" +
				"service <http://fr.dbpedia.org/sparql> {"+
				"<http://fr.dbpedia.org/resource/Auguste> p:succ+ ?y .}" +
			"}" +
			"pragma {kg:path kg:expand 12}";
		
		
		try {
			Mappings map = exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 12, map.size());
			
			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	
	
	
	
	@Test
	public void testExpand(){
		
		String query = 
				"prefix ex: <http://example.org/>" +
				"select  ?x ?c where {" +
						"?x rdf:type/rdfs:subClassOf* ?c" +
						//"?x ex:p0 / (!(ex:p1 | ex:p2))*  ?y" +
						//"ex:list rdf:value/rdf:rest*/rdf:first ?y" +
				"}" ;
				//"pragma {kg:path kg:expand 3}";
		
		String init = 
				"prefix ex: <http://example.org/> " +
				"insert data {" +
				"ex:a ex:p0 ex:b " +
				"ex:b ex:p0 ex:c " +
				"ex:c ex:p1 ex:d " +
				"" +
				"ex:list rdf:value (1 2 3) " +
				"" +
				"ex:Human rdfs:subClassOf ex:Animal " +
				"ex:Animal rdfs:subClassOf ex:Living " +
				"ex:John a ex:Human" +
				"}" 
				;
		
		
		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		exec.addVisitor(ExpandPath.create(2));

		try {
			exec.query(init);
			Mappings map = exec.query(query);
			exec.getGraph(map);
			
//			ASTQuery ast = exec.getAST(map);			
//			ExpandPath rew = ExpandPath.create(3);
//			System.out.println(rew.rewrite(ast.getBody()));
			
			System.out.println(exec.getAST(map));
			System.out.println(map.getQuery());
			System.out.println(map);
			System.out.println(ResultFormat.create(map));
			System.out.println(map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	public void testSyntax(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		String init  = "insert data {<a> a rdfs:Resource}";
		
		String cons = "construct {?x rdf:value ?y} where {?x a rdfs:Resource} values ?y {10}";
		
		String query = "select * where {?x ?p ?y}";
		
		Load ld = Load.create(g);
		
		ld.load(root + "test/deco.rl");
		
		try {
			exec.query(init);
			
			RuleEngine re = ld.getRuleEngine();
			re.process();
			
			Mappings map = exec.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Total: 0.6
	 * Without XML parse: 0.4
	 * 
	 * 0.553
	 */
	public void testService(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		String q = QueryLoad.create().read(root + "alu/nico.rq");
		
		try {
			int size = 1;
			Date d1 = new Date();
			for (int i = 0; i<size; i++){
				//exec.setDebug(true);
				Mappings map = exec.query(q);
				if ( i == 0) System.out.println(map);
			}
			Date d2 = new Date();
			System.out.println(((d2.getTime() - d1.getTime()) / 1000.0) / size);
			//System.out.println(map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	

	public void testSAP(){
		Graph g = Graph.create(true);
		QueryProcess exec = QueryProcess.create(g);
		
		Load ld = Load.create(g);
		ld.load(root + "sap/q1-light.ttl");
		ld.load(root + "sap/q2-light.ttl");
		ld.load(root + "sap/sqlOnto.ttl");

		String q = QueryLoad.create().read(root + "sap/q1.rq");

		try {
			Date d1 = new Date();
			Mappings map = exec.query(q);
			Date d2 = new Date();
			//System.out.println(map);
			System.out.println(map.size());
			System.out.println((d2.getTime() - d1.getTime()) / 1000.0);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	public void testCountPath(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		String init ="" +
				"prefix ex: <http://www.inria.fr/acacia/comma#>" +
				"insert data {" +
				"ex:a ex:p ex:b ex:b ex:p ex:c " +
				"ex:a ex:p ex:d ex:d ex:p ex:c " +
				"" +
				"} " ;
		
		String query ="" +
				"prefix ex: <http://www.inria.fr/acacia/comma#> " +
				"select * where {" +
					"ex:a ex:p+ ?x" +
				"}" +
				"pragma {kg:path kg:count false}" ;
		
		try {
			//exec.setCountPath(true);
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());
			
			

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void testBUG(){			
		
		Graph g = Graph.create();	
		QueryProcess exec = QueryProcess.create(g);	
		
		Load ld = Load.create(g);
		ld.load(root + "test/q1.ttl");
		
		QueryLoad ql = QueryLoad.create();
		String q = ql.read(root + "test/q1.rq");
		System.out.println(q);
		
		try {
			Mappings map = exec.query(q);
			System.out.println(exec.getGraph().display());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void testDelete(){			
			
			Graph graph = Graph.create(true);	
			QueryProcess exec = QueryProcess.create(graph, true);		

			String init = 
				"prefix ex: <http://example.org/> "+
				"" +
				"insert data {" +
										
					"ex:John a ex:Man " +
					"ex:Jill a ex:Woman " +
					"ex:John a ex:Human " +
				"} ";
			
			
			String update = 
					"prefix ex: <http://example.org/> " +
					"delete where {" +
					"ex:John ?p ?y" +
					"}" +
					"" +
					"";
			
			String query = 
					"prefix ex: <http://example.org/> " +
					"select *  where  {" +
					"{?x rdf:type* ?c}" +
					"}";
			
			
			try {
				
				exec.query(init);
				exec.query(update);
				Mappings map = exec.query(query);
							
				System.out.println(map);
				System.out.println(map.size());
				System.out.println(graph);
				System.out.println(graph.getResource("http://example.org/John"));

				//assertEquals("Results", 9, map.size());
				
				

			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
			
	
	
	
	
	
	
	
	
	
	
	
	
	
public void testType(){			
		
		Graph graph = Graph.create(true);	
		QueryProcess exec = QueryProcess.create(graph, true);		

		String init = 
			"prefix ex: <http://example.org/> "+
			"" +
			"insert data {" +
				"ex:Human rdfs:subClassOf ex:Animal " +
				"ex:Man   rdfs:subClassOf ex:Human " +
				"ex:Woman rdfs:subClassOf ex:Human " +
				
				"graph ex:g1 { " +
					"ex:John a ex:Man " +
					"ex:Jill a ex:Woman " +
				"}" +
				"ex:John a ex:Human " +
				"ex:John a ex:Man " +
			"} ";
		
		String query = 
				"prefix ex: <http://example.org/> " +
				"select *  where  {" +
				//"graph ?g " +
				"{?x a ex:Human}" +
				"}";
		
		
		try {
			
			exec.query(init);
			Mappings map = exec.query(query);
						
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Results", 9, map.size());
			
			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void testOption(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"" +
			"insert data {" +
			":a :p :b, :c ." +
			
			":b :p :d, :a " +
			":c :p :d " +
			"" +
			":e :p :b, :c ." +
			""+
			"} ";
		
		String query = 
				"prefix : <http://example.org/> " +
				"select *  where  {" +
				"?x ((:p/:p) ?)  :d " +
				"}";
		
		
		try {
			
			exec.query(init);
			Mappings map = exec.query(query);
						
			System.out.println(map);
			System.out.println(map.size());
			System.out.println(map.getQuery().getAST());

			//assertEquals("Results", 9, map.size());
			
			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	

	
	
	/**
	 * Create a Query graph from an RDF Graph
	 * Execute the query
	 * Use case: find similar Graphs (cf Corentin)
	 */
	public void test52(){			
		
		Graph graph = Graph.create(true);	
		QueryProcess exec = QueryProcess.create(graph);		

		String init = 
			"prefix : <http://example.org/> "+
			"insert data {" +
			
				"graph <q1> {" +
					"[a :Query ; :query <q1> ; " +
					":where [:subject [] ; :property [] ; :object 10 ]]" +
				"}" +
					
				"graph <q2> {" +
					"[a :Query ; :query <q2> ; " +
					":where [:subject [] ; :property [] ; :object 20 ]]" +
				"}" +
			
			"" +
			"} ";
		
		String onto = 
				"prefix : <http://example.org/> "+
				"insert data {" +
					":Query rdfs:subClassOf :Action" +
				"}";
		
		// extract a subgraph 
		// replace literal with bnode
		String cons = 
				"prefix : <http://example.org/> "+
				"construct { graph ?g { ?x ?p ?o }}" +
				"where {" +
					"select * " +
						"(if (?p = :query || isLiteral(?y), bnode(), ?y) as ?o) " +
					"where {" +
						"graph ?g { [:object ?v] . filter(?v >= 10) ?x ?p ?y }" +
					"}" +
				"}"  ;					
		
		// rewrite subClass as superClass
		String rew = 
				"prefix : <http://example.org/> "+
				"delete {graph ?g {?x a ?c}}" +
				"insert {graph ?g {?x a ?c2}}" +
				"where  {" +
					"graph ?g {?x a ?c} " +
					"?c rdfs:subClassOf ?c2" +
				"}";
		
		Graph go = Graph.create();
		
		
		try {
			// Load ontology
			QueryProcess.create(go).query(onto);
			
			// create a graph
			exec.query(init);
			exec.query(onto);
			
			// create a copy where triple objects (values) are Blank Nodes (aka Variables)
			Mappings map = exec.query(cons);
//			System.out.println(map);
//			System.out.println(map.size());
						
			Graph g2 = exec.getGraph(map);
			//System.out.println(TripleFormat.create(g2, true));
			
			List<Graph> list = g2.split();
			
			for (Graph g : list){
				System.out.println(TripleFormat.create(g, true));
				
				QueryProcess rewrite = QueryProcess.create(g);
				rewrite.add(go);
				rewrite.query(rew);
				
				
				System.out.println(TripleFormat.create(g, true));

				map = exec.query(g);									
				System.out.println(map.toString(true));
				System.out.println(map.size());
			}
			
//			QueryProcess rewrite = QueryProcess.create(g2);
//			rewrite.query(rew);
//			System.out.println(TripleFormat.create(g2));


//			QueryGraph qg = QueryGraph.create(g2);
//			QGVisitor vis = new QGVisitor();
			//qg.setVisitor(vis);
			//qg.setConstruct(true);
			//map = exec.query(g2);									
			
			//Graph res = exec.getGraph(map);
			//assertEquals("Results", 2, res.size());
			
			//System.out.println(TripleFormat.create(res));
//			
//			System.out.println(map.toString(true));
//			System.out.println(map.size());


		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	public void testBNode(){
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init = "" +
				"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
				"insert data {" +
				"<John> foaf:knows <Jim>, <James>; foaf:prout <jjj>  " +
				"<Jack> foaf:knows <Jim> ; foaf:prout <hhh>" +
				"}";
		
		String query =
				"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
				"select ?x " +
				"(count(?y) as ?c1) (count(distinct ?z) as ?c2)" +
				"(bnode(?c1) as ?b1) (bnode(?c2) as ?b2)" +
				"where {" +
				"?x foaf:knows ?y ; foaf:prout ?z " +
				"} group by ?x" ;

		try {
			exec.query(init);
			
			Mappings map = exec.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	public void test60(){			
		DatatypeMap.setLiteralAsString(false);
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String init, query;
		
		init = "" +
		"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
				"insert data {" +
				"<John> foaf:knows <Jack> ; foaf:knows 'John' " +
				"<Jack> foaf:knows <Jim> ; foaf:knows 'Jack'" +
				"<John> foaf:knows <James>" +
				"<James> foaf:knows <Jim> ; foaf:knows 'James' " +
				"<Jim> foaf:knows <Jules>  " +
				"<Jim> foaf:knows 'Jim' " +
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
	 * select where { ?x foaf:FamilyName 'Corby'  ?x foaf:isMemberOf ?org  
	 * ?x foaf:FirstName ?name  filter  (?name = 'toto' || ?org ~ 'inria' )} 
	 */

	Graph init(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create(true);
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
		load.load(data + "comma/data2");
		
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
//		RuleEngine re = load.getRuleEngine();
//		//int nb = re.process();
//		 t2 = new Date().getTime();
//		System.out.println("** Time: " + (t2-t1) / 1000.0 + "s");
//		System.out.println("** Size: " +graph.size());
		//System.out.println("** Rule Entailment: " + nb);
//		
		return graph;
	}
	
	
	
	
	
	
	//
	public void test00(){
		Graph g = init();
		QueryProcess exec = QueryProcess.create(g);
		
		String query="" +
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select * where {" +
 		"?x c:FirstName '\"?Olivier\"' } " ;
		
		try {
			//exec.setCountPath(true);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());
			
			

			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
				"prefix foaf: <http://www.inria.fr/acacia/comma#>" +
				"select debug * where {" +
				"service <http://localhost:8080/corese/sparql> {" +
				"?x foaf:FirstName 'Olivier'} " +
				"} ";
		
		query = 
			"prefix foaf: <http://www.inria.fr/acacia/comma#>" +
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>"+
			
			"select debug *  where {" +
		
//				"{select (strlang(?n, 'fr') as ?name) where { " +
//				"?y foaf:FirstName ?n ; foaf:FirstName 'Olivier'}" +
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
				"values ?a {<a>}" +
				"graph $path {?x ?p ?y}" +
				"} " +
				//"having (?l = min(?l))" +
				"bindings (?a ?b) {" +
				"(<a> <c>)" +
				"}";
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			ASTQuery ast = exec.getAST(map);
			System.out.println(ast);

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
	
	
	
	
	
	
	

	public void test451(){			
			
			Graph graph = Graph.create();			
			QueryProcess exec = QueryProcess.create(graph);
			
			 String init = 
					"prefix foaf: <http://test/> " +
					"insert data {" +
						"tuple(foaf:knows <John>  <James> 2)" +
						"tuple(foaf:knows <John>  <James> 1)" +
						"tuple(foaf:knows <John>  <James> 2 3)" +
						"tuple(foaf:knows <John>  <Jim>   1)" +
						"tuple(foaf:knows <John>  <James> )" +
						"tuple(foaf:knows <Jack>  <James> )" +
						"tuple(foaf:knows <Jim>  <James> )" +
					"} ;" ;
			 
			 String query = 
				"prefix foaf: <http://test/> " +
				"prefix ext: <function://junit.TestUnit>" +
				 "select * where {" +
					//"graph ?g " +
					"{ " +
					"tuple(foaf:knows ?x ?n ?v) " +
					  "?x foaf:knows::?p ?y  " +
//					  "filter(?x != ?y)" +
					"}" +
				 "}";
			 
			

			 try {
					exec.query(init);
					graph.init();
					System.out.println("** Size: " + graph.size());

					ASTQuery ast = ASTQuery.create(query);
					ParserSparql1.create(ast).parse();
					
					System.out.println(ast);

					Mappings map = exec.query(query);
					
					System.out.println(map);
					
					assertEquals("Result", 2, map.size());

					
				} catch (EngineException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 
	}
	
	public static String dateToGMTString(Date dateToBeFormatted) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
		return dateFormat.format(dateToBeFormatted);
	}

	public static Date GMTStringToDate(String gmtDateString) {
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.FRANCE);
			return dateFormat.parse(gmtDateString);
		} catch (ParseException e) {
			// manage exception
			e.printStackTrace();
		}
		return null;
	}

	public void test50(){			
		
		Graph graph = Graph.create();			
		QueryProcess exec = QueryProcess.create(graph);
		String d1 = dateToGMTString(new Date());
		for (int i=0; i<10000000; i++){}
		String d2 = dateToGMTString(new Date());

		System.out.println(d1);
		System.out.println(d2);
		
		try {
			IDatatype dt1 = new CoreseDate(d1);
			IDatatype dt2 = new CoreseDate(d2);
		} catch (CoreseDatatypeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		 String init = 
				"prefix foaf: <http://test/> " +
				"insert data {" +
					"<John> foaf:date '" + d1 + "'^^xsd:dateTime "+
					"<Jim> foaf:date  '" + d2 + "'^^xsd:dateTime "+
				"} ;" ;
		 
		 String query = 
			"prefix foaf: <http://test/> " +
			 "select * where {" +
				"?x foaf:date ?d"+
			 "}" +
			 "order by desc(?d)";
		 

		 try {
				exec.query(init);
				graph.init();
				System.out.println("** Size: " + graph.size());

			

				Mappings map = exec.query(query);
				
				System.out.println(map);
				
				assertEquals("Result", 2, map.size());

				
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
}


	
	public Object fun (Object obj){
		return DatatypeMap.TRUE;
	}
	
	
	
	
	
	public void testDate(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		ld.load(root + "isicil/date.rdf");
		
		QueryLoad ql = QueryLoad.create();						
		String query = ql.read(root + "isicil/date.rq");
		
		IEngine engine = new EngineFactory().newInstance();
		try {
			engine.load(root + "isicil/date.rdf");
		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		try {
			IResults res = engine.query(query);
			System.out.println(res);

		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		
		String query2 = "prefix dc: <http://purl.org/dc/elements/1.1/>" +
				"select * where {	" +
				"?msg dc:created ?date " +
				"}" +
				"order by (?date)";
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(query);
			
			ResultFormat f = ResultFormat.create(map);
			
			//
			//System.out.println(f);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
	public void test65(){
		Graph g = Graph.create();
		
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(root + "test/iso.ttl");
			ld.loadWE(root + "test/iso.rdf");
			
			ld.loadWE(root + "test/utf.ttl");
			ld.loadWE(root + "test/utf.rdf");
			
			ld.loadWE(root + "test/iso.rul");

		} catch (LoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String query = "select * where {" +
				"?x ?p ?y . ?z ?q ?y filter(?x != ?z)" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);
			
			RDFFormat ff = RDFFormat.create(g);
			System.out.println(ff);
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
		RuleEngine re = ld.getRuleEngine();
		Rule r = re.getRules().get(0);
		System.out.println(r.getQuery().getAST());
		
		System.out.println(System.getProperty("file.encoding"));
	
	}
	
	
	public void test70(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		try {
			System.out.println("** Load: alu/dbpedia_3.7.rdfs"  );			
			ld.loadWE(root + "alu/dbpedia_3.7.rdfs");

		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		String query = "select ?sim (kg:similarity(owl:Thing,owl:Thing) as ?sim) where {}";



		String query2 = "select ?depth (kg:depth(<http://schema.org/CreativeWork>) as ?depth) where {}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			System.out.println("** Query 1"  );			
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println("** Query 2"  );			
			 map = exec.query(query2);
			
			System.out.println(map);
			System.out.println(map.size());
			assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
	
	}
	
	
	
	
	
	
	
	public void testJoin(){
		Graph g = init(); //Graph.create();
		Load ld = Load.create(g);
		
		String init = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data   {" +
			"graph <g1> {" +
		"<John> foaf:name 'John' " +
		"<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>" +
		"<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <Jack>" +
		"<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>" +
		"}" +

		"graph <g1> {" +		
		"<Jim> foaf:knows <James>" +
		"<Jim> foaf:name 'Jim' " +
		"}" +
		"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select debug * where {" +
			
				"service <http://fr.dbpedia.org/sparql> {{" +
					"select * where {" +
					"<http://fr.dbpedia.org/resource/Auguste> <http://www.w3.org/2000/01/rdf-schema#label> ?n" +
					"?x rdfs:label ?n " +
					"} limit 20" +
				"}}" +
				

				"service <http://fr.dbpedia.org/sparql> {{" +
					"select * where {" +
					"?x rdfs:label ?n " +
					"}" +
				"}}" +				
			"}" +
			"";
		
		
		query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select debug *  where {" +
			
				"?x ?p ?y " +
				

				"service <http://fr.dbpedia.org/sparql> {{" +
					"select * where {" +
					"?x ?p ?y " +
					"}" +
				"}}" +				
			"}" +
			"";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.setOptimize(true);
			exec.query(init);
		
			Mappings map = exec.query(query);
			System.out.println(map);
			
			assertEquals("Result", 2, map.size());
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
	
	}
	
	public void testRelax(){
		Graph g = init();
					
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"insert data {" +
				"<John> foaf:type c:Researcher " +
				"<John> foaf:knows <Jack> " +
				"<Jack> foaf:type c:Engineer " +
				
				"<John> foaf:knows <Jim> " +
				"<Jim> foaf:type c:Fireman " +
				
				"<e> foaf:type c:Event " +
				"}" ;
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select   more * (kg:similarity() as ?s) " +
			"(uuid() as ?u)" +
			"(struuid() as ?su)" +
			"where {" +
				"?x foaf:type c:Engineer " +
				"?x foaf:knows ?y " +
				"?y foaf:type c:Engineer" +
			"}" +
			"order by desc(?s) " +
			"pragma {kg:kgram kg:relax  rdf:type, foaf:type}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 2, map.size());
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
		

	}
	
	
	public void testValues(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<a> foaf:knows <b> " +
				"<c> foaf:knows <d> " +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				//"?x foaf:knows ?y " +
				"values ?x {<b> <c>}" +
				"}" +
				"values ?y {<b> <e>}" +
				"";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.getQuery());
			System.out.println(exec.getAST(map));

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	
	
	

	public void testNode(){
		Graph g = Graph.create();
		
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<?a> foaf:knows <?b> " +
				"<?b> foaf:knows <?c> " +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				"<?a> foaf:knows ?a ?a foaf:knows ?b " +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	
	public void testBlank(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"graph <g1> {_:b foaf:knows <b>} " +
				"graph <g2> {_:b foaf:knows <b>} " +
				"graph <g2> {_:b1 foaf:knows <b>} " +
				"}";
		
		String query0 = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert {graph <g2> {?x foaf:knows ?y} }" +
			" where {" +
				"graph <g1> {?x foaf:knows ?y} " +
				//"values ?x {<a>}" +
				"}";
		
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				"graph ?g {?x foaf:knows ?y} " +
				//"values ?x {<a>}" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings m = exec.query(query0);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	public void testTTL(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(data + "commattl/comma.ttl");
			ld.loadWE(data + "commattl/model.ttl");

		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		System.out.println(g);
		
		EngineFactory fac = new EngineFactory();
		IEngine engine = fac.newInstance();
		GraphEngine eng = (GraphEngine) engine;
		Graph gg = eng.getGraph();
		gg.setEntailment(false);
		
		
	}
	
	
	
	public void testAlban(){
		Graph g = Graph.create();
		QueryLoad ld = QueryLoad.create();
		
		String q = ld.read(ndata + "alban/query/q1.rq");
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<a> foaf:knows <b> " +
				"<c> foaf:knows <d> " +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				"?x foaf:knows ?y " +
				//"values ?x {<a>}" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			System.out.println(q);
			Mappings map = exec.query(q);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	public void testBind(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		String init = 	" insert data {" +
				"<John> foaf:age 2" +
				"<John> foaf:age 3" +
				"<John> foaf:age 4" +
				"}";			

		
		String query2 = 				
			"select more * where {" +
			//"filter(?x = 2)" +
			"bind(3 as ?x)" +
			"" +
			"}" +
			"values ?x {1 2 3}" ;
		
		String query = 				
			"select * where {" +
			"select more  *  (5 as ?x) (3 as ?x)  where {" +
			"?a foaf:age ?x" +
			//"{select (3 as ?x) {}}" +
			"" +
			"}" +
			"}"  ;
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			
			Mappings map = exec.query(query);

			System.out.println(map);
			System.out.println(map.size());
			//System.out.println(map.get(0).getQueryNodes().length);
			System.out.println(exec.getAST(map).getSelectAllVar());
			System.out.println(map.getQuery().getSelectFun());
			System.out.println(exec.getAST(map));

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	

	
	
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
			
			System.out.println(ast);
			System.out.println(map);
			System.out.println(f);
			assertEquals("Result", map.size(), 2);
			
	
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			assertEquals("Result", true, e);
		}
	}

	public void testAgg(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		String qq = ql.read(ndata + "test/agg.rq");
		

		
		String init = 				
			"PREFIX ex: <http://example.org/meals#>  " +
			"insert data {" +
				"[ ex:mealPrice 1 ; " +
				 " ex:mealTip 2 ;" +
				
				"ex:mealPrice 3 ; " +
				"ex:mealTip 4 ;" +
				" ] " +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				"?x foaf:knows ?y " +
				//"values ?x {<a>}" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(qq);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	public void testJulien(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		
		String qq = ql.read(ndata + "test/dbpedia.rq");
		
		try {
			ld.loadWE(root + "test/iso.ttl");
			//ld.loadWE(data + "commattl/comma.ttl");

		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<a> foaf:knows <b> " +
				"<c> foaf:knows <d> " +
				"}";
		
		//http://fr.dbpedia.org/property/texte	
		
		String query = 
			   "prefix dbpedia-owl: <http://dbpedia.org/ontology/> " +
               "prefix dbfr: <http://fr.dbpedia.org/> " +
               "prefix dbpedia-prop: <http://dbpedia.org/property/> " +
               "prefix dbp: <http://dbpedia.org/property/> " +
               "select debug  distinct  ?p1 " +
               "where { " +
               
               "service <http://dbpedia.org/sparql>    {<http://dbpedia.org/resource/Paris> ?r2 ?p1 " +
               "filter(?r2 != dbp:texte)" +
               "filter(isLiteral(?p1))" +
                             
              // "filter(?p1 = 20)" +
               "}  . " +
               "filter( isNumeric(?p1))" +
//               "filter( lang(?p1) = 'fr')" +
//               
               " service <http://fr.dbpedia.org/sparql> {<http://fr.dbpedia.org/resource/Paris> ?r1 ?p1 . }  " +

              
               "}" +
               "order by ?p1 " +
               "pragma {" +
               		//"kg:service kg:slice 50 " +
               "}";;
		
		
		QueryProcess exec = QueryProcess.create(g);
		//exec.setSlice(30);
		
		try {
			Mappings map = exec.query(query);
			System.out.println("** Result: "  + map.size());
			System.out.println(map.size());
			System.out.println(map);

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	public void test1(){
		
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(data + "comma/comma.rdfs");
			ld.loadWE(data + "comma/data");
			ld.loadWE(data + "comma/data2");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TripleFormat t = TripleFormat.create(g, true);
		try {
			t.write(data + "commattl/global.ttl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void test2(){
		
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(data + "commattl/global.ttl");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String query = "select * where {?x <p> 'ab\\ncd'}";
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(exec.getAST(map));

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("** Size: " + g.size());
	}
	
	

	public void test3(){
		
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(data + "comma/data2/f125.rdf");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String query = 
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select * where {?x c:Title ?t}";
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void testNicolas(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		
		
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<a> foaf:knows <b> " +
				"<c> foaf:knows <d> " +
				"}";
		
		//  9.095

		// 11.81


		String prop = "prefix db: <http://fr.dbpedia.org/property/>" +
				"select  ?p where {" +
		"service <http://fr.dbpedia.org/sparql> {" +
			"select distinct ?p where {" + 
			"?p rdf:type rdf:Property " +
			"filter(?p != db:isbn)" +
			"filter (! regex(str(?p), 'owl'))" +
			"filter (! regex(str(?p), 'wiki'))" +
			"}  limit 100" +
		"}" +
		"}  order by ?p ";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			//"select * " +
			"insert {?x <%s> ?p1 }" +
			"where {" +
            "service <http://fr.dbpedia.org/sparql> {" +
            "select * where {?x <%s> ?p1 } limit 10" +
            "}}" ;
		
		/**
219247 22.83
29 17.271

353422
49 45.816
49 25.154

49 37.099


498615
99 60.255
99 44.21


		 */
				

		QueryProcess exec = QueryProcess.create(g);
		
		try {
			//exec.query(init);
			Date d1 = new Date();
			
			int slice = 1;
			
			Mappings res = exec.query(prop);
			System.out.println(res);
			
			
			for (int i = 0; i<res.size(); i++){
				Node np = res.get(i).getNode("?p");
				String name = np.getLabel();
				System.out.println(name);
				Formatter f = new Formatter();
				String qq = f.format(query, name, name).toString();
				//String qq = f.format(query, name).toString();
				//System.out.println(qq);
				Mappings map = exec.query(qq);
				//System.out.println(map);
				System.out.println(g.size());
				Date d2 = new Date();
				System.out.println(name);
				System.out.println(i + " " + (d2.getTime() - d1.getTime()) /1000.0);
			}
			
			//System.out.println(res);

						
	/**
	 * 	9 12.824
		9 12.073
		9 12.288

	 */
// 28.5 pour 200 000 (vs 15.7 select *)

// 		300 000 insert : 51.007    select * : 23
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	
	
	public void testIGN(){
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		
		try {
			ld.loadWE(root + "ign/ontology/ign.owl");
			//ld.loadWE(ndata + "test/onto.ttl");


		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		// query generate a construct where rule for property Chain
		String init = 
			"select debug  " +
			"(concat('construct {?x ',  kg:qname(?q), ' ?y} where {?x ', ?r , ' ?y}') as ?req)" +
			"(group_concat(kg:qname(?p) ; separator='/') as ?r)" +
			"where {" +
				"?q owl:propertyChainAxiom/rdf:rest*/rdf:first ?p " +
			"}" +
			"group by ?q";
		
		
		String query = 
				"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
				"prefix ign: <http://www.semanticweb.org/ontologies/2012/5/Ontology1339508605479.owl#>" +
				"select * where {" +
				"?x ign:aLaTeinteDe ?t " +
				"}" ;
		
		String query2 = 
				"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
				"prefix t: <http://ns.inria.fr/test/>" +
				"select * where {" +
				"graph ?g {?x a t:Male ;  a ?t } " +
				"}" ;
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {

			
//			OWLRule owl = OWLRule.create(g);
//			owl.process();
			
			Mappings map = exec.query(init);
			System.out.println(map);
			System.out.println(map.getQuery());
			System.out.println(map.size());
			System.out.println(exec.getAST(map));
	
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		} 
//		catch (LoadException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	// http://www.subshell.com/en/subshell/blog/article-Changing-from-m2eclipse-to-m2e-Eclipse-Indigo100.html
	// org.eclipse.m2e.launchconfig.classpathProvider"/>
	// org.maven.ide.eclipse.launchconfig.classpathProvider
	
	
	
	
	
	public void dbpedia(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		
		String query = ql.read(root + "test/dbpedia.rq");

		System.out.println(query);

		
		
		QueryProcess exec = QueryProcess.create(g);
		//exec.setDebug(true);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void start(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		
		try {
			ld.loadWE(root + "test/iso.ttl");

		} catch (LoadException e1) {
			e1.printStackTrace();
		}
		
		String init = 				
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data {" +
				"<a> foaf:knows <b> " +
				"<c> foaf:knows <d> " +
				"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select * where {" +
				"?x foaf:knows ?y " +
				//"values ?x {<a>}" +
				"}";
		
		
		QueryProcess exec = QueryProcess.create(g);
		
		try {
			exec.query(init);
			Mappings map = exec.query(query);
			System.out.println(map);
			System.out.println(map.size());

			//assertEquals("Result", 4, map.size());
			
			ResultFormat f = ResultFormat.create(map);
			System.out.println(f);			
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	

}
