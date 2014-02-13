package fr.inria.edelweiss.kgdqp.core;

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

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLRestEndpointClient;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SPARQLSoapEndpointClient;
import fr.inria.edelweiss.kgdqp.sparqlendpoint.SparqlEndpointInterface;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.CompileService;
import fr.inria.edelweiss.kgraph.query.ProducerImpl;
import fr.inria.edelweiss.kgraph.query.QueryProcess;

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
@Deprecated
public class ProviderWSImpl implements Provider {

    private static Logger logger = Logger.getLogger(ProviderWSImpl.class);
    HashMap<String, QueryProcess> table;
    QueryProcess defaut;
    CompileService compiler;
    private WSImplem wsImplem;

    ProviderWSImpl(WSImplem wsImplem) {
        table = new HashMap<String, QueryProcess>();
//        compiler = new CompileService(this);
        this.wsImplem = wsImplem;
    }

    public static ProviderWSImpl create(WSImplem wsImplem) {
        return new ProviderWSImpl(wsImplem);
    }

    @Override
    public void set(String uri, double version) {
//        compiler.set(uri, version);
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
     * If there is a QueryProcess for this URI, use it Otherwise send query to
     * spaql endpoint If endpoint fails, use default QueryProcess if it exists
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

        // share prefix
        compiler.prepare(q);

        int slice = compiler.slice(q);

        if (lmap == null || slice == 0) {
            return send(serv, q, lmap, env, 0, 0);
        } else if (lmap.size() > slice) {
            int size = 0;
            Mappings res = null;

            while (size < lmap.size()) {
                Mappings map = send(serv, q, lmap, env, size, size + slice); //lmap.size());
                size += slice;
                if (res == null) {
                    res = map;
                } else if (map != null) {
                    res.add(map);
                }
            }
            return res;
        } else {
            return send(serv, q, lmap, env, 0, lmap.size());
        }
    }

    /**
     * Send query to sparql endpoint using REST or SOAP messages
     */
    Mappings send(Node serv, Query q, Mappings lmap, Environment env, int start, int limit) {
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
            if (g.isDebug()) {
                logger.info("** Provider query: \n" + query);
            }
//            System.out.println("---QUERY---");
//            System.out.println(q);
//            System.out.println("");


//            System.out.println("---SERVICE---");
//            System.out.println(query);
//            System.out.println("");

            if (g.isDebug()) {
                logger.info("** Provider: \n" + query);
            }


            String sparqlRes = null;
            if (wsImplem == WSImplem.SOAP) {
                SparqlEndpointInterface rp = new SPARQLSoapEndpointClient(new URL(serv.getLabel()));
                sparqlRes = rp.query(query);
            } else if (wsImplem == WSImplem.REST) {
                SparqlEndpointInterface rp = new SPARQLRestEndpointClient(new URL(serv.getLabel()));
                sparqlRes = rp.query(query);
            }

            // count number of queries

            String queryWithoutBindings = query;
            if (queryWithoutBindings.contains("filter")) {
                queryWithoutBindings = queryWithoutBindings.substring(timeout, queryWithoutBindings.indexOf("filter"));
            }
            if (queryWithoutBindings.contains("values")) {
                queryWithoutBindings = queryWithoutBindings.substring(timeout, queryWithoutBindings.indexOf("values"));
            }

            if (QueryProcessDQP.queryCounter.containsKey(queryWithoutBindings)) {
                Long n = QueryProcessDQP.queryCounter.get(queryWithoutBindings);
                QueryProcessDQP.queryCounter.put(queryWithoutBindings, n + 1L);
            } else {
                QueryProcessDQP.queryCounter.put(queryWithoutBindings, 1L);
            }

            // count number of source access
            String endpoint = serv.getLabel();
            if (QueryProcessDQP.sourceCounter.containsKey(endpoint)) {
                Long n = QueryProcessDQP.sourceCounter.get(endpoint);
                QueryProcessDQP.sourceCounter.put(endpoint, n + 1L);
            } else {
                QueryProcessDQP.sourceCounter.put(endpoint, 1L);
            }

            if (sparqlRes == null) {
                return null;
            } else {
                Mappings maps = parseXML(new StringBuffer(sparqlRes));
//                System.out.println("------> Mappings from SERVICE: " + maps.size());
                if (QueryProcessDQP.queryVolumeCounter.containsKey(queryWithoutBindings)) {
                    Long n = QueryProcessDQP.queryVolumeCounter.get(queryWithoutBindings);
                    QueryProcessDQP.queryVolumeCounter.put(queryWithoutBindings, n + (long) maps.size());
                } else {
                    QueryProcessDQP.queryVolumeCounter.put(queryWithoutBindings, (long) maps.size());
                }

                return maps;
            }
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Search select variable of query that is bound in env Generate binding for
     * such variable Set bindings in ASTQuery
     */
//    void bindings(Query q, Environment env) {
//        ASTQuery ast = (ASTQuery) q.getAST();
//        ast.clearBindings();
//        ArrayList<Variable> lvar = new ArrayList<Variable>();
//        ArrayList<Constant> lval = new ArrayList<Constant>();
//
//        for (Node qv : q.getSelect()) {
//            String var = qv.getLabel();
//            Node val = env.getNode(var);
//
//            if (val != null) {
//                lvar.add(Variable.create(var));
//                IDatatype dt = (IDatatype) val.getValue();
//                Constant cst = Constant.create(dt);
//                lval.add(cst);
//            }
//        }
//
//        if (lvar.size() > 0) {
//            ast.setVariableBindings(lvar);
//            ast.setValueBindings(lval);
//        }
//    }
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
    Mappings parseXML(StringBuffer sb) throws ParserConfigurationException, SAXException, IOException {
        ProducerImpl p = ProducerImpl.create(Graph.create());
        XMLResult r = XMLResult.create(p);
        Mappings map = r.parseString(sb.toString());
        return map;
    }

    public StringBuffer doPost(String server, String query) throws IOException {
        URLConnection cc = post(server, query);
        return getBuffer(cc.getInputStream());
    }

    URLConnection post(String server, String query) throws IOException {
        String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

        URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml, text/xml");
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();

        return urlConn;

    }

    StringBuffer getBuffer(InputStream stream) throws IOException {
        InputStreamReader r = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(r);
        StringBuffer sb = new StringBuffer();

        String str = null;
        while ((str = br.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }

        return sb;
    }
//	public String callSoapEndPoint() {
//		SparqlSoapClient client = new SparqlSoapClient();
//		SparqlResult result = client.sparqlQuery("http://dbpedia.inria.fr/sparql", "select ?x ?r ?y where { ?x ?r ?y} limit 100");
//		String stringResult = result.toString();
//		return stringResult;
//	}
//
//	public static void main(String[] args) {
//		ProviderImpl impl = new ProviderImpl();
//		System.out.println(impl.callSoapEndPoint());
//	}

    void compile(Node serv, Query q, Mappings lmap, Environment env) {
        // share prefix
        compiler.prepare(q);
        // bindings
        //TODO modif alban nullpointer exception
        if (lmap == null) {
            lmap = Mappings.create(q);
        }
        //end modif alban
        compiler.compile(serv, q, lmap, env, 0, lmap.size());
    }
}
