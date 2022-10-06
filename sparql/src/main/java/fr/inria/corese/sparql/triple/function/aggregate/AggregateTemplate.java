package fr.inria.corese.sparql.triple.function.aggregate;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class AggregateTemplate extends Aggregate {
    Aggregate proxy;
    
    public AggregateTemplate(){}
    
    public AggregateTemplate(String name) {
        super(name);
    }
              
    /**
     * template st:aggregate(?out)
     * Two cases:
     * 1- function st:aggregate(?x) { st:agg_and(?x) }
     * 2- st:aggregate = st:group_concat .
     */    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {  
        init(eval, b, env, p);
        return super.eval(eval, b, env, p);
    }
    
    @Override
    public void aggregate(IDatatype dt) {  
        proxy.aggregate(dt);
    }
    
    void init(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        Function function = getDefine(this, env);
        if (function == null || ! (function.getBody() instanceof Aggregate) ) {
            // eval st:group_concat
            AggregateGroupConcat agg = groupconcat(); 
            agg.init(eval, b, env, p);
            proxy = agg;
        } else {
            // eval st:aggregate extension function
            // proxy = st:agg_and(?x)
            proxy = (Aggregate) function.getBody();
        }
        
        proxy.start();
    }
    
    AggregateGroupConcat groupconcat() {
        AggregateGroupConcat agg = new AggregateGroupConcat(getName());
        fill(agg);
        agg.setOper(ExprType.STL_GROUPCONCAT);
        return agg;
    }
    
    @Override
    public IDatatype result(){
        return proxy.result();
    }
            
}
