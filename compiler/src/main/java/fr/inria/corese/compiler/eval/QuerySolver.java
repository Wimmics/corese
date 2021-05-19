package fr.inria.corese.compiler.eval;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.Dataset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Atom;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.compiler.api.QueryVisitor;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.compiler.parser.Transformer;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.api.query.SPARQLEngine;
import fr.inria.corese.kgram.core.Eval;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.Sorter;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.kgram.event.EventListener;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.tool.MetaProducer;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;

/**
 * Evaluator of SPARQL query by KGRAM Ready to use Package with KGRAM and SPARQL
 * Parser & Transformer
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class QuerySolver implements SPARQLEngine {

    private static Logger logger = LoggerFactory.getLogger(QuerySolver.class);
    public static final String MAIN_FUN = NSManager.EXT + "main";

    public static final int STD_ENTAILMENT = 0;
    public static final int RDF_ENTAILMENT = 1;
    public static final int RDFS_ENTAILMENT = 2;

//    public static final int DEFAULT_MODE = 0;
//    public static final int PROTECT_SERVER_MODE = 1;
//    static int INIT_SERVER_MODE = DEFAULT_MODE;

    private static int QUERY_PLAN = Query.QP_DEFAULT;

    public static boolean BGP_DEFAULT = false;
    public static boolean ALGEBRA_DEFAULT = false;
    private static boolean visitorable = false;

    static String NAMESPACES;
    private static Metadata defaultMetadata;

    protected EventManager manager;
    protected ResultListener listener;
    protected Producer producer;
    protected Provider provider;
    protected Interpreter evaluator;
    protected Matcher matcher;
    protected Sorter sort;
    protected List<QueryVisitor> visit;
    private List<Atom> serviceList;

    boolean isListGroup = false,
            isListPath = false,
            isCountPath = false,
            isCheckLoop = false,
            isDebug = false,
            isOptimize = false,
            isSPARQLCompliant = false;
    private boolean isGenerateMain = true;
    private boolean isSynchronized = false;
    private boolean isPathType = false;
    private boolean isStorePath = true;
    private boolean isCachePath = false;
    private boolean isRule = false;
    private boolean algebra = ALGEBRA_DEFAULT;
    private boolean isBGP = BGP_DEFAULT;
    // two blank nodes match if they have the same description
    // (their edges  and target nodes math)
    // use case: match two OWL Blank nodes that represent the same exp
    private boolean isMatchBlank = false;
    // skolemize construct/describe result graphs (and only them)
    private boolean isSkolem = false;

    private boolean isDetail = false;

    boolean isSequence = false;

    int slice = Query.DEFAULT_SLICE;
    //private int mode = INIT_SERVER_MODE;

    // set default base for SPARQL Query
    // it is overloaded if query has a base (cf prefix/base)
    // use case: expand from <data.ttl> in manifest.ttl
    String defaultBase;

    private BasicGraphPattern pragma;
    private Metadata metadata;

    static int count = 0;

    private AccessRight accessRight;

    static boolean test = true;
    private int planner = QUERY_PLAN;
    private boolean isUseBind;
    Eval current;

    public QuerySolver() {
    }

    protected QuerySolver(Producer p, Interpreter e, Matcher m) {
        producer = p;
        evaluator = e;
        matcher = m;
        setPragma(BasicGraphPattern.create());
    }

    public static QuerySolver create() {
        return new QuerySolver();
    }

    public static void setPlanDefault(int n) {
        QUERY_PLAN = n;
    }

    public void add(Producer prod) {
        MetaProducer meta;
        if (producer instanceof MetaProducer) {
            meta = (MetaProducer) producer;
        } else {
            meta = MetaProducer.create();
            meta.add(producer);
            producer = meta;
            evaluator.setProducer(producer);
        }
        meta.add(prod);
    }

    public void set(Sorter s) {
        sort = s;
    }
    
    public ContextLog getLog(Mappings map) {
        //return getAST(map).getLog();
        return getLog();
    }
    
    public ContextLog getLog() {
        return getBinding().getCreateLog();
    }

    public void setVisitor(QueryVisitor v) {
        addVisitor(v);
    }

    public void addVisitor(QueryVisitor v) {
        if (visit == null) {
            visit = new ArrayList<QueryVisitor>();
        }
        visit.add(v);
    }

    public boolean hasVisitor() {
        return visit != null && !visit.isEmpty();
    }

    public List<QueryVisitor> getVisitorList() {
        return visit;
    }

    public void set(Provider p) {
        provider = p;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setSPARQL1(boolean b) {
    }

    public static void defaultSPARQL1(boolean b) {
    }

    public void setDefaultBase(String str) {
        setBase(str);
    }

    public void setBase(String str) {
        defaultBase = str;
    }

    public String getBase() {
        return defaultBase;
    }

   public Transformer transformer() {
        Transformer transformer = Transformer.create();
        transformer.setSPARQLEngine(this);
        if (sort != null) {
            transformer.set(sort);
        }
        if (getVisitorList() != null) {
            transformer.add(getVisitorList());
        }
        return transformer;
    }

    /**
     * Does not perform construct {} if any it return the Mappings in this case
     */
    public Mappings basicQuery(ASTQuery ast) throws EngineException {
        return basicQuery(ast, null, null);
    }

    public Mappings basicQuery(ASTQuery ast, Mapping m, Dataset ds) throws EngineException {
        Query query = compile(ast, ds);
        return query(query, m);
    }

    public Query compile(ASTQuery ast, Dataset ds) throws EngineException {
        if (ds != null) {
            ast.setDefaultDataset(ds);
        }
        Transformer transformer = transformer();
        return transformer.transform(ast);
    }

    public Mappings query(String squery) throws EngineException {
        return query(squery, null, null);
    }

    public Mappings query(String squery, Mapping map) throws EngineException {
        return query(squery, map, null);
    }

    public Mappings query(String squery, Mapping map, Dataset ds) throws EngineException {
        Query query = compile(squery, ds);
        return query(query, map);
    }
    
    public Mappings query(Query query) throws EngineException {
        return query(query, null);
    }


