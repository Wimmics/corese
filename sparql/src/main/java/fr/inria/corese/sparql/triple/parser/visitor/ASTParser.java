package fr.inria.corese.sparql.triple.parser.visitor;

import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Message;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.HashMap;

/**
 * Walker just after parsing to complete the AST.
 */
public class ASTParser implements Walker, URLParam {
    public static boolean SERVICE_LOG = false;
    private boolean log = SERVICE_LOG;
    
    ASTQuery ast;
    
    class BNodeMap extends HashMap<String, Exp> {}

    public ASTParser(ASTQuery ast) {
        this.ast = ast;
    }
    
    @Override
    public void start(ASTQuery ast) {
        serviceLog(ast);        
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
    
    
    
    void serviceLog(ASTQuery ast) {
        if (isLog() || ast.hasMetadata(Metadata.DETAIL)) {
            Variable var = new Variable(Binding.SERVICE_DETAIL);
            if (!ast.isSelectAll()) {
                ast.setSelect(var);
            }
            Values values = Values.create(var, (Constant) null);
            if (ast.getValues() == null) {
                ast.setValues(values);
            } else {
                ast.getBody().add(0, values);
            }

        }
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

    public boolean isLog() {
        return log;
    }

    public void setLog(boolean log) {
        this.log = log;
    }
    
}
