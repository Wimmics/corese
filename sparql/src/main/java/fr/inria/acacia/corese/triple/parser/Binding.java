/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.acacia.corese.triple.parser;

/**
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class Binding extends Exp {
    Expression exp;
    Variable var;
    
    Binding(Expression exp, Variable var){
        this.exp = exp;
        this.var = var;
    }
    
    public static Binding create(Expression exp, Variable var){
        return new Binding(exp, var);
    }
    
    
    public StringBuffer toString(StringBuffer sb) {
        sb.append("bind(");
        exp.toString(sb);
        sb.append(" as ");
        var.toString(sb);
        sb.append(")");
        return sb;
    }

    public boolean isBind() {
        return true;
    }
    
    public Expression getFilter(){
	return exp;
    }
	
   public Variable getVariable(){
       return var;
   }
   
    public boolean validate(ASTQuery ast, boolean exist) {
        if (ast.isBound(var)) {
            ast.addError("Scope error: " + var);
            ast.setCorrect(false);
            return false;
        }
        ast.bind(var);
        ast.defSelect(var);
      
        boolean b = exp.validate(ast);
        return b;
    }

    

}
