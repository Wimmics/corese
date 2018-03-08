package fr.inria.corese.rif.exceptions;


public class RuleSemanticException  {
	String message;

	private static final long serialVersionUID = 1L;
	
	public RuleSemanticException() {
		super() ;
	}
	
	public RuleSemanticException(String str) {
		message = str;
	}

}
