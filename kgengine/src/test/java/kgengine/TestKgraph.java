package kgengine;


import java.util.Date;


import fr.inria.acacia.corese.api.*;



import fr.inria.acacia.corese.exceptions.*;

import fr.inria.edelweiss.engine.core.Engine;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;


public class TestKgraph {
	
	public static void main(String[] args) throws EngineException {

		new TestKgraph().process();
	}

	void process() throws EngineException {

		String data = "/user/corby/home/workspace/coreseV2/src/test/resources/data/";

	
		
		QuerySolver.defaultNamespaces(
				"q http://www.inria.fr/edelweiss/2008/query# "
						+ "c http://www.inria.fr/acacia/comma#");
		
		

		GraphEngine ge = GraphEngine.create();
		
		
		ge.load(data + "engine/ontology/test.rdfs");
		ge.load(data + "engine/data/test.rdf");
//		server.load(data + "engine/rule/test2.brul");
//		server.load(data + "engine/rule/meta.brul");


		
		
		String query;
			 
		 query = 
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select     * where {" +
				"?x c:hasFather c:Marc c:Marc c:hasBrotherSister ?y " +
				"}";
		 
		 query = 
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select     * where {" +
				" scope{?x c:hasFather ?y}   ?y  c:hasBrotherSister ?z " +
				"}";
		 
		 query = 
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select     * where {" +
				"?x c:hasGrandParent c:Pierre ?x c:hasID ?id " +
				"}";
		 
		 query = 
				"prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select * where { " +
				"?x c:hasSister ?y " +
				//"?y c:hasGrandParent ?z" +
				"}";

		// engine.SPARQLQuery(query);
		 
		// engine.addEventListener(RuleListener.create());
		
		 Date d1 = new Date();
		 IResults res = null;

		 
		 
		 ge.load(data + "engine/rule/test2.brul");
		 ge.load(data + "engine/rule/meta.brul");
		 
		 //for (int i=0; i<10; i++)
		 res = ge.SPARQLProve(query);
		 
		 System.out.println(res);
		 Date d2 = new Date();

		// System.out.println(Time.spent(d1, d2));
	}
	
	
}
		
		
		
		
		
		
		