package fr.inria.acacia.corese.exceptions;

public class CompileException extends QuerySemanticException {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	String message = "";

	public CompileException() {}
	
	public CompileException(String mes) {
		super(mes);
	}
}
