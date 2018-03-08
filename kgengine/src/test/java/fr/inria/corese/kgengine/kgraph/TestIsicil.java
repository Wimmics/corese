package fr.inria.corese.kgengine.kgraph;

import fr.inria.corese.kgengine.GraphEngine;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;
import fr.inria.corese.kgtool.print.XMLFormat;

public class TestIsicil {
	
	
	public static void main(String[] args) throws EngineException{
		new TestIsicil().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

		GraphEngine engine = GraphEngine.create();
		Graph graph = Graph.create();
		Load ld = Load.create(graph);
		
		QueryLoad ql = QueryLoad.create();
		String ud = ql.read(data + "isicil/update.rq");
		String qq = ql.read(data + "isicil/test.rq");
		
		ld.load(data + "isicil/semSNA.rdf");
		
		
		QueryProcess exec = QueryProcess.create(graph);
		
		String query = "";
		
		Mappings map = exec.query(qq);
		
		
		XMLFormat f = XMLFormat.create(map);
		System.out.println(f);

		
	}
	
	
}
