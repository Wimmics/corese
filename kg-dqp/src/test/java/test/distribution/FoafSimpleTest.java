/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.distribution;

//import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryExecDQP;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import org.apache.commons.lang.time.StopWatch;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class FoafSimpleTest {

    public FoafSimpleTest() throws MalformedURLException {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws EngineException, MalformedURLException, IOException {

        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://neurolog.unice.fr:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        kg1.initEngine();
        kg2.initEngine();

        File rep1 = File.createTempFile("rep1", ".rdf");
        FileWriter fw = new FileWriter(rep1);
        InputStream is = FoafSimpleTest.class.getClassLoader().getResourceAsStream("kgram#1-persons.rdf");
        int c;
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        File rep2 = File.createTempFile("rep2", ".rdf");
        fw = new FileWriter(rep2);
        is = FoafSimpleTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf");
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        DataHandler data1 = new DataHandler(new FileDataSource(rep1));
        kg1.uploadRDF(data1);
        DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        kg2.uploadRDF(data2);

        rep1.delete();
        rep2.delete();
    }

    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //

    @Test
    public void remoteFoafQuery() throws EngineException, MalformedURLException, IOException {

        String sparqlSeqQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?y WHERE"
                + "{"
//                                + " <http://i3s/Alban> (foaf:knows/foaf:knows) ?y ."
                                + " <http://i3s/Simon> (foaf:knows+) ?y ."
//                                + " <http://i3s/Alban> (foaf:knows+)/foaf:name ?y ."
//                                                + " <http://i3s/Mireille> ^foaf:knows ?y "
//                                + " <http://i3s/Alban> (foaf:knows+) ?y ."
//                                + " <http://i3s/Alban> (foaf:knows*) ?y ."
                //                + " <http://i3s/Alban> (foaf:knows?) ?y ."
                //                + " <http://i3s/Alban> foaf:knows{2,3} ?y ."
                //                + " <http://i3s/Alban> foaf:knows{2,3}/foaf:name ?y ."
                //                + " <http://i3s/Alban> (foaf:knows{3}) ?y ."
                //                + " <http://i3s/Alban> (foaf:knows{3,}) ?y ."
//                                + " <http://i3s/Alban> (foaf:knows{,2}) ?y ."
                //                                + " <http://i3s/Alban> (foaf:knows | foaf:knows/foaf:name){2,3} ?y ."
                //                                + " <http://i3s/Tram> (^foaf:knows)+ ?y ."
                //                                + " <http://i3s/Alban> ! (foaf:knows) ?y ." //OK
                //                + " ?x foaf:givenname 'Alban'^^xsd:string ." //OK
//                + " <http://i3s/Alban> ! (foaf:knows | foaf:name | foaf:mbox) ?y ."
                //                                + " <http://i3s/Alban> ! (foaf:knows | foaf:name / foaf:mbox ) ?y ." 
                //                                + "FILTER( ?y ~ 'a')"
                + "}";

        String sparqlSampleQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?z WHERE"
                + "{"
                + "?x foaf:knows ?y ."
                + "?y foaf:knows ?z ."
//                + "FILTER(( ?u ~ 'a') && (?x ~ 'a'))"
                + "}";

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

//        QueryExec exec2 = QueryExec.create(engine);
        QueryExecDQP exec = QueryExecDQP.create(engine);
        exec.addRemote(new URL("http://neurolog.unice.fr:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://neurolog.unice.fr:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        StopWatch sw = new StopWatch();
        sw.start();
//        IResults res = exec.SPARQLQuery(sparqlSampleQuery);
        IResults res = exec.SPARQLQuery(sparqlSeqQuery);

        System.out.println("--------");
        System.out.println("Results in " + sw.getTime() + "ms");
        String[] variables = res.getVariables();
        
        assertEquals(2, res.size());

        for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
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
    }
}
