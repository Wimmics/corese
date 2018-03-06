package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class From {
	
	
	public static void main(String[] args) {
		new From().process();
	}
	
	void process() {
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		System.out.println("load");
		Load load = Load.create(graph);
		load.load(data + "comma/comma.rdfs");
		load.load(data + "comma/data2");

		
		String from = "from kg:entailment \n";
		for (Node n : graph.getGraphNodes()){
			from += "from   <" + n.getLabel() + ">\n"	;
		}
		
		String query1 =
			"prefix c: <http://www.inria.fr/acacia/comma#> "+ 
			"select * " + 
			from + // 11.6 vs 5.7 vs 1.75
			"where {" +
			"?x a c:Person " +
			"?x c:hasCreated ?doc ;  c:FirstName ?n1; c:FamilyName ?n2 " +
			"" +
			"?doc a c:Document; c:Title ?t " +
			"}";
		
		String query =
			"prefix c: <http://www.inria.fr/acacia/comma#> "+ 
			"select  * " + 
			from + // 11.6 vs 5.7 vs 1.75
			"where {" +
			//"graph ?g {" +
				" " +
				"?doc c:CreatedBy ?x ?" +
				"x  c:FirstName ?n1; c:FamilyName ?n2 " +
				"" +
				"?doc  c:Title ?t " +
			//"}" +
			"}";
		
		
		QueryProcess exec = QueryProcess.create(graph);
		System.out.println(query);

		System.out.println("start");
		long t1 = new Date().getTime();
		Mappings map = null;
			try {
				for (int i=0; i<10; i++)
				map = exec.query(query);
				//exec.compile(query);
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		long t2 = new Date().getTime();

		System.out.println(map.size());
		System.out.println((t2-t1)/1000.0);

		
		
	}
	
	
}
