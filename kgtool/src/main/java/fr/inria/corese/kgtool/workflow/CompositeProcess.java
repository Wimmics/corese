package fr.inria.corese.kgtool.workflow;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class CompositeProcess extends WorkflowProcess {

    List<WorkflowProcess> processList;
    
    CompositeProcess(){
        processList = new ArrayList<WorkflowProcess>();
    }
    
    CompositeProcess(List<WorkflowProcess> l){
        processList = l;
    }
    
    public void insert(WorkflowProcess p){
        processList.add(p);
    }
    
   
     public WorkflowProcess getProcessLast(){
         if (processList.isEmpty()){
             return null;
         }
        return processList.get(processList.size() - 1);
    }
    
    /**
     * @return the processList
     */
    @Override
    public List<WorkflowProcess> getProcessList() {
        return processList;
    }

    /**
     * @param processList the processList to set
     */
    public void setProcessList(List<WorkflowProcess> processList) {
        this.processList = processList;
    }
    
     @Override
    public void init(boolean b) {
        if (isVisitable(b)) {
            initialize();
            for (WorkflowProcess p : processList) {
                p.init(b);
            }
        }
    }
     
    @Override
    public void setRecDisplay(boolean b){
        super.setDisplay(b);
        for (WorkflowProcess p : getProcessList()){
            p.setDisplay(b);
        }
    } 
    
}
