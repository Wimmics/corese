package fr.inria.corese.triple.function;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.term.Binding;
import fr.inria.corese.triple.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Funcall extends TermEval {  
    
    public Funcall(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype name = getArg(0).eval(eval, b, env, p);
        IDatatype[] param = eval(eval, b, env, p, 1);
        if (name == null || param == null){
            return null;
        }
        return eval.funcall(name, param, this, env, p);
    }
    
    
   
}
