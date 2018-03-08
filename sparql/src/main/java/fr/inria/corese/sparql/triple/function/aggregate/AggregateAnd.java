package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

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
