package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.util.MappingsGraph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SPARQLProcess extends  AbstractProcess {

    private String query;
    // true means return input graph (use case: select where and return graph as is)
    private boolean probe = false;
    
    public SPARQLProcess(String q){
        this.query = q;
    }
    
    @Override
    public Data process(Data data) throws EngineException {  
        if (isDebug()){
            System.out.println("Query: " + getQuery());
        }
        Mappings map = query(data, getContext(), getDataset());
        if (isDebug() && map.getGraph() != null){
            System.out.println(map.getGraph());
        }
        Data res = new Data(this, map, getGraph(map, data));
        complete(res);
        setData(res);
        return res;
    }
    
    Mappings query(Data data, Context c, Dataset ds) throws EngineException{
        QueryProcess exec = QueryProcess.create(data.getGraph());
        if (ds != null){
            if (c != null){
                ds.setContext(c);
            }
            return exec.query(getQuery(), ds);   
        }
        return exec.query(getQuery(), getContext());   
    }
    
    void complete(Data data) {
        Transformer t = (Transformer) data.getMappings().getQuery().getTransformer();
        if (t != null && t.getVisitor() != null) {
            data.setVisitor(t.getVisitor());
        }
    }
    
           
    Graph getGraph(Mappings map, Data data) {
        ASTQuery ast = (ASTQuery) map.getAST();
        if (isProbe() || ast.hasMetadata(Metadata.TYPE, Metadata.PROBE)){
            // @type kg:probe : return input graph
            return data.getGraph();
        }
        else if (ast.isSPARQLUpdate()) {
            return data.getGraph();
        } else if (map.getGraph() != null) {
            // construct
            return (Graph) map.getGraph();
        } else {
            // select : return bindings as RDF graph
            MappingsGraph m = MappingsGraph.create(map);
            return m.getGraph();
        }
    }

    @Override
    public String stringValue(Data data) {
        Mappings m = data.getMappings();
        if (m.getQuery().isTemplate()){
            return m.getTemplateStringResult();
        }
        ResultFormat f = ResultFormat.create(m);
        return f.toString();
    }
    
    public boolean isProbe() {
        return probe;
    }
   
    public void setProbe(boolean neutral) {
        this.probe = neutral;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
        
}
