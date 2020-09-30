package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.core.BinaryFunction;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListBinary extends BinaryFunction {

    public ListBinary(){}
    
    public ListBinary(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = getExp1().eval(eval, b, env, p);
        IDatatype dt2 = getExp2().eval(eval, b, env, p);
        if (dt2 == null || dt1 == null) {
            return null;
        }       
        switch (oper()) {
            case ExprType.XT_MEMBER:
                // return DatatypeMap.member(dt1, dt2);            
                // dt1 = elem, dt2 = list/table
                return dt2.member(dt1); 
            case ExprType.XT_CONS:
                return DatatypeMap.cons(dt1, dt2);
            case ExprType.XT_APPEND:
                return DatatypeMap.append(dt1, dt2);
            case ExprType.XT_REMOVE:
                return DatatypeMap.remove(dt1, dt2);
            case ExprType.XT_REMOVE_INDEX:
                return DatatypeMap.remove(dt1, dt2.intValue());                
        }
        return null;
    }
    
}
