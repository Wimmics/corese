package fr.inria.corese.sparql.triple.function.proxy;

import static fr.inria.corese.kgram.api.core.ExprType.DEPTH;
import static fr.inria.corese.kgram.api.core.ExprType.LOAD;
import static fr.inria.corese.kgram.api.core.ExprType.WRITE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TUNE;
import static fr.inria.corese.kgram.api.core.ExprType.SIM;
import static fr.inria.corese.kgram.api.core.ExprType.XT_EDGE;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.GraphProcessor;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphSpecificFunction extends TermEval {  
    
    public GraphSpecificFunction(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }  
        
       GraphProcessor proc = eval.getGraphProcessor();
        
        switch (oper()) {
            case LOAD:
                switch (param.length) {
                    case 1:
                        return proc.load(param[0], null);
                    default:
                        return proc.load(param[0], param[1]);
                            
                }
                
            case WRITE:
                return proc.write(param[0], param[1]);
                
            case SIM:
                switch (param.length) {
                    case 0:
                        return proc.similarity(env, p);
                    case 2:
                        return proc.similarity(env, p, param[0], param[1]);
                    default: return null;
                }
                
            case DEPTH:
                return proc.depth(env, p, param[0]);
                
            case XT_TUNE:
                return proc.tune(this, env, p, param[0], param[1]);
                
            case XT_EDGE:
                switch (param.length) {
                    case 0:
                        return proc.edge(this, env, p, null, null, null);
                    case 1:
                        return proc.edge(this, env, p, null, param[0], null);    
                    case 2:
                        return proc.edge(this, env, p, param[0], param[1], null); 
                    default:
                        return proc.edge(this, env, p, param[0], param[1], param[2]);
                                                
                }
                
            default: return null;
                
        }
        
    }
    
    
  
         
}

