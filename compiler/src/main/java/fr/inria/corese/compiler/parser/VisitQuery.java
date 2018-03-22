package fr.inria.corese.compiler.parser;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.kgram.api.core.Edge;
import static fr.inria.corese.kgram.api.core.ExpType.BIND;
import static fr.inria.corese.kgram.api.core.ExpType.EDGE;
import static fr.inria.corese.kgram.api.core.ExpType.FILTER;
import static fr.inria.corese.kgram.api.core.ExpType.PATH;
import static fr.inria.corese.kgram.api.core.ExpType.QUERY;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Query;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class VisitQuery {

    Compiler compiler;
    Query query;

    VisitQuery(Compiler c) {
        compiler = c;
    }

    void visit(Query q) {
        query = q;
        q.setNodeList(visit(q.getBody()));
    }

    List<Node> visit(Exp exp) {
        ArrayList<Node> list = new ArrayList<Node>();
        visit(exp, list);
        return list;
    }
    
    void add(List<Node> list, Node p, Edge edge) {
        if (! list.contains(p)) {
            list.add(p);
        }
        query.recordPredicate(p, edge);
    }

    /**
     * Return predicate nodes of this exp: edge, path regex and constraints,
     * filter exists, query select having TODO: query order|group by exists
     */
    void visit(Exp exp, List<Node> list) {
        switch (exp.type()) {

            case EDGE:
                add(list, exp.getEdge().getEdgeNode(), exp.getEdge());
                if (exp.isPath()){
                    visit(exp.getPath());
                }
                break;

            case PATH:
                visitRegex(exp.getEdge(), (Expression) exp.getRegex(), list);
                break;

            case FILTER:
                visit(exp.getFilter().getExp(), list);
                break;

            case BIND:
                visit(exp.getFilter().getExp(), list);
                break;

            case QUERY:
                Query q = exp.getQuery();

                for (Exp ee : q.getSelectFun()) {
                    if (ee.getFilter() != null) {
                        visit(ee.getFilter().getExp(), list);
                    }
                }

                if (q.getHaving() != null) {
                    visit(q.getHaving().getFilter().getExp(), list);
                }
            // continue

            default:
                for (Exp ee : exp.getExpList()) {
                    visit(ee, list);
                }

        }
    }

    /**
     * exp is a Regex return its predicates
     */
    void visitRegex(Edge edge, Expression exp, List<Node> list) {
        if (exp.isConstant()) {
            Node node = compiler.createNode(exp.getConstant());
            add(list, node, edge);
        } else if (exp.isTerm() && exp.isTest()) {
            // path @[ a foaf:Person ]
            Expression ee = exp.getExpr();
            visit(ee, list);
        } else if (exp.isTerm() && exp.isNot()) {
            // ! p is a problem because we do not know the predicate nodes ...
            // let's return top level property, it subsumes all properties
            //list.clear();
            Node node = compiler.createNode(ASTQuery.getRootPropertyURI());
            add(list, node, edge);
        } else {
            for (Expression ee : exp.getArgs()) {
                visitRegex(edge, ee, list);
            }
        }
    }

    /**
     * Filter: check exists {}
     */
    void visit(Expr exp, List<Node> list) {
        for (Expr ee : exp.getExpList()) {
            visit(ee, list);
        }
        if (exp.oper() == ExprType.EXIST) {
            visit((Exp) exp.getPattern(), list);
        }
    }
}
