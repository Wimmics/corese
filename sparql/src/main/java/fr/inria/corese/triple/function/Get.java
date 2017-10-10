package fr.inria.corese.triple.function;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.term.Binding;
import fr.inria.corese.triple.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Get extends TermEval {

    public Get(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        return dt1.get(dt2.intValue());
    }
    
}
