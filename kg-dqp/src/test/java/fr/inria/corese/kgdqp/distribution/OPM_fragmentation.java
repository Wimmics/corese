/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

//import com.hp.hpl.jena.ontology.Individual;
//import com.hp.hpl.jena.ontology.OntClass;
//import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.rdf.model.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class OPM_fragmentation {

    public OPM_fragmentation() {
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

    @Test
    @Ignore
    public void hello() throws FileNotFoundException {
//        OntModel nlogModel = ModelFactory.createOntologyModel();
//        FileInputStream fis = new FileInputStream("/Users/gaignard/Desktop/Expe-VIP-Workflows/sorteo-provenance.rdf");
//        nlogModel.read(fis, null);
//        System.out.println("");
//
//        OntModel bridgeModel = ModelFactory.createOntologyModel();
//
//        //Iterate over Datasets
//        OntClass processC = nlogModel.getOntClass("http://purl.org/net/opmv/ns#Process");
//        Iterator<Individual> itProcesses = nlogModel.listIndividuals(processC);
////        Property name = nlogModel.getProperty("http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name");
////        Property nlexLabel = bridgeModel.createProperty("http://neurolex.org/wiki/Special:URIResolver/Property-3A/Label");
////        Property nlexLabel = bridgeModel.createProperty("http://neurolex.org/wiki/Special:URIResolver/Property-3A", "Label");
//
//        while (itProcesses.hasNext()) {
//            Individual ind = itProcesses.next();
//            System.out.println(ind);
//            StmtIterator stIt = ind.listProperties();
//            while (stIt.hasNext()) {
//                Statement st = stIt.next();
//                System.out.println("\t"+st);
//            }
//            System.out.println("");
//
////            RDFNode object = st.getObject();
////            if (object.toString().toLowerCase().contains("t1 weighted")) {
//////                System.out.println(object + " -> T1");    
////                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
////                bridgeInd.addProperty(nlexLabel, "T1 weighted protocol");
////            } else if (object.toString().toLowerCase().contains("t2 weighted")) {
//////                System.out.println(object + " -> T2");
////                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
////                bridgeInd.addProperty(nlexLabel, "T2 weighted protocol");
////            } else if (object.toString().toLowerCase().contains("dti")) {
//////                System.out.println(object + " -> DTI");
////                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
////                bridgeInd.addProperty(nlexLabel, "Diffusion magnetic resonance imaging protocol");
////            } else if (object.toString().toLowerCase().contains("dwi")) {
//////                System.out.println(object + " -> DTI");
////                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
////                bridgeInd.addProperty(nlexLabel, "Diffusion magnetic resonance imaging protocol");
////            } else {
//////                System.out.println(object + " -> OTHER");
////            }
//
////            System.out.println(ind.getURI()+" -> "+st.getObject().toString());
//        }
//        
//        FileOutputStream fos = new FileOutputStream(new File("/Users/gaignard/Desktop/bridgeNeuroLEX.rdf"));
//        bridgeModel.write(fos, "RDF/XML");
    }
}
