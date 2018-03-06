package sna;


import java.util.Date;
import java.util.List;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.path.Visit;
import fr.inria.corese.kgraph.core.edge.EdgeImpl;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.QueryLoad;



/**
 * 
 *  Semantic SNA
 *  Implemented as ResultListener on KGRAM, process path on the fly
 *  
 *  Optimizations:
 *  - PathFinder send path to ResultListener on the fly, no Mapping created 
 *  (hence query return no result Mapping) no thread.
 *  - Visit check loop with boolean in Node (work only with simple regex)
 *  - Graph manage copy as default graph (with no graph name duplicate)
 *  
 *  
 * Olivier Corby, Edelweiss, INRIA, 2011
 * 
 */
public class SNATest {
	
	
	public static void main(String[] args) throws EngineException{
		new SNATest().process();
	}
	
	
	void process() throws EngineException{
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";

		Graph graph = Graph.create(true);
		graph.setDefault(true);
		
		// use Node boolean to check loop
		Visit.speedUp 		= true;
		
		Load load 	 = Load.create(graph);		
		QueryLoad ql = QueryLoad.create();

		load.load(root + "sna/foaf.rdf");
						
		String start = ql.read(root + "sna/start.rq");
		String query = ql.read(root + "sna/sna.rq");

		QueryProcess exec = QueryProcess.create(graph);
		
		// compute the list of nodes of the network
		Mappings nodeList = exec.query(start);
		
		System.out.println("total: " + nodeList.size());	
//		2 147 483 647

		int n = 0;
		
		long t1 = new Date().getTime();
		int max = 0;
		
		SSNA sna  =  SSNA.create(nodeList.size());
		//SSNA2 sna =  SSNA2.create();
		SSNAHandlerImpl handler = SSNAHandlerImpl.create();
		//sna.set(handler);
		EdgeImpl.displayGraph = false;
		//sna.setFake(true);

		// set SNA as kgram ResultListener 
		// sna trap path using process(path)
		exec.addResultListener(sna);
		exec.setListPath(true);
		
		Query qsna = exec.compile(query);
		
		sna.start();
				
		for (Mapping map : nodeList.project()){
			// for each source Node ?x, find all path to all target ?y
			Node node = map.getNode("?x");
			//if (! node.getLabel().contains("corby")) continue;
			
			sna.reset();
			Mappings ms = exec.query(qsna, map);
			//System.out.println(n + ": " + node + " " + sna.nbResult());
			
			
			sna.process();	
			
			
			n++;
			//if (n == 2) break;
		}
		
		sna.complete();
		
		List<Node> list = sna.result();
		
		long t2 = new Date().getTime();
		
		for (Node node : list){
			System.out.println(node + " " + sna.getDegree(node) + " " + sna.getCentrality(node) + " " + sna.getCount(node) 
					+ " " + sna.getInDegree(node) + " " + sna.getOutDegree(node));
		}
		
		System.out.println("** NB Node: " + list.size());
		System.out.println("** Max Path Length: " + sna.getMaxPathLength());
		System.out.println("** NB Path : " + sna.totalResult());

		//handler.display();
		
		System.out.println("** Time: " + ((t2 - t1) / 1000.0) );	

		
	}
	
	
	
	
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

	** Size: 358
	** Max Path Length: 57
	** NB Path : 8829992
	** Time: 35.22
	*
	** NB Node: 358
** Max Path Length: 57
** NB Path : 8829992
** Time: 26.069    path: 19 ; sna: 7
*
*
** NB Node: 167
** Max Path Length: 57
** NB Path : 8830521
** Time: 21.50   path: 15.5 ; sna: 6

** NB Node: 167
** Max Path Length: 57
** NB Path : 8830606
** Time: 17.332

*
*
	 */
	
	
	
	
}
