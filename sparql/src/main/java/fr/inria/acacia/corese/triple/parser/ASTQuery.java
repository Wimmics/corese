package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.update.ASTUpdate;

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

public class ASTQuery  implements Keyword {

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
	public static final String KGRAMVAR = "?_kgram_";
	public static final String SYSVAR = "?_cos_";
	public static final String BNVAR = "?_bn_";
	static final String NL 	= System.getProperty("line.separator");

	static int nbt=0; // to generate an unique id for a triple if needed
	
	public final static int QT_SELECT 	= 0;
	public final static int QT_ASK 		= 1;
	public final static int QT_CONSTRUCT= 2;
	public final static int QT_DESCRIBE = 3;
	public final static int QT_DELETE 	= 4;
	public final static int QT_UPDATE 	= 5;
	

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
	boolean selectAll = false;
//    boolean hasGet=false;   // is there a get:gui ?
//    boolean hasGetSuccess=false; // if get:gui, does the first one has a value ?
    // validation mode (check errors)
    private boolean validate = false; 
	boolean sorted = true; // if the relations must be sorted (default true)
	boolean debug = false, isCheck = false;
    boolean nosort = false, 
    // load from and from named documents before processing
    isLoad = false, 
    isCorrect = true;
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
    Exp bodyExp;
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
	List<Expression> sort 	 = new ArrayList<Expression>();
	List<Expression> lGroup  = new ArrayList<Expression>();
	
	List<Atom> from  		= new ArrayList<Atom>();
	List<Atom> named 		= new ArrayList<Atom>();
	List<Atom> defFrom, defNamed; 
	
    List<Atom> adescribe = new ArrayList<Atom>();
	List<String> stack = new ArrayList<String>(); // bound variables
	List<String> vinfo;
	List<String> errors;
	
	List<Variable> varBindings;
	List<List<Constant>> valueBindings;
	
	List<Boolean> reverseTable = new ArrayList<Boolean>();
	
	Hashtable<String, Expression> selectFunctions = new Hashtable<String, Expression>();
	ExprTable selectExp   = new ExprTable();
	ExprTable regexExpr   = new ExprTable();

    // pragma {}
    Hashtable<String, Exp> pragma;
    Hashtable<String, Exp> blank;

	NSManager nsm;
	ASTUpdate astu;


	
	class ExprTable extends Hashtable<Expression,Expression> {};

	/**
	 * The constructor of the class It looks like the one for QueryGraph
	 */
	private ASTQuery() {
    }
	
