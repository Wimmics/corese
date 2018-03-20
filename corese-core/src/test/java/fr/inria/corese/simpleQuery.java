package fr.inria.corese;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.print.TripleFormat;
import fr.inria.corese.kgram.core.Mappings;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 *
 * @author edemairy
 */
public class simpleQuery {

	private static int cpt = 0;

	private static void heapDump() throws IOException {
		System.out.println("cpt = " + cpt);
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.substring(0, name.indexOf("@"));
//		After that we can start jmap process like this:
		String[] cmd = {"jmap", "-dump:file=/tmp/dump-" + cpt + ".bin", pid};
		System.out.println(cmd[1]);
//		Process p = 
		Runtime.getRuntime().exec(cmd);
		cpt++;
	}

	private static void heapDump2() {
//		ManagementFactory.getDiagnosticMXBean().dumpHeap("/tmp/dump-"+cpt+".bin", true);
		HeapDumper.dumpHeap("/tmp/dump2-" + cpt + ".hprof", true);
		cpt++;
	}
//	@Test

	public static void main(String[] args) throws EngineException, LoadException, IOException {
		Graph graph = Graph.create();
		Parameters params = Parameters.create();
		params.add(Parameters.type.MAX_LIT_LEN, 2);
		graph.setStorage(IStorage.STORAGE_FILE, params);
		Load ld = Load.create(graph);
		//ld.parse("/Users/edemairy/Developpement/Corese-master/kgtool/src/test/java/human_2007_09_11.rdf");
		heapDump2();
		ld.parse("/Users/edemairy/Downloads/peel.rdf");
		heapDump2();
		QueryProcess exec = QueryProcess.create(graph);
		heapDump2();
		String query = "select * where {?x ?p ?y}";
		Mappings map = exec.query(query);
		heapDump2();
		ResultFormat f1 = ResultFormat.create(map);
		System.out.println(f1);
		TripleFormat f2 = TripleFormat.create(graph);
		System.out.println(f2);
		Byte b;
	}
}
