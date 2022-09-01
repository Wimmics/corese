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
import fr.inria.corese.core.util.MappingsGraph;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SPARQLProcess extends  WorkflowProcess {

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

    private static Logger logger = LoggerFactory.getLogger(SPARQLProcess.class);
    static final String NL = System.getProperty("line.separator");
    static final NSManager nsm = NSManager.create();
    private static final String PARAM    = "$param";
    private static final String ARG      = "$arg";
    private static final String MODE     = "$mode";
    private static final String PATTERN  = "$pattern";
    private boolean userQuery = false;
    private Level level = Level.USER_DEFAULT;
    Level saveContextLevel = Level.USER_DEFAULT;
    Level saveBindingLevel;
    private IDatatype param;    
    private IDatatype value;
    private IDatatype option;
    private IDatatype process;
    
    private String query;
    
    public SPARQLProcess(String q){
        this(q, null);
    }
    
    public SPARQLProcess(String q, String path){
        this.query = q;
        this.path = path;
    }
    
    @Override
    public boolean isEmpty() {
        return query == null;
    }
    
    @Override
    void start(Data data){
        if (isRecDebug() || isRecDisplay()){
            System.out.println(getWorkflow().getPath());
            System.out.println("Query: " + getQuery());
        }
        data.getEventManager().start(Event.WorkflowQuery);
        if (getContext() != null) {
            getContext().setUserQuery(isUserQuery());
            saveContextLevel = getContext().getLevel();
            getContext().setLevel(getLevel());
        } 
        saveBindingLevel = null;
        if (data.getBinding() == null) {
            saveBindingLevel = saveContextLevel;
        }
        else {
            saveBindingLevel = data.getBinding().getAccessLevel();        
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
        data.getEventManager().finish(Event.WorkflowQuery);
        if (getContext() != null) {
            getContext().setUserQuery(false);
            getContext().setLevel(saveContextLevel);
            if (data.getBinding()!=null) {
                data.getBinding().setAccessLevel(level);
            }
        }
        if (data.getBinding() != null && saveBindingLevel != null) {
            data.getBinding().setAccessLevel(saveBindingLevel);
        }
    }
    
    
    @Override
    public Data run(Data data) throws EngineException { 
        Data res = query(data, getContext(), getDataset()); 
        complete(res);
        return res;
    }
    
    Data query(Data data, Context c, Dataset ds) throws EngineException{
        Graph g = data.getGraph();
        if (g == null){
            g = GraphStore.create();
        }
        QueryProcess exec = QueryProcess.create(g, data.getDataManager());
//        if (getWorkflow().getGraph() != null){
//            // draft: additional graph considered as contextual dataset
//            exec.add(getWorkflow().getGraph());
//        }
        if (getPath() != null){
            exec.setDefaultBase(getPath());
        }
        log1(g, c); 
        if (hasVisitor() && recVisitor().isTest()) {
            return test(exec, data, c, ds);
        }
        else {
            Date d1 = new Date();
            Mappings map = exec.query(tuneQuery(exec, data), data.dataset(c, ds)); 
            Data res = new Data(this, map, getGraph(map, data));
            res.setBinding( map.getBinding());
            log2(res, d1, new Date());  
            return res;
        }
    }
    
    Data test(QueryProcess exec, Data data, Context c, Dataset ds) throws EngineException {
        Mappings map = null;
        Date d1 = new Date();
        int nb = 5;
        for (int i = 0; i < nb; i++) {
            map = exec.query(getQuery(), data.dataset(c, ds));
        }
        Date d2 = new Date();
        Data res = new Data(this, map, getGraph(map, data));
        res.setBinding( map.getBinding());
        if (hasVisitor()) {
            recVisitor().visit(this, res, ((d2.getTime()-d1.getTime())/(1000.0 * nb)));
        }
        return res;
    }
    
    String tuneQuery(QueryProcess exec, Data data) {
        if (getQuery() == null) {
            return "select * where {?s ?p ?o} limit 10";
        }
        return tune(exec, getQuery());
    }
    
    /**
     * Replace $pattern $mode $param by pattern() st:get(st:mode) st:get(st:param)
     */
    String tune(QueryProcess exec, String query) {
        query = pattern(exec, query);
        if (getContext().hasValue(Context.STL_PARAM) && query.contains(PARAM)) {
            query = query.replace(PARAM, getContext().get(Context.STL_PARAM).stringValue()) ;
        }
        if (getContext().hasValue(Context.STL_ARG) && query.contains(ARG)) {
            query = query.replace(ARG, getContext().get(Context.STL_ARG).stringValue()) ;
        }
        if (getContext().hasValue(Context.STL_MODE) && query.contains(MODE)) {
            query = query.replace(MODE, String.format("<%s>", getContext().get(Context.STL_MODE).stringValue())) ;
        }
        return query;
    }
    
    /**
     * May call a function that rewrites the query
     * Replace $pattern by specific code
     */
    String pattern(QueryProcess exec, String query) {
        IDatatype name  = getValue(getProcess(), Context.STL_PROCESS_QUERY);
        if (name == null) {
            return query;
        }
        try {
            IDatatype res = exec.funcall(name.stringValue(), getContext(), DatatypeMap.newInstance(query));
            if (res != null) {
                return res.stringValue();
            }
        } catch (EngineException ex) {
            logger.error(ex.toString());
        }
        return query;
    }
    
    
    /**
     * replace $pattern by code that is computed
     * name : name of param, e.g. st:mode
     * param: value of param : e.g. db:Algérie
     * value : list of values for param that match for option[0]
     * option[0] = code when param match value 
     * option[1] = code when param does not match value
     * return replace($pattern, if (param.match(value), option[0], option[1])
     */
    @Deprecated
    String pattern(String query) {               
        IDatatype name   = getValue(getParam(), Context.STL_PATTERN_PARAM);
        IDatatype value  = getValue(getValue(), Context.STL_PATTERN_VALUE);
        IDatatype option = getValue(getOption(),Context.STL_PATTERN_OPTION);
        IDatatype param   = (name == null) ? null : getContext().get(name);
        IDatatype pattern = getContext().get(Context.STL_PATTERN);
        String pat = (pattern == null) ? PATTERN : pattern.stringValue();
        if (option == null || value == null || param == null) {
            return query;
        }
        IDatatype target ;
        if (value.getValues().contains(param)) {
            target = option.getList().get(0);
        }
        else {
            target = option.getList().get(1);
        }
        query = query.replace(pat, target.stringValue());
        return query;
    }
    
    /**
     * st:uri may be the uri of the query in the workflow (cf tutorial)
     */
    IDatatype getValue(IDatatype value, String name) {
        if (value == null) {
            value = getContext().get(name);
            if (value == null) {  
                value = getContextParamValue(getContext().get(Context.STL_URI), name);
            }                                    
        }
        return value;
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
    
    void log2(Data data, Date d1, Date d2){
        if (hasVisitor()) {
            recVisitor().visit(this, data, (d2.getTime() - d1.getTime()) / 1000.0);
        }
        if (isLog() || pgetWorkflow().isLog()){
            logger.info("Time: " + (d2.getTime() - d1.getTime()) / 1000.0);
        }
    }
    
    void complete(Data data) {
//        TemplateVisitor vis = (TemplateVisitor) data.getMappings().getQuery().getTemplateVisitor();
//        if (vis != null){
//            data.setVisitor(vis);
//        }
        Node temp = data.getMappings().getTemplateResult();
        if (temp != null){
            data.setDatatypeValue( temp.getValue());
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
    
     /**
     * @return the param
     */
    public IDatatype getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(IDatatype param) {
        this.param = param;
    }
    
    /**
     * @return the value
     */
    public IDatatype getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(IDatatype value) {
        this.value = value;
    }
    
    /**
     * @return the option
     */
    public IDatatype getOption() {
        return option;
    }

    /**
     * @param option the option to set
     */
    public void setOption(IDatatype option) {
        this.option = option;
    }
  
    /**
     * @return the process
     */
    public IDatatype getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(IDatatype process) {
        this.process = process;
    }
    
        /**
     * @return the userQuery
     */
    public boolean isUserQuery() {
        return userQuery;
    }

    /**
     * @param userQuery the userQuery to set
     */
    public void setUserQuery(boolean userQuery) {
        this.userQuery = userQuery;
    }
   
  
        
}
