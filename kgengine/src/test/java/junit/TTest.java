package junit;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;

import static org.junit.Assert.assertEquals;


public class TTest {
	
//	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        static String data = TTest.class.getClassLoader().getResource("data").getPath()+"/";
//	static String test = "/home/corby/workspace/coreseV2/text/";
//	static String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
//	static String text = "/home/corby/workspace/kgengine/src/test/resources/text/";

	//static String data = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/data/";
        static String root = data;
        static String text = "/home/corby/NetBeansProjects/kgram/trunk/kgengine/src/test/resources/text/";

	static Graph graph;
	
	@BeforeClass
	static public void init(){
		QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

		graph = Graph.create(true);
		graph.setOptimize(true);
		
		Load ld = Load.create(graph);
		init(graph, ld);
	}
	
	static  void init(Graph g, Load ld){
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");
	}
	
	
	@Test
	public void test18(){
		String query = "select ?a ?p ?b where {" +
				"c:Person rdfs:subClassOf+ :: $path ?c " +
				"graph $path {?a ?p ?b}" +
				"}" +
				"order by ?a ?p ?b";
		
		QueryProcess exec = QueryProcess.create(graph);
		
		try {
			Mappings map = exec.query(query);
			System.out.println(map);
			assertEquals("Result", 28, map.size()); 	
		} catch (EngineException e) {
			assertEquals("Result", 28, e); 	
		}

	}
	
	
	
	
	// 15
	
	
	

}
