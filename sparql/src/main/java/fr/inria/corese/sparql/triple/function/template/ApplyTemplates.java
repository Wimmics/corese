package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ApplyTemplates extends TemplateFunction {  
        
    public ApplyTemplates(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        switch (param.length){
            case 0:  return eval.getComputerTransform().transform(null, null, null, this, env, p); 
            case 1:  return eval.getComputerTransform().transform(null, param[0], null, null, null, this, env, p); 
            default: return eval.getComputerTransform().transform(param, param[0], null, null, null, this, env, p); 
        }
    }
    
    
    
    public IDatatype eval2(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }

        switch (param.length){
            case 0:  return eval.getComputerTransform().function(this, env, p); 
            case 1:  return eval.getComputerTransform().function(this, env, p, param[0]); 
            case 2:  return eval.getComputerTransform().function(this, env, p, param[0], param[1]); 
            default: return eval.getComputerTransform().eval(this, env, p, param); 
        }
    }
   
}

