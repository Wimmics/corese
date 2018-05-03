package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.triple.cst.Keyword;
import fr.inria.corese.sparql.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Or extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	static int num = 0;
	
	public Or() {}
	
	public  Or (Exp e1, Exp e2){
		add(e1);
		add(e2);		
	}
	
	public static Or create(){
		return new Or();
	}
	
	public static Or create(Exp e1, Exp e2){
		if (!e1.isAnd()){
			e1 = new BasicGraphPattern(e1);
		}
		if (!e2.isAnd()){
			e2 = new BasicGraphPattern(e2);
		}
		return new Or(e1, e2);
	}
	
	
        @Override
	public boolean isUnion(){
		return true;
	}
	
    @Override
    void getVariables(List<Variable> list) {
        if (size() > 1) {
            List<Variable> left  = get(0).getVariables();
            List<Variable> right = get(1).getVariables();
            
            for (Variable var : left) {
                if (right.contains(var)) {
                    add(var, list);
                }
            }
        }
    }
	
	String getOper() {
		return Keyword.SEOR;
	}
			
	
        @Override
	public ASTBuffer toString(ASTBuffer sb) {
            sb.append("{ ").incr();
            get(0).display(sb);
            sb.decr().nl().append("} ").append(KeywordPP.UNION).append(" ");
            get(1).pretty(sb);
            return sb;
	}
	
	/**
	 * Each branch of union binds its variable (in parallel)
	 */
        @Override
	public boolean validate(ASTQuery ast, boolean exist){
		boolean ok = true;
		
		List<Variable> list = ast.getStack();
		List<List<Variable>> ll = new ArrayList();
		
		for (Exp exp : getBody()){
			ast.newStack();
			boolean b = exp.validate(ast, exist);
			if (! b){
				ok = false;
			}
			ll.add(ast.getStack());
		}
		
		ast.setStack(list);
		
		for (List<Variable> l : ll){
			ast.addStack(l);
		}
		return ok;	
	}
	
}