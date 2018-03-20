package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.util.MappingsGraph;
import java.util.Date;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SPARQLProcess extends  WorkflowProcess {
    private static Logger logger = LogManager.getLogger(SPARQLProcess.class);
    static final String NL = System.getProperty("line.separator");
    static final NSManager nsm = NSManager.create();

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
        data.getEventManager().start(Event.WorkflowQuery);
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
        data.getEventManager().finish(Event.WorkflowQuery);
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
        if (getPath() != null){
            exec.setDefaultBase(getPath());
        }
        log1(g, c);    
        Date d1 = new Date();
        Mappings map = exec.query(getQuery(), data.dataset(c, ds)); 
        log2(g, d1, new Date());  
        return map;
    }
    
    void log1(Graph g, Context c){
        if (isLog() || pgetWorkflow().isLog()){
            //logger.info(NL + getQuery());
            if (c != null){ 
                String str = c.trace();
                if (str.length() > 0){
                    logger.info(NL + str + NL);
                }
            }  
        }
    }
    
    void log2(Graph g, Date d1, Date d2){
        if (isLog() || pgetWorkflow().isLog()){
            logger.info("Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
        }
    }
    
    void complete(Data data) {
        TemplateVisitor vis = (TemplateVisitor) data.getMappings().getQuery().getTemplateVisitor();
        if (vis != null){
            data.setVisitor(vis);
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
            return inherit((Graph) map.getGraph(), data.getGraph());
        } 
        else if (isGraphResult()){
            // select : return Mappings as RDF graph
            // -- default server mode for query =
            MappingsGraph m = MappingsGraph.create(map);
            return inherit(m.getGraph(), data.getGraph());
        }
        else {
            // select : return input graph (and Mappings)
            return data.getGraph();
        }
    }
    
    Graph inherit(Graph output, Graph input) {
       if (input.isVerbose()) {
            output.setVerbose(true); 
       }
       return output;
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
    
    @Override
    public boolean isQuery() {
        return true;
    }
    
    @Override
    public SPARQLProcess getQueryProcess(){
        return this;
    }
        
}
