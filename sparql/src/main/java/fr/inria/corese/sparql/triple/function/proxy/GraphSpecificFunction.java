package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.kgram.api.core.Edge;
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
import static fr.inria.corese.kgram.api.core.ExprType.XT_CREATE;
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
import static fr.inria.corese.kgram.api.core.ExprType.XT_MERGE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_MINDEGREE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_OBJECTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SUBJECTS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SYNTAX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_VALUE;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphSpecificFunction extends LDScript { 
    public static final String JOKER = NSManager.EXT + "_joker";
    
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
                return insert(proc, env, p, param);
                
            case XT_DELETE:
                return delete(proc, env, p, param);    
                
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
                
            case XT_MERGE:
                if (isList(param)) {
                    return (param.length == 2) ? DatatypeMap.merge(param[0], param[1]) :  DatatypeMap.merge(param[0]); 
                }
                else {
                    return proc.merge(this, env, p, param[0], param[1]);
                }
                
            case XT_ENTAILMENT:
                return entailment(proc, b, env, p, param);
                
            case XT_SHAPE_GRAPH:
            case XT_SHAPE_NODE:
                return proc.shape(this, env, p, param);
                
            case KGRAM:
                check(Feature.LDSCRIPT_SPARQL, b, SPARQL_MESS);
                return proc.sparql(env, p, param);
                
            case XT_TOGRAPH:
                return proc.graph(param[0]);
                
            case XT_CREATE:
                return proc.create(param[0]);
                
            default: return null;
                
        }
        
    }
    
    IDatatype insert(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        IDatatype first = param[0];
        Edge e;
        if (first.pointerType() == PointerType.GRAPH) {
            // insert(graph, s, p, o)
            return proc.insert(env, p, param);
        } else if (param.length == 3) {
            // insert(s, p, o)
            e = p.insert(null, first, param[1], param[2]);
        } else {
            // insert(uri, s, p, o)
            e = p.insert(first, param[1], param[2], param[3]);
        }
        return (e == null) ? FALSE : TRUE;
    }

    IDatatype delete(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        IDatatype first = param[0];
        Iterable<Edge> le;

        if (first.pointerType() == PointerType.GRAPH) {
            return proc.delete(env, p, param);
        } else if (param.length == 3) {
            le = p.delete(null, first, param[1], param[2]);
        } else {
            le = p.delete(first, param[1], param[2], param[3]);
        }

        return (le == null || !le.iterator().hasNext()) ? FALSE : TRUE;
    }

   boolean isList(IDatatype[] arr) {
       for (IDatatype dt : arr) {
           if (! dt.isList()) {
               return false;
           }
       }
       return true;
   }
   
   
    public IDatatype io(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param) throws SafetyException, EngineException {
        GraphProcessor proc = eval.getGraphProcessor();
        if (param.length == 0) {
            return null;
        }
        IDatatype dt = param[0];
        String path = dt.getLabel();
        
        switch (oper()) {

            case LOAD:
                check(Feature.READ_WRITE, b, LOAD_MESS);
                return load(p, proc, b, param);

            case WRITE:
                check(Feature.READ_WRITE, b, WRITE_MESS);
                if (dt.getLabel().startsWith("/") && Access.accept(Feature.SUPER_WRITE, b.getAccessLevel())) {
                    proc.superWrite(dt, param[1]);
                }
                else {
                    return proc.write(dt, param[1]);
                }

            case READ:
                check(Feature.READ, b, path, READ_MESS);
                if (isFile(path)) {
                    check(Feature.READ_FILE, b, path, READ_MESS);
                }
                IDatatype res = proc.read(dt);
                return  res;
                
            case XT_HTTP_GET:
                check(Feature.HTTP, b, READ_MESS);
                if (param.length>=2) {
                    return proc.httpget(dt, param[1]);
                }
                return proc.httpget(dt);    
                
            default: return null;
        }
    }

    
    public IDatatype load(Producer p, GraphProcessor proc, Binding b, IDatatype[] param) throws EngineException {
        switch (param.length) {
            case 0: return null;
            case 1:
                return proc.load(p, param[0], null, null, null, b.getAccessLevel());
            default:
                IDatatype dt = param[1];
                if (dt.pointerType() == PointerType.GRAPH) {
                    return proc.load(p, param[0], dt, getParam(param, 2), getParam(param, 3), b.getAccessLevel());
                } else {
                    return proc.load(p, param[0], null, dt, getParam(param, 2), b.getAccessLevel());
                }
        }
    }
    
    IDatatype getParam(IDatatype[] param, int n) {
        if (n < param.length) {
            return  param[n];
        }
        return null;
    }
    
    IDatatype entailment(GraphProcessor proc, Binding b, Environment env, Producer p, IDatatype[] param) throws EngineException {
        check(Feature.LINKED_RULE, b, TermEval.LINKED_RULE_MESS);
        switch (param.length) {
            case 0: return proc.entailment(env, p, null);
            // param is a graph
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
                return edge(env, p, null, null, null, null);
            case 1:
                return edge(env, p, null, clean(param[0]), null, null);
            case 2:
                return edge(env, p, clean(param[0]), clean(param[1]), null, null);
            case 3 :
                return edge(env, p, clean(param[0]), clean(param[1]), clean(param[2]), null);
            default:                
                return edge(env, p, clean(param[0]), clean(param[1]), clean(param[2]), clean(param[3]));
        }
    }
    
    IDatatype edge(Environment env, Producer prod, 
            IDatatype s, IDatatype p, IDatatype o, IDatatype g) {
        return prod.getEdges(prod.getEdges(s, p, o, getNodeList(env, g)));
    }
    
    IDatatype subjects(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return subjects(env, p, null, null, null, null);
            case 1:
                return subjects(env, p, null, clean(param[0]), null, null);
            case 2:
                return subjects(env, p, clean(param[0]), clean(param[1]), null, null);
            case 3 :
                return subjects(env, p, clean(param[0]), clean(param[1]), clean(param[2]), null);
            default:                
                return subjects(env, p, clean(param[0]), clean(param[1]), clean(param[2]), clean(param[3]));
        }
    }
    
    IDatatype subjects(Environment env, Producer prod, IDatatype s, IDatatype p, IDatatype o, IDatatype g) {
        Iterable<Edge> it = prod.getEdges(s, p, o, getNodeList(env, g));
        return getNodes(it, 0);
    }
    
    IDatatype objects(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return objects(env, p, null, null, null, null);
            case 1:
                return objects(env, p, null, clean(param[0]), null, null);
            case 2:
                return objects(env, p, clean(param[0]), clean(param[1]), null, null);
            case 3 :
                return objects(env, p, clean(param[0]), clean(param[1]), clean(param[2]), null);
            default:                
                return objects(env, p, clean(param[0]), clean(param[1]), clean(param[2]), clean(param[3]));
        }
    }
    
    IDatatype objects(Environment env, Producer prod, IDatatype s, IDatatype p, IDatatype o, IDatatype g) {
        Iterable<Edge> it = prod.getEdges(s, p, o, getNodeList(env, g));
        return getNodes(it, 1);
    }
    
    IDatatype exists(GraphProcessor proc, Environment env, Producer p, IDatatype[] param) {
        switch (param.length) {
            case 0:
                return exists(env, p, null, null, null);
            case 1:
                return exists(env, p, null, clean(param[0]), null);
            case 2:
                return exists(env, p, clean(param[0]), clean(param[1]), null);
            default:
                return exists(env, p, clean(param[0]), clean(param[1]), clean(param[2]));
        }
    }
    
    IDatatype exists(Environment env, Producer prod, IDatatype s, IDatatype p, IDatatype o) {
        for (Edge e : prod.getEdges(s, p, o, getNodeList(env.getGraphNode()))) {
            return e==null ? FALSE : TRUE;
        }
        return FALSE;
    }
    
    IDatatype clean(IDatatype dt) {
        if (dt==null) {
            return dt;
        }
        if (dt.getLabel().startsWith(JOKER)) {
            // xt:_joker stands for null joker value 
            return null;
        }
        return dt;
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
        return degree(env, p, node, pred, index);
    }
    
    IDatatype degree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index) {
        int min = Integer.MAX_VALUE;
        List<Node> from = getNodeList(env);
        if (index == null) {
            // input + output edges
            int d = degree(p, from, node, pred, 0, min) + degree(p, from, node, pred, 1, min);
            return DatatypeMap.newInstance(d);
        }
        int d = degree(p, from, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d);
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
        return mindegree(env, p, node, pred, index, min);
    } 
    
    IDatatype mindegree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index, IDatatype dtmin) {
        int min = dtmin.intValue();
        List<Node> from = getNodeList(env);
        if (index == null) {
            // input + output edges
            int d = degree(p, from, node, pred, 0, min) + degree(p, from, node, pred, 1, min);
            return DatatypeMap.newInstance(d >= min);
        }
        int d = degree(p, from, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d >= min);
    }
    
    // node is nth node of edges with pred as predicate
    // return number of edges with such nth node or min if number >= min
    // by default min = Integer.MAX
    int degree(Producer p, List<Node> from, Node node, Node pred, int n, int min) {
        Node sub = (n == 0) ? node : null;
        Node obj = (n == 1) ? node : null;
                
        int count = 0;

        for (Edge edge : p.getEdges(sub, pred, obj, from)) {
            if (edge == null) {
                break;
            }
            if (node.equals(edge.getNode(n).getDatatypeValue())) {
                count++;
                if (count >= min) {
                    break;
                }
            } else {
                break;
            }
        }
        return count;
    }
    
    List<Node> getNodeList(Environment env) {
        return getNodeList(env.getGraphNode());
    }

    List<Node> getNodeList(Node node) {
        if (node == null) {
            return null;
        }
        return List.of(node);
    }
    // graph may be a list
    List<Node> getNodeList(Environment env, IDatatype graph) {
        if (graph == null) {
            return getNodeList(env.getGraphNode());
        }
        return DatatypeMap.toNodeList(graph);
    }
    
    IDatatype getNodes(Iterable<Edge> it, int n) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Edge edge : it) {
            if (edge != null) {
                list.add(edge.getNode(n).getDatatypeValue());
            }
        }
        return DatatypeMap.newList(list);
    }
        
}

