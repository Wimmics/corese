package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.ArrayList;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class IfThenElseTerm extends TermEval {
    Expression test, e1, e2;
      
    public IfThenElseTerm(){}
    
    public IfThenElseTerm(String name){
        super(name);
    }
    
    
    @Override
    public void add(Expression exp) {
        super.add(exp);
        if (getArgs().size() == 3) {
            init();
        }
    }
    
    @Override
    public void setArg(int i, Expression exp){
        super.setArg(i, exp);
        set(i, exp);
    }
    
    @Override
    public void setArgs(ArrayList<Expression> list) {
        super.setArgs(list);
        init();
    }
    
    void init() {
        for (int i = 0; i < getArgs().size(); i++) {
            set(i, getArg(i));
        }
    }
    
    void set(int i, Expression exp){
        switch (i) {
            case 0: test = exp; break;
            case 1: e1 = exp; break;
            case 2: e2 = exp; break;
        }
    }
       
     @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype val = test.eval(eval, b, env, p);
        if (val == null) {
            return null;
        }
        if (val.booleanValue()) {
            return e1.eval(eval, b, env, p);
        } else {
            return e2.eval(eval, b, env, p);
        }
    }
    
     @Override
    public void tailRecursion(Function fun){
        e1.tailRecursion(fun);
        e2.tailRecursion(fun);
    }
   
}
