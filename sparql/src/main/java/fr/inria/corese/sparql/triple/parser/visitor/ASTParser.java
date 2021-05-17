package fr.inria.corese.sparql.triple.parser.visitor;

import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;

/**
 * Walker just after parsing to complete the AST.
 */
public class ASTParser implements Walker, URLParam {
    
    ASTQuery ast;

    public ASTParser(ASTQuery ast) {
        this.ast = ast;
    }
    
    @Override
    public void enter(Exp exp) {
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
