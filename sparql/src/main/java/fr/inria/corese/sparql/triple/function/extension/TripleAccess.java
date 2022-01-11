package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExprType.IS_TRIPLE;
import static fr.inria.corese.kgram.api.core.ExprType.OBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.PREDICATE;
import static fr.inria.corese.kgram.api.core.ExprType.SUBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.TRIPLE;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.GraphProcessor;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class TripleAccess extends TermEval {

    public TripleAccess(){}
    
    public TripleAccess(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        switch (oper()) {
            case TRIPLE:
                return triple(eval,  b,  env,  p);
        }
        
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return null;
        }
        
        if (dt.isTriple() || dt.isPointer() && dt.pointerType() == PointerType.TRIPLE) {
            return access(dt);
        }
        
        switch (oper()) {          
            case IS_TRIPLE: return DatatypeMap.FALSE;                                
        }
        return null;
    }
    
    
    IDatatype access(IDatatype dt) {
        if (dt.getPointerObject() == null) {
            return null;
        }
        Edge e = dt.getPointerObject().getEdge();
        switch (oper()) {
            case SUBJECT:   return e.getSubjectValue();
            case PREDICATE: return e.getPredicateValue();
            case OBJECT:    return e.getObjectValue();
            case IS_TRIPLE: return DatatypeMap.TRUE;                                
        }
        return null;
    }
    
    IDatatype triple(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype sub  = getBasicArg(0).eval(eval, b, env, p);
        IDatatype pred = getBasicArg(1).eval(eval, b, env, p);
        IDatatype obj  = getBasicArg(2).eval(eval, b, env, p);
        
        if (sub == null || pred == null || obj == null) {
            return null;
        }
        
        return eval.getGraphProcessor().triple(env, p, sub, pred, obj);       
    }
    
}
