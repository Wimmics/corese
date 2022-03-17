package fr.inria.corese.core.transform;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;

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
    
    Graph graph;
    Context context;
    HashMap<String, Node> done; 
    
    public ContextBuilder(Graph g){
        this.graph = g;
        done = new HashMap<String, Node>();
        context = new Context();
    }
    
    public ContextBuilder(String path){
        this(Graph.create());
        Load ld = Load.create(graph);
        try {
            ld.parse(path);
        } catch (LoadException ex) {
            LoggerFactory.getLogger(ContextBuilder.class.getName()).error(  "", ex);
        }
    }
    
    /**
     * 
     * Create a Context from content of st:param
     */
    public Context process(){
        Edge ent = graph.getEdge(Context.STL_PARAM);
        if (ent == null){
            return context;
        }
        return process(ent.getNode(1));
    }
    
    public Context process(Node ctx){
        //context = new Context();
        context(ctx, false);
        return context;
    }
    
    public ContextBuilder setContext(Context c){
        context = c;
        return this;
    }
        
    void context(Node ctx, boolean exporter) {
        importer(ctx);

        for (Edge ent : graph.getEdgeList(ctx)) {
            String label = ent.getEdgeLabel();
            Node object = ent.getNode(1);
            
            if (label.equals(Context.STL_EXPORT) && object.isBlank()){
                // st:export [ st:lod (<http://dbpedia.org/>) ]
                context(object, true);
            }
            else if (! label.equals(Context.STL_IMPORT)) {

                if (object.isBlank()) {
                    IDatatype list = list(graph, object);
                    if (list != null) {
                        set(label, list, exporter);
                        continue;
                    }
                }
                set(label,  object.getValue(), exporter);
            }
        }        
    }
    
    void set(String name, IDatatype dt, boolean b){
        if (b){
            context.export(name, dt);
        }
        else {
            context.set(name, dt);
        }
    }
    
     /** 
      *           
      */
     void importer(Node n) {         
        for (Edge ent : graph.getEdges(Context.STL_IMPORT, n, 0)) {
            if (ent != null){
                Node imp = ent.getNode(1);
                if (done(imp)){
                    continue;
                }
                Edge par = graph.getEdge(Context.STL_PARAM, imp, 0);
                if (par != null) {
                    context(par.getNode(1), false);
                }
            }
        }
     }
     
     boolean done(Node n) {
        if (done.containsKey(n.getLabel())) {
            return true;
        } else {
            done.put(n.getLabel(), n);
        }
        return false;
    }
             
    IDatatype list(Graph g, Node object) {
        List<IDatatype> list = g.reclist(object);
        if (list == null) {           
            return null;
        }
       IDatatype dt = DatatypeMap.createList(list);
       return dt;
    }
    

}
