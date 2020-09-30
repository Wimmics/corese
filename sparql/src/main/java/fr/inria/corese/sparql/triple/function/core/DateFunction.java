package fr.inria.corese.sparql.triple.function.core;

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
public class DateFunction extends TermEval {

    public DateFunction() {}

    public DateFunction(String name) {
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        IDatatype dt = getBasicArg(0).eval(eval, b, env, p);
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
