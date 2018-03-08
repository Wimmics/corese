package fr.inria.corese.sparql.compiler.java;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.kgram.api.core.ExprType;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Rewrite {

    ASTQuery ast;

    Rewrite(ASTQuery ast) {
        this.ast = ast;
    }

    Expression process(Expression exp) {
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
                case ExprType.FOR:
                case ExprType.SEQUENCE:
                case ExprType.RETURN:
                    break;
                    
                default: return Term.function(Processor.RETURN, exp);
            }
        } else {
            return Term.function(Processor.RETURN, exp);
        }
        return exp;
    }

    /**
     * Rewrite the body of a function to conform to Java atom -> return (atom)
     * if (a, b, c) -> if (a, return(b), return(c))
     */
    Expression rewriteif(Expression exp) {
        if (exp.isTerm()) {
            switch (exp.oper()) {
                case ExprType.IF:
                    return ifthenelse(exp);
            }
        } else {
            return Term.function(Processor.RETURN, exp);
        }
        return exp;
    }

    Expression ifthenelse(Expression exp) {
        Term term = ast.ifThenElse(exp.getArg(0), rewriteif(exp.getArg(1)), rewriteif(exp.getArg(2)));
        return term;
    }
}
