package fr.inria.corese.sparql.triple.function.core;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
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
public class BiTriFunction extends TermEval {
       
    public BiTriFunction(){}
    
    public BiTriFunction(String name){
        super(name);
        setArity(2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) return null;
        
        IDatatype dt3 = null, dt4 = null;
        if (arity() == 3) {
            dt3 = getBasicArg(2).eval(eval, b, env, p);
        }
        switch (oper()) {
            case ExprType.REGEX:
                if (eval.isCompliant() && !isStringLiteral(dt1)) {
                    return null;
                }
                boolean res = getProcessor().regex(dt1.stringValue(), dt2.stringValue(), (dt3 == null) ? null : dt3.stringValue());
                return value(res);
                
            case ExprType.SUBSTR:
                if (eval.isCompliant() && ! isStringLiteral(dt1)) {
                     return null;
                }
                return substr(dt1, dt2, dt3);
                
             case ExprType.STRREPLACE:
                 if (eval.isCompliant() && ! isStringLiteral(dt1)) {
                     return null;
                 }
                 if (dt3 == null) return null;
                 if (arity() == 4){
                     dt4 = getBasicArg(3).eval(eval, b, env, p);
                     if (dt4 == null) return null;
                 }
                String str = getProcessor().replace(dt1.stringValue(), dt2.stringValue(), dt3.stringValue(), (dt4 == null) ? null : dt4.stringValue());
                return result(str, dt1, dt3);
        }
        return null;
    }
    
    IDatatype substr(IDatatype dt, IDatatype ind, IDatatype len) {
        String str = dt.getLabel();
        int start = ind.intValue();
        start = Math.max(start - 1, 0);
        int end = str.length();
        if (len != null) {
            end = len.intValue();
        }
        end = start + end;
        if (end > str.length()) {
            end = str.length();
        }
        str = str.substring(start, end);
        return result(dt, str);
    }
 
}
