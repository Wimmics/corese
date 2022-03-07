package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.cst.Keyword;


/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * Implement the group graph pattern
 * 
 * @author Olivier Corby 
 */

public class And extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
        // true when this is a stack of annotation triple in parser
        private boolean stack = false;
	
	public And() {
	}
	
	public And(Exp exp){
		add(exp);
	}
	
	public And(Exp exp1, Exp exp2){
		add(exp1);
		add(exp2);
	}
	
	public static And create(Exp exp1){
		return new And(exp1);
	}
	
	public static And create(Exp exp1, Exp exp2){
		return new And(exp1, exp2);
	}
	
        @Override
	public boolean isAnd(){
		return true;
	}
	      	
//	boolean validateNegation() {
//		Triple triple;
//		boolean negation=false;
//		for (int i = 0; i < size(); i++) {
//			triple = (Triple) get(i);
//			if (! triple.isExp()){
//				return true;
//			}
//		}
//		return ! negation;
//	}
	
	
        @Override
	public boolean validate(ASTQuery ast, boolean exist){
		boolean ok = true;
		for (Exp exp : getBody()){
			boolean b = exp.validate(ast, exist);
			ok = ok && b;
		}
		return ok;
	}
	
	

	String getOper() {
		return Keyword.SEAND;
	}

    public boolean isStack() {
        return stack;
    }

    public And setStack(boolean stack) {
        this.stack = stack;
        return this;
    }
	
	
}