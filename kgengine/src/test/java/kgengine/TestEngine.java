package kgengine;

import java.util.Date;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.CoreseException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgtool.print.RDFFormat;

public class TestEngine {
	
    String DATA = "/home/corby/workspace/coreseV2/src/test/resources/data/";
    String PATH = "file://" + DATA + "comma/";

	
	//GraphEngine engine, engine2, server;
    
    IEngine engine, engine2;
    GraphEngine ge;
	
	public static void main(String[] args) throws EngineException, CoreseException{
		new TestEngine().process();
	}
	
	
	
	void process(){
		EngineFactory ef = new EngineFactory();
		engine = ef.newInstance();
		engine2 = ef.newInstance();
		ge = (GraphEngine) engine;
		
//		engine2 = GraphEngine.create();
//		engine2.definePrefix("c", "http://www.inria.fr/acacia/comma#");
//		
//		server = GraphEngine.create();
//		server.definePrefix("c", "http://www.inria.fr/acacia/comma#");
//		server.add(engine);
//		server.add(engine2);
		
		try {
			Date d1 = new Date();
//			engine.load(DATA + "comma/comma.rdfs");
//			engine.load(DATA + "comma/model.rdf");
//			engine.load(DATA + "comma/comma.rul");
//			engine.load(DATA + "comma/data");
//			engine.load(DATA + "comma/data2");

			// backward rules:
//			engine2.load(DATA + "engine/ontology/test.rdfs");
//			engine2.load(DATA + "engine/data/test.rdf");
//			engine2.load(DATA + "engine/rule/test2.brul");
//			engine2.load(DATA + "engine/rule/test2.rul");
//			engine2.load(DATA + "engine/rule/meta.brul");

			Date d2 = new Date();
			
			System.out.println("** Time: " + (d2.getTime() - d1.getTime())/ 1000.0 + "s");

			String query;
			IResults res;
			
//			query = "select * where {?x rdfs:label ?l} order by ?l limit 10";
//			res = engine.SPARQLQuery(query);
//			//System.out.println(res);
//			
//			query = "construct {?x rdfs:fake ?l} where {?x rdfs:label ?l} order by ?l limit 10";
//			res = engine.SPARQLQuery(query);
//			//System.out.println(res);
//			
//			query = "insert {?x rdfs:fake ?l} where {select * where {?x rdfs:label ?l} order by ?l limit 5} " ;
//					//"order by ?l " +
//					//"limit 10";
//			res = engine.SPARQLQuery(query);
//			
//			query = "select * where {?x rdfs:fake ?l} order by ?l limit 10";
//			res = engine.SPARQLQuery(query);
			//System.out.println(res);
			
			//engine2.runRuleEngine();

			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
					"select debug *  where {graph ?g {?x ?p ?v} filter(?p ^ c:)} group by ?x";
			
			
			
			d1 = new Date();
			
			
			//res = engine2.SPARQLQuery(query);

		
			d2 = new Date();
			
			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
					"select debug * where {" +
					"graph ?g {?x c:FirstName ?n} " +
					"}" ;

			//res = engine.SPARQLQuery(query);
			
			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
					"delete {?x ?p ?y}" +
					"where {?x c:FirstName ?n filter(?n in ('Olivier', 'Alain'))" +
					"?x ?p ?y}" ;
			
			//res = engine.SPARQLQuery(query);

			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
			"select debug * where {" +
			"graph ?g {?x c:FirstName ?n} " +
			"}" ;
			
			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
			"select debug * where {" +
			"graph ?g {  <http://www.ii.atos-group.com/sophia/comma/HomePage.htm> c:Include* ?org } " +
			"}" ;
			
			query = 
				"construct{?x c:FirstName 'Olivier'} " +
				"where {" +
				" ?x c:FirstName 'Olivier'^^xsd:string " +
				"} " ;
			
			
			query = "prefix c: <http://www.inria.fr/acacia/comma#> " +
			"select  * (count(?x) as ?c) where {" +
			" graph ?g {?x c:FirstName ?name} " +
			"filter(?name = 'Laurent')" +
			"?x c:isMemberOf ?org" +
			"} group by ?org" ;
			
			query = "select * where {" +
					"graph ?g {?x c:FirstName ?y}" +
					"}";
			
			
			
			
			
			QueryExec exec = QueryExec.create(engine);
			exec.definePrefix("c", "http://www.inria.fr/acacia/comma#");
			exec.definePrefix("path", PATH);
			//exec.setListGroup(true);
			//exec.setDebug(true);
			
			System.out.println(1);
			
			String update = 
				"load path:comma.rdfs; " +
				"load path:model.rdf; " +
				"load path:comma.rul; " +
				"load path:data";
			
			res = exec.SPARQLQuery(update);
			
			//res = exec.SPARQLQuery("select where {} pragma {kg:kgram kg:entail false}");

			
			//ge.getGraph().setEntailment(false);
			
			System.out.println(2);
			String delete = //"delete where {graph kg:entail {?x ?p ?y}};" +
				"drop graph kg:entailment; " +
				"delete where {?p rdfs:range ?r ?q rdfs:domain ?d}" ;
				;
			
			res = exec.SPARQLQuery(delete);
			
			query = "select distinct ?p " +
					"from kg:entailment " +
					"where { ?x ?p ?y ?z ?q ?t filter(?y = ?z)}";
			
			
			

			System.out.println(3);
			res = exec.SPARQLQuery(query);

			System.out.println(res);
			
			
			res = exec.SPARQLQuery("create graph kg:entailment;");
			res = exec.SPARQLQuery(query);

			System.out.println(res);
			
			

//			QueryResults qr = (QueryResults) res;
//			
//			RDFFormat f = RDFFormat.create(qr.getNSM());
//			for (Mapping map : qr.getMappings()){
//				f.add(map);
//			}
//
//			for (IResult rr : res){
//				for (String var : res.getVariables()){
//					for (IResultValue val : rr.getResultValues(var)){
//						System.out.println(var + " = " + val.getDatatypeValue());
//					}
//				}
//			}
			
			System.out.println("** Time: " + (d2.getTime() - d1.getTime())/ 1000.0 + "s");
		
			
//			query = 
//				"prefix c: <http://www.inria.fr/acacia/comma#>" +
//				"select * where { " +
//				"c:Anne c:hasSister ?y " +
//				//"?y c:hasGrandParent ?z" +
//				"}";
//			
//			res = engine.SPARQLProve(query);
//			System.out.println(res);
//			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}
	

}
