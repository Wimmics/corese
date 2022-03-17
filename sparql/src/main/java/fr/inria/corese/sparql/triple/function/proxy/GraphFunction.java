package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExprType.XT_GRAPH;
import static fr.inria.corese.kgram.api.core.ExprType.XT_INDEX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_OBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_PROPERTY;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SUBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_VERTEX;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.script.LDScript;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GraphFunction extends LDScript {

    public GraphFunction(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }

        switch (oper()) {
           
            case XT_GRAPH:
                if (param.length == 0) {
                    return DatatypeMap.createObject(p.getGraph());
                }
                else {
                    return access(param[0], eval, p);
                }
                
            case XT_NODE:
                Node n = p.getGraph().getNode(param[0]);
                if (n == null) {
                    return null;
                }
                return DatatypeMap.createObjectBasic(null, n);
                
            case XT_VERTEX:
                Node v = p.getGraph().getVertex(param[0]);
                if (v == null) {
                    return null;
                }
                return DatatypeMap.createObjectBasic(null, v);    
                
                
            default:
                switch (param.length) {
                    case 1: return access(param[0], eval, p);
                    default: return null;
                }
                

        }
    }
       
    IDatatype access(IDatatype dt, Computer eval, Producer p) {
        if (dt.pointerType() != PointerType.TRIPLE && ! dt.isTripleWithEdge()) {
            switch (oper()) {
                case XT_INDEX: return index(dt, p);
                case XT_GRAPH:
                    // xt:graph(dt:mappings map) -> return the graph of the Mappings if any, null otherwise
                    // there is also a cast dt:graph(dt:mappings map) in class Cast
                    if (dt.pointerType() == PointerType.MAPPINGS) {
                        Mappings map = dt.getPointerObject().getMappings();
                        if (map.getGraph() == null) {
                            return null;
                        }
                        return DatatypeMap.createObject(map.getGraph());
                    }
                default: return null;
            }
        }
        
        Edge edge = dt.getEdge();            
        if (edge == null) {
            return  null;
        }
        switch (oper()) {
            case XT_GRAPH:
                return  edge.getGraphValue();

            case XT_SUBJECT:
                return  edge.getSubjectValue();

            case XT_OBJECT:
                return  edge.getObjectValue();

            case XT_PROPERTY:
                return  edge.getPropertyValue();

            case XT_INDEX:
                return DatatypeMap.newInstance(edge.getEdgeIndex());
        }
        return null;
    }
    
    IDatatype index(IDatatype dt, Producer p) {
        Node n = getNode(dt, p);
        if (n == null) {
            return null;
        }
        return value(n.getIndex());
    }

}