	ASTQuery(String query) {
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
	
	public List<Atom> getFrom() {
		return from;
	}

	public List<Atom> getNamed() {
		return named;
	}
	
	public void setNamed(String uri) {
		setNamed(createConstant(uri));
	}

	public void setNamed(Atom uri) {
		named.add(uri);
	}

	public void setFrom(String uri) {
		setFrom(createConstant(uri));
	}

	public void setFrom(Atom uri) {
		from.add(uri);
	}

	

	public void setDefaultFrom(List<String> from){
		if (from != null && from.size()>0){
			defFrom = new ArrayList<Atom>(from.size());
			for (String name : from){
				defFrom.add(createConstant(name));
			}
		}
	}
	
	public void setDefaultNamed(List<String> from){
		if (from != null && from.size()>0){
			defNamed = new ArrayList<Atom>(from.size());
			for (String name : from){
				defNamed.add(createConstant(name));
			}
		}
	}
	
	public void setDefaultFrom(String[] from){
		if (from != null && from.length>0){
			defFrom = new ArrayList<Atom>(from.length);
			for (String name : from){
				defFrom.add(createConstant(name));
			}
		}
	}
	public void setDefaultNamed(String[] from){
		if (from != null && from.length>0){
			defNamed = new ArrayList<Atom>(from.length);
			for (String name : from){
				defNamed.add(createConstant(name));
			}
		}
	}
	

	
	List<Atom> getDefaultFrom(){
		return defFrom;
	}
	
	List<Atom> getDefaultNamed(){
		return defNamed;
	}
	
	public List<Atom> getActualFrom(){
		if (from.size() > 0) return from;
		if (defFrom != null) return defFrom;
		return from;
	}
	
	public List<Atom> getActualNamed(){
		if (named.size() > 0) return named;
		if (defNamed != null) return defNamed;
		return named;
	}
	
	
	public void setValidate(boolean b){
		validate = b;
	}
	
	public boolean isValidate(){
		return validate;
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
	
	public List<String> getErrors(){
		return getGlobalAST().errors();
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

	public void setNamed(List<Atom> named) {
		this.named = named;
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
    public  Term createFunction(Constant name, ExpressionList el) {
    	return createFunction(name.getName(), el);
    }
    
    
    public  Term createFunction(String name, ExpressionList el) {
    	Term term = createFunction(name);
    	term.setDistinct(el.isDistinct());
    	if (el.getSeparator()!=null){
    		term.setModality(clean(el.getSeparator()));
    	}
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
    
    public  Term createExist(Exp exp, boolean negation) {
    	Term term = Term.function(Term.EXIST);
    	term.setExist(Exist.create(exp));
    	if (negation){
    		term = negation(term);
    	}
    	return term;
    }
    
    
    public RDFList createRDFList(List<Expression> list){
    	return createRDFList(list, true);
    }
    
    /**
     * Create an RDF List (rdf:first/rdf:rest)
     * if close = true, end by rdf:nil (usual case) 
     * Return an RDFList which is an And on the triples
     * Can get starting first blank node with function head()
     * i.e. the subject of first triple
     */
    public RDFList createRDFList(List<Expression> list, boolean close){
    	Expression 
    	first = null, 
    	rest = null, 
    	blank; 
    	Exp triple;
    	RDFList rlist = RDFList.create(); 
    	rlist.setList(list);
  
    	for (Expression exp : list){
    		blank = newBlankNode();
  			
  			if (first == null) {
  				first = blank;
  				rlist.setHead(first);
  			}
  			
  			if (rest != null) {  				
  				triple = generateRest(rest, blank);
  				rlist.add(triple);
  			}
  			
  			triple = generateFirst(blank, exp);
  			rlist.add(triple);
  			
  			rest = blank;
    	}
    	
    	if (close){
      		triple = generateRest(rest, createQName(RDFS.qrdfNil));
       		rlist.add(triple);
    	}
    	
    	return rlist;
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
			return (Constant) exp;
		}
		Constant cst =  createConstant(RootPropertyQN);
		cst.setExpression(exp);
		return cst;
	}
	
	/**
	 * Create a triple for SPARQL JJ Parser
	 * Process the case where the property is a regex
	 * generate a filter(match($path, regex))
	 * return {triple . filter}
	 */
	public  Exp createTriple(Expression subject, Atom predicate, Expression object){
		Triple t = Triple.create(subject, predicate, object);
		
		Expression exp = t.getProperty().getExpression();
		
		if (exp != null){
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
			boolean isInverse = false;
			while (true){
				if (exp.isFunction()){
					if (exp.getName().equals(SINV)){
						exp = exp.getArg(0);
						isInverse = true;
						mode += "i";
					}
					else if (exp.getName().equals(SSHORT)){
						exp = exp.getArg(0);
						mode += "s";
					}
					else if (exp.getName().equals(SSHORTALL)){
						exp = exp.getArg(0);
						mode += "sa";
					}
					else if (exp.getName().equals(SDEPTH)){
						exp = exp.getArg(0);
						mode += "d";
					}
					else if (exp.getName().equals(SBREADTH)){
						exp = exp.getArg(0);
						mode += "b";
					}
					else break;
				}
				else break;
			}
			t.setRegex(exp);
			t.setMode(mode);
			
			if (isKgram()){
				if (isInverse){
					exp = Term.function(Term.SEINV, exp);
					t.setRegex(exp);
				}
				return t;
			}
			else 
			{
				Term fun = Term.function(MATCH, var, exp);
				if (mode != ""){
					fun.add(new Constant(mode, RDFS.xsdstring));
				}

				Triple ft = Triple.create(fun);
				BasicGraphPattern bg = BasicGraphPattern.create(t);
				bg.add(ft);
				return bg;
			}
		}
		else return t;
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
	 * Filter test associated to path regex exp
	 */
	public Expression setRegexTest(Expression exp, Expression test){
		regexExpr.put(exp, test);
		//exp.setExpr(test);
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
		s = clean(s);
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

    public Variable newBlankNode() {
		Variable var = createVariable( BNVAR + getNbBNode());
		var.setBlankNode(true);
		return var;
	}
    
    public Variable newBlankNode(Exp exp, String label) {
    	if (blank == null) blank = new Hashtable<String, Exp>();
    	Exp ee = blank.get(label);
    	if (ee == null){
    		blank.put(label, exp);
    	}
    	else if (ee != exp){
    		setCorrect(false);
    		logger.error("Blank Node used in different patterns: " + label);
    	}

    	Variable var;
    	if (exp instanceof BasicGraphPattern) {
    		BasicGraphPattern pat = (BasicGraphPattern)exp;
    		var = pat.getBNode(label);
    		if (var==null) {
    			// create a new blank node and put it in the table
    			var = newBlankNode();
    			pat.addBNodes(label, var);
    		}
    	} 
    	else {
    		logger.error("ERROR - stack not instance of BasicGraphPattern: "+exp.getClass());
    		var = newBlankNode();
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
    
    
    public void compile(){
    	if (isConstruct()  && getBody()!=null) {
            compileConstruct();
        } 
        else if (isAsk()) {
        	compileAsk();
        } 
        else if (isDescribe()) {
        	compileDescribe();
			setBasicSelectAll(true);
       }      
		Exp exp = getBody();
		if (exp != null){
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

    private void compileDescribe() {
    	String root = KGRAMVAR;
    	String PP = root + "p_";
    	String VV = root + "v_";
    	
		Exp bodyExpLocal = getBody();
		
		boolean describeAllTemp = isDescribeAll();
        setDescribeAll(false);
		
        BasicGraphPattern body = BasicGraphPattern.create();
        
		for (Atom expression : adescribe) {
			
			//setMerge(true);

			//// create variables
			int nbd = getVariableId();
			Variable prop1 = createVariable(PP + nbd);
			Variable val1  = createVariable(VV + nbd);
			
			nbd = getVariableId();
			Variable prop2 = createVariable(PP + nbd);
			Variable val2  = createVariable(VV + nbd);
		
			//// create triple sd ?p0 ?v0
			Triple triple = Triple.create(expression, prop1, val1);
			Exp e1 = triple; 
			BasicGraphPattern bgp1 = BasicGraphPattern.create();
			bgp1.add(e1);
			body.add(e1);
			
			//// create triple ?v0 ?p0 sd
			Triple triple2 = Triple.create(val2, prop2, expression);
			Exp e2 = triple2; 
			BasicGraphPattern bgp2 = BasicGraphPattern.create();
			bgp2.add(e2);
			body.add(e2);

			//// create the union of both
			Or union = new Or();
			union.add(bgp1);
			union.add(bgp2);
			
			// make the union optional
			Option opt =  Option.create(union);
			
			bodyExpLocal.add(opt);
			
			if (expression.isVariable()){ 
				setSelect(expression.getVariable());
			}
		}
		setDescribeAll(describeAllTemp);
		setBody(bodyExpLocal);
		
		if (isKgram()){
			setInsert(body);
			setConstruct(body);
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
    	getSparqlPrefix(sb);
    	
    	if (isUpdate()){
    		getUpdate().toString(sb);
    	}
    	else {
    		getSparqlHeader(sb);
    		if (! isDescribe() || getBody()!=null){
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
        
        for (Exp e : getPrefixExp().getBody()) {
            Triple t = e.getTriple();
            String r = t.getSubject().getName();
            String p = t.getPredicate().getName();
            String v = t.getObject().getName();
            
            // if v starts with "<function://", we have add a ".", so we have to remove it now
            if (v.startsWith(KeywordPP.CORESE_PREFIX)) {
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
    	List<Atom> from = getFrom();
    	List<Atom> named = getNamed();
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
    			for (Atom s : getSelectVar()){
    				if (getExpression(s.getName()) != null) {
    					sb.append("(");
    					getExpression(s.getName()).toString(sb);
    					sb.append(" as "  + s + ")");
    				}
    				else {
    					sb.append(s + SPACE);
    				}
    			}
    		} 
  		
    	} 
    	else if (isAsk()) {
    		sb.append(KeywordPP.ASK + SPACE);
    	} 
    	else if (isConstruct()) {
    		sb.append(KeywordPP.CONSTRUCT + SPACE); 
    		getConstruct().toString(sb); 
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
    	if (! isConstruct())    // because it's already done in the construct case
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
    	if (! (isDescribe() && ! isWhere()))
    		sb.append(KeywordPP.WHERE + NL) ; 

    	return sb;
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

    	if (getVariableBindings() != null){
    		sb.append(KeywordPP.BINDINGS);
    		sb.append(SPACE);

    		for (Atom var : getVariableBindings()){
    			sb.append(var.getName());
    			sb.append(SPACE);
    		}
    		sb.append(KeywordPP.OPEN_BRACKET);
    		sb.append(NL);

    		for (List<Constant> list : getValueBindings()){
    			sb.append(KeywordPP.OPEN_PAREN);

    			for (Constant value : list){
    				sb.append(value);
    				sb.append(SPACE);
    			}
    			sb.append(KeywordPP.CLOSE_PAREN);
    			sb.append(NL);
    		}

    		sb.append(KeywordPP.CLOSE_BRACKET);
    		sb.append(NL);
    	}

    	if (getPragma()!=null){
    		sb.append(KeywordPP.PRAGMA);
    		sb.append(SPACE);
    		getPragma().toString(sb);
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
    
   
    public void defNamespace(String prefix, String ns){
    	if (prefix.endsWith(":")){
    		prefix = prefix.substring(0, prefix.length() - 1); // remove :
    	}
    	getNSM().defNamespace(ns, prefix);
    	Triple triple = Triple.createNS(
    			 Constant.create(KeywordPP.PREFIX),  Constant.create(prefix), 
    			 Constant.create(ns));
    	addPrefixExp(triple);
    }
    
    public void defBase(String ns){
    	getNSM().setBase(ns);
       	Triple triple = Triple.createNS(
    			 Constant.create(KeywordPP.BASE),  Constant.create(""), 
    			 Constant.create(ns));
    	addPrefixExp(triple);
    }
    
    public String defURI(String s){
    	//s = nsm.prepare(s);
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
    
    public void setVariableBindings(List<Variable> list){
    	varBindings = list;
    }
    
    public List<Variable> getVariableBindings(){
    	return varBindings;
    }
    
    public void setValueBindings(List<Constant> list){
    	if (valueBindings == null){
    		valueBindings = new ArrayList<List<Constant>>();
    	}
    	valueBindings.add(list);
   }
    
    public List<List<Constant>> getValueBindings(){
    	return valueBindings;
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
    	if (! contains(selectVar, var)){
          selectVar.add(var);
    	}
    }
    
    boolean contains(List<Variable> list, Variable var){
    	for (Variable vv : list){
    		if (vv.getName().equals(var.getName())){
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean checkSelect(Variable var){
    	if (contains(selectVar, var)){
    		setCorrect(false);
    		return false;
    	}
    	return true;
    }
    
        
    public void setSelect(Variable var, Expression e) {
    	setSelect(var);
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
    			if (isKgram()){
    				setSelect(vv);
    			}
    			else {
    				Term get = createGet(var, n++);
    				setSelect(vv, get);
    			}
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
    	if (b) setResultForm(QT_ASK);
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

		// TODO: check
	public static String clean(String str) {
		if (str.length() <= 1) return str;
		if ((str.startsWith(SQ3)   && str.endsWith(SQ3)) ||
			(str.startsWith(SSQ3)  && str.endsWith(SSQ3))){
			str = str.substring(3,(str.length()-3));
			return recClean(str);
		}
		else
		if ((str.startsWith(SQ1)   && str.endsWith(SQ1)) ||
			(str.startsWith(SSQ)  && str.endsWith(SSQ))){
			str = str.substring(1,(str.length()-1));
			return recClean(str);
		}
		return str;
	}
	
	//	 replace \\ by \ inside the string
	// \" -> "
	// \' -> '
	static String recClean(String str){
		int index = str.indexOf(BS);
		if (index != -1){
			str = str.substring(0, index) + "\\" + 
				  str.substring(index+BS.length());
			return recClean(str);
		}
		index = str.indexOf(ESQ); //  \" -> "
		if (index != -1){
			str = str.substring(0, index) + "\"" + 
				  str.substring(index+ESQ.length());
			return recClean(str);
		}
		index = str.indexOf(ESSQ); //  \' -> "
		if (index != -1){
			str = str.substring(0, index) + "'" + 
				  str.substring(index+ESSQ.length());
			return recClean(str);
		}
		return str;
	} 
	
	public boolean isDefineExp(Expression exp){
		return selectExp.get(exp) != null;
	}
	
	public Expression getExpression(String name){
		return selectFunctions.get(name);
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
	
    
    void bind(String var){
        if (! stack.contains(var)){
            stack.add(var);
        }
    }
    
    boolean isBound(String var){
        return stack.contains(var);
    }

    public List<String> getStack() {
    	return stack;
    }

    public void setStack(List<String> stack) {
        this.stack = stack;
    }
    void clear(){
        stack.clear();
    }
    boolean hasOptionVar(Expression exp){
        return  exp.isOptionVar(stack);
    }
    int getStackSize(){
        return stack.size();
    }

	
}
