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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Walker just after parsing to complete the AST.
 */
public class ASTParser implements Walker, URLParam {
    // set by Property SERVICE_REPORT
    public static boolean SERVICE_REPORT = false;
    private boolean log = SERVICE_REPORT;
    
    ASTQuery ast;
    private int nbService = 0;
    private boolean bnode = true;
    // false: do it later after Federate Visitor
    private boolean report = true;
    private boolean provenance = true;
    
    class BNodeMap extends HashMap<String, Exp> {}

    public ASTParser(ASTQuery ast) {
        this.ast = ast;
    }
    
    public ASTParser configure() {
        if (ast.isFederateVisitorable()) {
            // first visit: no report
            // do it after FederateVisitor
            setReport(false);
        }
        return this;
    }
    
    // after FederateVisitor
    public ASTParser report() {
        setBnode(false);
        setProvenance(false);
        setReport(true);
        return this;
    }
    
    
    @Override
    public void start(ASTQuery ast) {
        //serviceReport(ast);        
    }
    
    @Override
    public void finish(ASTQuery ast) {
        serviceReport(ast);        
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
     * When @report : declare variable ?_service_report_n
     */
    void serviceReport(ASTQuery ast) {
        if (isReport()) {
            if (isLog() || ast.hasMetadata(Metadata.REPORT)) {
                serviceReportBasic(ast);
            }
        }
    }

    void serviceReportBasic(ASTQuery ast) {
        ArrayList<Variable> varList = new ArrayList<>();
        ArrayList<Constant> valList = new ArrayList<>();
        int count = Math.max(1, getNbService());

        for (int i = 0; i < count; i++) {
            Variable var = new Variable(String.format(Binding.SERVICE_REPORT_FORMAT, i));
            varList.add(var);
            valList.add(null);
            if (!ast.isSelectAll()) {
                ast.setSelect(var);
            }
        }
        Values values = Values.create(varList, valList);
        if (ast.getValues() == null && !ast.isConstruct()) {
            // virtuoso reject construct with values
            ast.setValues(values);
        } else {
            ast.getBody().add(0, values);
        }
    }
    
    
    
    /**
     * check bnode scope
     */
    void bnodeScope(Exp exp) {
        if (isBnode()) {
            if (exp.isAnd()) {
                and(exp, exp, new BNodeMap());
            }
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
            URLServer url = exp.getService().getURL();
            
            if (isProvenance()) {
                provenance(url, exp.getService());
            }
            
            if (url != null) {
                if (url.hasParameter(MODE, REPORT)) {
                    setLog(true);
                }
            }
            
            nbService++;
        }
    }   
    
    /**
     * http://server.fr/sparql?mode=provenance
     * FederateVisitor in core will rewrite service with 
     * a variable -> declare this variable in select clause 
     */ 
    void provenance(URLServer url, Service exp) {
        if (url != null) {
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

    public int getNbService() {
        return nbService;
    }

    public void setNbService(int nbService) {
        this.nbService = nbService;
    }

    public boolean isBnode() {
        return bnode;
    }

    public void setBnode(boolean bnode) {
        this.bnode = bnode;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public boolean isProvenance() {
        return provenance;
    }

    public void setProvenance(boolean provenance) {
        this.provenance = provenance;
    }
    
}
