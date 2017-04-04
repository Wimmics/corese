package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Query;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import java.util.ArrayList;

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
        ArrayList<Exp> expandList = new ArrayList<Exp> ();
        
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
                // triple t -> service <Si> { t }
                Exp res = rewrite(name, exp.getTriple(), body);
                body.set(i, res);
            } else if (exp.isGraph()) {
                // graph ?g { t1 t2 .. } -> { service <Si> { graph ?g { ti }} .. }
                Exp res = rewrite(exp.getNamedGraph().getSource(), exp.get(0));
                body.set(i, res);
                expandList.add(res);
            } else {
                // rewrite BGP, union, optional, mminus
                rewrite(name, exp);
            }
        }
        
        if (!expandList.isEmpty()) {
            expand(body, expandList);
        }

        return body;
    }
    
    /**
     * expandList contains rewrite of graph ?g { } if any
     * each element of expandList is the rewrite of the BGP of a graph ?g BGP
     * This function includes the elements of the BGP directly in the body
     * in other words, it removes the { } from the BGP and add the elements 
     */
    void expand(Exp body, ArrayList<Exp> expandList) {
        // CARE: in this loop, the i++ is done explicitely in the body 
        for (int i = 0; i < body.size();  ) {
            Exp exp = body.get(i);
            if (expandList.contains(exp)) {
                for (Exp e : exp) {
                    body.set(i++, e);
                }
            } else {
                i++;
            }
        }
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
        filter(body, t, bgp);        
        Exp exp = from(name, bgp);
        Service s = Service.create(ast.getServiceList(), exp, false);
        return s;
    }
    
    /** exp = BGP | graph name { BGP }
     * if from or from named, generate 
     * select * from  from named where { exp }
     */
    Exp from(Atom name, Exp exp){
        if (name == null){ 
            if (ast.getDataset().hasFrom()){
                  Query q = query(exp);
                  q.getAST().getDataset().setFrom(ast.getFrom());
                  return BasicGraphPattern.create(q);
            }   
        }
        else if (ast.getDataset().hasNamed()) {
            Query q = query(exp);
            q.getAST().getDataset().setNamed(ast.getNamed());
            return BasicGraphPattern.create(q);
        }
        return exp;
    }
    
    Query query(Exp exp){
         ASTQuery as = ASTQuery.create(exp);
         as.setSelectAll(true);
         return Query.create(as);
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
    void filter(Exp body, Triple t, Exp bgp) {
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
    public void visit(fr.inria.edelweiss.kgram.core.Query query) {
    }
}
