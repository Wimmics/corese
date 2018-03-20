package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestProcess extends SemanticProcess {

    WorkflowProcess pif, pthen, pelse;

    TestProcess(WorkflowProcess pif, WorkflowProcess pthen, WorkflowProcess pelse) {
        super();
        this.pif = pif;
        this.pthen = pthen;
        this.pelse = pelse;
        insert(pif);
        if (pthen != null){
            insert(pthen);
        }
        if (pelse != null) {
            insert(pelse);
        }
    }
    
    @Override
    void start(Data data){

    }
    
    @Override
    void finish(Data data){
        
    }

    @Override
    public Data run(Data data) throws EngineException {
        boolean test = test(data);
        if (isDebug()){
            System.out.println(pif + " : " + test);
        }
        if (test) {
            if (pthen != null) {
                return pthen.compute(data);
            }
        } 
        else if (pelse != null) {
            return pelse.compute(data);
        }

        return data;
    }
    
    boolean test(Data data) throws EngineException{
         Data test = pif.compute(data);
         IDatatype dt = test.getDatatypeValue();
         Mappings map = test.getMappings();
         if (dt != null){
             try {
                 return dt.isTrue();
             } catch (CoreseDatatypeException ex) {
                 throw new EngineException(ex);
             }
         }
         else if (map == null){
             throw new EngineException("Error:\n" + pif.toString());
         } else {
            return map.size() > 0;
         }
         
    }

}
