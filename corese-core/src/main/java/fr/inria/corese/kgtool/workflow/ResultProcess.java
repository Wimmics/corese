/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.corese.kgtool.print.ResultFormat;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ResultProcess extends WorkflowProcess {
    int format = ResultFormat.UNDEF_FORMAT;
    
    public ResultProcess() {
    }

    public ResultProcess(int type) {
        format = type;
    }

    @Override
    public Data run(Data data) throws EngineException {       
        Data res = new Data(this, data.getMappings(), data.getGraph());
        collect(res);
        return res;
    }
    
     @Override
    public String stringValue(Data data) {       
        Mappings m = data.getMappings();  
        if (m == null){
            ResultFormat f = ResultFormat.create(data.getGraph(), format);
            return f.toString();
        }
        if (m.getQuery().isTemplate()){
            return m.getTemplateStringResult();
        }
        ResultFormat f = ResultFormat.create(m, format);
        return f.toString();
    }
    

}
