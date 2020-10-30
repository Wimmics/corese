package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.sparql.triple.parser.Access;

/**
 * Workflow of Query | Update | RuleBase | Transformation
 * Each Process pass a Data with Graph to next Process
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SemanticWorkflow extends  CompositeProcess {
    private static final String NL = System.getProperty("line.separator");
     
    Data data;
    private int loop = -1;
    private Graph workflowGraph;
    private boolean serverMode = false;
    
    public SemanticWorkflow(){
        super();
    }
    
     public SemanticWorkflow(String name){
        super();
        setName(name);
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(":");
        int i = 1;
        for (WorkflowProcess p : getProcessList()){
            sb.append(NL).append(i++).append(": ").append(p.toString());
        }
        return sb.toString();
    }
       
    public SemanticWorkflow add(WorkflowProcess p){
        insert(p);
        p.subscribe(this);
        return this;
    }  
    
    public SemanticWorkflow add(WorkflowProcess p, int n){
        insert(p, n);
        p.subscribe(this);
        return this;
    }    
    
    public SemanticWorkflow addQuery(String q){
       return add(new SPARQLProcess(q));
    }
    
    public SemanticWorkflow addQuery(String q, int n, boolean protect, Access.Level l){
        SPARQLProcess wp = getEmptyQuery();
        if (wp == null) {
            wp = new SPARQLProcess(q);
            //wp.setUserQuery(protect);
            return add(wp, n);
        }
        wp.setUserQuery(protect);
        wp.setLevel(l);
        wp.setQuery(q);
        return this;
    }
    
    SPARQLProcess getEmptyQuery() {
        for (WorkflowProcess wp : getProcessList()) {
            if (wp instanceof SPARQLProcess && wp.isEmpty()) {
                return (SPARQLProcess) wp;
            }
        }
        return null;
    }
    
    public SemanticWorkflow addQuery(String q, String path){
       return add(new SPARQLProcess(q, path));
    }
    
//     public SemanticWorkflow addQueryPath(String path) throws LoadException{
//        String q = QueryLoad.create().readWE(path, isServerMode());
//        return addQuery(q, path);
//    }
    
    // return input graph
    public SemanticWorkflow addQueryProbe(String q){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setProbe(true);
       return add(sp);
    }
    
    // select return Graph Mappings
    public SemanticWorkflow addQueryGraph(String q, boolean protect, Access.Level l){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setUserQuery(protect);
       sp.setLevel(l);
       sp.setResult(GRAPH);
       return add(sp);
    }
    
    public SemanticWorkflow addQueryMapping(String q, boolean protect, Access.Level l){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setUserQuery(protect);
       sp.setLevel(l);
       return add(sp);
    }
    
    public SemanticWorkflow addTemplate(String q){
       return add(new TransformationProcess(q));
    }
    
    public SemanticWorkflow addTemplate(String q, boolean isDefault){
       return add(new TransformationProcess(q, isDefault));
    }
    
    public SemanticWorkflow addRule(String q){
       return add(new RuleProcess(q));
    }
    
    // RuleBase.OWL_RL
    public SemanticWorkflow addRule(int type){
       return add(new RuleProcess(type));
    }
    
     public SemanticWorkflow addResult(int type){
       return add(new ResultProcess(type));
    }
    
    public SemanticWorkflow addResult(){
       return add(new ResultProcess());
    }     
     
    
    @Override
    public void initialize(){
        super.initialize();
    }
        
    /**
     * starting point of a Workflow
     */
    public Data process() throws EngineException {
        return process(new Data(GraphStore.create()));
    }
    
    /**
     * compute ::= super.before(); this.start(); this.run(); this.finish(); super.after()
     */
    public Data process(Data data) throws EngineException {
        before(data);
        Data res = doProcess(data);
        after(res);
        return res;
    }
    
    Data doProcess(Data data)throws EngineException {
        init(isVisit());
        return compute(data);
    }
    
    // local before
    void before(Data data){
        prepare(data);
        if (getContext() != null) {
            getContext().init();
        }
    }
    
    void prepare(Data data){
        setGraph(data.getGraph());
        if (getWorkflowGraph() != null){
            data.getGraph().setNamedGraph(Context.STL_SERVER_PROFILE, getWorkflowGraph());
        }
    }
    
    // local after
    void after(Data data){
    }
    
    
    @Override
    void start(Data data){
       setWorkflowVisitor(data.createVisitor());
       getWorkflowVisitor().beforeWorkflow(getCreateContext(), data);
       initLoop();
    }
    
    
    @Override
    void finish(Data data){
        getWorkflowVisitor().afterWorkflow(getContext(), data);
    }
    
    /**
     * run is the effective method that runs the workflow
     */
    
    @Override
    public Data run(Data data) throws EngineException { 
        Data res;
        if (getLoop() > 0){
            res = loop(data);
        }
        else {
            res = exec(data);
        }
        return res;
    }
           
    Data exec(Data data) throws EngineException {  
        collect(data);
        trace();
        for (WorkflowProcess p : getProcessList()){
            p.inherit(this);
            data = p.compute(data);           
        }   
        collect(data);
        return data;
    }
    
    Data loop(Data data) throws EngineException{
        Context c = getContext();
        for (int i = 1; i <= loop; i++){   
            if (c != null){
                c.set(Context.STL_INDEX, i);
            }
            data = exec(data);
        }
        return data;
    }
    
    
      /**
     * May start a loop if:
     * st:loop 5
     * &param = 10
     */
    void initLoop(){
        Context c = getContext();
        if (c == null){
            if (getLoop() > 0){
                setContext(new Context());
                initContext(getContext());
            }
        }
        else {
           initContext(getContext());
        }
    }
    
    void initContext(Context c) {
        if (c.get(Context.STL_LOOP) == null) {
            if (getLoop() > 0) {
                c.set(Context.STL_LOOP, getLoop());
            }
        } else {
            if (c.get(Context.STL_PARAM) != null) {
                // param arg overload loop number
                int val = Integer.parseInt(c.get(Context.STL_PARAM).stringValue());
                c.set(Context.STL_LOOP, val);
            }
            setLoop(c.get(Context.STL_LOOP).intValue());
        }
    } 
    
    void trace(){
         if (isDebug() && getContext() != null){
            System.out.println(getContext());
        }
    }
        
    
    @Override
    public String stringValue(Data data){
        if (data.getProcess() == this){
            return null;
        }
        return data.stringValue();
    }
       

    /**
     * @return the loop
     */
    public int getLoop() {
        return loop;
    }

    /**
     * @param loop the loop to set
     */
    public void setLoop(int loop) {
        this.loop = loop;
    }
    
    @Override
    public void setDebug(boolean b){
        super.setDebug(b);
        for (WorkflowProcess p : getProcessList()){
            p.setDebug(b);
        }
    }

    
    @Override
    public String getTransformation(){       
        WorkflowProcess wp = getProcessLast();
        if (wp == null){
            return null;
        }
        else {
            return wp.getTransformation();
        } 
    } 
    
    public boolean hasTransformation(){  
        return getTransformation() != null;
    }
    
    public boolean hasResult() {
        WorkflowProcess wp = getProcessLast();
        if (wp == null){
            return false;
        }
        else {
            return (wp instanceof ResultProcess);
        } 
    }

    /**
     * @return the workflowGraph
     */
    public Graph getWorkflowGraph() {
        return workflowGraph;
    }

    /**
     * @param workflowGraph the workflowGraph to set
     */
    public void setWorkflowGraph(Graph workflowGraph) {
        this.workflowGraph = workflowGraph;
    }

    /**
     * @return the serverMode
     */
    public boolean isServerMode() {
        return serverMode;
    }

    /**
     * @param serverMode the serverMode to set
     */
    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

}
