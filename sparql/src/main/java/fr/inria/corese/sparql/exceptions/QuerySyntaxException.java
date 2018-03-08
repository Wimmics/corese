package fr.inria.corese.sparql.exceptions;

/**
 * This exception is thrown when there is an error in the syntax of the query.
 * <br>
 * @author Virginie Bottollier
 */
public class QuerySyntaxException extends EngineException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	
	public QuerySyntaxException() {
		super();
	}
	
	public QuerySyntaxException(Exception e) {
		super(e);
	}
	
	public QuerySyntaxException(String mes) {
		super(mes);
	}
	

}
