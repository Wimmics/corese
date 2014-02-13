package fr.inria.edelweiss.kgramserver.webservice;

import com.sun.xml.ws.developer.StreamingDataHandler;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.print.RDFFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.soap.MTOM;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

/**
 * KGRAM engine exposed as a web service. The engine can be remotely
 * initialized, populated with an RDF file, and queried through SPARQL requests.
 *
 * @author Alban Gaignard, alban.gaignard@i3s.unice.fr
 */
@WebService
@MTOM(threshold = 1024)
public class RemoteProducer {

    private String endpoint;
    private Graph graph = Graph.create(false);        
    private QueryProcess exec = QueryProcess.create(graph);
    private Logger logger = Logger.getLogger(RemoteProducer.class);
    private HashMap<String, String> rdfSqlMappings = new HashMap<String, String>();
    //<http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name>
    private final String sql_has_for_name = " insert  { ?s <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name> ?o } where { \n"
            + "   {  select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', '"
            //            + "         SELECT Dataset.dataset_id, Dataset.name FROM Dataset WHERE ???')"
            + "SELECT Dataset.dataset_id, Dataset.name FROM Dataset ')\n"
            + "as (?x, ?o)) where {} "
            + "} .\n"
            + "{ "
            + "     select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-\",?x)) as ?s) where {}"
            + "} }";
    //<http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#is-referred-to-by>
    private final String sql_is_referred_to_by = " insert  { ?s <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#is-referred-to-by> ?o } where { \n"
            + "{ select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', '"
            + "  SELECT Dataset.Subject_subject_id, Dataset.dataset_id FROM Dataset ')\n"
            + "  as (?x, ?y)) where {} "
            + "} .\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-\",?x)) as ?s) where {} }\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#dataset-\",?y)) as ?o) where {} }"
            + "}";
    //<http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#has-for-subject-identifier>
    private final String sql_has_for_subject_identifier = " insert  { ?s <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#has-for-subject-identifier> ?o } where { \n"
            + "{ select(sql(db:%DATABASE%, %DRIVER%, '%LOGIN%', '%PASSWORD%', '"
            + " SELECT Subject.subject_id, Subject.subject_common_identifier FROM Subject ')\n"
            + " as (?x, ?y)) where {} "
            + "} .\n"
            + "{ select(uri(concat(\"http://neurolog.techlog.anr.fr/data.rdf#subject-\",?x)) as ?s) where {} }\n"
            + "{ select(?y as ?o) where {} }"
            + "}";

    public RemoteProducer(Graph g) {
        initMappings();
        initEngine();
    }

