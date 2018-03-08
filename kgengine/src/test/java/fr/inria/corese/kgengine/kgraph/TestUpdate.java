package fr.inria.corese.kgengine.kgraph;

import java.util.Date;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class TestUpdate {
	
	public static void main(String[] args){
		new TestUpdate().process();
	}
	
	void process(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String file = "file://" + data + "test.xml";
		
		String path = "file:///home/corby/workspace/coreseV2/src/test/resources/data";
		
		String ns = "data "  + path + "/comma/ " +
		"data2 " + path + "/comma/data2/  " +
		"data1 " + path + "/comma/data/  " +
		"c http://www.inria.fr/acacia/comma#";
		
		QuerySolver.defaultNamespaces(ns);
		
		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(true);
		//graph.set(RDFS.RANGE, true);
		ProducerImpl producer =  ProducerImpl.create(graph);
		Load loader =  Load.create(graph);
		
		System.out.println("load");
		
		long t1 = new Date().getTime();

		loader.load(data + "comma/comma.rdfs");
		loader.load(data + "comma/testrdf.rdf");
		loader.load(data + "comma/model.rdf");
		loader.load(data + "comma/data");
		loader.load(data + "comma/data2");

		long t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");
		
		t1 = new Date().getTime();
		graph.init();
		t2 = new Date().getTime();
		System.out.println((t2-t1) / 1000.0 + "s");
		
		String update = 
			"delete {?x  ?p ?y}" +
			"insert {?x  ?p ?y}" +
			" where {" +
			"?x c:FirstName 'Olivier' ; c:FamilyName 'Corby'; ?p ?y " +
			"}";
		
		String query = 
			"select * where {" +
			"?x rdf:type c:Person" +
			"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		System.out.println(graph.size());

		try {
			Mappings map = null;
			
			t1 = new Date().getTime();
			
			for (int i = 0; i<10; i++)
			map = exec.query(update);

			t2 = new Date().getTime();
			
			System.out.println(map);
			System.out.println((t2-t1) / 1000.0 + "s");
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//3.79s

		//38427
		
	}
	
	
	
	void trace(Graph graph){
		System.out.println(graph);
//		graph.init();
		//System.out.println(graph.getIndex());
		int n = 0;
//		for (Entity ent : graph.getIndex().get(graph.getNode(RDF.RDFTYPE))){
//			System.out.println(ent);
//			if (n++>50) break;
//		}
	}
	
	
	
	
	
	
	
	
	
}
