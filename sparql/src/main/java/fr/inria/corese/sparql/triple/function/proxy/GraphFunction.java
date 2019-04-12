package fr.inria.corese.sparql.triple.function.proxy;

import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExprType.XT_GRAPH;
import static fr.inria.corese.kgram.api.core.ExprType.XT_INDEX;
import static fr.inria.corese.kgram.api.core.ExprType.XT_NODE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_OBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_PROPERTY;
import static fr.inria.corese.kgram.api.core.ExprType.XT_SUBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.XT_VALUE;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
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
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) {
            return null;
        }

        switch (oper()) {

            case XT_VALUE:
                // xt:value(subject, predicate) : value of object
                int index = (param.length == 3) ? param[2].intValue() : 1;
                Node node = p.getGraph().value(param[0], param[1], index);
                if (node == null) {
                    return null;
                }
                return (IDatatype) node.getDatatypeValue();

            case XT_GRAPH:
                if (param.length == 0) {
                    return DatatypeMap.createObject(p.getGraph());
                }
                else {
                    return access(param[0], p);
                }
                
            case XT_NODE:
                //Node n = p.getNode(param[0]);
                Node n = p.getGraph().getVertex(param[0]);
                if (n == null) {
                    return null;
                }
                return DatatypeMap.createObjectBasic(null, n);
                
                
            default:
                switch (param.length) {
                    case 1: return access(param[0], p);
                    default: return null;
                }
                

        }
    }
    
    IDatatype access(IDatatype dt, Producer p) {
        if (dt.pointerType() != PointerType.TRIPLE) {
            switch (oper()) {
                case XT_INDEX: return index(dt, p);
                default: return null;
            }
        }
        Edge edge = dt.getPointerObject().getEdge();
        switch (oper()) {
            case XT_GRAPH:
                return (IDatatype) edge.getGraph().getDatatypeValue();

            case XT_SUBJECT:
                return (IDatatype) edge.getNode(0).getDatatypeValue();

            case XT_OBJECT:
                return (IDatatype) edge.getNode(1).getDatatypeValue();

            case XT_PROPERTY:
                return (IDatatype) edge.getEdgeNode().getDatatypeValue();

            case XT_INDEX:
                return DatatypeMap.newInstance(edge.getIndex());
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
