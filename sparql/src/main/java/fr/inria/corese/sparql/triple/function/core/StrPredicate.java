package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class StrPredicate extends BinaryFunction {
    
    public StrPredicate() {        
    }
    
    public StrPredicate(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = getExp1().eval(eval, b, env, p);
        IDatatype dt2 = getExp2().eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        if (!compatible(dt1, dt2)) {
                    return null;
        }
        switch (oper()) {
            case ExprType.STARTS:
                return value(dt1.getLabel().startsWith(dt2.getLabel()));
            case ExprType.ENDS:
                return value(dt1.getLabel().endsWith(dt2.getLabel()));
            case ExprType.CONTAINS:
                return value(dt1.getLabel().contains(dt2.getLabel()));
            case ExprType.STRBEFORE:
                if (eval.isCompliant() &&  ! (isStringLiteral(dt1) && isStringLiteral(dt2))) return null;
                return strbefore(dt1, dt2);
            case ExprType.STRAFTER:
                 if (eval.isCompliant() && ! (isStringLiteral(dt1) && isStringLiteral(dt2))) return null;
               return strafter(dt1, dt2); 
               
            case ExprType.XT_SPLIT:
                return DatatypeMap.split(dt1, dt2);
        }
        return null;
    }
    
     IDatatype strbefore(IDatatype dt1, IDatatype dt2) {       
        int index = dt1.getLabel().indexOf(dt2.getLabel());
        String str = "";
        if (index != -1) {
            str = dt1.getLabel().substring(0, index);
        }
        return result(str, dt1, dt2);
    }

     IDatatype strafter(IDatatype dt1, IDatatype dt2) {        
        int index = dt1.getLabel().indexOf(dt2.getLabel());
        String str = "";
        if (index != -1) {
            str = dt1.getLabel().substring(index + dt2.getLabel().length());
        }
        return result(str, dt1, dt2);
    }
    
}
