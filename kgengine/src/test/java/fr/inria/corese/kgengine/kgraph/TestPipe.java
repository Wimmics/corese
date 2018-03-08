package fr.inria.corese.kgengine.kgraph;


import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.kgpipe.Pipe;
import fr.inria.corese.kgraph.api.Loader;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.load.Load;






public class TestPipe {
	
	public static void main(String[] args){
		new TestPipe().process();
	}
	
	void process(){
		String data = "/home/corby/workspace/kgengine/src/test/resources/data/";		
				
		DatatypeMap.setLiteralAsString(false);

		Graph graph = Graph.create(true);	
		
		Loader load = Load.create(graph);

		Pipe pipe = Pipe.create(graph);
		
		pipe.setDebug(true);
		
		pipe.process(data + "cstb/Pipe.rdf");
		
		
	}
	
}