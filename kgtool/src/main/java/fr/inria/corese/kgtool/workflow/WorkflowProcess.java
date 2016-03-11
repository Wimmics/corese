package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgraph.core.Graph;
import java.util.ArrayList;

/**
 * Workflow of Query | Update | RuleBase | Transformation
 * Each Process pass a Data with Graph to next Process
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class WorkflowProcess extends  AbstractProcess {
    
    ArrayList<Process> list;
    Data data;

    
    public WorkflowProcess(){
        list = new ArrayList<Process>();
    }
    
    public WorkflowProcess add(Process p){
        list.add(p);
        p.subscribe(this);
        return this;
    }    
    
    public WorkflowProcess addQuery(String q){
       return add(new SPARQLProcess(q));
    }
    
    // return input graph
    public WorkflowProcess addProbeQuery(String q){
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
      
    public Data process() throws EngineException {
        return process(new Data(Graph.create()));
    }
    
    @Override
    public Data process(Data data) throws EngineException {  
        setData(data);
        if (isDebug() && getContext() != null){
            System.out.println(getContext());
        }
        for (Process p : list){
            complete(p);
            data = p.process(data);           
        }        
        return data;
    }
    
    // Process inherit workflow Context and Dataset (if any)
    void complete(Process p) {
        if (getContext() != null) {
            p.setContext(getContext());
        }
        if (getDataset() != null) {
            p.setDataset(getDataset());
        }
    }
    
    @Override
    public String stringValue(Data data){
        if (data.getProcess() == this){
            return null;
        }
        return data.stringValue();
    }
       
    Process last(){
        if (list.isEmpty()){
            return null;
        }
        return list.get(list.size() -1);
    }      
 
   
}
