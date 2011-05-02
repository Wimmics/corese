package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IModel;
import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * Its the main parser class for the 1st parser (Corese RDF Query Language),
 * but it is also used for the 2nd parser (Sparql), as an execution environment.
 * <br>
 * @author Olivier Corby
 */

public class Parser {
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Parser.class);
	
	ICoreseParser server;
	ASTQuery astq;
	//NSManagerParser nsm;
	IModel model=null; // may contain an object to complete get:gui item
	int nbvar=0, vcount = 0;
	final static String GET = RDFS.GETPrefix + ":";
	final static String EGET = RDFS.EGETPrefix + ":";
	final static String CHECK = RDFS.CHECKPrefix + ":";
	final static String VARFROM = ExpParser.SYSVAR + "from_";
	final static String VARSTATE = ExpParser.SYSVAR + "st_";
	
	String access=null;
	boolean prettyPrint = false;
	boolean isKgram = false;
	// if true: when from and not from named there is no named graph (and converse) 
	// ?s p o graph ?g {?s ?q ?v} s cannot be blank
	boolean SPARQLCompliant = !true;//Corese.SPARQLCompliant;
	
	Vector<Expression> filters;
	
	 Parser(ICoreseParser s) {
		this(s, null);
	}
	
	 Parser(){}
	 
	 Parser(ICoreseParser s, IModel m) {
		filters = new Vector<Expression>();
		server=s;
		model = m;
	}
	
	 public static Parser create(ICoreseParser s){
		 return new Parser(s, null);
	 }
	 
	 public static Parser create(){
		 return new Parser();
	 }


	 public static Parser create(ICoreseParser s, IModel m){
		 return new Parser(s, m);
	 }
	
	public static boolean isVarFrom(String name){
		return name.startsWith(VARFROM);
	}
	
	public void setASTQuery(ASTQuery aq) {
		astq = aq;
	}
	
	/**
	 * IModel contains data that complete the query using get:elem
	 * arguments : ?x rdf:type get:doc_type
	 * completed using model.getParameter(doc_type)
	 * IModel can retrieve such elements by getParameter(elem)
	 * get:doc_type is replaced by its value, else triple is withdrawn
	 *
	 */
	public void setModel(IModel m){
		model=m;
	}
	

	public ASTQuery compile(ASTQuery aq){
		setASTQuery(aq);
		// construct/ask/describe
		aq.complete(this);
		// where
		if (aq.getBody() != null){
			validate(aq, aq.getBody());
		}
		return aq;
	}
	
	// draft KGRAM compiler
	public ASTQuery ncompile(ASTQuery aq){
		isKgram = aq.isKgram();
		setASTQuery(aq);
		// construct/ask/describe
		aq.complete(this);
		if (aq.isDescribe()){
			aq.setBasicSelectAll(true);
		}
		//validate();
		// where
		Exp exp = aq.getBody();
		if (exp != null){
			if (! isKgram) exp = from(exp);
			//TODO: rec exp.finalize(this)
			aq.setQuery(exp);
		}
		return aq;
	}
	
	
	boolean validate(){
		//System.out.println("** Parser: " + exp);
		try {
			astq.getBody().validate(new Bind(), 0);
			return true;
		}
		catch (QuerySemanticException e){
			//System.out.println("** Parser: " + e.getMessage());
			astq.addError(e.getMessage());
			astq.setCorrect(false);
			return false;
		}
	}
	
	/**
	 * Compile from into source
	 * Expand prefix to namespace
	 * Generate path
	 * Distribute OR vs AND, create an OR of AND
	 * Compile option
	 * Set the ASTQuery with the expression
	 */
	public ASTQuery validate(ASTQuery aq, Exp exp){
		boolean trace = !true;
		if (trace)
			logger.debug("** Parser : " + exp);
		//aq.setTripleQuery(exp);
		
		if (SPARQLCompliant){
			//System.out.println("** Parser: " + exp);
			validate();
		}
		
		
		// expand prefix, generate path :
		exp = exp.complete(this);
		// assign score variables to leaf triples :
		//if (aq.getScore()) exp.setScore(new Vector<String>());
		// compile from and from named as source ?src statements
		exp = from(exp);
		if (aq.getScore()) exp.setScore(new Vector<String>());
		if (trace)
			logger.debug("** Parser to distrib " + exp);
		exp = exp.distrib(); // A and (B or C) -> (A and B) or (A and C)
		if (trace)
			logger.debug("** Parser to sort " + exp);
		// sort as {triple filter option [fake filter]} may add fake relation to carry filter
		exp = exp.recsort(this);
		if (trace)
			logger.debug("** Parser to option " + exp);
		// compile option with ID first/last, then collapse options
		exp = exp.option(this);
		if (trace)
			logger.debug("** Parser out " + exp);
		
		if (aq.isRule() && ! aq.isConclusion()){
			exp = exp.validateRule();
		}
		
		if (exp.validate()){
			//exp.process(aq); // set  exp into aq
			aq.setQuery(exp);
		}
		if (trace)
			logger.debug("** Parser out " + aq.getQuery());
		return aq;
	}
	
	class Context {
		Vector<String> localVars;
		Vector<String> localStates;
		Vector<String> leafStates;
		
		Context(){
			localVars =   new Vector<String>();
			localStates = new Vector<String>();
			leafStates =  new Vector<String>();
		}
	}
	
	/**
	 * from named uri : graph ?src { pattern } => filter ?src = uri
	 * from uri :  if not in graph ?src, ?x ?p ?y => source ?tmp { ?x ?p ?y } filter ?tmp = uri
	 * graph ?src { pattern } => assign ?src to inner triples
	 */
	Exp from(Exp exp) {
		BasicGraphPattern aexp = BasicGraphPattern.create();
		Vector<String> named = astq.getActualNamed();
		Vector<String> from =  astq.getActualFrom();
		
//		if (SPARQLCompliant){
//			if (named.size() == 0     && from.size() > 0){
//				named.add("");
//			}
//			else if (named.size() > 0 && from.size() == 0){
//				from.add("");
//			}
//		}

		// first : process state ?state {PAT}
		// generate graph ?si {TRIPLE}  and collect ?si in vars
		// generate ?state sso ?si
		Env env = new Env(true);
		env.setName(VARSTATE);
		// 1. assign state ?si variable to its inner triples
		exp.setSource(this,  env, null, false);
		// generate ?state sso ?si + leaf condition 
		exp.defState(this, env);
		
		// 2. process FROM NAMED uri
		if (named.size() > 0) {
			// 1. collect graph ?src and state ?si in vars vector
			// 2. generate filter var = uri
			// process optional {graph ?src} in option locally  
			Vector<String> vars = new Vector<String>();
			exp.collectSource(this, vars, named);
			if (vars.size() > 0) {
				// there are std source variables (not option source)
				// from named uri :   add filter ?src = uri || ...
				Exp expNamed = exp.source(this, vars, named);
				aexp.add(expNamed);
			}
		}

		// 3. process FROM uri
		if (! isKgram() && from.size() > 0) {
			Vector<String> varFrom = new Vector<String>();
			boolean generate = from.size() >= 1;
			// generate/attach  source var to triples, collect them in varFrom :
			// source var in option are processed locally, hence they are not
			// present in varFrom.
			exp.setFromSource(this, VARFROM, varFrom, from, generate);
			if (varFrom.size() > 0){
				// generate var = uri for each var and each uri
				Exp expFrom = exp.source(this, varFrom, from);
				aexp.add(expFrom);
			}
		}
		
		env = new Env(false);
		env.setName(VARFROM);
		// 4. assign graph ?src variable to inner triples
		exp.setSource(this,  env, null, false);
		
		if (aexp.size() > 0){
			aexp.add(0, exp);
			exp=aexp;
		}
		return exp;
	}
	
	
	public static boolean[] getArray(Vector<Boolean> bv) {
		boolean[] res = new boolean[bv.size()];
		for (int i=0; i<res.length; i++){
			res[i]=(bv.get(i)).booleanValue();
		}
		return res;
	}
	
	/**
	 *
	 * @param var: use for "count" and "distance"
	 * @param breverse: indicate if we want the ASC or the DESC order (asc by default <=> breverse = false)
	 */
	void setSort(String var, boolean breverse) {
		astq.setSort(var, breverse);
	}
	
	/**
	 *
	 * @param e: e can be a variable, a term or an expression
	 * @param breverse: indicate if we want the ASC or the DESC order (asc by default <=> breverse = false)
	 */
	void setSort(Expression e, boolean breverse) {
		astq.setSort(e, breverse);
	}
	
	void setGroup(String var) {
		astq.setGroup(var);
	}
	
	void setCount(String var) {
		astq.setCount(var);
	}
	
	void setSelect(String var){
		//astq.setSelect(var);
	}
	
	void setSelectAll(boolean b){
		astq.setSelectAll(b);
	}
	
	void setSelect(){
		astq.setSelect();
	}
	
	boolean isDescribe() {
		return astq.isDescribe();
	}
	
	boolean isKgram(){
		return isKgram;
	}
	
	boolean isDefType(Constant predicate){
		String uri = predicate.getName();
		if (server != null)
			return server.isDefType(getNSM().toNamespace(uri));
		else 
			return 
			uri.equals(RDFS.rdftype) ||
			uri.equals(RDFS.RDFTYPE) ||
			getNSM().toNamespace(uri).equals(RDFS.RDFTYPE);
	}

	/**
	 * Does first get:gui (if any) has a value ?
	 */
	boolean isSelected(Exp exp){
		return astq.isSelected(exp);
	}
	
	/**
	 * There is a get:gui and if b == true, it has a value
	 * Store whether first get:gui has a value
	 */
	void setGetGui(boolean b){
		astq.setGetGui(b);
	}
	
	void setOne(boolean b){
		astq.setOne(b);
	}
	
	void setSorted(boolean b){
		astq.setSorted(b);
	}
	
	void setDebug(boolean b){
		astq.setDebug(b);
	}
	
	/**
	 * Collect bound() filters for option(union) bound()
	 */
	void addFilter(Expression exp){
		if (exp.isBound()){
			filters.add(exp);
		}
	}
	
	/**
	 * exp is an option{union}
	 * if there is a bound() we cannot evaluate it
	 * TODO : we should test whether bound() is about a variable from exp
	 * and if this variable is free and bound by exp ...
	 * currently we eliminate all bound()
	 */
	boolean hasBound(Exp exp){
		return filters.size() > 0;
	}
	
	void setDistinct(boolean b) {
		astq.setDistinct(b);
	}
	
	void setNamed(String uri){
		astq.setNamed(uri);
	}
	
	void setFrom(String uri){
		astq.setFrom(uri);
	}
	
	
	void setSource(String src){
		astq.setSource(src);
	}
	
	void bind(String var){
		astq.bind(var);
	}
	
	boolean isBound(String var){
		return isBound(var);
	}
	
	/**
	 *
	 * @param size : pop stack until size() == size
	 */
	void pop(int size){
		Vector<String> stack = astq.getStack();
		int n = stack.size() - size;
		for (int i=0; i<n; i++){
			if (stack.size() >  0){
				stack.remove(stack.size() - 1);
			}
		}
		astq.setStack(stack);
	}
	
	void clear(){
		astq.clear();
	}
	
	boolean hasOptionVar(Expression exp){
		return  astq.hasOptionVar(exp);
	}
	
	int getStackSize(){
		return astq.getStackSize();
	}
	
	
	void setStrictDistinct(boolean b) {
		astq.setStrictDistinct(b);
	}
	
	void setAll(boolean b){
		astq.setUnion(b);
	}
	
	void setScore(boolean b){
		astq.setScore(b);
	}
	
	/**
	 * store filter containing bound() 
	 */
	void submit(Expression exp){
		
	}
	
	void setMerge(boolean b){
		astq.setMerge(b);
	}
	
	void setBinding(boolean b){
	}
	
	boolean isPGet(String str) {
		return ispGet(str) || ispCheck(str);
	}
	
	String getPValue(String str) {
		if (ispGet(str)) {
			return getValue(pget(str));
		}
		else if (ispCheck(str) && getValue(pget(str))!=null) {
			// check:list return list
			return pget(str);
		}
		else
			return null;
	}
	
	
	/**
	 * If exp is an evaluable qname such as get:gui
	 * where prefix gui is bound to Corese GETNS namespace
	 * return gui (the local name)
	 * else return null
	 */
	String pget(String exp) {
		if (ispGet(exp) || ispCheck(exp)){
			int index = exp.indexOf(":");
			if (index == -1) {
				return null;
			} else {
				return exp.substring(index + 1);
			}
		}
		else
			return null;
	}
	
	boolean ispGet(String str) {
		String ns=getNSM().getQNamespace(str);
		return (ns != null) && (ns.equals(RDFS.GETNS) || ns.equals(RDFS.EGETNS));
	}
	
	boolean ispCheck(String str) {
		String ns=getNSM().getQNamespace(str);
		return (ns != null) && (ns.equals(RDFS.CHECKNS));
	}
	
	void setConnex(boolean b){
		astq.setConnex(b);
	}
	
	void setTable(boolean b){
	}
	
	void setXMLBind(boolean b){
		astq.setXMLBind(b);
	}
	
	
	void setFake(boolean b) {
		astq.setFake(b);
	}
	
	
	void setFlat(boolean b){
		astq.setFlat(b);
	}
	
	void setPQuery(boolean b){
		astq.setPQuery(b);
	}
	
	void setDisplayBNID(boolean b){
		astq.setDisplayBNID(b);
	}
	
	void setMaxResult(int n){
		astq.setMaxResult(n);
	}
	
	void setMaxProjection(int n){
		astq.setMaxProjection(n);
	}
	
	void setThreshold(float n){
		astq.setThreshold(n);
	}
	
	void setMaxDisplay(int n) {
		astq.setMaxDisplay(n);
	}
	
	void setMore(boolean b){
		astq.setMore(b);
	}
	
	void setDelete(boolean b){
		astq.setDelete(b);
	}
	
