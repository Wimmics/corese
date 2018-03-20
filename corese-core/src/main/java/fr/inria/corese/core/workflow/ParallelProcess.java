package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.exceptions.EngineException;
import java.util.ArrayList;
import java.util.List;

/**
 * Run Processes as in parallel with copy of input Graph
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ParallelProcess extends SemanticProcess {
        
     ParallelProcess(List<WorkflowProcess> l){
        super(l);
    }
     
     public ParallelProcess(){
        super();
     }
    
     @Override
     void start(Data data){
         
     }
     
     @Override
     void finish(Data data){
         
     }
     
    @Override
    public Data run(Data data) throws EngineException{
        ArrayList<Data> list = new ArrayList<Data>();
        for (WorkflowProcess wp : getProcessList()){   
            Data res = wp.compute(data.copy(wp.isModify()));
            res.setName(wp.getName());
            list.add(res);
        }
        Data output = new Data(this, list);
        output.setGraph(data.getGraph());
        return output;
    }
    
    

}
