/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.coresetimer.utils;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

/**
 *
 * @author edemairy
 */
public class BenchmarkProcessBuilder {

	public static Process createDatabaseBuilderProcess(List<String> shellCommand) throws IOException {
		List<String> fullCommandLine = new ArrayList<String>();
		fullCommandLine.addAll(shellCommand);
		fullCommandLine.forEach(System.out::println);
		ProcessBuilder pb = new ProcessBuilder(fullCommandLine);
		File log = new File("log");
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		Process p = pb.start();
		assert pb.redirectInput() == Redirect.PIPE;
		assert pb.redirectOutput().file() == log;
		assert p.getInputStream().read() == -1;
		return p;
	}

	public static void main(String... args) throws IOException, InterruptedException {
		String commandRoot = "/Users/edemairy/Developpement/myCorese/persistency-tools/rdf-to-graph-app/";
		String scriptRun = commandRoot + "run.sh";
		String inputData = "/Users/edemairy/data/btc_2010/btc-2010-chunk-000.nq.gz:10000";
		String outputDatabasePath = "/tmp/test_db";
		String driver = "neo4j";

		String[] shellCommands = {"/bin/bash", "-l", "-c", String.format("%s \"%s\" \"%s\" \"%s\"", scriptRun, inputData, outputDatabasePath,driver) };
		Process databaseProcess = createDatabaseBuilderProcess(Arrays.asList(shellCommands));
		int result = databaseProcess.waitFor();
		System.out.println("result was " + result);

	}
}
