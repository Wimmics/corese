package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Entity;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;

public class Test {
	
	
	public static void main(String[] args) throws EngineException{
		new Test().process();
	}
	
	void process() {
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String w3c = "/home/corby/workspace/coreseV2/src/test/resources/data/test-suite-archive/data-r2/";
		
		Graph graph = Graph.create();
		
		Load load = Load.create(graph);
		
		System.out.println("start");
	
		long t1 = new Date().getTime();
		int c = 0;
		for (int i = 1; i<10000000; i++){
			add(graph);
			if (c++ >= 100000){
				System.out.println(i);
				c = 1;
			}
		}
		long t2 = new Date().getTime();
		System.out.println((t2-t1)/1000.0);
		
		System.out.println("index");

		 t1 = new Date().getTime();
		graph.prepare();
		 t2 = new Date().getTime();
		System.out.println((t2-t1)/1000.0);
		
		System.out.println(graph);

		QueryLoad ql = QueryLoad.create();
		

		QueryProcess exec = QueryProcess.create(graph);		
		
		String init = 
			"prefix i: <http://www.inria.fr/test/> " +
			"" +
			"insert data {" +
			"i:a foaf:knows i:b " +
			"i:b foaf:knows i:c " +
			"i:c foaf:knows i:d " +
			"}";

		String query = 		"" +
		"prefix i: <http://www.inria.fr/test/> " +
				"select * where {" +
				"i:a (foaf:knows*)* ?y " +
				"}";
		
		query = 
				"select * (max(pathLength($path)) as ?max) where {" +
				"{select ?x where {?x foaf:knows ?y} limit 1}" +
				"{select * where {?x (foaf:knows) + :: $path ?y} " +
				" order by desc(pathLength($path)) }" +
				"} ";

		// 1.7
		DatatypeMap.setSPARQLCompliant(true);
		System.out.println(query);

		System.out.println("start");
		
		//exec.setListPath(true);

		
		Mappings map = null;
		 t1 = new Date().getTime();
		 for (int i = 0; i<5; i++){
			 System.out.println("query: " + i);
			 try {
				 //map =exec.query(init);
				 map =exec.query(query);
				 System.out.println(map);
				 System.out.println(map.size());
				 //System.out.println(map.getQuery().getAST());
				 //System.out.println(TripleFormat.create((Graph)map.getGraph()));

			 } catch (EngineException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }
		
		 t2 = new Date().getTime();
		//map = exec.query(query);
		
		//if (map != null) System.out.println(ResultFormat.create(map));

		System.out.println(map.size());
		System.out.println(map.get(0).getPath("?path"));


		System.out.println((t2-t1)/1000.0);
		//System.out.println(XMLFormat.create(map));

DatatypeMap.setLiteralAsString(false);
		
	
	}
	
	void add3(Graph g){
		Double ds 		= Math.random();
		Node subject 	= g.addResource("s" + ds);

	}
	
	void add(Graph g){
		
		Double ds 		= Math.random() * 1000000;
		Double dv 		= Math.random() * 1000000;
		Double dp 		= Math.random() * 10;

		
		Node source 	= g.addGraph   ("g");
		Node subject 	= g.addResource("s" + ds.intValue());
		Node predicate 	= g.addProperty("foaf:knows");
		Node object 	= g.addResource ("s" + dv.intValue());
		
		Entity edge = g.create(source, subject, predicate, object);
		//System.out.println(edge);
		g.addEdge(edge);
		
	}
	
	void test(Mappings ms){
		ms.getValue("?s");
		for (Mapping m : ms){
			IDatatype dt = (IDatatype) m.getValue("?s");
		}
		
	}
	
	
}
