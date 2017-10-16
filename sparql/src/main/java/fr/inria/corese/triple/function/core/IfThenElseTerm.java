package fr.inria.corese.triple.function.core;

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
public class IfThenElseTerm extends TermEval {

    public IfThenElseTerm(String name, Expression e1, Expression e2, Expression e3){
        super(name, e1, e2, e3);
    }
    
    public IfThenElseTerm(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype test = getArg(0).eval(eval, b, env, p);
        if (test == null) {
            return null;
        }
        if (isTrue(test)) { 
            return getArg(1).eval(eval, b, env, p);
        } else {
            return getArg(2).eval(eval, b, env, p);
        }
    }
}
