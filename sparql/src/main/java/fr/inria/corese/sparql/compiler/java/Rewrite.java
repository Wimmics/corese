package fr.inria.corese.sparql.compiler.java;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.function.script.Let;

/**
 * Add return statement if needed
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Rewrite {

    ASTQuery ast;
    JavaCompiler jc;

    Rewrite(ASTQuery ast, JavaCompiler jc) {
        this.ast = ast;
        this.jc = jc;
    }

    /**
     * exp is the body of a function
     */
    Expression process(Expression exp) throws EngineException {
        Expression res = rewrite(exp);                      
        res.compile(ast);
        return res;
    }
    
    Expression rewrite(Expression exp) {
        if (exp.isTerm()) {
            switch (exp.oper()) {
                case ExprType.IF:
                    return rewriteif(exp);
                case ExprType.LET:
                    return let(exp.getLet());
                case ExprType.FOR:
                case ExprType.SEQUENCE:
                case ExprType.RETURN:
                    break;
                    
                default: return doreturn(exp);
            }
        } else {
            // constant value
            return doreturn(exp);
        }
        return exp;
    }
    
    Let let(Let exp) {
        if (exp.getBody().oper() == ExprType.IF) {
            return new Let(exp.getDeclaration(), rewriteif(exp.getBody()), false);
        }
        else if (jc.isReturnable(exp.getBody())) {
            return new Let(exp.getDeclaration(), doreturn(exp.getBody()), false);
        }
        return exp;
    }
    
    Expression doreturn(Expression exp) {
        return Term.function(Processor.RETURN, exp);
    }

    /**
     * Rewrite the body of a if to conform to Java 
     * atom -> return (atom)
     * if (a, b, c) -> if (a, return(b), return(c))
     */
    Expression rewriteif(Expression exp) {
        if (exp.isTerm()) {
            switch (exp.oper()) {
                case ExprType.IF:
                    return ifthenelse(exp);
                case ExprType.LET:
                    return let(exp.getLet());
                default: if (jc.isReturnable(exp)) {
                    return doreturn(exp);
                }
            }
        } else {
            // constant value
            return doreturn(exp);
        }
        return exp;
    }

    Expression ifthenelse(Expression exp) {
        Term term = ast.ifThenElse(exp.getArg(0), rewriteif(exp.getArg(1)), rewriteif(exp.getArg(2)));
        return term;
    }
}
