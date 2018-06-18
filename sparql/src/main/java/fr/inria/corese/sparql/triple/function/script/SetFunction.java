package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class SetFunction extends TermEval {  
    
    public SetFunction(){}
    
    public SetFunction(String name){
        super(name);
        setArity(2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {       
        IDatatype val = getBasicArg(1).eval(eval, b, env, p);
        if (val == null) {
            return null;
        }
        b.bind(this, getBasicArg(0), val);
        return val;
    }   
   
}
