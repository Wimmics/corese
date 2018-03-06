package junit;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class LocalTest {
	
//	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        static String data = LocalTest.class.getClassLoader().getResource("data").getPath()+"/";

	static Graph graph;
	
	@BeforeClass
	static public void init(){
		QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");

		graph = Graph.create();
		//graph.setOptimize(true);
		
		Load ld = Load.create(graph);
		init(graph, ld);
	}
	
	static  void init(Graph g, Load ld){
		ld.load(data + "comma/comma.rdfs");
		ld.load(data + "comma/model.rdf");
		ld.load(data + "comma/data");
	}
	
	
	
	@Test
	public void test14(){
				
		String query = "select  *  where {" +
				"?x c:FirstName 'Fabien' " +
				"?x rdf:type c:Person" +
				"; c:hasCreated ?doc " +
				//"?doc rdf:type/rdfs:subClassOf* c:Document " +
//				"c:Document rdfs:label ?l ;" +
//				"rdfs:comment ?c" +
				"}"  ;
				
		try {
			
			Graph g = Graph.create(true);
			g.setTag(true);
			Load ld = Load.create(g);
			//ld.setBuild(new MyBuild(g));
						
			init(g, ld);
			
			QueryProcess exec = QueryProcess.create(g);
			Mappings map = exec.query(query);
			
			System.out.println(map);
			
			assertEquals("Result", 9, map.size()); 	

		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}

	}

}
