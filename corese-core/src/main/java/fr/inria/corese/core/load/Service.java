package fr.inria.corese.core.load;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.CompileService;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Send a SPARQL query to a SPARQL endpoint
 * Return a Mappings
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 */
public class Service {

    public static final String QUERY = "query";
    public static final String MIME_TYPE = "application/sparql-results+xml,application/rdf+xml";
    static final String ENCODING = "UTF-8";
    private ClientBuilder clientBuilder;

    boolean isDebug = !true;
    String service;
    
    public Service() {
        clientBuilder = ClientBuilder.newBuilder();
    }

    public Service(String serv) {
        this(serv, ClientBuilder.newBuilder());
    }
    public Service(String serv, ClientBuilder builder) {
        service = serv;
        this.clientBuilder = builder;
    }


    public Mappings select(String query) throws LoadException {
        return parseMapping(process(query));
    }

    public Graph construct(String query) throws LoadException {
        return parseGraph(process(query));
    }

    public Mappings query(Query query, Mapping m) throws LoadException {
        ASTQuery ast = (ASTQuery) query.getAST();
        if (ast.hasMetadata(Metadata.LIMIT) && ! ast.hasLimit()) {
            ast.setLimit(ast.getMetadata().getDatatypeValue(Metadata.LIMIT).intValue());
        }      
        Mappings map;
        if (m != null) {
            mapping(query, m);
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

    void mapping(Query q, Mapping m) {
        Mappings map = new Mappings();
        map.add(m);
        CompileService cs = new CompileService();
        cs.filter(q, map, 0, 1);
    }

    public String process(String query) {
        return process(query, MIME_TYPE);
    }

    public String process(String query, String mime) {
        if (isDebug) {
            System.out.println(query);
        }
        Client client = clientBuilder.build();
        WebTarget target = client.target(service);
        Form form = new Form();
        form.param(QUERY, query);
        String res = target.request(mime).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);
        if (isDebug) {
            System.out.println(res);
        }
        return res;
    }
    
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

}
