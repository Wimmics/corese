package fr.inria.edelweiss.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Metadata;
import fr.inria.acacia.corese.triple.parser.Service;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgenv.api.QueryVisitor;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * Draft prototype for federated query
 * @service <s1> @service <s2> select * where { }
 * Recursively rewrite every triple t as: service <s1> <s2> { t }
 * Generalized service statement with several URI
 * Returns the union of Mappings without duplicate (select distinct *)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class ServiceVisitor implements QueryVisitor {

    @Override
    public void visit(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.TYPE)){
            System.out.println("before:");
            System.out.println(ast.getExtBody());
        }
        
        Exp body = rewrite(ast, ast.getExtBody());
        ast.setQuery(body);
        
        if (ast.hasMetadata(Metadata.TYPE)){
            System.out.println("after:");
            System.out.println(body);
        }    
    }
    
    
    /**
     * Recursively rewrite every triple t as: service <s1> <s2> { t } 
     * Add filters that are bound by the triple (except exists {}  which must stay local)
     */
     Exp rewrite(ASTQuery ast, Exp body) { 
        
        for (int i = 0; i<body.size(); i++) {
            Exp exp = body.get(i);
            if (exp.isService() ||  exp.isValues() || exp.isBind()) {
                // keep it
            } 
            else if (exp.isFilter()) {
                // rewrite exists { }
                rewrite(ast, exp.getFilter());
            }
            else if (exp.isTriple()) {
                BasicGraphPattern res = BasicGraphPattern.create(exp);
                Service s = Service.create(ast.getServiceList(), res, false);
                filter(body, exp.getTriple(), res);
                body.set(i, s);
            } 
            else {
                body.set(i, rewrite(ast, exp));
            }
        }
               
        return body;
    }

     /*
      * Rewrite filter exists
      */
    boolean rewrite(ASTQuery ast, Expression exp){
        boolean exist = false;
        if (exp.isTerm()){
            if (exp.getTerm().isTermExist()){
                exist = true;
                rewrite(ast, exp.getTerm().getExist());
            }
            else {
                for (Expression arg : exp.getArgs()){
                    if (rewrite(ast, arg)){
                        exist = true;
                    }
                }
            }
        }
        return exist;
    }
    
    
    /**
     * Find filters bound by t in body, except exists {}
     * Add them to res
     */
    void filter(Exp body, Triple t, BasicGraphPattern res) {
        for (Exp exp : body) {
            if (exp.isFilter()){
                Expression f = exp.getFilter();
                if (! (f.isTerm() && f.getTerm().isTermExistRec())) {
                    if (t.bind(f)){
                        res.add(exp);
                    }
                }
            }
        }
    }
    

    @Override
    public void visit(Query query) {
    }

}
