/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URI;
import org.junit.Ignore;
import java.net.MalformedURLException;
import java.net.URL;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgimport.JenaGraphFactory;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgtool.print.RDFFormat;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author gaignard
 */
public class JenaLoadingTest {

    public JenaLoadingTest() {
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

    @Test
    public void laodFileTest() throws EngineException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = JenaLoadingTest.class.getClassLoader().getResourceAsStream("kgram2-persons.rdf");
        if (in == null) {
            throw new IllegalArgumentException("File: not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        Graph g = JenaGraphFactory.createGraph(model);
        System.out.println(RDFFormat.create(g).toString());
        System.out.println(g.size());
    }

    @Test
    @Ignore
    public void laodSDBTest() throws EngineException, MalformedURLException {
        Graph g = JenaGraphFactory.createGraph("jdbc:mysql://localhost:8889/SDB2", "root", "root", "executions");
        System.out.println(RDFFormat.create(g));
    }

    @Test
    @Ignore
    public void isSDBTest() throws EngineException, URISyntaxException {
        assertTrue(JenaGraphFactory.isJenaSDBConnection("jdbc:mysql://localhost:8889/SDB2", "root", "root"));
        assertFalse(JenaGraphFactory.isJenaSDBConnection("jdbc:mysql://localhost:8889/SDB2", "root", "roo"));
        assertFalse(JenaGraphFactory.isJenaSDBConnection("jdbc:mysql://localhost:8889/SDB3", "root", "root"));
    }
    
    @Test
    public void laodOPMFileTest() throws EngineException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = JenaLoadingTest.class.getClassLoader().getResourceAsStream("sampleOPM.rdf");
        if (in == null) {
            throw new IllegalArgumentException("File: not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        Graph g = JenaGraphFactory.createGraph(model);
        System.out.println(RDFFormat.create(g).toString());
        System.out.println(g.size());
    }
}
