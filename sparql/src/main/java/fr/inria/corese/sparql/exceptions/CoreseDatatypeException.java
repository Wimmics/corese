package fr.inria.corese.sparql.exceptions;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This exception is thrown when an error occurs during creation of a datatype value (in a Marker).<br>
 * Example: when we try to create a CoreseInteger with a string, which is not an integer.<br>
 * <br>
 * @author Olivier Savoie
 */

public class CoreseDatatypeException extends CoreseException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	private static String message = "Datatype error: ";

	public CoreseDatatypeException() {
		super();
	}

	public CoreseDatatypeException(Throwable cause, String jDatatype, String label) {
		super(message + jDatatype + " " + label);
	}

	public CoreseDatatypeException(String jDatatype, String label) {
		super(message + jDatatype + " " + label);
	}

	public CoreseDatatypeException(String m) {
		super(m);
	}
	


}