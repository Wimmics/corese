package test.distribution;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.Messages;
import fr.inria.edelweiss.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgdqp.strategies.ServiceGrouper;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Alban Gaignard <alban.gaignard@cnrs.fr>
 */
public class RestServiceGroupingTest {

    Logger logger = LogManager.getLogger(RestServiceGroupingTest.class);

    String ginsengQuery1 = "PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n"
            + "SELECT ?codePostal (count(*) as ?total) WHERE { \n"
            + "    ?cv semehr:value \"BHGSA5B0\"^^xsd:string . \n"
            + "    ?patient semehr:hasMedicalBag ?bag .\n"
            + "    ?bag semehr:hasMedicalEvent ?evt .\n"
            + "    ?evt semehr:hasClinicalVariable ?cv . \n"
            + "    ?patient semehr:address ?addr .\n"
            + "    ?addr semehr:postalCode ?codePostal . \n"
            + "} GROUP BY ?codePostal ORDER BY desc(?total)";

    String ginsengDBPediaQuery = "PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n"
            + "SELECT (count(distinct ?patient) as ?nbPatients)  (sum(?pop) as ?totalPop) ?postalCode (count(distinct ?patient)*10000/sum(?pop) as ?occurencePer10k) WHERE { \n"
            + "    ?cv semehr:value \"BHGSA5B0\"^^xsd:string . \n"
            + "    ?patient semehr:hasMedicalBag/semehr:hasMedicalEvent/semehr:hasClinicalVariable ?cv . \n"
            + "    ?patient semehr:address/semehr:postalCode ?postalCode . \n"
            + "\n"
            + "    SERVICE <http://fr.dbpedia.org/sparql> { \n"
            + "        SELECT DISTINCT (str(?cp) as ?postalCode) ?pop WHERE { \n"
            + "            ?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
            + "            ?s dbpedia-owl:postalCode ?cp . \n"
            + "            ?s dbpedia-owl:populationTotal ?pop \n"
            + "        } \n"
            + "    } \n"
            + "} GROUP BY  ?postalCode ORDER BY desc(?occurencePer10k)";

    String ginsengDBPediaQueryWithoutPath = "PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n"
            + "SELECT (count(distinct ?patient) as ?nbPatients)  (sum(?pop) as ?totalPop) ?postalCode (count(distinct ?patient)*10000/sum(?pop) as ?occurencePer10k) WHERE { \n"
            + "    ?cv semehr:value \"BHGSA5B0\"^^xsd:string . \n"
            + "    ?patient semehr:hasMedicalBag ?bag .\n"
            + "    ?bag semehr:hasMedicalEvent ?evt .\n"
            + "    ?evt semehr:hasClinicalVariable ?cv . \n"
            + "    ?patient semehr:address ?addr .\n"
            + "    ?addr semehr:postalCode ?codePostal . \n"
            + "\n"
            + "    SERVICE <http://fr.dbpedia.org/sparql> { \n"
            + "        SELECT DISTINCT (str(?cp) as ?postalCode) ?pop WHERE { \n"
            + "            ?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
            + "            ?s dbpedia-owl:postalCode ?cp . \n"
            + "            ?s dbpedia-owl:populationTotal ?pop \n"
            + "        } \n"
            + "    } \n"
            + "} GROUP BY  ?postalCode ORDER BY desc(?occurencePer10k)";

    String ginsengDBPediaSimpleQuery = "PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n"
            + "SELECT * WHERE { \n"
            + "    ?cv semehr:value \"BHGSA5B0\"^^xsd:string . \n"
            + "    ?patient semehr:hasMedicalBag ?bag . \n"
            + "    ?bag semehr:hasMedicalEvent ?evt . \n"
            + "    ?evt semehr:hasClinicalVariable ?cv . \n"
            + "    ?patient semehr:address ?addr . \n"
            + "    ?addr semehr:postalCode ?codePostal . \n"
            + "    ?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
            + "    ?s dbpedia-owl:postalCode ?cp . \n"
            + "    ?s dbpedia-owl:populationTotal ?pop \n"
            + "} ";

    String ginsengDBPediaUnionQuery = "PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n"
            + "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n"
            + "SELECT * WHERE { \n"
            + " { "
            + "    ?cv semehr:value \"BHGSA5B0\"^^xsd:string . \n"
            + "    ?patient semehr:hasMedicalBag ?bag . \n"
            + "    ?bag semehr:hasMedicalEvent ?evt . \n"
            + "    ?evt semehr:hasClinicalVariable ?cv . \n"
            + "    ?patient semehr:address ?addr . \n"
            + "    ?addr semehr:postalCode ?codePostal . "
            + " } UNION { \n"
            + "    ?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n"
            + "    ?s dbpedia-owl:postalCode ?cp . \n"
            + "    ?s dbpedia-owl:populationTotal ?pop } \n"
            + "} ";

    String inseeQuery = "PREFIX idemo:<http://rdf.insee.fr/def/demo#> \n"
            + "PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n"
            + "SELECT ?nom ?popTotale WHERE { \n"
            + "    ?region igeo:codeRegion \"24\" .\n"
            + "    ?region igeo:subdivisionDirecte ?departement .\n"
            + "    ?departement igeo:nom ?nom .\n"
            + "    ?departement idemo:population ?popLeg .\n"
            + "    ?popLeg idemo:populationTotale ?popTotale .\n"
            + "} ORDER BY ?popTotale";
    
    String inseeQueryEasy = "PREFIX idemo:<http://rdf.insee.fr/def/demo#> \n"
            + "PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n"
            + "SELECT ?nom ?popTotale WHERE { \n"
            + "    ?region igeo:codeRegion \"24\" .\n"
            + "    ?region igeo:subdivisionDirecte ?departement .\n"
//            + "    ?departement igeo:nom ?nom .\n"
            + "    ?departement idemo:population ?popLeg .\n"
            + "    ?popLeg idemo:populationTotale ?popTotale .\n"
            + "} ORDER BY ?popTotale";

    public RestServiceGroupingTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
//    @Ignore
    public void queryRewritingTest() throws MalformedURLException, EngineException {
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, false);
        ServiceGrouper sg = execDQP.setGroupingEnabled(true);
        execDQP.addRemote(new URL("http://localhost:9091/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://localhost:9092/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://fr.dbpedia.org/sparql"), WSImplem.REST);

        Query q = execDQP.compile(inseeQuery);
//        Query q = execDQP.compile(inseeQueryEasy);
//        Query q = execDQP.compile(ginsengDBPediaSimpleQuery);
        sg.visit(q);
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
//    @Ignore
    public void dqpGroupTest() throws MalformedURLException, EngineException {
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, false);
        execDQP.setGroupingEnabled(true);
        execDQP.addRemote(new URL("http://localhost:9091/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://localhost:9092/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://fr.dbpedia.org/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
//        Mappings map = execDQP.query(ginsengQuery1);
//        Mappings map = execDQP.query(ginsengDBPediaUnionQuery);
//        Mappings map = execDQP.query(ginsengDBPediaSimpleQuery);
        Mappings map = execDQP.query(ginsengDBPediaQueryWithoutPath);
        logger.info(map.size() + " results in " + sw.getTime() + " ms");
        logCost();
    }

    private void logCost() {
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
