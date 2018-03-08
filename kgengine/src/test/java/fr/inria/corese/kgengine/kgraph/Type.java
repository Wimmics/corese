package fr.inria.corese.kgengine.kgraph;

import java.util.Date;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class Type {
	
	
	public static void main(String[] args) throws EngineException{
		new Type().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		//graph.set(RDFS.SUBCLASSOF, true);
		Load load = Load.create(graph);
		System.out.println("load");
		load.load(data + "comma/comma.rdfs");
		load.load(data + "comma/data");
		load.load(data + "comma/data2");
		load.load(data + "comma/model.rdf");

		QueryProcess exec = QueryProcess.create(graph);
			
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select (count(*) as ?c) where {" +
				"?x rdf:type c:Person" +
				"}";
		// 0.741

		Mappings map = null;
		
		System.out.println("query");
		long t1 = new Date().getTime();
		//for (int i = 0; i<10; i++)
		map = exec.query(query);
		long t2 = new Date().getTime();
		

		System.out.println(map);
		System.out.println(map.size());
		System.out.println((t2-t1)/1000.0);

		//test();
		
	}
	
	
	
}
