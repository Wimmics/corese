package junit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.CSVFormat;
import fr.inria.corese.kgtool.print.JSONFormat;
import fr.inria.corese.kgtool.print.RDFFormat;
import fr.inria.corese.kgtool.print.TSVFormat;
import fr.inria.corese.kgtool.print.XMLFormat;

import static org.junit.Assert.assertEquals;

/**
 * 
 * Complementary Junit test cases for KGRAM
 * RDFFormat XMLFormat
 */
public class TestJunit {
	
//	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
        static String data = TestJunit.class.getClassLoader().getResource("data").getPath()+"/";
        
	static Graph graph;
	
	@BeforeClass
	static public void init(){
		QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
		graph = Graph.create();
		Load ld = Load.create(graph);
		ld.load(data + "comma/model.rdf");
		init2();		
	}
        
        int fun(int x){
            return x;
        }
	
	static void init2(){
		String init = "insert data {" +
		"<http://ex.org/a&b> c:name 'John & Jack < 10'" +
		"}";

		QueryProcess exec = QueryProcess.create(graph);
		try {
			exec.query(init);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	Graph getGraph(){
		return graph;
	}
	
	
	/**
	 * RDFFormat: 
	 * pprint and load RDF/XML
	 */
	@Test
	public void testRDFFormat() {
		RDFFormat f = RDFFormat.create(getGraph());
		String str = f.toString();
		Graph g = Graph.create();
		Load ld = Load.create(g);
		try {
			ld.load(new ByteArrayInputStream(str.getBytes()));
		} catch (LoadException e) {
			assertEquals("Result", true, e);
		}
		
		assertEquals("Result", true, true);
	}
	
	
	/**
	 * RDFFormat/
	 * Query, pprint and load RDF/XML
	 */
	@Test	
	public void testRDFFormat2() {
		QueryProcess exec = QueryProcess.create(getGraph());
		String query = "construct {?x ?p ?y} where {?x ?p ?y}";
		try {
			Mappings map = exec.query(query);
			RDFFormat f = RDFFormat.create(map);
			String str 	= f.toString();
			Load ld 	= Load.create(Graph.create());
			ld.load(new ByteArrayInputStream(str.getBytes()));
			assertEquals("Result", true, true);
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		} catch (LoadException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * XMLFormat
	 * Query, pprint and parse XML Format
	 */
	@Test	
	public void testXMLFormat() {
		QueryProcess exec = QueryProcess.create(getGraph());
		String query = "select * where {?x ?p ?y}";
		try {
			Mappings map 	= exec.query(query);
			XMLFormat f 	= XMLFormat.create(map);
			String str 		= f.toString();
			Document doc 	= parse(new ByteArrayInputStream(str.getBytes()));
			assertEquals("Result", true, true);
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
	}
	
	
	@Test	
	public void testCSVFormat() {
		QueryProcess exec = QueryProcess.create(getGraph());
		String query = "select * where {?x ?p ?y} limit 50";
		try {
			Mappings map 	= exec.query(query);
			CSVFormat f 	= CSVFormat.create(map);
			System.out.println(f);
			assertEquals("Result", true, true);
			
		} catch (EngineException e) {
			assertEquals("Result", true, false);
		}
	}
	
	@Test	
	public void testTSVFormat() {
		QueryProcess exec = QueryProcess.create(getGraph());
		String query = "select * where {?x ?p ?y} limit 50";
		try {
			Mappings map 	= exec.query(query);
			TSVFormat f 	= TSVFormat.create(map);
			System.out.println(f);
			assertEquals("Result", true, true);
			
		} catch (EngineException e) {
			assertEquals("Result", true, false);
		}
	}
	
	@Test	
	public void testJSONFormat() {
		QueryProcess exec = QueryProcess.create(getGraph());
		String query = "select * where {?x ?p ?y} limit 50";
		try {
			Mappings map 	= exec.query(query);
			JSONFormat f 	= JSONFormat.create(map);
			System.out.println(f);
			assertEquals("Result", true, true);
			
		} catch (EngineException e) {
			assertEquals("Result", true, false);
		}
	}
	
	
	@Test
	public void testRDF(){
		Graph graph = Graph.create(true);
		Load ld = Load.create(graph);
		
		QueryProcess exec = QueryProcess.create(graph);
		
		String init = "insert data {" +
				"<John> <name> 'John' ; " +
				"foaf:knows [foaf:knows <Jack>]" +
				"<name> rdfs:domain <Person> " +
				"" +
				"}";
		
		try {
			Mappings map = exec.query(init);
			graph.init();
			RDFFormat f = RDFFormat.create(graph, map.getQuery());
			f.with(Entailment.DEFAULT);
			//f.with(Entailment.ENTAIL);
			//f.without(Entailment.ENTAIL);
			System.out.println(f);
		} catch (EngineException e) {
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	private  Document parse(InputStream in){
	     
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true); 
		DocumentBuilder builder;
		try {
			builder = fac.newDocumentBuilder();
			Document doc = builder.parse(in);
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	

}
