package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Sequence extends TermEval {  
    
    public Sequence(){}
    
    public Sequence(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype res = TRUE;
        for (Expression exp : getArgs()) {
            res = exp.eval(eval, b, env, p);
                if (b.isResult()) { 
                return res;
            }
        }
        return res;
    }
    
    @Override
    public void tailRecursion(Function fun){
        if (getArity() > 0){
            getArg(getArity() - 1).tailRecursion(fun);
        }
    }
   
}
