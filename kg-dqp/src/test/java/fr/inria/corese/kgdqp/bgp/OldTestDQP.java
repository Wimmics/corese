/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.bgp;

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
import java.util.ArrayList;
import org.apache.logging.log4j.Level;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author macina
 */
public class OldTestDQP {

    private Logger logger = LogManager.getLogger(OldTestDQP.class);

    static final String host = "localhost";

    ArrayList<String> queries = new ArrayList<String>();

    public OldTestDQP() {

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
                + "} ";

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

        String limit = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?nom ?popTotale  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement . "
                + "    ?departement igeo:nom ?nom .   "
                + "    ?departement idemo:population ?popLeg ."
                + "    ?popLeg idemo:populationTotale ?popTotale . "
                + "} LIMIT 2 ";

        String filters = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>"
                + "PREFIX igeo:<http://rdf.insee.fr/def/geo#>"
                + "SELECT ?arrondissement ?cantonNom  WHERE { "
                + "   ?region igeo:codeRegion ?v ."
                + "    ?region igeo:subdivisionDirecte ?departement . "
                + "    ?departement igeo:nom ?nom .   "
                + "   ?departement igeo:subdivisionDirecte ?arrondissement . "
                + "   ?arrondissement igeo:subdivisionDirecte ?canton . "
                + "    ?canton igeo:nom ?cantonNom .   "
                + "FILTER (?v = \"11\")"
                + "FILTER (?cantonNom = \"Paris 14e  canton\")"
                + "} ORDER BY ?popTotale";

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

     queries.add(simple); //OK
//     queries.add(minus);//OK
//     queries.add(union);//OK
//     queries.add(filters);//OK
//     queries.add(optional);//OK???
//        queries.add(all);//


//     queries.add(subQuery);//?? but processed as AND by default  because EDGES + SUBQUERY is not an AND BGP-able
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
            ld.parseDir(OldTestDQP.class.getClassLoader().getResource("demographie").getPath() + "/cog-2012.ttl");
        } catch (LoadException ex) {
            LogManager.getLogger(OldTestDQP.class.getName()).log(Level.ERROR, "", ex);
        }

        try {
            ld.parseDir(OldTestDQP.class.getClassLoader().getResource("demographie").getPath() + "/popleg-2010.ttl");
        } catch (LoadException ex) {
            LogManager.getLogger(OldTestDQP.class.getName()).log(Level.ERROR, "", ex);
        }

        logger.info("Graph size: " + graph.size());

        for (String q : queries) {
            logger.info("Querying with : \n" + q);
            for (int i = 0; i < 10; i++) {
                sw.reset();
                sw.start();
                Mappings results = exec.query(q);
                logger.info(results.size() + " results: " + sw.getTime() + " ms");
            }
        }
    }

    public void testDQP() throws EngineException, MalformedURLException {
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);
        execDQP.setGroupingEnabled(true);
        
        execDQP.setPlanProfile(Query.QP_BGP);

//      DUPLICATED DATA
//        execDQP.addRemote(new URL("http://"+host+":8081/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8082/sparql"), WSImplem.REST);
        
//      GLOBAL BGP
//        execDQP.addRemote(new URL("http://" + host + ":8083/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://" + host + ":8084/sparql"), WSImplem.REST);
        
        
//      Partial BGP and AND Lock
        execDQP.addRemote(new URL("http://"+host+":8085/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8086/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8087/sparql"), WSImplem.REST);

//      Demographic
        execDQP.addRemote(new URL("http://" + host + ":8088/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        for (String query : queries) {
            Mappings map = execDQP.query(query);
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
        }
    }

    public static void main(String[] args) throws EngineException, MalformedURLException {
        try {
            OldTestDQP test = new OldTestDQP();

//            test.testLocal();
            test.testDQP();
        } catch (IOException ex) {
            LogManager.getLogger(OldTestDQP.class.getName()).log(Level.ERROR, "", ex);
        }
    }
}
