/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.exceptions.EngineException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class AssertProcess extends SemanticProcess {
    
    WorkflowProcess test;
    IDatatype value;
    
    AssertProcess(WorkflowProcess w, IDatatype dt){
        add(w);
        test = w;
        value = dt;
    }
    
    @Override
    public Data run(Data data) throws EngineException{
        Data val = test.compute(data);
        IDatatype dt = val.getDatatypeValue();
        Data res = new Data(this, data.getMappings(), data.getGraph());
        res.setDatatypeValue(dt);
        if (dt == null || ! dt.equals(value)){
            res.setSuccess(false);
        }
        return res;
    }
    
    @Override
    void finish(Data data){
       if (! data.isSuccess()){
           System.out.println("Error: find " + data.getDatatypeValue() + " instead of: " + value);
       }
    }
    
    @Override
    public String  stringValue(Data data){
        return data.getDatatypeValue().toString();
    }

}
