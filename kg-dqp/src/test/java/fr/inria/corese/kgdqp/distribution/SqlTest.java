/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgdqp.distribution;

import fr.inria.corese.kgengine.api.IResult;
import fr.inria.corese.kgengine.api.IEngine;
import fr.inria.corese.kgengine.api.EngineFactory;
import fr.inria.corese.kgengine.api.IResultValue;
import fr.inria.corese.kgengine.api.IResults;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgdqp.core.QueryExecDQP;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.print.RDFFormat;
import fr.inria.corese.kgtool.print.XMLFormat;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import org.apache.commons.lang.time.StopWatch;
import org.junit.*;

/**
 *
 * @author gaignard
 */
public class SqlTest {

    public SqlTest() throws MalformedURLException {
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
    public void testSimpleSql() throws EngineException, LoadException {


        String sparqlSqlRemote = "PREFIX db:<jdbc:mysql://neurolog.unice.fr:3306/>"
                + "PREFIX dr:<com.mysql.jdbc.>"
                //                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "construct {?s <http://xmlns.com/foaf/0.1/name> ?z}"
                + "where {"
                + "{ select(sql(db:NeuroLOG_Metadata_I3S_v21, dr:Driver, 'nlogI3SDEV', 'nlogserv', 'SELECT Dataset.dataset_id, Dataset.Study_study_id, Dataset.dataset_creation_date_time FROM Dataset') as (?x, ?y, ?z)) where {} } ."
                + "{ select(uri(?x) as ?s) where {}}"
                + "}";

        String sparqlSqlLocal = "PREFIX db:<jdbc:mysql://localhost:8889/>"
                + "PREFIX dr:<com.mysql.jdbc.>"
                //                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "construct {?s <http://xmlns.com/foaf/0.1/name> ?z}"
                + "where {"
                + "{ select(sql(db:NeuroLOG_Metadata_I3S, dr:Driver, 'root', 'root', 'SELECT Dataset.dataset_id, Dataset.Study_study_id, Dataset.dataset_creation_date_time FROM Dataset') as (?x, ?y, ?z)) where {} } ."
                + "{ select(uri(?x) as ?s) where {}}"
                + "}";

        String testSparql = "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX dr:<com.mysql.jdbc.>"
                + "PREFIX db:<jdbc:mysql://neurolog.unice.fr:3306/>"
                + "construct  { ?dataset <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name> ?dsName } "
                + "where { "
                //                + " { select(sql(<jdbc:mysql://neurolog.unice.fr:3306/NeuroLOG_Metadata_I3S_v21>, <com.mysql.jdbc.Driver>, 'nlogI3SDEV', 'nlogserv', 'SELECT Dataset.dataset_id, Dataset.Study_study_id FROM Dataset') as (?x, ?dsName)) where {} } ."
                + " { select(sql(db:NeuroLOG_Metadata_I3S_v21, dr:Driver, 'nlogI3SDEV', 'nlogserv', 'SELECT Dataset.dataset_id, Dataset.Study_study_id FROM Dataset') as (?x, ?dsName)) where {} } ."
                + "{ select(uri(?x) as ?dataset) where {}} }";

        Graph graph = Graph.create();
        QueryProcess exec = QueryProcess.create(graph);

//        Mappings map = exec.query(sparqlSqlRemote);
        Mappings map = exec.query(sparqlSqlRemote);

        XMLFormat xmlF = XMLFormat.create(map);
        System.out.println(xmlF);
        System.out.println("");

        RDFFormat rdfF = RDFFormat.create(map);
        System.out.println(rdfF);

    }

    @Test
    @Ignore
    public void remoteSQLQuery() throws EngineException, MalformedURLException, IOException {

        String sparqlT2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>"
                
//                + "SELECT distinct ?subject ?d ?name WHERE {"
//		+ "     ?d linguistic-expression:has-for-name ?name ."
//		+ "     ?subject iec:is-referred-to-by ?name ."
//		+ "FILTER (?name ~ 'T2')"
                
                + "SELECT distinct ?patient ?clinID ?dataset ?dsName WHERE"
                + "{"
                + "     ?patient examination-subject:has-for-subject-identifier ?clinID ."
                + "     ?patient iec:is-referred-to-by ?dataset ."
//                + "     ?dataset linguistic-expression:has-for-name ?dsName ."
//                + "     FILTER ((?clinID ~ 'MS') && (?dsName ~ 'T2'))"
                + "     FILTER ((?clinID ~ 'MS'))"
                + "}";

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExecDQP exec = QueryExecDQP.create(engine);
//        exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.6-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemoteSQL("jdbc:mysql://neurolog.unice.fr:3306/NeuroLOG_Metadata_I3S_v21", "com.mysql.jdbc.Driver", "nlogI3SDEV", "nlogserv");
//        exec.addRemoteSQL("jdbc:datafederator://neurolog.unice.fr:3055/globalI3S_v21", "LeSelect.ThinDriver.ThinDriver", "globalI3S_v21", "nlogserv");
        exec.addRemoteSQL("jdbc:datafederator://neurolog.unice.fr:3055/globalI3S_v21", "LeSelect.ThinDriver.ThinDriver", "globalI3S_v21", "nlogserv");

        StopWatch sw = new StopWatch();
        sw.start();
        IResults res = exec.SPARQLQuery(sparqlT2);

        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        String[] variables = res.getVariables();

        int cnt = 0;
        for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
            cnt++;
            IResult r = en.nextElement();
            HashMap<String, String> result = new HashMap<String, String>();
            for (String var : variables) {
                if (r.isBound(var)) {
                    IResultValue[] values = r.getResultValues(var);
                    for (int j = 0; j < values.length; j++) {
                        System.out.println(var + " = " + values[j].getStringValue());
//                            result.put(var, values[j].getStringValue());
                    }
                } else {
                    System.out.println(var + " = Not bound");
                }
            }
        }
        System.out.println(cnt + " res");
    }
    
    @Test
    @Ignore
    public void testExtractDSid() {
        String url = "<http://neurolog.techlog.anr.fr/data.rdf#dataset-GIN-SS-122>";
        url = url.substring(url.lastIndexOf("#dataset-")+9);
        url = url.substring(0,url.lastIndexOf(">"));
        System.out.println(url);
    }
}
