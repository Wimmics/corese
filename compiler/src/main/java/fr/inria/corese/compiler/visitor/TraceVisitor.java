package fr.inria.corese.compiler.visitor;

import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.triple.parser.ASTQuery;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class TraceVisitor implements QueryVisitor {

    @Override
    public void visit(ASTQuery ast) {
        System.out.println("TraceVisitor:");
        System.out.println(ast);
    }

    @Override
    public void visit(Query query) {
    }

}
