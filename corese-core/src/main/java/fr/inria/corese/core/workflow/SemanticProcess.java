package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import java.util.List;


/**
 * Super class of Process with a Process List (Test, Parallel, ..)
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SemanticProcess extends CompositeProcess {
    
    SemanticProcess(List<WorkflowProcess> l){
        super(l);
    }
    
    SemanticProcess (){
        super();
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

   
    

}
