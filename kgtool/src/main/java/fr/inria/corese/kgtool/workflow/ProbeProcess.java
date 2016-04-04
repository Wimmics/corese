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
    public Data run(Data data) throws EngineException {       
        for (WorkflowProcess wp : getProcessList()){
            Data res = wp.compute(data);
        }
        return data;
    }
    
    
    
}
