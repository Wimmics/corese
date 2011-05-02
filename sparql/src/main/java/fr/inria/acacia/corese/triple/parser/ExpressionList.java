package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This small class is used to manage function�s parameters list in the Sparql parser.
 * <br>
 * @author Virginie Bottollier
 */

public class ExpressionList extends Vector<Expression> {
	
	boolean isDistinct = false;
	String separator;

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;

	/** Empty constructor: create a Vector<Expression> */
	public ExpressionList() {
		super();
	}
	
	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
	public boolean isDistinct(){
		return isDistinct;
	}
	
	public void setSeparator(String s){
		separator = s;
	}
	
	public String getSeparator(){
		return separator;
	}

}
