package fr.inria.corese.core.print;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for Exception throwned during query processing Manager generates
 * Turtle representation of exceptions
 *
 * 1) local exceptions managed as a list in Query AST Context 2) remote endpoint
 * service exception managed as list of URI of Turtle document in Query AST
 * Context URI inserted in SPARQL Query Results XML Format link href and stored
 * in Mappings
 *
 * API: LogManager man = exec.getLogManager(map);
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2021
 */
public class LogManager implements LogKey {

    private static Logger logger = LoggerFactory.getLogger(LogManager.class);

    ContextLog log;
    StringBuilder sb;
    private boolean debug = false;

    public LogManager(ContextLog log) {
        this.log = log;
    }

    /**
     * Generate Turtle format for describing exceptions
     */
    @Override
    public String toString() {
        return process();
    }

    /**
     * Generate Turtle format for exceptions, parse Turtle and return RDF graph
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
        for (Edge e : g.getEdges(NS + name)) {
            Node n = e.getNode(1);
            if (!list.contains(n.getLabel())) {
                list.add(n.getLabel());
            }
        }
        return list;
    }

    public List<IDatatype> getPropertyList(Graph g, String name) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge e : g.getEdges(NS + name)) {
            Node n = e.getNode(1);
            IDatatype dt = g.list(n);
            list.add(dt);
        }
        return list;
    }

    /**
     * Create Turtle with local exception list and remote error document URI
     * list
     */
    public String process() {
        sb = new StringBuilder();
        main();
        processLocal();
        processRemote();
        processMap();
        sb.append("\n");
        return sb().toString();
    }

    void main() {
        sb.append(String.format("prefix %s <%s>\n", PREF, NS));

//        property("[] a %s ;\n ", EVALUATION_REPORT);
//        property("%s %s . \n",   DATE, DatatypeMap.newDate());
        if (log.getAST() != null) {
            property("[] %s \"\"\"\n%s\"\"\" .\n", AST, log.getAST());
        }
        if (log.getASTSelect() != null) {
            property("[] %s \"\"\"\n%s\"\"\" .\n", AST_SELECT, log.getASTSelect());
        }
        if (log.getSelectMap() != null) {
            property("[] %s \"\"\"\n%s\"\"\" .\n", RESULT_SELECT, log.getSelectMap());
        }
    }

    String pretty(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String str : list) {
            if (str.startsWith("http://") || str.startsWith("https://")) {
                sb.append(String.format("<%s> ", str));
            } else {
                sb.append(String.format("\"%s\" ", str));
            }
        }
        sb.append(")");
        return sb.toString();
    }

    // local exception list
    void processLocal() {
        for (EngineException e : log.getExceptionList()) {
            process(e);
            //sb.append("\n");
        }
    }

    // remote error document URI list
    void processRemote() {
        for (String url : log.getLinkList()) {
            processLinkTurtle(url);
        }
    }
    
    
    void processLinkTurtle(String url) {
        if (url.endsWith(".ttl")) {
            processLink(url);
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

    void processMap() {
        getLog().getSubjectMap().display(sb);
    }

    /**
     * Generate Turtle representation of Exception, append string to buffer
     */
//    void process(EngineException e) {
//        property("%s a ns:ServiceReport; \n", "[]");
//        if (e.getURL() != null) {
//            property("%s <%s>; \n", URL, e.getURL().getServer());
//        }
//
//        if (e.getCause() instanceof ResponseProcessingException) {
//            Response resp = (Response) e.getObject();
//            if (resp != null) {
//                property("%s \"%s\" ; \n", INFO, resp.getStatusInfo());
//                property("%s %s ; \n", STATUS, resp.getStatus());
//                property("%s \"%s\" ; \n", SERVER, getServer(resp));
//                
//                property("%s \"%s\" ; \n", DATE, resp.getHeaderString("Date"));
//
//                trace(e.getURL(), resp);
//            }
//        }
//
//        property("%s \"\"\" %s \"\"\" ;\n", MESSAGE, e.getMessage());
//        property("%s \"\"\"\n%s\"\"\" \n", QUERY, e.getAST());
//        sb.append(".\n");
//    }

    void process(EngineException e) {
        ContextLog log = getLog();
        String sub = DatatypeMap.createBlank().getLabel();

        log.set(sub, "a", REPORT);
        if (e.getURL() != null) {
            log.set(sub, URL, e.getURL().getServer());
        }

        if (e.getCause() instanceof ResponseProcessingException) {
            Response resp = (Response) e.getObject();
            if (resp != null) {
                log.set(sub, INFO, resp.getStatusInfo().toString());
                log.set(sub, STATUS, resp.getStatus());
                String serv = getServer(resp);
                if (serv != null) {
                    log.set(sub, SERVER, serv);
                }
                if (resp.getHeaderString("Date") != null){
                    log.set(sub, DATE, resp.getHeaderString("Date"));
                }

                trace(e.getURL(), resp);
            }
        }

        log.set(sub, MESSAGE, e.getMessage());
        log.set(sub, QUERY, DatatypeMap.genericPointer(e.getAST().toString()));
    }

    void property(String format, Object... obj) {
        if (obj.length >= 2 && obj[1] == null) {
            return;
        }
        sb.append(String.format(format, obj));
    }

    String getServer(Response resp) {
        if (resp.getHeaderString("Server") != null) {
            return resp.getHeaderString("Server");
        }
        return resp.getHeaderString("server");
    }

    void trace(URLServer url, Response resp) {
        if (isDebug()) {
            System.out.println("LogManager: " + url.getURL());
            System.out.println(resp.getStatusInfo() + " " + resp.getStatus());
            for (String name : resp.getHeaders().keySet()) {
                System.out.println(String.format("header %s=%s", name, resp.getHeaderString(name)));
            }
            for (String name : resp.getCookies().keySet()) {
                System.out.println(String.format("cookie %s=%s", name, resp.getCookies().get(name)));
            }
            for (Link name : resp.getLinks()) {
                System.out.println(String.format("link %s", name));
            }
            System.out.println();
        }
    }

    // trace
    public void trace(List<EngineException> list) {
        for (EngineException e : list) {
            if (e.getCause() instanceof ResponseProcessingException) {
                Response resp = (Response) e.getObject();
            }
        }
    }

    StringBuilder sb() {
        return sb;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public ContextLog getLog() {
        return log;
    }

}
