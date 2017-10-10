package fr.inria.corese.triple.function;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.term.Binding;
import fr.inria.corese.triple.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Sequence extends TermEval {  
    
    public Sequence(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype res = DatatypeMap.TRUE;
        for (Expression exp : getArgs()) {
            res = exp.eval(eval, b, env, p);
            if (isReturn(res)) {
                return res;
            }
        }
        return res;
    }
    
    
   
}
