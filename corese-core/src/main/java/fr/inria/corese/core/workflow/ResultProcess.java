package fr.inria.corese.core.workflow;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.core.print.ResultFormat;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ResultProcess extends WorkflowProcess {
    private int format = ResultFormat.UNDEF_FORMAT;
    
    public ResultProcess() {
    }

    public ResultProcess(int type) {
        format = type;
    }
    
    public ResultProcess(String type) {
        format = ResultFormat.getFormat(type);
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
            ResultFormat f = ResultFormat.create(data.getGraph(), getFormat());
            return f.toString();
        }
        if (m.getQuery().isTemplate()){
            return m.getTemplateStringResult();
        }
        ResultFormat f = ResultFormat.create(m, getFormat());
        return f.toString();
    }
    
       /**
     * @return the format
     */
    public int getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(int format) {
        this.format = format;
    }
    

}
