package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgraph.query.QueryProcess;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.transform.Transformer;
import fr.inria.edelweiss.kgtool.util.MappingsGraph;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SPARQLProcess extends  WorkflowProcess {

    private String query;
    
    public SPARQLProcess(String q){
        this(q, null);
    }
    
    public SPARQLProcess(String q, String path){
        this.query = q;
        this.path = path;
    }
    
    @Override
    void start(Data data){
        if (isRecDebug() || isRecDisplay()){
            System.out.println(getWorkflow().getPath());
            System.out.println("Query: " + getQuery());
        }
    }
    
     @Override
    void finish(Data data) {
        collect(data);
        if (isRecDebug() || isRecDisplay()) {
            if (isGraphResult()) {
                System.out.println(data.getGraph());
            } else {
                System.out.println(data.getMappings());
            }
        }
    }
    
    
    @Override
    public Data run(Data data) throws EngineException { 
        Mappings map = query(data, getContext(), getDataset());        
        Data res = new Data(this, map, getGraph(map, data));
        complete(res);
        return res;
    }
    
    Mappings query(Data data, Context c, Dataset ds) throws EngineException{
        Graph g = data.getGraph();
        if (g == null){
            g = GraphStore.create();
        }
        QueryProcess exec = QueryProcess.create(g);
        if (getWorkflow().getGraph() != null){
            // draft: additional graph considered as contextual dataset
            exec.add(getWorkflow().getGraph());
        }
        if (path != null){
            exec.setDefaultBase(path);
        }
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
        Node temp = data.getMappings().getTemplateResult();
        if (temp != null){
            data.setDatatypeValue((IDatatype) temp.getValue());
            data.setTemplateResult(data.getDatatypeValue().stringValue());
        }
    }
    
    /**
     * Should select where return a graph of bindings ?
     */
    boolean isGraphResult(){
        String res = theResult();
        if (res == null){
            return false;
        }
        return res.equals(GRAPH);
    }
    
    boolean isAProbe() {
        if (isProbe() || getWorkflow().isProbe()) {
            return true;
        }
        String res = theResult();
        if (res == null) {
            return false;
        }
        return res.equals(PROBE);
    }
    
    String theResult(){
        return (getResult() == null) ? getWorkflow().getResult() : getResult();
    }
     
    
    Graph getGraph(Mappings map, Data data) {
        ASTQuery ast = (ASTQuery) map.getAST();
        if (isAProbe() || ast.hasMetadata(Metadata.TYPE, Metadata.PROBE)){
            // probe is a query that does not impact the workflow (except Update)
            // @type kg:probe : return input graph as is
            return data.getGraph();
        }
        else if (ast.isSPARQLUpdate()) {
            return data.getGraph();
        } else if (map.getGraph() != null) {
            // construct
            return (Graph) map.getGraph();
        } 
        else if (isGraphResult()){
            // select : return Mappings as RDF graph
            // -- default server mode for query =
            MappingsGraph m = MappingsGraph.create(map);
            return m.getGraph();
        }
        else {
            // select : return input graph (and Mappings)
            return data.getGraph();
        }
    }

    @Override
    public String stringValue(Data data) {
        Mappings m = data.getMappings();
        if (m.getQuery().isTemplate()){
            return m.getTemplateStringResult();
        }
        ResultFormat f = ResultFormat.format(m);
        return f.toString();
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
