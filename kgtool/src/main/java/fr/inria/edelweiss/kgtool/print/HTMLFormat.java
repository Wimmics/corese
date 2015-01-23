package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.util.MappingsProcess;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Apply HTML Transformation on Mappings or on Graph :
 * construct : result graph
 * select    : serialization of Mappings into RDF
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class HTMLFormat {
    static final String defaultTransform   = Transformer.SPARQL;
    static final String constructTransform = Transformer.SPARQL;
    static final String selectTransform    = Transformer.SPARQL;
    private String transformation;
    
    Mappings map;
    Graph graph;
    private Context context;
    NSManager nsm;
    
    HTMLFormat(Graph g, Mappings m, Context c){
        graph = g;
        map = m;
        context = c;
        nsm = NSManager.create();
    }
    
    
    
    public static HTMLFormat create(Graph g, Mappings m, Context c){
        return new HTMLFormat(g, m, c);
    }
    
//    public static HTMLFormat create(Mappings m){
//        return new HTMLFormat(null, m);
//    }
//    
    public static HTMLFormat create(Graph g, Context c){
        return new HTMLFormat(g, null, c);
    }
    
    public String toString(){
        return process();
    }
    
    public void setContext(Context c){
        context = c;
    }
    
    String process(){
        transformation = context.stringValue(Context.STL_TRANSFORM);
        if (map == null){
            // no query processing
           return process(graph, getTransformation(transformation));        
        }
        else if (map.getQuery().isTemplate()){
            // the query was a template
            if (map.getTemplateResult() != null){
               return map.getTemplateStringResult();
            }
            else {
                return "";
            }
        }
        else if (map.getGraph()!=null){
            // query was construct where 
            Graph g = (Graph) map.getGraph();
            return process(g, getTransformation(constructTransform));       
        }
        else if (map.getQuery().isUpdate() && graph != null){
            return process(graph, getTransformation(constructTransform));       
        }
        else {
            // query was select where
            // generate a RDF graph with bindings of select
            Graph g = select();
            // process the RDF graph of bindings
            return process(g, getTransformation(selectTransform));   
        }
    }
    
    String getTransformation(String def){
        if (transformation != null){
            return expand(transformation);
        }
        if (def != null){
           return expand(def); 
        }
       return defaultTransform;
    }
    
    String process(Graph g, String trans){  
        Transformer t = Transformer.create(g, trans);
        context.set(Transformer.STL_TRANSFORM, trans);
        // triple store graph has a st:context graph
        // add it to the transformer context
        Graph cg = graph.getNamedGraph(Context.STL_CONTEXT);
        if (cg != null){
            context.set(Context.STL_CONTEXT, DatatypeMap.createObject(Context.STL_CONTEXT, cg));
        }
        t.setContext(context);
        return t.toString();
    }
    
    String expand(String str){
        //ASTQuery ast = (ASTQuery) map.getQuery().getAST();
        return nsm.toNamespace(str);
    }
      
    
    Graph select2(){
        RDFResultFormat rdf = RDFResultFormat.create(map);
        Graph g = Graph.create();
        Load ld = Load.create(g);
        try {
            ld.loadString(rdf.toString(), Load.TURTLE_FORMAT);
        } catch (LoadException ex) {
            Logger.getLogger(HTMLFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return g;             
    }
    
    Graph select(){
        MappingsProcess mp = MappingsProcess.create(map);
        return mp.getGraph(); 
    }

    /**
     * @return the transformation
     */
    public String getTransformation() {
        return transformation;
    }

    /**
     * @param transformation the transformation to set
     */
    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }


}
