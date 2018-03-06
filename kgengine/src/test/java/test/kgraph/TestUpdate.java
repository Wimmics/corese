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
//import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
//import fr.inria.acacia.corese.exceptions.EngineException;
//import fr.inria.acacia.corese.triple.parser.ASTQuery;
//import fr.inria.acacia.corese.triple.parser.NSManager;
//import fr.inria.acacia.corese.triple.update.ASTUpdate;
//import fr.inria.acacia.corese.triple.update.Basic;
//import fr.inria.acacia.corese.triple.update.Composite;
//import fr.inria.acacia.corese.triple.update.Update;
//import fr.inria.edelweiss.kgenv.eval.QuerySolver;
//import fr.inria.corese.kgram.api.core.Node;
//import fr.inria.corese.kgram.api.query.Matcher;
//import fr.inria.corese.kgram.core.Mapping;
//import fr.inria.corese.kgram.core.Mappings;
//import fr.inria.corese.kgram.core.Query;
//import fr.inria.corese.kgram.event.EvalListener;
//import fr.inria.edelweiss.kgraph.core.Graph;
//import fr.inria.edelweiss.kgraph.logic.Entailment;
//import fr.inria.edelweiss.kgraph.query.MatcherImpl;
//import fr.inria.edelweiss.kgraph.query.ProducerImpl;
//import fr.inria.edelweiss.kgraph.query.QueryProcess;
//import fr.inria.edelweiss.kgtool.load.Load;
//import fr.inria.edelweiss.kgtool.print.JSONFormat;
//import fr.inria.edelweiss.kgtool.print.RDFFormat;
//import fr.inria.edelweiss.kgtool.print.XMLFormat;
//import fr.inria.edelweiss.kgtool.print.XSLTQuery;
//
//public class TestUpdate {
//	
//	public static void main(String[] args){
//		new TestUpdate().process();
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
//		//loader.load(data + "comma/comma.rdfs");
////		loader.load(data + "comma/commatest.rdfs");
////		loader.load(data + "comma/comma.rdfs");
////		loader.load(data + "comma/testrdf.rdf");
////		loader.load(data + "comma/model.rdf");
////		loader.load(data + "comma/data");
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
//	query = 
//		"with <test>" +
//		"delete {?x c:LastName ?l}" +
//		"insert {?x c:name ?n}" +
//		"using <g1>" +
//		"using named <g2>" +
//		"where {" +
//		"?x c:FirstName ?n" +
//		"}";
//
//	query = "insert data {<a> c:name 'John'};" +
//			"delete data {<a> c:name 'James'}";
//	
//	String update = 
//		"base <file://" + data + ">" +
//		"load <comma/comma.rdfs> into <http://www.test.fr/g1>;" +
////			"load <test> into graph <graph>;" +
////			"clear graph <uri>;" +
////			"clear default;" +
////			"clear named;" +
////			"clear silent all;" +
////			"create graph <uri>;" +
////			"add graph <uri> to default" +
//			"";
//	update = 
//		"insert data {" +
//		"graph <g1> {" +
//			"<John> c:FirstName 'John' ; c:FamilyName 'McLaughlin' }" +
//		"graph <g2> {" +
//			"<John> c:FirstName 'John' ; c:FamilyName 'McLaughlin' }"	+
//		"graph <g3> {" +
//			"<John> c:FirstName 'John' ; c:FamilyName 'McLaughlin' }"	+		
//		"}";
//	
//	String update2 = 
//		//"with <test> " +
//		"with <http://ns.inria.fr/edelweiss/2010/kgraph#default> " +
//		"insert {?x c:name ?n} " +
////			"using <http://ns.inria.fr/edelweiss/2010/kgraph#default> " +
//			"using named <http://ns.inria.fr/edelweiss/2010/kgraph#default> " +
//			"where {" +
//			"graph ?g {" +
//			"?x c:FirstName ?n" +
//			"}" +
//			"}";
//	
//	String delete = 
//		"delete data {<John> c:name 'John'}";
//	
//	String delete2 = 
//		"delete  {?x c:name ?n} " +
//		"insert  {?x c:fname ?n} " +
//		"where {?x c:FirstName ?n} ";
//	
//	 delete2 = 
//		"delete   " +
//		"where {?x c:FirstName ?n} ";
//	
//	query = "select * where {" +
//			"graph ?g {" +
//			"?x ?p ?l" +
//			"}" +
//			"} limit 10";
//	
//	delete = "clear graph <g1>; clear graph <g3>";
//	
//	update = 
//		"base <file://" + data + ">" +
//		"load <comma/comma.rdfs> into graph <http://www.test.fr/g1>;" ;
//	
//	
//	
//	String conc = "select * where {?x ?p ?y}";
//	
//	query = "select  * where {" +
//	"graph ?g {?x rdfs:label 'engineer'@en}" +
//	"}";
//	
//	update2 = "copy graph <http://www.test.fr/g1> to graph <http://www.test.fr/g1>";
//	
//	
//	update = "insert data {<John> <name> 'John' <Jack> <name> 'Jack'}";
//	
//	query = "select * where { {?x <name>{0} ?y}}";
//	
//	update2 = "delete data {<Jack> <name> 'Jack'}";
//	
//	query = "select * where {{?x <name>{0} ?y}}";
//
//
//try {
//	QueryProcess exec = QueryProcess.create(graph);
//	exec.setDebug(true);
//	ProducerImpl p = (ProducerImpl) exec.getProducer();
//	exec.setLoader(Load.create(graph));
//
//	t1 = new Date().getTime();
//	Mappings lMap;
//	exec.query(update);
//	exec.query(query);
//	exec.query(update2);
//	lMap = exec.query(query);
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
