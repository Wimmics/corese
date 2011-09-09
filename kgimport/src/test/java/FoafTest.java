

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgimport.JenaGraphFactory;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.ws.BindingProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author gaignard
 */
public class FoafTest {

    private static EngineFactory ef;
    private static IEngine engine;
    
    private String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x WHERE"
                + "{"
//                + "     ?x foaf:knows ?y"
                + "     ?x a foaf:Person"
                + "}";

    public FoafTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ef = new EngineFactory();
//            ef.setProperty(EngineFactory.PROPERTY_FILE, "corese.properties");
//            ef.setProperty(EngineFactory.ENGINE_LOG4J, "log4j.properties");
        engine = ef.newInstance();

//            engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-foaf-t.owl"), null);
        engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
//        engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram#1-persons.rdf"), null);
        engine.load(FoafTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf"), null);
        engine.runRuleEngine();
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
    public void rdfFormatTest() throws EngineException, LoadException {
        Graph g = Graph.create();
        Load loader = Load.create(g);
        InputStream in = FoafTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf");
        loader.load(in, null);
        System.out.println(RDFFormat.create(g).toString());
        System.out.println("");
        
        ///
        Model model = ModelFactory.createDefaultModel();
        InputStream in2 = FoafTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf");
        model.read(in2, null);
        Graph g2 = JenaGraphFactory.createGraph(model);
        System.out.println(RDFFormat.create(g2).toString());
        System.out.println("");
        
        
        //////
//        IEngine eng = JenaGraphFactory.createEngine(model);
        EngineFactory ef = new EngineFactory();
        GraphEngine eng = (GraphEngine) ef.newInstance();
        JenaGraphFactory.updateGraph(model, eng.getGraph());
        
        QueryExec exec = QueryExec.create();
        exec.add(eng);
        
        IResults res = exec.SPARQLQuery(sparqlQuery);
        String[] variables = res.getVariables();

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
    
    @Test
    @Ignore
    public void foafJena() throws EngineException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FoafTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf");
        if (in == null) {
            throw new IllegalArgumentException("File: not found");
        }

        // read the RDF/XML file
        model.read(in, null);

        Graph g = JenaGraphFactory.createGraph(model);
        QueryProcess qp = QueryProcess.create(g);
        Mappings maps = qp.query(sparqlQuery);
        Iterator it = maps.listIterator();
        while (it.hasNext()) {
            Mapping map = (Mapping) it.next();
            System.out.println(map.toString());
        }
    }

    @Test
    @Ignore
    public void foafQuery() throws EngineException {
        
        QueryExec exec = QueryExec.create();
        exec.add(engine);

        IResults res = exec.SPARQLQuery(sparqlQuery);
        String[] variables = res.getVariables();

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

    @Test
    @Ignore
    public void testSparqlQuery() {
        String sparqlQuery = "PREFIX kg: <http://ns.inria.fr/edelweiss/2010/kgram/>"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "PREFIX xml: <http://www.w3.org/XML/1998/namespace>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX cos: <http://www.inria.fr/acacia/corese#>"
                + "select * where { "
                + "     ?x foaf:knows ?y"
                + "}";

        ASTQuery astQuery = ASTQuery.create(sparqlQuery);
        Expression ex = astQuery.bind();
    }

    @Test
    @Ignore
    public void remoteFoafQuery() throws EngineException, MalformedURLException, IOException {

        RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8089/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
        RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://cavaco.unice.fr:8090/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

        File rep1 = File.createTempFile("rep1", ".rdf");
        FileWriter fw = new FileWriter(rep1);
        InputStream is = FoafTest.class.getClassLoader().getResourceAsStream("kgram#1-persons.rdf");
        int c;
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        File rep2 = File.createTempFile("rep2", ".rdf");
        fw = new FileWriter(rep2);
        is = FoafTest.class.getClassLoader().getResourceAsStream("kgram#2-persons.rdf");
        while ((c = is.read()) != -1) {
            fw.write(c);
        }
        is.close();
        fw.close();

        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

        DataHandler data = new DataHandler(new FileDataSource(rep1));
        kg1.uploadRDF(data);
        DataHandler data2 = new DataHandler(new FileDataSource(rep2));
        kg2.uploadRDF(data2);

        rep1.delete();
        rep2.delete();

        String sparqlQuery = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
                + "SELECT distinct ?x ?m ?z WHERE"
                + "{"
                + "     ?x foaf:knows ?y ."
                + "     ?y foaf:knows ?z ."
                + "     OPTIONAL {?x foaf:mbox ?m}"
                + " FILTER (?x ~ 'Filip')"
                + "}";
//                + "GROUP BY ?x ORDER BY ?x "
//                + "LIMIT 6";

        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();

        QueryExec exec = QueryExec.create(engine);
        exec.addRemote(new URL("http://cavaco.unice.fr:8089/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
        exec.addRemote(new URL("http://cavaco.unice.fr:8090/kgserver-1.0-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        IResults res = exec.SPARQLQuery(sparqlQuery);
        String[] variables = res.getVariables();

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
