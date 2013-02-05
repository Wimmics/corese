package test.distribution;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgimport.JenaGraphFactory;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
    @Ignore
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

        ArrayList<File> toLoad = new ArrayList<File>();
        toLoad.add(new File("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-dbpedia-1/article_categories_en.nt"));
        toLoad.add(new File("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-drugbank/drugbank_dump.nt"));
//        toLoad.add(new File("/Users/gaignard/Documents/These/ExperimentsG5K/FedBench-dataset/dataset-chebi/chebi.nt"));
        for (File f : toLoad) {
            logger.info("Loading "+f.getAbsolutePath());
            String path = f.getAbsolutePath();
            if (path.endsWith(".nt")) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(f);
                    Model model = ModelFactory.createDefaultModel();
                    model.read(fis, null, "N-TRIPLE");
                    JenaGraphFactory.updateGraph(model, graph);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        logger.info("Graph (" + graph.size() + ") loaded: " + sw.getTime() + " ms");
        System.out.println("");

        logger.info("Querying with : \n" + sparqlQuery);
        for (int i = 0; i < 5; i++) {
            sw.reset();
            sw.start();
            Mappings results = exec.query(sparqlQuery);
            logger.info(results.size() + " results: " + sw.getTime() + " ms");
        }
        System.out.println("");

        logger.info("Querying with : \n" + edgeSelect);
        for (int i = 0; i < 5; i++) {
            sw.reset();
            sw.start();
            Mappings results = exec.query(edgeSelect);
            logger.info(results.size() + " results: " + sw.getTime() + " ms");
        }
        System.out.println("");

        logger.info("Querying with : \n" + edgeConstruct);
        for (int i = 0; i < 5; i++) {
            sw.reset();
            sw.start();
            Mappings results = exec.query(edgeConstruct);
            logger.info(results.size() + " results: " + sw.getTime() + " ms");
        }
        System.out.println("");

        logger.info("Querying with : \n" + edgeConstruct2);
        for (int i = 0; i < 5; i++) {
            sw.reset();
            sw.start();
            Mappings results = exec.query(edgeConstruct2);
            logger.info(results.size() + " results: " + sw.getTime() + " ms");
        }
        System.out.println("");
    }
}
