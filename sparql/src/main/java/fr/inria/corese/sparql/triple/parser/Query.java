package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ExpressionVisitor;
import fr.inria.corese.sparql.triple.api.Walker;
import java.util.List;

public class Query extends Exp {

    ASTQuery ast;

    Query() {
    }

    Query(ASTQuery a) {
        ast = a;
        if (ast.getBody() != null) {
            add(ast.getBody());
        }
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        //sb.append(ast.toString());
        ASTPrinter pr = new ASTPrinter(ast, sb);
        pr.setPrefix(false);
        pr.process();
        return sb;
    }

    public static Query create(ASTQuery a) {
        return new Query(a);
    }

//    @Override
//    public ASTQuery getQuery() {
//        return ast;
//    }

    @Override
    // TODO: complete this
    public Query copy() {
        return this;
    }

    void basicVariables(List<Variable> list) {
        for (Variable var : ast.getSelect()) {
            add(var, list);
        }
    }
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        //System.out.println("query: "+ ast.getSelectVariables());
        switch (sort.getScope()) {
            case INSCOPE:
            case SUBSCOPE:  basicVariables(list); break;
            case ALLSCOPE:  basicVariables(list); ast.getBody().getVariables(sort, list); break;
        }
    }

    @Override
    public ASTQuery getAST() {
        return ast;
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    /**
     * If Subquery is a bind, check scope.
     */
    @Override
    public boolean validate(ASTQuery a, boolean exist) {

        for (Variable var : ast.getSelectVar()) {
            // select exp as var
            // var must not be already in scope
            if (ast.hasExpression(var) && a.isBound(var)) {
                ast.addErrorMessage(Message.SCOPE_ERROR , var);
                a.setCorrect(false);
                return false;
            }
        }

        boolean b = ast.validate();

        if (!b) {
            a.setCorrect(false);
        }

        for (Variable var : ast.getSelectVar()) {
            a.bind(var);
            a.defSelect(var);
        }
        // select *
        if (ast.isSelectAll()) {
            for (Variable var : ast.getSelectAllVar()) {
                a.bind(var);
                a.defSelect(var);
            }
        }

        return b;
    }

    @Override
    void visit(ExpressionVisitor v) {
        // the Visitor determines whether it visists the subquery or not
        // because some may and other may not
        // e.g. Local variable visitor does not recursively visit subquery
        v.visit(this);
    }
    
    @Override
     public void walk(Walker walker) {
        walker.enter(this);
        getAST().walk(walker);
        walker.leave(this);
    }
    

}
