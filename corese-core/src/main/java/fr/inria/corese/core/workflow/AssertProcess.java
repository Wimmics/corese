/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class AssertProcess extends SemanticProcess {
    
    WorkflowProcess test;
    IDatatype value;
    
    AssertProcess(WorkflowProcess w, IDatatype dt){
        insert(w);
        test = w;
        value = dt;
    }
    
    @Override
    public Data run(Data data) throws EngineException{
        Data val = test.compute(data);
        IDatatype dt = val.getDatatypeValue();
        Data res = new Data(this, data.getMappings(), data.getGraph());
        res.setDatatypeValue(dt);
        if (val.getMappings() != null){
            res.setSuccess(val.getMappings().size() > 0);
            res.addData(val);
            if (isDebug()){
                System.out.println(val.getMappings());
            }
        }
        else if (dt == null || ! dt.equals(value)){
            res.setSuccess(false);
        }
        return res;
    }
    
    @Override
    void start(Data data){
          
     }
    
    @Override
    void finish(Data data){
       if (isVerbose()){
           message(data);
       }
    }
       
    void message(Data data){
        if (data.isSuccess()){
            System.out.println(getName() + " ok");
        }
        else {
           if (data.getDataList() != null){
               System.out.println(getName() + " fail");
           }
           else {
                System.out.println(getName() + " fail: find " + data.getDatatypeValue() + " instead of: " + value);
           }
       }
    }
    
    @Override
    public String  stringValue(Data data){
        return Boolean.valueOf(data.isSuccess()).toString();        
    }

}
