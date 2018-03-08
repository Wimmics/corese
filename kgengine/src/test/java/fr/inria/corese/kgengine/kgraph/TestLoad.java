package fr.inria.corese.kgengine.kgraph;

import java.util.Date;
import java.util.Hashtable;

import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;

public class TestLoad  {
	Hashtable<String, String> blank;
	Graph graph;
	
	TestLoad(){
		blank = new Hashtable<String, String>();
	}
	
	public static void main(String[] args) throws EngineException{
		new TestLoad().process();
	}
	

	

	
	void process() {
		
		System.out.println("load");
		
		graph = Graph.create(true);
		graph.setOptimize(true);
		Load load = Load.create(graph);
		graph.set(Entailment.DATATYPE_INFERENCE, true);
		//graph.set(Entailment.DUPLICATE_INFERENCE, false);

		
		QueryLoad ql = QueryLoad.create();
		
	//graph.getEdgeFactory().define("http://www.inria.fr/test/name", EdgeExtend.class);

		QueryProcess exec = QueryProcess.create(graph);		
		//exec.setPathLoop(false);
		
		String init = "prefix c: <http://www.inria.fr/test/>" +
				"insert data {" +
				"tuple(c:name <a>  'a' 5 5)" +
				"tuple(c:name [c:name 'Jim']  'b' 5 [c:name 'Paul'])" +
				"}";
		
		load.setDebug(true);
		//load.setMax(10);
		long tt1 = new Date().getTime();
		
		//load.exclude(RDF.RDF);
		
		
		// <http://dbpedia.org/ontology/wikiPageWikiLink>: 2955528

		
		load.load("/user/corby/home/Download/3hops_Musee_du_Louvre", "g1");
		long tt2 = new Date().getTime();

		String query = 

		"select distinct ?t  where {" +
		"?x  s (! rdf:type){0,5} ?t filter(isURI(?t))" +
		"}" +
		"order by ?t " +
		"bindings ?x {" +
		"(<http://dbpedia.org/resource/Mus%C3%A9e_du_Louvre>)" +
		"} ";
		

		String q1 = "SELECT (count(distinct ?x) as ?count) WHERE{?x rdf:type <http://dbpedia.org/ontology/Artist>}";


		String q2 = "SELECT (count(distinct ?x) as ?count) WHERE{?x rdf:type <http://dbpedia.org/ontology/Person>}";


		String q3 = "SELECT (count(distinct ?x) as ?count) WHERE{?x rdf:type <http://dbpedia.org/ontology/Museum>}";


		String q4 = "SELECT (count(distinct ?x) as ?count) WHERE{?x rdf:type <http://dbpedia.org/ontology/Building>}";


		
		System.out.println("load: " + (tt2-tt1)/1000.0);

		System.out.println("first");
		long t1 = new Date().getTime();
		graph.prepare();
		long t2 = new Date().getTime();	
		System.out.println("first: " + (t2-t1)/1000.0);
				
		exec.setPathLoop(false);
		exec.setListPath(true);
		
		System.out.println("graph: \n" + graph);
		System.out.println("graph: \n" + graph.toString2());


//		ResultListenerImpl rl = new ResultListenerImpl();
//		exec.addResultListener(rl);
			
		for (int i=0; i<1; i++){
			t1 = new Date().getTime();
			try {
				Mappings map;
				map = exec.query(q1);
				System.out.println("map: " + map);	
				map = exec.query(q2);
				System.out.println("map: " + map);					
				map = exec.query(q3);
				System.out.println("map: " + map);	
				map = exec.query(q4);
				System.out.println("map: " + map);	
			} 
			catch (EngineException e) {
				e.printStackTrace();
			} 
			t2 = new Date().getTime();	
			System.out.println("exec: " + (t2-t1)/1000.0);	
		}
		

		
		System.out.println("graph: " + graph.size());
		System.out.println("load: " + (tt2-tt1)/1000.0);
		

		
		
		
		
	}

	
	
	void process2() {

		System.out.println("load");
		
		IEngine engine = new EngineFactory().newInstance();
		long tt1 = new Date().getTime();
		//load.getBuild().exclude("*");
		
		try {
			engine.load("/user/corby/home/Download/3hops_Musee_du_Louvre");
		} catch (EngineException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long tt2 = new Date().getTime();

		String query = 

		"select distinct ?t where {" +
		"?x  (! rdf:type){0,3} ?t filter(isURI(?t))" +
		"}" +
		"order by ?t " +
		"bindings ?x {" +
		"(<http://dbpedia.org/resource/Mus%C3%A9e_du_Louvre>)" +
		"} ";
		
		String query2 = 

			"select distinct ?t where {" +
			"?x  s (! rdf:type){0,3} ?t filter(isURI(?t))" +
			"}" +
			"order by ?t " +
			"bindings ?x {" +
			"(<http://dbpedia.org/resource/Mus%C3%A9e_du_Louvre>)" +
			"} ";
		
		System.out.println("load: " + (tt2-tt1)/1000.0);

		System.out.println("first");
		long t1 = new Date().getTime();
		long t2 = new Date().getTime();	
		System.out.println("first: " + (t2-t1)/1000.0);
				
		for (int i = 0; i<2; i++){
			t1 = new Date().getTime();
			try {
				IResults map = engine.SPARQLQuery(query2);
				System.out.println("nb res: " + map.size());				
			} 
			catch (EngineException e) {
				e.printStackTrace();
			} 
			t2 = new Date().getTime();	
			System.out.println("exec: " + (t2-t1)/1000.0);	
		}
		

		System.out.println("load: " + (tt2-tt1)/1000.0);

		//System.out.println(graph.getIndex());

	}
	
	
	public String getID(String b){
		String id = blank.get(b);
		if (id == null){
			id = graph.newBlankID();
			blank.put(b, id);
		}
		return id;
	}
	
	
}
