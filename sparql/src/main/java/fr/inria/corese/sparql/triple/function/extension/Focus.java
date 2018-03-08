package fr.inria.corese.sparql.triple.function.extension;

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
public class Focus extends TermEval {

    public Focus() {}
    
    public Focus(String name) {
        super(name);
        setArity(2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);       
        if (dt == null || !p.isProducer(dt)) {
            return null;
        }
        Producer pp = p.getProducer(dt, env);
        return getBasicArg(1).eval(eval, b, env, pp);
    }

   
}
