package fr.inria.acacia.corese.triple.parser;

/**
 * Root of SPARQL Expression (filter) and Exp (triple, option, ...) 
 * @author corby
 *
 */

public class Statement {
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}
	
	public StringBuffer toString(StringBuffer sb){
		return sb;
	}
	
	public String toSparql(){
		return toString();
	}

}
