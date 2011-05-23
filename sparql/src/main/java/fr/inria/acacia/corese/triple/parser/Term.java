package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.edelweiss.kgram.api.core.ExpPattern;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Term extends Expression {
	static final String STNOT = Keyword.STNOT;
	static final String SENOT = Keyword.SENOT;
	public static final String SEINV = "i";
	public static final String SREV = Keyword.SBE;
	public static final String SEAND = Keyword.SEAND;
	static final String SEDIV = Keyword.SDIV;
	static final String SEOR  = Keyword.SEOR;
	public static final String SEQ   = Keyword.SEQ;
	public static final String STAR = "star";
	public static final String TEST = "test";

	static final String OPT = "opt";
	static final String PLUS = "plus";
	static final String XPATH = "xpath";
	static final String DIFFER  = "differ";
	static final String ISDIFFER  = "isDifferent";
	static final String SIM = "similarity";
	static final String SCORE = "score";
	static final String SBOUND = "bound";
	static final String EXIST = "exist";

	Processor proc;
	Exp exist;
	
	ArrayList<Expression> args=new ArrayList<Expression>();
	// additional system arg:
	Expr exp;
	boolean isFunction = false,
	isPlus = false;
	boolean isSystem = false;
	boolean isDistinct = false;
	String longName, modality;
	
	public Term() {
	}
	
	public Term(String name){
		setName(name);
	}
	
	public Term(String name, Expression exp1){
		setName(name);
		args.add(exp1);
	}
	
	public Term(String name, Expression exp1, Expression exp2){
		setName(name);
		args.add(exp1);
		args.add(exp2);
	}
	
	public static Term create(String name, Expression exp1, Expression exp2){
		return new Term(name, exp1, exp2);
	}
	
	public static Term create(String name, Expression exp1){
		return new Term(name, exp1);
	}
	
	public static Term create(String name){
		return new Term(name);
	}
	
	public static Term function(String name){
		Term fun = new Term(name); 
		fun.isFunction = true;
		return fun;
	}
	
	public static Term function(String name, Expression exp){
		Term t = function(name);
		t.args.add(exp);
		return t;
	}
	
	public static Term function(String name, Expression exp1, Expression exp2){
		Term t = function(name, exp1);
		t.args.add(exp2);
		return t;
	}
	
	public static Term function(String name, Expression exp1, Expression exp2, Expression exp3){
		Term t = function(name, exp1, exp2);
		t.args.add(exp3);
		return t;
	}
	
	public String getLabel() {
		if (longName!=null) return longName;
		return name;
	}
	
	public String getLongName(){
		return longName;
	}
	
	public void setLongName(String name){
		 longName = name;
	}
	
	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
	public boolean isDistinct(){
		return isDistinct;
	}
	
	public void setModality(String s){
		modality = s;
	}
	
	public String getModality(){
		return modality;
	}

	public static Term negation(Expression exp){
		return new Term(SENOT, exp);
	}
	
	public boolean isTerm(){
		return true;
	}
	
	public void setName(String name){
		super.setName(name);
	}
	
	public String toSparql() {
//		if (args.size() == 0) {
//			return "no arg";
//		} else 
			if (getName() == null) {
			return "no name";
		} else {
			String str = "";
			boolean isope = true;
			int n = args.size();
			int start = 0;
			if (isNegation(getName())){
				str = "(" + SENOT;
				n = 1;
			} else if (isFunction()){
				str += getName();
				start = 0;
				isope = false;
			}
			str += "(";
			String fst;
			for (int i=start; i < n; i++){
				fst = getArg(i).toSparql();
				//System.out.println("Term.java - fst: "+fst);
				if (fst.startsWith("'") || fst.startsWith("\""))
					str += KeywordPP.SPACE + fst;
				else
					str += fst;
				if (i < n - 1) {
					//str += ((isope) ? PrettyPrintCst.SPACE + getName() + PrettyPrintCst.SPACE : "");
					if (isope) {
						if (getName().matches("\\$.*")) {
							// we are in the case filter(?o) -> filter(?o $istrue ?o)
							// we stop the loop
							i = n;
						} else {
							str += KeywordPP.SPACE + getName() + KeywordPP.SPACE;
						}
					}
				}
				if (!isope && i < (n-1)) str += KeywordPP.COMMA;
			}
			if (isNegation(getName())) {
				str += ")";
			}
			str +=")";
			return str;
			//return "Term";
		}
	}
	
	public String toString(){
		boolean isope = true;
//		if (args.size() == 0) {
//			return "no arg";
//		}
//		else 
			if (getName() == null) {
			return "no name";
		}
		else {
			String str = "";
			int n = args.size();
			int start = 0;
			if (isNegation(getName())){
				str = SENOT + " " ;
				n = 1;
			}
			else if (isFunction()){
				str += getName() ;
				start = 0;
				isope = false;
			}
			str += "(";
			String fst;
			for (int i=start; i < n; i++){
				if (getArg(i) != null) {
					fst = getArg(i).toString();
					if (fst.startsWith("'") || fst.startsWith("\""))
						str += " " ;
					str += fst;
					if (i < n - 1)
						str += " " + ((isope) ? getName() : "") + " " ;
				}
			}
			if (getPattern() != null){
				str += getPattern();
			}
			str +=" )";
			return str;
		}
	}
	
	
	static boolean isNegation(String name) {
		return (name.equals(STNOT) || name.equals(SENOT));
	}
	
	
	public Variable getVariable(){
		Variable var;
		for (int i = 0; i < args.size(); i++) {
			var = getArg(i).getVariable();
			if (var != null) return var;
		}
		return null;
	}
	
	
	
	void validate(Parser parser){
		getArg(0).validate(parser);
		getArg(1).validate(parser);
	}
	
	Bind validate(Bind env){
		for (Expression exp : getArgs()){
			exp.validate(env);
		}
		return env;
	}
	
	public boolean isSeq(){
		return getName().equals(SEAND) || getName().equals(SEDIV);
	}
	
	public boolean isAnd(){
		return getName().equals(SEAND);
	}
	
	public boolean isOr(){
		return getName().equals(SEOR);
	}
	
	public boolean isNot(){
		return getName().equals(SENOT);
	}
	
	public boolean isInverse(){
		return getName().equals(SEINV) || super.isInverse() ;
	}
	
	public boolean isReverse(){
		return getName().equals(SREV) || super.isReverse();
	}
	
	public boolean isStar(){
		return isFunction(STAR);
	}
	
	public boolean isOpt(){
		return isFunction(OPT);
	}
	
	public boolean isTest(){
		return isFunction(TEST);
	}
	
	// final state in regexp
	public boolean isFinal(){
		if (isStar() || isOpt()) return true;
		if (isAnd() || isOr()){
			for (Expression exp : args){
				if (! exp.isFinal()) return false;
			}
			return true;
		}
		return false;
	}
	
	
	/**
	 * Return a copy of  the reverse regex :
	 * p/q 
	 * ->
	 * q/p
	 * 
	 * use case: ?x exp <a>
	 * walk the exp from <a> to ?x 
	 * and set index = 1
	 * 
	 */
	public Expression reverse(){
		Term term = this;
		if (isSeq()){
			term = Term.create(SEDIV, getArg(1).reverse(), getArg(0).reverse());
		}
		else if (isOr() || isFunction()){
			if (isOr()){
				term = Term.create(SEOR);
			}
			else {
				term = Term.function(getName());
			}
			for (Expression arg : getArgs()){
				term.add(arg.reverse());
			}
			
			term.copy(this);
		}
		return term;
	}
	
	void copy(Term t){
		setMax(t.getMax());
		setMin(t.getMin());
		setPlus(t.isPlus());
		setExpr(t.getExpr());
	}
	
	
	
	/**
	 * ^(p/q) -> ^q/^p
	 * 
	 *  and translate()
	 *  
	 *  inside reverse, properties (and ! prop)  are setReverse(true)
	 */
	public Expression transform(boolean isReverse){
		Term term = this;
		Expression exp;
		boolean trace = !true;		
		if (trace) System.out.println("** T: " + this + " " + isFunction());
		
		if (isNotOrReverse()){
			if (trace) System.out.println("** T1: " + this );
			exp = translate();
			exp = exp.transform(isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}

		if (isReverse()){
			if (trace) System.out.println("** T2: " + this );
			// Constant redefine transform()
			exp = getArg(0).transform(! isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}
		else if (isReverse && isSeq()){
			if (trace) System.out.println("** T3: " + this );
			term = Term.create(getName(), getArg(1).transform(isReverse), getArg(0).transform(isReverse));
		}
		else { 
			if (trace) System.out.println("** T4: " + this );
			if (isFunction()){
				term = Term.function(getName());
			}
			else {
				term = Term.create(getName());
			}
			for (Expression arg : getArgs()){
				term.add(arg.transform(isReverse));
			}
			if (isNot()){
				term.setReverse(isReverse);
			}
			term.copy(this);
		}
		if (trace) System.out.println("** T5: " + this );
		term.setretype(term.getretype());
		return term;
	}
	

	/**
	 * this term is one of:
	 * ! (^ p) -> ^ !(p)
	 * ! (p | ^q) -> (!p) | ^ (!q)
	 */
	public Expression translate(){
		Expression exp = getArg(0);
		
		if (exp.isReverse()){
			Expression e1 = Term.negation(exp.getArg(0));
			Expression e2 = Term.function(SREV, e1);
			return e2;
		}
		
		if (exp.isOr()){
			Expression std = null, rev = null;
			for (int i=0; i<exp.getArity(); i++){
				Expression ee = exp.getArg(i);
				if (ee.isReverse()){
					rev = add(SEOR, rev, Term.negation(ee.getArg(0)));
				}
				else {
					std = add(SEOR, std, Term.negation(ee));
				}
			}
			Expression res = null;
			if (std != null){
				res = std;
			}
			if (rev != null){
				res = add(SEOR, res, Term.function(SREV, rev));
			}
			return res;
		}
		
		return this;
	}
	
	
	/**
	 * ! (p1 | ^p2)
	 */
	public boolean isNotOrReverse(){
		if (! isNot()) return false;
		Expression ee = getArg(0);
		if (ee.isReverse()) return true;
		if (ee.isOr()){
			for (int i = 0; i<ee.getArity(); i++){
				if (ee.getArg(i).isReverse()){
					return true;
				}
			}
		}
		return false;
	}
	
	
	Expression add (String ope, Expression e1, Expression e2){
		if (e1 == null){
			return e2;
		}
		else {
			return Term.create(ope, e1, e2);
		}
	}
	
	/**
	 * Length of shortest path that matches the regexp
	 */
	public int regLength(){
		if (isStar())
			return 0; //getArg(0).length();
		else return length();
	}
	
	public int length(){
		if (isSeq()){
			return getArg(0).length() + getArg(1).length();
		}
		else if (isOr()){
			return Math.min(getArg(0).length(), getArg(1).length());
		}
		else if (isNot()){
			return 1;
		}
		else return 0;
	}
	
	
	
	public boolean isPlus(){
		return isPlus;
	}
	
	void setPlus(boolean b){
		isPlus = b;
	}
	
	public boolean isTerm(String oper){
		return name.equals(oper);
	}
	
	public boolean isFunction(){
		return isFunction;
	}
	
	public boolean isFunction(String str){
		return isFunction &&  getName().equals(str);
	}
	
	public boolean isType(ASTQuery ast, int type) {
		return isType(ast, null, type);
	}
	
	/**
	 * 1. Is the exp of type aggregate or bound ?
	 * 2. When var!=null: if exp contains var return false (sem checking)
	 */
	public boolean isType(ASTQuery ast, Variable var, int type) {
		if (isFunction()) {
			if (isType(getName(), type))
				return true;
		}
		else if (getName().equals(SEOR)){
			// if type is BOUND : return true
			if (isType(getName(), type))
				return true;
		}
		for (Expression arg : getArgs()) {
			if (var != null && arg == var && type == BOUND){
				// it is not bound() hence we return false
				return false;
			}
			if (arg.isType(ast, type)){
				return true;
			}
		}
		return false;
	}
	
	boolean isType(String name, int type){
		switch (type) {
		case Expression.ENDFILTER :
			return name.equalsIgnoreCase(SIM) || name.equalsIgnoreCase(SCORE);
		case Expression.POSFILTER :
			return isAggregate(name);
		case Expression.BOUND :
			// see compiler
			return name.equalsIgnoreCase(SBOUND) || name.equals(SEOR);
		}
		return false;
	}
	
	public  boolean isAggregate(String name){
		for (String n : Keyword.aggregate){
			if (n.equalsIgnoreCase(name)){ 
				return true;
			}
		}
		return false;
	}
	
	public boolean isRecAggregate(){
		if (isAggregate(name)){
			return true;
		}
		for (Expr exp : getExpList()){
			if (exp.isRecAggregate()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isAggregate(){
		return isAggregate(name);
	}
	
	public boolean isFunctional() {
		return isFunction() && 
		(name.equals(Processor.UNNEST) || 
		name.equals(Processor.SQL) || 
		name.equals(Processor.XPATH) ||
		name.equals(Processor.KGRAM) ||
		name.equals(Processor.EXTERN)) ;
	}
	
	public boolean isBound(){
		if (isFunction()) {
			return getName().equalsIgnoreCase(TermParser.BOUND);   
		} 
		else for (int i = 0; i < getArity(); i++) {
			if (getArg(i).isBound())
				return true;
		}
		return false;
	}
	
	
	public Variable getOptionVar(Vector<String> stdVar) {
		for (int i = 0; i < getArity(); i++) {
			Variable var = getArg(i).getOptionVar(stdVar);
			if (var != null) return var;
		}
		return null;
	}
	
	
	
	public  int getArity(){
		return args.size();
	}
	
	public ArrayList<Expression> getArgs(){
		return args;
	}
	
	public void add(Expression exp) {
		args.add(exp);
	}
	
	public void setArg(int i, Expression exp){
		args.set(i, exp);
	}
	
	public Expression getArg(int n){
		if (n > args.size() - 1)
			return null;
		return args.get(n);
	}

	public String getOper(){
		return getName();
	}
	
	public void setOper(String str){
		setName(str);
	}
	
	/**
	 * x = v1 || x = v2 || x = v3
	 */
	public boolean isOrVarEqCst(){
		if (! isOr()) return false;
		// take arg 1 because arg 0 may still be an or itself
		if (! getArg(1).isTerm()) return false;
		Term t = (Term) getArg(1);
		if (! t.getArg(0).isVariable()) return false;
		return isOrVarEqCst((Variable) t.getArg(0));
	}

	boolean isOrVarEqCst(Variable var){
		if (isOr()) return getArg(0).isOrVarEqCst(var) && 
			getArg(1).isOrVarEqCst(var);
		else if (isTerm() && getOper().equals(SEQ)){
			return 
				getArg(0).isVariable() && 
				getArg(0).getName().equals(var.getName()) &&
				getArg(1).isConstant();
		}
		return false;
	}
	
	// pragma: only when isOrVarEqCst() = true
	public void getCst(Vector<Constant> vec){
		if (isOr()){
			 getArg(0).getCst(vec);
			 getArg(1).getCst(vec);
		}
		else { 
			vec.add((Constant) getArg(1));
		}
	}
	
	/**
	 * Process get:gui
	 * filter ?x >= get:gui --> ?x >= 12
	 */
	public Expression parseGet(Parser parser){
		// case ?x get:oper ?y
		String str = parser.pget(name);
		if (str != null){
			// str = oper, get its value :
			String value = parser.getExtValue(str);
			if (value == null) return null;
			else setOper(value); // the target operator
		}
		// case ?x >= get:gui
		for (int i = 0; i < getArity(); i++) {
			Expression exp = getArg(i).parseGet(parser);
			if (exp != null && exp.isEget()) setEget(true);
			if (exp == null){ 
				return null;
			}
			else args.set(i, exp);
		}
		return this;
	}
	
	
	/**
	 * ! (!A || !B) -> A && B
	 * !! A -> A
	 * !A || !B -> ! (A && B)
	 */
	public Expression rewrite(){
		if (isTerm(SENOT)) {
			Expression term = getArg(0);
			if (term.isTerm(SEOR) && term.getArg(0).isTerm(SENOT) && term.getArg(1).isTerm(SENOT)) {
				Term aterm =  new Term(SEAND, term.getArg(0).getArg(0), term.getArg(1).getArg(0));
				return aterm;
			}
			else if (term.isTerm(SENOT)){
				return term.getArg(0);
			}
		}
		else if (isTerm(SEOR)) {
			if (getArg(0).isTerm(SENOT) && getArg(1).isTerm(SENOT)) {
				Term aterm =  
					new Term(SENOT, new Term(SEAND, getArg(0).getArg(0), getArg(1).getArg(0)));
				return aterm;
			}
		}
		return this;
	}
	
	
	/**
	 * Translate some terms like :
	 * differ(?x ?y ?z) -> (?x != ?y && ?y != ?z && ?x != ?z)
	 */
	public Expression process(){
		if (isFunction() && 
				(getName().equals(DIFFER) || getName().equals(ISDIFFER))){
			return differ();
		}
		else return this;
	}
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 * Exception: do not rewrite in case of aggregate:
	 * foo(?x) as ?y
	 * sum(?y) as ?z
	 */
	public Expression process(ASTQuery ast){
		if (isAggregate() || (ast.isKgram() && isFunctional())) return this;
		for (int i=0; i<args.size(); i++){
			Expression exp = args.get(i).process(ast);
			if (exp == null) return null;
			args.set(i, exp);
		}
		return this;
	}
	
	public Term differ(){
		if (args.size() >= 2){
			Term res =  diff(args, 0);
			return res;
		}
		else return this;
	}
	
	/**
	 * generate ?x != ?y ?x != ?z ?y != ?z 
	 * from (?x ?y ?z)
	 */
	public Term diff(ArrayList<Expression> vars, int start){
		Term res = null;
		for (int i=start; i<vars.size(); i++){
			for (int j=i+1; j<vars.size(); j++){
				Term tt = 	new Term(Keyword.SNEQ, getArg(i), getArg(j));
				if (res == null) res = tt;
				else res = new Term(Keyword.SEAND, res, tt);
			}
		}
		return res;
	}
	
	
	/**
	 * KGRAM
	 */
	
	// Filter
	public void getVariables(List<String> list) {
		for (Expression ee : getArgs()){
			ee.getVariables(list);
		}
		if (oper() == ExprType.EXIST){
			getPattern().getVariables(list);
		}
	}
	
	
	public Expr getExp(int i){
		return proc.getExp(i);
	}
	
	public Expr getArg(){
		return exp;
	}
	
	public void setArg(Expr e){
		exp = e;
	}
	
	public List<Expr> getExpList(){
		return proc.getExpList();
	}
	
	public ExpPattern getPattern(){
		if (proc == null) return null;
		return proc.getPattern();
	}
	
	public void setPattern(ExpPattern pat){
		proc.setPattern(pat);
	}
	
	void setExist(Exp exp){
		exist = exp;
	}
	
	public Exp getExist(){
		return exist;
	}
	
	// Exp
	
	public void compile(ASTQuery ast){
		if (proc != null) return ;
		
		for (Expression exp : getArgs()){
			exp.compile(ast);
		}
		
		proc = new Processor(this);
		proc.compile(ast);
		
	}

	
	public void compile(){
		compile(null);
	}
	
	public int arity(){
		return proc.arity();
	}
	
	
	public int type(){
		return proc.type();
	}
	
	public int oper(){
		return proc.oper();
	}

	public Processor getProcessor() {
		// TODO Auto-generated method stub
		return proc;
	}
	


}