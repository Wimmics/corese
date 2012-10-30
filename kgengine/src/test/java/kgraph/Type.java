package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.RDFS;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

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
