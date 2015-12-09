/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.bgp;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.Messages;
import fr.inria.edelweiss.kgdqp.core.ProviderImplCostMonitoring;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.Util;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 *
 * @author macina
 */
public class TestDQP {
    

    private Logger logger = Logger.getLogger(TestDQP.class);
    
    static final String host = "localhost";
    
    String query = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "    ?region igeo:codeRegion \"24\" ." +
    "    ?region igeo:subdivisionDirecte ?departement ." +
    "    ?departement igeo:nom ?nom . " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale ." +
    "} ORDER BY ?popTotale";

    
    public TestDQP() {
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

        ld.load(TestDQP.class.getClassLoader().getResource("demographie").getPath()+"/cog-2012.ttl");
        ld.load(TestDQP.class.getClassLoader().getResource("demographie").getPath()+"/popleg-2010.ttl");

        logger.info("Graph size: " + graph.size());

        ArrayList<String> queries = new ArrayList<String>();
        queries.add(query);

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
    
    public void testDQP() throws EngineException, MalformedURLException{
        Graph graph = Graph.create(false);
        ProviderImplCostMonitoring sProv = ProviderImplCostMonitoring.create();
        QueryProcessDQP execDQP = QueryProcessDQP.create(graph, sProv, true);
        execDQP.setGroupingEnabled(true);


//      ONE BGP
        execDQP.addRemote(new URL("http://"+host+":8081/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8082/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8083/sparql"), WSImplem.REST);
//        
////        //join variables
//        execDQP.addRemote(new URL("http://"+host+":8084/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8085/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8086/sparql"), WSImplem.REST);
//        
//        //partial BGP and AND
//        execDQP.addRemote(new URL("http://"+host+":8087/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8088/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8089/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8090/sparql"), WSImplem.REST);

        
//        //demographic
        execDQP.addRemote(new URL("http://"+host+":8091/sparql"), WSImplem.REST);
        
        StopWatch sw = new StopWatch();
        sw.start();
        Mappings map = execDQP.query(query);
        logger.info(map.size() + " results in " + sw.getTime() + " ms");
        logger.info("\n"+map.toString());
        logger.info(Messages.countQueries);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryCounter));
        logger.info(Messages.countTransferredResults);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.queryVolumeCounter));
        logger.info(Messages.countDS);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceCounter));
        logger.info(Messages.countTransferredResultsPerSource);
        logger.info(Util.prettyPrintCounter(QueryProcessDQP.sourceVolumeCounter));
    }

     public static void main(String[] args) throws EngineException, MalformedURLException {
        try {
            TestDQP test = new TestDQP();
//            test.testLocal();
            test.testDQP();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
}
