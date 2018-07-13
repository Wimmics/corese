package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Pointerable;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.List;

/**
 * funcall(fun, exp) apply(fun, list)
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Funcall extends TermEval {

    public Funcall() {}
    
    public Funcall(String name) {
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        IDatatype name = getBasicArg(0).eval(eval, b, env, p);
        IDatatype[] param = evalArguments(eval, b, env, p, 1);
        if (name == null || param == null) {
            return null;
        }

        if (oper() == ExprType.APPLY) {
            // apply(fun, list)
            if (param.length == 0) {
                return null;
            }
            param = DatatypeMap.toArray(param[0]);
        }

        Function function = getFunction(eval, env, name, param.length);
        if (function == null) {
            return null;
        }
        return call(eval, b, env, p, function, param);
    }
    
    Function getFunction(Computer eval, Environment env, IDatatype dt, int n) {
        String name = dt.stringValue();
        Function function = (Function) eval.getDefineGenerate(this, env, name, n);
        if (function == null) {
            if (dt.pointerType() == Pointerable.EXPRESSION_POINTER) {
                // lambda expression, arity is not correct                
            } else {
                eval.getEval().getSPARQLEngine().getLinkedFunction(name);
                function = (Function) eval.getDefineGenerate(this, env, name, n);

                if (function == null) {
                    logger.error("Undefined function: " + name);
                }
            }
        }
        return function;
    }

    public IDatatype call(Computer eval, Binding b, Environment env, Producer p, Function function, IDatatype... param) {
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
        return b.resultValue(dt);
    }
}
