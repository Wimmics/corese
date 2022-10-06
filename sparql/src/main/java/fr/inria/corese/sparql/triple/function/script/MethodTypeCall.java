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
public class MethodTypeCall extends Funcall {  
    
    public MethodTypeCall(){}
    
    public MethodTypeCall(String name){
        super(name);
        setArity(2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype name = getBasicArg(0).eval(eval, b, env, p);
        IDatatype type = getBasicArg(1).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 2);
        if (name == null || type == null || param == null){
            return null;
        }
        Function function = getDefineMethod(env, name.stringValue(), type, param);
        if (function == null) {
            return null;
        }
        return call(eval, b, env, p, function, param);
    }
    
    
   
}
