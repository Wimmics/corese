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
public class Get extends TermEval {
    public static final IDatatype UNDEF = DatatypeMap.UNBOUND;

    public Get(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt    = getArg(0).eval(eval, b, env, p);
        IDatatype dtind = getArg(1).eval(eval, b, env, p);
               
        if (dt == null || dtind == null) {
            return null;
        }
        
        if (dt.isList()) {
            return dt.get(dtind.intValue());
        }
        return gget(dt, null, dtind);
    }
    
       
    /**
     * Generic get with variable name and index
     * may be unbound, return specific UNDEF value because null would be considered an error  
     * embedding let will let the variable unbound, see getConstantValue()
     * it can be catched with bound(var) or coalesce(var)
     */
    IDatatype gget(IDatatype dt, IDatatype var, IDatatype ind){       
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
