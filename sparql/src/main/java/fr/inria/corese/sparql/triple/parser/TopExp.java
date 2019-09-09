package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ASTVisitable;
import fr.inria.corese.sparql.triple.api.ASTVisitor;

/**
 * Root of SPARQL Expression (filter) and Exp (triple, option, ...) 
 * @author corby
 *
 */

public class TopExp implements ASTVisitable  {
    
    public enum VariableSort { ALL,
    INSCOPE, 
    // Variable surely bound: left part of optional, common var in union branchs
    // former getVariables()
    SUBSCOPE } ;

    /**
     * @return the generated
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * @param generated the generated to set
     */
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }
    private boolean generated = false;
	
        @Override
	public String toString(){
		ASTBuffer sb = new ASTBuffer();
		toString(sb);
		return sb.toString();
	}
	
	public StringBuffer toString(StringBuffer sb){
		return sb;
	}
        
        public ASTBuffer toString(ASTBuffer sb){
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
