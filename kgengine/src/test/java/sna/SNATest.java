package sna;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Group;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.path.Path;
import fr.inria.edelweiss.kgram.path.PathFinder;
import fr.inria.edelweiss.kgram.path.Visit;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.RDFFormat;


/**
 * 615 nodes
** 410 intermediate nodes
** Max Path Length: 58
** NB Path : 9.188.534
** Time: 238 = 4 min
*
*** Size: 408
** Max Path Length: 58
** NB Path : 9176713
** Time: 134.567

*** Size: 408
** Max Path Length: 58
** NB Path : 9222437
** Time: 110.224
*
*** Size: 408
** Max Path Length: 58
** NB Path : 9222437
** Time: 91.125
*
*** Size: 408
** Max Path Length: 58
** NB Path : 9222437
** Time: 74.879
*
*
** Size: 408
** Max Path Length: 58
** NB Path : 9235641
** Time: 36.603  (path: 20, sna: 16)
252.319 path per sec


**
 */

public class SNATest {
	
	
	public static void main(String[] args) throws EngineException{
		new SNATest().process3();
	}
	
	
	void process3() throws EngineException{
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		graph.setDefault(true);
		
		// use Node boolean to check loop
		Visit.speedUp 		= true;
		
		Load load = Load.create(graph);
		
		
//		load.load(data + "comma/comma.rdfs");
//		load.load(data + "comma/data2");
		load.load(root + "sna/foaf.rdf");

		
				
		QueryLoad ql = QueryLoad.create();
		String start = ql.read(root + "sna/fstart.rq");
		String query = ql.read(root + "sna/fsn7.rq");
		
		//load.load(root + "sna/sna.rdf");
		//query = ql.read(root + "sna/query.txt");

		QueryProcess exec = QueryProcess.create(graph);
		exec.setListGroup(true);

		
		Mappings lm = exec.query(start);
		
		System.out.println("total: " + lm.size());	
		int n = 0;
		
		long t1 = new Date().getTime();
		int max = 0;
		
		SSNA sna =  SSNA.create();

		// set SNA as kgram ResultListener 
		// sna trap results using process(env)
		exec.addResultListener(sna);
		
		Query qq = exec.compile(query);
		
		for (Mapping m : lm.project()){
			// for each source Node ?x, find all path to all target ?y
			sna.reset();
			Mappings ms = exec.query(qq, m);
			System.out.println(n + ": " + m.getValue("?x") + " " + sna.nbResult());
			ProducerImpl p = (ProducerImpl) exec.getProducer();
			sna.process();			
			n++;
			//if (n == 100) break;
		}
		
		List<Node> list = sna.result();
		
		for (Node node : list){
			System.out.println(node + " " + node.getProperty(Node.DEGREE));
		}
		
		System.out.println("** Size: " + list.size());
		System.out.println("** Max Path Length: " + sna.getMaxPathLength());
		System.out.println("** NB Path : " + sna.totalResult());

		
		long t2 = new Date().getTime();
		System.out.println("** Time: " + ((t2 - t1) / 1000.0) );	
		
//		long t1 = new Date().getTime();
//		query = ql.read(root + "sna/fsn76.rq");
//		System.out.println(query);	
//		Mappings map = null;
//		int NB = 3;
//		for (int i=0; i<NB; i++)
//		map = exec.query(query);
//		
//		process(map);
//		
//		
//		
//		System.out.println("** Loop: " + PathFinder.cedge + " " + PathFinder.ctest);	
//		long t2 = new Date().getTime();
//		
//		System.out.println(map.size());
//		
//		System.out.println("** Time: " + ((t2 - t1) / 1000.0) / NB);	
		
	}
	
	
	
	

	
	void process2() throws EngineException{
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		graph.setDefault(true);
		
		Visit.speedUp = true;
		
		Load load = Load.create(graph);
		
		
//		load.load(data + "comma/comma.rdfs");
//		load.load(data + "comma/data2");
		load.load(root + "sna/foaf.rdf");

		
				
		QueryLoad ql = QueryLoad.create();
		String start = ql.read(root + "sna/fstart.rq");
		String query = ql.read(root + "sna/fsna6.rq");
		
		//load.load(root + "sna/sna.rdf");
		//query = ql.read(root + "sna/query.txt");

		QueryProcess exec = QueryProcess.create(graph);
		exec.setListGroup(true);
		
		
//		Mappings lm = exec.query(start);
//		
//		System.out.println("total: " + lm.size());	
//		int n = 0;
//		
//		long t1 = new Date().getTime();
//
//		for (Mapping m : lm.project()){
//			System.out.println(n + ": " + m.getValue("?x"));
//			Mappings ms = exec.query(query, m);
//			System.out.println("size: " + ms.size());
//			System.out.println("** Loop: " + PathFinder.cedge );
//			ProducerImpl p = (ProducerImpl) exec.getProducer();
//			//System.out.println("Individuals: " + p.getLocalGraph().nbIndividuals());
//			//System.out.println("Literals: "    + p.getLocalGraph().nbLiterals());
//			//if (ms.size()>0) 
//			//System.out.println(ms);	
//			n++;
//			//if (n == 100) break;
//		}
//		long t2 = new Date().getTime();
//		System.out.println("** Time: " + ((t2 - t1) / 1000.0) );	

		
		long t1 = new Date().getTime();
		query = ql.read(root + "sna/fsn76.rq");
		System.out.println(query);	
		Mappings map = null;
		int NB = 3;
		for (int i=0; i<NB; i++)
		map = exec.query(query);
		
		
		
		
		System.out.println("** Loop: " + PathFinder.cedge + " " + PathFinder.ctest);	
		long t2 = new Date().getTime();
		
		System.out.println(map.size());
		
		System.out.println("** Time: " + ((t2 - t1) / 1000.0) / NB);	
		
		
//		query = ql.read(root + "sna/ins.rq");
//		Mappings map = exec.query(query);	
//		System.out.println(map.size());
//		RDFFormat f = RDFFormat.create(map);
//		try {
//			f.write(root + "sna/foaf.rdf");
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		

		
	}
	
	
	
	
	
