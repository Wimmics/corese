package fr.inria.corese.kgengine.kgraph;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.engine.core.Engine;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class TestRIF {
	
	
	public static void main(String[] args) throws EngineException{
		new TestRIF().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create();
		Load load = Load.create(graph);
		load.load(data + "rule/test.rdf");
		
		
		QueryProcess exec = QueryProcess.create(graph);
		
		Engine re = Engine.create(exec);
		re.load(data + "rule/test.brul");
		

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select  * where {" +
			"[ c:name c:isFatherOf ;" +
//			"c:term ?v1 " +
//			"?v1 rdf:first c:Bernard " +
//			"?v1 rdf:rest ?v2 " +
//			"?v2 rdf:first ?y " +
			"c:term(?x ?y)" +
			"]" +
			
			"}";
		
		
		
//		Mappings map = exec.query(query);
////		
//		System.out.println(map);
//		System.out.println(map.size());
		
		LBind lb  = re.SPARQLProve(query);
		
		System.out.println(lb);
		System.out.println(lb.size());

	}
	
	
}
