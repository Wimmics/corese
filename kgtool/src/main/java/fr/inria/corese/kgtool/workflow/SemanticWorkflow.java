package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgraph.core.GraphStore;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;

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
    private String path;
    
    public SemanticWorkflow(){
        super();
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
    
    public SemanticWorkflow addQuery(String q){
       return add(new SPARQLProcess(q));
    }
    
    public SemanticWorkflow addQuery(String q, String path){
       return add(new SPARQLProcess(q, path));
    }
    
     public SemanticWorkflow addQueryPath(String path) throws LoadException{
        String q = QueryLoad.create().readWE(path);
        return addQuery(q, path);
    }
    
    // return input graph
    public SemanticWorkflow addQueryProbe(String q){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setProbe(true);
       return add(sp);
    }
    
    // select return Graph Mappings
    public SemanticWorkflow addQueryGraph(String q){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setResult(GRAPH);
       return add(sp);
    }
    
    public SemanticWorkflow addTemplate(String q){
       return add(new TemplateProcess(q));
    }
    
    public SemanticWorkflow addTemplate(String q, boolean isDefault){
       return add(new TemplateProcess(q, isDefault));
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
        init(isVisit());
        return compute(data);
    }
    
    @Override
    void start(Data data){
       initLoop();
    }
    
    @Override
    void finish(Data data){
        
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

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
