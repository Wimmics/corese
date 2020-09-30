package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class NotTerm extends TermEval {

    public NotTerm() {       
    }

    public NotTerm(String name) {
        super(name);
        setArity(1);
    }

    public NotTerm(String name, Expression e1) {
        super(name, e1);
        setArity(1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null || !dt.isTrueAble()) {
            return null;
        }
        if (isTrue(dt)) {
            return FALSE;
        } else {
            return TRUE;
        }
    }

   
}
