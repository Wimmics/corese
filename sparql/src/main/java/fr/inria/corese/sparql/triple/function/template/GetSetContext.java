package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.Context;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class GetSetContext extends TemplateFunction {  
        
    public GetSetContext(String name){
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
            
            case ExprType.STL_GET:
                switch (param.length) {
                    case 1:
                        return c.get(param[0]);
                    case 2:
                        IDatatype dt = c.get(param[0]);
                        if (dt == null) {
                            return FALSE;
                        }
                        boolean res = dt.equals(param[1]);
                        return (res) ? TRUE : FALSE;
                    default:
                        return null;
                }
                
            case ExprType.STL_SET:
                if (param.length == 2) {
                    c.set(param[0], param[1]);
                    return param[1];
                }
                else {
                    c.set(param[0], (IDatatype)null);
                    return TRUE;
                }
                                
             case ExprType.STL_EXPORT:
                c.export(param[0].getLabel(), param[1]);
                return param[1];    
                
            default:
                return null;
        }
    }
    
   
}

