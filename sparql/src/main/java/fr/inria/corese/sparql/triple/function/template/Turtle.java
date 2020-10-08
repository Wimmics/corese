package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Turtle extends TemplateFunction {  
        
    public Turtle(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        switch (oper()) {
            case ExprType.TURTLE:
                switch (param.length) {
                    case 1:
                        return eval.getNSM(b, env, p).turtle(param[0]);
                    case 2:
                        return eval.getNSM(b, env, p).turtle(param[0], param[1].equals(TRUE));                       
                    case 3:
                        return eval.getNSM(b, env, p).turtle(param[0], param[1].equals(TRUE), param[2].equals(TRUE));    
                    default:
                        return null;
                }
                
            case ExprType.XSDLITERAL:
                return DatatypeMap.newStringBuilder(param[0].toSparql(true, true));
                
            default: return null;
        }
    }
   
}

