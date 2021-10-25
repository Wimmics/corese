package fr.inria.corese.sparql.triple.parser.visitor;

import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.parser.Message;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.HashMap;

/**
 * Walker just after parsing to complete the AST.
 */
public class ASTParser implements Walker, URLParam {
    
    ASTQuery ast;
    
    class BNodeMap extends HashMap<String, Exp> {}

    public ASTParser(ASTQuery ast) {
        this.ast = ast;
    }
    
       @Override
    public void enter(ASTQuery ast) {
        bnodeScope(ast.getBody());
    }
        
    @Override
    public void enter(Exp exp) {
        process(exp);
        bnodeScope(exp);
    }
    
    /**
     * check bnode scope
     */
    void bnodeScope(Exp exp) {
        if (exp.isAnd()) {
            and(exp, exp, new BNodeMap());
        }
    }
    
    
    void and(Exp exp, Exp outer, BNodeMap bnode) {
        for (Exp ee : exp) {
            if (ee.isTriple()) {
                scope(ee.getTriple(), outer, bnode);
            }
            else if (ee.isBinaryExp()) {
                and(ee.get(0), ee, bnode);
                and(ee.get(1), ee, bnode);
            }
        }
    }
    
    void scope(Triple t, Exp exp, BNodeMap bnode) {
        scope(t.getSubject(), exp, bnode);
        scope(t.getObject(), exp, bnode);
    }
    
    void scope(Atom at, Exp exp, BNodeMap bnode) {
        if (at.isBlankNode()) {
            Exp ee = (bnode.get(at.getLabel()));
            if (ee == null) {
                bnode.put(at.getLabel(), exp);
            }
            else if (exp != ee) {
                ast.setFail(true);
                ASTQuery.logger.error(String.format(Message.BNODE_SCOPE, at, exp));
            }
        }
    }
    
    
    
    void process(Exp exp) {
        if (exp.isService()) {
            enter(exp.getService());
        }
    }
    
    /**
     * http://server.fr/sparql?mode=provenance
     * FederateVisitor in core will rewrite service with 
     * a variable -> declare this variable in select clause 
     */ 
    void enter(Service exp) {
        Atom serv = exp.getServiceName();
        if (serv.isConstant()) {
            URLServer url = new URLServer(serv.getLabel());            
            if (url.hasParameter(MODE, PROVENANCE)) {
                boolean b = false;
                int n = 1;
                if (url.hasParameter(NBVAR)) {
                    n = url.intValue(NBVAR);
                }
                if (exp.getBodyExp().size()>0 && exp.getBodyExp().get(0).isQuery()) {
                    ASTQuery aa = exp.getBodyExp().get(0).getAST();
                    b = aa.provenance(ast, n);
                }
                if (! b) {
                    ast.provenance(null, n);
                }
            }
        }
    }
    
}
