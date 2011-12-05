package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.xml.ws.developer.StreamingDataHandler;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import java.io.File;
import java.io.IOException;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * KGRAM engine exposed as a web service. 
 * The engine can be remotely initialized, populated with an RDF file, 
 * and queried through SPARQL requests. 
 * 
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
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

    
    /**
     * Transfers an RDF file and load it into KGRAM.
     * @param data streamed RDF file to be loaded by KGRAM. 
     */
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

            localFile.delete();
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
    
    /**
     * Processes a SPARQL query and return the SPARQL results. 
     * @param sparqlQuery the query to be processed. 
     * @return the corresponding SPARQL results. 
     */
    @WebMethod
    public String getEdges(String sparqlQuery) {
        StopWatch sw = new StopWatch();
        sw.start();

//        logger.debug("Received query: \n" + sparqlQuery);
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
//                for (Enumeration<IResult> en = results.getResults(); en.hasMoreElements();) {
//                    IResult r = en.nextElement();
//                    HashMap<String, String> result = new HashMap<String, String>();
//                    for (String var : variables) {
//                        if (r.isBound(var)) {
//                            IResultValue[] values = r.getResultValues(var);
//                            for (int j = 0; j < values.length; j++) {
//                                System.out.println(var + " = " + values[j].getStringValue());
////                            result.put(var, values[j].getStringValue());
//                            }
//                        } else {
//                            System.out.println(var + " = Not bound");
//                        }
//                    }
//                }
                sw.stop();
                logger.info("kg-slave processed query in " + sw.getTime() + " ms.");

                return sparqlRes;
            } else {
                logger.debug("No results found");
                sw.stop();
                logger.info("kg-slave processed query in " + sw.getTime() + " ms.");
                return null;
            }
        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            sw.stop();
            logger.info("kg-slave processed query in " + sw.getTime() + " ms.");
            return null;
        }
    }

    /**
     * Initializes the KGRAM engine with a new instance. 
     */
    @WebMethod
    public void initEngine() {
        try {
            //need to use the EngineFactory ?
//            engine = GraphEngine.create(this.getGraph());
            StopWatch sw = new StopWatch();
            sw.start();
            logger.info("Initializing GraphEngine");
            engine = ef.newInstance();
            engine.load(RemoteProducer.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
            engine.load(RemoteProducer.class.getClassLoader().getResourceAsStream("dbpedia_3.6.owl"), null);
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
