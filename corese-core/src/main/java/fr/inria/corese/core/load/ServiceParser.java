package fr.inria.corese.core.load;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.result.SPARQLJSONResult;
import fr.inria.corese.core.load.result.SPARQLResult;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * Parser for service result
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2021
 */
public class ServiceParser implements URLParam {
     public static final String ENCODING = "UTF-8";
     public static boolean DISPLAY_MESSAGE = false;
     private boolean trap;
     private boolean showResult;
     private String format;
     private URLServer url;
     private Binding bind;
     private boolean log = false;
     private ServiceReport report;
     
    public ServiceParser(URLServer url) {
         setURL(url);
    }
    
    public ServiceParser(String url) {
        this(new URLServer(url));
    }
   
//    public Mappings parseMapping(String str) throws LoadException {
//        return parseMapping(null, "", str, ENCODING);
//    }

    public Mappings parseMapping(Query q, String query, String str, String encoding) 
            throws LoadException {
        Mappings map = null;
        log(str);
        boolean suc = true;
        if (getURL().hasParameter(WRAPPER)) {
            map = wrapper(str);
        }
        else if (getFormat() != null) {
            switch (getFormat()) {
                case ResultFormat.SPARQL_RESULTS_JSON:
                    map = parseJSONMapping(str);
                    break;

                case ResultFormat.SPARQL_RESULTS_XML:
                    map = parseXMLMapping(str, encoding);
                    break;
                    
                case ResultFormat.SPARQL_RESULTS_CSV:
                case ResultFormat.SPARQL_RESULTS_TSV:
                    Service.logger.warn(
                         "Format not handled by local parser: " + getFormat());
                    map = Mappings.create(q);
                    suc = false;
                    break;
                    
                    
                    // W3C Query Results RDF Format
                    // use case: @federate url with format=rdfxml|turtle|jsonld
                    // return query results as a graph in a Mappings, for testing prupose
                case ResultFormat.TURTLE:
                case ResultFormat.TURTLE_TEXT:
                case ResultFormat.RDF_XML:
                case ResultFormat.JSON_LD:
                    map = parseGraphMapping(q, str, encoding); 
                    break;                    

                default:
                    Service.logger.warn(
                    String.format("Format not handled by local parser: %s %s", getFormat(), getURL().getServer()));
                    if (DISPLAY_MESSAGE) {
                        Service.logger.info(str.substring(0, Math.min(800, str.length())));
                    }
                    map = Mappings.create(q).setFake(true);
                    suc = false;
            }
        }
        else {     
            map = parseXMLMapping(str, encoding);
        }
        map.setLength(str.length());
        map.setQueryLength(query.length());
        getReport(q).parserReport(map, str, suc);
        return map;
    } 
        
    
    public Mappings parseGraphMapping(Query q, String str, String encoding) throws LoadException {
        Graph g = parseGraph(q, str, encoding);
        Mappings map = Mappings.create(q);
        map.setGraph(g);
        getReport(q).parserReport(map, str, true);
        return map;
    }

    
    public Graph parseGraph(String str) throws LoadException {
        return parseGraph(null, str, ENCODING);
    }
    
    public Graph parseGraph(Query q, String str) throws LoadException {
        return parseGraph(q, str, ENCODING);
    }

    public Graph parseGraph(Query q, String str, String encoding) throws LoadException {
        try {
            Graph g = Graph.create();
            Load ld = Load.create(g);

            if (getFormat() != null) {
                switch (getFormat()) {
                    case ResultFormat.RDF_XML:
                        ld.loadString(str, Load.RDFXML_FORMAT);
                        break;
                    case ResultFormat.JSON_LD:
                        ld.loadString(str, Load.JSONLD_FORMAT);
                        break;
                    case ResultFormat.TURTLE:
                    case ResultFormat.TURTLE_TEXT:
                        ld.loadString(str, Load.TURTLE_FORMAT);
                        break;
                    default:
                        Service.logger.warn(
                          "Format not handled by local parser: " + getFormat());
                }
            } else {
                ld.loadString(str, Load.RDFXML_FORMAT);
            }
            return g;
        } catch (LoadException e) {
            if (isLog() && getLog() != null) {
                    getLog().addException(
                       new EngineException(e, e.getMessage()).setURL(getURL()));
            }
            throw e;
        }
    }
    
    synchronized void log(String result) {
        if (getLog()!=null){
            getLog().traceResult(getURL(), result);
        }
    }
    
    /**
     * URL = /sparql?wrapper=functionName
     * Parse service string result str by calling wrapper ldscript function 
     * Wrapper return e.g. RDF graph
     */
    Mappings wrapper(String str) throws LoadException  {
        String name = getURL().getParameter(WRAPPER);
        String fname = NSManager.nsm().toNamespace(name);
        IDatatype dt;
        try {
            dt = QueryProcess.create().funcall(fname, getBind(), DatatypeMap.newInstance(str));
            if (getURL().hasParameter(MODE, SHOW)) {
                System.out.println("wrap: "+ name);
                System.out.println(dt);
            }
        } catch (EngineException ex) {
            Service.logger.error("Service wrapper error: " + name);
            throw new LoadException(ex);
        }
        
        if (dt != null && dt.isExtension()) { 
            if (dt.getDatatypeURI().equals(IDatatype.GRAPH_DATATYPE)) {
                Mappings map = new Mappings();
                map.setGraph(dt.getPointerObject().getTripleStore());
                return map;
            }
            else if (dt.getDatatypeURI().equals(IDatatype.MAPPINGS_DATATYPE)) {
                // corner case where the wrapper overload service query and 
                // return query result
                return dt.getPointerObject().getMappings();
            }
        }
        
        throw new LoadException(new IOException(String.format("Wrapper %s fail on service result", name)));
    }    
    
    public Mappings parseJSONMapping(String str) {
        SPARQLJSONResult res = new SPARQLJSONResult();
        Mappings map = res.parseString(str);
        return map;
    }
    
    public Mappings parseXMLMapping(String str, String encoding) throws LoadException {
        SPARQLResult xml = SPARQLResult.create(Graph.create());
        xml.setTrapError(isTrap());
        xml.setShowResult(isShowResult());
        try {
            Mappings map = xml.parseString(str, encoding);            
            return map;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw LoadException.create(ex);
        }
    }



   
    public URLServer getURL() {
        return url;
    }

   
    public void setURL(URLServer url) {
        this.url = url;
    }

    
    public boolean isTrap() {
        return trap;
    }

   
    public void setTrap(boolean trap) {
        this.trap = trap;
    }

    
    public boolean isShowResult() {
        return showResult;
    }

   
    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    
    public String getFormat() {
        return format;
    }
    
    String getFormatText() {
        return (getFormat()==null)?"undefined":getFormat();
    }
    
    public void setFormat(String format) {
        this.format = format;
    }

    // @todo: synchronized wrt ProviderService & Service
    ContextLog getLog() {
        if (getBind() == null) {
            return null;
        }
        return getBind().getCreateLog();
    }
    
    public Binding getBind() {
        return bind;
    }

    
    public void setBind(Binding bind) {
        this.bind = bind;
    }

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }

    public ServiceReport getReport() {
        return report;
    }
    
    public ServiceReport getReport(Query q) {
        return getReport().setQuery(q);
    }

    public void setReport(ServiceReport report) {
        this.report = report;
    }

    
    
}
