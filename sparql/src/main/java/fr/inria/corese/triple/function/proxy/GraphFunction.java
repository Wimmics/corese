package fr.inria.corese.triple.function.proxy;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphFunction extends TermEval {  
    
    public GraphFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }        
        switch (param.length){
            case 0:  return eval.getComputerPlugin().function(this, env, p); 
            case 1:  return eval.getComputerPlugin().function(this, env, p, param[0]); 
            case 2:  return eval.getComputerPlugin().function(this, env, p, param[0], param[1]); 
            default: return eval.getComputerPlugin().eval(this, env, p, param); 
        }
    }
         
}

