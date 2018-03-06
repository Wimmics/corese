package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Memory;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.Event;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.RDF;
import fr.inria.corese.kgraph.logic.RDFS;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class TestTest {
	String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

	Graph graph;
	
	public static void main(String[] args) throws EngineException{
		new TestTest().process();
	}
	
	void process() throws EngineException{
		graph = Graph.create(true); 
		Load load=Load.create(graph);
		//load.setDebug(true);
		
//		load.load(Entailment.RDF);
//		load.load(Entailment.RDFS);
//
//		load.load(data + "comma/comma.rdfs");
//		load.load(data + "comma/model.rdf");
//		load.load(data + "comma/data");
//		load.setDebug(false);
//		load.load(data + "comma/data2");

		QueryProcess exec = QueryProcess.create(graph);
		
		
		
		String query = "prefix ext: <function://test.kgraph.TestTest>" +
				"select  (unnest(ext:fun(kg:env())) as (?a, ?q, ?b)) where {}" ;		
						
//		Mappings map = exec.query(query);
//		
//		System.out.println(map);
//		System.out.println(map.size());
		
//		Node root = graph.getNode(Entailment.RDFS + "Resource");
//		graph.prepare();
//		Distance dd =  Distance.create (graph, root);
//		String ns = "http://www.inria.fr/acacia/comma#";
//		Node n1 = graph.getNode(ns + "Person");
//		Node n2 = graph.getNode(ns + "Document");
//		
//		System.out.println(n2);
//		System.out.println(n1);
//		System.out.println(dd.similarity(n1, n2));
//		
		
		query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
				"select debug  * (max(kg:similarity(?c1, ?c2)) as ?sim) where {" +
				//"?x rdf:type c:Person ?y rdf:type c:OrganizationalEntity " +
				"graph ?g {?x rdf:type ?c1 ?y rdf:type ?c2} " +
				"filter(?g ~ 'model')" +
				"filter(?c1 != ?c2) " +
				"" +
				"filter(kg:similarity(?c1, ?c2) > .2)" +
				"}";
		
		
		query = "base <http://www.inria.fr/>" +
				"prefix c: <http://www.inria.fr/acacia/comma#> " +
		"select  debug   more  * " +
		//"((kg:similarity()) as ?sim)  " +
		"where {" +
		" {?x rdf:type c:Person " +
		//"filter(! isBlank(?x))" +
		"?doc c:CreatedBy ?x  " +
		"?doc rdf:type c:TechnicalReport " +
		"} " +
		//"scope {filter(?sim < 1 )}" +
		"" +
		"} " +
		//"order by desc(?x) " +
//		"limit 10" +
		"";
		
query = 
	"construct {?x rdfs:seelso ?l}" +
 "where {?x ?p ?l}" +
 "limit 10";

query = "select distinct * where {" +
		" rdfs:Datatype ?p ?x" +
		"}";

String update = "select * where  {}" +
		"pragma {" +
//		"kg:entailment " +
//			"rdfs:subPropertyOf false ;" +
//			"rdfs:subClassOf false;" +
//			"rdfs:domain false; " +
//			"rdfs:range false;" +
//			"kg:datatype true " +
		"}" +
		"";

//update = "load rdf:";
//
//exec.query(update);

query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
		"select distinct * " +
		//"(kg:similarity() as ?sim) " +
		"where {" +
		"?x rdf:type c:Engineer " +
		//" rdf:type ?class ;" +
		"?x c:hasCreated ?doc " +
//		"?x rdf:type c:Person " +
		"?doc rdf:type c:WebPage33 " +
		//"filter(?z = 3)"+
		"}" +
		"bindings ?z {(3)}" +
		
		//"order by desc(?sim)" +
		"pragma {" +
			"kg:match kg:mode 'subsume'" +
		"}";


query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
		"prefix ext: <function://test.kgraph.TestTest>" +
		"SELECT  * (kg:similarity(c:WebPage, c:Report) as ?sim) " +
		" (kg:depth(c:Document) as ?d) " +
		"(kg:setObject(?x, ?d) as ?tmp) " +
		"(kg:getObject(?x) as ?val)" +
		"WHERE {" +
		"?x rdf:type c:Document"+
 "}";


query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
		"select * (count(?doc) as ?c)" +
		"(kg:setObject(?x, ?c) as ?t)" +
		"where {" +
		"?x c:hasCreated ?doc" +
		"" +
		"}" +
		"group by ?x" ;


//load.load(data + "comma/comma.rdfs");
//load.load(data + "comma/model.rdf");
//load.load(data + "comma/data");



		
//exec.getMatcher().setMode(Matcher.SUBSUME);
		
		Date d1 = new Date();
		Mappings map = exec.query(query);
		
		String query2 = "prefix c: <http://www.inria.fr/acacia/comma#>" +
		"select distinct ?x (kg:getObject(?x) as ?c) " +
		"(kg:load(?x) as ?l) " +
		"where {" +
				"?x c:hasCreated ?doc filter(kg:getObject(?x) >= 2)" +
				"" +
				"}";
		
		
		
		
		
		query = "prefix foaf: <http://xmlns.com/foaf/0.1/>"+
		"SELECT more * (kg:similarity() as ?sim) " +
		"(kg:similarity(foaf:Person, foaf:Person2) as ?d2) " +
		"WHERE {" +
		//"?x rdf:type ?class filter(?class ^ foaf:)" +
		"?x rdf:type foaf:Person " +
		"}" +
		
		"";
		
		
		query = "prefix foaf: <http://xmlns.com/foaf/0.1/>"+
		"select ( kg:similarity(?c1, ?c2) as ?sim ) where{"+
"<http://Luca#me> rdf:type ?c1 "+
"<http://Nico#me> rdf:type ?c2 }";
		
		//load.load(root + "alu/export.rdf");
		
		query = //"base <htp://test.fr/> " +
				//"prefix ex: <htp://test.fr/> " +
				"insert data { " +
				"<Object> rdfs:subClassOf rdfs:Resource " +
				//"<name> owl:inverseOf <hasName> " +
				"<John?a=b&c=d> <name> 'John&toto<tutu' }";

		try {
		map = exec.query(query);
		
		
		}
		catch (EngineException e){
			e.printStackTrace();
		}
		query = "base <http://test.fr/>\n" +
				"select debug * (fun() as ?f)  (max(?a, ?b) as ?c) where {\n" +
				"graph ex:gg {" +
				"{select * where {?x ex:name+ ?n " +
				"filter(kg:foo(?z) in " +
				"('a', 'b'^^xsd:string, 'fr'@en, ?y, 1, ex:name, <ab>)" +
				")}}" +
				"}}";
		
		query = "select  check * where {" +
				"?x rdf:type <Object> " +
				"graph ?g {{select * where{" +
				"?x <name> ?y optional {?y <address> ?z}}}}" +
				"}";
		
		
		
		
		query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
		"select   * (count(?a) as ?count) (pathLength($path) as ?l) where { " +
		"{select * where {c:Person rdfs:subClassOf* :: $path ?x}}  " +
		"graph $path {?a ?p ?b}" +
		"}" +
		"group by $path" ;
		
		query = 
			"select * (pathLength($path) as ?l) where { " +
			"{select $path where { " +
			"?x rdf:rest*/rdf:first :: $path ?y }}" +
			"{select $path1 where { " +
			"?x rdf:rest*/rdf:first :: $path1 ?y }}" +
			"graph $path  {?a ?p ?b}" +
			"graph $path1 {?a ?p ?b}}";
		
		query = 
			"select * (pathLength($path) as ?l) where { " +
			"{select $path where { " +
			"?x rdf:rest*/rdf:first :: $path ?y }}" +
			
			"graph $path  {?a ?p ?b}}";
		
query = "prefix cc: <http://www.inria.fr/acacia/comma#>" +
		"select   * where { " +
		"graph ?g { ?doc cc:CreatedBy ?x ?x cc:FamilyName ?name  " +
		"filter( ?name = 'Corby' )} " +
		"filter(?name = xpath(?g, '/rdf:RDF//*[cc:FamilyName = \"Corby\" ]/*/text()' ))" +
		"{select (xpath(?g, '/rdf:RDF/*/cc:Title/text()' ) as ?title) where {}}" +
		"}";

 query = "prefix ext: <function://test.kgraph.TestTest>" +
"select   " +
"(?x in self(?z) as ?y)" +
"where {?x ?p ?z} limit 1" ;	
 
 
load.load(data + "kgraph/rdf.rdf",  RDF.RDF);
load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);

//load.load(data + "comma/comma.rdfs");
load.load(data + "comma/commatest.rdfs");
//load.load(data + "comma/model.rdf");
//load.load(data + "comma/testrdf.rdf");
//load.load(data + "comma/data");
//load.load(data + "comma/data2");

//01 ?x = <http://www.inria.fr/acacia/comma#Icon>; ?y = <http://www.inria.fr/acacia/comma#Sign>; ?p = <http://www.inria.fr/acacia/comma#superClassOf>; 

 
 query = "select * where {" +
 		"?x ?p ?y optional{?y rdf:type ?class}" +
 " filter (! bound(?class) && ! isLiteral(?y))" +
 "}";

 query = 
	 "prefix c: <http://www.inria.fr/acacia/comma#> " +
 "ask {c:Sign rdf:type ?class}" ;

 query = 
	 "prefix c: <http://www.inria.fr/acacia/comma#> " +
 "describe c:Sign " ;

	
		MyListener el = new MyListener();
		//exec.addEventListener(el);
		map = exec.query(query);
		//System.out.println(exec.getAST(map));

		
		System.out.println(System.getProperty("os.name"));
		
		Query q = map.getQuery();
		Date d2= new Date();
		System.out.println((d2.getTime()- d1.getTime()) /1000.0);
		
//		XMLFormat f = XMLFormat.create(map);
//		System.out.println(f);


		//System.out.println(map);
		//RDFFormat f = RDFFormat.create(exec.getGraph(map));
		System.out.println(map);
		System.out.println(map.size());

	}
	
	
	class MyListener extends EvalListener {
		
		
		public boolean send(Event event){
			
			switch(event.getSort()){
			
			case Event.BEGIN:
				
				// debut de la requete:
				System.out.println(event.getObject());
				break;
				
				
			case Event.RESULT:
				
				// une solution:
				Mapping m = (Mapping) event.getArg(1);
				System.out.println(m);
				break;
			
				
			case Event.END:
				
				// fin de la requête
				// récupérer les résultats: 
				Mappings map = (Mappings) event.getArg(1);
				System.out.println(map.size());
				break;
				
			}
			
			return true;
			
		}
	
		
	}
	
	
	Node getNode(Object e, Object o){
		Graph g = (Graph) e;
		IDatatype dt = (IDatatype) o;
		Node n = g.getNode(dt.getLabel());
		return n;
	}
	
	public IDatatype depth(Object o){
		Node n = (Node) o;
		IDatatype d = DatatypeMap.newInstance((Integer)n.getObject());
		return d;
	}
	

	public Mappings fun(Object obj){ 
		Memory mem = (Memory) obj;
		ProducerImpl p = (ProducerImpl) mem.getEval().getProducer();
		Graph g = p.getGraph();
		QueryProcess exec = QueryProcess.create(g);
		try {
			//exec.query("insert data {<John> <name> 'John'}");
			Mappings map = exec.query("select * where {?x ?p ?y}");
			return map;
		} catch (EngineException e) {
			e.printStackTrace();
		}
		DatatypeMap.newInstance("");
		
		return new Mappings();

	} 

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public IDatatype foo(){
		return DatatypeMap.newInstance(51);
	}
	
	
//	ASTQuery ast = ASTQuery.create();
//	NSManager nsm = ast.getNSM();
//	nsm.definePrefix("ext", "function://test.kgraph.TestTest");
//	
//	Term t = ast.createFunction("function://test.kgraph.TestTest.foo");
//	
//	ast.setSelect(new Variable("?x"), t);
//	
//	ast.setBody(BasicGraphPattern.create());
	
	
}
