package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListUnary extends TermEval {

    public ListUnary(){}
    
    public ListUnary(String name){
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype list = getBasicArg(0).eval(eval, b, env, p);       
        if (list == null) {
            return null;
        }
        switch (oper()){
            case ExprType.XT_FIRST:    return DatatypeMap.first(list);
            case ExprType.XT_REST:     return DatatypeMap.rest(list);           
            case ExprType.XT_REVERSE:  return DatatypeMap.reverse(list);                          
        }
        return null;
    }
    
}
