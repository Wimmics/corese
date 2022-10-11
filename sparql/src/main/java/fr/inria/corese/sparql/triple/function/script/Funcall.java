package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.PointerType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.exceptions.UndefinedExpressionException;
import fr.inria.corese.sparql.triple.parser.Access;

/**
 * funcall(fun, exp) apply(fun, list)
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Funcall extends LDScript {

    public Funcall() {}
    
    public Funcall(String name) {
        super(name);
        setArity(1);
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
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

        Function function = getFunction(eval, b, env, p, name, param.length);
        if (function == null) {
            return null;
        }
        return call(eval, b, env, p, function, param);
    }
    
    Function getFunction(Computer eval, Binding b, Environment env, Producer p, IDatatype dt, int n) throws EngineException {
        String name = dt.stringValue();
        Function function = getDefineGenerate(this, env, name, n);

        if (function == null) {
            if (dt.pointerType() == PointerType.EXPRESSION) {
                // lambda expression, arity is not correct                
            } 
            else if (env.getEval() != null) {
                if (accept(Access.Feature.LINKED_FUNCTION, b)) {
                    getLinkedFunction(name, env);
                    function = getDefineGenerate(this, env, name, n);
                }
                if (function == null) {
                    throw new UndefinedExpressionException(UNDEFINED_EXPRESSION_MESS + ": " + toString());
                }
            }
        }
        return function;
    }
    
    void getLinkedFunction(String name, Environment env) throws EngineException {
        try {
            env.getEval().getSPARQLEngine().getLinkedFunction(name);
        } catch (SparqlException ex) {
            throw EngineException.cast(ex);
        }
    }

    public IDatatype call(Computer eval, Binding b, Environment env, Producer p, Function function, IDatatype... param) 
            throws EngineException{
        Expression fun = function.getSignature();
        b.set(function, fun.getExpList(), param);
        IDatatype dt = null;
        if (function.isSystem()) {
           // fr.inria.corese.kgram.core.Eval cc = eval.getComputerEval(env, p, function);
            fr.inria.corese.kgram.core.Eval cc = getComputerEval(eval.getEvaluator(), env, p, function);
            dt = function.getBody().eval(cc.getEvaluator(), b, cc.getEnvironment(), p);
        } else {
            dt = function.getBody().eval(eval, b, env, p);
        }
        b.unset(function, fun.getExpList());
        if (dt == null) {
            return null;
        }
        return b.resultValue(dt);
    }
    
    // restore binding stack in case of exception
    public IDatatype callWE(Computer eval, Binding b, Environment env, Producer p, Function function, IDatatype... param)
            throws EngineException {
        int varSize = b.getVariableSize();
        int levelSize = b.getLevelSize();
        try {
            return call(eval, b, env, p, function, param);
        } catch (EngineException e) {
            b.pop(varSize, levelSize);
            throw e;
        }
    }
}
