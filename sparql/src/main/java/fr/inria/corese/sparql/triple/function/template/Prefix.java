package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class Prefix extends TemplateFunction {  
        
    public Prefix(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        NSManager nsm = eval.getNSM(b, env, p);
        switch (oper()) {
            case ExprType.STL_PREFIX:
                return DatatypeMap.createObject(nsm);

            case ExprType.PROLOG:
                IDatatype[] param = evalArguments(eval, b, env, p, 0);
                if (param == null) {
                    return null;
                }
                String pref = nsm.toString((param.length==0)?null:param[0].getLabel(), false, false);
                return DatatypeMap.newInstance(pref);
                
            default: return null;
        }
    }
   
}

