package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.core.BinaryFunction;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Operation extends BinaryFunction {
        
    public Operation(){
    }
    
    public Operation(String name){
        super(name);
    }
    
    public Operation(String name, Expression e1, Expression e2){
        super(name, e1, e2);
    }
         
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt1 = getExp1().eval(eval, b, env, p);
        IDatatype dt2 = getExp2().eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        switch (oper()){
            case ExprType.PLUS:     return dt1.plus(dt2);
            case ExprType.MULT:     return dt1.mult(dt2);
            case ExprType.DIV:      return dt1.div(dt2);
            case ExprType.MINUS:    return dt1.minus(dt2);
            case ExprType.POWER:    return DatatypeMap.newInstance(Math.pow(dt1.doubleValue(), dt2.doubleValue()));
            
            case ExprType.EQUAL:    return dt1.equals(dt2)?TRUE:FALSE;
            case ExprType.NOT_EQUAL:
                try { return dt1.equalsWE(dt2)?FALSE:TRUE; } 
                catch (CoreseDatatypeException e) {
                    return TRUE;
                }
                
            case ExprType.EQ:       return dt1.eq(dt2);
            case ExprType.NEQ:      return dt1.neq(dt2);
            case ExprType.LE:       return dt1.le(dt2);
            case ExprType.LT:       return dt1.lt(dt2);
            case ExprType.GT:       return dt1.gt(dt2);
            case ExprType.GE:       return dt1.ge(dt2);
        }
        return null;
    }
      
}
