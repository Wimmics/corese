//package test.kgraph;
//
//import java.util.Date;
//
//import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
//import fr.inria.acacia.corese.exceptions.EngineException;
//import fr.inria.edelweiss.kgenv.eval.QuerySolver;
//import fr.inria.corese.kgram.api.query.Producer;
//import fr.inria.corese.kgram.core.Mappings;
//import fr.inria.edelweiss.kgraph.core.Graph;
//import fr.inria.edelweiss.kgraph.logic.Entailment;
//import fr.inria.edelweiss.kgraph.query.MatcherImpl;
//import fr.inria.edelweiss.kgraph.query.QueryProcess;
//import fr.inria.edelweiss.kgraph.rule.RuleEngine;
//import fr.inria.edelweiss.kgraph.rule.RuleProducer;
//import fr.inria.edelweiss.kgtool.load.Load;
//import fr.inria.edelweiss.kgtool.load.RuleLoad;
//import fr.inria.edelweiss.kgtool.print.RDFFormat;
//
//public class EWok {
//
//	public static void main(String[] args) throws EngineException{
//		new EWok().process();
//	}
//
//	void process(){
//		String data = "/home/corby/workspace/corese/data/ewok/fulldata/";
//		
//
//		String root = "/home/corby/workspace/coreseV2/src/test/resources/data";
//
//		QuerySolver.defaultNamespaces(
//				
//		"c http://www.inria.fr/acacia/comma#");
//
//		DatatypeMap.setLiteralAsString(false);
//
//		Graph graph = Graph.create(!true);
//		graph.set(Entailment.RDFSRANGE, true);
//		graph.set(Entailment.RDFSSUBCLASSOF, !true);
//		graph.set(Entailment.RDFSSUBPROPERTYOF, !true);
//		Load loader =  Load.create(graph);
//
//		//loader.setDebug(true);
//		
//		long t1 = new Date().getTime();
//		loader.load(data + "/dateTimeData");
//		loader.load(data + "/geographicalData");
//		loader.load(data + "/geologicalData");
//		loader.load(data + "/model");
//
//		
//		Graph ng = Graph.create();
//		//Producer prod = RuleProducer.create(graph, ng);
//		//QueryProcess infer = QueryProcess.create(prod, MatcherImpl.create(graph));
//		//QueryProcess infer = QueryProcess.create(graph, ng);
//		QueryProcess infer = QueryProcess.create(graph, ng);
//		RuleEngine re = RuleEngine.create(infer);
//		RuleLoad rl = RuleLoad.create(re);
//		rl.load(data + "rule/rule.rul");
//		rl.load(data + "rule/meta.rul");
//		rl.load(data + "rule/meta2.rul");
////17.581s
//
//		long t2 = new Date().getTime();
//		System.out.println(graph.size());
//		System.out.println("Load: " + (t2-t1) / 1000.0 + "s");
//		
//		graph.init();
//
//		long t3 = new Date().getTime();
//		System.out.println("Index: " + (t3-t2) / 1000.0 + "s");
//		//System.out.println(graph.getIndex());
//		//System.out.println(graph);
//		
//		//re.setDebug(true);
//		re.process(ng);
//		long t4 = new Date().getTime();
//		System.out.println("Rule Engine: " + (t4-t3) / 1000.0 + "s");
//
//		//System.out.println(ng);
//
//		String query = "prefix geo: <http://rdf.insee.fr/geo/>" +
//		"select * where {" +
//		"?sub geo:dept 'Alpes-Maritimes'}" ;
//		
////QueryProcess exec = QueryProcess.create(ng, graph);
////Mappings map = exec.query(query);
////System.out.println( map.size());
//
//		query = "select debug * where {"+
//			//"{select distinct ?x  ?d where {"+
//			" {?p rdfs:domain ?d }" +
//			"?x ?p ?y " +
//			//"filter(?x = <http://www.owl-ontologies.com/geoTime.owl#Aalenian>)"+
//			//"}}"+
//			"not {?x rdf:type ?d}" +
//			//"graph ?g {?x rdf:type ?t}"+
//			"}";
//		
//		
//		
//		//System.out.println("** query");
//		//<http://www.owl-ontologies.com/geoTime.owl#Aalenian>
//		Mappings map;
//		try {
//			QueryProcess exec = QueryProcess.create(graph, ng);
//			//prod.setMode(0);
//			map = exec.query(query);
//
//			System.out.println(map.size());
//			long t5 = new Date().getTime();
//			System.out.println("Query: " + (t5-t4) / 1000.0 + "s");
//			} catch (EngineException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		
//		
//		
//		
//		
//		// load:
//		//42.946s
//		//11.324s
//		
//	}
//}
