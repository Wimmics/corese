/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class SqlImporterTest {

    public SqlImporterTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    @Ignore
    public void testSQLImport() throws EngineException, LoadException {

        String testStudyPatients = "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX dr:<LeSelect.ThinDriver.>"
                + "PREFIX db:<jdbc:datafederator://neurolog.unice.fr:3055/>"
                + "insert  { ?study <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#involves-as-patient> ?patient } "
                + "where { "
                + "{ select(sql(db:globalI3S_v21, dr:ThinDriver, 'globalI3S_v21', 'nlogserv', "
                + "     'SELECT Rel_Subject_Study.Subject_subject_id, Rel_Subject_Study.Study_study_id FROM Rel_Subject_Study') as (?x, ?y)) where {} } ."
                + "{ select(uri(concat(\"http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#subject-\",?x)) as ?patient) where {} }.\n"
                + "{ select(uri(concat(\"http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#study-\",?y)) as ?study) where {} }"
                + "}";

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

        Mappings map = exec.query(testStudyPatients);

//        RDFFormat rdfF = RDFFormat.create(map);
//        System.out.println(rdfF);

        //"http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#subject-GIN-SS-41"
        String nodeSparql = "CONSTRUCT {?x ?p ?y} WHERE "
                + "{ ?x <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#involves-as-patient>{0} <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#subject-GIN-SS-41>}";
        Mappings map2 = exec.query(nodeSparql);
        
        RDFFormat rdfF = RDFFormat.create(map2);
        XMLFormat xmlF = XMLFormat.create(map2);
        System.out.println(xmlF);

    }
}
