package fr.inria.corese.sparql.exceptions;

/**
 * This exception is used for semantic errors: (one exception is raised for
 * all the semantic errors that may occur)<br />
 * <ul>
 * 	<li>Undefined prefix</li>
 * 	<li>Undefined property: in case of a triple of kind "A c:prop B", we check if the property "c:prop" exists</li>
 * 	<li>Undefined class: in case of a triple of kind "A rdf:type B", we check if B is a defined class</li>
 * </ul>
 * <br>
 * @author Virginie Bottollier
 */
public class QuerySemanticException extends EngineException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;

	//String message = "";

	public QuerySemanticException() {
		super();
	}
	
	public QuerySemanticException(String mes) {
		super(mes);
	}
	
}
