package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
//import fr.inria.acacia.corese.cg.datatype.RDF;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import fr.inria.edelweiss.kgtool.print.XMLFormat;

public class Metro {
	
	
	public static void main(String[] args) throws EngineException{
		new Metro().process();
	}
	
	void process() {
		String root = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
		String w3c = "/home/corby/workspace/coreseV2/src/test/resources/data/test-suite-archive/data-r2/";
		System.out.println("load");
		
		Graph graph = Graph.create(true);
		graph.setOptimize(true);
		Load load = Load.create(graph);
		graph.set(Entailment.DATATYPE_INFERENCE, true);
		//graph.set(Entailment.DUPLICATE_INFERENCE, false);

		

		//load.load(data + "kgraph/normalization-01.rdf");
		
		QueryLoad ql = QueryLoad.create();
		
		

		QueryProcess exec = QueryProcess.create(graph);		
		
		String init = "prefix foaf: <http://xmlns.com/foaf/0.1/>" +
				"insert data {" +
				"foaf:Person a rdfs:Class " +
				"foaf:Animal a rdfs:Class " +
				"foaf:Person rdfs:subClassOf foaf:Animal" +
				"<John> a foaf:Person " +
				"}";
		
		init = "select * where {} ";
		
		
		String query = "select where {} pragma {kg:kgram kg:status true}";
		
		query = "prefix c: <http://www.inria.fr/acacia/comma#>" +
				"select * where {" +
				"?x ?p ?y filter(?x ~ 'olivier.corby')" +
				"filter(?p = c:Designation) " +
				//"?y ?q ?z" +
				"?p rdfs:subPropertyOf{1} ?q " +
				"} ";

		
		// 1.6 vs 1.2
		// 1.7
		DatatypeMap.setSPARQLCompliant(true);

		System.out.println("Load");
		
		//exec.setListPath(true);

		
		query 			= ql.read(root + "metro/query/search2.txt");
		init  			= ql.read(root + "metro/query/insert.txt");
		String test  	= ql.read(root + "metro/query/test.txt");
		String deg  	= ql.read(root + "metro/query/degre.txt");
		String list  	= ql.read(root + "metro/query/list.txt");
		String ligne  	= ql.read(root + "metro/query/ligne.txt");
		String change  	= ql.read(root + "metro/query/change.txt");

		load.load(root + "metro/test");
		load.load(root + "metro/rdf");
		
//		graph.getResource(RDF.FIRST).setProperty(Node.WEIGHT, 2);
//		graph.getResource(RDF.REST).setProperty(Node.WEIGHT, 1);

		//http://plandeparis.info/metro-de-paris/liste-des-stations.html#Ligne_4
		//System.out.println(query);

		Mappings map = null;
		try {
			map = exec.query(init);
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 4.266

		//query = "select * where {?x sa rdf:rest@1/rdf:first@2 ?y} limit 1";
		
		System.out.println("Start");
		long t1 = new Date().getTime();
		
			try {
				for (int i=0; i<10; i++)
				map =exec.query(query);
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		long t2 = new Date().getTime();
		//map = exec.query(query);
		
		System.out.println(map.size());
		System.out.println(map);
//		Mapping m = map.get(0);
//		System.out.println(m.getPath("?path"));

		System.out.println((t2-t1)/1000.0);
		//System.out.println(XMLFormat.create(map));


		// 1.564

	}
	
	
}
