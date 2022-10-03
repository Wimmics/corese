package fr.inria.corese.sparql.triple.parser.visitor;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.api.Walker;
import fr.inria.corese.sparql.triple.parser.Message;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.Service;
import fr.inria.corese.sparql.triple.parser.Term;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import fr.inria.corese.sparql.triple.parser.Values;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Composite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Walker just after parsing to complete the AST.
 */
public class ASTParser implements Walker, URLParam {
    
    private static final String UNNEST = "unnest";
    private static final String JSREPORTS_ENUM = "js:reportsEnum";
    private static final String JSREPORT = "js:report";
    private static final String VAR_VAL = "?val";
    private static final String VAR_KEY = "?key";

    // set by Property SERVICE_REPORT
    public static boolean SERVICE_REPORT = false;
    public static boolean RDF_STAR = false;
    private boolean log = SERVICE_REPORT;

    private ASTQuery ast;
    private ArrayList<ASTQuery> stack;
    private int nbService = 0;
    private boolean bnode = true;
    // when false: do it later after Federate Visitor
    private boolean report = true;
    private boolean provenance = true;

    class BNodeMap extends HashMap<String, Exp> {
    }

    public ASTParser(ASTQuery ast) {
        this.ast = ast;
        stack = new ArrayList<>();
        setLog(isLog() || ast.hasMetadata(Metadata.REPORT));
    }

    // std parser call
    public ASTParser configure() {
        if (getAST().isFederateVisitorable()) {
            // first visit: no report
            // do it after FederateVisitor
            setReport(false);
        }
        return this;
    }

    // special call after FederateVisitor
    public ASTParser report() {
        setBnode(false);
        setProvenance(false);
        setReport(true);
        return this;
    }

    @Override
    public void start(ASTQuery ast) {
        init(ast);
    }

    @Override
    public void finish(ASTQuery ast) {
        serviceReport(ast);
    }

    @Override
    public void enter(ASTQuery ast) {
        push(ast);
        bnodeScope(ast.getBody());
    }

    @Override
    public void leave(ASTQuery ast) {
        leaveAST(ast);
        pop();
    }
    
    
    
    @Override
    public void enter(ASTUpdate ast) {       
    }

    @Override
    public void leave(ASTUpdate ast) {
    }

    @Override
    public void enter(Composite c) {
    }
    


    @Override
    public void leave(Composite c) {
    }
    
    

    @Override
    public void enter(Exp exp) {
        processEnter(exp);
        bnodeScope(exp);
    }

    @Override
    public void leave(Exp exp) {
        processLeave(exp);
    }
    
    void init(ASTQuery ast) {
        index(ast);
        storage(ast);
    }
    
    // select *
    // from <index:http://prod-dekalog.inria.fr>
    // where 
    // ->
    // @federation @index <http://prod-dekalog.inria.fr>
    void index(ASTQuery ast) {
        boolean index = ast.getDataset().index();
        if (index) {
            ast.getCreateMetadata().add(Metadata.FEDERATION);
        }
    }

    // select * from <store:/my/path> where {}
    void storage(ASTQuery ast) {
        ast.getDataset().storage();
    }

    /**
     * When @report : declare variable ?_service_report_n
     */
    void serviceReport(ASTQuery ast) {
        if (isReport()) {
            if (isLog()) {
                serviceReportMain(ast);
            }
        }
    }

    void leaveAST(ASTQuery ast) {
        if (isReport()) {
            if (ast != getAST()) {
                serviceReportSub(ast);
            }
        }
    }

    /**
     * ast is the main query
     */
    void serviceReportMain(ASTQuery ast) {
        ArrayList<Variable> varList = new ArrayList<>();
        // generate at least one variable ?_service_report 
        // in case of xt:sparql() with service inside
        int count = Math.max(1, getNbService());

        for (int i = 0; i < count; i++) {
            Variable var = new Variable(String.format(fr.inria.corese.sparql.triple.function.term.Binding.SERVICE_REPORT_FORMAT, i));
            varList.add(var);

            if (! ast.isSelectAll() && ! ast.hasMetadata(Metadata.SKIP)) {
                ast.setSelect(var);
            }
        }

        Values values = Values.create(varList);

        if (ast.getValues() == null
                && !ast.isConstruct()
                && // @federate <singleURL>
                (!(ast.hasMetadata(Metadata.FEDERATE) && ast.getServiceList().size() == 1))) {
            // virtuoso reject construct with values
            // virtuoso reject external values 
            ast.setValues(values);
        } else {
            ast.getBody().add(0, values);
        }

        if (ast.hasMetadata(Metadata.ENUM)) {
            enumReport(ast);
        }
    }

