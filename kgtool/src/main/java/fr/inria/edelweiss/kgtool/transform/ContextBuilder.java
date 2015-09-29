package fr.inria.edelweiss.kgtool.transform;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extract a Transformer Context from a profile.ttl graph st:param object
 * st:cal a st:Profile ;
 *   st:transform st:calendar ;
 *   st:param [ st:arg value ; st:title value ] .
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ContextBuilder {
    
    Graph g;
    Context c;
    
    public ContextBuilder(Graph g){
        this.g = g;
    }
    
    public ContextBuilder(String path){
        g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadWE(path);
        } catch (LoadException ex) {
            Logger.getLogger(ContextBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * Create a Context from content of st:param
     */
    public Context process(){
        Entity ent = g.getEdges(Context.STL_PARAM).iterator().next();
        if (ent == null){
            return new Context();
        }
        return process(ent.getNode(1));
    }
    
    public Context process(Node ctx){
        c = new Context();
        context(ctx);
        return c;
    }
        
    void context(Node ctx) {
        importer(ctx);

        for (Entity ent : g.getEdgeList(ctx)) {
            if (!ent.getEdge().getLabel().equals(Context.STL_IMPORT)) {
                Node object = ent.getNode(1);

                if (object.isBlank()) {
                    IDatatype list = list(g, object);
                    if (list != null) {
                        c.set(ent.getEdge().getLabel(), list);
                        continue;
                    }
                }
                c.set(ent.getEdge().getLabel(), (IDatatype) object.getValue());
            }
        }        
    }
    
     /** 
      *       
      * TODO: prevent loop 
      * n =  [ st:import st:cal ]
      * st:cal st:param [ ... ] 
      * 
      */
     void importer(Node n){
         Edge imp = g.getEdge(Context.STL_IMPORT, n, 0);        
          if (imp != null){
                Edge par = g.getEdge(Context.STL_PARAM, imp.getNode(1), 0);
                if (par != null){
                    context(par.getNode(1));
                }
            }
     }
        
    
    IDatatype list(Graph g, Node object) {
        List<IDatatype> list = g.getDatatypeList(object);
        if (! list.isEmpty()) {           
            IDatatype dt = DatatypeMap.createList(list);
            return dt;
        }
        return null;
    }
    

}
