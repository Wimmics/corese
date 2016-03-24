package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.Dataset;
import fr.inria.edelweiss.kgram.core.Mappings;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class TestProcess extends SemanticProcess {

    WorkflowProcess pif, pthen, pelse;
    ArrayList<WorkflowProcess> list;

    TestProcess(WorkflowProcess pif, WorkflowProcess pthen, WorkflowProcess pelse) {
        this.pif = pif;
        this.pthen = pthen;
        this.pelse = pelse;
        list = new ArrayList<WorkflowProcess>();
        list.add(pif);
        if (pthen != null){
            list.add(pthen);
        }
        if (pelse != null) {
            list.add(pelse);
        }
    }

    @Override
    public Data process(Data data) throws EngineException {
        boolean test = test(data);
        
        if (test) {
            if (pthen != null) {
                return pthen.process(data);
            }
        } 
        else if (pelse != null) {
            return pelse.process(data);
        }

        return data;
    }
    
    boolean test(Data data) throws EngineException{
         Data test = pif.process(data);
         IDatatype dt = test.getDatatype();
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

    @Override
    public void subscribe(SemanticWorkflow w) {
        super.subscribe(w);
        for (WorkflowProcess p : list) {
            p.subscribe(w);
        }
    }

    @Override
    public void inherit(Context c) {
        super.inherit(c);
        for (WorkflowProcess p : list) {
            p.inherit(c);
        }
    }

    @Override
    public void inherit(Dataset ds) {
        super.inherit(ds);
        for (WorkflowProcess p : list) {
            p.inherit(ds);
        }
    }
}
