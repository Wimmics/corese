package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Draft prototype for federated query
 *
 * @service <s1>
 * @service <s2> 
 * select * where { } 
 * Recursively rewrite every triple t as:
 * service <s1> <s2> { t } Generalized service statement with several URI
 * Returns the union of Mappings without duplicate (select distinct *) 
 * PRAGMA:
 * Property Path evaluated in each of the services but not on the union 
 * (hence PP is not federated)
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ServiceVisitor implements QueryVisitor {

    ASTQuery ast;

    /**
     * Query Visitor just before compiling AST
     */
    @Override
    public void visit(ASTQuery ast) {
        this.ast = ast;

        if (ast.hasMetadata(Metadata.TYPE)) {
            System.out.println("before:");
            System.out.println(ast);
        }

        rewrite(ast);

        if (ast.hasMetadata(Metadata.TYPE)) {
            System.out.println("after:");
            System.out.println(ast.getBody());
        }
    }

    /**
     * Rewrite select (exists {BGP} as ?b) order by group by having body
     */
    void rewrite(ASTQuery ast) {
        for (Expression exp : ast.getSelectFunctions().values()) {
            rewrite(exp);
        }
        for (Expression exp : ast.getGroupBy()) {
            rewrite(exp);
        }
        for (Expression exp : ast.getOrderBy()) {
            rewrite(exp);
        }
        if (ast.getHaving() != null) {
            rewrite(ast.getHaving());
        }

        rewrite(ast.getBody());
    }


    Exp rewrite(Exp body) {
        return rewrite(null, body);
    }

    /**
     * Recursively rewrite every triple t as: service <s1> <s2> { t } Add
     * filters that are bound by the triple (except exists {} which must stay
     * local)
     */
    Exp rewrite(Atom name, Exp body) {

        for (int i = 0; i < body.size(); i++) {
            Exp exp = body.get(i);

            if (exp.isQuery()) {
                // TODO: graph name ??
                rewrite(exp.getQuery());
            } else if (exp.isService() || exp.isValues()) {
                // keep it
            } else if (exp.isFilter() || exp.isBind()) {
                // rewrite exists { }
                // TODO: name
                rewrite(name, exp.getFilter());
            } else if (exp.isTriple()) {
                Exp res = rewrite(name, exp.getTriple(), body);
                body.set(i, res);
            } else if (exp.isGraph()) {
                Exp res = rewrite(exp.getNamedGraph().getSource(), exp.get(0));
                body.set(i, res);
            } else {
                rewrite(name, exp);
            }
        }

        return body;
    }

    /**
     * Rewrite Triple t as: service <Si> { t } or: service <Si> { graph name { t
     * } } Add filters of body bound by t in the BGP, except exists filters.
     */
    Exp rewrite(Atom name, Triple t, Exp body) {
        BasicGraphPattern bgp = BasicGraphPattern.create();
        if (name == null) {
            // service uri { triple } 
            bgp.add(t);
        } else {
            // service uri { graph name { triple } } 
            bgp.add(Source.create(name, t));
        }
        Service s = Service.create(ast.getServiceList(), bgp, false);
        filter(body, t, bgp);
        return s;
    }

    boolean rewrite(Expression exp) {
        return rewrite(null, exp);
    }

    /*
     * Rewrite filter exists { t }
     * as:
     * filter exists { service <Si> { t } }
     * PRAGMA: it returns all Mappings whereas in this case
     * it could return only one. However, in the general case: 
     * exists { t1 t2 } it must return all Mappings.
     */
    boolean rewrite(Atom name, Expression exp) {
        boolean exist = false;
        if (exp.isTerm()) {
            if (exp.getTerm().isTermExist()) {
                exist = true;
                rewrite(name, exp.getTerm().getExist());
            } else {
                for (Expression arg : exp.getArgs()) {
                    if (rewrite(name, arg)) {
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }

    /**
     * Find filters bound by t in body, except exists {} Add them to bgp
     */
    void filter(Exp body, Triple t, BasicGraphPattern bgp) {
        for (Exp exp : body) {
            if (exp.isFilter()) {
                Expression f = exp.getFilter();
                if (!(f.isTerm() && f.getTerm().isTermExistRec())) {
                    if (t.bind(f)) {
                        bgp.add(exp);
                    }
                }
            }
        }
    }

    @Override
    public void visit(Query query) {
    }
}
