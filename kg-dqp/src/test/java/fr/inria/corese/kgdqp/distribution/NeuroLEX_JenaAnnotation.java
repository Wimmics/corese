/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

//import com.hp.hpl.jena.ontology.Individual;
//import com.hp.hpl.jena.ontology.OntClass;
//import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
//import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.rdf.model.Statement;
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
@Ignore
public class NeuroLEX_JenaAnnotation {

    public NeuroLEX_JenaAnnotation() {
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
//        FileInputStream fis = new FileInputStream("/Users/gaignard/Documents/These/DistributedSemanticRepositories/NeuroLOG-LinkedData/linkedData-single-source-neurolex.rdf");
//        nlogModel.read(fis, null);
//        System.out.println("");
//
//        OntModel bridgeModel = ModelFactory.createOntologyModel();
//
//        //Iterate over Datasets
//        OntClass mrDatasetC = nlogModel.getOntClass("http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#MR-dataset");
//        Iterator<Individual> it = nlogModel.listIndividuals(mrDatasetC);
//        Property name = nlogModel.getProperty("http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name");
////        Property nlexLabel = bridgeModel.createProperty("http://neurolex.org/wiki/Special:URIResolver/Property-3A/Label");
//        Property nlexLabel = bridgeModel.createProperty("http://neurolex.org/wiki/Special:URIResolver/Property-3A", "Label");
//
//        while (it.hasNext()) {
//            Individual ind = it.next();
//            Statement st = ind.getProperty(name);
//
//            RDFNode object = st.getObject();
//            if (object.toString().toLowerCase().contains("t1 weighted")) {
////                System.out.println(object + " -> T1");    
//                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
//                bridgeInd.addProperty(nlexLabel, "T1 weighted protocol");
//            } else if (object.toString().toLowerCase().contains("t2 weighted")) {
////                System.out.println(object + " -> T2");
//                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
//                bridgeInd.addProperty(nlexLabel, "T2 weighted protocol");
//            } else if (object.toString().toLowerCase().contains("dti")) {
////                System.out.println(object + " -> DTI");
//                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
//                bridgeInd.addProperty(nlexLabel, "Diffusion magnetic resonance imaging protocol");
//            } else if (object.toString().toLowerCase().contains("dwi")) {
////                System.out.println(object + " -> DTI");
//                Individual bridgeInd = bridgeModel.createIndividual(ind.getURI(), ind.getOntClass());
//                bridgeInd.addProperty(nlexLabel, "Diffusion magnetic resonance imaging protocol");
//            } else {
////                System.out.println(object + " -> OTHER");
//            }
//
////            System.out.println(ind.getURI()+" -> "+st.getObject().toString());
//        }
//        
//        FileOutputStream fos = new FileOutputStream(new File("/Users/gaignard/Desktop/bridgeNeuroLEX.rdf"));
//        bridgeModel.write(fos, "RDF/XML");
    }
}
