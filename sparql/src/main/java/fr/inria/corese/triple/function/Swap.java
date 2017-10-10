package fr.inria.corese.triple.function;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.term.Binding;
import fr.inria.corese.triple.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Swap extends TermEval {

    public Swap(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype list = getArg(0).eval(eval, b, env, p);
        IDatatype dt1  = getArg(1).eval(eval, b, env, p);
        IDatatype dt2  = getArg(2).eval(eval, b, env, p);
        if (list == null || dt1 == null || dt2 == null) {
            return null;
        }
        return DatatypeMap.swap(list, dt1, dt2);
    }
    
}
