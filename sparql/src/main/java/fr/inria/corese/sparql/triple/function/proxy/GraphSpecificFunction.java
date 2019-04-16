package fr.inria.corese.sparql.triple.function.proxy;

import static fr.inria.corese.kgram.api.core.ExprType.APPROXIMATE;
import static fr.inria.corese.kgram.api.core.ExprType.APP_SIM;
import static fr.inria.corese.kgram.api.core.ExprType.DEPTH;
import static fr.inria.corese.kgram.api.core.ExprType.KGRAM;
import static fr.inria.corese.kgram.api.core.ExprType.LOAD;
import static fr.inria.corese.kgram.api.core.ExprType.WRITE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TUNE;
import static fr.inria.corese.kgram.api.core.ExprType.SIM;
import static fr.inria.corese.kgram.api.core.ExprType.STL_INDEX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ENTAILMENT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_EXISTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_JOIN;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MINUS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_OPTIONAL;
import static fr.inria.corese.kgram.api.core.ExprType.XT_UNION;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.GraphProcessor;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SHAPE_GRAPH;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SHAPE_NODE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TOGRAPH;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.triple.function.script.LDScript;
import java.util.logging.Level;
import java.util.logging.Logger;
import static fr.inria.corese.kgram.api.core.ExprType.XT_EDGES;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphSpecificFunction extends LDScript {  
    
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
                return load(proc, param);
                
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
                
            case APPROXIMATE:
                  return proc.approximate(this, env, p, param);
                  
             case APP_SIM:
                  return proc.approximate(this, env, p);
                  
             case STL_INDEX: 
                 return proc.index(env, p);
                
            case DEPTH:
                return proc.depth(env, p, param[0]);
                
            case XT_TUNE:
                return proc.tune(this, env, p, param[0], param[1]);
                
            case XT_EDGES:
                return edge(proc, env, p, param);
                
            case XT_EXISTS:
                return exists(proc, env, p, param);  
                
            case XT_MINUS:
            case XT_JOIN:
            case XT_OPTIONAL:
                return proc.algebra(this, env, p, param[0], param[1]);
                
            case XT_UNION:    
                return proc.union(this, env, p, param[0], param[1]);
                
            case XT_ENTAILMENT:
                return entailment(proc, env, p, param);
                
            case XT_SHAPE_GRAPH:
            case XT_SHAPE_NODE:
                return proc.shape(this, env, p, param);
                
            case KGRAM:
                return proc.sparql(env, p, param);
                
            case XT_TOGRAPH:
                return proc.graph(param[0]);
                
            default: return null;
                
        }
        
    }
    
    IDatatype load(GraphProcessor proc, IDatatype[] param) {
        switch (param.length) {
            case 0: return null;
            case 1:
                return proc.load(param[0], null, null, null);
            default:
                IDatatype dt = param[1];
                if (dt.pointerType() == PointerType.GRAPH) {
                    return proc.load(param[0], dt, getParam(param, 2), getParam(param, 3));
                } else {
                    return proc.load(param[0], null, dt, getParam(param, 2));
                }
        }
    }
    
    IDatatype getParam(IDatatype[] param, int n) {
        if (n < param.length) {
            return  param[n];
        }
        return null;
    }
    
    IDatatype entailment(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0: return proc.entailment(env, p, null);
            default:return proc.entailment(env, p, param[0]);
        }
    }
    
    IDatatype edge(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return proc.edge(env, p, null, null, null);
            case 1:
                return proc.edge(env, p, null, param[0], null);
            case 2:
                return proc.edge(env, p, param[0], param[1], null);
            default:
                return proc.edge(env, p, param[0], param[1], param[2]);
        }
    }
    
    IDatatype exists(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return proc.exists(env, p, null, null, null);
            case 1:
                return proc.exists(env, p, null, param[0], null);
            case 2:
                return proc.exists(env, p, param[0], param[1], null);
            default:
                return proc.exists(env, p, param[0], param[1], param[2]);

        }
    }
  
         
}

