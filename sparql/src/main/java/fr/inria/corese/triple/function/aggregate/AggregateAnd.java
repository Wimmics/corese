package fr.inria.corese.triple.function.aggregate;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateAnd extends Aggregate {
   
    boolean and = true;

    public AggregateAnd(){}
  
    public AggregateAnd(String name) {
        super(name);
        start();
    }

    @Override
    public void aggregate(IDatatype dt) {
        if (dt == null) {
            isError = true;
        } else {
            try {
                boolean b = dt.isTrue();
                and &= b;

            } catch (CoreseDatatypeException ex) {
                isError = true;
            }
        }
    }
    
    @Override
    public void start(){
        and = true;
        
    }
    
    @Override
    public IDatatype result() {
        return value(and);
    }
}
