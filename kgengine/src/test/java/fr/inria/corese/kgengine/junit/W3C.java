package fr.inria.corese.kgengine.junit;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;


import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.QueryLoad;
import fr.inria.corese.kgtool.load.SPARQLResult;

public class W3C {
	
	final static String local = "/home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/sparql11-test-suite/";
	static final String www = "http://www.w3.org/2009/sparql/docs/tests/data-sparql11/";
	static final String data = www;
	
	@Test
	public void test(){
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		
		DatatypeMap.setSPARQLCompliant(true);
		exec.setSPARQLCompliant(true);
		exec.getEvaluator().setMode(Evaluator.SPARQL_MODE);

		Load load = Load.create(g);
		QueryLoad ql = QueryLoad.create();
		String q = ql.read(data + "functions/concat02.rq");
		System.out.println(q);
		SPARQLResult res = SPARQLResult.create(Graph.create());
		Mappings m = null;
		try {
			m = res.parse(data + "functions/concat02.srx");
			System.out.println("W3C Result: ");
			System.out.println(m);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			load.loadWE(data + "functions/data2.ttl");
		} catch (LoadException e) {
			e.printStackTrace();
		}
		
		String init = "insert data {" +
				"<http://test.fr/André> rdfs:comment 'Jérôme & \" test    '" +
				"}";
		
		g.init();
		
		
		String qq = "select * where {" +
				"?x ?p ?y }";
		
		try {
			
			System.out.println(g.display());
			exec.query(init);

			Mappings map = exec.query(q);
			
		
			System.out.println("KGRAM Result: ");

			System.out.println(map);
			
//			ResultFormat f = ResultFormat.create(map);
//			System.out.println(f);
			
//			MappingComparator test = MappingComparator.create();
//			test.validate(map, m);
			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
		System.out.println("français");
		System.out.println(System.getProperty("file.encoding"));
		
	}

}
