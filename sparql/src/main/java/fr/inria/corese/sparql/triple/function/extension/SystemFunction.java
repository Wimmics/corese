package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class SystemFunction extends TermEval {

     public SystemFunction(){}
     
     public SystemFunction(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {       
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) return null;
        
        switch (oper()) {
            case ExprType.DEBUG: return debug(param[0], eval, b, env, p);
            
            case ExprType.SLICE: return slice(param[0], env);    
            
            case ExprType.ENV: 
                return DatatypeMap.createObject(env);
                
            case ExprType.XT_STACK: 
                return DatatypeMap.createObject(b);    
                
            case ExprType.XT_RESULT: 
                return DatatypeMap.createObject(env.getMapping()); 
                
            case ExprType.XT_VISITOR:
                if (env.getEval() == null) {
                    return null;
                }
                return DatatypeMap.createObject(env.getEval().getVisitor());
                
                // PRAGMA: define extension datatype inside visitor context: @event select where
                // for overloading operators for the extension datatype
                // to be used in @init function
            case ExprType.XT_DATATYPE:
                if (param.length < 2) {return null;}
                return  env.getVisitor().datatype(param[0], param[1]);
                
            case ExprType.XT_GET_DATATYPE_VALUE:
                // system function for try catch, see TryCatch
                // the catch store the exception object in the Binding
                // xt:getDatatypeValue() return the exception object stored in the Binding
                return b.getDatatypeValue();
        }
        
        return TRUE;
    }
    
    IDatatype debug(IDatatype dt, Computer eval, Binding b, Environment env, Producer p) {
        b.setDebug(dt.booleanValue());
        return TRUE;
    }
    
    IDatatype slice (IDatatype dt, Environment env){
        env.getQuery().setSlice(dt.intValue());
        return TRUE;
    }
    
}
