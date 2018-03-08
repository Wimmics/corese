package fr.inria.corese.kgengine.kgraph;

import java.util.Date;





import fr.inria.corese.sparql.cg.datatype.DatatypeMap;
import fr.inria.corese.kgenv.eval.QuerySolver;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.Load;

public class TestInsee {
	
	public static void main(String[] args){
		new TestInsee().process();
	}
	
	void process(){
		String data = "/home/corby/Download/insee/arrondissements-71-2011.rdf";
		String file = "file://" + data + "test.xml";
		
		String path = "file://" + data;
		
		QuerySolver.definePrefix("geo", "http://rdf.insee.fr/geo/");
		QuerySolver.definePrefix("data",  path + "comma/");
		QuerySolver.definePrefix("data1", path + "comma/data/");
		QuerySolver.definePrefix("data2", path + "comma/data2/");

		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(true);
//		graph.set(Entailment.RDFSRANGE, true);
//		graph.set(Entailment.RDFSSUBCLASSOF, true);
		//graph.set(Entailment.RDFSSUBPROPERTYOF, !true);

		Load loader =  Load.create(graph);
		
		long t1 = new Date().getTime();
		loader.load(data);
		long t2 = new Date().getTime();
		
		System.out.println("** Time: " + (t2-t1)/1000.0);

	}

}
