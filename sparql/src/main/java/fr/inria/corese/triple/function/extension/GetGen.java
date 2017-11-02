package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class GetGen extends TermEval {
    public static final IDatatype UNDEF = DatatypeMap.UNBOUND;

    public GetGen(String name){
        super(name);
        setArity(2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
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
              
        return gget(dt, dtvar, dtind);
    }
    
       
    /**
     * Generic get with variable name and index
     * may be unbound, return specific UNDEF value because null would be considered an error  
     * embedding let will let the variable unbound, see getConstantValue()
     * it can be catched with bound(var) or coalesce(var)
     */
    IDatatype gget(IDatatype dt, IDatatype var, IDatatype ind){
        if (dt.isList()) {
            return getResult(DatatypeMap.get(dt, ind));           
        }
        if (dt.isPointer()){
            Object res = dt.getPointerObject().getValue((var == null) ? null : var.getLabel(), ind.intValue());
            if (res == null) {                
                return UNDEF;
            }                 
            return  DatatypeMap.getValue(res);           
        } 
        return getResult(dt.get(ind.intValue()));
    }
    
    IDatatype getResult(IDatatype dt){
        if (dt == null){
            return UNDEF;
        }
        return dt;
    }
    
    
}
