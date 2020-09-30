package fr.inria.corese.sparql.triple.function.proxy;

import static fr.inria.corese.kgram.api.core.ExprType.APPROXIMATE;
import static fr.inria.corese.kgram.api.core.ExprType.APP_SIM;
import static fr.inria.corese.kgram.api.core.ExprType.DEPTH;
import static fr.inria.corese.kgram.api.core.ExprType.KGRAM;
import static fr.inria.corese.kgram.api.core.ExprType.LOAD;
import static fr.inria.corese.kgram.api.core.ExprType.READ;
import static fr.inria.corese.kgram.api.core.ExprType.WRITE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TUNE;
import static fr.inria.corese.kgram.api.core.ExprType.SIM;
import static fr.inria.corese.kgram.api.core.ExprType.STL_INDEX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_DEGREE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_DELETE;
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
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.GraphProcessor;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SHAPE_GRAPH;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SHAPE_NODE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_TOGRAPH;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.triple.function.script.LDScript;
import static fr.inria.corese.kgram.api.core.ExprType.XT_EDGES;
import static fr.inria.corese.kgram.api.core.ExprType.XT_HTTP_GET;
import static fr.inria.corese.kgram.api.core.ExprType.XT_INSERT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MINDEGREE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_OBJECTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SUBJECTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SYNTAX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_VALUE;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access.Feature;

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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }  

        GraphProcessor proc = eval.getGraphProcessor();
        
        switch (oper()) {
            case LOAD:               
            case WRITE:               
            case READ:
            case XT_HTTP_GET:
                return io(eval, b, env, p, param);
          
            case XT_SYNTAX:
                return proc.syntax(param[0], param[1], (param.length == 3) ? param[2] : null);

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
                return proc.tune(this, env, p, param);
                               
            case XT_VALUE:
                return value(proc, env, p, param);    
                
            case XT_EDGES:
                return edge(proc, env, p, param);
            case XT_SUBJECTS:
                return subjects(proc, env, p, param);
            case XT_OBJECTS:
                return objects(proc, env, p, param);                
            case XT_EXISTS:
                return exists(proc, env, p, param); 
                
            case XT_INSERT:
                return proc.insert(env, p, param);
                
            case XT_DELETE:
                return proc.delete(env, p, param);    
                
            case XT_DEGREE:
                return degree(proc, env, p, param); 
                
            case XT_MINDEGREE:
                return mindegree(proc, env, p, param);  
                
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
                check(Feature.SPARQL, b, SPARQL_MESS);
                return proc.sparql(env, p, param);
                
            case XT_TOGRAPH:
                return proc.graph(param[0]);
                
            default: return null;
                
        }
        
    }
    
   
    
    public IDatatype io(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param) throws SafetyException {
        GraphProcessor proc = eval.getGraphProcessor();
        switch (oper()) {

            case LOAD:
                check(Feature.READ_WRITE, b, LOAD_MESS);
                return load(proc, param);

            case WRITE:
                check(Feature.READ_WRITE, b, WRITE_MESS);
                return proc.write(param[0], param[1]);

            case READ:
                check(Feature.READ_WRITE, b, READ_MESS);
                return proc.read(param[0]);
                
            case XT_HTTP_GET:
                check(Feature.READ_WRITE, b, READ_MESS);
                return proc.httpget(param[0]);    
                
            default: return null;
        }
    }

    
    public IDatatype load(GraphProcessor proc, IDatatype[] param) {
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
    
     /**
     * xt:value([graph], subject, predicate [, index of result node]) 
     */
    IDatatype value(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) { 
        int default_index = 1;
        IDatatype dt = param[0];
        IDatatype g = null;
        if (dt.pointerType() == PointerType.GRAPH) {
            g = dt;
        }
        switch (param.length) {
            // xt:value(s, p) (where s may be a graph, in this case g=null)
            case 2: return proc.value(env, p, null, param[0], param[1], default_index);
            // xt:value(s, p, 1)
            // xt:value(g, s, p)
            case 3: 
                if (g == null) {
                    return proc.value(env, p, g, param[0], param[1], param[2].intValue());
                } else {
                    return proc.value(env, p, g, param[1], param[2], default_index);
                }
            // xt:value(g, s, p, 1)
            case 4: return proc.value(env, p, g, param[1], param[2], param[3].intValue());
            default: return null;
        }        
    }
    
    public IDatatype edge(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return proc.edge(env, p, null, null, null);
            case 1:
                return proc.edge(env, p, null, param[0], null);
            case 2:
                return proc.edge(env, p, param[0], param[1], null);
            case 3 :
                return proc.edge(env, p, param[0], param[1], param[2]);
            default:                
                return proc.edge(env, p, param[0], param[1], param[2], param[3]);
        }
    }
    
    IDatatype subjects(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return proc.subjects(env, p, null, null, null, null);
            case 1:
                return proc.subjects(env, p, null, param[0], null, null);
            case 2:
                return proc.subjects(env, p, param[0], param[1], null, null);
            case 3 :
                return proc.subjects(env, p, param[0], param[1], param[2], null);
            default:                
                return proc.subjects(env, p, param[0], param[1], param[2], param[3]);
        }
    }
    
    IDatatype objects(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return proc.objects(env, p, null, null, null, null);
            case 1:
                return proc.objects(env, p, null, param[0], null, null);
            case 2:
                return proc.objects(env, p, param[0], param[1], null, null);
            case 3 :
                return proc.objects(env, p, param[0], param[1], param[2], null);
            default:                
                return proc.objects(env, p, param[0], param[1], param[2], param[3]);
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
    
    IDatatype degree(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        IDatatype node, pred=null, index=null;
        switch (param.length) {
            case 1: node = param[0]; break;
            case 2: node = param[0]; 
            if (param[1].isNumber()){index = param[1];} else {pred = param[1];};
            break;
            case 3: node = param[0]; pred = param[1]; index = param[2];  break;
            default: return null;
        }
        return proc.degree(env, p, node, pred, index);
    }
  
    IDatatype mindegree(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        IDatatype node, pred=null, index=null, min=null;
        switch (param.length) {
            case 2: node = param[0]; 
            min = param[1]; break;
            case 3: node = param[0]; 
            if (param[1].isNumber()){index = param[1];} else {pred = param[1];};
            min = param[2];
            break;
            case 4: node = param[0]; pred = param[1]; index = param[2];  
            min = param[3];
            break;           
            default: return null;
        }
        return proc.mindegree(env, p, node, pred, index, min);
    }     
}

