package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.xml.ws.developer.StreamingDataHandler;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 *
 * @author gaignard
 */
@WebService
@MTOM(threshold = 1024)
public class RemoteProducer extends ProducerImpl {

    private String endpoint;
    private IEngine engine;
    private EngineFactory ef = new EngineFactory();
    private Logger logger = Logger.getLogger(RemoteProducer.class);

    public RemoteProducer(Graph g) {
        super(g);
        initEngine();
    }

    public RemoteProducer() {
        super();
        initEngine();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    //todo load (sparql 1.1)
    
    @WebMethod
    public void uploadRDF(@XmlMimeType(value = "application/octet-stream") DataHandler data) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();

            File localFile = null;
            localFile = File.createTempFile("KgramRdfContent", ".rdf");
            StreamingDataHandler streamingDh = (StreamingDataHandler) data;
            streamingDh.moveTo(localFile);
            streamingDh.close();

            logger.debug("Loading " + localFile.getAbsolutePath() + " into KGRAM");
            engine.load(localFile.getAbsolutePath());

//            localFile.delete();
            sw.stop();
            logger.info("Uploaded content to KGRAM: " + sw.getTime() + " ms");
        } catch (EngineException ex) {
            logger.error("Error while loading RDF content into KGRAM.");
            ex.printStackTrace();
        } catch (IOException ex) {
            logger.error("Error while uploading RDF content.");
            ex.printStackTrace();
        }
    }

    @WebMethod
    public void loadRDF(String remotePath) {
    }

    @WebMethod
    public String getEdges(String sparqlQuery) {
        logger.debug("Received query: \n" + sparqlQuery);
        try {
            QueryExec exec = QueryExec.create(engine);
            exec.add(engine);
            IResults results = exec.SPARQLQuery(sparqlQuery);

            ASTQuery astQuery = ASTQuery.create(sparqlQuery);
            Exp exp = astQuery.getBody();

            String sparqlRes = results.toSPARQLResult();
            //results processing
            String[] variables = results.getVariables();
            if (results.getSuccess()) {
                for (Enumeration<IResult> en = results.getResults(); en.hasMoreElements();) {
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
                
//                logger.info("Sparql result :\n" + sparqlRes);

                //todo result "insert data.."
                
                return sparqlRes;
            } else {
                logger.debug("No results found");
                return null;
            }
        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return null;
        }
    }

    private void initEngine() {
        try {
            //need to use the EngineFactory ?
//            engine = GraphEngine.create(this.getGraph());
            StopWatch sw = new StopWatch();
            sw.start();
            logger.info("Initializing GraphEngine");
            engine = ef.newInstance();
            engine.load(RemoteProducer.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
            engine.load(RemoteProducer.class.getClassLoader().getResourceAsStream("dbpedia_3.6.owl"), null);
//            engine.load(RemoteProducer.class.getClassLoader().getResourceAsStream("kgram-persons.rdf"), null);
            sw.stop();
            logger.info("Initialized GraphEngine: " + sw.getTime() + " ms");
//            engine.runRuleEngine();
        } catch (EngineException ex) {
            logger.error("Error while initialiazing the remote KGRAM engine");
            ex.printStackTrace();
        }
    }

    public void load() {
    }

    public void runRule() {
    }

    public void reset() {
    }
}
