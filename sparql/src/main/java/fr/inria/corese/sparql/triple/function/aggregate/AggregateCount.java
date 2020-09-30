package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateCount extends Aggregate {

    int num;
    public AggregateCount(){
    }
    
    public AggregateCount(String name) {
        super(name);
        setArity(0);
        start();
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (arity() == 0){          
            return count(env);
        }
        return super.eval(eval, b, env, p);
    }

    
    IDatatype count(Environment env){
        init(env);
        int count = 0;
        for (Mapping map : env.getAggregate()) {
            if (accept(map)){
                count++;
            }
        }
        return value(count);
    }

    
    @Override
    public void aggregate(IDatatype dt) {
      if (accept(dt)) {           
            num++;
        }
    }
    
    @Override
    public void start(){
        num = 0;
    }
    
    @Override
    public IDatatype result() {
        return value(num);
    }
}
