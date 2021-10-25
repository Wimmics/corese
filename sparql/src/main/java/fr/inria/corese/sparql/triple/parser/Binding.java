package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.Walker;
import java.util.List;

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
    
    @Override
    public Binding copy() {
        return this;
    }
        
    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append("bind (");
        exp.toString(sb);
        sb.append(" as ");
        var.toString(sb);
        sb.append(")");
        return sb;
    }

    @Override
    public boolean isBind() {
        return true;
    }
    
    @Override
    public Binding getBind() {
        return this;
    }
    
    @Override
    public Expression getFilter(){
	return exp;
    }
	
   public Variable getVariable(){
       return var;
   }
   
    @Override
   public void walk(Walker walker) {
       walker.enter(this);
       //getVariable().walk(walker);
       getFilter().walk(walker);
       walker.leave(this);
   }
   
   /*
    * bind (exp as var)
    * bind (exp as (v1, .. vn))
    * */
    public boolean validate(ASTQuery ast, boolean exist) {
        List<Variable> list = var.getVariableList();
        
        if (list == null || list.isEmpty()){
            if (ast.isBound(var)) {
                ast.addErrorMessage(Message.SCOPE_ERROR , var);
                ast.setCorrect(false);
                return false;
            }
            ast.bind(var);
            ast.defSelect(var); 
        }
        else {
            boolean ok = true;
            for (Variable v : list){
                if (ast.isBound(v)) {
                ast.addErrorMessage(Message.SCOPE_ERROR , v);
                    ast.setCorrect(false);
                    ok = false;
                }
                ast.bind(v);
                ast.defSelect(v); 
            }
            if (! ok){
                return false;
            }
        }
      
        boolean b = exp.validate(ast);
        return b;
    }

    void getVariables(List<Variable> list) {
        add(getVariable(), list);
    }
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        getVariables(list);
    }

}