	void groupBy(Mappings map){
		Query q = map.getQuery();
		for (Query qq : q.getQueries()){
			System.out.println(qq);
			List<Exp> list = qq.getGroupBy();
			map.groupBy(list);
			System.out.println(map);	
			break;
		}
	}
	
	
	void process() throws EngineException{
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		
		
		load.load(root + "kgraph/sna.rdf");
		
		QueryLoad ql = QueryLoad.create();
		String query = ql.read(root + "kgraph/sna.rq");

		QueryProcess exec = QueryProcess.create(graph);
		exec.setListGroup(true);
		
		String qq = "prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select distinct ?from where {?from c:knows ?to}";
		Mappings mm = exec.query(qq);
		System.out.println(mm.project());

		for (Mapping gm : mm){
			
			Mappings map = exec.query(query, gm);
			System.out.println(map);
			System.out.println(map.size());

			Query q = map.getQuery();



			ArrayList<String> ll = new ArrayList<String>();
			ll.add("?from");
			ll.add("?to");
			ll.add("?l");

			Group group1 = map.defineGroup(ll);

			ll.add("?between");

			Group group2 = map.defineGroup(ll);


			map.groupBy(group1);


			System.out.println(map);
			for (Mapping m : map){
				System.out.println(m.getMappings().size());
			}

			System.out.println("___");


			map.groupBy(group2);

			System.out.println(map);
			for (Mapping m : map){
				System.out.println(m.getMappings().size());
			}

			System.out.println("===");

		}


		/**
		 * enumerate map
		 * group by same ?from ?to min(?length) 
		 * in group : total = sum(count)
		 * for each map in group : count2 = count/total
		 */

		// group by ?from ?to min(pathLength($path)) ?between

	}
	
	
	void test(Mappings lm){
		
		Node tfrom1= null , tto1= null, tfrom2= null, tto2= null, tl1 = null, tl2 = null;
		
		for (Mapping map : lm){
			
			Node from 	= map.getQueryNode("?from");
			Node to 	= map.getQueryNode("?to");
			Node ll 	= map.getQueryNode("?l");
			
			tfrom2 = map.getNode(from);
			tto2   = map.getNode(to);
			tl2    = map.getNode(ll);
			
			if (tfrom1 == null){
				tfrom1 = tfrom2;
				tto1 = tto2;
				tl1 = tl2;
			}
			else {
				
			}

			
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
