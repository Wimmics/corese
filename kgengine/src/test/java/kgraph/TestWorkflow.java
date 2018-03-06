package kgraph;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class TestWorkflow {
	
	
	public static void main(String[] args) throws EngineException{
		new TestWorkflow().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create();
		Load load = Load.create(graph);
		load.load(data + "comma/comma.rdfs");
		
		QueryProcess exec = QueryProcess.create(graph);
		
		String s1 = "select * where {" +
				"?x rdfs:label ?l ; rdf:type rdfs:Class" +
				"}";
		
		String s2 = "select * where {" +
		"?x rdfs:label ?l; rdf:type rdf:Property" +
		"}";
		
		Query q1 = exec.compile(s1);
		Query q2 = exec.compile(s2);

		
		
		Mappings map = exec.query(q1.union(q2));
		
		System.out.println(map);
		
		
	}
	
	
}
