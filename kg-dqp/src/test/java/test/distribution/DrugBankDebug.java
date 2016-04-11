package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gaignard
 */
public class DrugBankDebug {

    private Logger logger = Logger.getLogger(DrugBankDebug.class);
    String sparqlQuery = "SELECT ?predicate ?object WHERE {"
            + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }"
            + " UNION    "
            + "{    <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.w3.org/2002/07/owl#sameAs> ?caff ."
            + "     ?caff ?predicate ?object . } "
            + "}";
    String edgeSelect = "SELECT ?predicate ?object WHERE { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object . }";
//    String edgeSelect = "SELECT ?predicate ?object WHERE { <http://dbpedia.org/resource/Channel_Tunnel> ?predicate ?object . }";  
    String edgeConstruct = "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object } "
            + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> <http://www.inria.fr/acacia/corese#Property> ?object .}";
    String edgeConstruct2 = "construct  { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object } "
            + "where { <http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00201> ?predicate ?object .}";

    public DrugBankDebug() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:

    @Test
    public void drugBankQuery() throws EngineException, MalformedURLException, IOException {
        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        StopWatch sw = new StopWatch();
        sw.start();
        logger.info("Initializing GraphEngine, entailments: " + graph.getEntailment());
        Load ld = Load.create(graph);
        logger.info("Initialized GraphEngine: " + sw.getTime() + " ms");

        sw.reset();
        sw.start();
//        
//        File rep = new File("/Users/gaignard/Desktop/Expe-FedEx-FedBench-G5K/updated-datasets/KEGG-2010-11");
////        ld.parseDir(rep.getAbsolutePath());
//
//        for (File f : rep.listFiles()) {
//            if (f.getAbsolutePath().endsWith(".rdf")) {
//                final String path = f.getAbsolutePath();
//                ld.parseDir(path);
//                logger.info(path + " loaded -> Graph size " + graph.size());
//            }
//        }
        try {
            //         ld.parseDir("/Users/gaignard/Desktop/producer-4/drugbank_dump.rdf"); // 300ms
            ld.parseDir("/Users/gaignard/Desktop/Expe-FedEx-FedBench-G5K/updated-datasets/drugbank_dump.ttl"); // 300ms
        } catch (LoadException ex) {
            java.util.logging.Logger.getLogger(DrugBankDebug.class.getName()).log(Level.SEVERE, null, ex);
        }

//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/category_labels_en.ttl"); // 300ms
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/DBpedia-LGD.ttl"); //  20 ms 
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/geo_coordinates_en.ttl"); //  800ms (1.5M) 
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/persondata_en.ttl"); //  140 ms (350K) 
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/images_en.ttl"); //  2300ms (4M) 
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-3/instance_types_en.ttl"); // 1393 ms (5.5M) 
//        ld.parseDir("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-1/article_categories_en.ttl"); // 4487 ms (10M)

        System.out.println("Graph size: " + graph.size());

        ArrayList<String> queries = new ArrayList<String>();
//        queries.add(sparqlQuery);
        queries.add(edgeSelect);
//        queries.add(edgeConstruct);
//        queries.add(edgeConstruct2);

        for (String query : queries) {
            logger.info("Querying with : \n" + query);
            for (int i = 0; i < 10; i++) {
                sw.reset();
                sw.start();
                Mappings results = exec.query(query);
                logger.info(results.size() + " results: " + sw.getTime() + " ms");
            }
            System.out.println("");
        }
    }
}
