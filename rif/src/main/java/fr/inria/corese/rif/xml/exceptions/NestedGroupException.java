package fr.inria.corese.rif.xml.exceptions;

import fr.inria.corese.rif.xml.schema.Group;

public class NestedGroupException extends java.lang.Exception {
	
	private static final long serialVersionUID = 1L;
	
	private Group nestedGroup ;
	
	public NestedGroupException(Group g) {
		super() ;
		this.nestedGroup = g ;
	}
	
	public Group getNestedGroup() {
		return this.nestedGroup ;
	}
	
}
