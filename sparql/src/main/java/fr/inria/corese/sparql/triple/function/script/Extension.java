package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * LDScript function call
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Extension extends LDScript {

    private static Logger logger = LoggerFactory.getLogger(Extension.class);
    Function function;
    boolean isUnary = false, isBinary = false, isSystem = false;
    private boolean tailRecursion = false;
    Expression var1, var2, exp1, exp2, body;
    Term signature;
    List<Expr> arguments;
    Computer cc;
    boolean visit = true;
    boolean typecheck = Function.typecheck;
    boolean todo = true;
 
    public Extension() {}
    
    public Extension(String name) {
        super(name);
    }

    void init() {
        body = function.getBody();
        isSystem = function.isSystem();
        if (!isSystem) {
            isUnary = arity() == 1;
            isBinary = arity() == 2;
        }
        if (isUnary) {
            var1 = function.getSignature().getArg(0);
            exp1 = getArg(0);
        } else if (isBinary) {
            var1 = function.getSignature().getArg(0);
            var2 = function.getSignature().getArg(1);
            exp1 = getArg(0);
            exp2 = getArg(1);

        } else {
            arguments = function.getSignature().getExpList();
        }
    }


    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        if (todo) {
            synchronized (this) {
                // public functions are shared ...
                if (function == null) {
                    function = getDefine(this, env);
                    if (function == null) {
                        logger.error("Undefined function: " + this.getLabel() + " " + this);
                        return null;
                    } else {
                        init();
                    }
                }
                todo = false;
            }
        }
                
        if (isUnary) {
            IDatatype value1 = exp1.eval(eval, b, env, p);
            if (value1 == null) {
                return null;
            }
            IDatatype dt;
            if (tailRecursion) {
                b.setTailRec(function, var1, value1);
                if (visit) {
                    visit(env);
                }
                dt = body.eval(eval, b, env, p);
            } else {
                b.set(function, var1, value1);
                if (visit) {
                    visit(env);
                }               
                dt = body.eval(eval, b, env, p);
                b.unset(function);
            }
            if (typecheck) {
                check(eval, b, env, p, dt, value1);
            }
            if (dt == null) {
                return null;
            }          
            return b.resultValue(dt);
        } else if (isBinary) {
            IDatatype value1 = exp1.eval(eval, b, env, p);
            IDatatype value2 = exp2.eval(eval, b, env, p);
            if (value1 == null || value2 == null) {
                return null;
            }
            b.set(function, var1, value1, var2, value2);
            if (visit) {
                visit(env);
            }               
            IDatatype dt = body.eval(eval, b, env, p);
            b.unset(function);
            if (typecheck) {
                check(eval, b, env, p, dt, value1, value2);
            }
            if (dt == null) {
                return null;
            }
            return b.resultValue(dt);
        } else {
            IDatatype[] param = evalArguments(eval, b, env, p, 0);
            if (param == null) {
                return null;
            }
            b.set(function, arguments, param);
            if (visit) {
                visit(env);
            }
            IDatatype dt = null;
            if (isSystem) {
                //fr.inria.corese.kgram.core.Eval cc = eval.getComputerEval(env, p, function);
                fr.inria.corese.kgram.core.Eval cc = getComputerEval(eval.getEvaluator(), env, p, function);
                // PRAGMA: b = cc.getEnvironment().getBind()
                dt = body.eval(cc.getEvaluator(), b, cc.getEnvironment(), p);
            } else {
                dt = body.eval(eval, b, env, p);
            }
            b.unset(function, arguments);
            if (typecheck) {
                check(eval, b, env, p, dt, param);
            }
            if (dt == null) {
                return null;
            }            
            return b.resultValue(dt);
        }
    }
        
    void check(Computer eval, Binding b, Environment env, Producer p, IDatatype dt, IDatatype... param) {
        function.check(eval, b, env, p, param, dt);
    }
    
    void visit(Environment env) {
        if (env.getEval() != null) {
            env.getEval().getVisitor().function(env.getEval(), this, function.getSignature());
        }
    }

    /**
     * Eval with param already computed Use case: xt:main(), xt:produce(?q)
     *
     * @param eval
     * @param b
     * @param env
     * @param p
     * @param param
     * @return
     */
    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p, IDatatype[] param) throws EngineException {
        if (function == null) {
            function = getDefine(this, env);
            if (function == null) {
                logger.error("Undefined function: " + this);
                return null;
            }
        }

        Expression fun = function.getSignature();
        IDatatype dt;
        b.set(function, fun.getExpList(), param);
        if (function.isSystem()) {
            //fr.inria.corese.kgram.core.Eval cc = eval.getComputerEval(env, p, function);
            fr.inria.corese.kgram.core.Eval cc = getComputerEval(eval.getEvaluator(), env, p, function);
            // PRAGMA: b = cc.getEnvironment().getBind()
            dt = function.getBody().eval(cc.getEvaluator(), b, cc.getEnvironment(), p);
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

    /**
     * @return the tailRecursion
     */
    public boolean isTailRecursion() {
        return tailRecursion;
    }

    /**
     * @param tailRecursion the tailRecursion to set
     */
    public void setTailRecursion(boolean tailRecursion) {
        this.tailRecursion = tailRecursion;
    }
    
    @Override
    public void tailRecursion(Function fun){
        if (getName().equals(fun.getSignature().getName())
                && getArity() == fun.getSignature().getArity()){
            setTailRecursion(true);
        }
    }
}