    public RemoteProducer() {
        super();
        initMappings();
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
     *
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
            Load ld = Load.create(graph);
            ld.load(localFile.getAbsolutePath());

            localFile.delete();
            sw.stop();
            logger.info("Uploaded RDF content to KGRAM: " + sw.getTime() + " ms");
            logger.info("Graph size " + graph.size());
        } catch (IOException ex) {
            logger.error("Error while uploading RDF content.");
            ex.printStackTrace();
        }
    }
    
    /**
     * Transfers an RDFS file and load it into KGRAM.
     *
     * @param data streamed RDF file to be loaded by KGRAM.
     */
    @WebMethod
    public void uploadRDFS(@XmlMimeType(value = "application/octet-stream") DataHandler data) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();

            File localFile = null;
            localFile = File.createTempFile("KgramRdfContent", ".rdfs");
            StreamingDataHandler streamingDh = (StreamingDataHandler) data;
            streamingDh.moveTo(localFile);
            streamingDh.close();

            logger.debug("Loading " + localFile.getAbsolutePath() + " into KGRAM");
            Load ld = Load.create(graph);
            ld.load(localFile.getAbsolutePath());

            localFile.delete();
            sw.stop();
            logger.info("Uploaded RDFS content to KGRAM: " + sw.getTime() + " ms");
            logger.info("Graph size " + graph.size());
        } catch (IOException ex) {
            logger.error("Error while uploading RDF content.");
            ex.printStackTrace();
        }
    }

    @WebMethod
    public void loadRDF(String remotePath) {
        logger.info("Loading " + remotePath);
//        Graph g = Graph.create();
        if (remotePath.endsWith(".rdf") || remotePath.endsWith(".rdfs") || remotePath.endsWith(".owl") || remotePath.endsWith(".ttl")) {
            Load ld = Load.create(graph);
            ld.load(remotePath);
            logger.info("Successfully loaded " + remotePath + " (Server graph size: "+graph.size()+")");
        } else if (remotePath.endsWith(".n3") || remotePath.endsWith(".nt") ) {
//            FileInputStream fis = null;
//            try {
//                File f = new File(remotePath);
//                fis = new FileInputStream(f);
//                Model model = ModelFactory.createDefaultModel();
//                model.read(fis, null, "N-TRIPLE");
//                System.out.println("Loaded " + f.getAbsolutePath());
//                JenaGraphFactory.updateGraph(model,graph);
//            } catch (FileNotFoundException ex) {
//                ex.printStackTrace();
//            } finally {
//                try {
//                    fis.close();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
        }
//        exec.add(g);
    }
    
    @WebMethod
    public String query(String sparqlQuery) {
//        logger.debug(sparqlQuery);
        try {
            Mappings results = exec.query(sparqlQuery);
            XMLFormat res = XMLFormat.create(results);
            return res.toString();
          } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Processes a SPARQL query and return the SPARQL results.
     *
     * @param sparqlQuery the query to be processed.
     * @return the corresponding SPARQL results.
     */
    @WebMethod
//    public @XmlMimeType(value = "application/octet-stream")
//    DataHandler getEdges(String sparqlQuery) {
    public String getEdges(String sparqlQuery) {
//        StopWatch sw = new StopWatch();
//        sw.start();

//        logger.debug("Received query: \n" + sparqlQuery);
        try {
            Mappings results = exec.query(sparqlQuery);
//            logger.debug("Processed query in " + sw.getTime()+" ms.");
//            RDF serialization
            RDFFormat rdfFormat = RDFFormat.create(results);
            if (rdfFormat == null) {
                return null;
            } else {
                String sResults = rdfFormat.toString();
//                byte[] bytes = sResults.getBytes();
//                final DataHandler data = new DataHandler(bytes, "application/octet-stream");
//                logger.debug("Query processing and RDF serialization in " + sw.getTime() + " ms.");
                return sResults;
//                return sResults;
            }

        } catch (EngineException ex) {
            logger.error("Error while querying the remote KGRAM engine");
            ex.printStackTrace();
//            sw.stop();
            return null;
        }
    }

    /**
     * Initializes the KGRAM engine with a new instance.
     */
    @WebMethod
    public void initEngine() {
//        try {
            //need to use the EngineFactory ?
//            engine = GraphEngine.create(this.getGraph());
//            graph = Graph.create(true); //with entailments
            graph = Graph.create();
            exec = QueryProcess.create(graph);

            StopWatch sw = new StopWatch();
            sw.start();
//            logger.info("Initializing GraphEngine, entailments: "+graph.getEntailment());
            Load ld = Load.create(graph);
//            ld.load(RemoteProducer.class.getClassLoader().getResourceAsStream("kgram-foaf.rdfs"), null);
//            ld.load(RemoteProducer.class.getClassLoader().getResourceAsStream("dbpedia_3.6.owl"), null);
            sw.stop();
            logger.info("Initialized GraphEngine: " + sw.getTime() + " ms");
//            engine.runRuleEngine();
//        } catch (LoadException ex) {
//            logger.error("Error while initialiazing the remote KGRAM engine");
//            ex.printStackTrace();
//            java.util.logging.Logger.getLogger(RemoteProducer.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Initializes the KGRAM engine with a new instance.
     */
    @WebMethod
    public void initEngineFromSQL(String url, String driver, String login, String password) {
        try {

            StopWatch sw = new StopWatch();
            sw.start();
            logger.info("Initializing GraphEngine");

            for (Map.Entry<String, String> entry : rdfSqlMappings.entrySet()) {
                String sql = entry.getValue();
                sql = sql.replaceAll("db:%DATABASE%", "<" + url + ">");
                sql = sql.replaceAll("%DRIVER%", "<" + driver + ">");
                sql = sql.replaceAll("%LOGIN%", login);
                sql = sql.replaceAll("%PASSWORD%", password);

                entry.setValue(sql);

                exec.query(sql);
            }

            sw.stop();
            logger.info("Initialized GraphEngine from SQL: " + sw.getTime() + " ms");
//            engine.runRuleEngine();
        } catch (EngineException ex) {
            logger.error("Error while initialiazing the remote KGRAM engine");
            ex.printStackTrace();
        }
    }

    private void initMappings() {
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#has-for-name", sql_has_for_name);
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#is-referred-to-by", sql_is_referred_to_by);
        rdfSqlMappings.put("http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#has-for-subject-identifier", sql_has_for_subject_identifier);
    }

//    @WebMethod
//    public ArrayList<PropCard> buildCardinalityIndex() {
//        ArrayList<PropCard> propCardIndex = null;
//        try {
//            propCardIndex = new ArrayList<PropCard>();
//            StopWatch sw = new StopWatch();
//            sw.start();
//            logger.info("Building the cardinality index");
//            String cardQ = "select distinct * projection 2000000 "
//                    + "where"
//                    + "{"
//                    + "?sujet ?propriete ?objet"
//                    + "}"
//                    + "order by ?propriete"
//                    + " limit 1000000";
//            IResults res = engine.SPARQLQuery(cardQ);
//            for (IResult r : res) {
//                String propriete = r.getStringValue("?propriete");
//                if (containsKey(propCardIndex,propriete)) {
//                    long card = propCardIndex.get(propriete);
//                    card = card + 1;
//                    propCardIndex.put(propriete, card);
//                } else {
//                    propCardIndex.put(propriete, new Long(1));
//                }
//            }
//        } catch (EngineException ex) {
//            logger.error("Error while initialiazing the remote KGRAM engine");
//            ex.printStackTrace();
//        }
//        return propCardIndex;
//    }
//
//    @WebMethod
//    public ArrayList<String> buildExistencyIndex() {
//        ArrayList<String> propExistencyIndex = null;
//        try {
//            StopWatch sw = new StopWatch();
//            sw.start();
//            logger.info("Building the existency index");
//            String existQ = "select distinct ?propriete projection 2000000 "
//                    + "where"
//                    + "{"
//                    + "?sujet ?propriete ?objet"
//                    + "}"
//                    + "order by ?propriete"
//                    + " limit 1000000";
//            IResults res = engine.SPARQLQuery(existQ);
//            for (IResult r : res) {
//                String propriete = r.getStringValue("?propriete");
//                propExistencyIndex.add(propriete);
//            }
//        } catch (EngineException ex) {
//            logger.error("Error while initialiazing the remote KGRAM engine");
//            ex.printStackTrace();
//        }
//        return propExistencyIndex;
//    }
    public void load() {
    }

    public void runRule() {
    }

    public void reset() {
    }
//    public boolean containsKey(ArrayList<PropCard> list, String prop) {
//        for (PropCard pair : list) {
//            if (((String)pair.getValue()).equals(prop)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
