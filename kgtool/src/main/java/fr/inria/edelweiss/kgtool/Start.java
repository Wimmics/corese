package fr.inria.edelweiss.kgtool;

import java.util.Date;
import java.util.ArrayList;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.ResultFormat;

public class Start {
	 ArrayList<String> load  = new ArrayList<String>();
	 ArrayList<String> query = new ArrayList<String>();
	 boolean debugRule = false;
	
	/**
	 * Corese as command line
	 * take path and query as argument
	 * load the docs from path
	 * java -cp corese.jar fr.inria.edelweiss.kgtool.Start -load dataset.rdf -query "select * where {?x ?p ?y}"   
	 */
	public static void main(String[] args){
		Start st = new Start();
		st.process(args);
		st.start();
	}
		
	void process(String[] args){
		int i = 0;
		while (i < args.length){
			if (args[i].equals("-load")){
				i++;
				while (i < args.length && ! args[i].startsWith("-")){
					load.add(args[i++]);
				}
			}
			else if (args[i].equals("-query")){
				i++;
				while (i < args.length && ! args[i].startsWith("-")){
					query.add(args[i++]);
				}
			}	
			else if (args[i].equals("-debug")){
				i++;
				while (i < args.length && ! args[i].startsWith("-")){
					if (args[i].equals("rule")){
						debugRule = true;
					}
					i++;
				}
			}
		}
	}
	
	void start(){
		Date d1 = new Date();
		Graph g = Graph.create(true);
		Load ld = Load.create(g);
		for (String doc : load){
			try {
				ld.loadWE(doc);
			} catch (LoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Date d2 = new Date();
		try {
			QueryProcess exec = QueryProcess.create(g);
			for (String q : query){
				Mappings map = exec.query(q);
				ResultFormat f = ResultFormat.create(map);
				System.out.println(f);
			}
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
}
