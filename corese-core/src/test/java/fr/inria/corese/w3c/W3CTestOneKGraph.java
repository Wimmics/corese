package fr.inria.corese.w3c;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.event.EvalListener;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import org.junit.Ignore;

@Ignore
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

	    Graph graph = Graph.create();
		Load load = Load.create(graph);
		//BuildOptim bb = BuildOptim.create(graph);
		//load.setBuild(bb);
		
//String gg = "file:///home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdf03.rdf";		
//ArrayList<String> from = new ArrayList<String>();
//ArrayList<String> named = new ArrayList<String>();
//from.add(gg);
//named.add("");

//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.ttl 
//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.rq 
//WWW/2009/sparql/docs/tests/data-sparql11/entailment/rdfs04.srx


		load.load(data + "i18n/normalization-02.rdf");
		try {
			load.loadWE("/home/corby/workspace/coreseV2/src/test/resources/data/w3c-sparql11/data/earl.ttl");
		} catch (LoadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(graph.getIndex());
		String query = new TestW3C11KGraph().read(data + "i18n/normalization-02.rq");
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
		
		//System.out.println( RDFFormat.create(graph));
//		System.out.println( XMLFormat.create(res));
		
//		ASTQuery ast = exec.getAST(res);
//		Triple t = ast.getBody().get(0).getTriple();
//		System.out.println(t.getObject().getLongName());

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
