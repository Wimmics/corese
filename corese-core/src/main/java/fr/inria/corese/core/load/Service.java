package fr.inria.corese.core.load;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.CompileService;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.HashMapList;
import fr.inria.corese.sparql.triple.parser.URLServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

/**
 * Send a SPARQL query to a SPARQL endpoint
 * Return a Mappings
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 */
public class Service {
    static Logger logger = LoggerFactory.getLogger(Service.class);

    public static final String QUERY = "query";
    public static final String ACCESS = "access";
    public static final String MIME_TYPE = "application/sparql-results+xml,application/rdf+xml";
    static final String ENCODING = "UTF-8";
    static final int REDIRECT = 303;
    private ClientBuilder clientBuilder;

    boolean isDebug = !true;
    private boolean post = true;
    private boolean showResult = false;
    private boolean trap = false;
    private URLServer url;
    private Access.Level level = Access.Level.DEFAULT;
    
    public Service() {
        clientBuilder = ClientBuilder.newBuilder();
    }

    public Service(URLServer serv) {
        this(serv, ClientBuilder.newBuilder());
    }
    public Service(String serv) {
        this(new URLServer(serv), ClientBuilder.newBuilder());
    }
    public Service(URLServer serv, ClientBuilder builder) {
        this.clientBuilder = builder;
        setURL(serv);
    }


    public Mappings select(String query) throws LoadException {
        return parseMapping(process(query));
    }

    public Graph construct(String query) throws LoadException {
        return parseGraph(process(query));
    }

    public Mappings query(Query query, Mapping m) throws LoadException {
        return query(query, getAST(query), m);
    }
    
