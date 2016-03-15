package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.load.QueryLoad;
import java.util.ArrayList;
import java.util.List;

/**
 * Workflow of Query | Update | RuleBase | Transformation
 * Each Process pass a Data with Graph to next Process
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowProcess extends  AbstractProcess {
    private static final String NL = System.getProperty("line.separator");
   
    
    ArrayList<Processor> list;
    Data data;
    private int loop = -1;

    
    public WorkflowProcess(){
        list = new ArrayList<Processor>();
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(":");
        int i = 1;
        for (Processor p : getProcessList()){
            sb.append(NL).append(i++).append(": ").append(p.toString());
        }
        return sb.toString();
    }
    
    public WorkflowProcess add(Processor p){
        list.add(p);
        p.subscribe(this);
        return this;
    }    
    
    public WorkflowProcess addQuery(String q){
       return add(new SPARQLProcess(q));
    }
    
     public WorkflowProcess addQueryPath(String path) throws LoadException{
        String q = QueryLoad.create().readWE(path);
        return addQuery(q);
    }
    
    // return input graph
    public WorkflowProcess addQueryProbe(String q){
       SPARQLProcess sp = new SPARQLProcess(q);
       sp.setProbe(true);
       return add(sp);
    }
    
    public WorkflowProcess addTemplate(String q){
       return add(new TemplateProcess(q));
    }
    
    public WorkflowProcess addTemplate(String q, boolean isDefault){
       return add(new TemplateProcess(q, isDefault));
    }
    
    public WorkflowProcess addRule(String q){
       return add(new RuleProcess(q));
    }
    
    // RuleBase.OWL_RL
    public WorkflowProcess addRule(int type){
       return add(new RuleProcess(type));
    }
    
     public WorkflowProcess addResult(int type){
       return add(new ResultProcess(type));
    }
    
    public WorkflowProcess addResult(){
       return add(new ResultProcess());
    }
    
    public List<Processor> getProcessList(){
        return list;
    }
      
    public Data process() throws EngineException {
        return process(new Data(Graph.create()));
    }
    
    @Override
    public Data process(Data data) throws EngineException { 
        init();
        if (getLoop() > 0){
            return loop(data);
        }
        else {
            return run(data);
        }
    }
           
    Data run(Data data) throws EngineException {  
        setData(data);
        trace();
        for (Processor p : list){
            complete(p);
            data = p.process(data);           
        }   
        setData(data);
        return data;
    }
    
    Data loop(Data data) throws EngineException{
        Context c = getContext();
        for (int i = 1; i <= loop; i++){   
            if (c != null){
                c.set(Context.STL_INDEX, i);
            }
            data = run(data);
        }
        return data;
    }
    
    
      /**
     * May start a loop if:
     * st:loop 5
     * &param = 10
     */
    void init(){
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
    
    
    // Process inherit workflow Context and Dataset (if any)
    void complete(Processor p) {
        if (getContext() != null) {
            p.inherit(getContext());
        }
        if (getDataset() != null) {
            p.inherit(getDataset());
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
        for (Processor p : getProcessList()){
            p.setDebug(b);
        }
    }
 
   
}
