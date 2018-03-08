/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.corese.kgdqp.core.QueryProcessDQP;
import fr.inria.corese.kgdqp.core.Util;
import fr.inria.corese.kgdqp.core.WSImplem;
import fr.inria.corese.kgdqp.strategies.ServiceGrouper;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class ServiceGroupingJows2013Test {

    final int nloop = 5;
    final int expectedResults = 53;
    static final String host = "localhost";
//    static final String host = "nyx.unice.fr";
    static String LS1 = "SELECT $drug $melt WHERE {"
            + "{ $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/meltingPoint> $melt. }"
            + "UNION"
            + "{ $drug <http://dbpedia.org/ontology/Drug/meltingPoint> $melt . }"
            + "}";

    static String LS2 = "SELECT ?predicate ?object WHERE {\n"
            + "    { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }\n"
            + "    UNION    \n"
            + "    { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff .\n"
            + "      ?caff ?predicate ?object . } \n"
            + "}";

    static String LS3 = "SELECT ?Drug ?IntDrug ?IntEffect WHERE {\n"
            + "    ?Drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Drug> .\n"
            + "    ?Drug <http://www.w3.org/2002/07/owl#sameAs> ?y .\n"
            + "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug1> ?y .\n"
            + "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/interactionDrug2> ?IntDrug .\n"
            + "    ?Int <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/text> ?IntEffect . \n"
            + "}";

    static String LS4 = "SELECT ?drugDesc ?cpd ?equation WHERE {"
            + "     ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> ."
            + "     ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd ."
            + "     ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc ."
            + "     ?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd ."
            + "     ?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> ."
            + "     ?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme ."
            + "     ?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . "
            + "}"
            + "PRAGMA {kg:service kg:slice 50}";
    static String LS5 = "SELECT $drug $keggUrl $chebiDefinition WHERE {"
            + "     $drug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs> ."
            + "     $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> $keggDrug ."
            + "     $keggDrug <http://bio2rdf.org/ns/bio2rdf#url> $keggUrl ."
            + "     $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/genericName> $drugBankName ."
            + "     $chebiDrug <http://www.w3.org/2004/02/skos/core#altLabel> $drugBankName ."
            + "     $chebiDrug <http://www.w3.org/2004/02/skos/core#definition> $chebiDefinition ."
            + "}"
            + "PRAGMA {kg:service kg:slice 50}";
    static String LS6 = "SELECT ?drug ?title WHERE { "
            + "     ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/micronutrient> ."
            + "     ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> ?id ."
            + "     ?keggDrug <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Drug> ."
            + "     ?keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> ?id ."
            + "     ?keggDrug <http://purl.org/dc/elements/1.1/title> ?title ."
            + "}"
            + "PRAGMA {kg:service kg:slice 50}";
    static String LS7 = "SELECT $drug $transform $mass WHERE {  "
            + "{    $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/affectedOrganism>  'Humans and other mammals'."
            + "     $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/casRegistryNumber> $cas ."
            + "     $keggDrug <http://bio2rdf.org/ns/bio2rdf#xRef> $cas ."
            + "     $keggDrug <http://bio2rdf.org/ns/bio2rdf#mass> $mass"
            + "         FILTER ( $mass > '5' )"
            + "} "
            + "     OPTIONAL { $drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/biotransformation> $transform . } "
            + "}";

    public ServiceGroupingJows2013Test() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void rewritingTest() throws MalformedURLException, EngineException {

//        String query = LS1; //TODO OK 
//        String query = LS2; //TODO OK 
//        String query = LS3; //TODO fix rdf:type and sameAs predicates -> DONE
//        String query = LS4; //TODO OK
//        String query = LS5; //TODO OK
//        String query = LS6; //TODO OK
        String query = LS7; //TODO OK
        
        Graph g = Graph.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(g);
        execDQP.addVisitor(new ServiceGrouper(execDQP));
        ProviderImplCostMonitoring p = ProviderImplCostMonitoring.create();
        execDQP.set(p);

//        execDQP.setOptimize(false);
        execDQP.addRemote(new URL("http://" + host + ":8080/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://" + host + ":8081/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://" + host + ":8082/kgram/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://" + host + ":8083/kgram/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps = execDQP.query(query);
        System.out.println(maps);
    }

    @Test
    @Ignore
    public void serviceGroupingFuseki() throws EngineException, MalformedURLException, IOException {
        String query = LS6;

        System.out.println("============= REST service grouping ==============");
        System.out.println(query);

        //---------------Service grouping-----------------------
        Graph g1 = Graph.create();
        QueryProcessDQP execDQP1 = QueryProcessDQP.create(g1);
        execDQP1.addVisitor(new ServiceGrouper(execDQP1));
        ProviderImplCostMonitoring p = ProviderImplCostMonitoring.create();
//        p.set("http://" + host + ":3030/KEGG", 1.1);
//        p.set("http://" + host + ":3030/drugbank", 1.1);
//        p.set("http://" + host + ":3030/chebi", 1.1);

        execDQP1.set(p);
//        execDQP.setOptimize(false);
        execDQP1.addRemote(new URL("http://" + host + ":3030/KEGG/sparql"), WSImplem.REST);
        execDQP1.addRemote(new URL("http://" + host + ":3030/drugbank/sparql"), WSImplem.REST);
        execDQP1.addRemote(new URL("http://" + host + ":3030/chebi/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps1 = execDQP1.query(query);
//        System.out.println(maps1);
        System.out.println("[REST service grouping] Results size " + maps1.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
//        assertEquals(expectedResults, maps1.size());

        System.out.println("");
        System.out.println("***********************************************************");
        System.out.println("***********************************************************");
        System.out.println("Remote queries");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        System.out.println("Transferred results per query");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        System.out.println("Remote queries per data source");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));

        QueryProcessDQP.queryCounter.clear();
        QueryProcessDQP.queryVolumeCounter.clear();
        QueryProcessDQP.sourceCounter.clear();

        //---------------No service grouping-----------------------
        Graph g2 = Graph.create();
        QueryProcessDQP execDQP2 = QueryProcessDQP.create(g2);
        execDQP2.addRemote(new URL("http://" + host + ":3030/KEGG/sparql"), WSImplem.REST);
        execDQP2.addRemote(new URL("http://" + host + ":3030/drugbank/sparql"), WSImplem.REST);
        execDQP2.addRemote(new URL("http://" + host + ":3030/chebi/sparql"), WSImplem.REST);

        sw = new StopWatch();
        sw.start();
        Mappings maps2 = execDQP2.query(query);
        System.out.println("[REST no service grouping] Results size " + maps2.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
        System.out.println("");
        System.out.println("***********************************************************");
        System.out.println("***********************************************************");
        System.out.println("Remote queries");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        System.out.println("Transferred results per query");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        System.out.println("Remote queries per data source");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
    }

    @Test
    @Ignore
    public void manualServiceGroupingFuseki() throws EngineException, MalformedURLException, IOException {
        String queryDrugbankKeggType = "SELECT ?drugDesc ?cpd ?equation WHERE {\n"
                + "    SERVICE <http://localhost:3030/drugbank> {\n"
                + "        {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd . }\n"
                + "    }\n"
                + "    SERVICE <http://localhost:3030/KEGG> {\n"
                + "        {?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . }\n"
                + "    }\n"
                + "    ?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> . \n"
                + "}\n"
                + "PRAGMA {kg:service kg:slice 50}";

        String queryKeggDrugbankType = "SELECT ?drugDesc ?cpd ?equation WHERE {\n"
                + "    SERVICE <http://localhost:3030/KEGG> {\n"
                + "        {?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . }\n"
                + "    }\n"
                + "    SERVICE <http://localhost:3030/drugbank> {\n"
                + "        {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd . }\n"
                + "    }\n"
                + "    ?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> . \n"
                + "}";
//                + "PRAGMA {kg:service kg:slice 50}";

        String queryDrugbankTypeKegg = "SELECT ?drugDesc ?cpd ?equation WHERE {\n"
                + "    SERVICE <http://localhost:3030/drugbank> {\n"
                + "        {?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/description> ?drugDesc . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugCategory> <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugcategory/cathartics> . \n"
                + "        ?drug <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/keggCompoundId> ?cpd . }\n"
                + "    }\n"
                + "    ?enzyme <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/ns/kegg#Enzyme> . \n"
                + "    SERVICE <http://localhost:3030/KEGG> {\n"
                + "        {?enzyme <http://bio2rdf.org/ns/kegg#xSubstrate> ?cpd . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#xEnzyme> ?enzyme . \n"
                + "        ?reaction <http://bio2rdf.org/ns/kegg#equation> ?equation . }\n"
                + "    }\n"
                + "}"
                + "PRAGMA {kg:service kg:slice 50}";

        String query = queryKeggDrugbankType;

        System.out.println("============= REST service grouping ==============");
        System.out.println(query);

        //---------------Service grouping-----------------------
        Graph g1 = Graph.create();
        QueryProcessDQP execDQP1 = QueryProcessDQP.create(g1);
//        execDQP1.addVisitor(new ServiceQueryVisitorPar(execDQP1));
        ProviderImplCostMonitoring p = ProviderImplCostMonitoring.create();
//        p.set("http://" + host + ":3030/KEGG", 1.1);
//        p.set("http://" + host + ":3030/drugbank", 1.1);
//        p.set("http://" + host + ":3030/chebi", 1.1);

        execDQP1.set(p);
//        execDQP.setOptimize(false);
        execDQP1.addRemote(new URL("http://" + host + ":3030/KEGG/sparql"), WSImplem.REST);
        execDQP1.addRemote(new URL("http://" + host + ":3030/drugbank/sparql"), WSImplem.REST);
        execDQP1.addRemote(new URL("http://" + host + ":3030/chebi/sparql"), WSImplem.REST);

        StopWatch sw = new StopWatch();
        sw.start();
        Mappings maps1 = execDQP1.query(query);
        System.out.println(maps1);
        System.out.println("[REST service grouping] Results size " + maps1.size() + " in " + sw.getTime() + " ms");
        sw.stop();
        sw.reset();
//        assertEquals(expectedResults, maps1.size());

        System.out.println("");
        System.out.println("***********************************************************");
        System.out.println("***********************************************************");
        System.out.println("Remote queries");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        System.out.println("Transferred results per query");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        System.out.println("Remote queries per data source");
        System.out.println(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
    }

    public Float average(ArrayList<Long> values) {
        Float av = new Float(0);

        for (Long v : values) {
            av += v;
        }
        av = av / values.size();
        return av;
    }
}
