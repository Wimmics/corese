package fr.inria.corese.triple.function.core;

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
public class DateFunction extends TermEval {

    public DateFunction(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
        if (dt == null || !dt.isDate()) {
            return null;
        }
        switch (oper()) {
            case ExprType.YEAR:
                return DatatypeMap.getYear(dt);
            case ExprType.MONTH:
                return DatatypeMap.getMonth(dt);
            case ExprType.DAY:
                return DatatypeMap.getDay(dt);
            case ExprType.HOURS:
                return DatatypeMap.getHour(dt);
            case ExprType.MINUTES:
                return DatatypeMap.getMinute(dt);
            case ExprType.SECONDS:
                return DatatypeMap.getSecond(dt);
            case ExprType.TIMEZONE:
                return DatatypeMap.getTimezone(dt); 
            case ExprType.TZ:
                return DatatypeMap.getTZ(dt);     
        }

        return null;
    }
}
