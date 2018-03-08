/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.kgenv.parser;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.corese.kgenv.api.QueryVisitor;
import fr.inria.corese.kgram.core.Query;

/**
 * Replace RDF List by Property Path rdf:rest* / rdf:first in SPARQL Query
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
public class ExpandList implements QueryVisitor {
    
    public static ExpandList create(){
        return new ExpandList();
    }
    
    
    @Override
    public void visit(ASTQuery ast) {
        process(ast);
    }

    @Override
    public void visit(Query query) {
    }
    
    
    

    void process(ASTQuery ast) {
        process(ast, ast.getBody());
    }

    void process(ASTQuery ast, Exp exp) {
        for (int i = 0; i<exp.getBody().size(); i++){
            Exp ee = exp.getBody().get(i);
            if (ee.isRDFList()) {
                RDFList l = (RDFList) ee;
                Exp lp = ast.path(l);
                exp.getBody().set(i, lp);
            }
            else {
                process(ast, ee);
            }
        }
    }
     
}
