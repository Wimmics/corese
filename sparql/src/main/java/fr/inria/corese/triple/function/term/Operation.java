package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Operation extends TermEval {
    
    public Operation(String name){
        super(name);
    }
    
    public Operation(String name, Expression e1, Expression e2){
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
            case ExprType.PLUS:     return dt1.plus(dt2);
            case ExprType.MULT:     return dt1.mult(dt2);
            case ExprType.DIV:      return dt1.div(dt2);
            case ExprType.MINUS:    return dt1.minus(dt2);
            case ExprType.POWER:    return DatatypeMap.newInstance(Math.pow(dt1.doubleValue(), dt2.doubleValue()));
        }
        return null;
    }
    
}
