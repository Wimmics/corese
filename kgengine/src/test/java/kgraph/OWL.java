package kgraph;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.Load;

public class OWL {
	
	
	public static void main(String[] args) throws EngineException{
		new OWL().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		load.load(data + "kgraph/owl.rdf");
		load.load(data + "kgraph/owl.rul");
		
		RuleEngine re = load.getRuleEngine();
		re.process();
		
		QueryProcess exec = QueryProcess.create(graph);
		exec.definePrefix("n", "http://ns.inria.fr/edelweiss/2011/onto#");
		
		String query = "select * where {" +
				"?x rdf:type n:Person " +
				"}";
		
		query = "select * where {" +
				"?x rdfs:subClassOf ?y" +
				"}";
		
		query = "select * where {" +
		"?x rdf:type n:Person " +
		"}";
		
		Mappings map = exec.query(query);
		
		System.out.println(map);
		System.out.println(map.size());

		
	}
	
	
}
