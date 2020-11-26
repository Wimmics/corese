package fr.inria.corese.sparql.triple.function.extension;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListNary extends TermEval {

    public ListNary(){}
    
    public ListNary(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) return null;

        switch (oper()) {
            case ExprType.IOTA: return DatatypeMap.iota(param);
            case ExprType.XT_SET:
                if (param.length == 3) {
                    return param[0].set(param[1], param[2]);
                }
                break;
//            case ExprType.XT_MERGE:
//                return (param.length == 2) ? DatatypeMap.merge(param[0], param[1]) :  DatatypeMap.merge(param[0]);   
            case ExprType.XT_ADD:
                return (param.length == 2) ? DatatypeMap.add(param[0], param[1]) : DatatypeMap.add(param[0], param[1], param[2]);
        }
        return null;
    }
    
}
