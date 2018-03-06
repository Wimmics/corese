/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgdqp.core;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class FedQueryingCLI {

    static Logger logger = LogManager.getLogger(FedQueryingCLI.class);

    @SuppressWarnings("unchecked")
    public static void main(String args[]) throws ParseException, EngineException {

        List<String> endpoints = new ArrayList<String>();
        String queryPath = null;
        int slice = -1;

        Options options = new Options();
        Option helpOpt = new Option("h", "help", false, "print this message");
        Option queryOpt = new Option("q", "query", true, "specify the sparql query file");
        Option endpointOpt = new Option("e", "endpoints", true, "the list of federated sparql endpoint URLs");
        Option groupingOpt = new Option("g", "grouping", true, "triple pattern optimisation");
        Option slicingOpt = new Option("s", "slicing", true, "size of the slicing parameter");
        Option versionOpt = new Option("v", "version", false, "print the version information and exit");
        options.addOption(queryOpt);
        options.addOption(endpointOpt);
        options.addOption(helpOpt);
        options.addOption(versionOpt);
        options.addOption(groupingOpt);
        options.addOption(slicingOpt);

        String header = "Corese/KGRAM DQP command line interface";
        String footer = "\nPlease report any issue to alban.gaignard@cnrs.fr";

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("kgdqp", header, options, footer, true);
            System.exit(0);
        }
        if (!cmd.hasOption("e")) {
            logger.info("You must specify at least the URL of one sparql endpoint !");
            System.exit(0);
        } else {
            endpoints = new ArrayList<String>(Arrays.asList(cmd.getOptionValues("e")));
        }
        if (!cmd.hasOption("q")) {
            logger.info("You must specify a path for a sparql query !");
            System.exit(0);
        } else {
            queryPath = cmd.getOptionValue("q");
        }
        if (cmd.hasOption("s")) {
            try {
                slice = Integer.parseInt(cmd.getOptionValue("s"));
            } catch (NumberFormatException ex) {
                logger.warn(cmd.getOptionValue("s") + " is not formatted as number for the slicing parameter");
                logger.warn("Slicing disabled");
            }
        }
        if (cmd.hasOption("v")) {
            logger.info("version 3.0.4-SNAPSHOT");
            System.exit(0);
        }

        /////////////////
        Graph graph = Graph.create();
        QueryProcessDQP exec = QueryProcessDQP.create(graph);
        exec.setGroupingEnabled(cmd.hasOption("g"));
        if (slice > 0) {
            exec.setSlice(slice);
        }
        Provider sProv = ProviderImplCostMonitoring.create();
        exec.set(sProv);

        for (String url : endpoints) {
            try {
                exec.addRemote(new URL(url), WSImplem.REST);
            } catch (MalformedURLException ex) {
                logger.error(url + " is not a well-formed URL");
                System.exit(1);
            }
        }

        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(queryPath));
        } catch (FileNotFoundException ex) {
            logger.error("Query file " + queryPath + " not found !");
            System.exit(1);
        }
        char[] buf = new char[1024];
        int numRead = 0;
        try {
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
            reader.close();
        } catch (IOException ex) {
            logger.error("Error while reading query file " + queryPath);
            System.exit(1);
        }

        String sparqlQuery = fileData.toString();

//        Query q = exec.compile(sparqlQuery, null);
//        System.out.println(q);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = exec.query(sparqlQuery);
        int dqpSize = map.size();
        System.out.println("--------");
        long time = sw.getTime();
        System.out.println(time + " " + dqpSize);
    }
}
