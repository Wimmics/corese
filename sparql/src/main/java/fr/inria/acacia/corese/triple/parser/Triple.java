package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.acacia.corese.triple.cst.RDFS;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * Represents a triple (resource property value)
 * <br>
 * @author Olivier Savoie
 */

public class Triple extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(Triple.class);
	
	final static String SDT = KeywordPP.SDT;
	final static String LANG = KeywordPP.LANG;
	final static String PREFIX = "PREFIX";
	final static String BASE = "BASE";
	static int ccid = 0;
	// nb different system variables in a query ...
	static final int MAX = 1000;
	ASTQuery ast;
	//String tproperty;
	Atom subject, object;
	// property variable
	Variable variable;
	// property qname (or uri if written as <>)
	Constant predicate;
	// graph ?src/uri
	Atom source;
	// draft for tuple
	Vector<Expression> vexp;
	// tuple contain a filter
	Expression exp, 
		// path regex
		regex;
	String mode;
	Vector<String> score; // score ?s { pattern }
	int id=-1; // an unique id for triple (if needed to generate variable/option)
	int star=0; // for path of length n
	int line=-1; 
	String rvar, pre;          // variable on get:gui::?x pre=?x value=get:gui
	boolean isexp=false;     // is a filter
	boolean isoption=false, isFirst=false, isLast=false, fake=false;  // is option::
	int firstOptionID = -1, lastOptionID = -1; // index of first/last triple of an option pattern
	boolean isRec = false;   // graph rec ?s {}
	boolean isall=false;     // all::p{n}
	boolean istype=false;    // rdf:type or <= rdf:type
	boolean isdirect=false;  // direct::rdf:type
	boolean isone=false;     // one:: only one occurrence of ?x ?p ?y
	boolean isset=false;     // all path of length <= n : relation{n}
	boolean trace = false;
	
	public Triple() {
		setID();
	}
	
	public Triple(int num) {
		id = num;
	}
	

	// subject pred::var object
	Triple(Atom sub, Constant pred, Variable var, Atom obj) {
		subject = sub;
		object  = obj;
		predicate = pred;
		variable = var;
		if (predicate.getName().equals(RDFS.rdftype) || predicate.getName().equals(RDFS.RDFTYPE)){
			istype = true;
		}
	}
	
	public static Triple create(Atom src, Atom sub, Constant pred, Variable var, Atom obj) {
		Triple t = new Triple(sub, pred, var, obj);
		t.source = src;
		t.setID();	
		return t;
	}
	
	public static Triple create(Atom src, Atom sub, Constant pred, Atom obj) {
		Triple t = new Triple(sub, pred, null, obj);
		t.source = src;
		t.setID();	
		return t;
	}
	
	public static Triple create(Atom sub, Constant pred, Variable var, Atom obj) {
		Triple t = new Triple(sub, pred, var, obj);
		t.setID();	
		return t;
	}
	
	public static Triple create(String subject, String property, String value){
	return create(null, subject, property, value);
	}
	
	public static Triple create(String source, 
			String subject, String property, String value){
		Constant src = null;
		if (source != null) src = new Constant(source);
		Triple t = Triple.create(src, new Constant(subject),
				new Constant(property), null, new Constant(value));
		return t;
	}
	
	// for triples
	public static Triple create(Expression subject, Atom predicate, Expression object) {
		if (! subject.isAtom() && ! object.isAtom()) return null;
		Variable var = null;
		Constant pred = null;
		if (predicate.isConstant()){
			pred = (Constant) predicate;
			var = pred.getIntVariable();
		}
		else {
			pred =  Constant.createResource(getRootPropertyQN());
			var = (Variable) predicate;
		}
		Triple t = new Triple((Atom)subject, pred, var, (Atom)object);
		t.setID();	
		t.setTriple(t.subject, predicate, t.object);
		return t;
	}
	
	
	// for filters
	public static Triple create(Expression exp) {
		Triple t = new Triple(); 
		t.exp = exp;
		t.isexp = true;
		t.setID();
		return t;
	}
	
	public static Triple createNS(Constant type, Constant prefix, Constant uri){
		return create(type, prefix, uri);
	}
	
	private void setTriple(Atom exp1, Atom atom, Atom exp2) {
		setOne(atom.isIsone());
	    setAll(atom.isIsall());
	    setDirect(atom.isIsdirect());
	    setIsset(atom.isIsset());
	    setPath(atom.getStar());
	    if (exp1.getIntVariable() != null){
	    	setRVar(exp1.getIntVariable().getName());
	    }
	    if (exp2.getIntVariable() != null){
	    	setVVar(exp2.getIntVariable().getName());
	    }
	}
	
	public Triple getTriple(){
		return this;
	}

	/**
	 * Used in union and old path
	 */
	Exp copy(){
		Triple triple=new Triple(subject, predicate, variable, object);
		triple.source = source;
		triple.score=score;
		triple.isone = isone;
		triple.isdirect = isdirect;
		triple.istype = istype;
		triple.isoption = isoption;
		triple.isexp = isexp;
		triple.exp = exp;
		triple.setID();
		return triple;
	}
	
	public void setID() {
		id = nextID();
	}
	
	public boolean isTriple(){
		return true;
	}
	
	synchronized static int nextID(){
		// nb max de variables systemes differentes ds une requete
		if (ccid == MAX){
			ccid = 0;
		}
		return ccid++;
	}
	
	public void setID(int num) {
		id = num;
	}
	
	public void setLine(int n){
		line = n;
	}
	
	public int getLine(){
		return line;
	}
	
	/************************************************************************
	 * 2. Semantic phase
	 * expand prefix with namespace uri
	 * expand get:gui
	 * expand path : p[2] p{2}
	 * return uri triple
	 */
	Exp complete(Parser parser, boolean option, boolean union) {
		if (true){
			// process get:gui
			// search a value associated to gui in the parser Model
			if (isexp){
				Expression cst = exp.parseGet(parser);
				if (cst == null){
					return null;
				}
				else {
					exp = cst;
				}
			}
			else {
				Exp exp1 = parseGet(parser);
				if (exp1 == null){
					return null;
				}
				else if (exp1.isTriple()){
					return ((Triple)exp1).validate(parser);
				}
				else {
					// recurse on exp1 to validate inner triples
					return exp1.complete(parser, option, union);
				}
			}
		}
		Exp stmt = validate(parser);
		return stmt;
	}
	
	public void setAST(ASTQuery a){
		ast = a;
	}
	
	public ASTQuery getAST(){
		if (ast == null) ast = defaultAST();
		return ast;
	}
	
	ASTQuery defaultAST(){
		ASTQuery ast = ASTQuery.create();
		ast.setKgram(true);
		ast.setBody(new And());
		return ast;
	}
	
	/****************************************************************************
	 * 3. Distribute AND/OR, here do nothing
	 */
	Exp distrib() {
		return this;
	}
	
	Exp option(Parser parser) {
		return this;
	}
	
	Exp sort() {
		return this;
	}
	
	Exp recsort(Parser parser) {
		return this;
	}
	
	/**
	 * 3. distribute AND/OR
	 */
	Exp product(Exp exp) {
		return exp.sproduct(this);
	}
	
	/**
	 * 3. Distribute AND/OR
	 * sproduct conceptually perfom arg * this (instead of this * arg)
	 * the args are permuted by product because of polymorphism weakness of java
	 * (and A B) C -> (and A B C)
	 */
	Exp sproduct(And exp) {
		exp.add(this);
		return exp;
	}
	
	/**
	 * (or A B) C -> (or AC BC)
	 */
	Exp sproduct(Or exp) {
		for (int i = 0; i < exp.size(); i++) {
			exp.eget(i).add(this);
		}
		return exp;
	}
	
	Exp sproduct(Triple exp) {
		logger.debug("*** sproduct should not happen between triple " + this + " " + exp);
		return this;
	}
	
	Exp sproduct(Option exp) {
		Exp aexp = new And(exp);
		aexp.add(this);
		return aexp;
	}
	
	/***************************************************************************
	 *
	 * 1. Parsing
	 */
	
	/**
	 * Value with expression such as ?x + ?y * 2
	 * @param st
	 * @return
	 */
	
	boolean isNumber(String str) {
		return ASTQuery.isNumber(str);
	}
	
	String decode(String str) {
		return str; //str.replace('+', ' ');
	}
	



	/**
	 * For term parser, remove group:: etc
	 */
	static String process(String str) {
		return str;
	}
	
	
	/***********************************************
	 *
	 * During parsing,  process get:gui
	 * get:gui is a reference to an GUI object that may return a value for get:gui
	 * if the triple is ?x p get:gui, replace get:gui by its value
	 * if no value, return null (the triple is skiped)
	 *
	 * **/
	
	/**
	 * Just after parsing the triple, process the get:gui if any
	 * if get:gui and no value, return null and warn the parser
	 */
	public Exp parseGet(Parser parser) {
		Exp exp1 = parseGetResource(parser);
		Exp exp2 = parseGetProperty(parser);
		Exp exp3 = parseGetValue(parser);
		Exp res = this;
		if (exp1 == null || exp2 == null || exp3 == null)
			return null;
		if (exp1 != this) { //   exp1 = (and this filter)
			res = exp1;
			if (exp3 != this) { //   exp3 = (and this filter)
				res.add(exp3.get(1));
			}
		} else {
			res = exp3;
		}
		return res;
	}
	
	/**
	 * ?x get:prop ?y
	 * retrieve the property name and replace the get:prop by the name
	 * This is done during parsing (ExpParser call Triple.parse())
	 */
	Exp parseGetProperty(Parser parser) {
		String exp = parser.pget(predicate.getName());
		if (exp == null) {
			return this;
		}
		String prop = parser.getValue(exp);
		if (parser.isEmpty(prop)) {
			return null;
		} else {
			//property = prop;
			if (isVariable(prop)){
				variable  = new Variable(prop);
				predicate =  Constant.createResource(getRootPropertyQN());
			}
			else {
				predicate = Constant.createResource(prop);
			}
			return this;
		}
	}
	
	/**
	 * At parse time, process a triple which has a value=get:gui
	 * where get: is bound to corese eval namespace
	 * search the value of get:gui from the parser env and set it as new value
	 * if there is an operator and/or a variable together with a value,
	 * generate an AND with triple and filter test of value
	 * example :
	 * let triple = ?x p get:gui
	 * let get:gui = value
	 * return ?x p value
	 * let triple = ?x p get:gui::?y
	 * return  ?x p ?y  AND  ?y = value
	 * if there is an operator (e.g. getOper(gui) return an oper)
	 * getOper(gui) search get:gui_oper i.e. a parameter with gui_oper as name
	 * from parser env.
	 * return  ?x p ?y  AND  ?y oper value
	 * if isDescribe() and there is no value for get:gui,
	 * generate a variable (or take ?y) and set triple as  option
	 * return the triple itself or an AND exp
	 */
	
	Exp parseGetResource(Parser parser) {
		boolean isget = false;
		String resource;
		String name = parser.pget(subject.getName()); // get:gui ?
		if (name != null) { // isa get:gui
			// will get its value from parser envt
			isget = true;
			resource = name; // name of gui element
		}
		if (!isget)
			return this;
		boolean isGetValue = parser.pget(object.getName()) != null; // is value also a get:gui
		// describe means that if get:gui has none value, must generate a
		// variable and an option
		Exp exp = this;
		String values[] = parser.getValues(name); // there may be several values
		if (values == null || values.length == 0) {
			parser.setGetGui(false);			
			String err = computeError(this);
			if (err != null) parser.getASTQuery().addError(err);
			return null;
		} else if (values.length > 1) {
			// as usual. I will process this later with an ||
			//parser.setGetGui(true);
			//return parseGetValues(parser, values);
			return this;
		}
		String val = values[0];
		//TermParser tp = new TermParser(parser);
		if (parser.isEmpty(val)) {
			// tell the parser there is no value for this get (it may be the first)
			parser.setGetGui(false);
			String err = computeError(this);
			if (err != null) parser.getASTQuery().addError(err);
			return null;
		} else { // there is a value val
			val = parser.getNSM().toPrefix(val, true);
			// tell the parser that the get:gui succeed
			parser.setGetGui(true);
			// is there an operator :
			String oper = parser.getOper(name);
			// if both res and val are get:gui, the operator is for value ...
			if (oper == null || isGetValue)
				oper = "=";
			// get the local variable if any :
			String var = rvar;
			if ((var == null || var.equals(val)) && oper.equals("=")) {
				// this triple modified to variable = value
				resource = val;
				//exp1 = tp.atom(resource);
				subject = parser.getASTQuery().createAtom(resource, parser);
			} else {
				// add a new triple that test : variable oper value
				// this triple
				// get or generate a variable :
				//var=getVariable(parser, describe); // use pre which is for value
				if (var == null)
					var = parser.getVar(name);
				// the resource of current triple is now var :
				resource = var;
				//exp1 = tp.atom(var);
				subject = parser.getASTQuery().createAtom(var, parser);
				// generate new triple : var oper val
				Atom e1 = parser.getASTQuery().createAtom(var, parser); //tp.atom(var);
				Atom e2 = parser.getASTQuery().createAtom(val, parser); //tp.atom(val);
				Triple test =  Triple.create(new Term(oper, e1, e2));
				//test.setID(parser.getTripleId());
				// add this new triple in an AND
				exp = new And();
				exp.add(this);
				exp.add(test);
			}
		}
		isget = false; // no more a get:gui because we retrieved the value
		// the exp will be processed in next phase for namespace, etc.
		return exp;
	}
	
	Exp parseGetValue(Parser parser) {
		boolean isget = false;
		//String name = get(value); // get:gui ?
		String value = object.getName();
		String name = parser.pget(value); // get:gui ?
		if (name != null) { // isa get:gui
			// will get its value from parser envt
			isget = true;
			value = name; // name of gui element
		}
		if (!isget)
			return this;
		// describe means that if get:gui has none value, must generate a
		// variable and an option
		String lang = getLang();
		String datatype = object.getSrcDatatype();
		boolean describe = parser.isDescribe();
		Exp exp = this;
		String values[] = parser.getValues(value); // there may be several values
		if (values == null || values.length == 0) {
			parser.setGetGui(false);
			String err = computeError(this);
			if (err != null) parser.getASTQuery().addError(err);
			return null;
		} else if (values.length > 1) {
			// as usual. I will process this later with an ||
			parser.setGetGui(true);
			return parseGetValues(parser, values);
			//return this;
		}
		String val = values[0];
		//TermParser tp = new TermParser(parser);
		if (parser.isEmpty(val)) {
			// tell the parser there is no value for this get (it may be the first)
			parser.setGetGui(false);
			String err = computeError(this);
			if (err != null) parser.getASTQuery().addError(err);
			if ((!describe)) {
				// generate no variable for them
				return null;
			} else {
				// describe => generate a variable when there exist no value
				value = getVariable(parser, describe, value);
				object = new Variable(value);
				setTOption(true);
			}
		} else { // there is a value val
			val = parser.getNSM().toPrefix(val, true);
			// tell the parser that the get:gui succeed
			parser.setGetGui(true);
			// is there an operator :
			String oper = parser.getOper(value);
			if (oper == null)
				oper = "=";
			// get the local variable if any :
			String var = getPre();
			if ((var == null || var.equals(val)) && oper.equals("=")) {
				// this triple modified to variable prop value
				value = val;
				//if (isLiteral(value))  isliteral = true;
				String str = value;
				if (datatype != null) {
					// get:gui^^xsd:date
					str += SDT + datatype;
				} else if (lang != null && lang != "") {
					// get:gui@en
					str += LANG + lang;
				}
				//exp2 = tp.atom(str);
				object = parser.getASTQuery().createAtom(str, parser);
				//this.exp = new Term(oper, exp1, exp2); // ???
			} else {
				// add a new triple that test : variable oper value
				// this triple
				// get or generate a variable :
				var = getVariable(parser, describe, value);
				// the value of current triple is now var :
				value = var;
				//exp2 = tp.atom(var);
				object = parser.getASTQuery().createAtom(var, parser);
				// generate new triple : var oper val
				Atom e1 = parser.getASTQuery().createAtom(var, parser); //tp.atom(var);
				String str = val;
				if (datatype != null) {
					// get:gui^^xsd:date
					str += SDT + datatype;
				} else if (lang != null && lang != "") {
					// get:gui@en
					str += LANG + lang;
				}
				Atom e2 = parser.getASTQuery().createAtom(str, parser); //tp.atom(str);
				Triple test =  Triple.create(new Term(oper, e1, e2));
				//test.setID(parser.getTripleId());
				//if (isLiteral(val))  test.isliteral = true; ??
				// add this new triple in an AND
				exp = new And();
				exp.add(this);
				exp.add(test);
			}
		}
		isget = false; // no more a get:gui because we retrieved the value
		// the exp will be processed in next phase for namespace, etc.
		return exp;
	}
	
	private String computeError(Triple triple) {
		String m = null;
		if (getSubject() != null && getSubject().getName().startsWith(Parser.EGET)) 
			m = getSubject().getName();
		else if (getObject() != null && getObject().getName().startsWith(Parser.EGET)) 
			m = getObject().getName();
		else if (getProperty().getName() != null && getProperty().getName().startsWith(Parser.EGET)) 
			m = getProperty().getName();
		if (m != null)
			m += " is not recognized";
		return m;		
	}
	
	/**
	 * There are several values for this get:gui triple
	 * Generate an OR with several tests for each possible value :
	 * ?x <=: c:Engineer  OR  ?x <=: c:Researcher
	 * if get:gui_bool = 'on' || get:gui_bool = 'and'
	 * generate AND instead of OR (default is OR)
	 * REQUIRE values.length >= 2 and no empty value in it
	 * @return
	 */
	Exp parseGetValues(Parser parser, String[] values) {
		Exp exp = this;
		parser.setGetGui(true);
		String value = object.getName();
		String oper = parser.getOper(value);
		String bool = parser.getBool(value);
		boolean istype = false;
		if (predicate != null && parser.isDefType(predicate)) {
			// when rdf:type, replace the rdf:type property by <=:
			// TODO : does not work if the values are variables ...
			istype = true;
			if (oper == null)
				oper = Keyword.STLE;
		}
		if (oper == null)
			oper = "=";
		String var;
		// get the variable if any
		if (!istype) {
			var = getVariable(parser, true, value);
			value = var; // for this triple
			object = new Variable(var);
		} else {
			var = subject.getName(); //resource;
		}
		//TermParser ep = new TermParser(parser);
		//Term root = makeBoolTerm(parser, ep, bool, oper, var, values, 0);
		Term root = makeBoolTerm(parser, bool, oper, var, values, 0);
		//isget = false; // no more a get:gui because we retrieved the value
		// the exp will be processed in next phase for namespace, etc.
		Triple orTriple;
		// Embedd the Term into a triple
		orTriple =   Triple.create(root); //new Triple( root.getArg(0), bool, root.getArg(1));
		//orTriple.setID(parser.getTripleId());
		if (istype) {
			// skip this triple that says ?x rdf:type ...
			// return  ?x <=: t1  OR  ?x <=: t2
			exp = orTriple;
		} else {
			exp = new And();
			exp.add(this);
			exp.add(orTriple);
		}
		return exp;
	}
	
	Term makeTerm(Parser parser, String oper, String res, String val) {
		Atom eres = parser.getASTQuery().createAtom(res, parser); //tp.atom(res);
		Atom evalue = parser.getASTQuery().createAtom(val, parser); //tp.atom(val);
		return new Term(oper, eres, evalue);
	}
	
	/**
	 * Generate an OR term from a list of values
	 */
	Term makeBoolTerm(Parser parser, String bool, String oper,
			String res, String[] values, int n) {
		String value = parser.getNSM().toPrefix(values[n], true);
		Term t = makeTerm(parser, oper, res, value);
		if (n == values.length - 1) {
			return t;
		} else {
			return new Term(bool, t, makeBoolTerm(parser, bool, oper, res,
					values, n + 1));
		}
	}
	
	/**
	 * Translate this exp triple as a Term
	 * @return
	 */
	Expression toTerm() {
		return exp;
	}
	
	boolean isString(String str) {
		if ((str.startsWith("\"") && str.endsWith("\""))
				|| (str.startsWith("'") && str.endsWith("'")))
			return true;
		else
			return false;
	}
	
	
	/***************************************************************************
	 *
	 * 2. Semantics
	 * expand prefix to uri namespace
	 * expand path and generate OR
	 */
	Exp validate(Parser parser){
		if (isall) {
			parser.setAll(true);
		}
		if (predicate != null) istype = parser.isDefType(predicate);
		Exp stmt=this;
		//processIS();
		if (isExp()){
			//exp=defExpression(parser);
		}
		else {
			stmt=defRelation(parser);
		}
		return stmt;
	}
	
	
	
	/**
	 * 2. Semantics after validate
	 * Generate target expression or relation, expand path
	 * @param parser
	 * @return
	 */
	Exp defExpression(Parser parser) {
		return this;
	}
	
	/**
	 * Return an  expression from this triple
	 * If property[2] generate (or (x p y) (x p z p y))
	 * isliteral = true if value between "" or has datatype or
	 * range is Literal
	 */
	Exp defRelation(Parser parser){
		boolean literalp= false;
		// to accept 12 as a (datatype) literal value, need to check range
		// i.e. 12 vs "12"
		Exp exp;
		if (isset){
			if (id == -1 ) id=nextID();//parser.getTripleId();
			exp= defSet(parser);
		}
		else if (star > 1){
			exp = defPath(parser, literalp);
		}
		else {
			exp=this;
		}
		return exp;
	}
	
	Exp defPath(Parser parser, boolean literalp) {
		if (id == -1)
			id = nextID();//parser.getTripleId();
		Exp filter = toVariable(); // replace cst by var
		Exp exp = new Or(), tmp;
		for (int i = 1; i <= star; i++) {
			tmp = defPath(parser, i, literalp);
			exp.add(new And(tmp));
		}
		if (filter != null) {
			exp = new And(exp);
			exp.add(filter);
		}
		return exp;
	}
	

	
	/**
	 * From ?x p[k] ?y Generate  one path of length n   ?x p ?v1 ?v1 p ?y ...
	 * @param n
	 * @param literalp
	 * @return
	 */
	Exp defPath(Parser parser, int n, boolean literalp){
		And star=new And();
		Triple triple;
		String prev=subject.getName(), //tresource,
		var=prev;// tresource
		Variable vprev=new Variable(prev), vvar=new Variable(var);
		for (int i=1; i<n; i++){
			var=genVar(i);
			vvar=new Variable(var);
			triple = create(vprev, predicate, variable, vvar);
			prev=var;
			vprev=vvar;
			triple.source=source;
			triple.score=score;
			triple.istype = istype;
			star.add(triple);
		}
		triple = create(vvar, predicate, variable, object);
		triple.source=source;
		triple.score=score;
		triple.istype = istype;
		star.add(triple);
		return star;
	}
	
	/**
	 * ?x rdfs:label ?l@en
	 * ?x rdfs:label toto@en
	 *
	 */
	Exp defLang(){
		Exp exp=new And();
		exp.add(this);
		
		
		return exp;
	}
	
	/**
	 * ?x relation{n} ?y
	 * create all paths of length 1 to n
	 * return an OR of these paths
	 */
	Exp defSet(Parser parser) {
		Exp set = new Or();
		Exp filter = toVariable();
		set = set(parser, set, getStar());
		if (filter != null) {
			set = new And(set);
			set.add(filter);
		}
		return set;
	}
	
	/**
	 * Replace constant by variable :
	 */
	Exp toVariable() {
		Triple tfilter1 = null, tfilter2 = null;
		if (subject.isConstant()) {
			// generate a variable and a filter variable = constant
			tfilter1 = toVariable(true);
		}
		if (object.isConstant()) {
			tfilter2 = toVariable(false);
		}
		if (tfilter1 != null && tfilter2 != null){
			And and = new And(tfilter1);
			and.add(tfilter2);
			return and;
		}
		else if (tfilter1 != null)
			return tfilter1;
		else return tfilter2;
	}
	
	/**
	 *
	 * Replace constant arg by variable arg
	 *    */
	Triple toVariable(boolean first) {
		String name = ExpParser.SYSVAR + id + ((first)?"_1":"_2");
		Variable var = new Variable(name);
		Term filter = new Term("=", var, (first)?subject:object);
		toVariable(var, first);
		Triple tfilter =   Triple.create(filter);
		//tfilter.setID(parser.getTripleId());
		return tfilter;
	}
	
	public static Triple fake(){
		int id = nextID();
		Triple t =  Triple.create(
				new Variable(ExpParser.SYSVAR + "fa"+id), 
				Constant.createResource(RDFS.RootPropertyURI), 
				new Variable(ExpParser.SYSVAR + "fb"+id));
		t.setFake(true);
		//t.setID(id);
		return t;
	}
	

	void toVariable(Variable var, boolean first) {
		if (first) {
			subject = var;
		}
		else {
			object = var;
		}
	}
	
	
	
	/**
	 * all paths from 1 to n
	 */
	Exp set(Parser parser, Exp set, int n){
		for (int i=1; i<=n; i++){
			set=set(parser, set, new And(), 0, i);
		}
		return set;
	}
	
	/**
	 * create all paths of same size n
	 * generate 2^n combinaison :
	 * ?x R ?v1 ?v1 R ?y
	 * ?x R ?v1 ?y R ?v1
	 * ?v1 R ?x ?v1 R ?y
	 * ?v1 R ?x ?y R ?v1
	 * set = set of paths already created
	 * cur = current path of length i {and a1 .. ai }
	 * add ai+1 and inv(ai+1) into current path
	 * complete recursively untill n
	 * return set
	 */
	Exp set(Parser parser, Exp set, Exp cur, int i, int n){
		if (i==n){
			set.add(cur);
			return set;
		}
		Triple triple=copy(parser, i, n, false);
		Exp tmp=cur.copy();
		tmp.add(triple);
		set=set(parser, set, tmp, i+1, n);
		triple=copy(parser, i, n, true);
		cur.add(triple);
		set=set(parser, set, cur, i+1, n);
		return set;
	}
	

	
	Triple copy(Parser parser, int i, int n, boolean swap){
		String x=getVar(i, n);
		String y=getVar(i+1, n);
		Variable vx=new Variable(x),
		vy=new Variable(y);
		Triple triple;
		if (swap){
			triple = create(vy, predicate, variable, vx);
	}
		else {
			triple = create(vx, predicate, variable, vy);
		}
		// when {1} do not complete both args :
		if (i==0)   complete(triple, true, swap);
		if (i + 1 == n)
			complete(triple, false, swap);
		triple.source=source;
		triple.score=score;
		triple.istype = istype;
		return triple;
	}
	
	/**
	 * triple is a copy of this
	 * set count, join, distinct
	 * if res : copy res (else value)
	 * if swap : swap to value (else res)
	 */
	Triple complete(Triple triple, boolean res, boolean swap){
		return triple;
	}
	
	
	/**
	 * Util functions
	 * @return
	 */
	
	
	public String getResource(){
		return subject.getName();
	}

	public String getValue(){
		return object.getName();
	}
	
	public String getSourceName(){
		if (source == null) return null;
		return source.getName();
	}
	
	public Atom getSourceExp(String name){
		if (name == null) return null;
		Atom at;
		if (Triple.isVariable(name)){ 
			Variable var = new Variable(name);
			if (Variable.isBlankVariable(name)){
				var.setBlankNode(true);
			}
			at = var;
		}
		else {
			at = Constant.createResource(name);
		}
		return at;
	}
	
	public Atom getSource(){
		return source;
	}
	
	public void setScore(Vector<String> names){
		score = names;
	}
	
	public Vector<String> getScore(){
		return score;
	}
	
	/**
	 * An outermost source does not overwrite local source
	 */
	public void setSource(String src) {
		if (source == null && ! isexp){
			setVSource(src);
		}
	}
	
	public void setVSource(String src) {
		source = getSourceExp(src);
	}
	
	
	public void setVSource(Atom at) {
		source = at;
	}
	
	
	/**
	 * FROM uri
	 * generates a source ?src variable
	 * side effect : complete vars, the list of source var
	 */
	void setFromSource(Parser parser, String name, Vector<String> vars, Vector<String> from, boolean generate) {
		if (! (source == null && ! isexp)) return;
		// rdf:type triple has no source
		// use case: with from file, it may generate a filter ?from = file
		// but with no relation to carry the filter
		if (istype && getExp(1).isConstant()) return;
		// use case in kgram:
		// from <uri> where {?x p* ?y}
		// from <uri> is managed directly by kgram, not by ?src = <uri>
		// hence do not generate a source variable and a filter for this path edge
		if (isPath() && parser.isKgram()) return;
		if (generate){
			name = name + vars.size();
		}
		if (! vars.contains(name)) vars.add(name);
		setSource(name);
	}
	
	/**
	 * Process graph/state ?src triple
	 * when state, generate a new ?si variable and  store it in vars
	 */
	void setSource(Parser parser,  Env env, String src, boolean isState) {
		if (! (source == null && ! isexp)) return;
		if (env.state){
			if (isState) {
				// generate a state variable
				String name = parser.newVar(env.name); // name + vars.size();
				env.vars.add(name);
				env.states.add(src);
				setSource(name);
			}
		}
		else if (! isState){
			setSource(src);
		}
	}
	
	/**
	 * Expand qnames
	 */
	void expand(NSManager nsm){
		for (int i=0; i<getArity(); i++){
			Atom node = getArg(i);
			if (node != null && node.isResource()){
				String uri = nsm.toNamespace(node.getName());
				setArg(i, Constant.create(uri));
			}
		}
	}
	
	public void setRec(boolean b){
		  isRec = b;
	  }
	
	public boolean isRec(){
		return isRec;
	}
	
	
	/**
	 * FROM NAMED uri
	 * collect source ?src variables
	 * side effect : complete vars, the list of source var
	 */
	void collectSource(Parser parser, Vector<String> vars, Vector<String> named) {
		if (source == null ||  isexp) return;
		if (source.isVariable() && ! vars.contains(source.getName()))
			vars.add(source.getName());
	}
	
	public Atom getExp(int i){
		switch (i){
		case 0: return getSubject();
		case 1: return getObject();
		case 2: return getVariable();
		case 3: return getSource();
		default: return null;
		}
	}
	
	public Atom getArg(int i){
		switch (i){
		case 2:  return getProperty();
		default: return getExp(i);
		}
	}
	

	
	public boolean similar(Triple t2){
		for (int i=0; i<getArity(); i++){
			if (getArg(i) == null || t2.getArg(i) == null)
				if (getArg(i) != t2.getArg(i)) 
					return false;
			if (! getArg(i).getName().equals(t2.getArg(i).getName()))
				return false;
		}
		return true;
	}
	
	public void setArg(int i, Atom exp){
		setExp(i, exp);
	}
	
	public void setExp(int i, Atom exp){
		switch (i){
		case 0: setSubject(exp);break;
		case 1: setObject(exp);break;
		case 2: 
			if (exp.isVariable()){
				setVariable((Variable)exp);
			}
			else {
				setProperty((Constant)exp);
			}
			break;
		case 3: source = (Atom) exp;break;
		}
	}
	
	public int getArity(){
		return 4;
	}
	
	public Vector<Expression> getVexp(){
		return vexp;
	}
	
	
	public void setExp(Expression e) {
		exp = e;
	}
	
	public void setSubject(Atom e1) {
		subject = e1;
	}
	
	public void setObject(Atom e2) {
		object = e2;
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public Expression getRegex() {
		return regex;
	}
	
	public void setRegex(Expression exp) {
		regex = exp;
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String m) {
		mode = m;
	}
	
	public boolean isExpression() {
		return isexp;
	}
	
	public String getDatatype() {
		return object.getDatatype();
	}
	
	public String getLang() {
		String l = object.getLang();
		if (l == null) return "";
		else return l;
	}
	
	public int getStar() {
		return star;
	}
	
	public void setStar(int s) {
		star = s;
	}
	
	public boolean getResp() {
		return ! object.isLiteral();
		//boolean b = (object.isConstant() && ((Constant) object).isLiteral());
//		||
//		// is it a variable with a datatype :
//		(object.isVariable() && ((Variable) object).getDatatype() != null);
		//return ! b;
	}
	
	
	boolean isNamespace() {
		return (subject.getName().equalsIgnoreCase(PREFIX) || 
				subject.getName().equals(BASE) );
	}
	
	/**
	 * Variable for ith node in the path
	 * 0 and n are the variables of the genuine query (?x and ?y)
	 * others are generated : ?v_i
	 */
	String getVar(int i, int n) {
		if (i == 0)
			return subject.getName() ;
		else if (i == n)
			return object.getName();
		else
			return genVar(i);
	}
	
	String genVar(int i) {
		return "?v" + id + "_" + i;
	}
	
	public static boolean isVariable(String str) {
		return (str.indexOf(KeywordPP.VAR1) == 0 || 
				str.indexOf(KeywordPP.VAR2) == 0 );
	}
	
	public static boolean isQName(String str) {
		return str.toLowerCase().matches("[a-z]*:[a-z_]*");
	}
	
	public static boolean isABaseWord(String str) {
		return (!isVariable(str) && !isQName(str) && 
				!(str.startsWith(KeywordPP.OPEN) && 
				  str.endsWith(KeywordPP.CLOSE)));
	}
	

	public boolean isType() {
		return istype;
	}
	
	public void setType(boolean b) {
		istype = b;
	}
	
	/*
	 In case of select properties, generate a variable or return existing var
	 (i.e. pre()) if any and add $option
	 */
	String getVariable(Parser parser, boolean select, String value) {
		String str = getPre();
		if (str == null)
			str = parser.getVar(value); //exp2.getName());
		return str;
	}
	
	String getPre() {
		return pre;
	}
	
	/*
	 (should) Return the root type name (tonamespace) of the type selector of this triple
	 this triple value = selector name, e.g. doc
	 could have doc.root=Data in the req parameters, hence could return Data
	 instead of rdfs:Resource
	 */
	String getDefaultType(NSManager nsm) {
		return RDFS.RDFSRESOURCE;
	}
	
	/**
	 *
	 * @param str
	 * @return
	 */
	String clean(String str) {
		return str;
	}
	
	public String toSparql() {
		return toSparql(null);
	}
	
	public String toSparql(NSManager nsm) {
		
		// filter
		if (isExpression()) {
			if (exp == null) return "";
			boolean isAtom = (exp.isAtom());
			String str = KeywordPP.FILTER + KeywordPP.SPACE;
			if (isAtom) str += "(";
			str += exp.toSparql();
			if (isAtom) str += ")";
			str += KeywordPP.SPACE;
			return str;
		}
		String s = "";
		String r;
		String p;
		String v;
		
		if (source != null){
			s = source.toSparql() + " ";
		}
		
		// resource
		r = subject.toSparql();
		
		// property
		p = predicate.getName();
		// if we have something like <Engineer> (because there is a base), add < and >
		//if (!isVariable(p) && !isUri(p) && !(p.startsWith(PrettyPrintCst.OPEN) && p.endsWith(PrettyPrintCst.CLOSE)))
		if (isABaseWord(p))
			p = KeywordPP.OPEN + p + KeywordPP.CLOSE;
		if (variable != null) {
			if (p.equals(getRootPropertyURI()) || p.equals(getRootPropertyQN()))
				p = variable.getName();
			else
				p += "::" + variable.getName();
		}
		if (isone)
			p = "one::" + p;
		if (isdirect)
			p = "direct::" + p;
		if (isall)
			p = "all::" + p;
		if (star >= 1) {
			if (isset)
				p += KeywordPP.OPEN_BRACKET + star + KeywordPP.CLOSE_BRACKET;
			else
				p += KeywordPP.OPEN_SQUARE_BRACKET + star + KeywordPP.CLOSE_SQUARE_BRACKET;
		}
		
		// value
		v = object.toSparql();
		
		// tuple?
		if (vexp != null) {
			String str = KeywordPP.TUPLE + KeywordPP.OPEN_PARENTHESIS + r + KeywordPP.SPACE + p + KeywordPP.SPACE + v + KeywordPP.SPACE;
			for (Expression e : vexp) {
				str += e.toSparql() + KeywordPP.SPACE;
			}
			str += KeywordPP.CLOSE_PARENTHESIS + KeywordPP.DOT;
			return str;
		}
		
		return s + r + KeywordPP.SPACE + p + KeywordPP.SPACE + v + KeywordPP.DOT;
	}
	
	public String toString() {
		return toSparql(null);
	}
	
	public String toString(NSManager nsm) {
		return toSparql(nsm);
		
	}
	
	public void setExp(boolean b) {
		isexp = b;
	}
	
	/**
	 * This triple will generate a relation in the graph
	 * not an exp, not an rdf:type with a constant value
	 */
	public boolean isRelation() {
		if (istype) {
			return (object.isVariable());
		} else
			return !isexp;
	}
	
	public boolean isExp() {
		return isexp;
	}
	
	public boolean isFilter(){
		return isexp;
	}
	
	/**
	 * Does this exp refer one option variable (a var that is only referenced
	 * in an option)
	 * stdVar is the list of non optional variables (std var)
	 */
	public boolean isOptionVar(Vector<String> stdVar) {
		if (exp == null)
			return false;
		return exp.isOptionVar(stdVar);
	}
	
	void bind(Parser parser) {
		for (int i=0; i<getArity(); i++){
			Atom arg = getExp(i);
			if (arg != null && arg.isVariable()){
				parser.bind(arg.getName());
			}
		}
	}
	
	Bind validate(Bind global, int n){
		Bind env = new Bind();
		if (isExp()){
			return exp.validate(env);
		}
		for (int i=0; i<getArity(); i++){
			Atom arg = getExp(i);
			if (arg != null && arg.isVariable()){
				env.bind(arg.getName());
			}
		}
		return env;
	}
	
	/**
	 * Does triple bind this variable ?
	 */
	public boolean bind(Variable var){
		if (isExp()) return false;
		String name = var.getName();
		for (int i=0; i<getArity(); i++){
			Expression arg = getExp(i);
			if (arg != null && arg.isVariable() && arg.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isOption() {
		return isoption;
	}
	
	public void setLastOptionID(int num) {
		lastOptionID = num;
	}
	
	public int getLastOptionID() {
		return lastOptionID;
	}
	
	public void setFirstOptionID(int num) {
		firstOptionID = num;
	}
	
	public int getFirstOptionID() {
		return firstOptionID;
	}
	
	public int getID() {
		return id;
	}
	
	Triple lastTriple() {
		return this;
	}
	
	public void setTOption(boolean b) {
		isoption = b;
		isFirst = b;
		isLast = b;
		firstOptionID = id;
		lastOptionID = id;
	}
	
	public void setOption(boolean b) {
		isoption = b;
	}
	
	public void setFake(boolean b) {
		fake = b;
	}
	
	public boolean isFake() {
		return fake;
	}
	
	public void setFirst(boolean b) {
		isFirst = b;
	}
	
	public void setLast(boolean b) {
		isLast = b;
	}
	
	public boolean isFirst() {
		return isFirst;
	}
	
	public boolean isLast() {
		return isLast;
	}
	
	public void setVariable(String var) {
		variable = new Variable(var);
	}
	
	public void setVariable(Variable var) {
		variable = var;
	}
	
	public boolean isDirect() {
		return isdirect;
	}
	
	public boolean isOne() {
		return isone;
	}
	
	public boolean isPath(){
		return variable != null && variable.isPath();
	}
	
	public boolean isXPath(){
		if (regex == null) return false;
		return regex.getName().equals(Term.XPATH);
	}
	
	public Expression getXPath(){
		return Term.function(Term.XPATH, subject, regex.getArg(0));
	}
	
	
	void setOne(boolean b) {
		isone = b;
	}

	
	/**
	 * @return  a variable on which we attach the evaluable expression
	 */
	public String getExpVariable() {
		if (exp == null)
			return null;
		Variable var = exp.getVariable();
		if (var != null)
			return var.getName();
		else
			return null;
	}
	
	public String getVariableName() {
		if (variable == null) return null;
		return variable.getName();
	}
	
	public Atom getSubject(){
		return subject;
	}
	
	public Atom getPredicate(){
		if (variable != null)
			return variable;
		else return predicate;
	}
	
	public Variable getVariable(){
		return variable;
	}
	
	public Constant getProperty(){
		return predicate;
	}
	
	public void setProperty(Constant prop){
		predicate = prop;
	}
	
	public Atom getObject(){
		return object;
	}
	
	
	
	public void setAll(boolean b) {
		isall = b;
	}
	
	public void setDirect(boolean b) {
		isdirect = b;
	}
	
	public void setIsset(boolean b) {
		isset = b;
	}
	
	public void setPath(int i) {
		star = i;
	}
	
	public void setRVar(String s) {
		rvar = s;
	}
	
	public void setVVar(String s) {
		pre = s;
	}
	
	private static String getRootPropertyQN() {
		return ASTQuery.getRootPropertyQN();
	}
	
	private String getRootPropertyURI() {
		return ASTQuery.getRootPropertyURI();
	}
	
	
	public boolean validateData(){
		if (subject.isSimpleVariable() || object.isSimpleVariable() || variable!=null){
			return false;
		}
		return true;
	}
	
	public boolean validateDelete(){
		if (subject.isBlankNode()){
			return false;
		}
		if (object.isBlankNode()){
			return false;
		}
		return true;
	}
	
}