//    public Mappings eval(Query query) throws EngineException {
//        return query((Node) null, query, null);
//    }

    @Override
    public Mappings eval(Query query, Mapping m, Producer p) throws SparqlException {
        return query((Node) null, query, m);
    }

    @Override
    public Mappings eval(Node gNode, Query query, Mapping m, Producer p) throws SparqlException {
        return query(gNode, query, m);
    }

//    public Mappings eval(Query query, Mapping m) throws EngineException {
//        return query((Node) null, query, m);
//    }

    /**
     * Core QueryExec processor
     */
    public Mappings query(Query query, Mapping m) throws EngineException {
        return query((Node) null, query, m);
    }

    /**
     * Main query function
     */
    public Mappings query(Node gNode, Query query, Mapping m) throws EngineException {
        init(query);
        debug(query);

        if (getProducer() instanceof MetaProducer) {
            query.setDistribute(true);
        }

        Eval kgram = makeEval();
        setEval(kgram);

        events(kgram);
        pragma(kgram, query);
        tune(kgram, query, m);

        before(query);
        Mappings map;
        try {
            map = kgram.query(gNode, query, m);
        } catch (SparqlException ex) {
            throw EngineException.cast(ex);
        }
        //TODO: check memory usage when storing Eval
        map.setEval(kgram);
        after(map);

        return map;
    }

    void before(Query q) {
        ASTQuery ast = (ASTQuery) q.getAST();
        for (fr.inria.corese.sparql.api.QueryVisitor vis : ast.getVisitorList()) {
            vis.before(q);
        }
    }

    void after(Mappings map) {
        ASTQuery ast = (ASTQuery) map.getQuery().getAST();
        for (fr.inria.corese.sparql.api.QueryVisitor vis : ast.getVisitorList()) {
            vis.after(map);
        }
    }

    /**
     * LDScript binding stack of current Eval use case: share global variables
     */
    public Binding getCurrentBinding() {
        if (getCurrentEval() == null) {
            return null;
        }
        return (Binding) getCurrentEval().getEnvironment().getBind();
    }

    public Binding getBinding() {
        if (getCurrentEval() == null
                || getCurrentEval().getEnvironment() == null
                || getCurrentEval().getEnvironment().getBind() == null) {
            return Binding.create();
        }
        return (Binding) getCurrentEval().getEnvironment().getBind();
    }

    public void finish() {
        if (current != null) {
            current.finish();
        }
    }

    public Eval getCurrentEval() {
        return current;
    }

    public void setCurrentEval(Eval e) {
        current = e;
    }

    public void setEval(Eval e) {
        setCurrentEval(e);
    }

    void tune(Eval kgram, Query q, Mapping m) {
        ASTQuery ast = (ASTQuery) q.getAST();
        boolean event = ast.hasMetadata(Metadata.EVENT);
        tune(kgram, m, event);
        if (q.isInitMode()) {
            // set visitor sleeping mode during init
            kgram.getVisitor().setActive(true);
        }
    }

    void tune(Eval kgram) {
        tune(kgram, null, false);
    }

    void tune(Eval kgram, Mapping m, boolean isVisitor) {
        if ((isVisitor || isVisitorable()) && isEvent(m)) {
            if (m != null && m.getVisitorParameter()!= null) {
                m.getVisitorParameter().setProcessor(kgram);
                kgram.setVisitor(m.getVisitorParameter());
            }
            else {
                kgram.setVisitor(createProcessVisitor(kgram));
            }
        }
        kgram.getVisitor().setDefaultValue(DatatypeMap.TRUE);
    }
    
    boolean isEvent(Mapping m) {
        return Access.accept(Feature.EVENT, Access.getLevel(m));
    }

    // overloaded by QueryProcess
    public ProcessVisitor createProcessVisitor(Eval eval) {
        return new QuerySolverVisitor(eval);
    }

    Eval makeEval() {
        Eval kgram = Eval.create(producer, evaluator, matcher);
        kgram.setSPARQLEngine(this);
        kgram.set(provider);
        return kgram;
    }

    /**
     *
     * @return an Eval able to execute callback functions str contains function
     * definitions
     * @throws EngineException
     */
    public Eval createEval(String str, Dataset ds) throws EngineException {
        Query q = compile(str, ds);
        Eval eval = createEval(q);
        tune(eval);
        return eval;
    }

    public Eval createEval(Query q) throws EngineException {
        Eval kgram = makeEval();
        try {
            kgram.query(q);
        } catch (SparqlException ex) {
            throw EngineException.cast(ex);
        }
        return kgram;
    }

    @Deprecated
    public IDatatype eval(String q) throws EngineException {
        return DatatypeMap.TRUE;
    }
