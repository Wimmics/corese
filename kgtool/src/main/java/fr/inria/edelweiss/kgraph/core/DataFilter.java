package fr.inria.edelweiss.kgraph.core;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * Simple filter applied to object or subject of current edge
 * during iteration 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class DataFilter implements ExprType {
    int test;
    int index;
    boolean positive = true;
    IDatatype value;
    
    DataFilter(int test){
        this(test, null, 1);
    }
    
    DataFilter(int test, int index){
       this(test, null, index);
    }
    
    DataFilter(int test, IDatatype dt){
        this(test, dt, 1);
    }
    
    DataFilter(int test, IDatatype dt, int index){
        this.test = test;
        this.value = dt;
        this.index = index;
    }
    
    void not(){
        positive = ! positive;
    }
    
    boolean result(boolean b){
        return (positive) ? b : !b;
    }
    
    boolean eval(Entity ent) {
        IDatatype dt = (IDatatype)ent.getNode(index).getValue();
        try {
            switch (test){  
                
                case GT:
                    return result(dt.greater(value));
                case GE:
                    return result(dt.greaterOrEqual(value));
                case LE:
                    return result(dt.lessOrEqual(value));
                case LT:
                    return result(dt.less(value));
                case EQ:
                    return result(dt.equals(value));
                case NEQ:
                    return result(! dt.equals(value));
                case CONTAINS:
                    return result(dt.contains(value));
                case STARTS:
                    return result(dt.startsWith(value));

                case ISURI:
                    return result(dt.isURI());

                case ISLITERAL:
                    return result(dt.isLiteral());

                case ISBLANK:
                    return result(dt.isBlank());

                case ISSKOLEM:
                    return result(dt.isSkolem());

                case ISNUMERIC:
                    return result(dt.isNumber());

                default: return true;
            }
        }
        catch (CoreseDatatypeException e){
            return false;
        }
    }
    

}
