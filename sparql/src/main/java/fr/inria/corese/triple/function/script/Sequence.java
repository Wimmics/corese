package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Sequence extends TermEval {  
    
    public Sequence(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype res = TRUE;
        for (Expression exp : getArgs()) {
            res = exp.eval(eval, b, env, p);
                if (b.isResult()) { //if (isReturn(res)) {
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
