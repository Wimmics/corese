package fr.inria.corese.sparql.exceptions;

/**
 *
 * @author corby
 */
public class SafetyException extends EngineException {
    
    public SafetyException() {
    }

    public SafetyException(EngineException e) {
        super(e);
    }
    
    public SafetyException(String mes) {
        super(mes);
    }
    
    public SafetyException(String mes, String path) {
        super(mes + ": " + path);
        setPath(path);
    }
    
    @Override
    public boolean isSafetyException() {
        return true;
    }

    @Override
    public SafetyException getSafetyException() {
        return this;
    }

}
