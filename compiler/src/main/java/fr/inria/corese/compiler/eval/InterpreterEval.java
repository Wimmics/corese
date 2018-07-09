package fr.inria.corese.compiler.eval;

import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.sparql.api.ComputerEval;

/**
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class InterpreterEval extends Eval implements ComputerEval {
    Interpreter eval;
    
    InterpreterEval (Producer p, Interpreter e, Matcher m) {
        super(p, e, m);
        eval = e;
    }
    
    @Override
    public Interpreter getComputer() {
        return eval;
    }
    
    @Override
    public Eval getEval() {
        return this;
    }

}
