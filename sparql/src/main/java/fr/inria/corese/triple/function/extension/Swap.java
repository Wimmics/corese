package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Swap extends TermEval {

    public Swap(){}
    
    public Swap(String name){
        super(name);
        setArity(3);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype list = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dt1  = getBasicArg(1).eval(eval, b, env, p);
        IDatatype dt2  = getBasicArg(2).eval(eval, b, env, p);
        if (list == null || dt1 == null || dt2 == null) {
            return null;
        }
        return DatatypeMap.swap(list, dt1, dt2);
    }
    
}
