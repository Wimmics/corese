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
public class ListUnary extends TermEval {

    public ListUnary(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype list = getArg(0).eval(eval, b, env, p);       
        if (list == null) {
            return null;
        }
        switch (oper()){
            case ExprType.XT_FIRST:    return DatatypeMap.first(list);
            case ExprType.XT_REST:     return DatatypeMap.rest(list);
            case ExprType.XT_SORT:     return DatatypeMap.sort(list);
            case ExprType.XT_REVERSE:  return DatatypeMap.reverse(list);                          
        }
        return null;
    }
    
}
