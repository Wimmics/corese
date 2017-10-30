package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 * funcall(fun, exp) apply(fun, list)
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Funcall extends TermEval {

    public Funcall(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype name = getArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }

        if (oper() == ExprType.APPLY) {
            // apply(fun, list)
            if (param.length == 0) {
                return null;
            }
            param = (IDatatype[]) param[0].getValueList().toArray();
        }

        Function function = (Function) eval.getDefineGenerate(this, env, name.stringValue(), param.length);
        if (function == null) {
            return null;
        }
        return call(eval, b, env, p, function, param);
    }

    IDatatype call(Computer eval, Binding b, Environment env, Producer p, Function function, IDatatype[] param) {
        Expression fun = function.getSignature();
        b.set(function, fun.getExpList(), param);
        IDatatype dt = null;
        if (function.isSystem()) {
            Computer cc = eval.getComputer(env, p, function);
            dt = function.getBody().eval(cc, b, cc.getEnvironment(), p);
        } else {
            dt = function.getBody().eval(eval, b, env, p);
        }
        b.unset(function, fun.getExpList());
        if (dt == null) {
            return null;
        }
        //return DatatypeMap.getResultValue(dt);
        return b.resultValue(dt);
    }
}
