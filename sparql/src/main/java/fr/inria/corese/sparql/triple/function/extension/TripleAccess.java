package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExprType.IS_TRIPLE;
import static fr.inria.corese.kgram.api.core.ExprType.OBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.PREDICATE;
import static fr.inria.corese.kgram.api.core.ExprType.SPARQL_COMPARE;
import static fr.inria.corese.kgram.api.core.ExprType.SUBJECT;
import static fr.inria.corese.kgram.api.core.ExprType.TRIPLE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_ASSERTED;
import static fr.inria.corese.kgram.api.core.ExprType.XT_EDGE;
import static fr.inria.corese.kgram.api.core.ExprType.XT_REFERENCE;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

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
            return access(eval, b, env, p, dt);
        }
        
        switch (oper()) {          
            case IS_TRIPLE: return DatatypeMap.FALSE;                                
        }
        return null;
    }
    
    
    IDatatype access(Computer eval, Binding b, Environment env, Producer p, IDatatype dt) throws EngineException {
        Edge e = dt.getEdge();
        if (e == null) {
            return null;
        }
        switch (oper()) {
            case SUBJECT:       return e.getSubjectValue();
            case PREDICATE:     return e.getPredicateValue();
            case OBJECT:        return e.getObjectValue();
            case XT_REFERENCE:  return e.hasReferenceNode()?e.getReferenceNode().getDatatypeValue():null; 
            case IS_TRIPLE: return DatatypeMap.TRUE; 
            case SPARQL_COMPARE: 
                IDatatype dt2 = getBasicArg(1).eval(eval, b, env, p);
                if (dt2 == null) {
                    return null;
                }
            
                try {
                    return compareValue(dt.compare(dt2));
                } catch (CoreseDatatypeException ex) {
                    return null;
                } 
                
            case XT_EDGE:
                return DatatypeMap.createObject(e);
            case XT_ASSERTED:
                return value(e.isAsserted());
        }
        return null;
    }
    
    IDatatype compareValue(int n) {
        if (n<0)  return DatatypeMap.MINUSONE;
        if (n==0) return DatatypeMap.ZERO;
        if (n>0)  return DatatypeMap.ONE;        
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
