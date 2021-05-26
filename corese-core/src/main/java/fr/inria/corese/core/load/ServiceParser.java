package fr.inria.corese.core.load;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
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
     private boolean trap;
     private boolean showResult;
     private String format;
     private URLServer url;
     private Binding bind;
     
    public ServiceParser(URLServer url) {
         setURL(url);
    }
    
    public ServiceParser(String url) {
        this(new URLServer(url));
    }
   
    public Mappings parseMapping(String str) throws LoadException {
        return parseMapping("", str, ENCODING);
    }

    public Mappings parseMapping(String query, String str, String encoding) throws LoadException {
        Mappings map = null;
        log(str);
        if (getURL().hasParameter(WRAPPER)) {
            map = wrapper(str);
        }
        else if (getFormat() != null) {
            switch (getFormat()) {
                case ResultFormat.SPARQL_RESULTS_JSON:
                    map = parseJSONMapping(str); break;
                    
                case ResultFormat.TURTLE:
                case ResultFormat.TURTLE_TEXT:
                    map = parseTurtle(str); break;
                default:
                   map = parseXMLMapping(str, encoding); 
            }
        }
        else {     
            map = parseXMLMapping(str, encoding);
        }
        map.setLength(str.length());
        map.setQueryLength(query.length());
        return map;
    }  
    
    // @federate with one URL may have no bind
    synchronized void log(String result) {
        if (getBind()!=null){
            getBind().getLog().traceResult(getURL(), result);
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
    
    public Mappings parseTurtle(String str) throws LoadException {
        Graph g = Graph.create();
        Load ld = Load.create(g);
        ld.loadString(str, Load.TURTLE_FORMAT);
        Mappings map = new Mappings();
        map.setGraph(g);
        return map;
    }
    
    public Mappings parseJSONMapping(String str) {
        SPARQLJSONResult res = new SPARQLJSONResult(Graph.create());
        Mappings map = res.parse(str);
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
     * @return the url
     */
    public URLServer getURL() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setURL(URLServer url) {
        this.url = url;
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
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the bind
     */
    public Binding getBind() {
        return bind;
    }

    /**
     * @param bind the bind to set
     */
    public void setBind(Binding bind) {
        this.bind = bind;
    }

    
    
}
