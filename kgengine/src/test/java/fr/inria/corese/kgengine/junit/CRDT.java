package fr.inria.corese.kgengine.junit;


import org.junit.Test;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.Entailment;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;

/**
 * 
 * Draft test for CSet CRDT
 * Triples are tagged with unique ID
 * Several corese with each a graph listener
 * Listener broadcast updates to other corese
 * 
 * @author Luis Ibanez & Pascal Molli & Olivier Corby 
 *
 */
public class CRDT {

	// change the path to the location of the data:
	static String data = "/home/corby/workspace/kgengine/src/test/resources/data/crdt/";
	//static String data = CRDT.class.getClassLoader().getResource("data/crdt").getPath()+"/";
	
	/**
	 * 1) INSERT DATA (ground triples)
	 */
    public void testCRDT1(){			

		Graph g1 = Graph.create();	
		g1.init();

		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
				
		Load ld = Load.create(g1);
		ld.load(data + "ex1.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		exec1.setDetail(true);
		String q1 = QueryLoad.create().read(data + "ex1.ru");
		
		try {
			Mappings map = exec1.query(q1);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    
    
    
    /**
	 * 1) INSERT DATA (ground triples) && copy
	 */
    @Test
    public void testCRDTcopy(){			

		Graph g1 = Graph.create();	
		g1.init();

		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
				
		Load ld = Load.create(g1);
		ld.load(data + "ex1.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		exec1.setDetail(true);
		String q1 = QueryLoad.create().read(data + "ex1.ru");
		String q2 = QueryLoad.create().read(data + "copy.ru");

		try {
			Mappings map = exec1.query(q1);
                        map = exec1.query(q2);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    
    
    
    
    
    
    
    
    
    
    
    

	
	/**
	 * 1.1) INSERT DATA (Already present ground triples)
	 * 
	 */
	public void testCRDT2(){			

		Graph g1 = Graph.create();
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
				

		
		// listen and broadcast
		g1.addListener(gl1);
		
		Load ld = Load.create(g1);
		ld.load(data + "ex2.ttl", Entailment.DEFAULT);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = QueryLoad.create().read(data + "ex1.ru");
		
		try {
			exec1.query(q1);
			System.out.println(g1.display());
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	/**
	 * 2) DELETE DATA (ground triples)

	 */
	public void testCRDT3(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex3.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		//exec1.setDebug(true);
                exec1.setDetail(true);
		String q1 = QueryLoad.create().read(data + "ex3.ru");
		
		try {
			Mappings m = exec1.query(q1);
                        System.out.println("delete: " );
                         System.out.println(m.nbDelete());
                       System.out.println(m.getDelete());
			
			String query = "select * where {?x ?p ?y}";
			Mappings map= exec1.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

	}
	
	
	
	/**
	 * 2.1) DELETE DATA (non-existent triples)
	 */

	public void testCRDT4(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex4.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		exec1.setDetail(true);
		String q1 = QueryLoad.create().read(data + "ex3.ru");
		
		try {
			Mappings m = exec1.query(q1);
			
			String query = "select * where {?x ?p ?y}";
			Mappings map= exec1.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 3) DELETE-INSERT

	 * Dans votre exemple il y a des triplets différents avec le même ID ???
	 */

	public void testCRDT5(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex5.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = QueryLoad.create().read(data + "ex5.ru");
		
		try {
			exec1.query(q1);
			g1.setTag(false);
			String query = "select * where {tuple(?p ?x ?y ?v)}";
			Mappings map= exec1.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 3.1 Delete
	 */

	public void testCRDT6(){			

		Graph g1 = Graph.create();	
		g1.init();
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex6.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = QueryLoad.create().read(data + "ex6.ru");
		
		try {
			String query = "select * where {tuple(?p ?x ?y ?v)}";
			Mappings map= exec1.query(query);
			System.out.println(map);
			System.out.println(map.size());

			
			exec1.query(q1);
			
			g1.setTag(false);
			map= exec1.query(query);
			System.out.println(map);
			System.out.println(map.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	
	
	/**
	 * 3.2) INSERT 
	 * erreur de namespace foaf:
	 * triplets différents avec même ID
	 */
	public void testCRDT7(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex7.ttl");
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = QueryLoad.create().read(data + "ex7.ru");
		
		try {
			String query = "select * where {tuple(?p ?x ?y ?v)}";
			Mappings map= exec1.query(query);
			System.out.println(map);

			
			exec1.query(q1);
			
			g1.setTag(false);
			map= exec1.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 4) Load
	 * Votre broadcast est erroné (il manque Taft)
	 */
	public void testCRDT8(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex8.ttl", Entailment.DEFAULT);
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = QueryLoad.create().read(data + "ex7.ru");
		
		try {
			ld.load(data + "ex8load.ttl", Entailment.DEFAULT);
			
			String query = "select * where {tuple(?p ?x ?y ?v)}";
			// just for query, to get same triples with different tags
			g1.setTag(false);
			Mappings map= exec1.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	
	/**
	 * 5) clear
	 */
	public void testCRDT9(){			

		Graph g1 = Graph.create();	
		g1.init();
		
		// Listener 
		GListener gl1 = GListener.create();
						
		// Listener is also a tagger (in this implementation, to simplify the code)
		g1.setTagger(gl1);
	
		Load ld = Load.create(g1);
		ld.load(data + "ex8.ttl", Entailment.DEFAULT);
		
		// listen and broadcast
		g1.addListener(gl1);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		
		String q1 = "clear graph <" + Entailment.DEFAULT +">";
		q1 = "clear default";
		
		try {
			exec1.query(q1);
			
			String query = "select * where {?x ?p ?y}";
			Mappings map= exec1.query(query);
			System.out.println(map);
			System.out.println(map.size());

		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/***************************************
	 * 
	 * Concurrent operations
	 * 
	 ***************************************/
	
	/**
	 * Two inserts
	 */
	public void testCRDT10(){			

		Graph g1 = Graph.create();	
		Graph g2 = Graph.create();	
		Graph g0 = Graph.create();	

		g1.init();
		g2.init();
		g0.init();

		GListener gl1 = GListener.create();
		GListener gl2 = GListener.create();
		GListener gl0 = GListener.create();
		
		// to tag triples
		g1.setTagger(gl1);
		g2.setTagger(gl2);
		g0.setTagger(gl0);
		
		
		Load ld = Load.create(g0);
		ld.load(data + "ex10.ttl");
		

		// to listen and broadcast
		g1.addListener(gl1);
		g2.addListener(gl2);
		g0.addListener(gl0);
		

		
		// broadcast to other peers
		gl1.addTarget(g2); gl1.addTarget(g0);
		gl2.addTarget(g1); gl2.addTarget(g0);
		gl0.addTarget(g1); gl0.addTarget(g2);

		QueryProcess exec1 = QueryProcess.create(g1);
		QueryProcess exec2 = QueryProcess.create(g2);
		QueryProcess exec0 = QueryProcess.create(g0);
		

		QueryLoad ql = QueryLoad.create();
		String q1 = ql.read(data + "ex10-1.ru");
		String q2 = ql.read(data + "ex10-2.ru");

		try {
			exec1.query(q1);
			exec2.query(q2);
			
			String query = "select * where {tuple(?p ?x ?y ?v)}";

			g0.setTag(false);
			Mappings map = exec0.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	/**
	 * Two deletes
	 */

	public void testCRDT11(){			

		Graph g1 = Graph.create();	
		Graph g2 = Graph.create();	
		Graph g0 = Graph.create();	

		g1.init();
		g2.init();
		g0.init();

		GListener gl1 = GListener.create();
		GListener gl2 = GListener.create();
		GListener gl0 = GListener.create();
		
		// to tag triples
		g1.setTagger(gl1);
		g2.setTagger(gl2);
		g0.setTagger(gl0);
		
		
		// no broadcast for g0 (cf data before)
		Load ld = Load.create(g0);
		ld.load(data + "ex10.ttl", Entailment.DEFAULT);
		

		// to listen and broadcast
		g2.addListener(gl2);
		
		
		// broadcast to other peers
		gl2.addTarget(g1); gl2.addTarget(g0);
		
		
		// g2 load data and broadcast to g0 and g1
		Load ld2 = Load.create(g2);
		ld2.load(data + "ex10.ttl", Entailment.DEFAULT);
				
		g1.addListener(gl1);
		gl1.addTarget(g0);
		
		Load ld1 = Load.create(g1);
		ld1.load(data + "ex10.ttl", Entailment.DEFAULT);
				
		System.out.println("graph: \n" + g0.display());
						
		g0.addListener(gl0);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		QueryProcess exec2 = QueryProcess.create(g2);
		QueryProcess exec0 = QueryProcess.create(g0);
		
		QueryLoad ql = QueryLoad.create();
		String q1 = ql.read(data + "ex11.ru");

		try {
			g1.setTag(false);
			g2.setTag(false);
			exec1.query(q1);
			exec2.query(q1);
			
			String query = "select * where { tuple(?p ?x ?y ?v) }";

			g0.setTag(false);
			Mappings map = exec0.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	/**
	 * 3) Insert and Delete
	 * The example is  wrong, I do no not run it
	 */
	public void testCRDT12(){			

	}
	
	
	
	
	
	
	/**
	 * 4) Insert and Delete/Insert
	 */

	public void testCRDT13(){			

		Graph g1 = Graph.create();	
		Graph g2 = Graph.create();	
		Graph g0 = Graph.create();	

		g1.init();
		g2.init();
		g0.init();

		GListener gl1 = GListener.create();
		GListener gl2 = GListener.create();
		GListener gl0 = GListener.create();
		
		// to tag triples
		g1.setTagger(gl1);
		g2.setTagger(gl2);
		g0.setTagger(gl0);
		
		
		// no broadcast for g0 (cf data before)
		Load ld = Load.create(g0);
		ld.load(data + "ex10.ttl", Entailment.DEFAULT);
				
		
		// to listen and broadcast
		// g2 load data and broadcast to g0 
		g2.addListener(gl2);

		// broadcast to g0
		gl2.addTarget(g0);

		Load ld2 = Load.create(g2);
		ld2.load(data + "ex10.ttl", Entailment.DEFAULT);

				
		
		
		g1.addListener(gl1);
		gl1.addTarget(g0);
		
		
				
		System.out.println("graph: \n" + g0.display());
						
		g0.addListener(gl0);
		
		QueryProcess exec1 = QueryProcess.create(g1);
		QueryProcess exec2 = QueryProcess.create(g2);
		QueryProcess exec0 = QueryProcess.create(g0);
		
		QueryLoad ql = QueryLoad.create();
		String q1 = ql.read(data + "ex13-2.ru");
		String q2 = ql.read(data + "ex13.ru");

		try {
			
			exec1.query(q1);
			exec2.query(q2);
			
			String query = "select * where { tuple(?p ?x ?y ?v) }";

			g0.setTag(false);
			Mappings map = exec0.query(query);
			System.out.println(map);
			
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Design Pattern with 3 corese peers
	 */
	public void testCRDT(){			

		Graph g1 = Graph.create();	
		Graph g2 = Graph.create();	
		Graph g0 = Graph.create();	

		g1.init();
		g2.init();
		g0.init();

		GListener gl1 = GListener.create();
		GListener gl2 = GListener.create();
		GListener gl3 = GListener.create();

		// to listen and broadcast
		g1.addListener(gl1);
		g2.addListener(gl2);
		g0.addListener(gl3);
		
		// to tag triples
		g1.setTagger(gl1);
		g2.setTagger(gl2);
		g0.setTagger(gl3);
		
		
		// broadcast to other peers
		gl1.addTarget(g2); gl1.addTarget(g0);
		gl2.addTarget(g1); gl2.addTarget(g0);
		gl3.addTarget(g1); gl3.addTarget(g2);

		QueryProcess exec1 = QueryProcess.create(g1);
		QueryProcess exec2 = QueryProcess.create(g2);
		QueryProcess exec3 = QueryProcess.create(g0);
		
		Load ld = Load.create(g1);

		

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	




}
