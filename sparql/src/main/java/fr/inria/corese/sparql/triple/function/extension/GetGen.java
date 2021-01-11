package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GetGen extends TermEval {
    public static final IDatatype UNDEF = DatatypeMap.UNBOUND;

    public GetGen(){}
    
    public GetGen(String name){
        super(name);
        setArity(2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt    = getBasicArg(0).eval(eval, b, env, p);
        IDatatype dtind = getBasicArg(1).eval(eval, b, env, p);
        IDatatype dtvar = null;
        if (arity() == 3){
           dtvar = dtind;
           dtind = getBasicArg(2).eval(eval, b, env, p); 
        }
        
        if (dt == null || dtind == null) {
            return null;
        }
          
        switch (oper()) {
            case ExprType.XT_GEN_GET:
                return gget(dt, dtvar, dtind);
                
            case ExprType.XT_LAST:
                return last(dt, dtind);
        }
        
        return null;
    }
    
       
    /**
     * Generic get with variable name and index
     * may be unbound, return specific UNDEF value because null would be considered an error  
     * embedding let will let the variable unbound, see getConstantValue()
     * it can be catched with bound(var) or coalesce(var)
     */
    public static IDatatype gget(IDatatype dt, IDatatype var){
        return gget(dt, var, DatatypeMap.ZERO);
    }
    
    static IDatatype last(IDatatype dt, IDatatype ind) {
        if (! dt.isList()) {
            dt = dt.toList();
        }        
        return getResult(DatatypeMap.last(dt, ind));   
    }
        
    public static IDatatype gget(IDatatype dt, IDatatype var, IDatatype ind){
        if (dt.isList()) {
            return getResult(DatatypeMap.get(dt, ind));           
        }
        if (dt.isMap() || dt.isJSON() || dt.isXML()) {
            return getResult(DatatypeMap.get(dt.toList(), ind));    
        }
        if (dt.isPointer()){
            Object res = dt.getValue((var == null) ? null : var.getLabel(), ind.intValue());
            if (res == null) {                
                return UNDEF;
            }                 
            return  DatatypeMap.getValue(res);           
        } 
        return getResult(dt.get(ind.intValue()));
    }
       
    static IDatatype getResult(IDatatype dt){
        if (dt == null){
            return UNDEF;
        }
        return dt;
    }
    
    
}
