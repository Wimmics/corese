package fr.inria.edelweiss.kgtool.load;

import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgtool.transform.Transformer;
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
    static final String JSONLD = ".jsonld";
    static final String SWF = ".sw";

    static HashMap<String, Integer> ptable, utable, dtable;
    
    static {
        init();
    }
    
    static void init(){
        ptable = new HashMap<String, Integer>();
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
        define(XML,     Loader.RDFA_FORMAT);
        define(SVG,     Loader.RDFA_FORMAT);
        define(EXT_RDF, Loader.RDFXML_FORMAT);
        define(EXT_RDFS,Loader.RDFXML_FORMAT);
        define(EXT_OWL, Loader.RDFXML_FORMAT);
        define(JSONLD,  Loader.JSONLD_FORMAT);
        define(SWF,     Loader.WORKFLOW_FORMAT);
        
        utable = new HashMap<String, Integer>();
        udefine("text/html",        Loader.RDFA_FORMAT);
        udefine("text/turtle",      Loader.TURTLE_FORMAT);
        udefine("text/n3",          Loader.NT_FORMAT);
        udefine("text/trig",        Loader.TRIG_FORMAT);
        udefine("text/n-quads",     Loader.NQUADS_FORMAT);
        udefine("application/rdf+xml", Loader.RDFXML_FORMAT);
        udefine("application/json",    Loader.JSONLD_FORMAT);
        udefine("application/ld+json", Loader.JSONLD_FORMAT);
        
        
        dtable = new HashMap<String, Integer>();
        ddefine(Transformer.TURTLE, Loader.TURTLE_FORMAT);
        ddefine(Transformer.RDFXML, Loader.RDFXML_FORMAT);
        ddefine(Transformer.JSON,   Loader.JSONLD_FORMAT);
        
   }
    
    static void define(String extension, int format){
        ptable.put(extension, format);
    }
    
     static void udefine(String extension, int format){
        utable.put(extension, format);
    }
     
       static void ddefine(String extension, int format){
        dtable.put(extension, format);
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
