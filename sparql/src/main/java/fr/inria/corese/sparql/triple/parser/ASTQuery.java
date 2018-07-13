package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.script.Let;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.function.script.ForLoop;
import fr.inria.corese.sparql.api.IDatatype;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.api.ASTVisitable;
import fr.inria.corese.sparql.triple.api.ASTVisitor;
import fr.inria.corese.sparql.triple.cst.Keyword;
import fr.inria.corese.sparql.triple.cst.KeywordPP;
import fr.inria.corese.sparql.triple.cst.RDFS;
import fr.inria.corese.sparql.triple.printer.SPIN;
import fr.inria.corese.sparql.triple.update.ASTUpdate;
import fr.inria.corese.sparql.compiler.java.JavaCompiler;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.query.Graphable;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class is the abstract syntax tree, it represents the initial query
 * (except for get:gui).<br>
 * When complete, it will be transformed into a Query Graph.java.
 * <br>
 *
 * @author Olivier Corby & Virginie Bottollier
 */
public class ASTQuery 
        extends ASTObject 
        implements Keyword, ASTVisitable, Graphable {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;
    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(ASTQuery.class);

    static String RootPropertyQN = RDFS.RootPropertyQN; // cos:Property
    static String RootPropertyURI = RDFS.RootPropertyURI; //"http://www.inria.fr/acacia/corese#Property";
    static final String LIST = "list";
    public static final String KGRAMVAR = "?_ast_";
    public static final String SYSVAR = "?_cos_";
    public static final String BNVAR = "?_bn_";
    public static final String MAIN_VAR = "?_main_";
    static final String FOR_VAR = "?_for_";
    static final String LET_VAR = "?_let_";
    static final String FUN_VAR = "?_fun_var_";

    static final String FUN_NAME = NSManager.EXT_PREF+":_fun_";
    static final String FUN_PREF = NSManager.EXT_PREF+":";
    static final String NL = System.getProperty("line.separator");
    static int nbt = 0; // to generate an unique id for a triple if needed
    public final static int QT_SELECT = 0;
    public final static int QT_ASK = 1;
    public final static int QT_CONSTRUCT = 2;
    public final static int QT_DESCRIBE = 3;
    public final static int QT_DELETE = 4;
    public final static int QT_UPDATE = 5;
    public final static int QT_TEMPLATE = 6;
    public final static int L_PATH = 2;
    public final static int L_LIST = 1;
    public final static int L_DEFAULT = 0;
    
    public static final String OUT = "?out";
    public static final String IN = "?in";
    public static final String IN2 = "?in_1";
   
    /**
     * if graph rule
     */
    boolean rule = false;
    boolean isConclusion = false;
    /**
     * approximate projection
     */
    boolean more = false;
    private boolean isRelax = false;
    boolean isDelete = false;
    /**
     * default process join
     */
    boolean join = false;
    /**
     * join result into one graph
     */
    boolean one = false;
    /**
     * sparql bind
     */
    boolean XMLBind = true;
    /**
     * select distinct where : all are distinct
     */
    boolean distinct = false;
    /**
     * true : sparql, false : corese
     */
    boolean strictDistinct = true;
    /**
     * relation on which join connex
     */
    boolean connex = false;
    boolean hasScore = false;
    /**
     * display blank node id
     */
    boolean displayBNID = false;
   
    /**
     * display in RDF
     */
    boolean rdf = false, isJSON = false;
    
    /**
     * select * : select all variables from query
     */
    boolean selectAll = false,
            // additional SPARQL constraints (dot, arg of type string type, ...)
            isSPARQLCompliant = false;
    // validation mode (check errors)
    private boolean validate = false;
    boolean isInsertData = false;
    boolean isDeleteData = false;
    boolean sorted = true; // if the relations must be sorted (default true)
    boolean debug = false, isCheck = false;
    boolean nosort = false,
            // load from and from named documents before processing
            isLoad = false;
    private boolean isFail = false;
    boolean isCorrect = true;
    /**
     * booleans useful for the sparql pretty printer
     */
    boolean where = false;
    boolean merge = false;
    /**
     * used in QueryGraph.java to compile the construct
     */
    boolean constructCompiled = false;
    // construct in the std graph:
    boolean isAdd = false;
    boolean describeAll = false;
    boolean isBind = false;
    /**
     * max cg result
     */
    int MaxResult = Integer.MAX_VALUE;
    int DefaultMaxResult = Integer.MAX_VALUE;
    /**
     * max projection
     */
    int MaxProjection = Integer.MAX_VALUE;
    int DefaultMaxProjection = Integer.MAX_VALUE;
    // path length max
    int DefaultMaxLength = 5;
    int MaxDisplay = 10000;
    /**
     * Offset
     */
    int Offset = 0;
    int nbBNode = 0;
    int nbd = 0; // to generate an unique id for a variable if needed
    int nbfun = 0, nbvar = 0;
    int resultForm = QT_SELECT;
    private int priority = 100;
    int countVar = 0;
    /**
     * if more, reject 2 times worse projection than best one
     */
    float Threshold = 1;
    float DefaultThreshold = 1;
    //byte access = Cst.ADMIN;
    // predefined ns from server
    String namespaces, base;
    // relax by dd:distance
    String distance;
    /**
     * the source text of the query
     */
    String text = null;
    /**
     * Represents the ASTQuery before compilation
     */
    String queryPrettyPrint = "";
    /**
     * Source body of the query returned by javacc parser
     */
    Exp bodyExp, bodySave;
    /**
     * Compiled triple query expression
     */
    Exp query;
    // compiled construct (graph ?g removed)
    Exp constructExp,
            // genuine construct
            construct,
            delete;
    // triples that define prefix/namespace
    Exp prefixExp = new And();
    ASTQuery globalAST;
    Expression having;
    List<Variable> selectVar = new ArrayList<Variable>();
    // select *
    List<Variable> selectAllVar = new ArrayList<Variable>();
    List<Variable> argList = new ArrayList<Variable>();
    List<Expression> sort = new ArrayList<Expression>();
    List<Expression> lGroup = new ArrayList<Expression>();
    List<Expression> relax = new ArrayList<Expression>();
    private Dataset // Triple store default dataset
            defaultDataset,
            // from, from named, with
            dataset;
    Context context;
    List<Atom> adescribe = new ArrayList<Atom>();
    List<Variable> stack = new ArrayList<Variable>(); // bound variables
    List<String> vinfo;
    List<String> errors;
    Values values;
    List<Boolean> reverseTable = new ArrayList<Boolean>();
    HashMap<String, Expression> selectFunctions = new HashMap<String, Expression>();
    private ASTExtension define, lambdaDefine;
    private HashMap<String, Expression> undefined;
    ExprTable selectExp = new ExprTable();
    ExprTable regexExpr = new ExprTable();
    // pragma {}
    HashMap<String, Exp> pragma;
    HashMap<String, Exp> blank;
    HashMap<String, Variable> blankNode;
    Metadata metadata;
    HashMap<String, Atom> dataBlank;
    NSManager nsm;
    ASTUpdate astu;
    ASTTemplate atemp;
    
    private boolean renameBlankNode = true;
    private String groupSeparator = " ";
    private boolean isTemplate = false;
    private boolean isAllResult = false;
    private String name;
    // @(a b) rewritten as rdf:rest*/rdf:first a, b
    private int listType = L_LIST;
    private String profile;
    private boolean isFunctional;
    private final Map<String, List<String>> approximateSearchOptions = new HashMap<String, List<String>>();
    private String service;
    private List<Atom> serviceList;
    private List<Constant> predicateList;
    private List<Triple> tripleList;

    /**
     * @return the defaultDataset
     */
    public Dataset getDefaultDataset() {
        return defaultDataset;
    }

    /**
     * @param defaultDataset the defaultDataset to set
     */
    public void setDefaultDataset(Dataset defaultDataset) {
        this.defaultDataset = defaultDataset;
        if (defaultDataset != null && defaultDataset.getContext() != null){
            setContext(defaultDataset.getContext());
        }
    }

    public Context getContext() {
        return context;
    }
    
    public void setContext(Context c){
        context = c;
    }
    
    public Object getTemplateVisitor(){
        if (defaultDataset != null){
            return defaultDataset.getTemplateVisitor();
        }
        return null;
    }

    public boolean isUserQuery() {
        Context c = getContext();
        if (c == null) {
            return false;
        }
        return c.isUserQuery();
    }

    @Override
    public String toGraph() {
        SPIN sp = SPIN.create();
        sp.visit(this);
        return sp.toString();
    }

    @Override
    public void setGraph(Object obj) {
    }

    @Override
    public Object getGraph() {
        return null;
    }

    public void setHasFunctional(boolean b) {
        isFunctional = b;
    }

    public boolean hasFunctional() {
        return isFunctional;
    }

    /**
     * @return the define
     */
    public ASTExtension getDefine() {
        return define;
    }
    
    public ASTExtension getDefineLambda() {
        return lambdaDefine;
    }

    /**
     * @param define the define to set
     */
    public void setDefine(ASTExtension define) {
        this.define = define;
    }
    
     public void setDefineLambda(ASTExtension define) {
        this.lambdaDefine = define;
    }

    /**
     * @return the undefined
     */
    public HashMap<String, Expression> getUndefined() {
        return undefined;
    }

    /**
     * @param undefined the undefined to set
     */
    public void setUndefined(HashMap<String, Expression> undefined) {
        this.undefined = undefined;
    }

    /**
     * @return the isFail
     */
    public boolean isFail() {
        return isFail;
    }

    /**
     * @param isFail the isFail to set
     */
    public void setFail(boolean isFail) {
        this.isFail = isFail;
    }

    /**
     * @return the isRelax
     */
    public boolean isRelax() {
        return isRelax;
    }

    /**
     * @param isRelax the isRelax to set
     */
    public void setRelax(boolean isRelax) {
        this.isRelax = isRelax;
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

    class ExprTable extends HashMap<Expression, Expression> {
    };

    /**
     * The constructor of the class It looks like the one for QueryGraph
     */
    private ASTQuery() {
        dataset = Dataset.create();
        define = new ASTExtension();
        lambdaDefine = new ASTExtension();
        undefined = new HashMap();
        predicateList = new ArrayList<>();
        tripleList = new ArrayList<>();
    }

    ASTQuery(String query) {
        this();
        setText(query);
    }

    public static ASTQuery create(String query) {
        return new ASTQuery(query);
    }

    public static ASTQuery create() {
        return new ASTQuery();
    }

    public static ASTQuery create(Exp exp) {
        ASTQuery ast = new ASTQuery();
        ast.setBody(exp);
        return ast;
    }

    public static ASTQuery create(String query, boolean isRule, boolean isConclusion) {
        ASTQuery aq = new ASTQuery(query);
        aq.setConclusion(isConclusion);
        aq.setRule(isRule);
        return aq;
    }

    /**
     * AST for a subquery share prefix declaration
     */
    public ASTQuery subCreate() {
        ASTQuery ast = create();
        ast.setGlobalAST(this);
        ast.setNSM(getNSM());
        return ast;
    }

    void setGlobalAST(ASTQuery a) {
        globalAST = a;
    }

    public ASTQuery getGlobalAST() {
        if (globalAST == null) {
            return this;
        }
        return globalAST;
    }

    public List<Constant> getFrom() {
        return dataset.getFrom();
    }

    public List<Constant> getNamed() {
        return dataset.getNamed();
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset ds) {
        dataset = ds;
    }

    public void setNamed(Constant uri) {
        dataset.addNamed(uri);
    }

    public void setFrom(Constant uri) {
        dataset.addFrom(uri);
    }

    public List<Constant> getActualFrom() {
        if (dataset.hasFrom()) {
            return dataset.getFrom();
        }
        if (dataset.hasWith()) {
            // with <uri> insert {} where {}
            return dataset.getWith();
        }
        if (defaultDataset != null && defaultDataset.hasFrom()) {
            return defaultDataset.getFrom();
        }
        return dataset.getFrom();
    }

    public List<Constant> getActualNamed() {
        if (dataset.hasNamed()) {
            return dataset.getNamed();
        }
        if (defaultDataset != null && defaultDataset.hasNamed()) {
            return defaultDataset.getNamed();
        }
        return dataset.getNamed();
    }

    public void setInsertData(boolean b) {
        isInsertData = b;
    }

    public boolean isInsertData() {
        return isInsertData;
    }

    public void setDeleteData(boolean b) {
        isDeleteData = b;
    }

    public boolean isDeleteData() {
        return isDeleteData;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean b) {
        validate = b;
    }

    /**
     * collect var for select * check scope for BIND(exp as var) and select exp
     * as var
     */
    public boolean validate() {

        // in some case, validate() may be called twice
        // hence clear the stack
        stack.clear();

        collect();

        if (getBody() != null) {
            // select ?x
            for (Variable var : getSelectVar()) {
                if (hasExpression(var)) {
                    bind(var);
                }
            }
            // select *
            if (isSelectAll()) {
                for (Variable var : getSelectAllVar()) {
                    if (hasExpression(var)) {
                        bind(var);
                    }
                }
            }

            boolean ok = true;
            
            for (Exp exp : getBody().getBody()) {
                boolean b = exp.validate(this);
                if (!b) {
                    ok = false;
                }
            }

            return ok;
        }

        return true;
    }

    // collect values for select *
    void collect() {
        if (getValues() != null) {
            for (Variable var : getValues().getVariables()) {
                defSelect(var);
            }
        }
    }

    void record(Atom blank) {
        if (dataBlank == null) {
            createDataBlank();
        }
        dataBlank.put(blank.getLabel(), blank);
    }

    public void createDataBlank() {
        dataBlank = new HashMap<String, Atom>();
    }

    public HashMap<String, Atom> getDataBlank() {
        return dataBlank;
    }

    /**
     *
     * @param info
     */
    public void addInfo(String info) {
        if (vinfo == null) {
            vinfo = new ArrayList<String>(1);
        }
        vinfo.add(info);
    }

    public void addFail(boolean b) {
        getGlobalAST().setFail(b);
    }

    /**
     *
     * @param error
     */
    public void addError(String error) {
        getGlobalAST().setError(error);
    }

    public void addError(String error, Object obj) {
        getGlobalAST().setError(error + obj);
    }

    void undefined(Expression t) {
        if (!getGlobalAST().getDefine().isDefined(t)) {
            getGlobalAST().getUndefined().put(t.getLabel(), t);
        }
    }

    /**
     * Used by VariableVisitor, called by Transformer def = function(st:foo(?x)
     * = st:bar(?x))
     */
    void define(Function fun) {
        Expression t = fun.getFunction(); 
        getGlobalAST().getUndefined().remove(t.getLabel());
    }

    public List<String> getErrors() {
        return getGlobalAST().errors();
    }
    
    public String getErrorString() {
        StringBuilder sb = new StringBuilder();
        for (String str : getErrors()){
            sb.append(str).append(NL);
        }
        return sb.toString();
    }
    
    public String getUpdateTitle() {
        if (isAdd()) {
            return KeywordPP.INSERT;
        }
        if (isDelete()) {
            return KeywordPP.DELETE;
        }
        return KeywordPP.CONSTRUCT;
    }

    void setError(String error) {
        if (errors == null) {
            errors = new ArrayList<String>();
        }
        if (!errors.contains(error)) {
            errors.add(error);
            logger.error(error);
        }
    }

    List<String> errors() {
        return errors;
    }

    public void setConnex(boolean connex) {
        this.connex = connex;
    }

    public void setDisplayBNID(boolean displayBNID) {
        this.displayBNID = displayBNID;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void setReduced(boolean b) {
        //this.distinct = distinct;
    }

    public void setStrictDistinct(boolean strictDistinct) {
        this.strictDistinct = strictDistinct;
    }

    public boolean isStrictDistinct() {
        return strictDistinct;
    }

    public void setRDF(boolean rdf) {
        this.rdf = rdf;
    }

    public void setJSON(boolean b) {
        isJSON = b;
    }

    public boolean isJSON() {
        return isJSON;
    }

    public void setConclusion(boolean isConclusion) {
        this.isConclusion = isConclusion;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public void setMaxDisplay(int maxDisplay) {
        MaxDisplay = maxDisplay;
    }

    public void setMaxProjection(int maxProjection) {
        MaxProjection = maxProjection;
    }

    public void setMaxResult(int maxResult) {
        MaxResult = maxResult;
    }

    public void setLimit(int maxResult) {
        MaxResult = maxResult;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public void setRelax(List<Expression> l) {
        relax = l;
    }

    public void addRelax(Expression e) {
        relax.add(e);
    }

    public List<Expression> getRelax() {
        return relax;
    }

    public void setOne(boolean one) {
        this.one = one;
    }


    public void setQuery(Exp query) {
        this.query = query;
    }

    public void setScore(boolean score) {
        this.hasScore = score;
    }

    public boolean getScore() {
        return hasScore;
    }

    public void setDistance(String dist) {
        distance = dist;
    }

    public String getDistance() {
        return distance;
    }

    public void setRule(boolean rule) {
        this.rule = rule;
    }

    public void setSelectAll(boolean selectAll) {
        // We print relations between concepts if SELECT DISPLAY RDF *
        // SELECT DISPLAY RDF * <=> SELECT DISPLAY RDF
        if (selectAll && isRDF()) {
            this.selectAll = false;
        } else {
            this.selectAll = selectAll;
        }
    }

    public void setBasicSelectAll(boolean b) {
        selectAll = b;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setThreshold(float threshold) {
        Threshold = threshold;
    }

    public void setXMLBind(boolean bind) {
        XMLBind = bind;
    }

    public boolean isConnex() {
        return connex;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public boolean isReduced() {
        return false;
    }

    public boolean isRDF() {
        return rdf;
    }

    public boolean isConclusion() {
        return isConclusion;
    }

    public boolean isJoin() {
        return join;
    }

    public int getMaxDisplay() {
        return MaxDisplay;
    }

    public int getMaxProjection() {
        return MaxProjection;
    }

    public int getMaxResult() {
        return MaxResult;
    }

    public boolean isMore() {
        return more;
    }

    public void setLoad(boolean b) {
        isLoad = b;
    }

    public boolean isLoad() {
        return isLoad;
    }

    /**
     * NS Manager
     */
    public NSManager getNSM() {
        if (nsm == null) {
            nsm = NSManager.create(getDefaultNamespaces());
            nsm.setBase(getDefaultBase());
            nsm.setRecord(true);
        }
        return nsm;
    }

    public void setNSM(NSManager nsm) {
        this.nsm = nsm;
    }

    public String getDefaultNamespaces() {
        return namespaces;
    }

    public void setDefaultNamespaces(String ns) {
        namespaces = ns;
    }

    public String getDefaultBase() {
        return base;
    }

    public void setDefaultBase(String ns) {
        base = ns;
    }

    public Exp getQueryExp() {
        return query;
    }

    public Exp getExtBody() {
        if (query != null) {
            return query;
        }
        return getBody();
    }

    public boolean isRule() {
        return rule;
    }
    
    public List<Variable> getSelectVariables() {
        ArrayList<Variable> list = new ArrayList<>();
        list.addAll(getSelectVar());
        for (Variable var : getSelectAllVar()){
            if (! list.contains(var)){
                list.add(var);
            }
        }
        return list;
    }

    public List<Variable> getSelectVar() {
        return selectVar;
    }
    
    public Variable getSelectVar(String name) {
        for (Variable var : selectVar){
            if (var.getLabel().equals(name)){
                return var;
            }
        }
        return null;
    }

    public List<Variable> getSelectAllVar() {
        return selectAllVar;
    }
    
    public boolean isSelectVariable(Variable var){
        return getSelectVar(var.getName()) != null || isSelectAllVar(var.getName());
    }

    public boolean isSelectAllVar(Variable var) {
        return selectAllVar.contains(var);
    }

    public boolean isSelectAllVar(String name) {
        return getSelectAllVar(name) != null;
    }
    
    public Variable getSelectAllVar(String name) {
        for (Variable var : selectAllVar) {
            if (var.getLabel().equals(name)) {
                return var;
            }
        }
        return null;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public List<Expression> getSort() {
        return sort;
    }

    public List<Expression> getOrderBy() {
        return sort;
    }

    public String getText() {
        return text;
    }

    public float getThreshold() {
        return Threshold;
    }

    public boolean isXMLBind() {
        return XMLBind;
    }

    /**
     * created for the new parser
     */
    public static Term createRegExp(Expression exp) {
        Term term = Term.function(REGEX, exp);
        return term;
    }

    boolean checkBlank(Expression exp) {
        if (exp.isBlankNode()) {
            setCorrect(false);
            return false;
        }
        return true;
    }

    /**
     * BIND( f(?x) as ?y )
     */
    public Exp createBind(Expression exp, Variable var) {
       return Binding.create(exp, var);
    }
    
    public Exp createBind2(Expression exp, Variable var) {
        if (var.getVariableList() != null) {
            // bind (sql() as ())
            return createASTBind(exp, var);
        } else {
            return Binding.create(exp, var);
        }
    }

    public Exp createASTBind(Expression exp, Variable var) {
        ASTQuery ast = subCreate();
        ast.setBody(BasicGraphPattern.create());
        ast.setSelect(var, exp);
        ast.setBind(true);
        Query q = Query.create(ast);
        return q;
    }

    Term createTerm(String oper, Expression exp1, Expression exp2) {
        checkBlank(exp1);
        checkBlank(exp2);
        Term term = Term.create(oper, exp1, exp2);
        return term;
    }

    public Term createConditionalAndExpression(String oper, Expression exp1, Expression exp2) {
        return createTerm(oper, exp1, exp2);
    }

    public Term createConditionalOrExpression(String oper, Expression exp1, Expression exp2) {
        return createTerm(SEOR, exp1, exp2);
    }

    public Term createAltExpression(Expression exp1, Expression exp2) {
        return createTerm(Term.RE_ALT, exp1, exp2);
    }

    public Term createParaExpression(Expression exp1, Expression exp2) {
        return createTerm(Term.RE_PARA, exp1, exp2);
    }

    public Term createSeqExpression(Expression exp1, Expression exp2) {
        return createTerm(Term.RE_SEQ, exp1, exp2);
    }

    public Term createRelationalExpression(String oper, Expression exp1, Expression exp2) {
        return createTerm(oper, exp1, exp2);

    }

    public Term createMultiplicativeExpression(String oper, Expression exp1, Expression exp2) {
        return createTerm(oper, exp1, exp2);

    }

    public Term createAdditiveExpression(String oper, Expression exp1, Expression exp2) {
        return createTerm(oper, exp1, exp2);

    }

    public Expression createUnaryExpression(String oper, Expression expression) {
        checkBlank(expression);
        if (oper.equals(SENOT)) {
            expression = Term.negation(expression);
        } else if (oper.equals("-")) {
            expression = Term.create(oper,
                    Constant.create("0", RDFS.qxsdInteger), expression);
        } // else : oper.equals("+") => don't do anything
        return expression;
    }

    
    Constant functionName(){
        UUID uuid = UUID.randomUUID();
        return createQName(FUN_PREF +  uuid.toString());
    }
    
    Constant functionName2(){
        return createQName(FUN_NAME + nbfun++);
    }
   
    /**
     * function name(el) { exp } -> function (name(el), exp)
     */
    public Function defineFunction(Constant name, ExpressionList el, Expression exp, Metadata annot) {
        Function fun = defFunction(name, el, exp, annot);
        record(fun);    
        return fun;
    }
    
    // lambda(?x) {}
     public Function defineLambda(ExpressionList el, Expression exp, Metadata annot) {
         if (el.isNested()){
             // lambda((?x, ?y)){ exp } 
             // ->
             // lambda(?m) { let ((?x, ?y) = ?m) { exp }}
             Variable var = new Variable(LET_VAR + nbd++);
             ExpressionList list = new ExpressionList(var);
             Term deflet = defLet(el.getList().get(0), var);
             Term let = let(deflet, exp);
             return defineLambda(list, let, annot);
         }
         return getGlobalAST().defineLambdaUtil(el, exp, annot);
     }
     
    Function defineLambdaUtil(ExpressionList el, Expression exp, Metadata annot) {
        return  defineLambdaUtil(functionName(), el, exp, annot);
    }
        
    Function defineLambdaUtil(Constant name, ExpressionList el, Expression exp, Metadata annot) {    
        Function fun = defFunction(name, el, exp, annot);
        fun.defineLambda();
        record(fun);    
        return fun;
    }
     
    
    /**
     * Define lambda for function URI
     * use case:  
     * apply(rq:plus, ?list) ->
     * apply(lambda(?x, ?y) { rq:plus(?x, ?y) }, ?list)
     * Use globalAST in case of lambda generated in subquery
     */
    Function defineLambda(Constant uri, int arity) {
        return getGlobalAST().defineLambdaUtil(uri, arity);
    }
    
    Function defineLambdaUtil(Constant uri, int arity) {
        ExpressionList el = new ExpressionList();
        for (int i = 0; i < arity; i++){
            el.add(createVariable(FUN_VAR+nbvar++));
        }
        Term t = createFunction(uri, el);
        Function fun = defineLambdaUtil(el, t, null);
//        Function fun = defFunction(functionName(), el, t, null);
//        fun.setLambda(true);
//        record(fun);
        return fun;
    }
           
    void record(Function fun){
         if (fun.isLambda()){
             lambdaDefine.defineFunction(fun);
         }
         else {
             define.defineFunction(fun);
         }
    }
        
    Function defFunction(Constant name, ExpressionList el, Expression exp, Metadata annot) {
        Term fun = createFunction(name, el);      
        Function def = new Function(fun, exp);
        annotate(def, annot);
        if (el.getTable() != null){
            def.setTable(el.getTable());
        }
        return def;
    }
          

    /**
     * Runtime create extension function ext for predefined  function name
     * function rq:isURI(?x) { isURI(?x) }
     */
    public Function defExtension(String ext, String name, int arity) {
        Constant c = createQNameURI(ext);
        ExpressionList el = new ExpressionList();
        for (int i = 0; i < arity; i++) {
            el.add(createVariable(FUN_VAR + i));
        }
        Term t = createFunction(createQNameURI(name), el);
        Function fun = defineFunction(c, el, t, null);
        fun.compile(this);
        return fun;
    }

    public void annotate(Function t, Metadata la) {
        t.annotate(la);
    }

    public void setMetadata(Metadata m) {
         metadata = m;
    }
    
    public void setAnnotation(Metadata m){
         if (m != null) {
            setMetadata(m);
            annotate(m);
        }
    }
    
     public void addMetadata(Metadata m){
         if (metadata == null){
            setMetadata(m);
         }
         else {
             metadata.add(m);
         }
         annotate(metadata);
    }

    public Metadata getMetadata() {
        return metadata;
    }
    
    public String getMetadataValue(int type) {
        return metadata.getValue(type);
    }

    public boolean hasMetadata(int type) {
        return metadata != null && metadata.hasMetadata(type);
    }
    
    public boolean hasMetadata(int type, String value) {
        return metadata != null && metadata.hasValue(type, value);
    }
    
    public boolean hasMetadataValue(int type, String value) {
        return metadata != null && metadata.hasValues(type, value);
    }

    /**
     * SubQuery within function inherit function Metadata
     */
    void inherit(Metadata meta) {
        annotate(meta);
    }

    public void annotate(Metadata meta) {
        for (String m : meta) {
            switch (meta.type(m)) {
                case Metadata.MORE:
                    setMore(true);
                    break;
                case Metadata.RELAX:
                    setRelax(true);
                    break;
                case Metadata.DEBUG:
                    setDebug(true);
                    break;
                case Metadata.SERVICE:
                    defService(meta.getValues(m));
                    break;
            }
        }
    }

    public Expression defineBody(ExpressionList lexp) {
        Expression exp;
        if (lexp.size() == 0) {
            exp = Constant.create(true);
        } else if (lexp.size() == 1) {
            exp = lexp.get(0);
        } else {
            exp = createFunction(Processor.SEQUENCE, lexp);
        }
        return exp;
    }

    public Term ifThenElse(Expression ei, Expression et, Expression ee) {
        Term exp = createFunction(Processor.IF, ei);
        exp.add(et);
        if (ee == null) {
            ee = Constant.create(true);
        }
        exp.add(ee);
        return exp;
    }

    public Term set(Variable var, Expression exp) {
        return Term.function(Processor.SET, var, exp);
    }

    /**
     * let (var = exp, body)
     *
     * @param el
     * @param body
     * @return
     */
    public Term let(ExpressionList el, Expression body) {
        return defineLet(el, body, 0);
    }

    public Term defineLet(ExpressionList el, Expression body, int n) {
        if (n == el.size() - 1) {
            return let(el.get(n), body);
        }
        return let(el.get(n), defineLet(el, body, n + 1));
    }
    
    /**
     * exp: var = ee | match(var_1, .., var_n) = ee
     * match(?x, ?y) is AST for let ((?x, ?y) = ee)
     * this match() AST is compiled by Processor
     * nested: let (((?x, ?y)) = select where)
     */
    Term let(Expression exp, Expression body) {
        if (exp.getArg(0).isTerm() && exp.getArg(0).getTerm().isNested()) {
            return let(exp.getArg(0).getTerm().getNestedList(), exp.getArg(1), body);
        }
        return new Let(exp, body);
    }
          
    /**
     * let(varList = exp)
     * compile as 
     * let (var = xt:get(exp, 0), match(varList) = var){ body }
     * use case: let (((?x, ?y)) = select where)
     * get first Mapping, match it
     * use case: let (((?var, ?val)) = ?m)
     * get first Binding, match it
     */
    
     Term let(ExpressionList expList, Expression exp, Expression body) { 
         if (! exp.isTerm() || expList.getList().size() == 1){
            return let(expList, exp, body, 0);
         }
         // let (var = exp)
         Variable var = new Variable(LET_VAR + nbd++);
         return let(defLet(var, exp), let(expList, var, body, 0));
     }
    
    // recurse on  expList 
    Term let(ExpressionList expList, Expression exp, Expression body, int n) { 
        Variable var = new Variable(LET_VAR + nbd++);
        ExpressionList list = expList.getList().get(n) ;
        Term fst = defGet(var, exp, n);
        Term snd = defLet(list, var);  
        Expression rest =  body;
        if (n+1 < expList.getList().size()){
            rest = let(expList, exp, body, n+1);
        }
        return new Let(fst, new Let(snd, rest));
    }

    public Term defLet(Variable var, Constant type, Expression exp) {
        return Term.create("=", var, exp);
    }
    
    public Term defLet(Variable var, Expression exp) {
        return Term.create("=", var, exp);
    }
    
    public Term defLet(ExpressionList lvar, Expression exp) {
        complete(lvar, exp, true);
        Term t = createFunction(Processor.MATCH, lvar);
        t.setNestedList(lvar);
        t.setNested(lvar.isNested());
        return Term.create("=", t, exp);
    }
    
    /**
     * let (match(?x, ?p, ?y) = ?l) {} ::= 
     * let (?x = xt:get(?l, 0), ?p =
     * xt:get(?l, 1), ?y = xt:get(?l, 2)) {}
     *
     */
    void processMatch(Let term) {
        Expression match = term.getVariableDefinition().getArg(0);
        Expression list  = term.getDefinition();

        if (match.isFunction() && match.getLabel().equals(Processor.MATCH)) {
            ExpressionList l = new ExpressionList();

            Variable var;
            if (list.isVariable()) {
                var = list.getVariable();
            } else {
                // eval list exp once, store it in variable
                var = Variable.create(LET_VAR + nbd++);
                l.add(defLet(var, list));
            }

            ExpressionList nestedList =  match.getTerm().getNestedList();
            boolean isRest  = nestedList != null && nestedList.isRest();
            int subList     = nestedList.getSubListIndex();
            int lastElement = nestedList.getLastElementIndex();
            int nbLast      = 0;
            if (subList >= 0 && lastElement >= 0) {
                nbLast = nestedList.size() - lastElement;
            }
            int last = nestedList.size() - 1;
            /**
             * (?a ?b | ?l . ?c ?d)
             * subList = 2  -- index of ?l subList variable
             * lastElem = 3 -- index of last elements variable ?c            /**
             * (?a ?b | ?l . ?c ?d)
             * subList = 2  -- index of ?l subList variable
             * lastElem = 3 -- index of last elements variable ?c
             */

            for (int i = 0; i < match.getArgs().size(); i++) {
                Expression arg = match.getArg(i);
                
                if (i == subList) {
                    l.add(defRest(arg.getVariable(), var, i, nbLast));
                }
                else if (lastElement >= 0 && i >= lastElement) {
                    l.add(defGenericGetLast(arg.getVariable(), var, last - i));
                }
                
//                if (isRest && i == match.getArgs().size() - 1) {
//                    l.add(defRest(arg.getVariable(), var, i));
//                } 
                else {
                    l.add(defGenericGet(arg.getVariable(), var, i));
                }
            }                        

            Term let = defineLet(l, term.getBody(), 0);
            term.setArgs(let.getArgs());
        }
    }
    
    /**
         * map(rq:fun, ?list)
         * -> 
         * map(lambda(?x){ rq:fun(?x) }, ?list)
         */
    void processMap(Term term) {
        if (term.getArgs().size() > 1) {
            Expression fst = term.getArg(0);
            if (fst.isConstant()) {
                Constant cst = fst.getConstant();
                if (isDefined(cst.getLabel())) {
                    Function fun = defineLambda(cst, arity(term));
                    term.setArg(0, fun);
                }
            }
        }
    }
    
    /**
     * 
     * aggregate(?x, xt:mediane) ->
     * aggregate(?x, xt:mediane(?y))
     * TODO: fix it as above
     */
    void processAggregate(Term term) {
        if (term.getArgs().size() == 2) {
            Expression rst = term.getArg(1);
            if (rst.isConstant()) {
                Term fun = createFunction(rst.getConstant());
                Variable var = ASTQuery.createVariable("?_agg_var");
                fun.add(var);
                term.setArg(1, fun);
            }
        }
    }
    
      int arity(Term t){
          if (t.getLabel().equals(Processor.REDUCE)){
              return 2;
          }
          return t.getArgs().size() - 1;
      }
      
      boolean isDefined(String uri){
          return Processor.getOper(uri) != ExprType.UNDEF;
      }
    
    /**
     * exp = exists { select where }
     * use case: let (select where)
     */
    void complete(ExpressionList lvar, Expression exp, boolean nest) {
        if (lvar.isEmpty() && ! lvar.isNested() && exp.isTerm()) {
            Exp query = exp.getTerm().getExistContent();
            if (query != null){
                ASTQuery ast = query.getQuery();
                ast.validate();
                ExpressionList el = new ExpressionList();
                for (Variable var : ast.getSelectVariables()) {
                    el.add(var);
                }
                lvar.add(el);
            }
        }
    }
    
    /**
     * exp = exists { select where }
     * use case: for (select where)
     */
    void complete(ExpressionList lvar, Expression exp) {
        if (lvar.isEmpty() && !lvar.isNested() && exp.isTerm()) {
            Exp query = exp.getTerm().getExistContent();
            if (query != null){
                ASTQuery ast = query.getQuery();
                ast.validate();
                for (Variable var : ast.getSelectVariables()) {
                    lvar.add(var);
                }
            }
        }
    }
    
    public Term defLet2(ExpressionList lvar, Expression exp) {
        Term t = createFunction(Processor.MATCH, lvar);
        t.setNestedList(lvar);
        t.setNested(lvar.isNested());
        return Term.create("=", t, exp);
    }
    /**
     * 
     * 
     * @param var
     * @param exp
     * @param i
     * @return 
     */
    
    Term defGenericGet(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GGET), exp);
        fun.add(Constant.createString(var.getLabel()));
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    /**
     * ith element of the target list in reverse order
     * i = 0 => last element
     * 
     */
    Term defGenericGetLast(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_LAST), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    Term defRest(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GREST), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    /**
     * Generate a subList starting at ith element of target list
     * nbLast is the number of last elements of target list not to include in the subList
     */
    Term defRest(Variable var, Expression exp, int i, int nbLast) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GREST), exp);
        fun.add(Constant.create(i));
        fun.add(Constant.create(nbLast));
        return defLet(var, fun);
    }
    
    Term defGet(Variable var, Expression exp, int i) {
        Term fun = createFunction(createQName(Processor.FUN_XT_GET), exp);
        fun.add(Constant.create(i));
        return defLet(var, fun);
    }
    
    public Term defineLoop(Variable var, ExpressionList lvar, 
            Expression exp, Expression body, boolean isLoop){
        if (lvar == null){
            return (isLoop) ? defLoop(var, exp, body) : defFor(var, exp, body);
        }
        else {
            return (isLoop) ? defLoop(lvar, exp, body) : defFor(lvar, exp, body);
        }     
    }

    public Term defFor(Variable var, Expression exp, Expression body) {
        return new ForLoop(var, exp, body);
    }

    /**
     * for ((?s, ?p, ?o) in exp){body} -> for (?var in exp){ let ((?s, ?p, ?o) =
     * ?var){body}} }
     */
    public Term defFor(ExpressionList lvar, Expression exp, Expression body) {
        Variable var = new Variable(FOR_VAR + nbd++);
        complete(lvar, exp);
        return defFor(var, exp, let(defLet(lvar, var), body));
    }
    
    /*
     * loop (var in exp) {body}
     * ::=
     * let (?list = xt:list()){
     *   for (var in exp){
     *     xt:add(body, ?list)
     *   }
     *   reduce(rq:concat, ?list)
     * }
     */
    public Term defLoop(Variable var, Expression exp, Expression body) {
        Variable list = new Variable("?_list_" + nbd++);
        Expression let = defLet(list, createFunction(createQName("xt:list")));
        Expression add = createFunction(createQName("xt:add"), list, body);
        Expression loop = new ForLoop(var, exp, add);
        Expression app  = 
                createFunction(Constant.createResource("reduce"), 
                createQName("rq:concat"), list);
        Expression stmt = createFunction(Constant.createResource("sequence"), 
                loop, app);
        return let(let, stmt);
    }

    /**
     * loop ((?s, ?p, ?o) in exp){body} -> for (?var in exp){ let ((?s, ?p, ?o) =
     * ?var){body}} }
     */
    public Term defLoop(ExpressionList lvar, Expression exp, Expression body) {
        Variable var = new Variable(FOR_VAR + nbd++);
        return defLoop(var, exp, let(defLet(lvar, var), body));
    }

    public void exportFunction(Expression def) {
        def.getArg(0).setPublic(true);
        def.setPublic(true);
    }
    
    public Term createFunction(Constant name) {
        Term term = createFunction(name.getName());
        term.setCName(name);
        return term;
    }

    public Term createFunction(Constant name, Expression exp) {
        Term term = createFunction(name.getName(), exp);
        term.setCName(name);
        return term;
    }

    public Term createFunction(Constant name, Expression exp, Expression e2) {
        Term term = createFunction(name, exp);
        term.add(e2);
        return term;
    }
    
    public Term createFunction(Constant name, Expression e1, Expression e2, Expression e3) {
        Term term = createFunction(name, e1, e2);
        term.add(e3);
        return term;
    }
    
    public Term createFunction(Constant name, ExpressionList el) {
        Term term = createFunction(name.getName(), el);
        term.setCName(name);
        return term;
    }  
    
    
    public Term createFunction(String name) {
        String longName = getNSM().toNamespace(name);
        Term term = Term.function(name, longName);
        // no toNamespaceB()
        term.setLongName(longName);
        term.setAST(this);
        return term;
    }
    
    public Term createFunction(String name, ExpressionList el) {
        if (name.equals(Processor.MAPFUN)){
            return createMapfun(name, el);
        }
        return createFun(name, el);
    }
    
    public Term createFunction(String name, Expression expression1) {
        Term term = createFunction(name);
        term.add(expression1);
        return term;
    }



 
    
    public Term createReturn(Expression exp) {
        Term term = createFunction(Processor.RETURN);
        term.setCName(Constant.createResource(Processor.RETURN));
        term.add(exp);
        return term;
    }

        
    Term createFun(String name, ExpressionList el) {
        Term term = createFunction(name);
    
        term.setModality(el);
        for (Expression exp : el) {
            term.add(exp);
        }
        return term;
    }
    
        // at runtime
    public Term createFunction(String name, ArrayList<Expression> args) {
        Term t = createFunction(name);
        t.setArgs(args);
        t.compile(this);
        return t;
    }
    
     /**
     * mapfun(st:concat, us:cell, ?list) 
     * ::=
     * apply(st:concat, maplist(us:cell, ?list))
     */
     public Term createMapfun(String name, ExpressionList el) {
         ExpressionList list = new ExpressionList();
         list.add(el.get(0));
         el.remove(0);
         Term maplist = createFunction(Processor.MAPLIST, el);
         list.add(maplist);
         Term mapfun = createFunction(Processor.REDUCE, list);
         return mapfun;
     }
    

    public Triple createTriple(Expression exp) {
        checkBlank(exp);
        return Triple.create(exp);
    }

    public Term createList(ExpressionList el) {
        Term list = Term.list();
        for (Expression exp : el) {
            list.add(exp);
        }
        return list;
    }

    public Term negation(Expression e) {
        return Term.negation(e);
    }

    public RDFList createRDFList(List<Expression> list) {
        return createRDFList(list, 0);
    }

    public void setListType(int n) {
        listType = n;
    }

    public int getListType() {
        return listType;
    }


    public Constant createLDSList(IDatatype dt){
//        Constant list = Constant.createBlank("_:list");
//        list.setDatatypeValue(dt);
        Constant list = Constant.create(dt);
        return list;
    }
    
     /**
     * Create an RDF List (rdf:first/rdf:rest) if close = true, end by rdf:nil
     * (usual case) Return an RDFList which is an And on the triples Can get
     * starting first blank node with function head() i.e. the subject of first
     * triple
     */   
    public RDFList createRDFList(List<Expression> list, int arobase) {
        RDFList rlist = new RDFList(newBlankNode(), list);
        if (arobase == L_DEFAULT) {
            arobase = listType;
        }
        switch (arobase) {

            case L_LIST:
                rlist = complete(rlist);
                break;

            case L_PATH:
                rlist = path(rlist);
                break;
        }
        return rlist;
    }

    RDFList complete(RDFList rlist) {
        Expression rest = null,
                blank = null;
        boolean isFirst = true;
        Exp triple;

        for (Expression exp : rlist.getList()) {

            if (isFirst) {
                blank = rlist.head();
                isFirst = false;
            } else {
                blank = newBlankNode();
            }

            if (rest != null) {
                triple = generateRest(rest, blank);
                rlist.add(triple);
            }

            triple = generateFirst(blank, exp);
            rlist.add(triple);

            rest = blank;
        }

        triple = generateRest(rest, createQName(RDFS.qrdfNil));
        rlist.add(triple);
        return rlist;
    }

    /**
     * Create list of Property Paths rdf:rest* / rdf:first that match list
     * elements
     *
     * @return
     */
    public RDFList path(RDFList exp) {
        RDFList ll = new RDFList(exp.head(), exp.getList());
        Expression re = list();

        for (Expression ee : exp.getList()) {
            Triple t = createPath(exp.head(), re, ee);
            ll.add(t);
        }

        return ll;
    }

    Term list() {
        return Term.create(Term.RE_SEQ,
                Term.function(Term.STAR, createQName(RDFS.qrdfRest)),
                createQName(RDFS.qrdfFirst));
    }

    static Term createTerm(String s) {
        Term term = new Term(s);
        return term;
    }

    public Term createGet(Expression exp, int n) {
        return Term.function("get", exp, Constant.create(n));
    }

    public static Variable createVariable(String s) {
        return Variable.create(s);
    }

    public static Variable createVariable(String s, ASTQuery aq) {
        Variable var = createVariable(s);
        // if we are in "describe *", add this variable to the list of variable to describe
        // notice: if the variable is already in the list, it won't add it again
        if (aq.isDescribeAll()) {
            aq.setDescribe(var);
        }
        return var;
    }

    // ex:name or <uri>
    public Constant createConstant(String s) {
        Constant cst = Constant.createResource(s, getNSM().toNamespaceB(s));
        return cst;
    }

    public Constant createQName(String qname) {
        String uri = getNSM().toNamespaceB(qname);
        Constant cst = Constant.createResource(qname, uri);
        if (qname.equals(uri)) {
            addError("Undefined prefix: ", qname);
        }
        cst.setQName(true);
        return cst;
    }
    
    // only for defined namespaces
    public Constant createQNameURI(String uri) {
        String qname = getNSM().toPrefix(uri, true);
        Constant cst = Constant.createResource(qname, uri);
        if (! uri.equals(qname)) {
           cst.setQName(true); 
        }       
        return cst;
    }
    

    // <uri>
    public Constant createURI(String s) {
        Constant cst = Constant.createResource(s, getNSM().toNamespaceB(s));
        return cst;
    }

    /*
     * Draft property regexp
     */
    public Constant createProperty(Expression exp) {
        if (exp.isConstant()) {
            // no regexp, std property
            return exp.getConstant();
        }
        return createExpProperty(exp);
    }

    Constant createExpProperty(Expression e) {
        Constant cst = createConstant(RootPropertyQN);
        cst.setExpression(e);
        return cst;
    }

    public Triple createTriple(Atom predicate, List<Atom> list) {
        Triple t = createTriple(list.get(0), predicate, list.get(1));
        // triple receive list with additional args only (so remove subject and object from list)
        list.remove(0);
        list.remove(0);
        t.setArgs(list);
        return t;
    }

    /**
     * Create a triple or a path for SPARQL JJ Parser
     */
    public Triple createTriple(Expression subject, Atom predicate, Expression object) {
        Expression exp = predicate.getExpression();
        Variable var = predicate.getIntVariable();
        Triple t;
        if (exp == null) {
            t = triple(subject, predicate, object);
        } else {
            t = createPath(subject, predicate, object, exp);
        }
        return t;
    }
    
    
    /**
     * THE function to use to create triples 
     */
    public Triple triple(Expression subject, Atom predicate, Expression object) {
        Triple t = Triple.create(subject, predicate, object);
        getGlobalAST().submit(t);
        return t;
    }
    
    void submit(Triple t) {
        if (t.isPath()) {
            for (Constant pred : t.getRegex().getPredicateList()) {
                submit(pred);
            }
        }
        else if (t.getPredicate().isConstant()) {
            submit(t.getPredicate().getConstant());
            record(t);
        }
    }
    
    void record(Triple t) {
        tripleList.add(t);
    }
    
    public List<Triple> getTripleList() {
        return tripleList;
    }
    
    void submit(Constant p){
        if (! predicateList.contains(p)) {
            predicateList.add(p);
        }
    }
    
    public List<Constant> getPredicateList() {
        return predicateList;
    }

    public Triple createPath(Expression subject, Expression exp, Expression object) {
        Constant predicate = createProperty(exp);
        predicate.setExpression(exp);
        Triple t = createPath(subject, predicate, object, exp);
        return t;
    }

    /**
     * Create a Triple that contains a Property Path with exp as PP expression
     */
    public Triple createPath(Expression subject, Atom predicate, Expression object, Expression exp) {
        Triple t = Triple.create(subject, predicate, object);
        // property path or xpath
        Variable var = t.getVariable();
        if (var == null) {
            var = new Variable(SYSVAR + nbd++);
            var.setBlankNode(true);
            t.setVariable(var);
        }
        if (exp.getName().equals(Term.XPATH)) {
            t.setRegex(exp);
            return t;
        }

        var.setPath(true);
        String mode = "";
        boolean isDistinct = false,
                isShort = false;

        while (true) {
            if (exp.isFunction()) {

                if (exp.getName().equals(DISTINCT)) {
                    exp = exp.getArg(0);
                    //mode += DISTINCT;
                    isDistinct = true;
                } else if (exp.getName().equals(SSHORT)) {
                    exp = exp.getArg(0);
                    mode += "s";
                    isShort = true;
                } else if (exp.getName().equals(SSHORTALL) || exp.getName().equals(SHORT)) {
                    exp = exp.getArg(0);
                    mode += "sa";
                    isShort = true;
                } else {
                    break;
                }
            } else {
                break;
            }
        }


        exp.setDistinct(isDistinct);
        exp.setShort(isShort);
        t.setRegex(exp);
        t.setMode(mode);
        submit(t);
        return t;

    }

    // regex only
    public Expression createOperator(String ope, Expression exp) {
        Term fun = null;
        if (ope.equals(SINV) || ope.equals(SBE)) {
            fun = Term.function(ope, exp);
        } else if (ope.equals(SMULT)) {
            fun = star(exp);
        } else if (ope.equals(SPLUS)) {
            if (true){ //isKgram()) {
                // first exp is member of visited (SPARQL 1.1)
                // for checking loop
                fun = createOperator(1, Integer.MAX_VALUE, exp);
                fun.setPlus(true);
            } else {
                fun = sequence(exp, Term.function(Term.STAR, exp));
            }
        } else if (ope.equals(Keyword.SQ)) {
            fun = Term.function(Term.OPT, exp);
        } else {
            fun = Term.function(ope, exp);
        }
        return fun;
    }

    public Expression createOperator(String ope, Expression exp1, Expression exp2) {
        if (ope.equals(SOR)) {
            ope = SEOR;
        }
        return createTerm(ope, exp1, exp2);
    }

    /**
     * exp is a subquery nest it in Term exists { exp } use case: for (?m in
     * select where){}
     */
    public Term term(Exp exp) {
        return term(exp, null);
    }
       
    public Term term(Exp exp, Expression graph) {
        Term t = createExist(exp, false);
        if (graph != null){
            t.add(graph);
        }
        // return all Mapping of subquery:
        t.setSystem(true);
        return t;
    }

    public Term createExist(Exp exp, boolean negation) {
        Term term = Term.function(Term.EXIST);
        term.setExist(Exist.create(exp));
        if (negation) {
            term = negation(term);
        }
        return term;
    }

    /**
     * foaf:knows
     *
     * @[a foaf:Person] foaf:knows
     * @{?this a foaf:Person} foaf:knows
     * @{filter(?this != ex:John)}
     */
    public Expression createRegexTest(Expression prop, Exp test) {
        Expression exp;
        if (test.size() == 1 && test.get(0).isFilter()) {
            exp = test.get(0).getFilter();
        } else {
            exp = createExist(test, false);
        }
        return setRegexTest(prop, exp);
    }

    /**
     * Filter test associated to path regex exp
     */
    public Expression setRegexTest(Expression exp, Expression test) {
        regexExpr.put(exp, test);
        Expression tt = Term.function(Term.TEST);
        tt.setExpr(test);
        Expression seq = sequence(exp, tt);
        return seq;
    }

    public Collection<Expression> getRegexTest() {
        return regexExpr.values();
    }

    Term star(Expression exp) {
        return Term.function(Term.STAR, exp);
    }

    Term sequence(Expression e1, Expression e2) {
        return Term.create(Term.RE_SEQ, e1, e2);
    }

    Expression alter(Expression e1, Expression e2) {
        return Term.create(Term.RE_ALT, e1, e2);

    }

    public Expression createOperator(String s1, String s2, Expression exp) {
        int n1 = 0, n2 = Integer.MAX_VALUE;
        if (s1 != null) {
            n1 = Integer.parseInt(s1);
        }
        if (s2 != null) {
            n2 = Integer.parseInt(s2);
        }
        Term t = createOperator(n1, n2, exp);
        t.setCount(true);
        return t;
    }

    Term createOperator(int n1, int n2, Expression exp) {
        Term t = star(exp);
        t.setMin(n1);
        t.setMax(n2);
        return t;
    }

    public Constant createConstant(String s, String datatype) {
        return createConstant(s, datatype, null);
    }

    // Literal
    public Constant createConstant(String s, String datatype, String lang) {
        if (datatype == null) {
            datatype = datatype(lang);
        } else if (!knownDatatype(datatype)) {
            datatype = getNSM().toNamespaceB(datatype);
        }
        //s = clean(s);
        return Constant.create(s, datatype, lang);
    }

    String datatype(String lang) {
        return DatatypeMap.datatype(lang);
    }

    private boolean knownDatatype(String datatype) {
        if (datatype.startsWith(RDFS.XSD)
                || datatype.startsWith(RDFS.XSDPrefix)
                || datatype.startsWith(RDFS.RDF)
                || datatype.startsWith(RDFS.RDFPrefix)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * used for collections
     */
    public Exp generateFirst(Expression expression1, Expression expression2) {
        Atom atom = createQName(RDFS.qrdfFirst);
        Triple triple = triple(expression1, atom, expression2);
        return triple;
    }

    public Exp generateRest(Expression expression1, Expression expression2) {
        Atom atom = createQName(RDFS.qrdfRest);
        Triple triple = triple(expression1, atom, expression2);
        return triple;
    }


    public void setList(boolean b) {
        this.setJoin(!b);
    }

    public void setCorrect(boolean b) {
        isCorrect = b;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean b) {
        isCheck = b;
    }

    public boolean isNosort() {
        return nosort;
    }

    public void setNosort(boolean b) {
        nosort = b;
    }

    public boolean isSorted() {
        return sorted;
    }

    public void setSorted(boolean b) {
        sorted = b;
    }

    public int getOffset() {
        return Offset;
    }

    public void setOffset(int offset) {
        Offset = offset;
    }

    public int getResultForm() {
        return resultForm;
    }

    public void setResultForm(int resultForm) {
        this.resultForm = resultForm;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public boolean isInsert() {
        return isAdd;
    }

    public void setAdd(boolean b) {
        isAdd = b;
    }

    public void setWhere(boolean b) {
        where = b;
    }

    public boolean isWhere() {
        return where;
    }

    public long getNbBNode() {
        nbBNode++;
        return nbBNode;
    }

    public Variable getBlankNode(String label) {
        if (isRenameBlankNode()) {
            return newBlankNode();
        } else {
            return newBlankNode(label);
        }
    }

    public Variable newBlankNode() {
        return newBlankNode(BNVAR + getNbBNode());
    }

    public Variable metaVariable() {
        return newBlankNode();
    }

    public Variable newBlankNode(String label) {
        Variable var = createVariable(label);
        var.setBlankNode(true);
        return var;
    }

    /**
     * Reset tables when start a new query (update)
     */
    public void reset() {
        if (blank != null) {
            blank.clear();
        }
        if (blankNode != null) {
            blankNode.clear();
        }
    }

    /**
     * Same blank label must not be used in different BGP exp except in insert
     * data {}
     */
    public Variable newBlankNode(Exp exp, String label) {
        if (blank == null) {
            blank = new HashMap<String, Exp>();
            blankNode = new HashMap<String, Variable>();
        }

        if (!isInsertData()) {
            Exp ee = blank.get(label);

            if (ee == null) {
                blank.put(label, exp);
            } else if (ee != exp) {
                setCorrect(false);
                logger.error("Blank Node used in different patterns: " + label);
                addError("Blank Node used in different patterns: ", label);
            }
        }

        Variable var = blankNode.get(label);
        if (var == null) {
            // create a new blank node and put it in the table
            //var = newBlankNode();
            var = getBlankNode(label);
            blankNode.put(label, var);
        }
        return var;
    }

    /**
     * use case: select sql() as (?x, ?y)
     *
     * @param var1
     * @param var2
     */
    public void addVariable(Variable var1, Variable var2) {
        var1.addVariable(var2);
    }


    public void setDescribe(Atom at) {
        setResultForm(QT_DESCRIBE);
        for (Atom aa : adescribe) {
            if (aa.getLabel().equals(at.getLabel())) {
                return;
            }
        }
        adescribe.add(at);
    }

    public List<Atom> getDescribe() {
        return adescribe;
    }

    public void setDescribeAll(boolean b) {
        describeAll = b;
    }

    boolean isDescribeAll() {
        return describeAll;
    }


    public boolean isSPARQLCompliant() {
        return isSPARQLCompliant;
    }

    public void setSPARQLCompliant(boolean b) {
        isSPARQLCompliant = b;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean b) {
        isBind = b;
    }


    public int getVariableId() {
        return nbd++;
    }

    /**
     * *************************************************************
     *
     * Compile AST
     *
     **************************************************************
     */
    public void compile() {
        if (isConstruct() && getBody() != null) {
            compileConstruct();
        } else if (isAsk()) {
            compileAsk();
        } else if (isDescribe()) {
            compileDescribe();
            setBasicSelectAll(true);
        } else if (isTemplate()) {
            compileTemplate();
        }
        Exp exp = getBody();
        if (exp != null) {
            setQuery(exp);
        }
    }

    // TODO: clean
    private void compileConstruct() {
        if (getConstruct() != null) {
            // kgram:
            setInsert(getConstruct());
            Exp exp = getConstruct();
            //Env env = new Env(false);
            // assign graph ?src variable to inner triples
            // TODO: for backward rules only
            //exp.setSource(env, null, false);
            setConstruct(exp);
        } else if (getInsert() != null) {
            // kgram update
            setConstruct(getInsert());
        }
    }

    /**
     * compile describe ?x as:
     *
     * construct { {?x ?p ?y} union {?y ?p ?x} } where { {?x ?p ?y} union {?y ?p
     * ?x} }
     */
    private void compileDescribe() {
        String root = KGRAMVAR;
        String PP = root + "p_";
        String VV = root + "v_";

        Exp bodyExpLocal = getBody();
        int size = bodyExpLocal.size();

        boolean describeAllTemp = isDescribeAll();
        setDescribeAll(false);

        BasicGraphPattern template = BasicGraphPattern.create();

        for (Atom atom : adescribe) {

            if (atom.isVariable()) {
                // TODO: compile only if variable is in the where clause
                Variable var = atom.getVariable();
                if (!getSelectAllVar().contains(var)) {
                    continue;
                }
            }

            //// create variables
            int nbd = getVariableId();
            Variable prop1 = createVariable(PP + nbd);
            Variable val1 = createVariable(VV + nbd);

            nbd = getVariableId();
            Variable prop2 = createVariable(PP + nbd);
            Variable val2 = createVariable(VV + nbd);

            //// create triple sd ?p0 ?v0
            Triple triple = Triple.create(atom, prop1, val1);
            Exp e1 = triple;
            BasicGraphPattern bgp1 = BasicGraphPattern.create(e1);
            template.add(e1);

            //// create triple ?v0 ?p0 sd
            Triple triple2 = Triple.create(val2, prop2, atom);
            Exp e2 = triple2;
            BasicGraphPattern bgp2 = BasicGraphPattern.create(e2);
            template.add(e2);

            //// create the union of both
            Or union = new Or();
            union.add(bgp1);
            union.add(bgp2);

            // make the union optional
            Option opt = Option.create(BasicGraphPattern.create(union));

            bodyExpLocal.add(opt);

            if (atom.isVariable()) {
                setSelect(atom.getVariable());
            }
        }

        setDescribeAll(describeAllTemp);
        setBody(bodyExpLocal);

        if (true){ //isKgram()) {
            setInsert(template);
            setConstruct(template);
        }
    }

    private void compileAsk() {
        setMaxResult(1);
    }

   
    /**
     * **********************************************************
     *
     * Pretty Printer
     *
     ***********************************************************
     */
    
    public String toJava() throws IOException{
          JavaCompiler jc = new JavaCompiler();
          jc.toJava(this);
          return jc.toString();
    }
    
    @Override
    public String toString() {
       ASTPrinter pr = new ASTPrinter(this);
       return pr.toString();
    }
    
   

     boolean isData() {
        return isInsertData() || isDeleteData();
    }

    

    public void setConstruct(Exp constructExp) {
        this.setResultForm(QT_CONSTRUCT);
        this.constructExp = constructExp;
    }

    public void duplicateConstruct(Exp exp) {
        boolean check = checkConstruct(exp);
        if (check) {
            setConstruct(exp);
        } else {
            setConstruct(null);
        }
    }

    /**
     * construct where {exp} construct = duplicate(exp) and exp should have no
     * filter and no graph pattern
     */
    boolean checkConstruct(Exp body) {
        for (Exp exp : body.getBody()) {
            if (!exp.isTriple() || exp.isExp()) {
                return false;
            }
        }
        return true;
    }

    public Exp getConstruct() {
        return constructExp;
    }

    public void setInsert(Exp exp) {
        this.construct = exp;
    }

    public Exp getInsert() {
        return construct;
    }

    public void setDelete(Exp exp) {
        this.delete = exp;
    }

    public Exp getDelete() {
        return delete;
    }

    public static int getTripleId() {
        return nbt++;
    }

    public Exp getBody() {
        return bodyExp;
    }

    public Exp getSaveBody() {
        return bodySave;
    }

    public void setSaveBody(Exp exp) {
        bodySave = exp;
    }

    public Exp getHead() {
        return constructExp;
    }

    public void setBody(Exp bodyExp) {
        this.bodyExp = bodyExp;
    }

    public void setPragma(String name, Exp exp) {
        if (pragma == null) {
            pragma = new HashMap<String, Exp>();
        }
        if (name == null) {
            name = RDFS.COSPRAGMA;
        } else {
            name = getNSM().toNamespace(name);
        }
        if (exp == null) {
            pragma.remove(name);
        } else {
            pragma.put(name, exp);
        }
    }

    public void setPragma(Exp exp) {
        setPragma(RDFS.COSPRAGMA, exp);
    }

    public Exp getPragma(String name) {
        if (pragma == null) {
            return null;
        }
        return pragma.get(name);
    }

    public Exp getPragma() {
        return getPragma(RDFS.COSPRAGMA);
    }

    public boolean hasPragma(String subject, String property, String object) {
        if (getPragma() == null) {
            return false;
        }
        for (Exp exp : getPragma().getBody()) {
            if (exp.isRelation()) {
                Triple t = (Triple) exp;
                if (t.getSubject().getName().equals(subject)
                        && t.getProperty().getName().equals(property)
                        && t.getObject().getName().equals(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addPragma(Triple t) {
        Exp pragma = getPragma();
        if (pragma == null) {
            pragma = BasicGraphPattern.create();
            setPragma(pragma);
        }
        pragma.add(t);
    }

    public Exp getPrefixExp() {
        return prefixExp;
    }

    /**
     * Note: only for pretty print, do not really add the prefix in NSManager
     *
     * @param t
     */
    public void addPrefixExp(Triple t) {
        prefixExp.add(t);
    }

    public void setPrefixExp(Exp exp) {
        prefixExp = exp;
    }

    public void definePrefix(String prefix, String ns) {
        defNamespace(prefix, ns);
    }

    public void defNamespace(String prefix, String ns) {
        defNSNamespace(prefix, ns);
        defPPNamespace(prefix, ns);
    }
    
    void defService(List<String> list){
        if (list != null && ! list.isEmpty()){
            defService(list.get(0));
            setServiceList(new ArrayList<Atom>());
            for (String serv : list){
                getServiceList().add(Constant.createResource(serv));
            }
        }
    }

    public void defService(String ns) {
        service = ns;
    }

    public String getService() {
        return service;
    }

    public boolean hasService() {
        return service != null;
    }

    public void defNSNamespace(String prefix, String ns) {
        if (prefix.endsWith(":")) {
            prefix = prefix.substring(0, prefix.length() - 1); // remove :
        }
        getNSM().defNamespace(ns, prefix);
    }

    public void defPPNamespace(String prefix, String ns) {
        if (prefix.endsWith(":")) {
            prefix = prefix.substring(0, prefix.length() - 1); // remove :
        }
        Triple triple = Triple.createNS(
                Constant.create(KeywordPP.PREFIX), Constant.create(prefix),
                Constant.create(ns));
        addPrefixExp(triple);
    }

    public void defBase(String ns) {
        defNSBase(ns);
        defPPBase(ns);
    }

    public void defPPBase(String ns) {
        Triple triple = Triple.createNS(
                Constant.create(KeywordPP.BASE), Constant.create(""),
                Constant.create(ns));
        addPrefixExp(triple);
    }

    public void defNSBase(String ns) {
        getNSM().setBase(ns);
    }

    public String defURI(String s) {
        return s;
    }

    public void setCount(String var) {
    }

    public void setSort(String var, boolean breverse) {
    }

    public void setSort(Expression sortExpression) {
        setSort(sortExpression, false);
    }

    public void setHaving(Exp exp) {
        if (exp.getBody().isEmpty()) {
            return;
        }
        Exp body = exp.getBody().get(0);
        if (!body.isTriple()) {
            return;
        }
        setHaving(body.getTriple().getExp());
    }

    public void setHaving(Expression exp) {
        having = exp;
    }

    public Expression getHaving() {
        return having;
    }

    public void setSort(Expression sortExpression, boolean breverse) {
        sort.add(sortExpression);
        reverseTable.add(new Boolean(breverse));
    }

    public List<Expression> getGroupBy() {
        return lGroup;
    }

    public boolean isGroupBy(String name) {
        for (Expression exp : lGroup) {
            if (exp.isVariable() && exp.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void setGroup(Expression exp) {
        if (exp.isVariable()) {
            setGroup(exp.getName());
        }
        lGroup.add(exp);
    }

    public void setGroup(Expression exp, Variable var) {
        if (var != null) {
            // use case: group by (exp as var)
            // generate:
            // select (exp as var)
            // group by var
            setSelect(var);
            setSelect(var, exp);
            setGroup(var);
        } else {
            setGroup(exp);
        }
    }

    public void setGroup(String var) {
    }

    public List<Variable> getVariableBindings() {
        if (values != null) {
            return values.getVariables();
        }
        return null;
    }

    public void clearBindings() {
        values = null;
    }

    public List<List<Constant>> getValueBindings() {
        if (values != null) {
            return values.getValues();
        }
        return null;
    }
    
    public Values complete(Values val){
        if (val.hasExpression()){
            Variable meta;
            
            if (val.getVariables().size() == 1){
                meta = val.getVariables().get(0);                
            }
            else {
               meta = metaVariable();
               for (Variable var : val.getVariables()){
                   meta.addVariable(var);
               }
            }
            
            Exp b = createBind(val.getExp(), meta);
            val.setBind(b); 
        }
       
        return val;
    }

    public void setValues(Values v) {
        values = v;
    }

    public Values getValues() {
        return values;
    }

    public void defSelect(Variable var, Expression exp) {
        checkSelect(var);
        if (exp == null) {
            setSelect(var);
        } else {
            setSelect(var, exp);
        }
    }

    public void setSelect(Variable var) {
        if (!selectVar.contains(var)) {
            selectVar.add(var);
        }
    }

    /**
     * Use case: collect select *
     */
    public void defSelect(Variable var) {
        //if (isSelectAll()){
        addSelect(var);
        //}
    }

    void addSelect(Variable var) {
        if (!selectAllVar.contains(var)) {
            selectAllVar.add(var);
        }
    }

    public boolean checkSelect(Variable var) {
        if (selectVar.contains(var)) {
            setCorrect(false);
            return false;
        }
        return true;
    }

    public void setSelect(Variable var, Expression e) {
        setSelect(var);
        if (getExpression(var) != null) {
            addError("Duplicate select : " + e + " as " + var);
        }
        selectFunctions.put(var.getName(), e);
        selectExp.put(e, e);

        if (var.getVariableList() != null) {
            // use case:
            // select sql() as (nn_0, nn_1)
            // compiled as :
            // select sql() as var   get(var, i) as nn_i
            // now generate get() for sub variables
            int n = 0;
            for (Variable vv : var.getVariableList()) {
                setSelect(vv);
            }
        }
    }

    public void setSelect() {
    }

    public List<Boolean> getReverse() {
        return reverseTable;
    }

    public String toSparql() {
        return toString();
    }

    public void setDescribe(boolean describe) {
        if (describe) {
            setResultForm(QT_DESCRIBE);
        }
    }

    public void setAsk(boolean b) {
        if (b) {
            setResultForm(QT_ASK);
        }
    }

    public void setSelect(boolean b) {
        if (b) {
            setResultForm(QT_SELECT);
        }
    }

    public void setTemplate(boolean b) {
        isTemplate = b;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public boolean isDescribe() {
        return (getResultForm() == QT_DESCRIBE);
    }

    public boolean isAsk() {
        return (getResultForm() == QT_ASK);
    }

    public boolean isConstruct() {
        return (getResultForm() == QT_CONSTRUCT);
    }

    public boolean isSelect() {
        return (getResultForm() == QT_SELECT);
    }

    public boolean isUpdate() {
        return (getResultForm() == QT_UPDATE);
    }

    public void setDelete(boolean b) {
        if (b) {
            setResultForm(QT_DELETE);
            isDelete = b;
        }
    }

    public void setInsert(boolean b) {
        if (b) {
            setResultForm(ASTQuery.QT_CONSTRUCT);
            setAdd(true);
        }
    }

    public boolean isDelete() {
        return isDelete;
    }

    public boolean isSPARQLQuery() {
        return isSelect() || isAsk() || isDescribe() || (isConstruct() && !isInsert());
    }

    public boolean isSPARQLUpdate() {
        return isUpdate() || isInsert() || isDelete();
    }

    public boolean isConstructCompiled() {
        return constructCompiled;
    }

    public void setConstructCompiled(boolean constructCompiled) {
        this.constructCompiled = constructCompiled;
    }

    public void setDefaultThreshold(float threshold) {
        DefaultThreshold = threshold;
        setThreshold(threshold);
    }

    public void setDefaultMaxProjection(int maxProjection) {
        DefaultMaxProjection = maxProjection;
        setMaxProjection(maxProjection);
    }

    public void setDefaultMaxLength(int maxLength) {
        DefaultMaxLength = maxLength;
    }

    public int getDefaultMaxLength() {
        return DefaultMaxLength;
    }

    public void setDefaultMaxResult(int maxResult) {
        DefaultMaxResult = maxResult;
        setMaxResult(maxResult);
    }

    public float getDefaultThreshold() {
        return DefaultThreshold;
    }

    public int getDefaultMaxProjection() {
        return DefaultMaxProjection;
    }

    public int getDefaultMaxResult() {
        return DefaultMaxResult;
    }

    public static String getRootPropertyQN() {
        return RootPropertyQN;
    }

    public static void setRootPropertyQN(String rootPropertyQN) {
        RootPropertyQN = rootPropertyQN;
    }

    public static String getRootPropertyURI() {
        return RootPropertyURI;
    }

    public static void setRootPropertyURI(String rootPropertyURI) {
        RootPropertyURI = rootPropertyURI;
    }

    public boolean isDefineExp(Expression exp) {
        return selectExp.get(exp) != null;
    }

    public Expression getExpression(String name) {
        return selectFunctions.get(name);
    }

    public Expression getExpression(Variable var) {
        return selectFunctions.get(var.getName());
    }

    boolean hasExpression(Variable var) {
        return getExpression(var) != null;
    }

    public Expression getExtExpression(String name) {
        Expression sexp = getExpression(name);
        if (sexp == null) {
            return null;
        }
        // rewrite var as exp
        return sexp.process(this);
    }

    public HashMap<String, Expression> getSelectFunctions() {
        return selectFunctions;
    }

    public void setSelectFunctions(HashMap<String, Expression> selectFunctions) {
        this.selectFunctions = selectFunctions;
    }

    public void set(ASTUpdate u) {
        setResultForm(ASTQuery.QT_UPDATE);
        astu = u;
        u.set(this);
    }

    public ASTUpdate getUpdate() {
        return astu;
    }
  
    void bind(Variable var) {
        if (!stack.contains(var)) {
            stack.add(var);
        }
    }

    boolean isBound(Variable var) {
        return stack.contains(var);
    }

    public List<Variable> getStack() {
        return stack;
    }

    void newStack() {
        stack = new ArrayList<Variable>();
    }

    void setStack(List<Variable> list) {
        stack = list;
    }

    void addStack(List<Variable> list) {
        for (Variable var : list) {
            bind(var);
        }
    }
    
       public boolean isRenameBlankNode() {
        return renameBlankNode;
    }

    public void setRenameBlankNode(boolean renameBlankNode) {
        this.renameBlankNode = renameBlankNode;
    }

     

    /**
     * *************************************************
     *
     * Template template { ?x ... } where {} -> select (st:process(?x) as ?px)
     * ... (concat(?px ...) as ?out) where {}
     *
     *************************************************
     */
    
    public ASTTemplate defineTemplate(){
        atemp = new ASTTemplate(this);
        return atemp;
    }
    
    public ASTTemplate getTemplate(){
        return atemp;
    }
    
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
     

    /**
     * template { "construct {" ?x "} where {" ?y "}" } -> select
     * (st:process(?x) as ?px) (st:process(?y) as ?py) (concat(.. ?px .. ?py ..)
     * as ?out)
     */
    void compileTemplate() {
        atemp.compileTemplate();
    }

    /**
     * Aggregate that build the result of a template when there are several
     * results default is group_concat draft: agg_and
     */
    public static void setTemplateAggregate(String s) {
        ASTTemplate.setTemplateAggregate(s);
    }

    public static void setTemplateConcat(String s) {
        ASTTemplate.setTemplateConcat(s);
    }

    public void setName(String label) {
        name = label;
    }

    public void setName(Constant cst) {
        name = cst.getLabel();
    }

    public String getName() {
        return name;
    }

    public boolean isAllResult() {
        return isAllResult;
    }

    private void setAllResult(boolean isAllResult) {
        this.isAllResult = isAllResult;
    }
     
    public Term getTemplateGroup() {
        return atemp.getTemplateGroup();
    }
   
    public void defArg(Variable var) {
        argList.add(var);
    }

    public List<Variable> getArgList() {
        return argList;
    }

    public void defProfile(Constant cst) {
        profile = cst.getLabel();
    }

    public String getProfile() {
        return profile;
    }

    /**
     * ********************************************************
     *
     * End of Template
     *
     ********************************************************
     */
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public void setApproximateSearchOptions(String key, String value) {
        if (this.approximateSearchOptions.containsKey(key)) {
            this.approximateSearchOptions.get(key).add(value);
        } else {
            List l = new ArrayList();
            l.add(value);
            this.approximateSearchOptions.put(key, l);
        }
    }

    public List<String> getApproximateSearchOptions(String key) {
        return this.approximateSearchOptions.get(key);
    }
}
