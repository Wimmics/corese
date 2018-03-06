package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class Distance {
	
	
	public static void main(String[] args) throws EngineException{
		new Distance().test3();
	}
	
	

static  void init(Graph g, Load ld){
	
}	

public void test3(){
	QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
	QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

	Graph graph = Graph.create(true);
	Load ld = Load.create(graph);
	String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	ld.load(data + "comma/comma.rdfs");
	ld.load(data + "comma/model.rdf");
	ld.load(data + "comma/data");	

	String query = "select debug  * (kg:similarity() as ?sim) where {" +
	"?x rdf:type c:Engineer " +
	"?x c:hasCreated ?doc " +
	"?doc rdf:type c:WebPage" +
	"}" +
	"order by desc(?sim)" +
	"pragma {kg:match kg:mode 'general'}";
	QueryProcess exec = QueryProcess.create(graph);
	try {
		Mappings map = exec.query(query);
System.out.println(map.size());


	} catch (EngineException e) {
		e.printStackTrace();		}
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	void process() {
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		load.load(data + "comma/comma.rdfs");
		load.load(data + "comma/data");
		load.load(data + "comma/model.rdf");

		QueryProcess exec = QueryProcess.create(graph);
		
		
		String init2 = 				
			"prefix c: <http://www.inria.fr/acacia/comma#> " +
			"insert data {" +
				"c:Designation a rdf:Property ; rdfs:subPropertyOf owl:topDataProperty " +
				"c:FirstName a rdf:Property ;   rdfs:subPropertyOf c:Designation " +
				"c:FamilyName a rdf:Property ;  rdfs:subPropertyOf c:Designation " +
				"c:Address a rdf:Property ;     rdfs:subPropertyOf owl:topDataProperty " +
				
				
				"c:relatedTo a rdf:Property ;   rdfs:subPropertyOf owl:topObjectProperty " +
				"c:friendOf a rdf:Property ;    rdfs:subPropertyOf c:relatedTo " +
				"c:colleagueOf a rdf:Property ; rdfs:subPropertyOf c:relatedTo " +

				"c:relation a rdf:Property ;    rdfs:subPropertyOf owl:topObjectProperty " +

				"}";
		
		
		String query2 = 
			"prefix c: <http://www.inria.fr/acacia/comma#> " +
			"select * (kg:pSimilarity(?p1, ?p2) as ?sim) where {" +
			"?p1 rdf:type rdf:Property " +
			"?p2 rdf:type rdf:Property " +
			"}" +
			"order by desc(?sim) " +
			"limit 30 " +
			"bindings ?p1  {" +
			"(c:FirstName )" +
			"}" ;
		
		String init = 
			"prefix c: <http://www.inria.fr/acacia/comma#> " +
			"insert data {" +
			"rdfs:subClassOf rdfs:domain rdfs:Class; rdfs:range rdfs:Class " +
			"c:Event rdfs:subClassOf c:Object " +
			"c:Living rdfs:subClassOf c:Object " +
			"c:Human  rdfs:subClassOf c:Living " +
			"c:Person rdfs:subClassOf c:Living " +
			
//			"c:Human  rdfs:subClassOf c:Person " +
//			"c:Person rdfs:subClassOf c:Human " +
			
			"c:Human  owl:equivalentClass c:Person " +
			"c:Person owl:equivalentClass c:Human " +

			"c:Man rdfs:subClassOf c:Person " +
			"c:John a c:Man " +
			"}" ;
						
		
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#> " +
			"select * " +
			"(kg:similarity(?c1, ?c2) as ?sim1) " +
			"(kg:similarity(?c2, ?c1) as ?sim2) " +
			"where {" +
			"?c1 a rdfs:Class " +
			"?c2 a rdfs:Class " +
			"filter(?c1 < ?c2)" +
			"" +
			"}" +
			"pragma {kg:similarity kg:cstep 32}" ;
		
		
		query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
				"select *  where {" +
					"select more * (kg:similarity() as ?s) where {" +
					"?x a c:Engineer" +
					"?x c:hasCreated ?doc " +
					"?doc a c:AnnualReport " +
					"}" +
					"order by desc(?s)" +
				"}" +
				"group by ?x ?doc " +
				"order by desc(?s)" +
				""  ;
		
		
		// Time : 1.504


		
		System.out.println("Start");
		Mappings map = null;
		try {
			//exec.query(init);
			
			long t1 = new Date().getTime();
			for (int i=0; i<10; i++)
			map = exec.query(query);
			long t2 = new Date().getTime();

			System.out.println(map);
			System.out.println("Size:  " + map.size());
			System.out.println("Time : " + (t2-t1)/1000.0);

			} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
}
