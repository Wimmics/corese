package fr.inria.edelweiss.kgtool.print;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Apply HTML Transformation on Mappings :
 * construct : result graph
 * select    : serialization of Mappings into RDF
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class HTMLFormat {
    static final String constructTransform = Transformer.HTML;
    static final String selectTransform    = Transformer.RDFRESULT;
    private String transformation;
    
    Mappings map;
    Graph graph;
    private Context context;
    
    HTMLFormat(Graph g, Mappings m){
        graph = g;
        map = m;
    }
    
    public static HTMLFormat create(Graph g, Mappings m){
        return new HTMLFormat(g, m);
    }
    
    public static HTMLFormat create(Mappings m){
        return new HTMLFormat(null, m);
    }
    
    public String toString(){
        return process();
    }
    
    public void setContext(Context c){
        context = c;
    }
    
    String process(){
        if (map.getQuery().isTemplate()){
            if (map.getTemplateResult() != null){
                return map.getTemplateStringResult();
            }
            else {
                return "";
            }
        }
        else if (map.getGraph()!=null){
            Graph g = (Graph) map.getGraph();
            return process(g, getTransformation(constructTransform));       
        }
        else if (map.getQuery().isUpdate() && graph != null){
            return process(graph, getTransformation(constructTransform));       
        }
        else {
            Graph g = select();
            return process(g, getTransformation(selectTransform));   
        }
    }
    
    String getTransformation(String def){
        if (transformation != null){
            return expand(transformation);
        }
        return def;
    }
    
    String process(Graph g, String trans){       
        Transformer t = Transformer.create(g, trans);
        context.set(Transformer.STL_URI, trans);
        t.setContext(context);
        return t.toString();
    }
    
    String expand(String str){
        ASTQuery ast = (ASTQuery) map.getQuery().getAST();
        return ast.getNSM().toNamespace(str);
    }
      
    
    Graph select(){
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
