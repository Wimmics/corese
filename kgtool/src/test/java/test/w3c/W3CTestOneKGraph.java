package test.w3c;

import java.util.ArrayList;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgenv.eval.QuerySolver;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.MatcherImpl;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.BuildOptim;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.RDFFormat;

public class W3CTestOneKGraph {
	
	public static void main(String[] args) throws EngineException, CoreseException{
		new W3CTestOneKGraph().process();
	}
	
	void process() throws EngineException, CoreseException{
	    String root = 
"/home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/";
	   String data = 
"/home/corby/workspace/coreseV2/src/test/resources/data/test-suite-archive/data-r2/";
		DatatypeMap.setSPARQLCompliant(true);

	    Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		BuildOptim bb = BuildOptim.create(graph);
		load.setBuild(bb);
		
//String gg = "file:///home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdf03.rdf";		
//ArrayList<String> from = new ArrayList<String>();
//ArrayList<String> named = new ArrayList<String>();
//from.add(gg);
//named.add("");

//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.ttl 
//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.rq 
//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.srx


		load.load(root + "entailment/rdfs04.rdf");
		//System.out.println(graph.getIndex());
		String query = new W3CTest11KGraph().read(root + "entailment/rdfs04.rq");
		System.out.println(query);
	
		
		QueryProcess exec = QueryProcess.create(graph);
		exec.setDebug(true);
		
		EvalListener el = EvalListener.create();
		//exec.addEventListener(el);
		
		Mappings res = exec.query(query);
		Graph g = (Graph)res.getGraph();
		//System.out.println(RDFFormat.create(g));
		System.out.println(res);
		System.out.println(res.size());
		
//		Graph gg = exec.getGraph(res);
//		
//		System.out.println( RDFFormat.create(gg, exec.getAST(res.getQuery()).getNSM()));
		
//		System.out.println(res.getQuery().getAST());
		
//		query = "select * where {?x ?p ?y}";
//		res = exec.query(query);
//		System.out.println(res);



	}
	
	/**
	 * grouping//group-data-1.ttl 
WWW/2009/sparql/docs/tests/data-sparql11/grouping//group03.rq 
WWW/2009/sparql/docs/tests/data-sparql11/grouping//group03.srx
	 */
	
	
	
}