    public Mappings query(Query query, ASTQuery ast, Mapping m) throws LoadException {
        metadata(ast);
        Mappings map;
        if (m != null) {
            ast = mapping(query, m);
        }
        if (ast.isSelect() || ast.isAsk()) {
            map = parseMapping(process(ast.toString()), encoding(ast));
        } else {
            Graph g = parseGraph(process(ast.toString()), encoding(ast));
            map = new Mappings();
            map.setGraph(g);
        }
        map.setQuery(query);
        map.init(query);
        return map;
    }
    
    ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }
    
    void metadata(ASTQuery ast) {
        if (!ast.hasLimit()) {
            if (ast.hasMetadata(Metadata.LIMIT)) {
                ast.setLimit(ast.getMetadata().getDatatypeValue(Metadata.LIMIT).intValue());
            }
            // DRAFT: for testing (modify ast ...)
            String lim = getURL().getParameter("limit");
            if (lim != null) {
                ast.setLimit(Integer.valueOf(lim));
            }
        }
        if (getURL().isGET() || ast.getGlobalAST().hasMetadata(Metadata.GET)) {
            setPost(false);
        }
        if (ast.getGlobalAST().isDebug()) {
            System.out.println(isPost()?"POST":"GET");
        }
        setTrap(ast.getGlobalAST().hasMetadata(Metadata.TRAP));
        setShowResult(ast.getGlobalAST().hasMetadata(Metadata.SHOW));
    }

    ASTQuery mapping(Query q, Mapping m) {
        Mappings map = new Mappings();
        map.add(m);
        CompileService cs = new CompileService();
        ASTQuery ast = cs.filter(q, map, 0, 1);
        return ast;
    }

    public String process(String query) {
        return process(query, MIME_TYPE);
    }
    
    public String process(String query, String mime) {
        if (isPost()) {
            return post(query, mime);
        }
        else {
            return get(query, mime);
        }
    }

    public String post(String query, String mime) {
        // Server URL without parameters
        return post(getURL().getServer(), query, mime);
    }
    
    // https://docs.oracle.com/javaee/7/api/index.html
    public String post(String url, String query, String mime) {
        if (isDebug) {
            System.out.println(query);
        }
        Client client = clientBuilder.build();
        WebTarget target = client.target(url);
        Form form = getForm();
        form.param(QUERY, query);
        try {
            String res = target.request(mime).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
            if (isDebug) {
                System.out.println(res);
            }
            return res;
        } catch (javax.ws.rs.RedirectionException ex) {
            String uri = ex.getLocation().toString();
            logger.warn(String.format("Service redirection: %s to: %s", url, uri));
            if (uri.equals(url)) {
                throw ex;
            }
            return post(uri,query, mime);
        }
    }
    
    Form getForm() {
        return getURL().getMap() == null ? new Form() : new Form(getMap(getURL().getMap()));
    }
    
    MultivaluedMap<String, String> getMap(HashMapList<String> map) {
        MultivaluedMap<String, String> amap = new MultivaluedHashMap<>();
        for (String key : map.keySet()) {
            amap.put(key, map.get(key));
        }
        return amap;
    }
    
    void complete(Form form) {
        if (getURL().getMap() != null) {
            for (String key : getURL().getMap().keySet()) {
                //System.out.println("service: " + key + "=" + getURL().getParameter(key));
                form.param(key, getURL().getParameter(key));
            }
        }
    }

    public String get(String query, String mime) {
        // Server URL without parameters
        return get(getURL().getServer(), query, mime);
    }

    public String get(String uri, String query, String mime) {
        if (isDebug) {
            System.out.println(query);
        }
        String url;
        try {
            url = complete(uri, URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            url = complete(uri, query);
        }
        return getBasic(url, mime);
    }
    
    String complete(String uri, String query) {
        if (getURL().hasParameter()) {
            return String.format("%s?%s&query=%s", uri, param(), query);
        }
        return String.format("%s?query=%s", uri, query);
    }
    
    /**
     * Generate the parameter string
     * We can skip some key=val parameters here.
     */
    String param() {
        StringBuilder sb = new StringBuilder();
        for (String key : getURL().getMap().keySet()) {
            for (String value : getURL().getMap().get(key)) {
                String format = sb.length() == 0 ? "%s=%s" : "&%s=%s";
                sb.append(String.format(format, key, value));
            }
        }
        return sb.toString();
    }
        
    String getBasic(String url, String mime) {
        Client client = clientBuilder.build();
        WebTarget target = client.target(url);
        Response resp = target.request(mime).get();
        
        if (resp.getStatus() == REDIRECT) {
            String myUrl = resp.getLocation().toString();
            logger.warn(String.format("Service redirection: %s to: %s", url, myUrl));
            return getBasic(myUrl, mime);
        }
        
        String res = resp.readEntity(String.class);
        if (isDebug) {
            System.out.println(res);
        }
        return res;
    }
    
//        if (getLevel().equals(Level.PUBLIC)) {
//            form.param(ACCESS, getLevel().toString());
//        }
    
    public Response get(String uri) {
        Client client = clientBuilder.build();
        WebTarget target = client.target(uri);
        Response res = target.request().get();
        return res;
    }

    String encoding(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.ENCODING)) {
            return ast.getMetadata().getStringValue(Metadata.ENCODING);
        }
        return ENCODING;
    }

    public Mappings parseMapping(String str) throws LoadException {
        return parseMapping(str, ENCODING);
    }

    public Mappings parseMapping(String str, String encoding) throws LoadException {
        SPARQLResult xml = SPARQLResult.create(Graph.create());
        xml.setTrapError(isTrap());
        xml.setShowResult(isShowResult());
        try {
            Mappings map = xml.parseString(str, encoding);
            if (isDebug) {
                System.out.println(map);
            }
            return map;
        } catch (ParserConfigurationException ex) {
            throw LoadException.create(ex);
        } catch (SAXException ex) {
            throw LoadException.create(ex);
        } catch (IOException ex) {
            throw LoadException.create(ex);
        }
    }

    public Graph parseGraph(String str) throws LoadException {
        return parseGraph(str, ENCODING);
    }

    public Graph parseGraph(String str, String encoding) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadString(str, Load.RDFXML_FORMAT);
        return g;
    }

    /**
     * @return the level
     */
    public Access.Level getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Access.Level level) {
        this.level = level;
    }

    /**
     * @return the post
     */
    public boolean isPost() {
        return post;
    }

    /**
     * @param post the post to set
     */
    public void setPost(boolean post) {
        this.post = post;
    }

    /**
     * @return the showResult
     */
    public boolean isShowResult() {
        return showResult;
    }

    /**
     * @param showResult the showResult to set
     */
    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    /**
     * @return the trap
     */
    public boolean isTrap() {
        return trap;
    }

    /**
     * @param trap the trap to set
     */
    public void setTrap(boolean trap) {
        this.trap = trap;
    }

    
    public URLServer getURL() {
        return url;
    }

   
    public void setURL(URLServer url) {
        this.url = url;
    }
    
}
