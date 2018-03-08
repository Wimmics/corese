package fr.inria.corese.kgdqp.core;

import fr.inria.corese.sparql.exceptions.EngineException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.SAXException;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.kgenv.parser.Pragma;
import fr.inria.corese.kgenv.result.XMLResult;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgraph.core.Graph;
import fr.inria.corese.kgraph.query.CompileService;
import fr.inria.corese.kgraph.query.ProducerImpl;
import fr.inria.corese.kgraph.query.QueryProcess;
import fr.inria.corese.kgtool.load.LoadException;
import fr.inria.corese.kgtool.load.SPARQLResult;
import java.util.Hashtable;
import java.util.Iterator;

//import fr.inria.wimmics.sparql.soap.client.SparqlResult;
//import fr.inria.wimmics.sparql.soap.client.SparqlSoapClient;
/**
 * Implements service expression There may be local QueryProcess for some URI
 * (use case: W3C test case) Send query to sparql endpoint using HTTP POST query
 * There may be a default QueryProcess
 *
 * TODO: check use same ProducerImpl to generate Nodes ?
 *
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderImplCostMonitoring implements Provider {

    private static final String SERVICE_ERROR = "Service error: ";

    private static Logger logger = LogManager.getLogger(ProviderImplCostMonitoring.class);

    static final String LOCALHOST = "http://localhost:8080/corese/sparql";

    private boolean provEnabled = false;

    HashMap<String, QueryProcess> table;
    Hashtable<String, Double> version;
    QueryProcess defaut;

    Graph provenance = Graph.create();
    
    private int limit = 30;

    private ProviderImplCostMonitoring() {
        table = new HashMap<String, QueryProcess>();
        version = new Hashtable<String, Double>();
    }

    public static ProviderImplCostMonitoring create() {
        ProviderImplCostMonitoring p = new ProviderImplCostMonitoring();
        p.set(LOCALHOST, 1.1);
        return p;
    }

    public boolean isProvEnabled() {
        return provEnabled;
    }

    public void setProvEnabled(boolean provEnabled) {
        this.provEnabled = provEnabled;
    }

    @Override
    public void set(String uri, double version) {
        this.version.put(uri, version);
    }

    // everybody is 1.0 except localhost
    @Override
    public boolean isSparql0(Node serv) {
        Double f = version.get(serv.getLabel());
        return (f == null || f == 1.0);
    }

    /**
     * Define a QueryProcess for this URI
     */
    public void add(String uri, Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        table.put(uri, exec);
    }

    /**
     * Define a default QueryProcess
     */
    public void add(Graph g) {
        QueryProcess exec = QueryProcess.create(g);
        exec.set(this);
        defaut = exec;
    }

    /**
     * If there is a QueryProcess for this URI, use it.
     * Otherwise send query to * sparql endpoint.
     * If endpoint fails, use default QueryProcess if it exists
     * @param serv serv.getLabel() contains the URI of the SPARQL endpoint.
     * @param exp  SPARQL query to execute.
     * @param env  @TBD
     * @return The mappings obtained from applying the request.
     */
    @Override
    public Mappings service(Node serv, Exp exp, Environment env) {
        return service(serv, exp, null, env);
    }

    @Override
    public Mappings service(Node serv, Exp exp, Mappings lmap, Environment env) {
        Query q = exp.getQuery();

        QueryProcess exec = table.get(serv.getLabel());

        if (exec == null) {

            Mappings map = globalSend(serv, q, lmap, env);
            if (map != null) {
                return map;
            }

            if (defaut == null) {
                map = Mappings.create(q);
                if (q.isSilent()) {
                    map.add(Mapping.create());
                }
                return map;
            } else {
                exec = defaut;
            }
        }

        ASTQuery ast = exec.getAST(q);
        Mappings map = exec.query(ast);

        return map;
    }

    /**
     * Cut into pieces when to many Mappings
     */
    Mappings globalSend(Node serv, Query q, Mappings lmap, Environment env) {
        CompileService compiler = new CompileService(this);

        // share prefix
        compiler.prepare(q);

        int slice = compiler.slice(q);

        if (lmap == null || slice == 0) {
            return send(compiler, serv, q, lmap, env, 0, 0);
        } else if (lmap.size() > slice) {
            int size = 0;
            Mappings res = null;

            while (size < lmap.size()) {
                Mappings map = send(compiler, serv, q, lmap, env, size, size + slice);
                size += slice;
                if (res == null) {
                    res = map;
                } else if (map != null) {
                    res.add(map);
                }
            }
            return res;
        } else {
            return send(compiler, serv, q, lmap, env, 0, lmap.size());
        }
    }

    /**
     * Send query to sparql endpoint using a POST HTTP query
     */
    Mappings send(CompileService compiler, Node serv, Query q, Mappings lmap, Environment env, int start, int limit) {
        Query g = q.getOuterQuery();
        int timeout = 0;
        Integer time = (Integer) g.getPragma(Pragma.TIMEOUT);
        if (time != null) {
            timeout = time;
        }
        try {

            // generate bindings from env if any
            compiler.compile(serv, q, lmap, env, start, limit);

            ASTQuery ast = (ASTQuery) q.getAST();

            String query = ast.toString();

            String jsonQuery = query.replace("\n", " ");

            //////// Cost  /////
            // count number of queries
            if (QueryProcessDQP.queryCounter.containsKey(jsonQuery)) {
                Long n = QueryProcessDQP.queryCounter.get(jsonQuery);
                QueryProcessDQP.queryCounter.put(jsonQuery, n + 1L);
            } else {
                QueryProcessDQP.queryCounter.put(jsonQuery, 1L);
            }
            // count number of source access
            String endpoint = serv.getLabel();
            if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
                Long n = QueryProcessDQP.sourceCounter.get(endpoint);
                QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
            } else {
                QueryProcessDQP.sourceCounter.put(endpoint, 1L);
            }
            ///////   end cost //////

            if (g.isDebug()) {
                logger.info("** Provider query: \n" + query);
            }

            logger.info("** Provider: \n" + query);
            InputStream stream = doPost(serv.getLabel(), query, timeout);

            if (g.isDebug()) {
                //logger.info("** Provider result: \n" + sb);
            }

            Mappings map = parse(stream);

            if (g.isDebug()) {
                logger.info("** Provider result: \n" + map.size());
                if (g.isDetail()) {
                    logger.info("** Provider result: \n" + map);
                }
            }

            // monitor results produced
            if (QueryProcessDQP.queryVolumeCounter.containsKey(jsonQuery)) {
                Long n = QueryProcessDQP.queryVolumeCounter.get(jsonQuery);
                QueryProcessDQP.queryVolumeCounter.put(jsonQuery, n + (long) map.size());
            } else {
                QueryProcessDQP.queryVolumeCounter.put(jsonQuery, (long) map.size());
            }
            if (QueryProcessDQP.sourceVolumeCounter.containsKey(endpoint)) {
                Long n = QueryProcessDQP.sourceVolumeCounter.get(endpoint);
                QueryProcessDQP.sourceVolumeCounter.put(endpoint, n + (long) map.size());
            } else {
                QueryProcessDQP.sourceVolumeCounter.put(endpoint, (long) map.size());
            }

//            System.out.println(map);

            if (this.isProvEnabled() && (map != null)) {
                annotateResultsWithProv(map, jsonQuery, endpoint);
            }

            return map;
        } catch (IOException e) {
            logger.error(e);
            logger.error(q.getAST());
            g.addError(SERVICE_ERROR, e);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LoadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (g.isDebug()) {
            logger.info("** Provider error");
        }
        return null;
    }

    /**
     * ********************************************************************
     *
     * SPARQL Protocol client
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     *
     */
    Mappings parse(StringBuffer sb) throws ParserConfigurationException, SAXException, IOException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = XMLResult.create(p);
        Mappings map = r.parseString(sb.toString());
        return map;
    }

    Mappings parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException, LoadException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = SPARQLResult.create(p);
        Mappings map = r.parse(stream);

        return map;
    }

    public StringBuffer doPost2(String server, String query) throws IOException {
        URLConnection cc = post(server, query, 0);
        return getBuffer(cc.getInputStream());
    }

    public InputStream doPost(String server, String query, int timeout) throws IOException {
        URLConnection cc = post(server, query, timeout);
        return cc.getInputStream();
    }

    URLConnection post(String server, String query, int timeout) throws IOException {
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

        URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml,  application/sparql-results+xml");
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));
        urlConn.setRequestProperty("Accept-Charset", "UTF-8");
        urlConn.setReadTimeout(timeout);

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();

        return urlConn;

    }

    StringBuffer getBuffer(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();
        String str = null;

        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }

        return sb;
    }

    private void annotateResultsWithProv(Mappings m, String serviceQuery, String endpoint) {
        if (this.isProvEnabled()) {
            logger.info("Tracking provenance of " + serviceQuery);
            logger.info(m);
            Iterator<Mapping> it = m.iterator();

            // Annotate the invocation only once, Then iterate over edge results.
            String preInsertProv = "PREFIX prov:<" + Util.provPrefix + "> insert data {\n"
                    // Sparql processing activity 
                    + " _:b1 rdf:type prov:Activity . \n"
                    + " _:b1 prov:qualifiedAssociation _:b2 . \n"
                    // Association to a software agent through a plan (i.e. a sparql query) 
                    + " _:b2 rdf:type prov:Association . \n"
                    + " _:b2 prov:hadPlan _:b3 . \n"
                    + " _:b2 prov:agent <" + endpoint + "> . \n"
                    // The plan corresponding to the "recipe" 
                    + " _:b3 rdf:type prov:Plan . \n"
                    + " _:b3 rdfs:comment \"" + Constant.addEscapes(serviceQuery) + "\". \n";
//                                + " _:b3 rdfs:comment \"" + rwSparql.replaceAll("\"", "'").replaceAll("\n", " ").replaceAll("\t", " ") + "\". \n";

            while (it.hasNext()) {
                Mapping mapping = it.next();
//                System.out.println("Annotating " + mapping);

                if (mapping != null) {
                    //TODO provenance for node result ; associated rwSparql ; associated endpoint
                    //TODO TimeStamping
                    //TODO duration ?

                    // Resulting entity 
                    for (int i = 0; i < mapping.getNodes().length; i++) {

                        String insertProv = preInsertProv + "[ rdf:type prov:Entity ; \n"
                                + " prov:wasGeneratedBy _:b1 ; \n"
                                + " prov:wasAttributedTo <" + endpoint + "> ; \n"
                                + " rdfs:label \"" + mapping.getQueryNode(i) + "\" ; \n"
                                + " rdf:value " + mapping.getNode(i).getValue()+ " ; \n"
                                + " rdfs:comment \"" + Constant.addEscapes(serviceQuery) + "\" ] \n"
                                + "}";
                        try {
                            QueryProcess qp = QueryProcess.create(this.provenance);
                            qp.query(insertProv);

//                            Graph g = Graph.create();
//                            Node n = g.addBlank("_:bProv");
//                            
//                            EntityImpl e = EntityImpl.create(g.getRoot(), n);
//                            e.setProvenance(provG);
                        } catch (EngineException ex) {
                            logger.error("Error while inserting provenance:\n" + insertProv);
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

//	public String callSoapEndPoint() {
//		SparqlSoapClient client = new SparqlSoapClient();
//		SparqlResult result = client.sparqlQuery("http://dbpedia.inria.fr/sparql", "select ?x ?r ?y where { ?x ?r ?y} limit 100");
//		String stringResult = result.toString();
//		return stringResult;
//	}
//
//	public static void main(String[] args) {
//		ProviderImplCostMonitoring impl = new ProviderImplCostMonitoring();
//		System.out.println(impl.callSoapEndPoint());
//	}

    public Graph getProvenance() {
        return provenance;
    }
}
