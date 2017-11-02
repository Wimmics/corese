package fr.inria.corese.triple.function.extension;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ListNary extends TermEval {

    public ListNary(String name){
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype[] param = evalArguments(eval, b, env, p, 0);
        if (param == null) return null;

        switch (oper()) {
            case ExprType.IOTA: return DatatypeMap.iota(param);
            case ExprType.XT_SET:
                if (param.length == 3) {
                    return DatatypeMap.set(param[0], param[1], param[2]);
                }
                break;
            case ExprType.XT_MERGE:
                return (param.length == 2) ? DatatypeMap.merge(param[0], param[1]) :  DatatypeMap.merge(param[0]);   
            case ExprType.XT_ADD:
                return (param.length == 2) ? DatatypeMap.add(param[0], param[1]) : DatatypeMap.add(param[0], param[1], param[2]);
        }
        return null;
    }
    
}
