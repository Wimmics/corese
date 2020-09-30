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
public class AndTerm extends TermEval {
    
    public AndTerm() {
    }
    
    public AndTerm(String name) {
        super(name);
    }

    public AndTerm(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        boolean error = false;
        for (Expression arg : getArgs()) {
            IDatatype o = arg.eval(eval, b, env, p);
            if (o == null || !o.isTrueAble()) {
                error = true;
            } else if (!(isTrue(o))) {
                return FALSE;
            }
        }
        if (error) {
            return null;
        }
        return TRUE;
    }

}
