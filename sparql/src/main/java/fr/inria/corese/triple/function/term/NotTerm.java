package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class NotTerm extends TermEval {

    public NotTerm(String name) {
        super(name);
    }

    public NotTerm(String name, Expression e1) {
        super(name, e1);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
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