    /**
     *
     * @report @enum generate values (?key ?val) {unnest(js:reportsEnum())}
     * js:reportsEnum() ::= (key_i (val_i1 .. val_in))
     *
     */
    void enumReport(ASTQuery ast) {
        IDatatype num = ast.getMetadata().getDatatypeValue(Metadata.ENUM);
        Variable key = new Variable(VAR_KEY);
        Variable val = new Variable(VAR_VAL);
        List<Variable> vlist = List.of(key, val);
        Term rep;
        if (num == null) {
            if (getNbService()==1) {
            // report number 0
                rep = ast.createFunction(ast.createQName(JSREPORT));
            }
            else {
                // all reports
                rep = ast.createFunction(ast.createQName(JSREPORTS_ENUM));
            }
        }
        else {
            // report number num
            rep = ast.createFunction(ast.createQName(JSREPORT), Constant.create(num));
        }
        Term fun = ast.createFunction(UNNEST, rep);
        try {
            fun.compile(ast);
        } catch (EngineException ex) {
            ASTQuery.logger.error("ASTParser: " + ex.getMessage());
        }
        if (!ast.isSelectAll()) {
            ast.setSelect(0, key);
            ast.setSelect(1, val);
        }
        Values value = ast.createValues(vlist, fun);
        ast.getBody().add(value);
    }

    /**
     * ast is a subquery, not the main query
     */
    void serviceReportSub(ASTQuery ast) {
        ArrayList<Variable> varList = new ArrayList<>();

        for (Service service : ast.getServiceExpList()) {
            if (isLog() || (service.getURL() != null && service.getURL().hasParameter(MODE, REPORT))) {
                Variable var = new Variable(String.format(fr.inria.corese.sparql.triple.function.term.Binding.SERVICE_REPORT_FORMAT, service.getNumber()));
                varList.add(var);

                for (ASTQuery aa : getStack()) {
                    // export report variable through intermediate subselect 
                    // to top select (skip top select when @skip)
                    if (aa.isSelectAll() || 
                       (aa == getAST() && aa.hasMetadata(Metadata.SKIP))) {
                        // do nothing
                    }
                    else {
                        aa.setSelect(var);
                    }
                }
            }
        }

        if (!varList.isEmpty()) {
            ast.getBody().add(0, Values.create(varList));
        }
    }

    void processEnter(Exp exp) {
        if (exp.isService()) {
            processEnter(exp.getService());
        }
    }

    void processLeave(Exp exp) {
        if (exp.isService()) {
            processLeave(exp.getService());
        }
    }

    void processEnter(Service exp) {
        URLServer url = exp.getCreateURL();

        if (isProvenance()) {
            provenance(url, exp);
        }
    }

    /**
     * @report Generate service number on leave when nested services: inner
     * service is 0 and outer service is 1
     */
    void processLeave(Service exp) {
        URLServer url = exp.getURL();
        if (url != null) {
            // url = constant
            if (url.hasParameter(MODE, REPORT)) {
                setLog(true);
            }
        }
        exp.setNumber(nbService++);
        if (top() == null) {
            ast.logger.error("AST stack empty");
        }
        else {
            top().getServiceExpList().add(exp);
        }
    }

    /**
     * ***************************************
     * http://server.fr/sparql?mode=provenance FederateVisitor in core will
     * rewrite service with a variable -> declare this variable in select clause
     */
    void provenance(URLServer url, Service exp) {
        if (url != null) {
            if (url.hasParameter(MODE, PROVENANCE)) {
                boolean b = false;
                int n = 1;
                if (url.hasParameter(NBVAR)) {
                    n = url.intValue(NBVAR);
                }
                if (exp.getBodyExp().size() > 0 && exp.getBodyExp().get(0).isQuery()) {
                    ASTQuery aa = exp.getBodyExp().get(0).getAST();
                    b = aa.provenance(getAST(), n);
                }
                if (!b) {
                    getAST().provenance(null, n);
                }
            }
        }
    }

    /**
     * **********************************************
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
            } else if (ee.isBinaryExp()) {
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
            } else if (exp != ee) {
                getAST().setFail(true);
                ASTQuery.logger.error(String.format(Message.BNODE_SCOPE, at, exp));
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

    public ASTQuery getAST() {
        return ast;
    }

    public void setAST(ASTQuery ast) {
        this.ast = ast;
    }

    int last() {
        return getStack().size() - 1;
    }

    void push(ASTQuery ast) {
        getStack().add(ast);
    }

    ASTQuery pop() {
        ASTQuery ast = getStack().get(last());
        getStack().remove(last());
        return ast;
    }

    ASTQuery top() {
        if (getStack().isEmpty()) {
            return null;
        }
        return getStack().get(last());
    }

    public ArrayList<ASTQuery> getStack() {
        return stack;
    }

    public void setStack(ArrayList<ASTQuery> stack) {
        this.stack = stack;
    }

}
