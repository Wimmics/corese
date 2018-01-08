/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.corese.shell;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.TripleFormat;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author edemairy
 */
public class Shell {

	public static void main(String[] args) throws EngineException, IOException {
		Graph graph = Graph.create();
		Load ld = Load.create(graph);
		QueryProcess exec = QueryProcess.create(graph);
		Scanner scanner = new Scanner(Paths.get(args[0]));
		StringBuilder queryBuilder = new StringBuilder();
		while (scanner.hasNext()) {
			queryBuilder.append(scanner.nextLine()).append("\n");
		}
		long s = System.nanoTime();
		Mappings map = exec.query(queryBuilder.toString());
		long e = System.nanoTime();
		System.out.println("Elapsed time for the request: "+(e-s)/(1000000000.));
		ResultFormat f1 = ResultFormat.create(map);
		System.out.println("ResultFormat"+f1);
}
}
