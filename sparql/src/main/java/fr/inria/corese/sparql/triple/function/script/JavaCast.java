package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 * cast:node(exp) return eval(exp)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class JavaCast extends LDScript {  
    
    public JavaCast(){}
    
    public JavaCast(String name){
        super(name);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        return getBasicArg(0).eval(eval, b, env, p);
    }
    
    
   
}
