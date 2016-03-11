package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public interface Process {
        
    Data process(Data d) throws EngineException ;
   
    void subscribe(WorkflowProcess w);
    
    String stringValue(Data data);
        
    void setContext(Context c);
    
    void setDataset(Dataset ds);

    void setDebug(boolean b);
    
    boolean isDebug();

}
