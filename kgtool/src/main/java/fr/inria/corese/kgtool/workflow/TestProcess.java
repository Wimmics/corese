package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;

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
        add(pif);
        if (pthen != null){
            add(pthen);
        }
        if (pelse != null) {
            add(pelse);
        }
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
