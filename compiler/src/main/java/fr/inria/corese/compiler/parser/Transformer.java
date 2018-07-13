package fr.inria.corese.compiler.parser;

import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.datatype.DatatypeHierarchy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.parser.*;
import fr.inria.corese.sparql.triple.parser.ASTExtension.ASTFunMap;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.kgram.api.core.*;
import static fr.inria.corese.kgram.api.core.ExpType.NODE;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.Sorter;
import fr.inria.corese.kgram.tool.Message;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.kgram.filter.Extension.FunMap;
import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.federate.FederateVisitor;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.visitor.MetadataVisitor;
import java.io.IOException;
import java.util.HashMap;

/**
 * Compiler of SPARQL AST to KGRAM Exp Query Use Corese SPARQL parser Use an
 * abstract compiler to generate target edge/node/filter implementations
 *
 * sub query compiled as distinct edge/node to avoid inappropriate type
 * inference on nodes
 *
 *
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class Transformer implements ExpType {

    /**
     * @return the algebra
     */
    public boolean isAlgebra() {
        return algebra;
    }

    /**
     * @param algebra the algebra to set
     */
    public void setAlgebra(boolean algebra) {
        this.algebra = algebra;
    }

    /**
     * @return the BGP
     */
    public boolean isBGP() {
        return isBGP;
    }

    /**
     * @param BGP the BGP to set
     */
    public void setBGP(boolean BGP) {
        this.isBGP = BGP;
    }

    private static Logger logger = LoggerFactory.getLogger(Transformer.class);
    public static final String ROOT = "?_kgram_";
    public static final String THIS = "?this";
    private static final String EXTENSION = Processor.KGEXTENSION;
    private static final String EXT_NAMESPACE = NSManager.KGEXT;
    private static final String EXT_NAMESPACE_QUERY = NSManager.KGEXTCONS;
    static HashMap<String, String> loaded;
    public static final String FEDERATE = NSManager.KGRAM + "federate";
    int count = 0;
    CompilerFactory fac;
    Compiler compiler;
    private QuerySolver sparql;
    List<QueryVisitor> visit;
    private List<Atom> serviceList;
    Sorter sort;
    //Table table;
    ASTQuery ast;
    Checker check;
    HashMap<Edge, Query> table;
    ArrayList<Query> subQueryList;
    int ncount = 0, rcount = 0;
    boolean fail = false,
            isSPARQLCompliant = true,
            isSPARQL1 = true;
    private boolean isUseBind = true;
    private boolean isGenerateMain = true;
    private boolean isLoadFunction = false;
    private boolean isBGP = false;
    private boolean algebra = false;
    String namespaces, base;
    private Dataset dataset;
    private Metadata metadata;
    BasicGraphPattern pragma;
    private int planner = Query.STD_PLAN;

    static {
        loaded = new HashMap();
        create().init();
    }

    Transformer() {
        table = new HashMap<Edge, Query>();
        // new
        fac = new CompilerFacKgram();
        compiler = fac.newInstance();
        subQueryList = new ArrayList<Query>();
    }

    Transformer(CompilerFactory f) {
        this();
        fac = f;
        compiler = fac.newInstance();
    }

    public static Transformer create(CompilerFactory f) {
        return new Transformer(f);
    }

    public static Transformer create() {
        return new Transformer();
    }

    /**
     * Predefined extension functions for SPARQL functions
     */
    void init() {
        if (Processor.getAST() != null) {
            Query q = transform(Processor.getAST());
        }
    }

    public void set(Dataset ds) {
        if (ds != null) {
            dataset = ds;
        }
    }

    public void setMetadata(Metadata m) {
        metadata = m;
    }

    public void set(Sorter s) {
        sort = s;
    }

    public void add(QueryVisitor v) {
        if (visit == null) {
            visit = new ArrayList<QueryVisitor>();
        }
        visit.add(v);
    }

    public void add(List<QueryVisitor> v) {
        if (visit == null) {
            visit = new ArrayList<QueryVisitor>();
        }
        visit.addAll(v);
    }

    public Query transform(String squery) throws EngineException {
        return transform(squery, false);
    }

    public Query transform(String squery, boolean isRule) throws EngineException {

        ast = ASTQuery.create(squery);
        ast.setRule(isRule);
        ast.setDefaultNamespaces(namespaces);
        ast.setDefaultBase(base);
        ast.setSPARQLCompliant(isSPARQLCompliant);

        if (dataset != null) {
            ast.setDefaultDataset(dataset);
        }

        ParserSparql1.create(ast).parse();
        Query q = transform(ast);

        return q;

    }

    /**
     * Transform for a (outer) query (not a subquery)
     */
    public Query transform(ASTQuery ast) {
        this.ast = ast;
        ast.setSPARQLCompliant(isSPARQLCompliant);
        if (isSPARQLCompliant) {
            ast.getDataset().complete();
        }
        //new
        compiler.setAST(ast);
        annotate(ast);
        Pragma p = new Pragma(this, ast);
        if (ast.getPragma() != null) {
            p.compile();
        }
        if (pragma != null) {
            p.compile(pragma);
        }

        generateMain();

        if (ast.isDescribe()) {
            // need to collect select * before compiling 
            ast.validate();
        }

        // compile describe
        ast.compile();

        // type check:
        // check scope for bind()
        ast.validate();

        federate(ast);
        visit(ast);
        
        // from extended named graph
        preprocess(ast);
        template(ast);

        Query q = compile(ast);
        q.setRule(ast.isRule());
        q.setAlgebra(isAlgebra());
        if (ast.getContext() != null) {
            q.setContext(ast.getContext());
        }
        if (ast.getTemplateVisitor() != null) {
            q.setTemplateVisitor(ast.getTemplateVisitor());
        }
        template(q, ast);
        // compile select filters
        q = transform(q, ast);
        
//        compileFunction(ast);
        compileLambda(q, ast);
        
        error(q, ast);
        
        toJava(ast);
        
        metadata(ast, q);
        
        return q;
    }
    
    
    void metadata(ASTQuery ast, Query q){
        if (ast.hasMetadata(Metadata.TRACE)){
            System.out.println(ast);
        }
        if (ast.hasMetadata(Metadata.TEST)){
            q.setTest(true);
        }
    }
    
    void toJava(ASTQuery ast){
        if (ast.hasMetadata(Metadata.COMPILE)){
            String name = ast.getMetadata().getValue(Metadata.COMPILE);
            JavaCompiler jc = new JavaCompiler(name);
            try {
                jc.toJava(ast);
                jc.write();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    void visit(ASTQuery ast){
        visitor(ast);
         if (visit != null) {
            for (QueryVisitor v : visit) {
                v.visit(ast);
            }
        }
    }
    
    /**
     * Metadata => Visitor
     */
    void visitor(ASTQuery ast) {
        if (ast.hasMetadata(Metadata.METADATA)){
            add(new MetadataVisitor());
        }
        if (ast.hasMetadata(Metadata.VISITOR)) {
            for (String name : ast.getMetadata().getValues(Metadata.VISITOR)) {
                try {
                    Class visClass = Class.forName(name);
                    Object obj = visClass.newInstance();
                    if (obj instanceof QueryVisitor) {
                        add((QueryVisitor) obj);
                    }
                    else {
                        logger.error("Undefined QueryVisitor: " + name);
                    }
                } catch (ClassNotFoundException ex) {
                    logger.error("Undefined QueryVisitor: " + name);
                } catch (InstantiationException ex) {
                    logger.error("Undefined QueryVisitor: " + name);
                } catch (IllegalAccessException ex) {
                    logger.error("Undefined QueryVisitor: " + name);
                }
            }
        }
    }
    
    /**
     * use case: 
     * @service <s1> 
     * @service <s2> 
     * select * where { }
     * Rewrite every triple t as: service <s1> <s2> { t }
     */
    void federate(ASTQuery ast){
        if (ast.getServiceList() == null && getServiceList() != null){
            ast.setServiceList(getServiceList());
            if (ast.getServiceList().size() == 1){
                ast.defService(ast.getServiceList().get(0).getLabel());
            }
        }
        if (ast.getServiceList() != null && ast.getServiceList().size() > 1) {
            ast.defService(null);
            //add(new ServiceVisitor());
            add(new FederateVisitor(getSPARQLEngine()));
        }
    }

    void annotate(ASTQuery ast) {
        if (metadata != null) {
            ast.addMetadata(metadata);
        }
        annotateLocal(ast);
    }
    
    void annotateLocal(ASTQuery ast){
        if (ast.hasMetadata(Metadata.ALGEBRA)){
            // @algebra use case with @db
            setBGP(true);
            setAlgebra(true);
        }
    }

    /**
     * function xt:main(){} -> select (xt:main() as ?main)
     */
    void generateMain() {
        if (isGenerateMain) {
            Expr exp = ast.getDefine().get(Processor.XT_MAIN, 0);
            if (exp != null) {
                ast.defSelect(new Variable(ASTQuery.MAIN_VAR),
                        ast.createFunction(Processor.FUN_XT_MAIN));
            }
        }
    }

    /**
     * select * from eng:describe where { BGP } -> select * where { graph
     * eng:describe { BGP } }
     */
    void preprocess(ASTQuery ast) {
        if (ast.getFrom().size() == 1
                && isSystemGraph(ast.getFrom().get(0).getLabel())) {
            Source exp = Source.create(ast.getFrom().get(0), ast.getQueryExp());
            ast.setQuery(BasicGraphPattern.create(exp));
        }
    }

    /**
     * Optimize values in template Insert values in template body in case the
     * ?in variable is bound hence it is more efficient to verify values
     * according to ?in binding instead of testing values blindly use case:
     * template where { ?in a ?t EXP } values ?t { list of values } rewritten
     * as: template where { ?in a ?t values ?t { list of values } EXP }
     */
    void template(ASTQuery ast) {
        if (!ast.isTemplate()) {
            return;
        }
        fr.inria.corese.sparql.triple.parser.Exp body = ast.getBody();
        if (ast.getValues() != null
                && body.size() > 0
                && body instanceof BasicGraphPattern
                && body.get(0).isTriple()
                && !body.get(0).isFilter()) {

            Triple t = body.get(0).getTriple();

            if (bound(ast.getValues(), t)) {
                body.add(1, ast.getValues());
                ast.getValues().setMoved(true);
            }
        }
    }

    boolean bound(Values values, Triple t) {
        if (!t.getArg(1).isVariable()) {
            return false;
        }
        for (Variable var : values.getVariables()) {
            if (var.equals(t.getArg(1))) {
                return true;
            }
        }
        return false;
    }

    private void template(Query q, ASTQuery ast) {
        if (ast.isTemplate()) {
            q.setTemplate(true);
            q.setProfile(ast.getProfile());
            q.setAllResult(ast.isAllResult());

            if (ast.getName() != null) {
                q.setName(ast.getName());
            }

            ast.getTemplateGroup().compile(ast);
            q.setTemplateGroup(Exp.create(FILTER, ast.getTemplateGroup()));

            Term nl = Term.function(Processor.STL_NL);
            nl.compile(ast);
            q.setTemplateNL(Exp.create(FILTER, nl));

            for (Variable var : ast.getArgList()) {
                Node node = compiler.createNode(var);
                q.defArg(node);
            }
            q.setPriority(ast.getPriority());
        }
    }

    /**
     * Also used by QueryGraph to compile RDF Graph as a Query
     */
    public Query transform(Query q, ASTQuery ast) {

        compiler.setAST(ast);

        if (ast.isConstruct() || ast.isDescribe()) {
            construct(q, ast);
        }

        if (ast.isDelete()) {
            validate(ast.getDelete(), ast);
            Exp del = delete(ast);
            q.setDelete(del);
            q.setDelete(true);
        }

        if (ast.isUpdate()) {
            q.setUpdate(true);
        }

        // retrieve select nodes for query:
        complete(q, ast);

        having(q, ast);

        if (ast.isRule()) {
            new VisitQuery(compiler).visit(q);
        }

        if (compiler.isFail() || fail) {
            q.setFail(true);
        }

        q.setSort(ast.isSorted());
        q.setDebug(ast.isDebug());
        q.setCheck(ast.isCheck());
        q.setRelax(ast.isMore());
        q.setPlanProfile(getPlanProfile());

        for (Edge edge : table.keySet()) {
            q.set(edge, table.get(edge));
        }

        filters(q);
        relax(q);
        new QueryProfile(q).profile();
       
        define(q, ast);
        
        q.setSubQueryList(subQueryList);
        if (visit != null) {
            for (QueryVisitor v : visit) {
                v.visit(q);
            }
        }

        return q;
    }

    /**
     * defined functions use case: transformation profile PRAGMA: expressions
     * have declared local variables (see ASTQuery Processor)
     */
    void define(Query q, ASTQuery ast) {
        if (ast.getDefine() == null || ast.getDefine().isEmpty()) {
            return;
        }
        if (ast.isUserQuery()) {
            System.out.println("Compiler: extension function not available in server mode");
            return;
        }
       
        define(ast.getDefine(), q);
    }

    void compileFunction(Query q, ASTQuery ast) {
        compileFunction(q, ast, ast.getDefine());
    }
    
    void compileLambda(Query q, ASTQuery ast) {
        compileFunction(q, ast, ast.getDefineLambda());      
        define(ast.getDefineLambda(), q);
    }
    
    void compileFunction(Query q, ASTQuery ast, ASTExtension ext) {
        for (Function fun : ext.getFunList()) {
            compileFunction(q, ast, fun);
        }
    }
    
    void compileFunction(Query q, ASTQuery ast, Function fun) {
        fun.compile(ast);
        compileExist(fun, false);
        q.defineFunction(fun);
    }

    void error(Query q, ASTQuery ast) {
        if (ast.isFail()) {
            q.setFail(true);
        }
        if (ast.isTemplate()) {
            // TODO: because template st:profile may not have been read yet ...
            return;
        }
        undefinedFunction(q, ast);
    }

    void undefinedFunction(Query q, ASTQuery ast) {
        for (Expression exp : ast.getUndefined().values()) {
            boolean ok = Interpreter.isDefined(exp);
            if (ok) {
                return;
            } else {
                ok = !ast.isUserQuery()
                        && (isLinkedFunction() || ast.hasMetadata(Metadata.IMPORT))
                        && importFunction(q, exp);
                if (!ok) {
                    ast.addError("undefined expression: " + exp);
                }
            }
        }
    }

    /**
     */
//    boolean importFunction2(Query q, Expression exp) {
//        String path = NSManager.namespace(exp.getLabel());
//        if (loaded.containsKey(path)) {
//            return true;
//        }
//        loaded.put(path, path);
//        if (q.isDebug()) {
//            System.out.println("Transformer: load " + exp.getLabel());
//        }
//
//        Query imp = sparql.load(exp.getLabel());
//
//        if (imp != null && imp.hasDefinition()) {
//            // loaded functions are exported in Interpreter  
//            definePublic(imp.getExtension(), imp);
//            return Interpreter.isDefined(exp);
//        }
//        return false;
//    }
    
    boolean importFunction(Query q, Expression exp) {
        boolean b = getLinkedFunction(exp.getLabel());
        if (b) {
            return Interpreter.isDefined(exp);
        }
        return false;
    }
    
    public boolean getLinkedFunction(String label) {
        if (! isLinkedFunction()){
            return false;
        }
        String path = NSManager.namespace(label);
        if (loaded.containsKey(path)) {
            return true;
        }
        loaded.put(path, path);
        Query imp = sparql.load(label);
        if (imp != null && imp.hasDefinition()) {
            // loaded functions are exported in Interpreter  
            definePublic(imp.getExtension(), imp);
            return true;
        }
        return false;
    }
    

    /**
     * Define function into Extension Export into Interpreter
     */
    void define(ASTExtension aext,  Query q) {
        Extension ext = q.getCreateExtension(); 
        DatatypeHierarchy dh = new DatatypeHierarchy();
        if (q.isDebug()) dh.setDebug(true);
        ext.setHierarchy(dh);
        
        for (ASTFunMap m : aext.getMaps()) {
            for (Function exp : m.values()) {
                ext.define(exp);
                if (exp.isPublic()) {
                    definePublic(exp, q);
                }
            }
        }
    }
      
    // TODO: check isSystem() because it is exported
    /**
     * ext is loaded function definitions define them as public
     *
     * @param ext
     * @param q
     */
    void definePublic(Extension ext, Query q) {
        definePublic(ext, q, true);
    }

    /**
     * isDefine = true means export to Interpreter Use case: Transformation
     * st:profile does not export to Interpreter hence it uses isDefine = false
     */
    public void definePublic(Extension ext, Query q, boolean isDefine) {
        for (FunMap m : ext.getMaps()) {
            for (Expr exp : m.values()) {
                Function e = (Function) exp;
                definePublic(e, q, isDefine);
            }
        }
    }

    void definePublic(Function fun, Query q) {
        definePublic(fun, q, true);
    }

    void definePublic(Function fun, Query q, boolean isDefine) {
        if (isDefine) {
            Interpreter.define(fun);
        }
        fun.setPublic(true);
        if (fun.isSystem()) {
            // export function with exists {} 
            fun.getTerm().setPattern(q);
        }
    }

    void construct(Query q, ASTQuery ast) {
        validate(ast.getInsert(), ast);
        //Exp cons = construct(ast);
        Exp cons = compile(ast.getInsert(), false);
        q.setConstruct(cons);
        q.setConstruct(true);

        q.setConstructNodes(cons.getNodes());
    }

    /**
     * For query and subquery Generate a new compiler for each (sub) query in
     * order to get fresh new nodes
     */
    Query compile(ASTQuery ast) {
        //compileFunction(ast);
        Exp ee = compile(ast.getExtBody(), false);
        Query q = Query.create(ee);
        q.setUseBind(isUseBind);
        //compileFunction(ast);
        compileFunction(q, ast);
        q.setAST(ast);
        q.setHasFunctional(ast.hasFunctional());
        q.setService(ast.getService());
        // use same compiler
        values(q, ast);
        path(q, ast);

        return q;

    }

    /**
     * Subquery is a construct where
     */
    Query constructQuery(ASTQuery ast) {
        Transformer t = Transformer.create();
        t.setAlgebra(isAlgebra());
        return t.transform(ast);
    }

    /**
     * subquery is compiled using a new compiler to get fresh new nodes to
     * prevent type inference on nodes between outer and sub queries
     */
    Query compileQuery(ASTQuery ast) {
        // new
        Compiler save = compiler;
        compiler = fac.newInstance();
        compiler.setAST(ast);

        Query q = compile(ast);
        subQueryList.add(q);
        // complete select, order by, group by
        complete(q, ast);
        having(q, ast);

        // bind is compiled as subquery
        q.setBind(ast.isBind());
        q.setRelax(ast.isMore());
        new QueryProfile(q).profile();
        if (save != null) {
            compiler = save;
        }

        return q;
    }

    Exp compileBind(ASTQuery ast, Binding b) {
        return compileBind(ast, b.getFilter(), b.getVariable());
    }

    Exp compileBind(ASTQuery ast, Expression e, Variable var) {
        Filter f = compileSelect(e, ast);
        Node node = compiler.createNode(var);
        Exp exp = Exp.create(BIND);
        exp.setFilter(f);
        exp.setNode(node);
        exp.setFunctional(f.isFunctional());
        ast.setHasFunctional(f.isFunctional());
        function(null, exp, var);
        return exp;
    }

    /**
     * Delete/Insert/Construct
     */
    Exp compile(ASTQuery ast, fr.inria.corese.sparql.triple.parser.Exp exp) {
        Compiler save = compiler;
        compiler = fac.newInstance();
        compiler.setAST(ast);
        Exp ee = compile(exp, false);

        if (save != null) {
            compiler = save;
        }
        return ee;
    }

    /**
     * Compile service as a subquery if it is a pattern and as a subquery if it
     * already is one
     */
    Exp compileService(Service service) {
        Node src = compile(service.getServiceName());
        Exp node = Exp.create(NODE, src);
              
        fr.inria.corese.sparql.triple.parser.Exp body = service.get(0);
        ASTQuery aa;

        if (body.isBGP() && body.size() == 1 && body.get(0).isQuery()) {
            // service body is a subquery
            aa = body.get(0).getQuery();
        } else {
            // service body is a pattern
            aa = ast.subCreate();
            aa.setSelectAll(true);
            // PRAGMA: body must be a BGP
            aa.setBody(body);
            //aa.setBody(BasicGraphPattern.create(body));
        }
        //collect select * nodes
        aa.validate();
        Query q = compileQuery(aa);
        q.setService(true);
        q.setSilent(service.isSilent());

        Exp exp = Exp.create(SERVICE, node, q);
        exp.setSilent(service.isSilent());
        
        if (service.getServiceList().size() > 1) {
            // special case for federated query
            // one service clause with several URI
            // perform merge of resut Mappings
            ArrayList<Node> list = new ArrayList<Node>();
            for (Atom at : service.getServiceList()) {
                Node serv = compile(at);
                list.add(serv);
            }
            exp.setNodeSet(list);
        }
        
        return exp;
    }

    Query create(Exp exp) {
        Query q = Query.create(exp);
        if (sort != null) {
            q.set(sort);
        }
        return q;
    }

    public boolean isSPARQLCompliant() {
        return isSPARQLCompliant;
    }

    public void setSPARQLCompliant(boolean b) {
        isSPARQLCompliant = b;
    }

    public void setNamespaces(String ns) {
        namespaces = ns;
    }

    public void setPragma(BasicGraphPattern p) {
        pragma = p;
    }

    public void setBase(String ns) {
        base = ns;
    }

    public void setSPARQL1(boolean b) {
        isSPARQL1 = b;
    }

    void values(Query q, ASTQuery ast){
        if (ast.getValues() == null) {
            return;
        }
        bindings(q, ast);
        if (q.getValues() != null && isAlgebra()){
            if (q.getBody().size() == 0){
                q.setBody(q.getValues());
            }
            else {
                Exp exp = Exp.create(JOIN, Exp.create(BGP, q.getValues()), q.getBody());
                q.setBody(Exp.create(BGP, exp));
            }
        }
    }
    
    void bindings(Query q, ASTQuery ast) {
        Exp bind = bindings(ast.getValues());
        if (bind == null) {
            q.setCorrect(false);
            q.addError("Value Bindings: ", "#values != #variables");
        } else {
            q.setValues(bind);
            if (ast.getValues().isMoved()) {
                //q.setTemplateMappings(bind.getMappings());
                q.getValues().setPostpone(true);
            } 
//            else {
//                q.setMappings(bind.getMappings());
//                q.setBindingNodes(bind.getNodeList());
//            }
        }
    }

    Exp bindings(Values values) {
        List<Node> lNode = bind(values);
        Node[] nodes = getNodes(lNode);

        Mappings lMap = new Mappings();

        for (List<Constant> lVal : values.getValues()) {
            if (values.getVariables().size() != lVal.size()) {
                // error: not right number of values
                return null;
            } else {
                List<Node> list = bind(lVal);
                Mapping map = create(nodes, list);
                lMap.add(map);
            }
        }

        Exp bind = Exp.create(VALUES);
        bind.setNodeList(lNode);
        bind.setMappings(lMap);
        return bind;
    }

    List<Node> bind(Values values) {

        List<Node> lNode = new ArrayList<Node>();

        for (Variable var : values.getVariables()) {
            Node qNode = compiler.createNode(var);
            lNode.add(qNode);
        }

        return lNode;
    }

    Node[] getNodes(List<Node> lNode) {
        Node[] nodes = new Node[lNode.size()];
        lNode.toArray(nodes);
        return nodes;
    }

    List<Node> bind(List<Constant> lVal) {
        List<Node> lNode = new ArrayList<Node>();
        for (Constant val : lVal) {
            Node node = null;
            if (val != null) {
                node = compiler.createNode(val);
            }
            lNode.add(node);
        }
        return lNode;
    }

    Mapping create(Node[] lNode, List<Node> lVal) {
        Node[] nodes = new Node[lVal.size()];
        lVal.toArray(nodes);
        return Mapping.safeCreate(lNode, nodes);
    }

    Exp construct(ASTQuery ast) {
        return compile(ast, ast.getInsert());
    }

    Exp delete(ASTQuery ast) {
        return compile(ast, ast.getDelete());
    }

    public ASTQuery getAST() {
        return ast;
    }

    public Compiler getCompiler() {
        return compiler;
    }

    void complete(Query qCurrent, ASTQuery ast) {
        qCurrent.collect();
        //qCurrent.setSelectFun(select(qCurrent, ast));
        select(qCurrent, ast);
        qCurrent.setOrderBy(orderBy(qCurrent, ast));
        qCurrent.setGroupBy(groupBy(qCurrent, ast));

        qCurrent.setDistinct(ast.isDistinct());
        // generate a DISTINCT(?x) for distinct ?x
        qCurrent.distinct();
        qCurrent.setFrom(nodes(ast.getActualFrom()));
        qCurrent.setNamed(nodes(ast.getActualNamed()));

        // sort from uri to speed up verification at query time 
        // Producer may use dichotomy
        qCurrent.setFrom(sort(qCurrent.getFrom()));
        qCurrent.setNamed(sort(qCurrent.getNamed()));

        qCurrent.setLimit(Math.min(ast.getMaxResult(), ast.getMaxProjection()));
        qCurrent.setOffset(ast.getOffset());

        qCurrent.setGraphNode(createNode());

        if (qCurrent.isCorrect()) {
            // check semantics of select vs aggregates and group by
            boolean correct = qCurrent.check();
            if (!correct) {
                qCurrent.setCorrect(false);
            } else {
                qCurrent.setCorrect(ast.isCorrect());
            }
        }

    }

    void path(Query q, ASTQuery ast) {
        if (ast.getRegexTest().size() > 0) {
            Node node = compiler.createNode(Variable.create(THIS));
            q.setPathNode(node);
        }
        for (Expression test : ast.getRegexTest()) {
            // ?x c:isMemberOf[?this != <inria>] + ?y
            Filter f = compile(test);
            q.addPathFilter(f);
        }
    }

    void having(Query q, ASTQuery ast) {
        if (ast.getHaving() != null) {
            //Filter having = compile(ast.getHaving());			
            Filter having = compileSelect(ast.getHaving(), ast);
            q.setHaving(Exp.create(FILTER, having));
        }
    }

    /**
     * Retrieve/Compute the nodes for the select of qCurrent Query Nodes may be
     * std Node or select fun() as ?var node in this last case we may create a
     * node from scratch for ?var This function is called - once for each
     * subquery - once for the global query
     */
    List<Exp> select(Query qCurrent, ASTQuery ast) {
        List<Exp> select = new ArrayList<Exp>();
        // list of query nodes created for variables in filter that need
        // an outer node value
        List<Node> lNodes = new ArrayList<Node>();

        if (ast.isSelectAll() || ast.isConstruct()) {
            // select *
            // get nodes from query nodes and edges
            select = qCurrent.getSelectNodesExp();
        }

        qCurrent.setSelectFun(select);

        for (Variable var : ast.getSelectVar()) {
            // retrieve var node from query
            String varName = var.getName();
            Node node = getNode(qCurrent, var);
            Exp exp = Exp.create(NODE, node);

            // process filter if any
            Expression ee = ast.getExpression(varName);

            if (ee != null) {
                // select fun() as var
                Filter f = compileSelect(ee, ast);

                if (f != null) {
                    // select fun() as var
                    exp.setFilter(f);
                    checkFilterVariables(qCurrent, f, select, lNodes);
                    function(qCurrent, exp, var);
                    aggregate(qCurrent, exp, ee, select);
                }
            }

            // TODO: check var in select * to avoid duplicates

            //select.add(exp);
            add(select, exp);

            if (lNodes.contains(exp.getNode())) {
                // undef variable of former exp is current exp as var
                lNodes.remove(exp.getNode());
            }
        }

        for (Node node : lNodes) {
            // additional variables for exp in select (exp as var)
            Exp exp = Exp.create(NODE, node);
            exp.status(true);
            select.add(exp);
        }

        qCurrent.setSelectWithExp(select);
        return select;
    }

    /**
     * select * (exp as var) if var is already in select *, add exp to var
     */
    void add(List<Exp> select, Exp exp) {
        boolean found = false;

        for (Exp e : select) {
            if (e.getNode().same(exp.getNode())) {
                if (exp.getFilter() != null) {
                    e.setFilter(exp.getFilter());
                }
                found = true;
                break;
            }
        }

        if (!found) {
            select.add(exp);
        }
    }

    void aggregate(Query qCurrent, Exp exp, Expression ee, List<Exp> list) {
        if (exp.isAggregate()) {
            // process  min(?l, groupBy(?x, ?y))
            extendAggregate(qCurrent, exp, ee);
        } else {
            // check if exp has a variable that is computed by a previous aggregate
            // if yes, exp is also considered as an aggregate
            checkAggregate(exp, list);
        }
    }

    /**
     * use case: select (count(?x) as ?c) (?c + ?c as ?d) check that ?c is an
     * aggregate variable set ?c + ?c as aggregate
     */
    void checkAggregate(Exp exp, List<Exp> select) {
        List<String> list = exp.getFilter().getVariables();

        for (Exp ee : select) {
            if (ee.isAggregate()) {
                String name = ee.getNode().getLabel();
                if (list.contains(name)) {
                    exp.setAggregate(true);
                    break;
                }
            }
        }
    }

    /**
     * min(?l, groupBy(?x, ?y))
     */
    void extendAggregate(Query qCurrent, Exp exp, Expression ee) {
        if (ee.isAggregate() && ee.arity() > 1) {
            Expression g = ee.getArg(ee.arity() - 1);
            if (g.oper() == ExprType.GROUPBY) {
                List<Exp> ob = orderBy(qCurrent, g.getArgs(), ast);
                exp.setExpGroupBy(ob);
            }
        }
    }

    Node getNode(Query qCurrent, Variable var) {
        Node node = null;
        if (qCurrent != null) {
            node = getProperAndSubSelectNode(qCurrent, var.getName());
        }
        if (node == null) {
            node = compiler.createNode(var);
        }
        return node;
    }

    ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }

    Node getProperAndSubSelectNode(Query q, String name) {
        Node node;
        if (Query.test) {
            node = q.getSelectNodes(name);
        } else {
            node = q.getProperAndSubSelectNode(name);
        }
        return node;
    }

    /**
     * If filter isFunctionnal() create it's query node list
     */
    void function(Query qCurrent, Exp exp, Variable var) {
        if (exp.getFilter().isFunctional()) {
            if (var.getVariableList() != null) {
                // sql() as (?x, ?y)
                for (Variable vv : var.getVariableList()) {
                    Node qNode = getNode(qCurrent, vv);
                    exp.addNode(qNode);
                }
            } else {
                exp.addNode(exp.getNode());
            }
        }
    }

    /**
     * Check that variables in filter have corresponding proper node otherwise
     * create a Node to import value from outer query
     *
     * @param query
     * @param f
     */
    void checkFilterVariables(Query query, Filter f, List<Exp> select, List<Node> lNodes) {
        switch (f.getExp().oper()) {
            // do not create Node for local variables
            case ExprType.PACKAGE:
            case ExprType.STL_DEFINE:
            case ExprType.FUNCTION:
            case ExprType.LET:
            //return;
            }

        List<String> lVar = f.getVariables();
        for (String name : lVar) {
            Node node = getProperAndSubSelectNode(query, name);
            if (node == null) {
                if (!containsExp(select, name) && !containsNode(lNodes, name)) {
                    node = compiler.createNode(name);
                    lNodes.add(node);
                }
            }
        }
    }

    boolean containsExp(List<Exp> lExp, String name) {
        for (Exp exp : lExp) {
            if (exp.getNode().getLabel().equals(name)) {
                return true;
            }
        }
        return false;
    }

    boolean containsNode(List<Node> lNode, String name) {
        for (Node node : lNode) {
            if (node.getLabel().equals(name)) {
                return true;
            }
        }
        return false;
    }

    List<Exp> orderBy(Query qCurrent, ASTQuery ast) {
        List<Exp> order = orderBy(qCurrent, ast.getSort(), ast);
        if (order.size() > 0) {
            int n = 0;
            for (boolean b : ast.getReverse()) {
                order.get(n).status(b);
                n++;
            }
        }
        return order;
    }

    List<Exp> groupBy(Query qCurrent, ASTQuery ast) {
        List<Exp> list = orderBy(qCurrent, ast.getGroupBy(), ast);
        qCurrent.setConnect(ast.isConnex());
        return list;
    }

    List<Exp> orderBy(Query qCurrent, List<Expression> input, ASTQuery ast) {
        List<Exp> list = new ArrayList<Exp>();

        for (Expression ee : input) {
            if (ee.isVariable()) {
                Exp exp = qCurrent.getSelectExp(ee.getName());
                Node node;

                if (exp != null) {
                    node = exp.getNode();
                } else {
                    node = getProperAndSubSelectNode(qCurrent, ee.getName());
                }

                if (node == null) {
                    ast.addError("OrderBy GroupBy Undefined exp: ", ee);
                    node = compiler.createNode(ee.getName());
                }
                Exp e = Exp.create(NODE, node);

                if (exp != null && exp.isAggregate()) {
                    // order by ?count
                    e.setAggregate(true);
                }
                list.add(e);
                //}
            } else {
                // order by fun(?x)
                // TODO: check rewrite fun() as var
                Filter f = compile(ee);
                Node node = createNode();
                Exp exp = Exp.create(NODE, node);
                exp.setFilter(f);
                list.add(exp);
            }
        }
        return list;
    }

    /**
     * Create a fake query node
     */
    Node createNode() {
        String name = getVarName();
        Node node = compiler.createNode(name);
        return node;
    }

    String getVarName() {
        return ROOT + count++;
    }

    List<Node> nodes(List<Constant> from) {
        List<Node> nodes = new ArrayList<Node>();
        for (Constant cst : from) {
            nodes.add(new NodeImpl(cst));
        }
        return nodes;
    }

    List<Node> sort(List<Node> list) {
        Collections.sort(list, new Comparator<Node>() {
            public int compare(Node o1, Node o2) {
                return o1.compare(o2);
            }
        });
        return list;
    }

    /**
     * Compile AST statements into KGRAM statements Compile triple into Edge,
     * filter into Filter
     */
    Exp compile(fr.inria.corese.sparql.triple.parser.Exp query, boolean opt) {
        return compile(query, opt, 0);
    }

    Exp compile(fr.inria.corese.sparql.triple.parser.Exp query, boolean opt, int level) {

        Exp exp = null;
        int type = getType(query);
        opt = opt || isOption(type);

        switch (type) {

            case FILTER:
                exp = compileFilter((Triple) query, opt);
                break;

            case EDGE:
                exp = compileEdge((Triple) query, opt);
                break;

            case QUERY:
                if (query.getQuery().isConstruct()) {
                    exp = constructQuery(query.getQuery());
                } else {
                    exp = compileQuery(query.getQuery());
                }
                break;

            case BIND:
                exp = compileBind(ast, (Binding) query);
                break;

            case SERVICE:
                exp = compileService((Service) query);
                break;

            case VALUES:
                Values val = (Values) query;
                if (val.hasExpression()) {
                    exp = compile(val.getBind(), opt, level);
                } else {
                    exp = bindings(val);
                    if (exp == null) {
                        // TODO:
                        //logger.error("** Value Bindings: #values != #variables");
                        ast.setFail(true);
                        ast.addError("Value Bindings: ", "#values != #variables");
                        return null;
                    }
                }
                break;

            default:

                /**
                 * ************************
                 *
                 * Compile Body
                 *
                 *************************
                 */
                exp = Exp.create(cpType(type));

                boolean hasBind = false;

                for (fr.inria.corese.sparql.triple.parser.Exp ee : query.getBody()) {

                    Exp cpl = compile(ee, opt, level + 1);

                    if (cpl != null) {

                        if (cpl.isGraph() && cpl.getBind() != null) {
                            // see compileGraph()
                            exp.add(cpl.getBind());
                            cpl.setBind(null);
                        }

//                        if (ee.isScope()) {
//                            // add AND as a whole
//                            exp.add(cpl);
//
//                        } else 
                            if (isJoinable(exp, ee)) {
                            exp.join(cpl, bgpType());
                        } else {
                            // add elements of AND one by one
                            exp.insert(cpl);
                        }
                    }
                }

                // PRAGMA: do it after loop above to have filter compiled
                query.validateBlank(ast);
                
                exp = complete(exp, query, opt);
                
                if (isAlgebra() && exp.isBGP()){
                    // possibly join arguments
                    exp.dispatch();
                }
                
        }

        return exp;

    }
    
    int bgpType(){
       return (isBGP()) ? BGP : AND;
    }
    
    Exp compileEdge(Triple t, boolean opt) {
        Edge r = compiler.compile(t, ast.isInsertData());
        Exp exp = Exp.create(EDGE, r);

        if (t.isType()) {
            Exp pe = pathType(ast, t);
            pe.setSystem(true);
            exp.setPath(pe);
        }

        if (t.isXPath()) {
            // deprecated ?x xpath() ?y
            exp.setType(EVAL);
            Filter xpath = compiler.compile(t.getXPath());
            exp.setFilter(xpath);
        } else if (t.isPath()) {
            path(t, exp);
        } else if (ast.isCheck()) {
            check(t, r);
        }

        return exp;
    }

    void path(Triple tt, Exp exp) {
        exp.setType(PATH);
        Expression regex = tt.getRegex();
        if (regex == null) {
            // deprecated: there may be a match($path, regex)
        } else {
            regex.compile(ast);
            exp.setRegex(regex);
        }
        exp.setObject(tt.getMode());
    }

    /**
     *
     * Generate rdf:type/rdfs:subClassOf*
     */
    Exp pathType(ASTQuery ast, Triple t) {
        Expression re = Term.create(Term.RE_SEQ,
                ast.createQName(RDFS.rdftype),
                Term.function(Term.STAR, ast.createQName(RDFS.rdfssubclassof)));
        Triple p = ast.createPath(t.getSubject(), re, t.getObject());
        Edge e = compiler.compile(p, false);
        Exp exp = Exp.create(PATH, e);
        re.compile(ast);
        exp.setRegex(re);
        return exp;
    }

    /**
     * Complete compilation
     */
    Exp complete(Exp exp, fr.inria.corese.sparql.triple.parser.Exp srcexp, boolean opt) {
        // complete path (deprecated)
        path(exp);

        switch (getType(srcexp)) {

            case MINUS:
                // add a fake graph node 
                // use case:
                // graph ?g {PAT minus {PAT}}
                exp.setNode(createNode());
                break;

            case GRAPH:
                compileGraph(ast, exp, srcexp.getNamedGraph());
                break;
                
        }

        return exp;
    }

    /**
     * graph kg:describe BGP -> bind(kg:describe() as ?g) graph ?g BGP
     */
    Exp compileGraph(ASTQuery ast, Exp exp, Source srcexp) {
        Atom at = srcexp.getSource();
        Atom nat = getSrc(at);
        Exp gr = compileGraph(exp, nat);

        if (at != nat) {
            // generate bind(kg:describe() as var)
            Term fun = ast.createFunction(ast.createQName(EXTENSION), at.getConstant());
            Exp b = compileBind(ast, fun, nat.getVariable());
            gr.setBind(b);
        }

        return gr;
    }

    Atom getSrc(Atom at) {
        if (at.isConstant() && isSystemGraph(at.getConstant().getLabel())) {
            at = Variable.create(getVarName());
        }
        return at;
    }

    boolean isSystemGraph(String cst) {
        return (cst.startsWith(EXT_NAMESPACE)
                || cst.startsWith(EXT_NAMESPACE_QUERY));
    }

    Exp compileGraph(Exp exp, Atom at) {
        Node src = compile(at);
        // create a NODE kgram expression for graph ?g
        Exp node = Exp.create(NODE, src);
        Exp gnode = Exp.create(GRAPHNODE, node);
        exp.add(0, gnode);
        return exp;
    }

    Exp compileFilter(Triple triple, boolean opt) {
        List<Filter> qvec = compiler.compileFilter(triple);
        Exp exp;

        if (qvec.size() == 1) {
            exp = Exp.create(FILTER, qvec.get(0));
            compileExist(qvec.get(0).getExp(), opt);
        } else {
            exp = Exp.create(AND);
            for (Filter qm : qvec) {
                Exp f = Exp.create(FILTER, qm);
                compileExist(qm.getExp(), opt);
                exp.add(f);
            }
        }
        return exp;
    }

    Node compile(Atom at) {
        return compiler.createNode(at);
    }

//    void pop(Exp exp) {
//        List<Exp> list = new ArrayList<Exp>();
//        for (Exp ee : exp) {
//            if (ee.isQuery() && ee.getQuery().isBind()) {
//                list.add(Exp.create(POP, ee));
//            }
//        }
//        for (Exp ee : list) {
//            exp.insert(ee);
//        }
//    }

    /**
     * Rewrite fun() as ?var in exp Compile exists {}
     */
    Filter compile(Expression exp) {
        Filter f = compiler.compile(exp);
        compileExist(f.getExp(), false);
        return f;
    }

    /**
     * Do not rewrite fun() as var
     */
    Filter compileSelect(Expression exp, ASTQuery ast) {
        Filter f = exp.compile(ast);
        compileExist(f.getExp(), false);
        return f;
    }

    /**
     * filter(exist {PAT})
     */
    void compileExist(Expr exp, boolean opt) {
        if (exp.oper() == ExprType.EXIST) {
            Term term = (Term) exp;
            Exp pat = compile(term.getExist(), opt);
            term.setPattern(pat);
        } 
        //else 
        {
            for (Expr ee : exp.getExpList()) {
                compileExist(ee, opt);
            }
        }
    }

    /**
     * Assign pathLength($path) <= 10 to its path
     */
    void path(Exp exp) {
        for (Exp ee : exp) {
            if (ee.isPath()) {
                for (Exp ff : exp) {
                    if (ff.isFilter()) {
                        processPath(ee, ff);
                    }
                }
                if (ee.getRegex() == null) {
                    String name = ee.getEdge().getLabel();
                    Term star = Term.function(Term.STAR, Constant.create(name));
                    star.compile(ast);
                    ee.setRegex(star);
                }
            }
        }
    }

    /**
     * Check if filter f concerns path e for regex, mode, min, max store them in
     * Exp e
     */
    void processPath(Exp exp, Exp ef) {
        Filter f = ef.getFilter();
        Edge e = exp.getEdge();
        Node n = e.getEdgeVariable();

        List<String> lVar = f.getVariables();
        if (lVar.size() == 0) {
            return;
        }
        if (n == null) {
            return;
        }
        if (!n.getLabel().equals(lVar.get(0))) {
            return;
        }

        Regex regex = compiler.getRegex(f);

        if (regex != null && exp.getRegex() == null) {
            // mode: i d s
            String mode = compiler.getMode(f);
            if (mode != null) {
                exp.setObject(mode);
                if (mode.indexOf("i") != -1) {
                    regex = Term.function(Term.SEINV, ((Expression) regex));
                }
            }
            ((Expression) regex).compile(ast);
            exp.setRegex(regex);
        } else {
            if (compiler.getMin(f) != -1) {
                exp.setMin(compiler.getMin(f));
            }
            if (compiler.getMax(f) != -1) {
                exp.setMax(compiler.getMax(f));
            }
        }
    }

    boolean isOption(int type) {
        switch (type) {
            case OPTION:
            case OPTIONAL:
            case UNION:
            case MINUS:
                return true;

            default:
                return false;
        }
    }

    int getType(fr.inria.corese.sparql.triple.parser.Exp query) {
        if (query.isFilter()) {
            return FILTER;
        } else if (query.isTriple()) {
            return EDGE;
        } else if (query.isUnion()) {
            return UNION;
        } else if (query.isJoin()) {
            return JOIN;
        } else if (query.isOption()) {
            return OPTION;
        } else if (query.isOptional()) {
            return OPTIONAL;
        } else if (query.isMinus()) {
            return MINUS;
        } else if (query.isGraph()) {
            return GRAPH;
        } else if (query.isService()) {
            return SERVICE;
        } else if (query.isQuery()) {
            return QUERY;
        } else if (query.isBind()) {
            return BIND;
        } else if (query.isValues()) {
            return VALUES;
        } else if (query.isBGP()) {
            return bgpType();
        } else if (query.isAnd()) {
            return AND;
        } else {
            return EMPTY;
        }
    }

    int cpType(int type) {
        switch (type) {
            default:
                return type;
        }
    }

    /**
     * ************************************
     */
    /**
     * Generate a complementary Query that checks: definition of class/property
     */
    void check(Triple tt, Edge edge) {
        ASTQuery aa = new Checker(ast).check(tt);
        if (aa != null) {
            Transformer tr = Transformer.create();
            Query qq = tr.transform(aa);
            add(edge, qq);
        }
    }

    void add(Edge edge, Query query) {
        table.put(edge, query);
    }

    /**
     * Generate predefined system filters that may be used by kgram Filters are
     * stored in a table, we can have several predefined filters pathNode()
     * generate a blank node for each path (PathFinder)
     */
    void filters(Query q) {
        ASTQuery ast = (ASTQuery) q.getAST();

        Term t = Term.function(Processor.PATHNODE);
        q.setFilter(Query.PATHNODE, t.compile(ast));
    }

    void relax(Query q) {
        ASTQuery ast = (ASTQuery) q.getAST();
        for (Expression exp : ast.getRelax()) {
            if (exp.isConstant()) {
                Constant p = exp.getConstant();
                Node n = compiler.createNode(p);
                q.addRelax(n);
            }
        }
    }

    /**
     * *******************************************
     *
     * Get Predicate Nodes for Rule
     *
     *******************************************
     */
    /**
     * **********************************************************
     */
    /**
     * check unbound variable in construct/insert/delete
     */
    boolean validate(fr.inria.corese.sparql.triple.parser.Exp exp, ASTQuery ast) {
        boolean suc = true;

        for (fr.inria.corese.sparql.triple.parser.Exp ee : exp.getBody()) {
            boolean b = true;

            if (ee.isTriple()) {
                b = validate(ee.getTriple(), ast);
            } else if (ee.isGraph()) {
                b = validate((Source) ee, ast);
            } else {
                b = validate(ee, ast);
            }

            suc = suc && b;
        }

        return suc;
    }

    boolean validate(Source exp, ASTQuery ast) {
        boolean suc = validate(exp.getSource(), ast);

        for (fr.inria.corese.sparql.triple.parser.Exp ee : exp.getBody()) {
            suc = validate(ee, ast) && suc;
        }

        return suc;
    }

    boolean validate(Atom at, ASTQuery ast) {
        if (at.isVariable()
                && !at.isBlankNode()
                && !ast.isSelectAllVar(at.getVariable())) {
            ast.addError(Message.get(Message.UNDEF_VAR)
                    + ast.getUpdateTitle() + ": ", at.getLabel());
            return false;
        }

        return true;
    }

    boolean validate(Triple t, ASTQuery ast) {
        boolean suc = validate(t.getSubject(), ast);
        suc = validate(t.getObject(), ast) && suc;

        Variable var = t.getVariable();
        if (var != null) {
            suc = validate(var, ast) && suc;
        }

        return suc;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Must exp ee be joined with preceding statements ?
     */
    private boolean isJoinable(Exp exp, fr.inria.corese.sparql.triple.parser.Exp ee) {
        return (isAlgebra()) ? isJoinableAlgebra(exp, ee) : isJoinableBasic(ee) ;        
    }
    
    private boolean isJoinableBasic(fr.inria.corese.sparql.triple.parser.Exp ee) {
        return ee.isBGP() || ee.isUnion() ; // || ee.isGraph();
    }
    
    private boolean isJoinableAlgebra(Exp exp, fr.inria.corese.sparql.triple.parser.Exp ee) {
        return ee.isBGP() || ee.isUnion();
    }

    /**
     * @return the planner
     */
    public int getPlanProfile() {
        return planner;
    }

    /**
     * @param planner the planner to set
     */
    public void setPlanProfile(int planner) {
        this.planner = planner;
    }

    /**
     * @return the isUseBind
     */
    public boolean isUseBind() {
        return isUseBind;
    }

    /**
     * @param isUseBind the isUseBind to set
     */
    public void setUseBind(boolean isUseBind) {
        this.isUseBind = isUseBind;
    }

    /**
     * @return the isGenerateMain
     */
    public boolean isGenerateMain() {
        return isGenerateMain;
    }

    /**
     * @param isGenerateMain the isGenerateMain to set
     */
    public void setGenerateMain(boolean isGenerateMain) {
        this.isGenerateMain = isGenerateMain;
    }

    /**
     * @return the sparql
     */
    public QuerySolver getSPARQLEngine() {
        return sparql;
    }

    /**
     * @param sparql the sparql to set
     */
    public void setSPARQLEngine(QuerySolver sparql) {
        this.sparql = sparql;
    }

    /**
     * @return the LoadFunction
     */
    public boolean isLinkedFunction() {
        return isLoadFunction;
    }

    /**
     * @param LoadFunction the LoadFunction to set
     */
    public void setLinkedFunction(boolean LoadFunction) {
        this.isLoadFunction = LoadFunction;
    }

    /**
     * @return the serviceList
     */
    public List<Atom> getServiceList() {
        return serviceList;
    }

    /**
     * @param serviceList the serviceList to set
     */
    public void setServiceList(List<Atom> serviceList) {
        this.serviceList = serviceList;
    }
}