//
//    public IDatatype eval(String q, Dataset ds) throws EngineException {
//        return eval(q, MAIN_FUN, ds);
//    }
//
//    IDatatype eval(String q, String name, Dataset ds) throws EngineException {
//        setGenerateMain(false);
//        Eval kgram = createEval(q, ds);
//        setEval(kgram);
//        IDatatype dt = funcall(name);
//        return dt;
//    }
//    
//    // overriden by QueryProcess
//    public IDatatype funcall(String name, IDatatype... param) throws EngineException {
//        return null;
//    }

    void init(Query q) {
        // use case: OWL RL kg:sparql(query) in a rule
        // query is evaluated as a Rule
        if (isRule) {
            q.setRule(isRule);
        }
        q.setMatchBlank(isMatchBlank);
        q.setListGroup(isListGroup);
        q.setListPath(isListPath);
        q.setPathType(isPathType);
        q.setStorePath(isStorePath);
        q.setCachePath(isCachePath());
        q.setCountPath(isCountPath);
        q.setCheckLoop(isCheckLoop);
        q.setDetail(q.isDetail() || isDetail);
        q.setSlice(slice);
        if (isDebug()) {
            q.setDebug(isDebug());
        }
        if (isOptimize) {
            q.setOptimize(isOptimize);
        }
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer p) {
        producer = p;
    }

    public Interpreter getEvaluator() {
        return evaluator;
    }

    public Interpreter getInterpreter() {
        return evaluator;
    }

    public ASTQuery getAST(Query q) {
        return (ASTQuery) q.getAST();
    }

    public ASTQuery getAST(Mappings lm) {
        Query q = lm.getQuery();
        return getAST(q);
    }

    public void addEventListener(EventListener el) {
        if (manager == null) {
            manager = new EventManager();
        }
        manager.addEventListener(el);
    }

    public void addResultListener(ResultListener el) {
        listener = el;
    }

    public void removeResultListener(ResultListener el) {
        if (listener == el) {
            listener = null;
        }
    }

    public Query compile(String squery) throws EngineException {
        return compile(squery, null);
    }

    // rule: construct where 
    public Query compileRule(String squery) throws EngineException {
        return compileRule(squery, null);
    }

    void setParameter(Transformer transformer) {
        transformer.setGenerateMain(isGenerateMain());
        transformer.setNamespaces(NAMESPACES);
        transformer.setPragma(getPragma());
        transformer.setMetadata(getActualMetadata());
        transformer.setPlanProfile(getPlanProfile());
        transformer.setUseBind(isUseBind());
        transformer.setBGP(isBGP());
        transformer.setAlgebra(isAlgebra());
        transformer.setServiceList(getServiceList());
    }

    public Query compileRule(String squery, Dataset ds) throws EngineException {
        Transformer transformer = createTransformer(ds);
        Query query = transformer.transform(squery, true);
        return query;
    }

    public Query compile(String squery, Dataset ds) throws EngineException {
        Transformer transformer = createTransformer(ds);
        transformer.setSPARQLCompliant(isSPARQLCompliant);
        Query query = transformer.transform(squery);
        return query;
    }
    
    public ASTQuery parse(String q) throws EngineException {
        return parse(q, (Dataset) null);
    }
    
    public ASTQuery parse(String q, Dataset ds) throws EngineException {
        Transformer transformer = createTransformer(ds);
        return transformer.parse(q);
    }

    Transformer createTransformer(Dataset ds) {
        Transformer transformer = transformer();
        setParameter(transformer);
        transformer.setBase(defaultBase);
        transformer.set(ds);
        return transformer;
    }

    public Query compile(ASTQuery ast) throws EngineException {
        Transformer transformer = transformer();
        setParameter(transformer);
        transformer.setSPARQLCompliant(isSPARQLCompliant);
        Query query = transformer.transform(ast);
        return query;
    }

