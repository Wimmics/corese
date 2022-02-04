package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class BinaryFunction extends TermEval {
    
    private Expression e1, e2;
    
    public BinaryFunction(){      
    }    
          
    public BinaryFunction(String name){
        super(name);
        setArity(2);
    }
    
    public BinaryFunction(String name, Expression e1, Expression e2){
        super(name, e1, e2);
        setArity(2);
    } 
    
    public Expression getExp1() {
        return e1;
    }
    
    public Expression getExp2() {
        return e2;
    }      
      
    @Override
    public void add(Expression exp) {
        // do not move this (because it tests arity)
       set(getArgs().size(), exp);
       super.add(exp);
    }
    
    @Override
    public void setArg(int i, Expression exp){
        set(i, exp);
        super.setArg(i, exp);
    }
    
    @Override
    public void setArgs(ArrayList<Expression> list) {
        super.setArgs(list);
        for (int i = 0; i<list.size(); i++){
            set(i, getArg(i));
        }
    }
    
    void set(int i, Expression exp){
        switch (i) {
            case 0: e1 = exp; break;
            case 1: e2 = exp; break;
        }
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = e1.eval(eval, b, env, p);
        IDatatype dt2 = e2.eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) return null;
        
        switch (oper()){
            case ExprType.LANGMATCH: 
                return langMatches(dt1, dt2);
                
            case ExprType.STRLANG:
                if (eval.isCompliant() && !isSimpleLiteral(dt1)) return null;                
                return DatatypeMap.newInstance(dt1.getPrettyLabel(), null, dt2.getLabel());
                
            case ExprType.STRDT:    
                if (eval.isCompliant() && !isSimpleLiteral(dt1)) return null;                
                return DatatypeMap.newInstance(dt1.getPrettyLabel(), dt2.getLabel());
                
            case ExprType.SAMETERM:  return value(dt1.sameTerm(dt2));        
        }
        
        return null;
    }
    
    
    IDatatype langMatches(IDatatype ln1, IDatatype ln2) {
        String l1 = ln1.getLabel();
        String l2 = ln2.getLabel();

        if (l2.equals("*")) {
            return value(l1.length() > 0);
        }
        if (l2.indexOf("-") != -1) {
            // en-us need exact match
            return value(l1.toLowerCase().equals(l2.toLowerCase()));
        }
        return value(l1.regionMatches(true, 0, l2, 0, 2));
    }
    
    @Override
    public void tailRecursion(Function fun){
        e2.tailRecursion(fun);
    }
 
}
