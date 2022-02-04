package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import static fr.inria.corese.kgram.api.core.ExprType.MIN;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateMinMax extends Aggregate {

    IDatatype dtres;
    
    public AggregateMinMax(){}
    
    public AggregateMinMax(String name) {
        super(name);
    }

    @Override
    public void aggregate(IDatatype dt) {
        if (dt.isBlank() && ! dt.isTriple()) {
            isError = true;
        } else if (dtres == null) {
            dtres = dt;
        } else {
            try {
                if (oper() == MIN) {
                    if (dt.less(dtres)) {
                        dtres = dt;
                    }
                } else if (dt.greater(dtres)) {
                    dtres = dt;
                }
            } catch (CoreseDatatypeException e) {
                // TODO: check this
            }
        }
    }

    @Override
    public void start() {
        dtres = null;
    }

    @Override
    public IDatatype result() {
        return dtres;
    }
}
