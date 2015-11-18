package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.api.ASTVisitable;
import fr.inria.acacia.corese.triple.api.ASTVisitor;
import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.printer.SPIN;
import fr.inria.acacia.corese.triple.update.ASTUpdate;
import fr.inria.edelweiss.kgram.api.query.Graphable;
import java.util.Map;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This class is the abstract syntax tree, it represents the initial query (except for get:gui).<br>
 * When complete, it will be transformed into a Query Graph.java.
 * <br>
 * @author Olivier Corby & Virginie Bottollier
 */

public class ASTQuery  implements Keyword, ASTVisitable, Graphable {

	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;

	/** logger from log4j */
	private static Logger logger = Logger.getLogger(ASTQuery.class);	
	
	static final String SQ1 = "\"";
	static final String SQ3 = "\"\"\"";
	static final String SSQ = "'";
	static final String SSQ3 = "'''";
	static final String BS = "\\\\";
	static final String ESQ = "\\\""; // occurrence of \" in a string
	static final String ESSQ = "\\'"; // occurrence of \'
	static String RootPropertyQN =  RDFS.RootPropertyQN; // cos:Property
	static String RootPropertyURI = RDFS.RootPropertyURI; //"http://www.inria.fr/acacia/corese#Property";
	static final String LIST = "list";
	public static final String KGRAMVAR = "?_ast_";
	public static final String SYSVAR = "?_cos_";
	public static final String BNVAR = "?_bn_";
        public static final String MAIN_VAR = "?_main_";
	static final String NL 	= System.getProperty("line.separator");

	static int nbt=0; // to generate an unique id for a triple if needed
	
	public final static int QT_SELECT 	= 0;
	public final static int QT_ASK 		= 1;
	public final static int QT_CONSTRUCT= 2;
	public final static int QT_DESCRIBE = 3;
	public final static int QT_DELETE 	= 4;
	public final static int QT_UPDATE 	= 5;
	public final static int QT_TEMPLATE = 6;

        public final static int L_PATH = 2;
        public final static int L_LIST = 1;
        public final static int L_DEFAULT = 0;

	/** if graph rule */
	boolean rule = false;
	boolean isConclusion = false;
	/** approximate projection */
	boolean more = false;
        boolean isDelete = false;
	/** default process join */
	boolean join = false;
	/** join result into one graph */
	boolean one = false;
	/** sparql bind */
	boolean XMLBind = true;
	boolean fake = false;
	/** select distinct where : all are distinct */
	boolean distinct = false;
	/** true : sparql, false : corese */
	boolean strictDistinct = true;
	/** relation on which join connex */
	boolean connex = false;
	boolean union = false;
    boolean hasScore = false;
	/** display blank node id */
	boolean displayBNID = false;
	/** display with rdf:resource */
	boolean flat = false;
	/** display in RDF */
	boolean rdf = false, isJSON = false;
	/** display types of query */
	boolean pQuery = false;
	/** select * : select all variables from query */
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
    /** booleans useful for the sparql pretty printer */
    boolean where = false;
    boolean merge = false;
    /** used in QueryGraph.java to compile the construct */
    boolean constructCompiled = false;
    // construct in the std graph:
    boolean isAdd = false;
    boolean describeAll = false;
    // generate a relation for rdf:type
    boolean isRelationForType = true;
    boolean isKgram = true, 
    // true means with SPARQL 1.1 syntax for select (fun(?x) as ?y) where
    isSPARQL1 = true,
    isBind = false;
    
	/** max cg result */
	int MaxResult = Integer.MAX_VALUE;
	int DefaultMaxResult = Integer.MAX_VALUE;
	/** max projection */
	int MaxProjection = Integer.MAX_VALUE;
	int DefaultMaxProjection = Integer.MAX_VALUE;
	// path length max
	int DefaultMaxLength = 5;
	int MaxDisplay = 10000;
	/** Offset */
	int Offset = 0;
	int nbBNode = 0;
    int nbd = 0; // to generate an unique id for a variable if needed
	int resultForm = QT_SELECT;
	private int priority = 100;
	int countVar = 0;
	
	/** if more, reject 2 times worse projection than best one */
	float Threshold = 1;
	float DefaultThreshold = 1;
	//byte access = Cst.ADMIN;
	
	// predefined ns from server
	String namespaces, base;
	// relax by dd:distance
	String distance; 
	/** the source text of the query */
	String text = null;
	/** Represents the ASTQuery before compilation */
    String queryPrettyPrint = "";
 
	
    /** Source body of the query returned by javacc parser */
    Exp bodyExp, bodySave;
	/** Compiled triple query expression */
	Exp query ;
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
	List<Expression> sort 	 = new ArrayList<Expression>();
	List<Expression> lGroup  = new ArrayList<Expression>();
	List<Expression> relax   = new ArrayList<Expression>();

        private Dataset 
                // Triple store default dataset
                defaultDataset, 
                // from, from named, with
                dataset;
	List<Expression> template ; 

        List<Atom> adescribe = new ArrayList<Atom>();
	List<Variable> stack = new ArrayList<Variable>(); // bound variables
	List<String> vinfo;
	List<String> errors;
	
	List<Variable> varBindings;
	List<List<Constant>> valueBindings;
	Values values;
	
	List<Boolean> reverseTable = new ArrayList<Boolean>();
	
	Hashtable<String, Expression> selectFunctions = new Hashtable<String, Expression>();
	HashMap<String, Variable> varTemplate = new HashMap<String, Variable>();
        private Extension define;
        private HashMap <String, Expression> undefined;
	ExprTable selectExp   = new ExprTable();
	ExprTable regexExpr   = new ExprTable();

    // pragma {}
    Hashtable<String, Exp> pragma;
    Hashtable<String, Exp> blank;
    Hashtable<String, Variable> blankNode;
    
    HashMap<String, Atom> dataBlank; 

	NSManager nsm;
	ASTUpdate astu;



	public static final String OUT 		= "?out";
	public static final String IN 		= "?in";
	public static final String IN2 		= "?in_1";
        
	private static final String GROUPCONCAT = Processor.GROUPCONCAT;      
	private static final String CONCAT 	= Processor.CONCAT;
	private static final String COALESCE 	= Processor.COALESCE;
	private static final String IF 		= Processor.IF;
        
        private static String FUN_TEMPLATE_AGG      = Processor.FUN_AGGREGATE ; //Processor.FUN_GROUPCONCAT ;
        private static String FUN_TEMPLATE_CONCAT   = Processor.FUN_CONCAT ; 
        private static String FUN_TURTLE            = Processor.FUN_TURTLE ; 
        
 	private static final String FUN_PROCESS = Processor.FUN_PROCESS;
 	private static final String FUN_PROCESS_URI = Processor.FUN_PROCESS_URI;
        private static final String FUN_NL      = Processor.FUN_NL;
        private static final String FUN_INDENT  = Processor.FUN_INDENT;
        
        private static final String IBOX        = "ibox";
        private static final String SBOX        = "sbox";
        private static final String BOX         = "box";

        // functions whose variable are compiled as (coalesce(st:process(?x), "")
        private static String[] PPRINT_META = {GROUPCONCAT, CONCAT, FUN_TEMPLATE_CONCAT, COALESCE, IF};

	private Constant empty, pack;

	private boolean renameBlankNode = true;

	private String groupSeparator = " ";
        private String templateSeparator = System.getProperty("line.separator");

	private boolean isTemplate = false;
	
	private boolean isAllResult = false;

	private String name;

	private boolean isTurtle;

	private Term templateGroup;
        private Expression templateExpSeparator;

        // @(a b) rewritten as rdf:rest*/rdf:first a, b
        private int listType = L_LIST;
        private String profile;
    private boolean isFunctional;
    
