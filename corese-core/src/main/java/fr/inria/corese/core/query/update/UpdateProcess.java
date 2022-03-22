package fr.inria.corese.core.query.update;

import fr.inria.corese.core.Event;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Source;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.triple.update.Basic;
import fr.inria.corese.sparql.triple.update.Composite;
import fr.inria.corese.sparql.triple.update.Update;
import fr.inria.corese.sparql.triple.function.term.Binding;

import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.ArrayList;
import java.util.List;

/**
 * SPARQL 1.1 Update
 *
 * Called by QueryProcess.query() to handle update query
 * Compile update query into specific AST which is executed by ManagerImpl
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class UpdateProcess {

    private static Logger logger = LoggerFactory.getLogger(UpdateProcess.class);

    private ManagerImpl manager; 
    private QueryProcess exec;
    Query query;
    private Dataset dataset;
    private ProcessVisitor visitor;

    boolean isDebug = false;

    UpdateProcess(QueryProcess e, ManagerImpl man, Dataset ds) {
        manager = man;
        exec = e;
        this.dataset = ds;
    }

    public static UpdateProcess create(QueryProcess e, ManagerImpl man, Dataset ds) {
        UpdateProcess u = new UpdateProcess(e, man, ds);
        return u;
    }

    /**
     * Process an update sparql query There may be a list of queries return the
     * Mappings of the last update ...
     */
    public Mappings update(Query q, Mapping m, Binding bind) throws EngineException {
        query = q;
        ASTQuery ast =  q.getAST();
        ASTUpdate astu = ast.getUpdate();
        Mappings map = Mappings.create(q);
        getManager().setLevel(getLevel(bind));
        getManager().setAccessRight(bind == null ? null : bind.getAccessRight());
        NSManager nsm = null;
        // Visitor was setup by QueryProcessUpdate init(q, m)
        setVisitor(getQueryProcess().getVisitor());
        
        for (Update u : astu.getUpdates()) {
            if (isDebug) {
                logger.debug("** Update: " + u);
            }
            boolean suc = true;

            switch (u.type()) {

                case Update.PROLOG:
                    // local prolog, get the local NSManager
                    // for next operations
                    nsm = u.getLocalNSM();
                    break;

                default:
                    // assign local NSManager to current operation
                    if (nsm != null) {
                        u.setLocalNSM(nsm);
                    }
            }
            
            getQueryProcess().getEventManager().start(Event.UpdateStep, u);
            Eval eval = getQueryProcess().getCurrentEval();
            if (u.isBasic()) {
                // load copy ...
                Basic b = u.getBasic();
                suc = getManager().process(q, b, getDataset());
            } else {
                // delete insert data where
                Composite c = u.getComposite();
                // pass current binding as parameter
                // use case: @event function has defined global variable
                map = process(q, c, m, bind); 
            }
            // save and restore eval to get initial Visitor with possible @event function
            // because @event function may execute query and hence reset eval
            getQueryProcess().setCurrentEval(eval);
            getQueryProcess().getEventManager().finish(Event.UpdateStep, u);

            if (!suc) {
                q.setCorrect(false);
                if (isDebug) {
                    logger.debug("** Failure: " + u);
                }
                break;
            }
        }
        return map;
    }
    
    Level getLevel(Mapping m) {
        return Access.getLevel(m, Level.USER_DEFAULT);
    } 
    
    Level getLevel(Binding b) {
        if (b == null) {
            return Level.USER_DEFAULT;
        }
        return b.getAccessLevel();
    } 
    

    public void setDebug(boolean b) {
        isDebug = b;
    }

    Mappings process(Query q, Composite ope, Mapping m, Binding b) throws EngineException {
        ASTQuery ast = null;
        switch (ope.type()) {

            case Update.INSERT:
                // insert data
                ast = getInsertData(q, ope);
                break;

            case Update.DELETE:
                // delete data
                ast = getDeleteData(q, ope);
                break;

            case Update.COMPOSITE:
                // delete insert where
                ast = getDeleteInsert(q, ope);
                break;

        }

        if (ast == null) {
            return Mappings.create(q);
        }
        return update(q, ast, m, b);
    }

    
    /**
     * Share global variables from LDScript Bind stack
     * Binding b is the stack shared by update subqueries and 
     * PRAGMA: m.getBind() = b
     * @event functions
     */
    Mapping getMapping(Query q, Mapping m, Binding b) {
        Mapping mm = m;
        if (m != null && m.size() == 0 && m.getBind() != null) {
            // m contains LDScript binding stack
            // generate appropriate Mapping for query q from this stack.
            // use case: query(insert where))
            mm = Mapping.create(q, m.getBind());
        }

        // use case: update query with several subqueries
        // previous subquery @event function has set global variables 
        // stored in Binding of previous Eval
        // share these global variables with current Eval
        if (b != null) {            
            mm = setBind(mm, b);
        }
        
        mm = setVisitor(mm, getVisitor());
        return mm;
    }
    
    Mapping setBind(Mapping m, Binding b) {
        if (m == null) {
            m = new Mapping();
        }
        m.setBind(b);
        return m;
    }
    
    Mapping setVisitor(Mapping m, ProcessVisitor vis) {
        if (m == null) {
            m = new Mapping();
        }
        if (m.getBind() == null) {
            m.setBind(Binding.create());
        }
        m.getBind().setVisitor(m.getBind().getVisitor());
        return m;
    }

    /**
     * query is the global Query ast is the current update action use case:
     * delete insert data delete insert where In case of data, fake an empty
     * where and process as a where update.
     */
    Mappings update(Query query, ASTQuery ast, Mapping m, Binding b) throws EngineException {

        //System.out.println("** QP:\n" + m.getBind());
        getQueryProcess().logStart(query);
        Query q = compile(ast);
        inherit(q, query);
        Mapping mm = getMapping(q, m, b);  
        
        //System.out.println("UP: " + vis);
        // execute where part in delete insert where
        Mappings map = getQueryProcess().basicQuery(null, q, mm);
        
        if (query.getAST().hasMetadata(Metadata.SELECT)) {
            // evaluate insert where as select where
            return map;
        }
   
        // execute delete in delete insert where
        if (q.isDelete()) {
            getManager().delete(q, map, getDataset());
        }

        // execute insert in delete insert where
        if (q.isInsert()) {
            getManager().insert(q, map, getDataset());
        }

        visitor(getVisitor(), q, map);
        getQueryProcess().logFinish(query, map);

        return map;
    }
    
    Query compile(ASTQuery ast) throws EngineException {
        Query q = ast.getUpdateQuery();
        if (q == null) {
            q = getQueryProcess().compile(ast, getDataset());
            q.setUpdate(true);
            ast.setUpdateQuery(q);
        }
        return q;
    }
    
    void inherit(Query update, Query query) {
        update.setDetail(query.isDetail());
    }
    
    /**
     * @update function
     * To get delete & insert list requires q.isDetail() = true
     * It is done when there is a local @update function
     * if @update function is @public, annotate query with: @event @update
     */
    void visitor(ProcessVisitor vis, Query q, Mappings map) {
        List<Edge> delete = map.getDelete();
        List<Edge> insert = map.getInsert();
        if (delete == null) {
            delete = new ArrayList<>();
        }
        if (insert == null) {
            insert = new ArrayList<>();
        }
        vis.update(q, delete, insert);
    }
    
    ASTQuery getInsertData(Query q, Composite ope) {
        ASTQuery ast = ope.getAST();
        if (ast == null) {
            ast = insertData(q, ope);
            ope.setAST(ast);
        }
        return ast;
    }
    
    ASTQuery getDeleteData(Query q, Composite ope) {
        ASTQuery ast = ope.getAST();
        if (ast == null) {
            ast = deleteData(q, ope);
            ope.setAST(ast);
        }
        return ast;
    }
    
    ASTQuery getDeleteInsert(Query q, Composite ope) {
        ASTQuery ast = ope.getAST();
        if (ast == null) {
            ast = deleteInsertWhere(q, ope);
            ope.setAST(ast);
        }
        return ast;
    }
    

    /**
     * insert data {<a> ex:p <b>} Ground pattern (no variable) Processed as a
     * construct query in the target graph
     */
    ASTQuery insertData(Query q, Composite ope) {

        ASTQuery ast = createAST(q, ope);
        ast.setInsert(true);

        Exp exp = ope.getData();
        if (!exp.validateData(ast)) {
            if (isDebug) {
                logger.debug("** Update: insert not valid: " + exp);
            }
            q.setCorrect(false);
            return null; 
        }

        if (exp != null) {
            ast.setBody(BasicGraphPattern.create());
            ast.setInsert(exp);
            ast.setInsertData(true);
        }

        // Processed as a construct (add) on target graph
        return ast;
    }

    /**
     * delete data {<a> ex:p <b>} Ground pattern (no variable) Processed by
     * Construct as a delete query in the target graph
     *
     */
    ASTQuery deleteData(Query q, Composite ope) {

        ASTQuery ast = createAST(q, ope);
        ast.setDelete(true);

        Exp exp = ope.getData();
        if (!exp.validateData(ast) || !exp.validateDelete()) {
            q.setCorrect(false);
            q.addError("** Update: delete not valid: ", exp);
            return null; 
        }

        if (exp != null) {
            ast.setBody(BasicGraphPattern.create());
            ast.setDelete(exp);
            ast.setDeleteData(true);
        }

        return ast;

    }

    /**
     * with delete {pat} insert {pat} using where {pat}
     * create specific AST ready to use
     */
    ASTQuery deleteInsertWhere(Query q, Composite ope) {

        // the graph where insert/delete occurs
        Constant src = ope.getWith();

        ASTQuery ast = createAST(q, ope);

        for (Composite cc : ope.getUpdates()) {

            Exp exp = cc.getPattern();

            if (src != null) {
                // insert in src
                exp = Source.create(src, exp);
                exp = BasicGraphPattern.create(exp);
            }

            if (cc.type() == Update.INSERT) {
                // insert {exp}
                ast.setInsert(true);
                ast.setInsert(exp);
            } else {
                // delete {exp}
                ast.setDelete(true);
                ast.setDelete(exp);

                if (!exp.validateDelete()) {
                    q.setCorrect(false);
                    q.addError("Error: Blank Node in Delete", "");
                    return null; //Mappings.create(q);
                }
            }
        }

        return ast;
    }

    /**
     * Create an AST with the where part (empty for data update)
     *
     */
    ASTQuery createAST(Query q, Composite ope) {
        ASTQuery ast = ASTQuery.create();
        ASTQuery ga =  q.getAST();
        ast.setNSM(ope.getNSM());
        
        ast.setPragma(ga.getPragma());
        ast.setPrefixExp(ga.getPrefixExp());
        ast.shareFunction(ga);
        ast.setAnnotation(ga.getMetadata());
        ast.setSelectAll(true);
        // where {pat}
        ast.setBody(ope.getBody());
        ast.setValues(ope.getValues());
        if (ope.getDataset().isEmpty()) {
            if (ope.getWith() != null) {
                // SPARQL requires that if there is WITH and if the endpoint Dataset has named graph
                // the named graph are still in the Dataset
                // so in this case, do not complete() the Dataset
                ast.getDataset().setWith(ope.getWith());
            }
        } else {
            ast.setDataset(ope.getDataset());
        }
        
        return ast;
    }

    /**
     * @return the visitor
     */
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    public QueryProcess getQueryProcess() {
        return exec;
    }

    public void setQueryProcess(QueryProcess exec) {
        this.exec = exec;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public ManagerImpl getManager() {
        return manager;
    }

    public void setManager(ManagerImpl manager) {
        this.manager = manager;
    }

}
