package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;

public class Start {
	
	
	public static void main(String[] args) throws EngineException{
		new Start().process();
	}
	
	void process() {
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String w3c = "/home/corby/workspace/coreseV2/src/test/resources/data/test-suite-archive/data-r2/";
		System.out.println("load");
		
		Graph graph = Graph.create(true);
		graph.setOptimize(true);
		Load load = Load.create(graph);
		
		
		
		
		try {
			load.loadWE(data + "w3c-sparql11/data/earl.ttl");
			load.loadWE(root + "alu/bbcontology.owl");

		} catch (LoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		graph.set(Entailment.DATATYPE_INFERENCE, true);
		//graph.set(Entailment.DUPLICATE_INFERENCE, false);


		
		QueryLoad ql = QueryLoad.create();
		

		QueryProcess exec = QueryProcess.create(graph);		
		
		String init = 
			"prefix i: <http://www.inria.fr/test/> " +
			"" +
			"insert data {" +
			"i:a foaf:knows i:b " +
			"i:b foaf:knows i:c " +
			"i:c foaf:knows i:d ; a foaf:Person" +
			"}";

		String query = 		"" +
		"prefix i: <http://www.inria.fr/test/> " +
				"select * where {" +
				"i:a (foaf:knows*)* ?y " +
				"{select (exists {?a foaf:knows ?b} as ?v) where {" +
				"?c foaf:knows ?d} group by (exists {?e foaf:knows@[a foaf:Person] ?f})" +
				"}" +
				"}";
		
		
		// 1.7
		DatatypeMap.setSPARQLCompliant(true);
		System.out.println(query);

		System.out.println("start");
		
		//exec.setListPath(true);

		
		Mappings map = null;
		long t1 = new Date().getTime();
		
			try {
				map =exec.query(init);
				map =exec.query(query);
				//System.out.println(map);
				System.out.println(map.getQuery().getAST());
				//System.out.println(TripleFormat.create((Graph)map.getGraph()));

			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		long t2 = new Date().getTime();
		//map = exec.query(query);
		
		//if (map != null) System.out.println(ResultFormat.create(map));

		System.out.println(map);

		System.out.println((t2-t1)/1000.0);
		//System.out.println(XMLFormat.create(map));

		
		

	}
	
	
	
	
	
	void test(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		Graph graph = Graph.create(true);
		graph.set(Entailment.DATATYPE_INFERENCE, true);

		Load load = Load.create(graph);
		graph.setOptimize(true);
		
		long t1 = new Date().getTime();
		load.load(data + "kgraph/rdf.rdf",  RDF.RDF);
		load.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
		load.load(data + "comma/comma.rdfs");
		load.load(data + "comma/commatest.rdfs");
		load.load(data + "comma/model.rdf");
		load.load(data + "comma/testrdf.rdf");
		load.load(data + "comma/data");
		//load.load(data + "comma/data2");
		long t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");
		System.out.println(graph);
		
		
		QueryProcess exec = QueryProcess.create(graph);
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select debug * where { " +
			"?x c:FamilyName 'Corby'  " +
			"?x c:isMemberOf ?org "+  
			"?x c:FirstName ?name  " +
			//"filter  (?name = 'toto' || ?org ~ 'inria' )" +
			"} ";
		
		query ="select debug distinct  ?y ?t    where {" +
				"?x ?p ?y   filter( ?x ~ 'olivier.corby')  " +
				
				"?z ?q ?t   filter(?z ~ 'olivier.corby')   " +
				
				"filter (datatype(?y) = datatype(?t)) " +
				"} order by ?y";
	
		t1 = new Date().getTime();
		graph.init();
		t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");
		
		try {
			t1 = new Date().getTime();
			Mappings map = exec.query(query);
			t2 = new Date().getTime();
			System.out.println((t2-t1) / 1000.0 + "s");
			System.out.println(map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
