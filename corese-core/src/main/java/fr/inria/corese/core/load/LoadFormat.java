package fr.inria.corese.core.load;

import fr.inria.corese.core.api.Loader;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.HashMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class LoadFormat {
    
    public static final String RULE = ".rul";
    static final String BRULE = ".brul";
    static final String IRULE = ".rl";
    static final String QUERY = ".rq";
    static final String UPDATE = ".ru";
    static final String TURTLE = ".ttl";
    static final String NT = ".nt";
    static final String N3 = ".n3";
    static final String TRIG = ".trig";
    static final String NQUADS = ".nq";
    static final String HTML = ".html";
    static final String XHTML = ".xhtml";
    static final String SVG = ".svg";
    static final String XML = ".xml";
    static final String EXT_RDF = ".rdf";
    static final String EXT_RDFS = ".rdfs";
    static final String EXT_OWL = ".owl";
    static final String OWL_RDFXML = ".owx";
    static final String JSONLD = ".jsonld";
    static final String JSON = ".json";
    static final String SWF = ".sw";
    
    static final String NT_FORMAT = NSManager.STL + "nt";

    static HashMap<String, Integer> ptable, utable, dtable;
    static HashMap<Integer, String> ftable;
    
    static {
        init();
    }
    
    static void init(){
        ptable = new HashMap<>();
        define(BRULE,   Loader.RULE_FORMAT);
        define(IRULE,   Loader.RULE_FORMAT);
        define(RULE,    Loader.RULE_FORMAT);
        define(QUERY,   Loader.QUERY_FORMAT);
        define(UPDATE,  Loader.QUERY_FORMAT);
        define(TURTLE,  Loader.TURTLE_FORMAT);
        define(NT,      Loader.NT_FORMAT);
        define(N3,      Loader.NT_FORMAT);
        define(TRIG,    Loader.TRIG_FORMAT);
        define(NQUADS,  Loader.NQUADS_FORMAT);
        define(HTML,    Loader.RDFA_FORMAT);
        define(XHTML,   Loader.RDFA_FORMAT);
        define(XML,     Loader.XML_FORMAT);
        define(SVG,     Loader.RDFA_FORMAT);
        define(EXT_RDF, Loader.RDFXML_FORMAT);
        define(EXT_RDFS,Loader.RDFXML_FORMAT);
        define(OWL_RDFXML, Loader.RDFXML_FORMAT);
        define(EXT_OWL, Loader.OWL_FORMAT);
        define(JSONLD,  Loader.JSONLD_FORMAT);
        define(JSON,    Loader.JSON_FORMAT);
        define(SWF,     Loader.WORKFLOW_FORMAT);
        
        utable = new HashMap<>();
        ftable = new HashMap<>();
        
        udefine(Loader.HTML_FORMAT_STR,   Loader.RDFA_FORMAT);
        udefine(Loader.TURTLE_FORMAT_STR, Loader.TURTLE_FORMAT);
        udefine(Loader.NT_FORMAT_STR,     Loader.NT_FORMAT);
        udefine(Loader.TRIG_FORMAT_STR,   Loader.TRIG_FORMAT);
        udefine(Loader.NQUADS_FORMAT_STR, Loader.NQUADS_FORMAT);
        udefine(Loader.RDFXML_FORMAT_STR, Loader.RDFXML_FORMAT);
        udefine(Loader.JSON_FORMAT_STR,   Loader.JSONLD_FORMAT);
        udefine(Loader.JSONLD_FORMAT_STR, Loader.JSONLD_FORMAT);
        udefine(Loader.ALL_FORMAT_STR,    Loader.OWL_FORMAT);
        
        
        dtable = new HashMap<String, Integer>();
        ddefine(Transformer.TURTLE, Loader.TURTLE_FORMAT);
        ddefine(NT_FORMAT,          Loader.NT_FORMAT);
        ddefine(Transformer.RDFXML, Loader.RDFXML_FORMAT);
        ddefine(Transformer.JSON,   Loader.JSONLD_FORMAT);
        
   }
      
    static void define(String extension, int format){
        ptable.put(extension, format);
    }
    
    static void udefine(String extension, int format){
        utable.put(extension, format);
        ftable.put(format, extension);
    }
     
    static void ddefine(String extension, int format){
        dtable.put(extension, format);
    }
    
    public static String getFormat(int format) {
        return ftable.get(format);
    }
    
    public static int getFormat(String path){
        if (path == null){
            return Loader.UNDEF_FORMAT;
        }
        int index = path.lastIndexOf(".");
        if (index == -1){
            return Loader.UNDEF_FORMAT;
        }
        String ext = path.substring(index);
        Integer format = ptable.get(ext);
        if (format == null){
           return Loader.UNDEF_FORMAT; 
        }
        return format;
    }
    
    static int getTypeFormat(String contentType, int format) {
        for (String key : utable.keySet()) {
            if (contentType.startsWith(key)) {
                return utable.get(key);
            }
        }
        return format;
    }
    
    public static int getDTFormat(String format){
        Integer ft = dtable.get(format);
        if (ft == null){
            return Loader.UNDEF_FORMAT;
        }
        return ft;
    }
      
}
