package junit;


import org.junit.BeforeClass;
import org.junit.Test;


import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.StatListener;
import fr.inria.edelweiss.kgraph.core.EdgeImpl;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.BuildImpl;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgraph.rdf.*;


import static org.junit.Assert.assertEquals;

/**
 * 
 * 
 */
public class TestQuery {
	
	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	static String test = "/home/corby/workspace/coreseV2/text/";

	static Graph graph;
	
	@BeforeClass
	static public void init(){
		QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		graph = Graph.create(true);
		Load ld = Load.create(graph);
		init(graph, ld);
	}
	
	static  void init(Graph g, Load ld){
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");
	}
	
	
	Graph getGraph(){
		return graph;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void test1(){
		String query = "select check where {" +
				"?x rdf:type c:Person ;" +
				"c:FirstName 'John' ;" +
				"c:name ?n" +
				"}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", true, true);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}

	@Test
	public void test2(){
		String query = "select more * (kg:similarity() as ?sim) where {" +
				"?x rdf:type c:Engineer " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type c:WebPage" +
				"}" +
				"order by desc(?sim)";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");
			double sim = dt.getDoubleValue();
			
			assertEquals("Result", sim, .84, 1e-2);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	@Test
	public void test2b(){
		String query = "select more * (kg:similarity() as ?sim) where {" +
				"?x rdf:type ?c1 filter(kg:similarity(?c1, c:Engineer) > .5) " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type ?c2 filter(kg:similarity(?c2, c:WebPage) > .4)" +
				"}" +
				"order by desc(?sim)";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			
			assertEquals("Result", 9, map.size());
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	@Test
	public void test2c(){
		String query = "select  (kg:similarity(c:Person, c:Document) as ?sim) where {}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");
			double sim = dt.getDoubleValue();
			
			assertEquals("Result", sim, .16, 1e-2);
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test3(){
		String query = "select  * (kg:similarity() as ?sim) where {" +
				"?x rdf:type c:Engineer " +
				"?x c:hasCreated ?doc " +
				"?doc rdf:type c:WebPage" +
				"}" +
				"order by desc(?sim)" +
				"pragma {kg:match kg:mode 'subsume'}";
		QueryProcess exec = QueryProcess.create(graph);
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?x");
			
			assertEquals("Result", dt.getLabel(),  
					"http://www-sop.inria.fr/acacia/personnel/Fabien.Gandon/");
			
			assertEquals("Result", 39, map.size());
	
	
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test4(){
		Load ld = Load.create(Graph.create());
		try {
			ld.loadWE("gogo.rdf");
			assertEquals("Result", false, true);
		} catch (LoadException e) {
			System.out.println(e);
			assertEquals("Result", e, e);
		}
		try {
			ld.loadWE(data + "comma/fail.rdf");
			assertEquals("Result", false, true);
		} catch (LoadException e) {
			System.out.println(e);
			assertEquals("Result", e, e);
		}
	}
	
	
	
	
	@Test
	public void test5(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 1, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test6(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String drop = "drop graph kg:entailment";
		
		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			exec.query(drop);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 0, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test7(){
		Graph graph = Graph.create(true);
		QueryProcess exec = QueryProcess.create(graph);

		String update = "insert data {" +
				"<John> c:name 'John' ; rdf:value (1 2 3)" +
				"c:name rdfs:domain c:Person " +
				"c:Person rdfs:subClassOf c:Human " +
				"}";
		
		String drop = "drop graph kg:entailment";
		String create = "create graph kg:entailment";

		String query = "select  *  where {" +
				"?x rdf:type c:Human ; c:name ?n ;" +
				"rdf:value @(1 2)" +
				"}" ;
				
		try {
			exec.query(update);
			exec.query(drop);
			exec.query(create);
			Mappings map = exec.query(query);
			
			assertEquals("Result", 1, map.size()); 					
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test8(){

		String query = "select  *  where {" +
				"?x c:hasCreated ?doc" +
				"} " +
				"group by any " +
				"order by desc(count(?doc))" +
				"pragma {kg:kgram kg:list true}" ;
				
		try {
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			assertEquals("Result", 3, map.size()); 	
			Mapping m = map.get(0);
			assertEquals("Result", 2, m.getMappings().size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test9(){
		
		Graph g1 = Graph.create(true);
		Graph g2 = Graph.create(true);				

		String query = "select  *  where {" +
				"?x rdf:type ?t; c:name ?n" +
				"} " ;
				
		try {
			QueryProcess e1 = QueryProcess.create(g1);
			QueryProcess e2 = QueryProcess.create(g2);
			QueryProcess exec = QueryProcess.create(g1);
			exec.add(g2);

			e1.query("insert data {<John> rdf:type c:Person}");
			e2.query("insert data {<John> c:name 'John'}");
			
			Mappings map = exec.query(query);
			assertEquals("Result", 1, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test10(){
				
		String query = "select  *  where {" +
				"select (unnest(kg:sparql('select * where {?x rdf:type c:Person; c:hasCreated ?doc}')) as (?x, ?doc)) where {}" +
				"} " ;
				
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			
			Mappings map = exec.query(query);
			assertEquals("Result", 9, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	
	@Test
	public void test11(){
				
		String query = 
		"select * (count(?doc) as ?c)" +
		"(kg:setObject(?x, ?c) as ?t)" +
		"where {" +
		"?x c:hasCreated ?doc" +
		"" +
		"}" +
		"group by ?x" ;
		
		String query2 = 
			"select distinct ?x" +
			"(kg:getObject(?x) as ?v)" +
			"where {" +
			"?x c:hasCreated ?doc filter(kg:getObject(?x) > 0)" +
			"}" +
			"order by desc(kg:getObject(?x))";
			
		
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			
			exec.query(query);
			Mappings map = exec.query(query2);

			assertEquals("Result", 3, map.size()); 	
			
			IDatatype dt = getValue(map, "?v");
			
			assertEquals("Result", 2, dt.getIntegerValue()); 		

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	
	
	
	@Test
	public void test12(){
				
		String query = "select debug *  where {" +
				"?x rdf:type ?class; c:hasCreated ?doc}"  ;
				
		try {
		
			QueryProcess.setSort(true);			
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			QueryProcess.setSort(false);
			
			assertEquals("Result", 22, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test
	public void test13(){
				
		String query = "select  *  where {" +
				"?x rdf:type ?class; c:hasCreated ?doc}"  ;
				
		try {
		
			QueryProcess exec = QueryProcess.create(graph);
			StatListener el = StatListener.create();
			exec.addEventListener(el);
			Mappings map = exec.query(query);
			//System.out.println(el);
			assertEquals("Result", 22, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	class MyBuild extends BuildImpl {
		
		MyBuild(Graph g){
			super(g);
			define(Entailment.RDFSSUBCLASSOF, EdgeSubClass.class);
			define(Entailment.RDFTYPE, EdgeType.class);
			define(Entailment.RDFSLABEL, EdgeLabel.class);
			define(Entailment.RDFSCOMMENT, EdgeComment.class);
		}
		
		public void process(Node gNode, EdgeImpl edge) {
			//System.out.println(edge);
			super.process(gNode, edge);
		}
		
	}
	
	
	@Test
	public void test14(){
				
		String query = "select  *  where {" +
				"?x rdf:type c:Person; c:hasCreated ?doc " +
				"?doc rdf:type/rdfs:subClassOf* c:Document " +
				"c:Document rdfs:label ?l ;" +
				"rdfs:comment ?c" +
				"}"  ;
				
		try {
			
			Graph g = Graph.create(true);
			Load ld = Load.create(g);
			ld.setBuild(new MyBuild(g));
						
			init(g, ld);
			
			QueryProcess exec = QueryProcess.create(g);
			Mappings map = exec.query(query);
			assertEquals("Result", 100, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
		
		
		
		
	}
	
	
	
	@Test
	public void test15(){
				
		String query = "select  (max(kg:depth(?x)) as ?max)  where {" +
				"?x rdfs:subClassOf ?sup" +
				"}"  ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?max");
			assertEquals("Result", 13, dt.getIntegerValue()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	@Test
	public void test16(){
				
		String query = "select  * (kg:number() as ?num)  where {" +
				"?x c:hasCreated ?doc " +
				"}"  ;
				
		try {
									
			QueryProcess exec = QueryProcess.create(graph);
			Mappings map = exec.query(query);
			Mapping m = map.get(map.size()-1);
			IDatatype dt = datatype(m.getNode("?num"));
			System.out.println(map);
			assertEquals("Result", map.size(), dt.getIntegerValue()+1); 	
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
				
	}
	
	
	
	
	@Test
	public void test17(){
		
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		ld.load(data + "comma/comma.rdfs");
		
		QueryProcess exec = QueryProcess.create(g);
		String query = "select (kg:similarity(c:Person, c:Document) as ?sim) {}";
		try {
			Mappings map = exec.query(query);
			IDatatype dt = getValue(map, "?sim");

			assertEquals("Result", true, dt.getDoubleValue()<0.5); 	

			String update = "insert data {c:Human rdfs:subClassOf c:Person}";
			exec.query(update);
			
			assertEquals("Result", null, g.getDistance()); 	

			map = exec.query(query);
			IDatatype sim = getValue(map, "?sim");
			
			assertEquals("Result", dt, sim); 	

			
		} catch (EngineException e) {
			assertEquals("Result", true, e); 	
		}
		
	}
	
	
	@Test
	public void test18(){
		String query = "select * where {" +
				"c:Person rdfs:subClassOf+ :: $path ?c " +
				"graph $path {?a ?p ?b}" +
				"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 155, map.size()); 	
		} catch (EngineException e) {
			assertEquals("Result", 155, e); 	
		}

	}
	
	@Test
	public void test19(){
		String query = "select * " +
				"(pathLength($path) as ?l) (count(?a) as ?c) where {" +
				"?x c:isMemberOf+ :: $path ?org " +
				"graph $path {?a ?p ?b}" +
				"}" +
				"group by $path";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 99, map.size()); 
			
			for (Mapping mm : map){
				IDatatype ldt = getValue(mm, "?l");
				IDatatype lc  = getValue(mm, "?c");

				assertEquals("Result", ldt, lc); 
			}
			
		} catch (EngineException e) {
			assertEquals("Result", 99, e); 	
		}

	}
	
	
	
	@Test
	public void test20(){
		String query = 
			"prefix ext: <function://junit.TestQuery> " +
			"select (ext:fun(?fn, ?ln) as ?res) where {" +
			"?x c:FirstName ?fn ; c:FamilyName ?ln" +
			"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 23, map.size()); 
			
			for (Mapping mm : map){
				IDatatype dt1 = getValue(mm, "?fn");
				IDatatype dt2  = getValue(mm, "?ln");
				IDatatype dt3  = getValue(mm, "?res");

				assertEquals("Result", dt3.getLabel(), concat(dt1, dt2)); 
			}
			
		} catch (EngineException e) {
			assertEquals("Result", 23, e); 	
		}

	}
	
	
	
	@Test
	public void test21(){
		String query = 
			
			"select  * where {" +
			"?x c:FirstName 'Olivier' " +
			"filter(kg:contains('é', 'e')) " +
			"filter(kg:contains('e', 'é')) " +
			"filter(kg:equals('e', 'é')) " +
			"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			assertEquals("Result", 2, map.size()); 
			
		} catch (EngineException e) {
			assertEquals("Result", 2, e); 	
		}

	}
	
	
	
	
	
	public IDatatype fun(Object o1, Object o2){
		IDatatype dt1 = datatype(o1);
		IDatatype dt2 = datatype(o2);
		String str = concat(dt1, dt2);
		return DatatypeMap.createLiteral(str); 
	}
	
	String concat(IDatatype dt1, IDatatype dt2){
		return dt1.getLabel() + "." + dt2.getLabel();
	}
	
	
	IDatatype getValue(Mapping map, String name){
		return datatype(map.getValue(name));
	}
	
	
	IDatatype getValue(Mappings map, String name){
		return datatype(map.getValue(name));
	}
	
	IDatatype datatype(Object n){
		return (IDatatype) n;
	}
	
	IDatatype datatype(Node n){
		return (IDatatype) n.getValue();
	}


}
