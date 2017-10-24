package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListBinary extends TermEval {

    public ListBinary(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getArg(1).eval(eval, b, env, p);
        if (dt2 == null || dt1 == null) {
            return null;
        }       
        switch (oper()) {
            case ExprType.XT_MEMBER:
                return DatatypeMap.member(dt1, dt2);            
            case ExprType.XT_CONS:
                return DatatypeMap.cons(dt1, dt2);
            case ExprType.XT_APPEND:
                return DatatypeMap.append(dt1, dt2);
        }
        return null;
    }
    
}
