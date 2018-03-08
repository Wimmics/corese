package fr.inria.corese.sparql.exceptions;

/**
 * This exception is used when errors are detected by the lexical analyser
 * <br>
 * @author Virginie Bottollier
 */
public class QueryLexicalException extends EngineException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	
	public QueryLexicalException() {
		super();
	}
	
	public QueryLexicalException(String mes) {
		super(mes);
	}
}