//	void setAccess(String str){
//		//access=str;
//		astq.setAccess(getAccess(str));
//	}
//	
//	void setAccess(byte val){
//		astq.setAccess(val);
//	}
	
//	byte getAccess(String src) {
//		for (int i = 0; i < Cst.ACCESSARRAY.length; i++) {
//			if (src.equals(Cst.ACCESSARRAY[i]))
//				return (byte)i;
//		}
//		return Cst.PUBLIC;
//	}
	
	ASTQuery getASTQuery(){
		return astq;
	}
	
	void trace(Object o){
		astq.addInfo(o.toString());
	}
	
	String getAccess(){
		return access;
	}
	
	void setList(boolean b){
		astq.setList(b);
	}
	
	void setJoin(boolean b){
		astq.setJoin(b);
	}

	
	void setError(String mes){
		astq.addError(mes);
	}
	
	
	/* **************************************************************************
	 
	 Query parameters from the form in the req
	 
	 *****************************************************************************/
	
	
	String getParam(String name){
		if (model==null)
			return null;
		else return model.getParameter(name);
	}
	
	String[] getParameterValues(String name){
		if (model==null)
			return null;
		return model.getParameterValues(name);
	}
	
	boolean isEmpty(String value){
		return (value==null || value.equals("") || value.equals("none"));
	}
	
	NSManager getNSM(){
		return astq.getNSM();
	}
	
