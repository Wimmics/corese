package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.term.TermEval;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class LDScript extends TermEval {
    
    public LDScript() {}
    
    public LDScript(String name) {
        super(name);
    }
    
    LDScript(String name, Expression e1, Expression e2) {
        super(name, e1, e2);
    }
    
    @Override
    public boolean isLDScript() {
        return true;
    }
    
    @Override
    public Expression prepare(ASTQuery ast) throws EngineException {
        ast.setLDScript(true);
        ast.getGlobalAST().setLDScript(true);
        return super.prepare(ast);
    }
    
    
    public boolean isDefined(Expr exp) {
        return getExtension().isDefined(exp);
    }

    public Function getDefine(Expr exp, Environment env) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function def = ext.get(exp);
            if (def != null) {
                return def;
            }
        }

        return getExtension().get(exp);
    }

    public Function getDefine(String name) {
        return getExtension().get(name);
    }

    public Function getDefineGenerate(Expr exp, Environment env, String name, int n)
            throws EngineException {
        Function fun = getDefine(env, name, n);
        if (fun == null) {
            fun = getDefine(exp, env, name, n);
        }
        return fun;
    }

    /**
     * exp = funcall(arg, arg) arg evaluates to name Generate extension function
     * for predefined function name rq:plus -> function rq:plus(x, y){
     * rq:plus(x, y) }
     */
    Function getDefine(Expr exp, Environment env, String name, int n) throws EngineException {
        if (Processor.getOper(name) == ExprType.UNDEF) {
            return null;
        }
        Query q = env.getQuery().getGlobalQuery();
        ASTQuery ast = getAST((Expression) exp, q);
        Function fun = ast.defExtension(name, name, n);
        q.defineFunction(fun);
        ASTExtension ext = q.getCreateExtension();
        ext.define(fun);
        return fun;
    }

    // use exp AST to compile exp
    // use case: uri() uses ast base
    ASTQuery getAST(Expression exp, Query q) {
        ASTQuery ast = exp.getAST();
        if (ast != null) {
            return ast.getGlobalAST();
        } else {
            return q.getAST();
        }
    }

    public Function getDefine(Environment env, String name, int n) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function ee = ext.get(name, n);
            if (ee != null) {
                return ee;
            }
        }
        return getExtension().get(name, n);
    }

    public Function getDefineMetadata(Environment env, String metadata, int n) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            Function ee = ext.getMetadata(metadata, n);
            if (ee != null) {
                return ee;
            }
        }
        return getExtension().getMetadata(metadata, n);
    }

    /**
     * Retrieve a method with name and type
     */
    public Function getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param) {
        ASTExtension ext = env.getExtension();
        if (ext != null) {
            if (env.getQuery().isDebug()) {
                ext.setDebug(true);
            }
            Function ee = ext.getMethod(name, type, param);
            if (ee != null) {
                return ee;
            }
        }
        return getExtension().getMethod(name, type, param);
    }

    public void define(Function exp) {
        getExtension().define(exp);
    }

    public ASTExtension getExtension() {
        return ASTExtension.getSingleton();
    }
    
     /**
     * context: ldscript function call require new Interpreter with new
     * Environment see sparql.triple.function.script.Extension use case:
     * function contains a sparql query such as let(select where) the function
     * must be executed with a fresh Environment initialized with the function
     * definition global query hint: the function has been compiled within a
     * public query q1 which may be different from the query q2 where public
     * function call occur
     */
    fr.inria.corese.kgram.core.Eval getComputerEval(Evaluator evaluator, Environment env, Producer p, Expr function) {
        Query q = getQuery(env, function);
        fr.inria.corese.kgram.core.Eval currentEval = env.getEval();
        fr.inria.corese.kgram.core.Eval eval = new fr.inria.corese.kgram.core.Eval(p, evaluator, currentEval.getMatcher());
        eval.setSPARQLEngine(currentEval.getSPARQLEngine());
        eval.set(currentEval.getProvider());
        eval.init(q);
        eval.setVisitor(currentEval.getVisitor());
        eval.getMemory().setBind(env.getBind());
        eval.getMemory().setGraphNode(env.getGraphNode());
        return eval;
    }

    Query getQuery(Environment env, Expr function) {
        if (function.isPublic() && env.getQuery() != function.getPattern()) {
            // function is public and contains query or exists
            // use function definition global query 
            return (Query) function.getPattern();
        }
        return env.getQuery();
    }
}
