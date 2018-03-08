package fr.inria.corese.kgengine.kgraph;

import java.util.Date;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.event.StatListener;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgraph.query.ValidateListener;
import fr.inria.corese.kgraph.rule.RuleEngine;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;

public class Neurolog {
	
	
	public static void main(String[] args) throws EngineException{
		new Neurolog().process();
	}
	
	void process() throws EngineException{
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";
		data = "/home/corby/Download/CoresePerfs/CoresePerfs/";

		QueryProcess.setSort(true);
		
		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		RuleEngine re = RuleEngine.create(graph);
		//re.set(SorterImpl.create(graph));
		load.setEngine(re);
		
		Date d1 = new Date();
		load.load(data + "opmo-20101012.owl");
		load.load(data + "SDB-dump5705839910527228377.rdf");
		//load.load(data + "SDB-dump1797524471969585668.rdf");		 
		load.load(data + "finishProvUsed.rul");
		
		QueryLoad ql = QueryLoad.create();
		// Loop: 5338 6382

		String query = ql.read(data + "query.rq");
		
//		query = "PREFIX opmo: <http://openprovenance.org/model/opmo#>" +
//				"select  ?gen ?class where {" +
//				"?gen opmo:effect ?a1 " +
//				"?gen rdf:type opmo:WasGeneratedBy " +
//				"?gen rdf:type ?class" +
//				"} limit 10";
		
		Date d2 = new Date();
		
		System.out.println("** Size: " + graph.size());

		System.out.println("** Load: " + (d2.getTime() - d1.getTime()) / 1000.0);

		//graph.init();
		// stop RDFS entailment during rule
		//graph.setEntailment(false);

		//System.out.println("** Entailment: " + (d1.getTime() - d2.getTime()) / 1000.0);
		
		re.setDebug(true);
		d1 = new Date();
		//re.process();
		d2 = new Date();
		System.out.println("** Rule Time: " + (d2.getTime() - d1.getTime()) / 1000.0);

		QueryProcess exec = QueryProcess.create(graph);
		//exec.set(SorterImpl.create(graph));
		//exec.query(query);
		StatListener el = StatListener.create();
		exec.addEventListener(el);
		
		ValidateListener vl = ValidateListener.create();
		exec.addEventListener(vl);

		Mappings map = exec.query(query);
		
		System.out.println(el.display());
		System.out.println(vl);

		System.out.println("** Size: " + map.size());
//		
		
		//System.out.println(map);



		
//		QueryProcess exec = QueryProcess.create(graph);
//		
//		String query = "select * where {" +
//				"" +
//				"}";
//		
//		Mappings map = exec.query(query);
//		
//		System.out.println(map);
		
		
	}
	
	
}
