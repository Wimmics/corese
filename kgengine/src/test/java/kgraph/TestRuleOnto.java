package kgraph;

import java.util.Date;


import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.logic.RDF;
import fr.inria.corese.kgraph.logic.RDFS;
import fr.inria.corese.kgraph.query.MatcherImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgraph.rule.RuleProducer;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.RuleLoad;

public class TestRuleOnto {

	public static void main(String[] args){
		for (int i=0; i<5; i++)
		new TestRuleOnto().process();
	}

	void process(){
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String file = "file://" + data + "test.xml";

		String path = "file:///home/corby/workspace/coreseV2/src/test/resources/data";

		QuerySolver.defaultNamespaces(
				"data "  + path + "/comma/ " +
				"data2 " + path + "/comma/data2/  " +
				"data1 " + path + "/comma/data/  " +
		"c http://www.inria.fr/acacia/comma#");

		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(!true);
		graph.set(RDFS.RANGE, true);
		graph.set(RDFS.SUBCLASSOF, !true);
		graph.set(RDFS.SUBPROPERTYOF, !true);
		Load loader =  Load.create(graph);

		long t1 = new Date().getTime();
		loader.load(data + "kgraph/rdf.rdf", RDF.RDF);
		loader.load(data + "kgraph/rdfs.rdf", RDFS.RDFS);
		//		loader.load(data + "meta.rdfs");
		//loader.load(data + "comma/commatest.rdfs");
		loader.load(data + "comma/testrdf.rdf");
		loader.load(data + "comma/model.rdf");
		loader.load(data + "comma/data");
		loader.load(data + "comma/data2");
		
		loader.load(data + "kgraph/comma.rdfs");

		Graph onto = Graph.create();
		Load ld =  Load.create(onto);
		ld.load(data + "comma/comma.rdfs");


		//loader.load(data + "tmp2.rdf");


		//		for (int i=2; i<=10; i++){
		//			System.out.println("** Load: " + i);
		//			loader.load(data + "comma/comma" + i);
		//		}


		//System.out.println(graph.getIndex());

		//System.out.println(graph.getInference().display());		
		long t2 = new Date().getTime();
		System.out.println(graph);
		System.out.println((t2-t1) / 1000.0 + "s");

		//for (int i=0; i<5; i++)
		{
		// apply rules in a new graph:
			t1 = new Date().getTime();
			
		/**
		 * Rule engine with RDFS ontology and RDF graph
		 * process rdfs entailment
		 */
		Graph ng = Graph.create();
		//RuleEngine re = RuleEngine.create(QueryProcess.create(graph, ng));
		Producer prod = RuleProducer.create(graph, ng);
		RuleEngine re = RuleEngine.create(QueryProcess.create(prod, MatcherImpl.create(graph)));

		RuleLoad rl = RuleLoad.create(re);
		rl.load(data + "kgraph/meta.rul");
		rl.load(data + "kgraph/meta2.rul");
		int count = re.process(ng);
		
		
		
		t2 = new Date().getTime();
		System.out.println("** Rules: " + count + " " + ng.size());
		//System.out.println(ng.getIndex());
		System.out.println((t2-t1) / 1000.0 + "s");
		
		String query = 
			"select  * where {" +
			"?x c:FirstName 'Olivier' " +
			"?x rdf:type/rdfs:subClassOf* ?c " +
			"} limit 50";
		
		QueryProcess exec = QueryProcess.create(graph);
		//exec.add(onto);
		exec.add(ng);
		try {
			Mappings map = null;
			//for (int i=0; i<2; i++)
			map = exec.query(query);
			t1 = new Date().getTime();
			map = exec.query(query);
			t2 = new Date().getTime();

			System.out.println((t2-t1) / 1000.0 + "s");
			System.out.println(map.size());
//			//System.out.println(ng.getIndex());
//			System.out.println(count);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		}
		
	}


	//	public IDatatype equalsIgnoreAccent(Object o1, Object o2){
	//		IDatatype dt1 = (IDatatype) o1;
	//		IDatatype dt2 = (IDatatype) o2;
	//		boolean b = StringHelper.equalsIgnoreAccent(dt1.getLabel(), dt2.getLabel());
	//		if (b) return CoreseBoolean.TRUE;
	//		return CoreseBoolean.FALSE;
	//	}


	void trace(Graph graph){
		System.out.println(graph);
		//		graph.init();
		//System.out.println(graph.getIndex());
		int n = 0;
		//		for (Entity ent : graph.getIndex().get(graph.getNode(RDF.RDFTYPE))){
		//			System.out.println(ent);
		//			if (n++>50) break;
		//		}
	}

}
