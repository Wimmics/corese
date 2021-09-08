package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import java.util.List;

/**
 *
 * @author Olivier Corby
 */
public class Filter extends Exp {

    Expression exp;

    public Filter(Expression e) {
        exp = e;
    }

    public static Filter create(Expression exp) {
        return new Filter(exp);
    }

    @Override
    public boolean isFilter() {
        return true;
    }

    @Override
    public Expression getFilter() {
        return exp;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        return ftoSparql(getFilter(), sb);
    }

    ASTBuffer ftoSparql(Expression exp, ASTBuffer sb) {
        if (exp == null) {
            return sb;
        }
        boolean isAtom = (exp.isAtom());
        sb.append(KeywordPP.FILTER + KeywordPP.SPACE);
        if (isAtom) {
            sb.append("(");
        }
        exp.toString(sb);
        if (isAtom) {
            sb.append(")");
        }
        sb.append(KeywordPP.SPACE);
        return sb;
    }

    @Override
    void getVariables(VariableScope scope, List<Variable> list) {
        if (scope.isFilter()) {
            getFilter().getVariables(scope, list);
        }
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        return getFilter().validate(ast);
    }

    @Override
    Bind validate(Bind global, int n) {
        Bind env = new Bind();
        return getFilter().validate(env);
    }

    @Override
    public Filter copy() {
        Expression e = getFilter().copy();
        return create(e);
    }

    @Override
    void visit(ExpressionVisitor v) {
        getFilter().visit(v);
    }
    
    @Override
    public void walk(Walker walker) {
        //System.out.println("walk filter: " + this);
        walker.enter(this);
        getFilter().walk(walker);
        walker.leave(this);
    }

}
