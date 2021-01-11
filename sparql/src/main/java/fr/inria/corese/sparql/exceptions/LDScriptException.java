package fr.inria.corese.sparql.exceptions;

import fr.inria.corese.sparql.api.IDatatype;

/**
 *
 * @author corby
 */
public class LDScriptException extends EngineException {
    
    IDatatype dt;
    
    public LDScriptException(IDatatype dt) {
        this.dt = dt;
    }
    
    public LDScriptException(IDatatype dt, boolean b) {
        this.dt = dt;
        setStop(b);
    }
    
    @Override
    public IDatatype getDatatypeValue() {
        return dt;
    }
    
    
}
