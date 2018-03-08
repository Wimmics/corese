package fr.inria.corese.kgengine.kgraph;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.edge.EdgeImpl;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryEngine;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.print.XMLFormat;
import org.junit.Ignore;

@Ignore
public class TestPath {
	
@Test
	public void test0(){
		Graph graph = Graph.create();
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
		QueryProcess exec = QueryProcess.create(graph);
		
		QueryEngine qe = QueryEngine.create(graph);
		
		String cons = "construct {?x foaf:knows ?y} where {?x foaf:knows [foaf:knows ?y]}";
		
		qe.addQuery(cons);
		
		ProducerImpl p = (ProducerImpl) exec.getProducer();
		p.set(qe);
		
		EdgeImpl.displayGraph = false;
		
		String init = 
				"insert data {" +
				"<a> foaf:knows <b> " +
				"<b> foaf:knows <a> " +
				"<b> foaf:knows <c> " +
				//"<a> foaf:knows <c> " +
				"}";
		
		String query = 
				"select debug * where {" +
				"<a> foaf:knows+ ?t " +
				"}" ;
		
		
		try {
			exec.query(init);
			Mappings res = exec.query(query);
			System.out.println("res: \n" + res);
			assertEquals("Result", 1, res.size());
			

		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void test1(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create();
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"<John> foaf:knows <Jack> ; rdf:type foaf:Person " +
				"graph <g1> {<Jack> foaf:knows <Jim>} " +
				"<Jim> rdf:type foaf:Person " +
				"}" ;
		
		String query2 = "select debug * where {" +
				"?x foaf:knows @{?this rdf:type foaf:Person} + ?y " +
				"}";
		
		String query = "select debug * where {" +
		"?x foaf:knows  + ?y " +
		"{?x rdf:type foaf:Person" +
		"}";
		
		try {
			exec.query(update);
			Mappings map = exec.query(query);
			
			System.out.println(map);
			assertEquals("Result", 2, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
	}
	
	public void test2(){
		// select (group_concat(distinct ?x, ?y) as ?str)
		Graph g = Graph.create(true);
		QueryProcess.definePrefix("foaf", "http://xmlns.com/foaf/0.1/");
		QueryProcess exec = QueryProcess.create(g);

		String update = "insert data {" +
				"foaf:Person rdfs:subClassOf foaf:Living " +
				"foaf:Man rdfs:subClassOf foaf:Person " +
				"<Jim>   a foaf:Living " +
				"<James> a foaf:Person " +
				"<John>  a foaf:Man " +
				"<xx>    a foaf:Prout " +
				"}" ;
		
		String query = "select distinct ?x where {" +
				"?x a foaf:Person " +
				"filter(! strstarts(?x, rdf:))" +
				"}" +
				"pragma {" +
				"kg:match kg:mode 'relax' " +
				"kg:pragma kg:help true " +
				"graph kg:style {" +
				"[kg:reject(rdf: rdfs: owl:) ; kg:accept (foaf:)] " +
				"}" +
				"}";
		
	
		
		try {
			exec.query(update);
			Mappings map = exec.query(query);
			
			System.out.println(map);
			
			XMLFormat f = XMLFormat.create(map);
			
			System.out.println(f);
			assertEquals("Result", 2, map.size());			
			
		} catch (EngineException e) {
			assertEquals("Result", true, e);
		}
		
	}
	
	
	
	
	
	

}
