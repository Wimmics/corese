package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class MethodTypeCall extends TermEval {  
    
    public MethodTypeCall(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype name = getArg(0).eval(eval, b, env, p);
        IDatatype type = getArg(1).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 2);
        if (name == null || type == null || param == null){
            return null;
        }
        return eval.method(name.stringValue(), type,  param, env, p);
    }
    
    
   
}
