package fr.inria.corese.kgengine.junit;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.core.GraphStore;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.print.ResultFormat;
import static org.junit.Assert.assertEquals;

public class TestQueryDBPedia {
	
             
      @Test
    public void testAG2() throws EngineException {

        GraphStore gs = GraphStore.create();
        QueryProcess exec = QueryProcess.create(gs);
        String init = "insert data {"
                + "[] rdfs:label 'Auguste'@fr "
                + "[] rdfs:label 'Auguste'@fr "
                + "}";
        
        String q = "select   * "
                + "(group_concat(?ll) as ?gc) "
                + "where {"
                + "?x rdfs:label ?l "
                + "service <http://fr.dbpedia.org/sparql>{"
                + "?y rdfs:label ?l "
                + "}"
                + "service <http://fr.dbpedia.org/sparql> {"
                + "?y rdfs:label ?ll"
                + "}"
                + "}"
                + "group by ?x";
        
        exec.query(init);
        Mappings map = exec.query(q);
       assertEquals(2, map.size());
        
        }
        
    
    @Test
	public void testDBP() throws EngineException{			
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
                String query = "select * where {"
                        + "service <http://dbpedia.org/sparql/>{"
                        + "select * where {"
                        + "<http://dbpedia.org/resource/Paris> ?p ?y"
                        + "} limit 10"
                        + "}"
                        + "}";
                
                Mappings map = exec.query(query);
		assertEquals("Result", 10, map.size());

                
    }
    
	@Test
	public void test61(){			
		DatatypeMap.setLiteralAsString(false);
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>" + 
			"select  * where {" +
				"service <http://fr.dbpedia.org/sparql> {"+
				"<http://fr.dbpedia.org/resource/Auguste> p:successeur+ ?y .}" +
			"}";
		
		
		try {
			exec.addPragma(Pragma.PATH, Pragma.EXPAND, 12);
			Mappings map = exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 12, map.size());
			
			

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testExpandPragma() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		String query = 
				"prefix foaf: <http://www.inria.fr/acacia/comma#>" +
						"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
						"prefix p: <http://fr.dbpedia.org/property/>"+

			"insert {<http://fr.dbpedia.org/resource/Auguste> p:successeur ?y}  where {" +
			"service <http://fr.dbpedia.org/sparql> {" +
			"<http://fr.dbpedia.org/resource/Auguste> p:successeur+ ?y " +
			"}" +		"" +
			"}" 
			+
			"pragma {" +
			"kg:service kg:timeout 100" +
			"kg:path kg:expand 5" +
			"}" ;


		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		//exec.addPragma(Pragma.SERVICE, Pragma.TIMEOUT, 10);
		//exec.addPragma(Pragma.PATH, Pragma.EXPAND, 5);

		try {
						
			Mappings map = exec.query(query);
			
			assertEquals("Result", 5, g1.size());

			
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void testExpandPragma2() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		String query = 
			"prefix foaf: <http://www.inria.fr/acacia/comma#>" +
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>"+

			"insert {<http://fr.dbpedia.org/resource/Auguste> p:successeur ?y}  where {" +
			"service <http://fr.dbpedia.org/sparql> {" +
			"<http://fr.dbpedia.org/resource/Auguste> p:successeur+ ?y " +
			"}" +		"" +
			"}";


		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		exec.addPragma(Pragma.SERVICE, Pragma.TIMEOUT, 100);
		exec.addPragma(Pragma.PATH, Pragma.EXPAND, 5);

		try {
						
			Mappings map = exec.query(query);
			
			assertEquals("Result", 5, g1.size());

			
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testExpandPragma3() throws ParserConfigurationException, SAXException, IOException{
		Graph g1 = Graph.create(true);
		String query = 
			"prefix foaf: <http://www.inria.fr/acacia/comma#>" +
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" +
			"prefix p: <http://fr.dbpedia.org/property/>"+

			"insert {<http://fr.dbpedia.org/resource/Auguste> p:successeur ?y}  where {" +
			"service <http://fr.dbpedia.org/sparql> {" +
			"<http://fr.dbpedia.org/resource/Auguste> p:successeur+ ?y " +
			"}" +		"" +
			"}";


		String del = "clear all";
		
		QueryProcess exec = QueryProcess.create(g1);
		exec.addPragma(Pragma.SERVICE, Pragma.TIMEOUT, 1);
		exec.addPragma(Pragma.PATH, Pragma.EXPAND, 5);

		try {
						
			Mappings map = exec.query(query);
			
			assertEquals("Result", 0, g1.size());

			
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Test
	public void test601(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"construct {?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr}" +
			" where {" +
				"service <http://fr.dbpedia.org/sparql> {" +
					"?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr " +
				"}" +
			"}";
		
		
		try {
			Mappings map = exec.query(query);
			//ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 6, map.size());
			Graph gg = (Graph) map.getGraph();
			assertEquals("Result", 6, gg.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	@Test
	public void test60(){			
		
		Graph graph = Graph.create();	
		QueryProcess exec = QueryProcess.create(graph);	
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/>" + 
			"select debug * where {" +
				"service <http://fr.dbpedia.org/sparql> {" +
					"?x foaf:name 'Olivier'@fr ; ?p 'Olivier'@fr " +
				"}" +
			"}";
		
		
		try {
			Mappings map = exec.query(query);
			ResultFormat f = ResultFormat.create(map);
			//System.out.println(f);
			assertEquals("Result", 6, map.size());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	
	
	
	@Test
	public void testJoin2(){
		Graph g = Graph.create();
		Load ld = Load.create(g);
		
		String init = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"insert data   {" +
			"graph <g1> {" +
		"<John> foaf:name 'John' " +
		"<http://fr.dbpedia.org/resource/Auguste>  foaf:knows <James>" +
		"<http://fr.dbpedia.org/resource/Augustus> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augustin> foaf:knows <Jim>" +
		"<http://fr.dbpedia.org/resource/Augusgus> foaf:knows <Jim>" +
		"}" +

		"graph <g1> {" +		
		"<Jim> foaf:knows <James>" +
		"<Jim> foaf:name 'Jim' " +
		"}" +
		"}";
		
		String query = 
			"prefix foaf: <http://xmlns.com/foaf/0.1/> " +
			"select debug * where {" +
			
                        	"?x foaf:knows ?y " +
                       
				"service <http://fr.dbpedia.org/sparql> {" +
					"select * where {" +
					"?x rdfs:label ?n " +
					"filter(regex(?n, '^August'))" +
					"} limit 20" +
				"}" +
				

				"service <http://fr.dbpedia.org/sparql> {" +
					"select * where {" +
					"?x rdfs:label ?n " +
					"}" +
				"}" +				
			"}" +
			"pragma {kg:kgram kg:detail true}" +
			"";
		
		QueryProcess exec = QueryProcess.create(g);
		exec.setSlice(30);
		exec.setDebug(true);

		try {
			exec.query(init);
		
			Mappings map = exec.query(query);
			System.out.println(map);
			
			assertEquals("Result", 12, map.size());
			
		} 
		catch (EngineException e) {
			e.printStackTrace();
		}
		
		
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
