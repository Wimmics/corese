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
   


    ArrayList<String> queries = new ArrayList<String>();
    public TestDQP() {
        
    String simple = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "    ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement ." +
    "     ?departement igeo:nom ?nom .  " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale ." +
    "} ORDER BY ?popTotale";
           
    String optional = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "    ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement ." +
    "     OPTIONAL { ?departement igeo:nom ?nom . } " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale ." +
    "} ORDER BY ?popTotale";
    
    
    String union = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "    { ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement ." +
    "     ?departement igeo:nom ?nom . } UNION { " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . } " +
    "} ORDER BY ?popTotale";
    
    
     String subQuery = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "      ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement ." +
    "     { Select * where {?departement igeo:nom ?nom . } }  " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . " +
    "} ORDER BY ?popTotale";
    
    
    
     String minus = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "   {  ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement . }" +
    "     minus {?departement igeo:nom ?nom . }  " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . " +
    "} ORDER BY ?popTotale";
    
    
    
       String limit = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "   ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement . " +
    "    ?departement igeo:nom ?nom .   " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . " +
    "} LIMIT 2 ";
    
    
     String filters = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "   ?region igeo:codeRegion ?v ." +
    "    ?region igeo:subdivisionDirecte ?departement . " +
    "    ?departement igeo:nom ?nom .   " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . " +
    "FILTER (?v = \"24\")" +
    "FILTER (?nom = \"Loir-et-Cher\")" +
    "} ORDER BY ?popTotale";
     
     
     String combination = "PREFIX idemo:<http://rdf.insee.fr/def/demo#>" +
    "PREFIX igeo:<http://rdf.insee.fr/def/geo#>" +
    "SELECT ?nom ?popTotale  WHERE { " +
    "   ?region igeo:codeRegion ?v ." +
    "    { Select * where { ?region igeo:subdivisionDirecte ?departement .} } " +
    "     OPTIONAL { ?departement igeo:nom ?nom . } " +
    "    ?departement idemo:population ?popLeg ." +
    "    ?popLeg idemo:populationTotale ?popTotale . " +
    "FILTER (?v = \"24\")" +
    "} ORDER BY ?popTotale";
     
     queries.add(simple); //OK
//     queries.add(optional);//OK
//     queries.add(minus);//OK
//     queries.add(union);//OK
//     queries.add(filters);//OK
//     queries.add(subQuery);//OK but processed as AND by default  because EDGES + SUBQUERY is not an AND BGP-able
//     //when method putFreeEdgesInBGP is used => duplicated result TO FIX
//     queries.add(combination); //OK but process as AND by default: BGP is not relevant in this case
     
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


//      DUPLICATED DATA
//        execDQP.addRemote(new URL("http://"+host+":8081/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8082/sparql"), WSImplem.REST);
//        

//      GLOBAL BGP
//        execDQP.addRemote(new URL("http://"+host+":8083/sparql"), WSImplem.REST);
//        execDQP.addRemote(new URL("http://"+host+":8084/sparql"), WSImplem.REST);
//        
//        
//      Partial BGP and AND Lock
        execDQP.addRemote(new URL("http://"+host+":8085/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8086/sparql"), WSImplem.REST);
        execDQP.addRemote(new URL("http://"+host+":8087/sparql"), WSImplem.REST);

        
        
//      Demographic
        execDQP.addRemote(new URL("http://"+host+":8088/sparql"), WSImplem.REST);
        
        StopWatch sw = new StopWatch();
        sw.start();
        for(String query : queries){
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
    }

     public static void main(String[] args) throws EngineException, MalformedURLException {
        try {
            TestDQP test = new TestDQP();
            
            test.testLocal();
//            test.testDQP();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(TestDQP.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
}
