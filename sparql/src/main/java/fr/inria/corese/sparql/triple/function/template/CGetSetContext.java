package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Context;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class CGetSetContext extends TemplateFunction {  
        
    public CGetSetContext(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        Context c = eval.getContext(b, env, p);
        
        switch (oper()) {
            
            case ExprType.STL_CGET:
                switch (param.length) {
                    case 1:
                        return c.getContext(param[0]).getDatatypeValue();
                    case 2:
                        return c.cget(param[0], param[1]);
                    case 3:
                        IDatatype dt = c.cget(param[0], param[1]);
                        if (dt == null) {
                            return FALSE;
                        }
                        boolean res = dt.equals(param[2]);
                        return (res) ? TRUE : FALSE;
                    default:
                        return null;
                }
              
                // named context
                // st:cset(us:name, us:key, us:value)
            case ExprType.STL_CSET:
                switch(param.length) {
                    // st:cset(us:name) with one argument -> remove us:name
                    case 1: c.cremove(param[0]);
                        return DatatypeMap.TRUE;
                    case 3:
                        return c.cset(param[0], param[1], param[2]);
                }
                                                                     
            default:
                return null;
        }
    }
    
   
}

