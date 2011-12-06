/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
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
public class GraphCounter {
    
    public GraphCounter() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    @Ignore
    public void counter() throws EngineException {
        EngineFactory ef = new EngineFactory();
        GraphEngine engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source.rdf");
//        System.out.println("linkedData-single-source.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-irisa.rdf");
//        System.out.println("linkedData-source-irisa.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-asclepios.rdf");
//        System.out.println("linkedData-source-asclepios.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-ifr49.rdf");
//        System.out.println("linkedData-source-ifr49.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-source-i3s.rdf");
//        System.out.println("linkedData-source-i3s.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep1.rdf");
//        System.out.println("persondata_en_rep1.rdf size : "+engine.getGraph().size());
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep_ascii.1.rdf");
//        System.out.println("persondata_en_rep_ascii.1.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/1-store/persondata.rdf");
//        System.out.println("persondata.rdf size : "+engine.getGraph().size());
//        System.out.println("");
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/2-stores/persondata.1.rdf");
//        System.out.println("persondata.1.rdf size : "+engine.getGraph().size());
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/2-stores/persondata.2.rdf");
//        System.out.println("persondata.2.rdf size : "+engine.getGraph().size());
//        System.out.println("");
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/4-stores/persondata.1.1.rdf");
//        System.out.println("persondata.1.1.rdf size : "+engine.getGraph().size());
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/4-stores/persondata.1.2.rdf");
//        System.out.println("persondata.1.2.rdf size : "+engine.getGraph().size());
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/4-stores/persondata.2.1.rdf");
//        System.out.println("persondata.2.1.rdf size : "+engine.getGraph().size());
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("//Users/gaignard/Desktop/DBPedia-fragmentation/147K/4-stores/persondata.2.2.rdf");
//        System.out.println("persondata.2.2.rdf size : "+engine.getGraph().size());
        
        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep1.rdf");
//        System.out.println("persondata_en_rep1.rdf size : "+engine.getGraph().size());
//        
//        engine = (GraphEngine)ef.newInstance();
//        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/persondata_en_rep2.rdf");
//        System.out.println("persondata_en_rep2.rdf size : "+engine.getGraph().size());
        
        engine = (GraphEngine)ef.newInstance();
        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/dbpediaNames.rdf");
        System.out.println("dbpediaNames size : "+engine.getGraph().size());
        
        engine = (GraphEngine)ef.newInstance();
        engine.load("/Users/gaignard/Documents/These/DistributedSemanticRepositories/DBPedia-persons/dbpediaBirthDate.rdf");
        System.out.println("dbpediaBirthDate size : "+engine.getGraph().size());
        
    }
}
