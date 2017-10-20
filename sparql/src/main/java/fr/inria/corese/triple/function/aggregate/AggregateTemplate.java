package fr.inria.corese.triple.function.aggregate;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Function;
import fr.inria.corese.triple.function.script.Extension;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

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
          
    //@Override
    public IDatatype eval2(Computer eval, Binding b, Environment env, Producer p) {  
        return eval.aggregate(this, env, p);
    }
    
    /**
     * template st:aggregate(?out)
     * Two cases:
     * 1- function st:aggregate(?x) { st:agg_and(?x) }
     * 2- st:aggregate = st:group_concat .
     */    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {  
        init(eval, b, env, p);
        return super.eval(eval, b, env, p);
    }
    
    @Override
    public void aggregate(IDatatype dt) {  
        proxy.aggregate(dt);
    }
    
    void init(Computer eval, Binding b, Environment env, Producer p) {
        Function function = (Function) eval.getDefine(this, env);
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
