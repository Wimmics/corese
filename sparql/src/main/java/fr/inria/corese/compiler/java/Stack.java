package fr.inria.corese.compiler.java;

import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.function.script.ForLoop;
import fr.inria.corese.triple.function.script.Function;
import fr.inria.corese.triple.function.script.Let;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import java.util.ArrayList;
import java.util.List;

/**
 * Stack record bound variables for parameter passing to exists and subquery 
 * Bound variables are function parameters and let local variables
 * TODO: for (?x in exp)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Stack {
    
    ArrayList<Variable> varList;
    ArrayList<Expression> expList;
    
    Stack(){
        varList = new ArrayList<Variable>();
        expList = new ArrayList<Expression>();
    }
    
    void push(Function exp){
        expList.add(exp);
        for (Expression arg : exp.getFunction().getArgs()){
            varList.add(arg.getVariable());
        }
    }
    
    void push(Let exp){
        expList.add(exp);
        varList.add(exp.getVariable());
    }
    
     void push(ForLoop exp){
        expList.add(exp);
        varList.add(exp.getVariable());
    }
    
    void popFunction(Expression exp){
        for (Expression arg : exp.getFunction().getArgs()){
            popVar();
        }
    }
    
    void popLet(Expression exp){
        popVar();
    }
    
    void popFor(Expression exp){
        popVar();
    }
    
    void popVar(){
        varList.remove(varList.size() - 1);
    }
    
    void pop(Expression e){        
        Expression exp = expList.remove(expList.size() - 1);
        switch(exp.oper()){
            case ExprType.FUNCTION: popFunction(exp); break;
            case ExprType.LET: popLet(exp); break;
            case ExprType.FOR: popFor(exp); break;
        }
    }

    List<Variable> getVariables() {
        return varList;
    }

}
