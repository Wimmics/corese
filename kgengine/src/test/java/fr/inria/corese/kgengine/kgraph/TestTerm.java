package fr.inria.corese.kgengine.kgraph;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;

public class TestTerm {
	
	
	public static void main(String[] args) throws EngineException{
		new TestTerm().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		
		QueryProcess.definePrefix("e",  
				"http://ns.inria.fr/edelweiss/2010/kgram/") ;
		
		QueryProcess exec = QueryProcess.create(graph);

		QueryLoad ql = QueryLoad.create();
		
		String query = "select * where {" +
				"[kg:father (?y [?p (?z)]) ]" +
				"}";
		
		query = "select * where {" +
				"[kg:and ( " +
					"[kg:father(?x [kg:cousin(?y)])] " +
					"[kg:not([kg:mother(?z)])] " +
				")] " +
		"}" ;
		
		query = ql.read(data + "kgraph/term.rq");
		//exec.setDebug(true);
		Mappings map = exec.query(query);
		
		query = "select * where {" +
				"[kg:grandFather(?x ?y)]" +
				"}";
				
		query = "select * where {" +
		"[kg:plus ([a kg:Atom] " +
				  "[kg:mult ([kg:cst ?y] " +
				  			"[kg:var ?z])])] ." +
		"}";
		
		

		
		query = "select * where {" +
				"[a kg:plus; " +
					"kg:arg1 [kg:var ?x] ;" +
					"kg:arg2 [a kg:mult ;" +
							  "kg:arg1 ?cst;" +
							  "kg:arg2 [kg:var ?y]" +
							"]" +
				"]" +
				"}";
		
		
	

		query = 
			"select * where {" +
			"[a e:And; e:args(" +
			"[a e:Father; " +
				"e:args(?x [a e:Father; e:args(?y ?z)])]" +
			"[a e:Mother; e:args(?a ?b)]" +
			")]" +
			"}";
		
		query = 
		"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
		"select * where {" +
		"[a e:Plus; e:term " +
			"([a e:Exp] " +
			 "[e:term " +
			 	"([a e:Constant; e:value ?y] " +
				 "[a e:Variable; e:name ?z])])] ." +
		"}";
		
		query = 
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			"[ a e:Exp ;" +
				"e:tree+ [a e:Constant; e:value ?v; e:unit 'euro'] ;" +
				"e:tree+ [a e:Variable; e:name ?x] ;" +
			"]" +
			"}";
		
		query = 
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			 "[" +
			 	"e:tree+ [a e:Constant; e:value ?val]" +
			 "]"+
			"}";
		
		query = 
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			"[e:term " +
				"([] " +
				 "[e:term " +
				 	"([e:value ?y] " +
					 "[e:name ?z])])] ." +
			"}";
		
		query = 
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			 "[" +
			 	"e:tree+/e:value ?val" +
			 "]"+
			"}";
		
		query = 			
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			"[a e:Triple; e:term([e:value ?s] [e:value ?p] [e:value ?o])] ." +
			"}" ;

		load.load(data + "kgraph/term.rdf");
		
		String query2 = 			
			"prefix e: <http://ns.inria.fr/edelweiss/2010/kgram/>" +
			"select * where {" +
			"[ e:term/rdf:rest* @([e:name ?var] ) ; " +
			"  e:term/rdf:rest* [rdf:first/e:name ?y; rdf:rest [rdf:first [e:value ?val]]] ]  " +
			"" +
			"}";
		
		
		map = exec.query(query2);
		
		System.out.println(map);
		System.out.println(map.size());
		System.out.println(map.getQuery().isCorrect());

		
	}
	
	
}
