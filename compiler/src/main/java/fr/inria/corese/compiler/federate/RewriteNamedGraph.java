package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.Union;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RewriteNamedGraph {
    
    ASTQuery ast;
    FederateVisitor vis;

    RewriteNamedGraph(FederateVisitor vis) {
        this.vis = vis;
    }
    
    
    
    /**
     * named graph sent as a whole in service clause    
     */
    Exp simpleNamed(ASTQuery ast, Source exp) {
        // send named graph as is to remote servers
        if (ast.getDataset().hasNamed()) {
            Query q = vis.getRewriteTriple().query(BasicGraphPattern.create(exp));
            q.getAST().getDataset().setNamed(ast.getNamed());
            return vis.service(ast.getServiceList(), BasicGraphPattern.create(q));
        }
        return vis.service(ast.getServiceList(), BasicGraphPattern.create(exp));
    }
    
    
    Exp rewriteNamed(ASTQuery ast, Source exp) {  
        Atom name = exp.getSource();
        if (name.isVariable()) {
            return rewriteNamed(ast, name.getVariable(), exp.getBodyExp(), exp);
        }
        else {
            return vis.rewrite(name.getConstant(), exp.getBodyExp());
        }
    }
    
   /**
    * from named G = {g1 .. gn}  -- gi on remote dataset
    * graph ?g { exp } -> rewrite as:
    * union  (g in G) { 
    * values ?g { g }
    * service (si) { select * where { graph g { exp } } }
    * @todo
    * service <si?named-graph-uri=g1 .. gn> {graph ?g {exp}}
    * @todo subquery.copy() 
    */
    Exp rewriteNamed(ASTQuery ast, Variable var, Exp body, Source exp) {
        if (ast.getNamed().isEmpty()) {
            return simpleNamed(ast, exp);
        }

        ArrayList<Exp> list = new ArrayList<>();
        
        for (Constant namedGraph : ast.getNamed()) {
            Values values = Values.create(var, namedGraph);
            Exp res = vis.rewrite(namedGraph, body.copy());
            res.add(values);
            list.add(res);
        }
        
        return union(list, 0);
    }
                 
    Exp union(List<Exp> list, int n) {
        if (n == list.size() - 1){
            return list.get(n);
        }
        else {
            return Union.create(list.get(n), union(list, n+1));
        }
    }    
        
    
    
    
    /**
     * expandList contains rewrite of graph ?g { } if any
     * This function includes the rewritten named graph statement
     */
    void expand(Exp body, ArrayList<Exp> expandList) {
        for (int i = 0; i < body.size();  ) {
            Exp exp = body.get(i);
            if (exp.isBGP() && expandList.contains(exp)) {
                body.remove(i);
                for (Exp e : exp) {
                    if (e.isBGP()){
                        for (Exp ee : e){
                           body.add(i++, ee); 
                        }
                    }
                    else {
                        body.add(i++, e);
                    }
                }
            } else {
                i++;
            }
        }
    }
    
}
