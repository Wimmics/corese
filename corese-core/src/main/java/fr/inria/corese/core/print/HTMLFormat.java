package fr.inria.corese.core.print;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.MappingsGraph;

/**
 * Apply HTML Transformation on Query result Mappings or on Graph dataset
 * Used by STTL Server
 * construct : result graph
 * select    : serialization of Mappings into RDF
 * used by corese server SPIN service
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
           return process(map, graph, getTransformation(transformation));        
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
            return process(map, g, getTransformation(constructTransform));       
        }
        else if (map.getQuery().isUpdate() && graph != null){
            return process(map, graph, getTransformation(constructTransform));       
        }
        else {
            // query was select where
            // generate a RDF graph with bindings of select
            Graph g = select();
            // process the RDF graph of bindings
            return process(map, g, getTransformation(selectTransform));   
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
    
    /**
     * Transformer Context:
     * st:context = additional st:context graph such as a default SPARQL query graph (SPARQL Tutorial)
     * profile: st:context [ st:uri <uri> ]
     * st:dataset = the triple store dataset, hence we can have a result graph and the dataset graph
     * The dataset give access e.g to the ontology
     * Use case: 
     * bind (st:get(st:dataset) as ?g) graph ?g { }
     * bind (st:get(st:context) as ?g) graph ?g { }.
     */
    String process(Mappings map, Graph g, String trans){ 
        Transformer t = Transformer.create(g, trans);
        context.setTransform(trans);        
        complete(context, graph);
        t.setContext(context);
        
//        if (map != null && map.getQuery() != null){
//            // Transformer inherit Query Transformer Visitor if any
//            Transformer tr = (Transformer) map.getQuery().getTransformer();
//            if (tr != null && tr.getVisitor() != null){
//                t.setVisitor(tr.getVisitor());
//            }
//        }
        return t.toString();
    }
    
        // triple store graph has a st:context graph
        // add it to the transformer context
    void complete(Context context, Graph graph) {
        Graph cg = graph.getNamedGraph(Context.STL_CONTEXT);
        if (cg != null) {
            context.set(Context.STL_CONTEXT, DatatypeMap.createObject(Context.STL_CONTEXT, cg));
        }
        context.set(Context.STL_DATASET, DatatypeMap.createObject(Context.STL_DATASET, graph));
    }
    
    String expand(String str){
        return nsm.toNamespace(str);
    }
      
    Graph select(){
        //MappingsProcess mp = MappingsProcess.create(map);
        MappingsGraph mg = MappingsGraph.create(map);
        return mg.getGraph(); 
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
