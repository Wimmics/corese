package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.kgram.api.core.ExprType;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateSumAvg extends Aggregate {

    IDatatype dtres;
    int num;
    
    public AggregateSumAvg(){}
    
    public AggregateSumAvg(String name) {
        super(name);
        start();
    }

    @Override
    public void aggregate(IDatatype dt) {
        if (!dt.isNumber()) {
            isError = true;
        } else if (accept(dt)) {
            if (dtres == null) {
                dtres = dt;
            } 
            else {
                dtres = dtres.plus(dt);
            }
            num++;
        }
    }
    
    @Override
    public void start(){
        num = 0;
        dtres = null;
    }
    
    @Override
    public IDatatype result() {
        switch (oper()){
            case ExprType.SUM:
                return dtres;
            case ExprType.AVG:
                 if (dtres == null) {
                    return DatatypeMap.ZERO;
                }
                try {
                    IDatatype dt = dtres.div(DatatypeMap.newInstance(num));
                    return dt;
                } catch (java.lang.ArithmeticException e) {
                    return null;
                }    
            default:
                return null;
        }
    }
}
