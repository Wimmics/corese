package kgraph;

import java.util.Date;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgpipe.Pipe;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.QueryLoad;






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