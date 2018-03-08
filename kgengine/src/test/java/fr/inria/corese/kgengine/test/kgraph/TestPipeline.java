package fr.inria.corese.kgengine.test.kgraph;

//package test.kgraph;
//
//import java.io.StringWriter;
//import java.util.Date;
//
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMResult;
//import javax.xml.transform.stream.StreamResult;
//import javax.xml.transform.stream.StreamSource;
//
//
//
//import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
//import fr.inria.corese.sparql.exceptions.EngineException;
//import fr.inria.corese.sparql.triple.parser.NSManager;
//import fr.inria.corese.kgenv.eval.QuerySolver;
//import fr.inria.corese.kgram.api.core.Node;
//import fr.inria.corese.kgram.api.query.Matcher;
//import fr.inria.corese.kgram.core.Mapping;
//import fr.inria.corese.kgram.core.Mappings;
//import fr.inria.corese.kgram.core.Query;
//import fr.inria.corese.kgraph.core.Graph;
//import fr.inria.corese.kgraph.logic.Entailment;
//import fr.inria.corese.kgraph.query.MatcherImpl;
//import fr.inria.corese.kgraph.query.ProducerImpl;
//import fr.inria.corese.kgraph.query.QueryProcess;
//import fr.inria.corese.kgtool.load.Load;
//import fr.inria.corese.kgtool.print.JSONFormat;
//import fr.inria.corese.kgtool.print.RDFFormat;
//import fr.inria.corese.kgtool.print.XMLFormat;
//import fr.inria.corese.kgtool.print.XSLTQuery;
//
//public class TestPipeline {
//	
//	public static void main(String[] args){
//		new TestPipeline().process();
//	}
//	
//	void process(){
//		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
//		String file = "file://" + data + "test.xml";
//		
//		String path = "file:///home/corby/workspace/coreseV2/src/test/resources/data";
//		
//		String ns = "data "  + path + "/comma/ " +
//		"data2 " + path + "/comma/data2/  " +
//		"data1 " + path + "/comma/data/  " +
//		"c http://www.inria.fr/acacia/comma#";
//		
//		QuerySolver.defaultNamespaces(ns);
//		
//		DatatypeMap.setLiteralAsString(false);
//
//		Graph graph = Graph.create(true);
//		graph.set(Entailment.RDFSRANGE, true);
//		ProducerImpl producer =  ProducerImpl.create(graph);
//		Load loader =  Load.create(graph);
//		
//		long t1 = new Date().getTime();
////		loader.load(data + "kgraph/rdf.rdf", Entailment.RDF);
////		loader.load(data + "kgraph/rdfs.rdf", Entailment.RDFS);
//		//loader.load("http://www.w3.org/2000/01/rdf-schema#");
////		loader.load(data + "meta.rdfs");
//		loader.load(data + "comma/comma.rdfs");
//		loader.load(data + "comma/commatest.rdfs");
//		loader.load(data + "comma/comma.rdfs");
//		loader.load(data + "comma/testrdf.rdf");
//		loader.load(data + "comma/model.rdf");
//		loader.load(data + "comma/data");
////		loader.load(data + "comma/data2");
//
//		long t2 = new Date().getTime();
//		System.out.println(graph);
//		System.out.println((t2-t1) / 1000.0 + "s");
//		
//		t1 = new Date().getTime();
//		graph.init();
//		t2 = new Date().getTime();
//		System.out.println(graph);
//		System.out.println((t2-t1) / 1000.0 + "s");
//		
//		
//		/**
//		 * parse  : 2.85
//		 * create : 1.62
//		 * entail : 2.12
//		 */
//
//
//		
//		String query;
//	
//
//query = "select * where {" +
//		"?x rdf:type c:Person" +
//		"}";
//
//String query1 = "select * where {" +
//"?x rdf:type c:OrganizationalEntity" +
//"}";
//
//String query2 = "select * where {" +
//"?x c:hasCreated ?doc" +
//"}";
//
//
//String query3 = "select  * where {" +
//"?x c:isMemberOf ?org" +
//"}";
//
//String query4 = "select  * where {" +
//"?doc rdf:type c:Document filter(?doc ~ 'ework')" +
//"}";
//
//
//
//
//try {
//	QueryProcess exec = QueryProcess.create(graph);
//	//exec.setListGroup(true);
////	exec.add(ponto);
//	
//	Query q0 = exec.compile(query);
//	Query q1 = exec.compile(query1);
//	Query q2 = exec.compile(query2);
//	Query q3 = exec.compile(query3);
//	Query q4 = exec.compile(query4);
//
//	Query q = q0.union(q1).and(q2).optional(q3).minus(q4).orderBy("?x");
//	
//	q.setDebug(true);
//	
//
//	//exec.addEventListener(EvalListener.create());
//	t1 = new Date().getTime();
//	Mappings lMap = exec.query(q);
//	
//	lMap = exec.query(q0).union(exec.query(q1)).and(exec.query(q2)).optional(exec.query(q3)).minus(exec.query(q4));
//	
//	
////	System.out.println(XMLFormat.create(lMap));
//	System.out.println(lMap);
//	System.out.println(lMap.size());
//	t2 = new Date().getTime();
//	System.out.println(lMap.size() + " " + (t2-t1) / 1000.0 + "s");
//	
//
//} catch (EngineException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//	}
//	
//	
//	
//	void trace(Graph graph){
//		System.out.println(graph);
////		graph.init();
//		//System.out.println(graph.getIndex());
//		int n = 0;
////		for (Entity ent : graph.getIndex().get(graph.getNode(RDF.RDFTYPE))){
////			System.out.println(ent);
////			if (n++>50) break;
////		}
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//	
//}
