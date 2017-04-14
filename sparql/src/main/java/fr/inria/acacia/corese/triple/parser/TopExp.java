package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.api.ASTVisitable;
import fr.inria.acacia.corese.triple.api.ASTVisitor;

/**
 * Root of SPARQL Expression (filter) and Exp (triple, option, ...) 
 * @author corby
 *
 */

public class TopExp implements ASTVisitable  {
	
        @Override
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
        
        public StringBuffer toJava(StringBuffer sb){
		return toString(sb);
	}
        
        @Override
        public void accept(ASTVisitor visitor) {
		
	}

}
