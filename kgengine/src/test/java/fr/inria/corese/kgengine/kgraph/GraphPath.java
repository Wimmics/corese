package fr.inria.corese.kgengine.kgraph;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class GraphPath {
	
	
	public static void main(String[] args) throws EngineException{
		new Start().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/corese/data/geographicalData";

		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		load.load(data + "ontologies");
		load.load(data + "annotations");

		QueryProcess exec = QueryProcess.create(graph);
		
		
		String init = "insert data {" +
		"<aaa> rdf:value ((1) (2 3) (4))" +
		"}";
		
		String query = "select * where {" +
				"" +
				"}";
		
		

		
		Mappings map = exec.query(query);
		
		System.out.println(map);
		
		
		
	}
	
	
}
