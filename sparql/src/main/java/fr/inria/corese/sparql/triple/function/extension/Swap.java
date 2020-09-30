package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype list = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dt1  = getBasicArg(1).eval(eval, b, env, p);
        IDatatype dt2  = getBasicArg(2).eval(eval, b, env, p);
        if (list == null || dt1 == null || dt2 == null) {
            return null;
        }
        return DatatypeMap.swap(list, dt1, dt2);
    }
    
}
