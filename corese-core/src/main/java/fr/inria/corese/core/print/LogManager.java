package fr.inria.corese.core.print;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for Exception throwned during query processing
 * Manager generates Turtle representation of exceptions
 * 
 * 1) local exceptions managed as a list in Query AST Context
 * 2) remote endpoint service exception managed as list of URI of Turtle document
 * in Query AST Context
 * URI inserted in SPARQL Query Results XML Format link href and stored in Mappings
 * 
 * API: LogManager man = exec.getLogManager(map);
 */
public class LogManager {
    private static Logger logger = LoggerFactory.getLogger(LogManager.class);
    static final String NS = "http://ns.inria.fr/corese/log/";  
    
    private List<EngineException> exceptionList;
    private List<String> linkList;
    private List<String> urlList;
    StringBuilder sb;
    
    public LogManager(List<EngineException> list) {
        this(list, null);
    }
    
    public LogManager(List<EngineException> list, List<String> linkList) {
        setExceptionList(list);
        setLinkList(linkList);
    }
    
    /**
     * Generate Turtle format for describing exceptions
     */
    @Override
    public String toString() {
        return process();
    }
    
    /**
     * Generate Turtle format for exceptions,  parse Turtle and return RDF graph
     */
    public Graph parse() throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadString(process(), Load.TURTLE_FORMAT);
        return g;
    }
    
    /**
     * Return list of endpoint URL that generate exception
     */        
    public List<String> getEndpointURL(Graph g) {
        return getProperty(g, "url");
    }
    
    
    public List<String> getProperty(Graph g, String name) {
        ArrayList<String> list = new ArrayList<>();
        for (Edge e : g.getEdges(NS+name)) {
            Node n = e.getNode(1);
            if (! list.contains(n.getLabel())) {
                list.add(n.getLabel());
            }
        }
        return list;
    }
    
    public List<IDatatype> getPropertyList(Graph g, String name) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge e : g.getEdges(NS+name)) {
            Node n = e.getNode(1);
            IDatatype dt = g.list(n);
            list.add(dt);
        }
        return list;
    }
    
    /**
     * Create Turtle with local exception list and remote error document URI list
     */
    public String process() {
        sb = new StringBuilder();
        sb.append(String.format("prefix ns: <%s>\n", NS));
        processLocal();
        processRemote();
        processURL();
        return sb().toString();
    }
    
    // local exception list
    void processLocal() {
        if (getExceptionList() != null) {
            for (EngineException e : getExceptionList()) {
                process(e);
                sb.append("\n");
            }
        }
    }
    
    // remote error document URI list
    void processRemote() {
        if (getLinkList() != null) {
            for (String url : getLinkList()) {
                processLink(url);
            }
        }
    }
    
    /**
     * read document at URL and append string to buffer
     */
    void processLink(String url) {
        QueryLoad ql = QueryLoad.create();
        try {
            sb.append(ql.readURL(url));
        } catch (LoadException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    void processURL() {
        if (getURLList() !=null && !getURLList().isEmpty()) {
            sb.append("[] ns:endpoint (\n");
            for (String url : getURLList()) {
                sb.append(String.format("<%s>\n", url));
            }
            sb.append(") .\n");
        }
    }
    
    /**
     * Generate Turtle representation of Exception, append string to buffer
     */
    void process(EngineException e) {
        sb.append("[] ");
        if (e.getURL() != null) {
            sb.append(String.format("ns:url <%s>; \n", e.getURL().getServer()));
        }
        
        if (e.getCause() instanceof ResponseProcessingException) {
            Response resp = (Response) e.getObject();
            sb.append(String.format("ns:info \"%s\" ; \n", resp.getStatusInfo()));
            sb.append(String.format("ns:status %s ; \n",   resp.getStatus()));
            String server = getServer(resp);
            if (server!=null) {
                sb.append(String.format("ns:server \"%s\" ; \n", server));
            }
            sb.append(String.format("ns:date \"%s\" ; \n", resp.getHeaderString("Date")));           
        }
        
        sb.append(String.format("ns:message \"\"\" %s \"\"\" ;\n", e.getMessage()));
        if (e.getAST() != null) {
            sb.append(String.format("ns:query \"\"\"\n%s\"\"\" \n", e.getAST()));
        }
        sb.append(".\n");
    }
    
    String getServer(Response resp) {
        if (resp.getHeaderString("Server") != null) {
            return resp.getHeaderString("Server");
        } 
        return resp.getHeaderString("server");
    }


    // trace
    public void trace(List<EngineException> list) {
        for (EngineException e : list) {
            if (e.getCause() instanceof ResponseProcessingException) {
                Response resp = (Response) e.getObject();
                System.out.println(e.getURL().getServer());
                System.out.println(resp.getStatusInfo() + " " + resp.getStatus());
                System.out.println(e.getMessage());
                //System.out.println(e.getAST());
            }
        }
    }

    /**
     * @return the exceptionList
     */
    public List<EngineException> getExceptionList() {
        return exceptionList;
    }

    /**
     * @param exceptionList the exceptionList to set
     */
    public void setExceptionList(List<EngineException> exceptionList) {
        this.exceptionList = exceptionList;
    }
    
    StringBuilder sb() {
        return sb;
    }

    /**
     * @return the linkList
     */
    public List<String> getLinkList() {
        return linkList;
    }

    /**
     * @param linkList the linkList to set
     */
    public void setLinkList(List<String> linkList) {
        this.linkList = linkList;
    }

    /**
     * @return the urlList
     */
    public List<String> getURLList() {
        return urlList;
    }

    /**
     * @param urlList the urlList to set
     */
    public LogManager setURLList(List<String> urlList) {
        this.urlList = urlList;
        return this;
    }
    
}
