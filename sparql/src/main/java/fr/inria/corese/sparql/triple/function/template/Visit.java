package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Visit extends TemplateFunction {  
        
    public Visit(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        
        switch (oper()) {
            case ExprType.STL_VISITED:
                switch (param.length) {
                    case 0:  return eval.getComputerTransform().visited(env, p, null);
                    default: return eval.getComputerTransform().visited(env, p, param[0]);
                }
                
            case ExprType.STL_VISIT:
                switch (param.length) {
                    case 1: return eval.getComputerTransform().visit(env, p, null, param[0], null);
                    case 2: return eval.getComputerTransform().visit(env, p, param[0], param[1], null);
                    case 3: return eval.getComputerTransform().visit(env, p, param[0], param[1], param[2]);
                }
                                
            default: return  null;
        }
    }
    
   
}

