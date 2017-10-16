package fr.inria.corese.triple.function.term;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class In extends TermEval {

    public In(String name) {
        super(name);
    }

    public In(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype dt = getArg(0).eval(eval, b, env, p);
        if (dt == null) {
            return dt;
        }
        boolean error = false;
        for (Expression exp : getArg(1).getArgs()) {
            IDatatype val = exp.eval(eval, b, env, p);
            if (val == null) {
                error = true;
            } else {
                Boolean res = in(dt, val);
                if (res == null) {
                    error = true;
                } else if (res) {
                    return TRUE;
                }
            }
        }
        if (error) {
            return null;
        }
        return FALSE;
    }

    Boolean in(IDatatype dt1, IDatatype dt2) {
        boolean error = false;
        if (dt2.isList()) {
            for (IDatatype dt : dt2.getValues()) {
                try {
                    if (dt1.equalsWE(dt)) {
                        return true;
                    }
                } catch (CoreseDatatypeException e) {
                    error = true;
                }
            }
            if (error) {
                return null;
            }
            return false;
        } else {
            try {
                if (dt1.equalsWE(dt2)) {
                    return true;
                }
            } catch (CoreseDatatypeException e) {
                return null;
            }
        }

        return false;
    }
}
