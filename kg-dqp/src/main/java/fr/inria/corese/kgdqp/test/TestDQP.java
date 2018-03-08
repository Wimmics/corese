/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.test;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.Messages;
import fr.inria.corese.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.corese.kgdqp.core.QueryProcessDQP;
import fr.inria.corese.kgdqp.core.Util;
import fr.inria.corese.kgdqp.core.WSImplem;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.Load;
import fr.inria.corese.kgtool.load.LoadException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
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
 * @author macina
 */
public class TestDQP {

    private Logger logger = LogManager.getLogger(TestDQP.class);

    static final String host = "localhost";

    private HashMap<String, String> queries = new HashMap<String, String>();
    private boolean modeBGP = false;
    private int round = 0;

    public TestDQP(boolean modeBGP) {

        this.modeBGP = modeBGP;
        String simple = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE { "
                + "    ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement ."
                + "     ?departement igeo:nom ?nom .  "
                + "    ?departement idemo:population ?popLeg ."
                + "    ?popLeg idemo:populationTotale ?popTotale ."
                + "} ORDER BY ?popTotale";

        String optional = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT * WHERE { "
                + "    ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement ."
                + "    ?departement igeo:nom ?nom . "
                + "    OPTIONAL { ?departement igeo:subdivisionDirecte ?arrondisement . }"
                + "} ORDER BY ?region ?departement ?nom ?arrondisement";

        String union = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?arrondisement ?popTotale  WHERE { "
                + "    { ?region igeo:codeRegion ?v ."
                + "       ?region igeo:subdivisionDirecte ?departement ."
                + "       ?departement igeo:nom ?nom . "
                + "       ?departement igeo:subdivisionDirecte ?arrondisement . "
                + "       FILTER (?v <= \"42\")"
                + "    } "
                + "       UNION "
                + "    { "
                + "       ?region igeo:codeRegion ?v ."
                + "       ?region igeo:subdivisionDirecte ?departement ."
                + "       ?departement igeo:nom ?nom . "
                + "       ?departement igeo:subdivisionDirecte ?arrondisement . "
                + "       FILTER (?v > \"42\")"
                + "    } "
                + "    ?arrondisement idemo:population ?popLeg ."
                + "    ?popLeg idemo:populationTotale ?popTotale . "
                + "} ORDER BY ?popTotale";

//        String filterbis = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
//                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
//                + "SELECT ?arrondisement2 ?arrondisement1  WHERE { "
//                + "       ?region igeo:codeRegion ?v1 ."
//                + "       ?region igeo:subdivisionDirecte ?departement1 ."
//                + "       ?departement1 igeo:nom ?nom . "
//                + "       ?departement1 igeo:subdivisionDirecte ?arrondisement1 . "
//                + "       ?region igeo:subdivisionDirecte ?departement2 ."
//                + "       ?departement2 igeo:subdivisionDirecte ?arrondisement2 . "
//                + "FILTER (?arrondisement1 != ?arrondisement2)"
//                + "} ";
//
//        String subQuery = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
//                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
//                + "SELECT ?nom ?popTotale  WHERE { "
//                + "    ?region igeo:codeRegion ?v ."
//                + "    ?region igeo:subdivisionDirecte ?departement ."
//                + "    ?departement igeo:nom ?nom . "
//                + "     { SELECT ?region "
//                + "       { ?region igeo:codeRegion \"31\" . } "
//                + "     }"
//                + "    ?departement idemo:population ?popLeg ."
//                + "    ?popLeg idemo:populationTotale ?popTotale . "
//                + "} ORDER BY ?popTotale";

        String minus = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "   ?region igeo:subdivisionDirecte ?departement ."
                + "   ?departement igeo:nom ?nom .  "
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   minus { "
                + "       ?region igeo:codeRegion \"24\" ."
                + "       ?departement igeo:subdivisionDirecte <http://id.insee.fr/geo/arrondissement/751> . "
                + "        }  "
                + "   ?arrondissement idemo:population ?popLeg ."
                + "   ?popLeg idemo:populationTotale ?popTotale . "
                + "} ORDER BY ?popTotale";

        String filters = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?arrondissement ?cantonNom  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement . "
                + "    ?departement igeo:nom ?nom .   "
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   ?arrondissement igeo:subdivisionDirecte ?canton . "
                + "    ?canton igeo:nom ?cantonNom . "
                + " FILTER (?v = \"11\") "
                + " FILTER (?cantonNom = \"Paris 14e  canton\") "
                + "} ORDER BY ?arrondissement ?cantonNom";

        String all = "PREFIX idemo:<http://rdf.insee.fr/def/demo#> "
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE {  "
                + "{ ?region igeo:codeRegion  \"24\". "
                + "?region igeo:subdivisionDirecte ?departement . "
                + "?departement igeo:subdivisionDirecte ?arrondissement . "
                + " OPTIONAL { "
                + " ?arrondissement igeo:nom ?nom .  "
                + " }"
                + "}  "
                + " UNION "
                + "{   "
                + "   ?region igeo:codeRegion ?v ."
                + "   ?region igeo:subdivisionDirecte ?departement ."
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   ?arrondissement igeo:nom ?nom .  "
                + "   minus { "
                + "        ?departement igeo:subdivisionDirecte <http://id.insee.fr/geo/arrondissement/751> . "
                + "        ?arrondissement igeo:subdivisionDirecte <http://id.insee.fr/geo/canton/6448> . "
                + "         FILTER (?v = \"24\")"
                + "        }  "
                + "  }  "
                + "    ?arrondissement idemo:population ?popLeg .  "
                + "    ?popLeg idemo:populationTotale ?popTotale .   "
                + " } ORDER BY ?popTotale";

        //name queries and queries
        queries.put("simple", simple);
//        queries.put("minus",minus);
//        queries.put("union",union);
//        queries.put("filters",filters);
//        queries.put("optional",optional);
//        queries.put("all", all);
//
//        queries.put("subQuery",subQuery);//?? but processed as AND by default  because EDGES + SUBQUERY is not an AND BGP-able
//     //when method putFreeEdgesInBGP is used => duplicated result TO FIX
    }

    public void testLocal() throws EngineException, MalformedURLException, IOException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcessDQP.create(graph);

        StopWatch sw = new StopWatch();
        sw.start();
        logger.info("Initializing GraphEngine, entailments: " + graph.getEntailment());
        Load ld = Load.create(graph);
        logger.info("Initialized GraphEngine: " + sw.getTime() + " ms");

        sw.reset();
        sw.start();
        try {
            ld.parseDir(TestDQP.class.getClassLoader().getResource("demographie").getPath() + "/cog-2012.ttl");
        } catch (LoadException ex) {
            LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
        }

        try {
            ld.parseDir(TestDQP.class.getClassLoader().getResource("demographie").getPath() + "/popleg-2010.ttl");
        } catch (LoadException ex) {
            LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
        }

        logger.info("Graph size: " + graph.size());

        for (String q : queries.values()) {
            logger.info("Querying with : \n" + q);
            for (int i = 0; i < 10; i++) {
                sw.reset();
                sw.start();
                Mappings results = exec.query(q);
                logger.info(results.size() + " results: " + sw.getTime() + " ms");
            }
        }
    }

    public void testDQP(String testCase) throws EngineException, MalformedURLException {
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);
        execDQP.setGroupingEnabled(true);

//      Mode BGP or not
        if (modeBGP) {
            execDQP.setPlanProfile(Query.QP_BGP);
        }

//      DUPLICATED DATA
        if (testCase.equals("d")) {
            execDQP.addRemote(new URL("http://" + host + ":8081/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8082/sparql"), WSImplem.REST);
        }

//      GLOBAL BGP
        if (testCase.equals("g")) {
            execDQP.addRemote(new URL("http://" + host + ":8083/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8084/sparql"), WSImplem.REST);
        }

//      Partial BGP and AND Lock
        if (testCase.equals("p")) {
            execDQP.addRemote(new URL("http://" + host + ":8085/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8086/sparql"), WSImplem.REST);
            execDQP.addRemote(new URL("http://" + host + ":8087/sparql"), WSImplem.REST);
        }
//      Demographic
        execDQP.addRemote(new URL("http://" + host + ":8088/sparql"), WSImplem.REST);

        for (Map.Entry<String, String> query : queries.entrySet()) {
//            try {
//                String resultFileName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey() + "Result";
//                String valuesFileName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey() + "Values";
//                File resultFile = new File(resultFileName);
//                File valuesFile = new File(valuesFileName);
//
//                if (modeBGP) {
////                    resultFileName += "H" + round + ".txt";
////                    valuesFileName += "H" + round + ".csv";
//                    resultFileName += "H.txt";
//                    valuesFileName += "H.csv";
//                    resultFile = new File(resultFileName);
//                    valuesFile = new File(valuesFileName);
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile;
//                    try {
//                        writeValuesFile = new FileWriter(valuesFile,true);
//                        BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
////                        if(!valuesFile.exists())
//                            bufferValuesFile.write("BGPs, Edges, Query, Results, Execution, Remote, Intermediate\n");
//                        bufferValuesFile.flush();
//                        writeValuesFile.close();
//                    } catch (IOException ex) {
//                        LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
//                    }
//                } else {
////                    resultFileName += "D" + round + ".txt";
////                    valuesFileName += "D" + round + ".csv";
//                    
//                    resultFileName += "D.txt";
//                    valuesFileName += "D.csv";
//                    
//                    resultFile = new File(resultFileName);
//                    valuesFile = new File(valuesFileName);
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile;
//                    try {
//                        writeValuesFile = new FileWriter(valuesFile,true);
//                        BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
////                        if(!valuesFile.exists())
//                            bufferValuesFile.write("Edges, Query, Results, Execution, Remote, Intermediate\n");
//                        bufferValuesFile.flush();
//                        writeValuesFile.close();
//                    } catch (IOException ex) {
//                        LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
//                    }
//                }
//
//                
////            for (int i = 0; i < 1; i++) {
//                logger.setAdditivity(false);
//                try {
////                    TO FIX
////                    String path = TestDQP.class.getClassLoader().getResource("test").getPath()+query.getKey()+"/log"+i+".html";
////                    System.out.println("?? "+path);
//                    String logName = "/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/" + query.getKey() + "/" + query.getKey();
//                    if (modeBGP) {
//                        logName += "H" + round + ".xml";
//                    } else {
//                        logName += "D" + round + ".xml";
//                    }
//                    FileAppender fa = new FileAppender(new XMLLayout(), logName, false);
//
//                    logger.addAppender(fa);
//
//                } catch (IOException ex) {
//                    LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
//                }

                StopWatch sw = new StopWatch();
                sw.start();
                Mappings map = execDQP.query(query.getValue());
                sw.stop();
                logger.info(map.size() + " results in " + sw.getTime() + " ms");
                logger.info("\n" + map.toString());
                logger.info(Messages.countQueries);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
                logger.info(Messages.countTransferredResults);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
                logger.info(Messages.countDS);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
                logger.info(Messages.countTransferredResultsPerSource);
                logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceVolumeCounter));

//                try {
//                    //To put results (mappings values) in a .txt file
//                    FileWriter writeResultFile = new FileWriter(resultFile, false);
//                    BufferedWriter bufferResultFile = new BufferedWriter(writeResultFile);
//                    bufferResultFile.write(map.toString());
//                    bufferResultFile.flush();
//                    bufferResultFile.close();
//                    writeResultFile.close();
//
//                    //To put values (execution time , final results, etc. => in a .csv file)
//                    FileWriter writeValuesFile = new FileWriter(valuesFile, true);
//                    BufferedWriter bufferValuesFile = new BufferedWriter(writeValuesFile);
//                    bufferValuesFile.write(round + "," + map.size() + "," + sw.getTime() + "," + Util.sum(QueryProcessDQP.queryCounter) + "," + Util.sum(QueryProcessDQP.queryVolumeCounter) + "," + "\n");
//                    bufferValuesFile.flush();
//                    writeValuesFile.close();
//                } catch (IOException e) {
//                    LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", e);
//                }

//            } catch (InterruptedException ex) {
//                LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
//            }
        }

    }

    public void setRound(int round) {
        this.round = round;
    }

    public static void main(String[] args) throws EngineException, MalformedURLException {

        Options options = new Options();
        Option bgpOpt = new Option("bgp", "modeBGP", false, "specify the evaluation strategy");
        Option helpOpt = new Option("h", "help", false, "print this message");
        Option centralizeOpt = new Option("c", "centralize", false, "to evualuate in a centralized context");
        Option testCaseOpt = new Option("tc", "testCase", true, "chose the test case ( d, g or p)");
        Option roundOpt = new Option("r", "round", true, "the roound of the test ( 0, 1 ..., n)");

        options.addOption(bgpOpt);
        options.addOption(helpOpt);
        options.addOption(centralizeOpt);
        options.addOption(testCaseOpt);
        options.addOption(roundOpt);

        String header = "blabla";
        String footer = "\nPlease report any issue to macina@i3s.unice.fr";

        TestDQP test = new TestDQP(false);

        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("kgdqp", header, options, footer, true);
                System.exit(0);
            }

            if (cmd.hasOption("bgp")) {
                test = new TestDQP(true);
            }

            if (cmd.hasOption("r")) {
                test.setRound(Integer.parseInt(cmd.getOptionValue("r")));
            }

            if (cmd.hasOption("c")) {
                test.testLocal();
            } else {

                if (cmd.hasOption("tc")) {
                    String tetsCase = cmd.getOptionValue("tc");
                    test.testDQP(tetsCase);
                }
            }

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        } catch (IOException ex) {
            LogManager.getLogger(TestDQP.class.getName()).log(Level.ERROR, "", ex);
        }
    }

}
