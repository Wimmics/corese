package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.core.transform.DefaultVisitor;

/**
 * Execute a sub Process but return the input Data
 * Used for e.g trace purpose
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ProbeProcess extends SemanticProcess {
    
    ProbeProcess(){}
         
    ProbeProcess(WorkflowProcess wp){
        insert(wp);
    }
    
    @Override
    void start(Data data){
//        if (getModeString() != null && getModeString().equals(WorkflowParser.VISITOR)){
//            data.setVisitor(new DefaultVisitor());
//        }
    }
    
    @Override
    void finish(Data data){        
    }
    
    @Override
    public Data run(Data data) throws EngineException {   
        logger.info("Probe workflow");
        for (WorkflowProcess wp : getProcessList()){
            System.out.println(wp);
            Data res = wp.compute(data);
        }
        return data;
    }
    
    
    
}
