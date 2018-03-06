package kgraph;


import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;

public class TestThread extends Thread {
	
	static String data = "/home/corby/workspace/coreseV2/src/test/resources/data/";
	Graph graph;
	int index;
	
	TestThread(int n, Graph g){
		graph = g;
		index = n;
	}
	
	TestThread(){
		
	}
	
	public static void main(String[] args){
		new TestThread().process();
	}
	
	void process(){
		Graph g = init();
		graph = g;
		//load();
		//new TestThread(0, g).run();
		for (int i = 1; i<= 20; i++){
			TestThread pp = new TestThread(i, g);
			//System.out.println("** Start Thread: " + i);
			pp.start();
		}
	}

	Graph init() {
		Graph g = Graph.create(true);
//		Load load = Load.create(g);
//		load.load(data + "comma/comma.rdfs");
//		load.load(data + "comma/model.rdf");
		return g;
	}
	
	
	public void run(){
		process(index);
	}
	
	void process(int i){
		switch (i){
		
		case 7: load();break;
		
		case 14: update(); break;
		
		default: query();
		}
	}

	void load(){
		QueryProcess exec = QueryProcess.create(graph);
		String query = 
			"prefix data: <" + data + "comma/>" +
			"load data:comma.rdfs ;" +
			"load data:model.rdf";
		try {
			exec.query(query);
			System.out.println("** Stop Thread Load: " + index);		
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	void update(){
		QueryProcess exec = QueryProcess.create(graph);
		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			" insert data {" +
				"<John> c:name 'John' " +
				"c:name rdfs:domain c:Person " +
			"}";
		try {
			exec.query(query);
			System.out.println("** Stop Thread Update: " + index);		
		} catch (EngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	void query(){
		QueryProcess exec = QueryProcess.create(graph);

		String query = 
			"prefix c: <http://www.inria.fr/acacia/comma#>" +
			"select * where {" +
				"?x rdf:type/rdfs:subClassOf* c:Person " +
				"c:Person rdfs:subClassOf* ?y" +
			"}";

		try {
			Mappings map = exec.query(query);
			System.out.println("** Stop Thread: " + index + " " + map.size());		
			} catch (EngineException e) {
			e.printStackTrace();
		}

	}
	
	
}
