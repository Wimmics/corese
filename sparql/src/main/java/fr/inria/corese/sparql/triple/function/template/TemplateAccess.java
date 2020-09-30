package fr.inria.corese.sparql.triple.function.template;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.api.TransformProcessor;
import fr.inria.corese.sparql.datatype.DatatypeMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TemplateAccess extends TemplateFunction {  
    static final IDatatype EMPTY = DatatypeMap.newStringBuilder("");
       
    public TemplateAccess(String name){
        super(name);
    }
    
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null){
            return null;
        }
        
        TransformProcessor trans = eval.getTransformer(env, p);
        
        switch (oper()) {
            case ExprType.STL_NL:
                switch (param.length) {
                    case 0:
                        return trans.tabulate();
                    case 1:
                        trans.setLevel(trans.getLevel() + param[0].intValue());
                        return trans.tabulate();
                }

            case ExprType.INDENT:
                switch (param.length) {
                    case 1: trans.setLevel(trans.getLevel() + param[0].intValue());
                    return EMPTY;
                }
                
            case ExprType.STL_ISSTART:
                return (trans.isStart()) ? TRUE : FALSE;
                

            default:
                return null;
        }
    }
    
   
}

