package fr.inria.corese.sparql.triple.function.extension;

import static fr.inria.corese.kgram.api.core.ExprType.XT_HAS;
import static fr.inria.corese.kgram.api.core.ExprType.XT_PATH;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.core.BinaryFunction;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Get extends BinaryFunction {
    public static final IDatatype UNDEF = null; //DatatypeMap.UNBOUND;

    public Get(){}
    
    public Get(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt    = getExp1().eval(eval, b, env, p);
        IDatatype dtind = getExp2().eval(eval, b, env, p);
               
        if (dt == null || dtind == null) {
            return null;
        }
           
        switch (oper()) {
            case XT_HAS: return dt.has(dtind);
            case XT_PATH: return dt.path(dtind);
            default: return get(dt, dtind);
        }
    }
       
    /**
     * Generic get 
     */
    public static IDatatype get(IDatatype dt,  IDatatype ind){  
        if (dt.isList()) {
            return dt.get(ind.intValue());
        }
        if (dt.isMap() || dt.isJSON()) {
            return dt.get(ind);
        }
        if (dt.isPointer()){
            Object res = dt.getPointerObject().getValue(null, ind.intValue());
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
