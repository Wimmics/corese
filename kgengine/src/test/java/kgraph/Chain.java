package kgraph;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class Chain {
	
	
	public static void main(String[] args) throws EngineException{
		new Chain().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create();
		Load load = Load.create(graph);
		
		String init = 			
			"prefix c: <http://www.inria.fr/acacia/comma#> " +
			"insert data {" +
				"[ c:chain (c:p1 c:p2)] ." +
				"" +
				"<a> c:p1 <b> " +
				"<b> c:p2 <c>" +
				"}";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		Load ld = Load.create(graph);
		ld.load(root + "alu/dataset20.rdf");
		
		
		String query = 
			"select distinct ?x ?y ?z ?t  where {" +
			"{ {?x ?p ?y}  . " +
			   "filter(?x = <http://A> || ?y = <http://A>) " +
			"} " +
			"union " +
			"{ {{?x ?p ?y} union {?y ?p ?x}} . {{?y ?q ?z} union {?z ?q ?y}} . " +
			   "filter(?x = <http://A> || ?y = <http://A> || ?z = <http://A> )" +
			"} " +
			"union " +
			"{ {{?x ?p ?y} union {?y ?p ?x}} . {{?y ?q ?z} union {?z ?q ?y}} . {{?z ?r ?t} union {?t ?r ?z}} ." +
			   "filter(?x = <http://A> || ?y = <http://A> || ?z = <http://A> || ?t = <http://A> )" +
			"} " +				
			"}";
		
		Mappings map = exec.query(query);
		
		System.out.println(map);
		

		
		System.out.println(map.size());

		
	}
	

}
