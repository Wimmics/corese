package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.api.Log;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.LogImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.print.ResultFormat;

public class Path {
	
	
	public static void main(String[] args) throws EngineException{
		new Path().process();
	}
	
	void process() {
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String root = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		System.out.println("load");
		
		LogImpl log = LogImpl.create();
		log.setTrace(true);
		
		Graph graph = Graph.create(true);
		Graph g2 = Graph.create(true);
		g2.setLog(log);
		Load load = Load.create(g2);
		//load.load(root + "comma/comma.rdfs");
		
		QueryProcess exec = QueryProcess.create(graph);
		graph.setLog(log);
		
		exec.add(g2);
		
		
		String init = 
			"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"insert data {" +
				"ex:a foaf:knows ex:b " +
				"ex:b foaf:knows ex:c " +
				
				"ex:a ex:rel ex:d " +
				"ex:d ex:rel ex:e " +
				"ex:e ex:rel ex:c " +
				"}";
		
		String query = 
			"prefix ex: <http://test/> " +
			"prefix foaf: <http://foaf/> " +
			"select * where {" +
			"ex:a foaf:knows [] " +
			"ex:a ( ex:rel+ | foaf:knows+ ) ?y" +
			"}";

		
		init = 
			"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"insert data {" +
				"ex:a foaf:knows ex:b " +
				"ex:b foaf:knows ex:c " +
				
				"ex:a ex:rel ex:b " +
				"ex:b ex:rel ex:c " +
				"" +
				"ex:b rdfs:seeAlso ex:a " +
				"ex:c rdfs:seeAlso ex:b " +
			"}";
		
		 query = 
			"prefix ex: <http://test/> " +
			"prefix foaf: <http://foaf/> " +
			"select * where {" +
			//"ex:a foaf:knows [] " +
			//"ex:a (  foaf:knows || ^rdfs:seeAlso)+ ?y" +
			"ex:a (  foaf:knows+ || (^rdfs:seeAlso) +) ?y" +

			"}";
		
		
		
		 init = 
				"prefix ex: <http://test/> " +
					"prefix foaf: <http://foaf/> " +
					"insert data {" +
					"foaf:knows rdfs:domain foaf:Person " +
					"foaf:knows rdfs:range foaf:Person " +
					
					"ex:a foaf:knows ex:b " +
					"ex:b rdfs:seeAlso ex:c " +
					"ex:c foaf:knows ex:d " +
					"ex:d rdfs:seeAlso ex:e " +					

					"" +
					"ex:a ex:rel ex:b " +
					"ex:b ex:rel ex:c " +
					"ex:c ex:rel ex:d " +					
					"ex:c ex:rel ex:e " +
					
					

					
				"}";
			
			 query = 
				"prefix ex: <http://test/> " +
				"prefix foaf: <http://foaf/> " +
				"select * (1 as ?val) where {" +
				//"ex:a foaf:knows [] " +
				//"ex:a (  foaf:knows || ^rdfs:seeAlso)+ ?y" +
				"?x (foaf:knows/rdfs:seeAlso @{?this a foaf:Person} || ex:rel+)+ ?y" +
				//"?x (foaf:knows/rdfs:seeAlso @{?this a foaf:Person})+ ?y" +

				"}" +
				"pragma {" +
				//"kg:kgram kg:status true" +
				"}";
			
		 
		
		System.out.println("start");
		
		
		Mappings map = null;
		long t1 = new Date().getTime();
		
			try {
				exec.query(init);
				map = exec.query(query);
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		long t2 = new Date().getTime();
		//map = exec.query(query);
		ResultFormat f = ResultFormat.create(map);
		System.out.println(f);

		System.out.println(map.size());
		System.out.println((t2-t1)/1000.0);

		// 1.564

			for (Object name : log.get(Log.QUERY)){
				System.out.println(name);
			}
			for (Object name : log.get(Log.UPDATE)){
				System.out.println(name);
			}

			map.getValue("?x");
			
			for (Node sn : map.getSelect()){
				for (Mapping m : map){
					if (m.isBound(sn)){
						IDatatype dt = (IDatatype) m.getValue(sn);
						if (dt.isNumber()){
							
							System.out.println(sn + " " +  dt.getLabel());
						}
						else if (dt.isURI()){
							dt.getDatatypeURI();
							System.out.println(sn + " " +  dt.toSparql());
						}
						else {
							System.out.println(sn + " " +  dt.toSparql());
						}
					}
				}
			}
			
			Double d = 1.0;
			Integer i = 1;
			
			Float ff = new Float(1);
			
			if (i < ff){
				
			}
			
			Double l = new Double(+1);
		
	}
	
	
}
