package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import java.util.ArrayList;
import java.util.List;


/**
 * Super class of Atomic Process
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SemanticProcess extends WorkflowProcess {
    
    List<WorkflowProcess> processList;
    
    SemanticProcess(){
        processList = new ArrayList<WorkflowProcess>();
    }
    
    SemanticProcess(List<WorkflowProcess> l){
        processList = l;
    }
    
    void add(WorkflowProcess p){
        processList.add(p);
    }
    
     @Override
    public void subscribe(SemanticWorkflow w) {
        super.subscribe(w);
        for (WorkflowProcess p : getProcessList()) {
            p.subscribe(w);
        }
    }

    @Override
    public void inherit(Context c) {
        super.inherit(c);
        for (WorkflowProcess p : getProcessList()) {
            p.inherit(c);
        }
    }

    @Override
    public void inherit(Dataset ds) {
        super.inherit(ds);
        for (WorkflowProcess p : getProcessList()) {
            p.inherit(ds);
        }
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
    

}