//    Query compileFilter(String filter) throws EngineException {
//        String str = "select * where {} having(" + filter + ")";
//        Query q = compile(str);
//        return q;
//    }


    void debug(Query query) {
        if (query.isDebug()) {
            logger.debug(query.getBody().toString());
            logger.debug("limit " + query.getLimit());
            if (query.isFail()) {
                logger.debug("Fail at compile time");
            }
        }
    }

    void events(Eval kgram) {
        if (manager != null) {
            for (EventListener el : manager) {
                kgram.addEventListener(el);
            }
        }
        kgram.addResultListener(listener);
    }

    void pragma(Eval kgram, Query query) {
        ASTQuery ast = (ASTQuery) query.getAST();
        Pragma pg = new Pragma(kgram, query, ast);
        if (getPragma() != null) {
            pg.parse(getPragma());
        }
        if (ast != null && ast.getPragma() != null) {
            pg.parse();
        }
    }

    public void addPragma(String subject, String property, String value) {
        Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
        getPragma().add(t);
    }

    public void addPragma(String subject, String property, int value) {
        Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
        getPragma().add(t);
    }

    public void addPragma(String subject, String property, boolean value) {
        Triple t = Triple.create(Constant.create(subject), Constant.create(property), Constant.create(value));
        getPragma().add(t);
    }

    public void addPragma(Atom subject, Atom property, Atom value) {
        if (getPragma() == null) {
            setPragma(BasicGraphPattern.create());
        }
        getPragma().add(Triple.create(subject, property, value));
    }

    public static void defaultNamespaces(String ns) {
        NAMESPACES = ns;
    }

    public static void definePrefix(String pref, String ns) {
        if (NAMESPACES == null) {
            NAMESPACES = "";
        }
        NAMESPACES += pref + " " + ns + " ";
    }

    public void setListGroup(boolean b) {
        isListGroup = b;
    }

    public void setListPath(boolean b) {
        isListPath = b;
    }

    public void setCountPath(boolean b) {
        isCountPath = b;
    }

    public void setPathLoop(boolean b) {
        isCheckLoop = !b;
    }

    public void setSPARQLCompliant(boolean isSPARQLCompliant) {
        this.isSPARQLCompliant = isSPARQLCompliant;
    }

    boolean isSPARQLCompliant() {
        return isSPARQLCompliant;
    }

    public void setDebug(boolean b) {
        isDebug = b;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setOptimize(boolean b) {
        isOptimize = b;
    }

    public void setSlice(int n) {
        slice = n;
    }

    public BasicGraphPattern getPragma() {
        return pragma;
    }

    public void setPragma(BasicGraphPattern pragma) {
        this.pragma = pragma;
    }

    public boolean isDetail() {
        return isDetail;
    }

    public void setDetail(boolean isDetail) {
        this.isDetail = isDetail;
    }

    /**
     * @return the isSkolem
     */
    public boolean isSkolem() {
        return isSkolem;
    }

    /**
     * @param isSkolem the isSkolem to set
     */
    public void setSkolem(boolean isSkolem) {
        this.isSkolem = isSkolem;
    }

    /**
     * @return the isMatchBlank
     */
    public boolean isMatchBlank() {
        return isMatchBlank;
    }

    /**
     * @param isMatchBlank the isMatchBlank to set
     */
    public void setMatchBlank(boolean isMatchBlank) {
        this.isMatchBlank = isMatchBlank;
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
     * @return the isPathType
     */
    public boolean isPathType() {
        return isPathType;
    }

    /**
     * @param isPathType the isPathType to set
     */
    public void setPathType(boolean isPathType) {
        this.isPathType = isPathType;
    }

    /**
     * @return the isStorePath
     */
    public boolean isStorePath() {
        return isStorePath;
    }

    /**
     * @param isStorePath the isStorePath to set
     */
    public void setStorePath(boolean isStorePath) {
        this.isStorePath = isStorePath;
    }

    /**
     * @return the isCachePath
     */
    public boolean isCachePath() {
        return isCachePath;
    }

    /**
     * @param isCachePath the isCachePath to set
     */
    public void setCachePath(boolean isCachePath) {
        this.isCachePath = isCachePath;
    }

//    /**
//     * @return the mode
//     */
//    public int getMode() {
//        return mode;
//    }
//
//    /**
//     * @param mode the mode to set
//     */
//    public void setMode(int mode) {
//        this.mode = mode;
//        //initMode();
//    }
//
//    static public void setModeDefault(int mode) {
//        INIT_SERVER_MODE = mode;
//    }

//    public void initMode() {
//    }
    
    // true = skip Lock ;  false = with Lock
    @Override
    public boolean isSynchronized() {
        return isSynchronized;
    }
   
    // true = skip Lock ;  false = with Lock
    @Override
    public void setSynchronized(boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
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

    public Query parseQuery(String path) throws EngineException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Query parseQuery(String path, Level level) throws EngineException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public ASTQuery parse(String path, Level level) throws EngineException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Metadata getActualMetadata() {
        return getMetadata();
    }

    /**
     * @return the metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void set(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * @return the rule
     */
    public boolean isRule() {
        return isRule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(boolean rule) {
        this.isRule = rule;
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

    public void service(String uri) {
        if (getServiceList() == null) {
            setServiceList(new ArrayList<Atom>());
        }
        getServiceList().add(Constant.createResource(uri));
    }

    /**
     * @return the algebra
     */
    public boolean isAlgebra() {
        return algebra;
    }

    /**
     * BGP must be set to true
     *
     * @param algebra the algebra to set
     */
    public void setAlgebra(boolean algebra) {
        this.algebra = algebra;
    }

    public static void testAlgebra(boolean b) {
        BGP_DEFAULT = b;
        ALGEBRA_DEFAULT = b;
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

    @Override
    public void getLinkedFunction(String uri) throws EngineException {
    }

    /**
     * @return the visitorable
     */
    public static boolean isVisitorable() {
        return visitorable;
    }

    /**
     * @param aVisitorable the visitorable to set
     */
    public static void setVisitorable(boolean aVisitorable) {
        visitorable = aVisitorable;
    }

}
