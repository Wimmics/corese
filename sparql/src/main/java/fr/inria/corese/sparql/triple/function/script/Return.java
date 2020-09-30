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
public class Return extends LDScript {  
    
    public Return(){}
    
    public Return(String name){
        super(name);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) return null;
        return b.result(dt);
    }
    
     @Override
    public void tailRecursion(Function fun) {
        getArg(0).tailRecursion(fun);
    }
   
}
