
package fr.inria.corese.core.util;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.Transformer;
import java.io.InputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * Initialize a GraphStore
 * Store information in named graph kg:system
 * Load resources/data/kgram.ttl (describe kgram version)
 * Dump kgram properties into kg:system
 * 
 * Set a GraphListener on graph kg:system
 * Hence can tune kgram according to edge inserted
 * e.g. insert data { graph kg:system { kg:kgram kg:skolem true }}
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class GraphStoreInit {
    
    private static final String KG      = ExpType.KGRAM;
    private static final String KGRAM   = KG + "kgram";
    private static final String SYSTEM   = Graph.SYSTEM;
    private static final String PPRINTER = KG+"pprinter";
    private static final String DATE    = KG+"date";
    private static final String RUN     = KG+"run";
    private static final String INIT    = "/data/kgram.ttl";
    
    GraphStore gs;
    
    GraphStoreInit(GraphStore gs){
        this.gs = gs;
    }
    
    public static GraphStoreInit create(GraphStore gs){
        return new GraphStoreInit(gs);
    }
    
     public void init(){
        Graph g = gs.createNamedGraph(SYSTEM);
        load(g, INIT);
        dump(g);
        
        // listen insert on kg:system graph 
        // may tune the GraphStore
        g.addListener(new SystemGraphListener(gs));
    }
    
    
    
    
    
     /**
     * Draft
     * Initialize system graph with description of KGRAM
     * load resources/data/kgram.ttl
     * 
     */
    void load(Graph g, String name) {
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream != null) {
            Load ld = Load.create(g);
            try {
                ld.load(stream, SYSTEM, name);
            } catch (LoadException ex) {
                LogManager.getLogger(GraphStore.class.getName()).log(Level.ERROR, "", ex);
            }
        }
    }

    /**
     * 
     * Dump kgram into graph
     */
    void dump(Graph g){
        date(g);
        pprinter(g);
    }
    
    void date(Graph g){
        g.addEdge(g.addGraph(SYSTEM),
                    g.addResource(KGRAM),
                    g.addProperty(RUN),
                    g.getNode(DatatypeMap.newDate(), true, true));
    }
    
    /**
     * Dump Predefined PPrinters
     */
    void pprinter(Graph g) {
        for (String ns : Transformer.getTable().keySet()) {
            g.addEdge(g.addGraph(SYSTEM),
                    g.addResource(PPRINTER),
                    g.addProperty(ns),
                    g.addResource(Transformer.getPP(ns)));
        }
    }
    
    
    
    
    
    
    

}
