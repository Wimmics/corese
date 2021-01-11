package fr.inria.corese.sparql.exceptions;

import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.api.IDatatype;

/**
 * This class gathers all the exceptions that can be thrown in Corese
 *
 * @author Virginie Bottollier
 */
public class EngineException extends SparqlException {
    
    private String path;

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;

    protected EngineException() {
        super();
    }

    public EngineException(Exception e) {
        super(e);
    }
    
    public EngineException(Error e) {
        super(e);
    }

    public EngineException(String message) {
        super(message);
    }

    public static EngineException cast(SparqlException e) {
        if (e instanceof EngineException) {
            return (EngineException) e;
        }
        return new EngineException(e);
    }

    public Exception getException() {
        if (getCause() instanceof Exception) {
            return (Exception) getCause();
        }
        return null;
    }

    public boolean isSafetyException() {
        return false;
    }

    public SafetyException getSafetyException() {
        return null;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    public IDatatype getDatatypeValue() {
        return null;
    }

}
