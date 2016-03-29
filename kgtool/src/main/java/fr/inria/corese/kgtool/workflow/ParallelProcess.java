package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ParallelProcess extends SemanticProcess {
        
     ParallelProcess(List<WorkflowProcess> l){
        super(l);
    }
     
     ParallelProcess(){
        super();
     }
    
     
    @Override
    public Data process(Data data) throws EngineException{
        ArrayList<Data> list = new ArrayList<Data>();
        for (WorkflowProcess wp : getProcessList()){
            Data res = wp.process(data);
            res.setName(wp.getName());
            list.add(res);
        }
        return new Data(this, list);
    }
    
   

}
