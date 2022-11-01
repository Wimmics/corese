package fr.inria.corese.core.transform;

import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage load, import, profile
 * TODO: level()
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Loader {
    
    private static Logger logger = LoggerFactory.getLogger(Loader.class);
    private String STL = NSManager.STL;
    public static final String PPLIB = "/template/";
    private Level level = Level.USER_DEFAULT;
    
    
    Graph graph;
    NSManager nsm;
    Transformer trans;
    HashMap<String, String> loaded;
    // Dataset to be set into query templates at compile time
    // Use case: st:apply-templates-graph(st:turtle, stname)
    Dataset ds;
    QueryEngine qe;
    
    Loader(Transformer t, QueryEngine qe){
        graph = t.getGraph();
        nsm = t.getNSM();
        trans = t;
        loaded = new HashMap();
        this.qe = qe;
    }
    
    
    // Dataset to be set into query templates at compile time
    // Use case: st:apply-templates-graph(st:turtle, stname)    
    void setDataset(Dataset ds){
        this.ds = ds;
    }
    
    /**
     * Load transformation from .rq or .rul file(s)
     * .rq loaded in QueryEngine
     * .rul loaded in RuleEngine
     * Templates are eventually stored in QueryEngine
     */
     QueryEngine load(String pp) throws LoadException {
        RuleEngine re  = RuleEngine.create(graph); 
        // PP as list (not in a thread)
        qe.getQueryProcess().setListPath(true);
        qe.setTransformation(true);
        qe.setDataset(ds);
        re.setQueryEngine(qe);
        re.setDataset(ds);
                
        if (pp == null) {
            // skip;
        } else {
            loaded.put(pp, pp);
            // TODO: set access level for function definition
            Load ld = Load.create(graph);
            ld.setTransformer(true);
            ld.setLevel(getLevel());
            ld.setEngine(qe);
            ld.setEngine(re);           
            load(ld, qe, pp);
        }
          
        return qe;

    }
     
     /**
      * If path is file:///somewhere/sttl/
      * return the path /somewhere/sttl/
      * Hence load has access to transformation directory if any
      * Use case:
      * st:transform <demo/sttl/>
      */
     String toFile(String path) {
        try {
            URL url = new URL(path);
            if (url.getProtocol().startsWith("file")){
                return url.getFile();
            }
        } catch (MalformedURLException ex) {            
        }
        return path;
     }
       
     /**
     * Predefined transformations loaded from Corese resource or ns.inria.fr server
     */
    void load(Load ld, QueryEngine qe, String pp) throws LoadException {
        //System.out.println("Loader: " + pp);    
        String name = null;
        pp = clean(pp);
        // base for templates
        // use case: format { <format/main.html> ?x }
        qe.setBase(NSManager.toURI(pp));
        if (nsm.isPredefinedTransformation(pp)) {
            // predefined pprinter: st:owl st:spin
            // loaded from Corese resource
            name = nsm.strip(pp, STL);
        }  else {
            ld.parseDir(toFile(pp), Load.QUERY_FORMAT);
            return;
        }           
        String src = PPLIB + name;

        if (!ld.isRule(src)) {
            src = src + LoadFormat.RULE;                  
        }
        //System.out.println("Loader: " + src);    
        InputStream stream = getClass().getResourceAsStream(src);
        if (stream == null) { 
            try {
                URL uri = new URL(pp);                
                stream = uri.openStream();
            } catch (MalformedURLException ex) {
                throw LoadException.create(ex);
            } catch (IOException ex) {                
                throw LoadException.create(ex);
            }
            if (stream == null){
                throw LoadException.create(new IOException(pp), pp);
            }
        }
        // use non synchronized load method because we may be inside a query 
        // with a read lock       
        ld.loadRuleBasic(new InputStreamReader(stream), src);

    }
    
    // remove #
    String clean(String uri){
        return trans.getURI(uri);
    }
        

    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    
}
