package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Compare extends TermEval {
    
    public Compare(String name) {
        super(name);
    }
    
    public Compare(String name, Expression e1, Expression e2){
        super(name, e1, e2);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getArg(0).eval(eval, b, env, p);
        IDatatype dt2 = getArg(1).eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        switch (oper()){
            case ExprType.EQ: return dt1.eq(dt2);
            case ExprType.NEQ: return dt1.neq(dt2);
            case ExprType.LE: return dt1.le(dt2);
            case ExprType.LT: return dt1.lt(dt2);
            case ExprType.GT: return dt1.gt(dt2);
            case ExprType.GE: return dt1.ge(dt2);
        }
        return null;
    } 
}
