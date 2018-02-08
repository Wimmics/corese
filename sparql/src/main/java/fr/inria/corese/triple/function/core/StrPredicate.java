package fr.inria.corese.triple.function.core;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class StrPredicate extends BinaryFunction {
    
    public StrPredicate(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
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
