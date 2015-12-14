package fr.inria.edelweiss.kgtool.load;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Send a SPARQL query to a SPARQL endpoint
 * Return a Mappings
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Service {

    public static final String QUERY = "query";
    public static final String MIME_TYPE = "application/sparql-results+xml,application/rdf+xml";
    
    boolean isDebug = !true;
    String service;

    public Service(String serv){
        service = serv;
    }
    
    
    
    public Mappings select(String query) throws LoadException{
        return parseMapping(process(query));
    }
    
    public Graph construct(String query) throws LoadException{
        return parseGraph(process(query));
    }
    
     public Mappings query(Query query) throws LoadException{
        ASTQuery ast = (ASTQuery) query.getAST();
        Mappings map;
        if (ast.isSelect() || ast.isAsk()){
           map = parseMapping(process(ast.toString())); 
        }
        else {
            Graph g = parseGraph(process(ast.toString()));
            map = new Mappings();
            map.setGraph(g);
        }
        map.setQuery(query);
        map.init(query);
        return map;
    }
    
    public String process(String query) {
         return process(query, MIME_TYPE);
     }
     
    
    public String process(String query, String mime) {
        if (isDebug){
            System.out.println(query);
        }
        Client client = Client.create();
        WebResource resource = client.resource(service);
        String res = resource.queryParam(QUERY, query)
                .accept(mime)
                .post(String.class);
        if (isDebug){
            System.out.println(res);
        }
        return res;
    }
    
    public Mappings parseMapping(String str) throws LoadException {
        SPARQLResult xml = SPARQLResult.create(Graph.create());
        try {
            Mappings map = xml.parseString(str);
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
    
   public Graph parseGraph(String str) throws LoadException{
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadString(str, Load.RDFXML_FORMAT);
        return g;
    }

}
