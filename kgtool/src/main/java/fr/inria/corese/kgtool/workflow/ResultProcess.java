/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.corese.kgtool.workflow;

import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgtool.print.ResultFormat;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ResultProcess extends AbstractProcess {
    int format = ResultFormat.UNDEF_FORMAT;
    
    public ResultProcess() {
    }

    public ResultProcess(int type) {
        format = type;
    }

    @Override
    public Data process(Data data) throws EngineException {       
        Data res = new Data(this, data.getMappings(), data.getGraph());
        setData(res);
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
