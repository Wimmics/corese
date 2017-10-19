package fr.inria.corese.triple.function.aggregate;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateTemplate extends Aggregate {
    
    public AggregateTemplate(){}
    
    public AggregateTemplate(String name) {
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {       
        return eval.aggregate(this, env, p);
    }

    
}
