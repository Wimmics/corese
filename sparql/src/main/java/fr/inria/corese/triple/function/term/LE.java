package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.function.core.BinaryFunction;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class LE extends BinaryFunction {
        
    public LE(){
    }
 
    public LE(String name){
        super(name);
    }
    
    public LE(String name, Expression e1, Expression e2){
        super(name, e1, e2);
    }
         
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt1 = getExp1().eval(eval, b, env, p);
        IDatatype dt2 = getExp2().eval(eval, b, env, p);
        if (dt1 == null || dt2 == null) {
            return null;
        }
        return dt1.le(dt2);
    }
      
}
