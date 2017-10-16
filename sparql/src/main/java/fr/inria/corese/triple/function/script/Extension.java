package fr.inria.corese.triple.function.script;

import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Function;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.corese.triple.function.term.TermEval;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LDScript function call
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Extension extends TermEval {

    private static Logger logger = LogManager.getLogger(Extension.class);
    Function function;
    boolean isUnary = false;
    Expr var;
    Expression exp, body;
    Computer cc;

    public Extension(String name) {
        super(name);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) {
        if (function == null) {
            function = (Function) eval.getDefine(this, env);
            if (function == null) {
                logger.error("Undefined function: " + this);
                return null;
            } else {
                isUnary = arity() == 1 && !function.isSystem();
                if (isUnary) {
                    exp = getArg(0);
                    var = function.getFunction().getExp(0);
                    body = function.getBody();
                }
            }
        }
        IDatatype dt = null;

        if (isUnary) {
            IDatatype value = exp.eval(eval, b, env, p);
            if (value == null) {
                return null;
            }
            b.set(function, var, value);
            dt = body.eval(eval, b, env, p);
            b.unset(function, var, value);

        } else {
            IDatatype[] param = evalArguments(eval, b, env, p, 0);
            if (param == null) {
                return null;
            }
            Expr fun = function.getSignature();
            b.set(function, fun.getExpList(), param);
            if (function.isSystem()) {                
                Computer  cc = eval.getComputer(env, p, function);               
                dt = function.getBody().eval(cc, b, cc.getEnvironment(), p);
            } else {
                dt = function.getBody().eval(eval, b, env, p);
            }
            b.unset(function, fun.getExpList());
        }

        if (dt == null) {
            return null;
        }
        return DatatypeMap.getResultValue(dt);
    }
}
