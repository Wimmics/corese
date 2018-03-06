package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.print.XMLFormat;

public class ALU {
	
	
	public static void main(String[] args) throws EngineException, CoreseDatatypeException{
		new ALU().process();
	}
	
	void process() throws CoreseDatatypeException {
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";
		String root = "/home/corby/workspace/coreseV2/src/test/resources/data/";

		System.out.println("load");
		
		Graph graph = Graph.create(true);
		Load load = Load.create(graph);
		load.load(data + "alu/Tennis.rdf");
		

		QueryProcess exec = QueryProcess.create(graph);
		
		
		String init = "prefix a: <test> " +
				"insert {?x ?p ?v} " +
				"where{" +
		"<aaa> rdf:value ((1) (2 3) (4))" +
		"}";
		
		String query = 
			"select (kg:setObject(<http://dbpedia.org/resource/Tennis>, 1.0) as ?b) where {} ";
		
		
		String q2 = "SELECT distinct ?x WHERE { "+
		"?x ?y ?z filter(kg:getObject(?x) = 1.0) "+
		"}" ;


		String q3 = 
			"select " +
			"(kg:setObject(<http://dbpedia.org/resource/Tennis>, 1.0) as ?b)" +
			"(kg:setProperty(<http://dbpedia.org/resource/Tennis>, ?pp, 2.0) as ?c) " +
			"(kg:setProperty(<http://dbpedia.org/resource/Tennis>, ?qq, 3.0) as ?d) " +
			"(kg:setProperty(<http://dbpedia.org/resource/Tennis>, ?pp, 4.0) as ?c) " +
			"where {} " +
			"bindings ?pp ?qq {(0 1)}";
		
		
		String q4 = "SELECT distinct ?x " +
		"(kg:getProperty(?x, 0) as ?p) " +
		"(kg:getProperty(?x, 1) as ?q) " +
				"WHERE { "+
		"?x ?y ?z " +
		"filter(kg:getObject(?x) = 1.0)" +
		"filter(kg:getProperty(?x, 0) >= 1.0) " +
		"filter(kg:getProperty(?x, 1) = 3) " +
		"(1 (3))" +
		"}" +
		"pragma {kg:query kg:display true}" ;
		
		DatatypeMap.setSPARQLCompliant(true);

		System.out.println("start");
		
		

		
		Mappings map = null;
		long t1 = new Date().getTime();
		
			try {
				map =exec.query(q3);
				map =exec.query(q4);
			} catch (EngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		long t2 = new Date().getTime();
		//map = exec.query(query);
		
		System.out.println(map);
		XMLFormat f = XMLFormat.create(map);
		System.out.println(f);

		System.out.println((t2-t1)/1000.0);
		
		
		t1 = new Date().getTime();
		IDatatype dt = DatatypeMap.createLiteral("12", RDFS.xsdshort);
		IDatatype dt2 = DatatypeMap.createLiteral("13", RDFS.xsdlong);
		System.out.println(dt.toSparql());
		System.out.println(dt2.toSparql());
		System.out.println(dt2.sameTerm(dt));
		System.out.println(dt2.greater(dt));

		t2 = new Date().getTime();
		System.out.println((t2-t1)/1000.0);

		// 1.564

	}
	
	
}
