package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class MethodCall extends Funcall {  
    
    public MethodCall(){}
    
    public MethodCall(String name){
        super(name);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name    = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null){
            return null;
        }
        //return eval.method(name.stringValue(), null, param, env, p);
        Function function = getDefineMethod(env, name.stringValue(), null, param);
        if (function == null) {
            return null;
        }
        return call(eval, b, env, p, function, param);
    }
    
    
   
}
