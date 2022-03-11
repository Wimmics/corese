package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import static fr.inria.corese.kgram.api.core.ExprType.XT_LABEL;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class UnaryExtension extends TermEval {

    public UnaryExtension() {}
    
    public UnaryExtension(String name) {
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }

        switch (oper()) {
            case ExprType.INDEX:
                return index(dt, p);
                
            case ExprType.XT_CONTENT:
                return content(dt);
                
            case ExprType.XT_DATATYPE_VALUE:
                return dt.getObjectDatatypeValue();
                
            case ExprType.XT_LOWERCASE:
                return DatatypeMap.newInstance(dt.stringValue().toLowerCase());
                
            case ExprType.XT_UPPERCASE:
                return DatatypeMap.newInstance(dt.stringValue().toUpperCase());
                
            case XT_LABEL: return DatatypeMap.newInstance(dt.getLabel());
                
                
        }

        return null;

    }

    IDatatype index(IDatatype dt, Producer p) {
        Node n = p.getNode(dt);
        if (n == null) {
            return null;
        }
        return value(n.getIndex());
    }
    
    IDatatype content(IDatatype dt){
        if (dt.getNodeObject() != null){
            return DatatypeMap.newInstance(dt.getContent());
        }
        return dt;
    }
}