    private final Map<String, List<String>> approximateSearchOptions = new HashMap<String, List<String>>();
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
    }
    
    public Context getContext(){
        if (defaultDataset == null){
            return null;
        }
        return defaultDataset.getContext();
    }

    @Override
    public String toGraph() {
       SPIN sp =  SPIN.create();
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

    public boolean hasFunctional(){
        return isFunctional;
    }

    /**
     * @return the define
     */
    public Extension getDefine() {
        return define;
    }

    /**
     * @param define the define to set
     */
    public void setDefine(Extension define) {
        this.define = define;
    }

    /**
     * @return the undefined
     */
    public HashMap <String, Expression> getUndefined() {
        return undefined;
    }

    /**
     * @param undefined the undefined to set
     */
    public void setUndefined(HashMap <String, Expression> undefined) {
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

	class ExprTable extends Hashtable<Expression,Expression> {};

	/**
	 * The constructor of the class It looks like the one for QueryGraph
	 */
	private ASTQuery() {
            dataset = Dataset.create();
            define = new Extension();
            undefined = new HashMap();
        }
	
	ASTQuery(String query) {
		this();
		setText(query);
    }
	
	public static ASTQuery create(String query){
		return new ASTQuery(query);
	}
	
	public static ASTQuery create(){
		return new ASTQuery();
	}
	
	public static ASTQuery create(Exp exp){
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
	 * AST for a subquery
	 * share prefix declaration
	 */
	public ASTQuery subCreate(){
		ASTQuery ast = create();
		ast.setGlobalAST(this);
		ast.setNSM(getNSM());
		return ast;
	}
	
	void setGlobalAST(ASTQuery a){
		globalAST = a;
	}
	
	public ASTQuery getGlobalAST(){
		if (globalAST == null) return this;
		return globalAST;
	}
	
	public List<Constant> getFrom() {
		return dataset.getFrom();
	}

	public List<Constant> getNamed() {
		return dataset.getNamed();
	}
        
        public Dataset getDataset(){
            return dataset;
        }
        
        public void setDataset(Dataset ds){
            dataset = ds;
        }
	

	public void setNamed(Constant uri) {
		dataset.addNamed(uri);
	}


	public void setFrom(Constant uri) {
		dataset.addFrom(uri);
	}

		
	public List<Constant> getActualFrom(){
		if (dataset.hasFrom()) return dataset.getFrom();
                if (dataset.hasWith()){
                    // with <uri> insert {} where {}
                    return dataset.getWith();
                }
		if (defaultDataset != null && defaultDataset.hasFrom()) return defaultDataset.getFrom();
		return dataset.getFrom();
	}
	
	public List<Constant> getActualNamed(){
		if (dataset.hasNamed()) return dataset.getNamed();
		if (defaultDataset != null && defaultDataset.hasNamed()) return defaultDataset.getNamed();
		return dataset.getNamed();
	}
	
	
	public void setInsertData(boolean b){
		isInsertData = b;
	}
	
	public boolean isInsertData(){
		return isInsertData;
	}
	
	public void setDeleteData(boolean b){
		isDeleteData = b;
	}
	
	public boolean isDeleteData(){
		return isDeleteData;
	}
	
	public boolean isValidate(){
		return validate;
	}
	
	public void setValidate(boolean b){
		validate = b;
	}
	
	/**
	 * collect var for select *
	 * check scope for BIND(exp as var) and select exp as var
	 */
	public boolean validate(){
            
            // in some case, validate() may be called twice
            // hence clear the stack
            stack.clear();
            
		collect();
	
		if (getBody()!=null){
			// select ?x
			for (Variable var : getSelectVar()){
				if (hasExpression(var)){
					bind(var);
				}
			}
			// select *
			if (isSelectAll()){
				for (Variable var : getSelectAllVar()){
					if (hasExpression(var)){
						bind(var);
					}
				}
			}
						
			boolean ok = true;
			
			for (Exp exp : getBody().getBody()){
				boolean b = exp.validate(this);
				if (! b){
	    			ok = false;
	    		}
			}
			
			return ok;
		}
		
		return true;
	}
	
	// collect values for select *
	void collect(){
		if (getValues() != null){
			for (Variable var : getValues().getVariables()){
				defSelect(var);
			}
		}
	}
	
	
	void record(Atom blank){
		if (dataBlank == null){
			createDataBlank();
		}
		dataBlank.put(blank.getLabel(), blank);
	}
	
	public void createDataBlank(){
		dataBlank = new HashMap<String, Atom>();
	}
	
	public HashMap<String, Atom> getDataBlank(){
		return dataBlank;
	}
	

 
	/**
	 *
	 * @param info
	 */
	public void addInfo(String info) {
		if (vinfo == null)
			vinfo = new ArrayList<String>(1);
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
        
        void undefined(Expression t){
            if (! getGlobalAST().getDefine().isDefined(t)){
                getGlobalAST().getUndefined().put(t.getLabel(), t);
            }
        }
        
        /**
         * Used by VariableVisitor, called by Transformer
         * def = function(st:foo(?x) = st:bar(?x))
         */
        void define(Expression fun){
            Expression def = fun; //.getArg(0);
            Expression t = def.getFunction(); //Arg(0);
            getGlobalAST().getDefine().define(def);
            getGlobalAST().getUndefined().remove(t.getLabel());
        }       
              
	public List<String> getErrors(){
		return getGlobalAST().errors();
	}
	
	public String getUpdateTitle(){
		if (isAdd()){
			return KeywordPP.INSERT;
		}
		if (isDelete()){
			return KeywordPP.DELETE;
		}
		return KeywordPP.CONSTRUCT;
	}
	
	void setError(String error){
		if (errors == null)
			errors = new ArrayList<String>();
		if (! errors.contains(error)){
			errors.add(error);
			logger.error(error);
		}
	}
		
	List<String> errors(){
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
	
	public void setFake(boolean fake) {
		this.fake = fake;
	}

	public void setRDF(boolean rdf) {
		this.rdf = rdf;
	}
	
	public void setJSON(boolean b) {
		isJSON = b;
	}
	
	public boolean isJSON(){
		return isJSON;
	}
	
	public void setFlat(boolean flat) {
		this.flat = flat;
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

	public void setPQuery(boolean query) {
		pQuery = query;
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
    
    public void setDistance(String dist){
    	distance = dist;
    }
    
    public String getDistance(){
    	return distance;
    }

	public void setRule(boolean rule) {
		this.rule = rule;
	}

	public void setSelectAll(boolean selectAll) {
		// We print relations between concepts if SELECT DISPLAY RDF *
		// SELECT DISPLAY RDF * <=> SELECT DISPLAY RDF
		if (selectAll && isRDF()) this.selectAll = false;
		else this.selectAll = selectAll;
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

//	public boolean isDisplayBNID() {
//		return displayBNID;
//	}

	public boolean isDistinct() {
		return distinct;
	}
        
        
        public boolean isReduced() {
		return false;
	}
	public boolean isFake() {
		return fake;
	}

	public boolean isRDF() {
		return rdf;
	}
	
	public boolean isFlat() {
		return flat;
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

	public void setLoad(boolean b){
		isLoad = b;
	}
	
	public boolean isLoad(){
		return isLoad;
	}

	/**
	 * NS Manager
	 */
	public NSManager getNSM() {
		if (nsm == null){ 
			nsm = NSManager.create(getDefaultNamespaces());
			nsm.setBase(getDefaultBase());
		}
		return nsm;
	}
	
	public void setNSM(NSManager nsm){
		this.nsm = nsm;
	}
	
	public String getDefaultNamespaces() {
		return namespaces;
	}
	
	public void setDefaultNamespaces(String ns){
		namespaces = ns;
	}
	
	public String getDefaultBase() {
		return base;
	}
	
	public void setDefaultBase(String ns){
		base = ns;
	}

	public boolean isPQuery() {
		return pQuery;
	}

	public Exp getQuery() {
		return query;
	}
	
	public Exp getExtBody(){
		if (query != null) return query;
		return getBody();
	}

	public boolean isRule() {
		return rule;
	}
	
	public List<Variable> getSelectVar() {
		return selectVar;
	}
	
	public List<Variable> getSelectAllVar() {
		return selectAllVar;
	}
	
	public boolean isSelectAllVar(Variable var){
		return selectAllVar.contains(var);
	}
	
	public boolean isSelectAllVar(String name){
		for (Variable var : selectAllVar){
			if (var.getLabel().equals(name)){
				return true;
			}
		}
		return false;
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

	/** created for the new parser */
	
	
	public static Term createRegExp(Expression exp) {
		Term term =  Term.function(REGEX, exp);
		return term;
	}
	
	boolean checkBlank(Expression exp){
		if (exp.isBlankNode()){
			setCorrect(false);
			return false;
		}
		return true;
	}
	
	/**
	 * BIND( f(?x) as ?y )
	 */
	public Exp createBind(Expression exp, Variable var){
            if (var.getVariableList() != null){
                // bind (sql() as ())
                return createASTBind(exp, var);
            }
            else {
                return Binding.create(exp, var);
            }
        }
            
            
        public Exp createASTBind(Expression exp, Variable var){		
                ASTQuery ast = subCreate();        
		ast.setBody(BasicGraphPattern.create());
		ast.setSelect(var, exp); 
		ast.setBind(true);
		Query q = Query.create(ast); 
		return q;
	}
	
	Term createTerm(String oper, Expression exp1, Expression exp2){
		checkBlank(exp1);
		checkBlank(exp2);
		Term term =   Term.create(oper, exp1, exp2);
		return term;
	}
	
	public  Term createConditionalAndExpression(String oper, Expression exp1, Expression exp2) {
		return createTerm(oper, exp1, exp2);
	}

	public  Term createConditionalOrExpression(String oper, Expression exp1, Expression exp2) {
		return createTerm(SEOR, exp1, exp2);
	}
	
	public  Term createAltExpression(Expression exp1, Expression exp2) {
		return createTerm(Term.RE_ALT, exp1, exp2);
	}
	
	public  Term createParaExpression(Expression exp1, Expression exp2) {
		return createTerm(Term.RE_PARA, exp1, exp2);
	}
	
	public  Term createSeqExpression(Expression exp1, Expression exp2) {
		return createTerm(Term.RE_SEQ, exp1, exp2);
	}

	public  Term createRelationalExpression(String oper, Expression exp1, Expression exp2) {
		return createTerm(oper, exp1, exp2);

	}

	public  Term createMultiplicativeExpression(String oper, Expression exp1, Expression exp2) {
		return createTerm(oper, exp1, exp2);

	}

	public  Term createAdditiveExpression(String oper, Expression exp1, Expression exp2) {
		return createTerm(oper, exp1, exp2);

	}

    public  Expression createUnaryExpression(String oper, Expression expression) {
    	checkBlank(expression);
        if (oper.equals(SENOT)) {
            expression =  new Term(oper, expression);
        } 
        else if (oper.equals("-")) {
            expression = new Term(oper, 
            		 Constant.create("0", RDFS.qxsdInteger), expression);
        } // else : oper.equals("+") => don't do anything
        return expression;
    }

    public  Term createFunction(String name) {
    	Term term = Term.function(name);
    	// no toNamespaceB()
    	term.setLongName(getNSM().toNamespace(name));
    	return term;
    }
    
    // TBD: clean this
    public  Term createFunction(Constant name) {
    	Term term =  createFunction(name.getName());
    	term.setCName(name);
    	return term;
    }
 
    public  Term createFunction(Constant name, ExpressionList el) {
    	Term term =  createFunction(name.getName(), el);
    	term.setCName(name);
    	return term;
    }
    
    /**
     * function name(el) { exp }
     * ->
     * function (name(el) = exp)
     */
     public  Term defineFunction(Constant name, ExpressionList el, Expression exp) {
    	Term fun  = createFunction(name, el);
        //Term body = createTerm(SEQ, fun, exp);
    	//Term def  = createFunction(Constant.create(Processor.FUNCTION), body);
        Term def = new Function(fun, exp);
        define.defineFunction(def);
        if (name.getLabel().equals(Processor.XT_MAIN)){
            defSelect(new Variable(MAIN_VAR), createFunction(name));
        }
    	return def;
    }
     
    public Expression defineBody(ExpressionList lexp){
         Expression exp;
        if (lexp.size() == 0){
            exp = Constant.create(true);
        }
        else if (lexp.size() == 1){
            exp = lexp.get(0);
        }
        else {
            exp = createFunction(Processor.SEQUENCE, lexp);
        }
        return exp;
     }
     
     public Term ifThenElse(Expression ei, Expression et, Expression ee){
         Term exp = createFunction(Processor.IF, ei);
         exp.add(et);
         if (ee == null){
             ee = Constant.create(true);
         }
         exp.add(ee);
         return exp;
     }
     
     public Term set(Variable var, Expression exp){
         return Term.function(Processor.SET, var, exp);
     }
     /**
      * let (var = exp, body)
      * @param el
      * @param body
      * @return 
      */
     public Term let(ExpressionList el, Expression body){
         return defineLet(el, body, 0);
     }
     
     public Term defineLet(ExpressionList el, Expression body, int n){
         if (n == el.size() -1){
             return new Let(el.get(n), body);
         }
         return new Let(el.get(n), defineLet(el, body, n+1));
     }
           
      public Term defLet(Variable var, Expression exp){
          return Term.create("=", var, exp);
      }
      
       public Term defLet(ExpressionList lvar, Expression exp){
          Term t = createFunction(Processor.MATCH, lvar);
          return Term.create("=", t, exp);
      }
       
       public Term loop(Variable var, Expression exp, Expression body){
           return new ForLoop(var, exp, body);
       }
     
     public void exportFunction(Expression def){
         def.getArg(0).setExport(true);
         def.setExport(true);
     }
       
    public  Term createFunction(Constant name, Expression exp) {
    	Term term =  createFunction(name.getName(), exp);
    	term.setCName(name);
    	return term;
    }
    
    public  Term createFunction(Constant name, Expression exp, Expression e2) {
    	Term term =  createFunction(name, exp);
        term.add(e2);    	
    	return term;
    }
    
    public  Term createFunction(String name, ExpressionList el) {
    	Term term = createFunction(name);
    	term.setModality(el);
    	for (Expression exp : el){
    		term.add(exp);
    	}
    	return term;
    }
    
    public Triple createTriple(Expression exp){
    	checkBlank(exp);
    	return Triple.create(exp);
    }
    
    public  Term createList(ExpressionList el) {
    	Term list = Term.list();
    	for (Expression exp : el){
    		list.add(exp);
    	}
    	return list;
    }

    public Term negation(Expression e){
    	return Term.negation(e);
    }
    

    public RDFList createRDFList(List<Expression> list){
    	return createRDFList(list, 0);
    }
    
    public void setListType(int n){
        listType = n;
    }
    
    public int getListType(){
        return listType;
    }
    
    /**
     * Create an RDF List (rdf:first/rdf:rest)
     * if close = true, end by rdf:nil (usual case) 
     * Return an RDFList which is an And on the triples
     * Can get starting first blank node with function head()
     * i.e. the subject of first triple
     */
    public RDFList createRDFList(List<Expression> list, int arobase) {
        RDFList rlist = new RDFList(newBlankNode(), list);  
        if (arobase == L_DEFAULT){
            arobase = listType;
        }
        switch (arobase){
            
            case L_LIST:       
                rlist = complete(rlist);
                break;
        
            case L_PATH:
                rlist = path(rlist);
                break;
        }
        return rlist;
    }

    RDFList complete(RDFList rlist){
        Expression 
                rest = null,
                blank = null;
        boolean isFirst = true;
        Exp triple;

        for (Expression exp : rlist.getList()) {
            
            if (isFirst) {
                blank = rlist.head();
                isFirst = false;
            }
            else {
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

        for (Expression ee : exp.getList()){
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
    
       

	public  Term createFunction(String name, Expression expression1) {
        Term term = createFunction(name);
        term.add(expression1);
		return term;
	}

	static Term createTerm(String s) {
		Term term = new Term(s);
		return term;
	}

	public Term createGet(Expression exp, int n){
		return Term.function("get", exp, Constant.create(n));
	}
	
	

	public static Variable createVariable(String s){
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
	public  Constant createConstant(String s) {
		Constant cst = Constant.create(s);
		cst.setLongName(getNSM().toNamespaceB(s));
		return cst;
	}
	
	// ex:name
	public  Constant createQName(String s) {
		Constant cst = Constant.create(s);
		String lname = getNSM().toNamespaceB(s);
		cst.setLongName(lname);
		if (s.equals(lname)){
			addError("Undefined prefix: ", s);
		}
		cst.setQName(true);
		return cst;
	}
	
	// <uri>
	public  Constant createURI(String s) {
		Constant cst = Constant.create(s);
		// base
		cst.setLongName(getNSM().toNamespaceB(s));
		return cst;
	}
	
	/*
	 * Draft property regexp
	 */
	public  Constant createProperty(Expression exp) {
		if (exp.isConstant()){
			// no regexp, std property
			return exp.getConstant();
		}		
		return createExpProperty(exp);
	}
        
        Constant createExpProperty(Expression e){
            Constant cst =  createConstant(RootPropertyQN);
            cst.setExpression(e);
            return cst;
        }
	

	public  Triple createTriple(Atom predicate, List<Atom> list){
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
	public  Triple createTriple(Expression subject, Atom predicate, Expression object){
		Expression exp = predicate.getExpression();
                Variable var   = predicate.getIntVariable();                               
		Triple t;
		if (exp == null){
			t = Triple.create(subject, predicate, object);
		}
		else {
			t = createPath(subject, predicate, object, exp);
		}
		return t;
	}
	
	
	public  Triple createPath(Expression subject, Expression exp, Expression object){		
		Constant predicate = createProperty(exp);
		predicate.setExpression(exp);
		Triple t = createPath(subject, predicate, object, exp);
		return t;
	}
	
		
	/**
	 * Create a Triple that contains a Property Path with exp as PP expression
	 */
	public  Triple createPath(Expression subject, Atom predicate, Expression object, Expression exp){		
		Triple t = Triple.create(subject, predicate, object);
		// property path or xpath
		Variable var = t.getVariable();
		if (var == null){
			var = new Variable(SYSVAR + nbd++);
			var.setBlankNode(true);
			t.setVariable(var);
		}
		if (exp.getName().equals(Term.XPATH)){
			t.setRegex(exp);
			return t;
		}

		var.setPath(true);
		String mode = "";
		boolean isDistinct = false, 
				isShort = false;

		while (true){
			if (exp.isFunction()){

				if (exp.getName().equals(DISTINCT)){
					exp = exp.getArg(0);
					//mode += DISTINCT;
					isDistinct = true;
				}
				else if (exp.getName().equals(SSHORT)){
					exp = exp.getArg(0);
					mode += "s";
					isShort = true;
				}
				else if (exp.getName().equals(SSHORTALL) || exp.getName().equals(SHORT)){
					exp = exp.getArg(0);
					mode += "sa";
					isShort = true;
				}

				else break;
			}
			else break;
		}


		exp.setDistinct(isDistinct);
		exp.setShort(isShort);
		t.setRegex(exp);
		t.setMode(mode);

		return t;

	}

	
	
	// regex only
	public  Expression createOperator(String ope, Expression exp) {
		Term fun = null;
		if (ope.equals(SINV) || ope.equals(SBE)) {
			fun = Term.function(ope, exp);
		} 
		else if (ope.equals(SMULT)){
			fun = star(exp);
		}
		else if (ope.equals(SPLUS)){
			if (isKgram()){
				// first exp is member of visited (SPARQL 1.1)
				// for checking loop
				fun = createOperator(1, Integer.MAX_VALUE, exp);
				fun.setPlus(true);
			}
			else {
				fun = sequence(exp, Term.function(Term.STAR, exp));
			}
		}
		else if (ope.equals(Keyword.SQ)){
			fun = Term.function(Term.OPT, exp);
		}
		else {
			fun = Term.function(ope, exp);
		}
		return fun;
	}
	
	public Expression createOperator(String ope, Expression exp1, Expression exp2) {
		if (ope.equals(SOR)){
			ope = SEOR;
		}
		return createTerm(ope, exp1, exp2);
	}
	
        /**
         * exp is a subquery
         * nest it in Term exists { exp }
         * use case: for (?m in select where){}
         */
       public  Term toExist(Exp exp) {
           Term t = createExist(exp, false);
           // return all Mapping of subquery:
           t.setSystem(true);
           return t;
       }

    public  Term createExist(Exp exp, boolean negation) {
    	Term term = Term.function(Term.EXIST);
    	term.setExist(Exist.create(exp));
    	if (negation){
    		term = negation(term);
    	}
    	return term;
    }
    
    /**
     *   foaf:knows @[a foaf:Person]
     *   foaf:knows @{?this a foaf:Person}
     *   foaf:knows @{filter(?this != ex:John)}
     */
    public Expression createRegexTest(Expression prop, Exp test) {
    	Expression exp;
    	if (test.size() == 1 && test.get(0).isFilter()){
    		exp = test.get(0).getFilter();
    	}
    	else {
    		exp = createExist(test, false); 
    	}
    	return setRegexTest(prop, exp);
    }
    
	
	/**
	 * Filter test associated to path regex exp
	 */
	public Expression setRegexTest(Expression exp, Expression test){
		regexExpr.put(exp, test);
		Expression tt = Term.function(Term.TEST);
		tt.setExpr(test);
		Expression seq = sequence(exp, tt);
		return seq;
	}
	
	public Collection<Expression> getRegexTest(){
		return regexExpr.values();
	}
	
	Term star(Expression exp){
		return Term.function(Term.STAR, exp);
	}
	
	Term sequence(Expression e1, Expression e2){
		return Term.create(Term.RE_SEQ, e1, e2);
	}
	
	Expression alter(Expression e1, Expression e2){
		return Term.create(Term.RE_ALT, e1, e2);

	}
	
	public  Expression createOperator(String s1, String s2, Expression exp) {
		int n1 = 0, n2 = Integer.MAX_VALUE;
		if (s1 != null) n1 = Integer.parseInt(s1);
		if (s2 != null) n2 = Integer.parseInt(s2);
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
		if (datatype == null){ 
			datatype = datatype(lang);
		}
		else if (! knownDatatype(datatype)){
			datatype = getNSM().toNamespaceB(datatype);
		}
		//s = clean(s);
		return  Constant.create(s, datatype, lang);
	}
	
	String datatype(String lang){
		return DatatypeMap.datatype(lang);
	}
	
	private  boolean knownDatatype(String datatype) {
		if (datatype.startsWith(RDFS.XSD) ||
				datatype.startsWith(RDFS.XSDPrefix) ||
				datatype.startsWith(RDFS.RDF) ||
				datatype.startsWith(RDFS.RDFPrefix) )
		return true;
		else return false;
	}

	
    /** used for collections */
    public  Exp generateFirst(Expression expression1, Expression expression2) {
        Atom atom = createQName(RDFS.qrdfFirst);
        Triple triple = Triple.create(expression1, atom, expression2);
        return triple; 
    }

    public  Exp generateRest(Expression expression1, Expression expression2) {
        Atom atom = createQName(RDFS.qrdfRest);
        Triple triple = Triple.create(expression1, atom, expression2);
        return triple; 
    }

	static boolean isString(String value) {
		return value.startsWith("\"") || value.startsWith("'");
	}

	

	public void setList(boolean b){
		this.setJoin(!b);
	}
	
	public void setCorrect(boolean b){
		isCorrect = b;
	}
	
	public boolean isCorrect(){
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
	
	public boolean isAdd(){
		return isAdd;
	}
	
	public boolean isInsert(){
		return isAdd;
	}
	
	public void setAdd(boolean b){
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
    	if (isRenameBlankNode() ){
    		return newBlankNode();
    	}
    	else {
    		return newBlankNode(label);
    	}
    }


    public Variable newBlankNode() {
		return newBlankNode( BNVAR + getNbBNode());
	}
    
    public Variable metaVariable(){
        return newBlankNode();
    }
    
    public Variable newBlankNode(String label) {
		Variable var = createVariable( label);
		var.setBlankNode(true);
		return var;
	}
    
    /**
     * Reset tables when start a new query (update) 
     */
    public void reset(){
    	if (blank != null){
    		blank.clear();
    	}
    	if (blankNode != null){
    		blankNode.clear();
    	}
    }
    
    /**
     * Same blank label must not be used in different BGP exp
     * except in insert data {}
     */
    public Variable newBlankNode(Exp exp, String label) {
    	if (blank == null){
    		blank = new Hashtable<String, Exp>();
    		blankNode = new Hashtable<String, Variable>();
    	}
    	
    	if (! isInsertData()){
    		Exp ee = blank.get(label);

    		if (ee == null){
    			blank.put(label, exp);
    		}
    		else if (ee != exp){
    			setCorrect(false);
    			logger.error("Blank Node used in different patterns: " + label);
    			addError("Blank Node used in different patterns: ", label);
    		}
    	}

    	Variable var = blankNode.get(label);
    	if (var == null){
    		// create a new blank node and put it in the table
			//var = newBlankNode();
			var = getBlankNode(label);
			blankNode.put(label, var);
    	}
    	return var;
    }

    /**
     * use case:
     * select sql() as (?x, ?y)
     * @param var1
     * @param var2
     */
    public void addVariable(Variable var1, Variable var2){
    	var1.addVariable(var2);
    }
	
	public Array newArray(ExpressionList list){
		Array array =  new Array(list);
		return array;
	}
    
    public void setDescribe(Atom at){
    	setResultForm(QT_DESCRIBE); 
    	for (Atom aa : adescribe){
    		if (aa.getLabel().equals(at.getLabel())){
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
    
    public boolean isEdgeForType(){
    	return isRelationForType;
    }
    
    public void setEdgeForType(boolean b){
    	isRelationForType = b;
    }
    
    public boolean isKgram(){
    	return isKgram;
    }
    
    public void setKgram(boolean b){
    	isKgram = b;
    }
    
    public boolean isSPARQLCompliant(){
    	return isSPARQLCompliant;
    }
    
    public void setSPARQLCompliant(boolean b){
    	isSPARQLCompliant = b;
    }
    
    public boolean isBind(){
    	return isBind;
    }
    
    public void setBind(boolean b){
    	isBind = b;
    }
    
    public boolean isSPARQL1(){
    	return isSPARQL1;
    }
    
    public void setSPARQL1(boolean b){
    	isSPARQL1 = b;
    }   

    public int getVariableId() {
        return nbd++;
    }

    
    /***************************************************************
     * 
     * Compile AST
     * 
     ***************************************************************/
    
    
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
    	if (getConstruct() != null){
    		// kgram:
    		setInsert(getConstruct());
    		Exp exp = getConstruct();
    		//Env env = new Env(false);
    		// assign graph ?src variable to inner triples
    		// TODO: for backward rules only
    		//exp.setSource(env, null, false);
    		setConstruct(exp);
    	}
    	else if (getInsert() !=null){
    		// kgram update
    		setConstruct(getInsert());
    	}
    }

    /**
     * compile describe ?x 
     * as:
     * 
     * construct { 
     *   {?x ?p ?y} union {?y ?p ?x} 
     * }
     * where {
     *   {?x ?p ?y} union {?y ?p ?x}
     * }
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
            
            if (atom.isVariable()){
             // TODO: compile only if variable is in the where clause
               Variable var = atom.getVariable();
                if (! getSelectAllVar().contains(var)){
                    continue;
                }
            }

            //// create variables
            int nbd = getVariableId();
            Variable prop1 = createVariable(PP + nbd);
            Variable val1  = createVariable(VV + nbd);

            nbd = getVariableId();
            Variable prop2 = createVariable(PP + nbd);
            Variable val2  = createVariable(VV + nbd);

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

        if (isKgram()) {
            setInsert(template);
            setConstruct(template);
        }
    }
    
    private void compileAsk() {
    	setMaxResult(1);
    }
    
    /**
     *  @deprecated
     */
    public Expression bind(){
 	   if (valueBindings == null) return null;
 	   Expression exp = null;
 	   
 	   for (List<Constant> lVal :  valueBindings){
 		   if (varBindings.size()!=lVal.size()){
 			   setCorrect(false);
 		   }
 		   else if (exp == null){
 			   exp = bind(varBindings, lVal);
 		   }
 	   }
 	   return exp;
    }
    
    /**
     *  @deprecated
     */     
    public Expression bind(List<Variable> lVar, List<Constant> lVal){
		Expression exp = null;
		int i = 0;
		for (Variable var : lVar){
			if (i>=lVal.size()){
				
			}
			else if (lVal.get(i)==null){
				i++;
			}
			else {
				Term term = Term.create(Term.SEQ, var, lVal.get(i++));
				if (exp == null){
					exp = term;
				}
				else {
					exp = exp.and(term);
				}
			}
		}
		return exp;
	}
   
 
    
    
    /************************************************************
     * 
     * Pretty Printer
     * 
     ************************************************************/

    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	toString(sb);
    	return sb.toString();
    }
    
    public StringBuffer toString(StringBuffer sb) {
    	if (isUpdate()){
    		getUpdate().toString(sb);
    	}
    	else {
        	getSparqlPrefix(sb);
    		getSparqlHeader(sb);
    		if ( ! isData() && (! isDescribe() || getBody()!=null)){
    			getBody().toString(sb);
    		}

    		if (! isAsk()) {
    			sb.append(NL);
    			getSparqlSolutionModifier(sb);
    		}
    	}
    	
        getFinal(sb);
    	
    	return sb;
    }

    StringBuffer getSparqlPrefix(StringBuffer sb) {
    	return getSparqlPrefix(getPrefixExp(), sb);
    }
    
    public StringBuffer getSparqlPrefix(Exp exp, StringBuffer sb) {
        
        for (Exp e : exp.getBody()) {
            Triple t = e.getTriple();
            String r = t.getSubject().getName();
            String p = t.getPredicate().getName();
            String v = t.getObject().getName();
            
            // if v starts with "<function://", we have add a ".", so we have to remove it now
            if (v.startsWith(KeywordPP.CORESE_PREFIX) &&
            	v.endsWith(".")) {
            	v = v.substring(0, v.length()-1) ;
            }

            if (r.equalsIgnoreCase(KeywordPP.PREFIX)) {
                sb.append(KeywordPP.PREFIX + KeywordPP.SPACE + p + ": " + 
                KeywordPP.OPEN + v + KeywordPP.CLOSE + NL);
            } else if (r.equalsIgnoreCase(KeywordPP.BASE)) {
                sb.append(KeywordPP.BASE + KeywordPP.SPACE + 
                KeywordPP.OPEN +  v + KeywordPP.CLOSE +  NL);
            }
        }
        return sb;
    }

    /**
     * Return the header part of the SPARQL-like Query (2nd parser)
     * @return
     */
    StringBuffer getSparqlHeader(StringBuffer sb) {
    	String SPACE = KeywordPP.SPACE;
    	List<Constant> from = getFrom();
    	List<Constant> named = getNamed();
    	List<Variable> select = getSelectVar();

    	// Select
    	if (isSelect()) {
    		sb.append(KeywordPP.SELECT + SPACE);
    		
    		if (isDebug())
    			sb.append(KeywordPP.DEBUG + SPACE);
    		
    		if (isMore())
    			sb.append(KeywordPP.MORE + SPACE);
    		
    		if (isDistinct())
    			sb.append(KeywordPP.DISTINCT + SPACE);

    		if (isSelectAll()) {
    			sb.append(KeywordPP.STAR + SPACE);
    		}
    		
    		if (select != null && select.size()>0){
    			for (Variable s : getSelectVar()){
                            
    				if (getExpression(s) != null) {
    					expr(getExpression(s), s, sb);
    				}
    				else {
    					sb.append(s);
    				}
                                sb.append(SPACE);
    			}
    		} 
  		
    	} 
    	else if (isAsk()) {
    		sb.append(KeywordPP.ASK + SPACE);
    	} 
    	else if (isDelete()) {
    		sb.append(KeywordPP.DELETE + SPACE); 
    		if (isDeleteData()){
    			sb.append(KeywordPP.DATA + SPACE); 
			}
    		getDelete().toString(sb);
    		
    		if (isInsert()){
    			sb.append(KeywordPP.INSERT + SPACE); 
        		getInsert().toString(sb); 
    		}
    		
    	}
    	else if (isConstruct()) {
    		if (isInsert()){
    			sb.append(KeywordPP.INSERT + SPACE); 
    			if (isInsertData()){
        			sb.append(KeywordPP.DATA + SPACE); 
    			}
        		getInsert().toString(sb); 
    		}
    		else if (getConstruct() != null){
        		sb.append(KeywordPP.CONSTRUCT + SPACE); 
        		getConstruct().toString(sb); 
    		}
    		else if (getInsert() != null){
        		sb.append(KeywordPP.INSERT + SPACE); 
        		getInsert().toString(sb); 
    		}
    		else if (getDelete() != null){
        		sb.append(KeywordPP.DELETE + SPACE); 
        		if (isDeleteData()){
        			sb.append(KeywordPP.DATA + SPACE); 
    			}
        		getDelete().toString(sb); 
    		}
    	} 
    	else if (isDescribe()) {
    		sb.append(KeywordPP.DESCRIBE + SPACE);
    		
    		if (isDescribeAll()) {
    			sb.append(KeywordPP.STAR + SPACE);
    		} 
    		else if (adescribe != null && adescribe.size()>0) {

    			for (Atom at : adescribe) {
    				at.toString(sb);
    				sb.append(SPACE);
    			}
    		}
    	} 
    	
    	// DataSet
    	//if (! isConstruct())    // because it's already done in the construct case
    		sb.append(NL);
    	
    	// From
    	for (Atom name: from) {
    		sb.append(KeywordPP.FROM + SPACE);
    		name.toString(sb);
    		sb.append(NL);
    	}
    	
    	// From Named
    	for (Atom name : named) {
       		sb.append(KeywordPP.FROM + SPACE + KeywordPP.NAMED + SPACE);
    		name.toString(sb);
    		sb.append(NL);
    	}
    	
    	// Where
    	if ((! (isDescribe() && ! isWhere())) && ! isData() ){
    		sb.append(KeywordPP.WHERE + NL) ; 
    	}

    	return sb;
    }

    
    void expr(Expression exp, Variable var, StringBuffer sb) {
        sb.append("(");
        exp.toString(sb);
        sb.append(" as ");
        
        if (var.getVariableList() != null) {
            sb.append("(");
            int i = 0;
            for (Variable v : var.getVariableList()){
                if (i++ > 0){
                    sb.append(", ");                    
                }
                sb.append(v);
            }
            sb.append(")");
        } 
        else {
            sb.append(var);
        }
        sb.append(")");
    }


    private boolean isData() {
		return isInsertData() || isDeleteData();
	}

	/**
     * return the solution modifiers : order by, limit, offset
     * @param parser
     * @return
     */
    public StringBuffer getSparqlSolutionModifier(StringBuffer sb) {
        String SPACE = KeywordPP.SPACE;
        List<Expression> sort = getSort();
        List<Boolean> reverse = getReverse();
        
        if (getGroupBy().size()>0){
        	sb.append(KeywordPP.GROUPBY + SPACE);
        	for (Expression exp : getGroupBy()){
        		sb.append(exp.toString() + SPACE);
        	}
        	sb.append(NL);
        }
        
        if (sort.size() > 0 ) {
        	int i = 0;
        	sb.append(KeywordPP.ORDERBY + SPACE);
        	
        	for (Expression exp : getOrderBy()) {
        		
        		boolean breverse = reverse.get(i++);
        		if (breverse) {
        			sb.append(KeywordPP.DESC + "(");
        		}
        		sb.append(exp.toString());
        		if (breverse) 
        			sb.append(")") ;
        		sb.append(SPACE);
        	}
        	sb.append(NL);
        }
        
        if (getOffset() > 0)
        	sb.append(KeywordPP.OFFSET + SPACE + getOffset() + SPACE);
        
        if (getMaxResult() != getDefaultMaxResult())
        	sb.append(KeywordPP.LIMIT + SPACE + getMaxResult() + KeywordPP.SPACE);
        
        if (getHaving()!=null){
        	sb.append(KeywordPP.HAVING);
        	sb.append(KeywordPP.OPEN_PAREN);
        	getHaving().toString(sb);
        	sb.append(KeywordPP.CLOSE_PAREN);
        }
         
        if (sb.length()>0)
        	sb.append(NL);
        
        return sb;
    }
    
    
    void getFinal(StringBuffer sb){
    	String SPACE = " ";

    	if (getValues()!=null){
    		getValues().toString(sb);
    	}

    	if (getPragma()!=null){
    		sb.append(KeywordPP.PRAGMA);
    		sb.append(SPACE);
    		getPragma().toString(sb);
    	}
        
        for (Expression fun : define.getFunList()){
            fun.toString(sb);
            sb.append(NL);
        }
    }

    
    

	public void setConstruct(Exp constructExp) {
		this.setResultForm(QT_CONSTRUCT);
        this.constructExp = constructExp;
    }
	
	public void duplicateConstruct(Exp exp){
		boolean check = checkConstruct(exp);
		if (check){
			setConstruct(exp);
		}
		else {
			setConstruct(null);
		}
	}
	
	/**
	 * construct where {exp}
	 * construct = duplicate(exp)
	 * and exp should have no filter and no graph pattern
	 */
	boolean checkConstruct(Exp body){
		for (Exp exp : body.getBody()){
			if (! exp.isTriple() || exp.isExp()) return false;
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

    public static int getTripleId(){
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
    
    public void setPragma(String name, Exp exp){
    	if (pragma == null){
    		pragma = new Hashtable<String, Exp>();
    	}
    	if (name == null) name = RDFS.COSPRAGMA;
    	else name = getNSM().toNamespace(name);
    	if (exp == null) pragma.remove(name);
    	else pragma.put(name, exp);
    }
    
    
    public void setPragma(Exp exp){
    	 setPragma(RDFS.COSPRAGMA, exp);
    }
    
    public Exp getPragma(String name){
    	if (pragma == null) return null;
    	return pragma.get(name);
    }
    
    public Exp getPragma(){
    	return getPragma(RDFS.COSPRAGMA);
    }
    
    public boolean hasPragma(String subject, String property, String object){
    	if (getPragma() == null) return false;
    	for (Exp exp : getPragma().getBody()){
			if (exp.isRelation()){
				Triple t = (Triple) exp;
				if (t.getSubject().getName().equals(subject) && 
					t.getProperty().getName().equals(property) &&
					t.getObject().getName().equals(object)){
					return true;
				}
			}
		}
    	return false;
    }
    
    public void addPragma(Triple t){
    	Exp pragma = getPragma();
    	if (pragma == null){
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
     * @param t
     */
    public void addPrefixExp(Triple t) {
        prefixExp.add(t);
    }
    
    public void setPrefixExp(Exp exp) {
        prefixExp = exp;
    }
    
   
    public void definePrefix(String prefix, String ns){
    	defNamespace(prefix, ns);
    }

    public void defNamespace(String prefix, String ns){
    	defNSNamespace(prefix, ns);
    	defPPNamespace(prefix, ns);
    }
    
    public void defNSNamespace(String prefix, String ns){
    	if (prefix.endsWith(":")){
    		prefix = prefix.substring(0, prefix.length() - 1); // remove :
    	}
    	getNSM().defNamespace(ns, prefix);
    }
    
    public void defPPNamespace(String prefix, String ns){
    	if (prefix.endsWith(":")){
    		prefix = prefix.substring(0, prefix.length() - 1); // remove :
    	}
    	Triple triple = Triple.createNS(
   			 Constant.create(KeywordPP.PREFIX),  Constant.create(prefix), 
   			 Constant.create(ns));
    	addPrefixExp(triple);
    }
    
    public void defBase(String ns){
    	defNSBase(ns);
    	defPPBase(ns);
    }
    
    public void defPPBase(String ns){
       	Triple triple = Triple.createNS(
    			 Constant.create(KeywordPP.BASE),  Constant.create(""), 
    			 Constant.create(ns));
    	addPrefixExp(triple);
    }
    
    public void defNSBase(String ns){
    	getNSM().setBase(ns);
    }
    
    public String defURI(String s){
    	return s;
    }

 
    public void setCount(String var) {
    }

	public void setSort(String var, boolean breverse) {
    }

    public void setSort(Expression sortExpression) {
    	setSort(sortExpression, false);
    }
    
    public void setHaving(Exp exp){
    	if (exp.getBody().size() == 0) return;
    	Exp body = exp.getBody().get(0);
    	if (! body.isTriple()) return;
    	setHaving(body.getTriple().getExp());
    }
    
    public void setHaving(Expression exp){
    	having = exp;
    }
    
    
    public Expression getHaving(){
    	return having;
    }
    
    public void setSort(Expression sortExpression, boolean breverse) {     
    	sort.add(sortExpression);
    	reverseTable.add(new Boolean(breverse));
    	//sortDistanceTable.add(new Boolean(false));   
    }

    
	public List<Expression> getGroupBy() {
		return lGroup;
	}
	
	public boolean isGroupBy(String name){
		for (Expression exp : lGroup){
			if (exp.isVariable() && exp.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public void setGroup(Expression exp) {
    	if (exp.isVariable()){
   	 		setGroup(exp.getName());
    	}
    	lGroup.add(exp);
   }
	
	
	public void setGroup(Expression exp, Variable var) {
		if (var != null){
			// use case: group by (exp as var)
			// generate:
			// select (exp as var)
			// group by var
			setSelect(var);
			setSelect(var, exp);
			setGroup(var);
		}
		else {
			setGroup(exp);
		}
	}

    
    public void setGroup(String var) {
    }
    
//    public void setVariableBindings(List<Variable> list){
//    	varBindings = list;
//    }
    
    public List<Variable> getVariableBindings(){
    	if (values != null){
    		return values.getVariables();
    	}
    	return null;
    }
    
//    public void setValueBindings(List<Constant> list){
//    	if (valueBindings == null){
//    		valueBindings = new ArrayList<List<Constant>>();
//    	}
//    	valueBindings.add(list);
//   }
    
    public void clearBindings(){
    	values = null;
    }
    
    public List<List<Constant>> getValueBindings(){
    	if (values != null){
    		return values.getValues();
    	}
    	return null;
    }
    
    public void setValues(Values v){
    	values = v;
    }
    
    public Values getValues(){
    	return values;
    }

   
   public void defSelect(Variable var, Expression exp){
	   checkSelect(var);
	   if (exp == null){
		   setSelect(var);
	   }
	   else {
		   setSelect(var, exp);
	   }
   }
      
    public void setSelect(Variable var) {
    	if (! selectVar.contains(var)){
            selectVar.add(var);
     	}    
    }
    
    /**
     * Use case: collect select *
     */
    void defSelect(Variable var){
    	//if (isSelectAll()){
    		addSelect(var);
    	//}
    }
    
    void addSelect(Variable var){
    	if (! selectAllVar.contains(var)){
            selectAllVar.add(var);
     	}
    }
       
    public boolean checkSelect(Variable var){
    	if (selectVar.contains(var)){
    		setCorrect(false);
    		return false;
    	}
    	return true;
    }
    
        
    public void setSelect(Variable var, Expression e) {
    	setSelect(var);
    	if (getExpression(var) != null){
    		addError("Duplicate select : " + e + " as " + var);
    	}
    	selectFunctions.put(var.getName(), e);
    	selectExp.put(e, e);
    	
    	if (var.getVariableList()!=null){
    		// use case:
    		// select sql() as (nn_0, nn_1)
    		// compiled as :
    		// select sql() as var   get(var, i) as nn_i
    		// now generate get() for sub variables
    		int n = 0;
    		for (Variable vv : var.getVariableList()){
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
    	if (describe) setResultForm(QT_DESCRIBE);
    }
    
    public void setAsk(boolean b) {
    	if (b){
    		setResultForm(QT_ASK);
    	}
    }
    
    public void setSelect(boolean b) {
    	if (b){
    		setResultForm(QT_SELECT);
    	}
    }
    
    public void setTemplate(boolean b){
    	isTemplate  = b;
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
		if	(b){
			setResultForm(QT_DELETE);
			isDelete = b;
		}
	}
	
	public void setInsert(boolean b) {
		if	(b){	
			setResultForm(ASTQuery.QT_CONSTRUCT);
			setAdd(true);
		}
	}

    public boolean isDelete() {
    	return isDelete;
    }
    
    public boolean isSPARQLQuery() {
    	return isSelect() || isAsk() || isDescribe() || (isConstruct() && ! isInsert());
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

	
	/**
	 * Remove leading and trailing " or ' of a string
	 */
//	public static String clean(String str) {
//		String res = str;
//		if (str.length() <= 1) return str;
//		
//		if ((str.startsWith(SQ3)  && str.endsWith(SQ3)) ||
//			(str.startsWith(SSQ3) && str.endsWith(SSQ3))){
//			res = str.substring(3,(str.length()-3));
//		}
//		else
//		if ((str.startsWith(SQ1) && str.endsWith(SQ1)) ||
//			(str.startsWith(SSQ) && str.endsWith(SSQ))){
//			res = str.substring(1,(str.length()-1));
//		}
//		return res;
//	}
	
	public boolean isDefineExp(Expression exp){
		return selectExp.get(exp) != null;
	}
	
	public Expression getExpression(String name){
		return selectFunctions.get(name);
	}
	
	public Expression getExpression(Variable var){
		return selectFunctions.get(var.getName());
	}
	
	boolean hasExpression(Variable var){
		return getExpression(var) != null;
	}
	
	public Expression getExtExpression(String name){
		Expression sexp = getExpression(name);
		if (sexp == null) return null;
		// rewrite var as exp
		return sexp.process(this);
	}

	public Hashtable<String, Expression> getSelectFunctions() {
		return selectFunctions;
	}

	public void setSelectFunctions(Hashtable<String, Expression> selectFunctions) {
		this.selectFunctions = selectFunctions;
	}
	
	
	
	
	public void set(ASTUpdate u){
        setResultForm(ASTQuery.QT_UPDATE);
		astu = u;
		u.set(this);
	}
	
	public ASTUpdate getUpdate(){
		return astu;
	}
	
	
	
	
    @Deprecated
    public void setConst(Exp exp) {
        setInsert(exp);
    }

    @Deprecated
   public Exp getConst() {
        return getInsert();
    }
	
    
    void bind(Variable var){
    	if (! stack.contains(var)){
    		stack.add(var);
    	}
    }

    boolean isBound(Variable var){
    	return stack.contains(var);
    }

    public List<Variable> getStack() {
    	return stack;
    }
    
    void newStack(){
    	stack = new ArrayList<Variable>();
    }
    
    void setStack(List<Variable> list){
    	stack = list;
    }

    void addStack(List<Variable> list){
    	for (Variable var : list){
    		bind(var);
    	}
    }

    public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
    
	
	/***************************************************
	 * 
	 *                     Template
	 * template { ?x ... } where {}
	 * ->
	 * select (st:process(?x) as ?px) ... (concat(?px ...) as ?out) where {}
	 * 
	 **************************************************/

    /**
     * template { group { exp } }
     * ->
     * group_concat(concat( exp ))
     */
    public Term createGroup(ExpressionList el) {
        Term term = Term.function(CONCAT);
        for (Expression exp : el){
    		term.add(exp);
    	} 
        term = Term.function(GROUPCONCAT, term);
    	if (el.getSeparator() == null && el.getExpSeparator() == null){
    		el.setSeparator(groupSeparator);
    	}
        term.setModality(el);
    	return term;
    }
    
    
    /**
     * box:  nl(+1) body nl(-1)
     * sbox: nl(+1) body indent(-1)
     * ibox: indent(+1) body indent(-1)
     */
    public Term createBox(ExpressionList el, String type) {
        String open  = FUN_NL;
        String close = FUN_NL;
        
        if (! type.equals(BOX)){
            close = FUN_INDENT;
        }
        if (type.equals(IBOX)){
            open = FUN_INDENT;
        }
        
        Constant fopen  = createQName(open);       
        Constant fclose = createQName(close);       
               
        
        Term t1 = createFunction(fopen, Constant.create(1));
        Term t2 = createFunction(fclose, Constant.create(-1));
        el.add(0, t1);
        el.add(t2);
        return createFunction(createQName(FUN_TEMPLATE_CONCAT), el);
    }
    
    public Term createXML(Constant cst, ArrayList<ExpressionList> lattr, ExpressionList el){
        Term nl  = createFunction(createQName(FUN_NL));       
        ExpressionList arg = new  ExpressionList();    
        if (lattr == null){
           arg.add(Constant.create("<" + cst.getName() + ">")); 
        }
        else {
            arg.add(0, Constant.create("<" + cst.getName() + " "));
            Constant eq = Constant.create("=");
            Constant quote = Constant.create("'");
            for (ExpressionList att : lattr){
                arg.add(att.get(0));
                arg.add(eq);
                arg.add(quote);
                arg.add(att.get(1));
                arg.add(quote);
            }
            arg.add(Constant.create(">"));
        }
        arg.add(nl);
        for (Expression ee : el){
            arg.add(ee);
        }
        arg.add(nl);
        arg.add(Constant.create("</" + cst.getName() + ">"));
        return createFunction(createQName(FUN_TEMPLATE_CONCAT), arg);
    }
    
   /**
    * vbox()
    * if (type.equals(VBOX) && el.size() > 1){
            // add NL between elements
            Term t = createFunction(nl);
            for (int i=1; i<el.size(); ){
                el.add(i, t);
                i += 2;
            }
        }
    * @param s 
    */
       
    
    public void setGroupSeparator(String s){
    	groupSeparator = s;
    }

	
	public void addTemplate(Expression at){
		if (template == null){
			template = new ArrayList<Expression> ();
		}
		template.add(at); 
	}
	
	
	/**
	 * template { "construct {" ?x "} where {" ?y "}" }
	 * ->
	 * select 
	 * (st:process(?x) as ?px)
	 * (st:process(?y) as ?py)
	 * (concat(.. ?px .. ?py ..) as ?out)
	 */
	void compileTemplate(){
		Expression t = compileTemplateFun();
		Variable out = Variable.create(OUT);
		defSelect(out, t);
		setTemplateGroup(createTemplateGroup());
	}
	
	
	/**
	 * Compile the template as a concat() 
	 * where variables are compiled as st:process()
	 */
	Expression compileTemplateFun(){
		Term t = createFunction(createQName(FUN_TEMPLATE_CONCAT));

		if (template != null){
			if (template.size() == 1){
				return compileTemplate(template.get(0), false, false);
			}
			else {
				for (Expression exp : template){                                   
					exp = compileTemplate(exp, false, false);
					t.add(exp);                                   
				}
			}
		}

		return t;
	}
        
    
	/**
	 * if exp is a variable: (st:process(?x) as ?px)
	 * if exp is meta, e.g. group_concat(?exp): group_concat(st:process(?exp))
	 * if exp is a simple function: xsd:string(?x)  (no st:process)
	 */
	Expression compileTemplate(Expression exp, boolean coalesce, boolean group){
		if (exp.isVariable()){
			exp = compile(exp.getVariable(), coalesce);
		}
		else if (isMeta(exp)){
			// variables of meta functions are compiled as st:process()
			// variable of xsd:string() is not
			exp = compileTemplateMeta((Term) exp, coalesce, group);
		}
		return exp;
	}
	
	
	/**
	 * Some function play a special role in template:
	 * concat, group_concat, coalesce, if
	 * Their variable argument are compiled as st:process(var)
	 */
	boolean isMeta(Expression exp){
		if (! exp.isFunction()){
			return false;
		}
		for (String name : PPRINT_META){
			if (exp.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	boolean isIF(Expression exp){
		return exp.getName().equals(IF);
	}
	
	boolean isCoalesce(Expression exp){
		return exp.getName().equals(COALESCE);
	}
        
        boolean isGroup(Expression exp){
		return exp.getName().equals(GROUPCONCAT);
	}
        
        // st:concat()
        boolean isSTCONCAT(Expression exp){
		return exp.getName().equals(FUN_TEMPLATE_CONCAT);
	}
        
	/**
	 * concat() st:concat()
	 * group_concat()
	 * if()
	 * coalesce()
         * copy the function and compile its variable as (coalesce(st:process(?var), "")
	 */
	Term compileTemplateMeta(Term exp, boolean coalesce, boolean group){		
		Term t = copy(exp, group);
		boolean isIF = isIF(exp);
                boolean isCoalesce = isCoalesce(exp);
                
		int count = 0;
		
		for (Expression ee : exp.getArgs()){
			if (count == 0 && isIF){
				// not compile the test of if(test, then, else)
			}
			else {
				ee = compileTemplate(ee, coalesce || isCoalesce, group || isGroup(exp));
			}
			count++;
			t.add(ee);
		}
		
                t.setArg(exp.getArg());
		t.setModality(exp.getModality());
		t.setDistinct(exp.isDistinct());
		return t;
		
	}
        
       Term copy(Term exp, boolean group) {
        Term t;
        if (exp.isFunction()) {
            if (group && isSTCONCAT(exp)) {
                // group {  box {} } := group_concat(concat( .. st:concat()))
                // rewrite box st:concat() as concat() in case box{ st:number() }
                // otherwise st:number() would act as a Future in st:concat()
                t = Term.function(CONCAT);
            } else if (exp.getCName() != null) {
                t = createFunction(exp.getCName());
            } else {
                t = Term.function(exp.getName());
            }
        } else {
            t = Term.create(exp.getName());
        }
        return t;
    }
	
	/**
	 * In template { } a variable ?x is compiled as:
	 * coalesce(st:process(?x), "")
	 * if ?x is unbound, empty string "" is returned
         * if we are already inside a coalesce, return st:process(?x)
	 */
        Term compile(Variable var, boolean coalesce){		
		Term t = createFunction(createQName(FUN_PROCESS), var);
                if (! coalesce){
                    t = Term.function(COALESCE, t, getEmpty());
                }
		return t;
	}
        
        Term compile(Constant cst){		
		Term t = createFunction(createQName(FUN_PROCESS_URI), cst);               
		return t;
	}
	
	/**
	 * 
	 * additional aggregate(?out)
         * default is st:group_concat
         * it may be redefined in template st:profile
         * using st:define(st:aggregate(?x) = st:agg_and(?x))
	 */
	Term createTemplateGroup(){
		Variable var = Variable.create(OUT);
		Term t = createFunction(createQName(FUN_TEMPLATE_AGG));
		t.add(var);
		t.setModality(getSeparator());
                t.setArg(getExpSeparator());
		return t;
	}
        
        /**
         * Aggregate that build the result of a template when there are several results
         * default is group_concat
         * draft: agg_and
         */
        public static void setTemplateAggregate(String s){
            FUN_TEMPLATE_AGG = s;
        }
        
        public static void setTemplateConcat(String s){
            FUN_TEMPLATE_CONCAT = s;
        }
	
	Constant getEmpty(){
		if (empty == null){
			empty = Constant.create("", null, null);
		}
		return empty;
	}
	
	
	Variable templateVariable(Variable var){
		return Variable.create(KGRAMVAR + countVar++);
	}

	public boolean isRenameBlankNode() {
		return renameBlankNode;
	}

	public void setRenameBlankNode(boolean renameBlankNode) {
		this.renameBlankNode = renameBlankNode;
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

	public String getSeparator() {
		return templateSeparator;
	}
       
        public void setSeparator(String sep) {
            this.templateSeparator = sep; //clean(sep);
	}
        
        public void setSeparator(Expression exp) {
            if (exp.isConstant()){
		setSeparator(exp.getLabel());
            }
            templateExpSeparator = exp;            
	}
        
        public Expression getExpSeparator(){
            return templateExpSeparator;
        }

	public boolean isTurtle() {
		return isTurtle;
	}

	private void setTurtle(boolean isTurtle) {
		this.isTurtle = isTurtle;
	}

	public Term getTemplateGroup() {
		return templateGroup;
	}

	private void setTemplateGroup(Term templateGroup) {
		this.templateGroup = templateGroup;
	}

        public void defArg(Variable var){
            argList.add(var);
        }
        
        public List<Variable> getArgList(){
            return argList;
        }
        
        public void defProfile(Constant cst){
            profile = cst.getLabel();
        }
        
        public String getProfile(){
            return profile;
        }

	
	/**********************************************************
	 * 
	 * End of Template
	 * 
	 *********************************************************/
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
        
	public void setApproximateSearchOptions(String key, String value){
            if(this.approximateSearchOptions.containsKey(key)){
                this.approximateSearchOptions.get(key).add(value);
            }else{
                List l = new ArrayList();
                l.add(value);
                this.approximateSearchOptions.put(key, l);
            }
        }
        
        public List<String> getApproximateSearchOptions(String key){
            return this.approximateSearchOptions.get(key);
        }
}