//	public void setNSM(NSManagerParser nsmp){
//		astq.setNSM(nsmp);
//		nsm = nsmp;
//	}
	
	String getValue(String name){
		return getParam(name);
	}
	
	String[] getValues(String name){
		String values[]=getParameterValues(name);
		return values;
	}
	
	/**
	 * str = the name of the gui element (i.e. gui)
	 *
	 */
	String getExtValue(String str) {
		String values[] = getValues(str); // there may be several values
		if (values == null || values.length == 0 || isEmpty(values[0])) {
			// no value
			setGetGui(false);
			return null;
		}
		else {
			setGetGui(true);
			return values[0];
		}
	}
	
	
	/**
	 * the connector of a multiple select : OR / AND
	 * default is OR
	 */
	String getBool(String name) {
		String exp = getParam(name + "_bool");
		if (exp != null && (exp.equals("on") || exp.equals("and")))
			return Keyword.SEAND;
		else
			return Keyword.SEOR;
	}
	
	String getOper(String name) {
		String EXP = "_oper";
		String exp = getParam(name + EXP);
		if (isEmpty(exp))
			exp = null;
		return exp;
	}
	
	/*
	 Generate a variable name
	 A new one because it enables to query several objects in the same gui
	 */
	String getVar(String name){
		return "?"+name + "_" + nbvar++;
	}
	
	String newVar(String name){
		return name +  vcount++;
	}
	
	int getTripleId(){
		return astq.getTripleId();
	}
	
}
