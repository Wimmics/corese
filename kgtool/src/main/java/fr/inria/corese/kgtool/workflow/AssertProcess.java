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
    public Data process(Data data) throws EngineException{
        Data val = test.process(data);
        IDatatype dt = val.getDatatype();
        IDatatype dtres = DatatypeMap.TRUE;
        if (dt == null || ! dt.equals(value)){
            dtres = DatatypeMap.FALSE;
            System.out.println("Error: find " + dt + " instead of: " + value);
        }
        Data res = new Data(this, data.getMappings(), data.getGraph());
        res.setDatatype(dtres);
        return res;
    }
    
    @Override
    public String  stringValue(Data data){
        return data.getDatatype().toString();
    }

}
