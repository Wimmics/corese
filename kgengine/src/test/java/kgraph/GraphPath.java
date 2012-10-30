package kgraph;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

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
