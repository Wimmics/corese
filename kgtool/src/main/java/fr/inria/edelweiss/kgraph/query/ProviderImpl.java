package fr.inria.edelweiss.kgraph.query;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgenv.result.XMLResult;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Provider;
import fr.inria.edelweiss.kgram.core.Exp;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;

//import fr.inria.wimmics.sparql.soap.client.SparqlResult;
//import fr.inria.wimmics.sparql.soap.client.SparqlSoapClient;


/**
 * Implements service expression 
 * There may be local QueryProcess for some URI (use case: W3C test case)
 * Send query to sparql endpoint using HTTP POST query
 * There may be a default QueryProcess
 * 
 * TODO:
 * check use same ProducerImpl to generate Nodes ?
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 *
 */
public class ProviderImpl implements Provider {
	
	private static final String SERVICE_ERROR = "Service error: ";

	private static Logger logger = Logger.getLogger(ProviderImpl.class);
	
	static final String LOCALHOST = "http://localhost:8080/corese/sparql";

	HashMap<String, QueryProcess> table;
	QueryProcess defaut;
	CompileService compiler;

	private int limit = 30;
	
	ProviderImpl(){
		table = new HashMap<String, QueryProcess>();
		compiler = new CompileService();
	}
	
	public static ProviderImpl create(){
		ProviderImpl p = new ProviderImpl();
		p.set(LOCALHOST, 1.1);
		return p;
	}
	
	public void set(String uri, double version){
		compiler.set(uri, version);
	}
	
	/**
	 * Define a QueryProcess for this URI
	 */
	public void add(String uri, Graph g){
		QueryProcess exec = QueryProcess.create(g);
		exec.set(this);
		table.put(uri, exec);
	}
	
	/**
	 * Define a default QueryProcess
	 */
	public void add(Graph g){
		QueryProcess exec = QueryProcess.create(g);
		exec.set(this);
		defaut = exec;
	}

	/**
	 * If there is a QueryProcess for this URI, use it
	 * Otherwise send query to spaql endpoint
	 * If endpoint fails, use default QueryProcess if it exists
	 */
	
	public Mappings service(Node serv, Exp exp, Environment env) {
		return service(serv, exp, null, env);
	}
	
	public Mappings service(Node serv, Exp exp, Mappings lmap, Environment env) {
		Query q = exp.getQuery();
		
		QueryProcess exec = table.get(serv.getLabel());

		if (exec == null){

			Mappings map = globalSend(serv, q, lmap, env);
			if (map != null){
				return map;
			}

			if (defaut == null){
				map =  Mappings.create(q);
				if (q.isSilent()){
					map.add(Mapping.create());
				}
				return map;
			}
			else {
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
	Mappings globalSend(Node serv, Query q, Mappings lmap, Environment env){
		
		// share prefix
		compiler.prepare(q);
		
		int slice = compiler.slice(q);

		if (lmap == null || slice == 0){
			return send(serv, q, lmap, env, 0, 0);
		}
		else if (lmap.size() > slice ){
			int size = 0;
			Mappings res = null;
			
			while (size < lmap.size()){
				Mappings map =  send(serv, q, lmap, env, size, size + slice); //lmap.size());
				size += slice;
				if (res == null){
					res = map;
				}
				else if (map != null){
					res.add(map);
				}
			}
			return res;
		}
		else {
			return send(serv, q, lmap, env, 0, lmap.size());
		}
	}
	
	
	
	
	/**
	 * Send query to sparql endpoint using a POST HTTP query
	 */		 	
	Mappings send(Node serv, Query q, Mappings lmap, Environment env, int start, int limit){
		Query g = q.getOuterQuery();
		try {
		
			// generate bindings from env if any
			compiler.compile(serv, q, lmap, env, start, limit);

			ASTQuery ast = (ASTQuery) q.getAST();
						
			String query = ast.toString();
						
			if (g.isDebug()){
				logger.info("** Provider query: \n" + query);
			}
			
			//logger.info("** Provider: \n" + query);

			//StringBuffer sb = doPost2(serv.getLabel(), query);
			
			InputStream stream = doPost(serv.getLabel(), query);
			
			if (g.isDebug()){
				//logger.info("** Provider result: \n" + sb);
			}
			
//			if (sb.length() == 0){
//				throw new IOException("Endpoint result is empty");
//			}

			//Mappings map = parse(sb);
			Mappings map = parse(stream);
		
			if (g.isDebug()){
				logger.info("** Provider result: \n" + map.size());
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
		}
		
		if (g.isDebug()){
			logger.info("** Provider error" );
		}
		return null;
	}
	
	



	
	
	
	
	/**********************************************************************
	 * 
	 * SPARQL Protocol client
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	
	
	Mappings parse(StringBuffer sb) throws ParserConfigurationException, SAXException, IOException{
		ProducerImpl p  = ProducerImpl.create(Graph.create());
		XMLResult r 	= XMLResult.create(p);
		Mappings map 	= r.parseString(sb.toString());
		return map;
	}
	
	
	Mappings parse(InputStream stream) throws ParserConfigurationException, SAXException, IOException{
		ProducerImpl p  = ProducerImpl.create(Graph.create());
		XMLResult r 	= SPARQLResult.create(p);
		Mappings map 	= r.parse(stream);
		return map;
	}
	
	public StringBuffer doPost2(String server, String query) throws IOException{
		URLConnection cc = post(server, query);
		return getBuffer(cc.getInputStream());
	}
	
	public InputStream doPost(String server, String query) throws IOException{
		URLConnection cc = post(server, query);
		return cc.getInputStream();
	}
	
	URLConnection post(String server, String query) throws IOException{
		String qstr = "query=" + URLEncoder.encode(query, "UTF-8");

		URL queryURL = new URL(server);
        HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
        urlConn.setRequestMethod("POST"); 
        urlConn.setDoOutput(true);
        urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConn.setRequestProperty("Accept", "application/rdf+xml,  application/sparql-results+xml");
        urlConn.setRequestProperty("Content-Length", String.valueOf(qstr.length()));
        urlConn.setRequestProperty("Accept-Charset", "UTF-8");

        OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
        out.write(qstr);
        out.flush();
        
        return urlConn;

	}
	
	
	
	StringBuffer getBuffer(InputStream stream) throws IOException{
		InputStreamReader r = new InputStreamReader(stream, "UTF-8");
		BufferedReader br = new BufferedReader(r);
		StringBuffer sb = new StringBuffer();
		String str = null;

		while ((str = br.readLine()) != null){
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
}
