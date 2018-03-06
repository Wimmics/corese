package kgraph;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;

public class Distrib {
	
	
	public static void main(String[] args){
		new Distrib().process();
	}
	
	void process(){
		
		Graph g = Graph.create();
		QueryProcess exec = QueryProcess.create(g);
		String q = "select * where {?x ?p ?y}";
		
		try {
			Mappings map = exec.query(q);
			
			System.out.println(map.size());
			
			
		} catch (EngineException e) {
			e.printStackTrace();
		}
		
	}

}
