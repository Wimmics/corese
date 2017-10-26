package fr.inria.corese.triple.function.proxy;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class TemplateFunction extends TermEval {  
    
    public TemplateFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
//        switch (oper()){
//            case ExprType.STL_PROCESS:
//            return eval.getComputerTransform().eval(this, env, p, param); 
//        }
        switch (param.length){
            case 0:  return eval.getComputerTransform().function(this, env, p); 
            case 1:  return eval.getComputerTransform().function(this, env, p, param[0]); 
            case 2:  return eval.getComputerTransform().function(this, env, p, param[0], param[1]); 
            default: return eval.getComputerTransform().eval(this, env, p, param); 
        }
    }
    
    
   
}

