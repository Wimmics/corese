package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;

/**
 * Execute a sub Process but return the input Data
 * Used for e.g trace purpose
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ProbeProcess extends SemanticProcess {
         
    ProbeProcess(WorkflowProcess wp){
        add(wp);
    }
    
    @Override
    public Data process(Data data) throws EngineException {
        if (isDebug()){
            System.out.println(getClass().getName());
        }
        for (WorkflowProcess wp : getProcessList()){
            Data res = wp.process(data);
        }
        return data;
    }
    
    
    
}
