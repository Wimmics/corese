package fr.inria.corese.sparql.exceptions;

import fr.inria.corese.kgram.core.SparqlException;

/**
 * This class gathers all the exceptions that can be thrown in Corese
 * @author Virginie Bottollier
 */
public class EngineException extends SparqlException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	protected EngineException() {
		super();
	}
	
	public EngineException(Exception e) {
		super(e);
	}

	public EngineException(String message) {
		super(message);
	}

//	protected EngineException(Error e) {
//		super(e);
//	}
        
        public static EngineException cast(SparqlException e) {
            if (e instanceof EngineException) {
                return (EngineException) e;
            }
            return new EngineException(e);
        }
	
}
