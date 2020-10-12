package fr.inria.corese.sparql.exceptions;

/**
 *
 */
public class StopException extends EngineException {
    
    public StopException() {
        setStop(true);
    }
    
    
}
