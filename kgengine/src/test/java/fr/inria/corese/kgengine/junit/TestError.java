package fr.inria.corese.kgengine.junit;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;

public class TestError  {
				
//		static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
                static String data = TestError.class.getClassLoader().getResource("data").getPath()+"/";

		static Graph graph;
		
		@BeforeClass
		static public void init(){
			QueryProcess.definePrefix("c", "http://www.inria.fr/acacia/comma#");
			graph = Graph.create(true);
			Load ld = Load.create(graph);
			init(graph, ld);
		}
		
		static  void init(Graph g, Load ld){
			ld.load(data + "comma/comma.rdfs");
			ld.load(data + "comma/model.rdf");
			ld.load(data + "comma/data");
		}
		
		
		Graph getGraph(){
			return graph;
		}
		
		
		@ Test
		public void test1(){
			String query = "select (min(?x) as ?y) where {" +
					"?x rdfs:seeAlso ?y" +
					"} group by ?z";
			QueryProcess exec = QueryProcess.create(graph);
			try {				
				Mappings map = exec.query(query);
				assertEquals("Result", true, true);
			} catch (EngineException e) {
				assertEquals("Result", true, e);
			}
		}
		
		@ Test
		public void test2(){
			String query = "select (self(?x) as ?y) (min(?x) as ?x) where {" +
					"filter(?t = ?z)" +
					"} ";
			QueryProcess exec = QueryProcess.create(graph);
			try {				
				Mappings map = exec.query(query);
				assertEquals("Result", true, true);
			} catch (EngineException e) {
				assertEquals("Result", true, e);
			}
		}
		
		
		@Test
		public void test3(){
			Graph g = Graph.create();
			QueryProcess exec = QueryProcess.create(g);

			String update = "insert data    {<John> <date> '2011'^^my:gYear}";
			String query  = "select * where {<John> <date> '2011'^^my:gYear}";
			String query2  = "select * where {<John> <date> ?d filter(?d != '2012'^^my:gYear)}";

			try {				
				exec.query(update);
				Mappings map = exec.query(query);
				assertEquals("Result", 1, map.size());
				map = exec.query(query2);
				assertEquals("Result", 0, map.size());
				
			} catch (EngineException e) {
				assertEquals("Result", true, e);
			}						
		}
		
		
		
		@ Test
		public void test4(){
			String query = 
				"select " +
					"(count(distinct self(?d)) as ?c1) " +
					"(count(distinct ?d) as ?c2) " +
					"where {" +
					"?p rdfs:domain ?d" +
					"} ";
			QueryProcess exec = QueryProcess.create(graph);
			try {				
				Mappings map = exec.query(query);
				IDatatype dt1 = getValue(map, "?c1");
				IDatatype dt2 = getValue(map, "?c2");
				assertEquals("Result", 29, dt1.getIntegerValue());
				assertEquals("Result", 29, dt2.getIntegerValue());
			} catch (EngineException e) {
				assertEquals("Result", true, e);
			}
		}
		
		
		
		@ Test
		public void test5(){
			String query = 
				"select " +
					"(count( self(?d)) as ?c) " +
					"where {" +
					"} ";
			QueryProcess exec = QueryProcess.create(graph);
			try {				
				Mappings map = exec.query(query);
				IDatatype dt1 = getValue(map, "?c");
				assertEquals("Result", 0, dt1.getIntegerValue());
			} catch (EngineException e) {
				assertEquals("Result", true, e);
			}
		}
		
		
		
		
		IDatatype getValue(Mapping map, String name){
			return datatype(map.getValue(name));
		}
		
		
		IDatatype getValue(Mappings map, String name){
			Object value = map.getValue(name);
			if (value == null) return null;
			return datatype(value);
		}
		
		IDatatype datatype(Object n){
			return (IDatatype) n;
		}
		
		IDatatype datatype(Node n){
			return (IDatatype) n.getValue();
		}
		

}
