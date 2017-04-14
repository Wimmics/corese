package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import java.util.ArrayList;
import java.util.List;


import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;
import fr.inria.corese.compiler.java.JavaCompiler;
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
        static final String NL = System.getProperty("line.separator");
	static final String RE_CHECK = "check";
	static final String RE_PARA = "||";
	public static final String RE_ALT = "|";
	public static final String RE_SEQ = "/";
	static final String OCST = "@{";
	static final String CCST = "}";
	static final String SPACE = " ";

	static final String STNOT = Keyword.STNOT;
	static final String SENOT = Keyword.SENOT;
	
	public static final String SEINV = "i";
	public static final String SREV = Keyword.SBE;
	public static final String SNOT = Keyword.SENOT;
	public static final String SEAND = Keyword.SEAND;
	static final String SEDIV = Keyword.SDIV;
	public static final String SEOR  = Keyword.SEOR;
	public static final String SEQ   = Keyword.SEQ;
	public static final String SNEQ   = Keyword.SNEQ;
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
	static final String EXIST = "exists";
	static final String LIST = "inList";
	static final String SERVICE = "service";

        // default processor to compile term
        static Processor processor;
        
        // possibly dynamic processor to implement some functions: regex, ...
	Processor proc;
	Exp exist;
	Constant cname;
	
	ArrayList<Expression> args = new ArrayList<Expression>();
        List<Expr> lExp;

	// additional system arg:
	Expression exp;
	boolean isFunction = false,
	isCount = false,
	isPlus = false;
	boolean isDistinct = false;
	boolean isShort = false;
	String  modality;       
        int type = ExprType.UNDEF, oper = ExprType.UNDEF;
        int min = -1, max = -1;
        private int place = -1;
        
        static {
            processor = new Processor();
        }
	
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
	
	public static Term list(){
		return Term.function(LIST);
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
       	
        @Override
	public String getLabel() {
		if (getLongName()!=null) return getLongName();
		return name;
	}
	
	public void setCName(Constant c){
		cname = c;
	}	
	
	public Constant getCName(){
		return cname;
	}
        
        void setMin(int n){
		min = n;
	}
	
        @Override
	public int getMin(){
		return min;
	}
	
	void setMax(int n){
		max = n;
	}
	
        @Override
	public int getMax(){
		return max;
	}
        
        @Override
        public boolean isCounter(){
		return (min!=-1 || max != -1);
	}
        
        public void setModality(ExpressionList el){
            setDistinct(el.isDistinct());
            setModality(el.getSeparator());
            setArg(el.getExpSeparator());
        }

        @Override
	public void setDistinct(boolean b){
		isDistinct = b;
	}
	
        @Override
	public boolean isDistinct(){
		return isDistinct;
	}
	
        @Override
	public void setShort(boolean b){
		isShort = b;
	}
	
        @Override
	public boolean isShort(){
		return isShort;
	}
	
        @Override
	public void setModality(String s){
		modality = s;
	}
	
        @Override
	public String getModality(){
		return modality;
	}

	public static Term negation(Expression exp){
		return new Term(SENOT, exp);
	}
	
        @Override
	public boolean isTerm(){
		return true;
	}
	
        @Override
	public void setName(String name){
		super.setName(name);
	}
	
        @Override
	public String toRegex() {
		if (isCount()){
			String str = paren(getArg(0).toRegex()) + "{";
			if (getMin() == getMax()){
				str += getMin();
			}
			else {
				//if (getMin()!=0){
					str += getMin();
				//}
				str += ",";
				if (getMax()!= Integer.MAX_VALUE){
					str += getMax();
				}
			}
			str += "}";
			return str;
		}
		else if (isPlus()){
			return paren(getArg(0).toRegex()) + "+";
		}
		else if (isStar()){
			return paren(getArg(0).toRegex()) + "*";
		}
		else if (isNot()){
			return SNOT + paren(getArg(0).toRegex());
		}
		else if (isReverse()){
			return SREV + paren(getArg(0).toRegex());
		}
		else if (isOpt()){
			return paren(getArg(0).toRegex()) + "?";
		}
		else if (isSeq()){
			if (getArg(1).isTest()){
				return toTest();
			}
			else {
				return getArg(0).toRegex() + RE_SEQ + getArg(1).toRegex();
			}
		}
		else if (isAlt()){
			return "(" + getArg(0).toRegex() + RE_ALT + getArg(1).toRegex() +")";
		}
		else if (isPara()){
			return getArg(0).toRegex() + RE_PARA + getArg(1).toRegex();
		}
		return toString();
	}
	
	String toTest(){
		return getArg(0).toRegex() + OCST + KeywordPP.FILTER + SPACE + getArg(1).getExpr() + CCST;
	}

	String paren(String s){
		return "(" + s + ")";
	}
	
        @Override
	public StringBuffer toString(StringBuffer sb) {

		if (getName() == null) {
			return sb;
		} 
		if (getName().equals(Processor.SEQUENCE)){
                    return funSequence(sb);
                }
                                              
		if (getName().equals(EXIST)){
			return funExist(sb);
		}
		boolean isope = true;
		int n = args.size();

		if (isNegation(getName())){
			sb.append(KeywordPP.OPEN_PAREN + SENOT);
			n = 1;
		} 
		else if (isFunction()){
			if (! getName().equals(LIST)){
				if (getCName() != null){
					getCName().toString(sb);
				}
				else {
					sb.append(getName());
				}
			}
			isope = false;
		}

		sb.append(KeywordPP.OPEN_PAREN);
		
		if (isDistinct()){
			// count(distinct ?x)
			sb.append(KeywordPP.DISTINCT);
			sb.append(SPACE);
		}

		for (int i=0; i < n; i++){

			getArg(i).toString(sb);

			if (i < n - 1) {
				if (isope) {						
					sb.append(SPACE + getName() + SPACE);						
				}
				else {
					sb.append(KeywordPP.COMMA);
					sb.append(SPACE);
				}
			}
		}

		if (getModality() != null && getName().equalsIgnoreCase(Processor.GROUPCONCAT)){
			sb.append(Processor.SEPARATOR);
			Constant.toString(getModality(), sb);
		}
		else if (n == 0 && getName().equalsIgnoreCase(Processor.COUNT)) {
			// count(*)
			sb.append(KeywordPP.STAR);
		}
		
		sb.append(KeywordPP.CLOSE_PAREN);

		if (isNegation(getName())) {
			sb.append(KeywordPP.CLOSE_PAREN);
		}
		
		return sb;
	}

    StringBuffer funExist(StringBuffer sb){
        if (isSystem()){
            Exp exp = getExist().get(0).get(0);
            return exp.toString(sb);
        }
        return getExist().toString(sb);
    }
        
    StringBuffer funSequence(StringBuffer sb){
        if (getArgs().size() >= 1){
            getArg(0).toString(sb);
        }
        for (int i = 1; i<getArgs().size(); i++){
            sb.append(";");
            sb.append(NL);
            getArg(i).toString(sb);
        }
        return sb;
    }
    
    public String javaName(){
        return NSManager.nstrip(getName());
    }
    
        @Override
    public void toJava(JavaCompiler jc) {
        jc.toJava(this);
    }     

    static boolean isNegation(String name) {
        return (name.equals(STNOT) || name.equals(SENOT));
    }
			
	
        @Override
	Bind validate(Bind env){
		for (Expression exp : getArgs()){
			exp.validate(env);
		}
		return env;
	}
	
        @Override
	public boolean validate(ASTQuery ast) {
		
		if (isExist()){
			return getExist().validate(ast);
		}
		
		boolean ok = true;
		
		for (Expression exp : getArgs()){
			boolean b = exp.validate(ast);
			ok = ok && b;
		}
		return ok;
	}
	
        @Override
	public boolean isExist(){
		return getExist() != null;
	}
        
        // when it is not compiled !
        public boolean isTermExist(){
            return getName().equals(EXIST);
        }
        
         public boolean isTermExistRec(){
            if (isTermExist()){
                return true; 
            }
            for (Expression exp : getArgs()){
                if (exp.isTerm() && exp.getTerm().isTermExistRec()){
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public boolean isRecExist(){
		if (isExist()){
                    return true;
                }
                for (Expression exp : getArgs()){
                    if (exp.isRecExist()){
                        return true;
                    }
                }
                return false;
	}
	
        @Override
	public boolean isSeq(){
		return getName().equals(RE_SEQ);
	}
	
        @Override
	public boolean isAnd(){
		return getName().equals(SEAND);
	}
	
        @Override
	public boolean isOr(){
		return getName().equals(SEOR);
	}
	
        @Override
	public boolean isAlt(){
		return getName().equals(RE_ALT);
	}
	
        @Override
	public boolean isPara(){
		return getName().equals(RE_PARA);
	}
	
        @Override
	public boolean isNot(){
		return getName().equals(SENOT);
	}
	
	public boolean isPathExp(){
		return getretype() != UNDEF;
	}
	
        @Override
	public boolean isInverse(){
		return getName().equals(SEINV) || super.isInverse() ;
	}
	
        @Override
	public boolean isReverse(){
		return getName().equals(SREV) || super.isReverse();
	}
	
        @Override
	public boolean isStar(){
		return isFunction(STAR);
	}
	
        @Override
	public boolean isOpt(){
		return isFunction(OPT);
	}
	
        @Override
	public boolean isTest(){
		return isFunction(TEST);
	}
	
        @Override
	public boolean isCheck(){
		return isFunction(RE_CHECK);
	}
	
	// final state in regexp
        @Override
	public boolean isFinal(){
		if (isStar() || isOpt()) return true;
		if (isAnd() || isAlt()){
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
        @Override
	public Expression reverse(){
		Term term = this;
		if (isSeq()){
			term = Term.create(RE_SEQ, getArg(1).reverse(), getArg(0).reverse());
		}
		else if (isAlt() || isFunction()){
			if (isAlt()){
				term = Term.create(RE_ALT);
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
               
        
        void getConstants(List<Constant> list) {
            if (isNot()) {
                // ! p is a problem because we do not know the predicate nodes ...
                // let's return top level property, it subsumes all properties
                Constant.rootProperty.getConstants(list);
            } else {
                for (Expression e : getArgs()) {
                    e.getConstants(list);
                }
            }
        }
        
	
	void copy(Term t){
		setMax(t.getMax());
		setMin(t.getMin());
		setPlus(t.isPlus());
		setExpr(t.getExpr());
		setShort(t.isShort());
		setDistinct(t.isDistinct());
	}
	
	
	
	/**
	 * ^(p/q) -> ^q/^p
	 * 
	 *  and translate()
	 *  
	 *  inside reverse, properties (and ! prop)  are setReverse(true)
	 */
        @Override
	public Expression transform(boolean isReverse){
		Term term = this;
		Expression exp;
		boolean trace = !true;		
		
		if (isNotOrReverse()){
			exp = translate();
			exp = exp.transform(isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}

		if (isReverse()){
			// Constant redefine transform()
			exp = getArg(0).transform(! isReverse);
			exp.setretype(exp.getretype());
			return exp;
		}
		else if (isReverse && isSeq() && ! getArg(1).isTest()){
			term = Term.create(getName(), getArg(1).transform(isReverse), 
					getArg(0).transform(isReverse));
		}
		else { 
			if (isFunction()){
				term = Term.function(getName());
			}
			else {
				term = Term.create(getName());
			}
			
			for (Expression arg : getArgs()){
				term.add(arg.transform(isReverse));
			}
			
			switch (getretype()){
			
				case NOT: 
					term.setReverse(isReverse); 
					break;
					
				case PARA: 
				case OPTION:
					// additional argument for checking
					Term t = Term.function(RE_CHECK, term);
					t.setretype(CHECK);
					term.add(t);
					break;
					
					
			}
			
			term.copy(this);
		}
		term.setretype(term.getretype());
		return term;
	}
	

	/**
	 * this term is one of:
	 * ! (^ p) -> ^ !(p)
	 * ! (p | ^q) -> (!p) | ^ (!q)
	 */
        @Override
	public Expression translate(){
		Expression exp = getArg(0);
		
		if (exp.isReverse()){
			Expression e1 = Term.negation(exp.getArg(0));
			Expression e2 = Term.function(SREV, e1);
			return e2;
		}
		
		if (exp.isAlt()){
			Expression std = null, rev = null;
			for (int i=0; i<exp.getArity(); i++){
				Expression ee = exp.getArg(i);
				if (ee.isReverse()){
					rev = add(RE_ALT, rev, Term.negation(ee.getArg(0)));
				}
				else {
					std = add(RE_ALT, std, Term.negation(ee));
				}
			}
			Expression res = null;
			if (std != null){
				res = std;
			}
			if (rev != null){
				res = add(RE_ALT, res, Term.function(SREV, rev));
			}
			return res;
		}
		
		return this;
	}
	
	
	/**
	 * ! (p1 | ^p2)
	 */
        @Override
	public boolean isNotOrReverse(){
		if (! isNot()) return false;
		Expression ee = getArg(0);
		if (ee.isReverse()) return true;
		if (ee.isAlt()){
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
        @Override
	public int regLength(){
		if (isStar())
			return 0; //getArg(0).length();
		else return length();
	}
	
        @Override
	public int length(){
		if (isSeq()){
			return getArg(0).length() + getArg(1).length();
		}
		else if (isAlt()){
			return Math.min(getArg(0).length(), getArg(1).length());
		}
		else if (isNot()){
			return 1;
		}
		else return 0;
	}
	
	
	
        @Override
	public boolean isPlus(){
		return isPlus;
	}
	
	void setPlus(boolean b){
		isPlus = b;
	}
	
	public boolean isCount(){
		return isCount;
	}
	
	void setCount(boolean b){
		isCount = b;
	}
	
        @Override
	public boolean isTerm(String oper){
		return name.equals(oper);
	}
	
        @Override
	public boolean isFunction(){
		return isFunction;
	}
        
        @Override
        public boolean isFuncall(){
		return isFunction;
	}        
        void setFunction(boolean b){
            isFunction = b;
        }
	
        @Override
	public boolean isFunction(String str){
		return isFunction &&  getName().equals(str);
	}
	
        @Override
	public boolean isType(ASTQuery ast, int type) {
		return isType(ast, null, type);
	}
	
	/**
	 * 1. Is the exp of type aggregate or bound ?
	 * 2. When var!=null: if exp contains var return false (sem checking)
	 */
        @Override
	public boolean isType(ASTQuery ast, Variable var, int type) {
		if (isFunction()) {
			if (isType(getName(), type))
				return true;
		}
		else if (isOr()){
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
		for (String n : Processor.aggregate){
			if (n.equalsIgnoreCase(name)){ 
				return true;
			}
		}
		return false;
	}
	
        @Override
	public boolean isRecAggregate(){
		if (isAggregate(getLabel())){
			return true;
		}
		for (Expr exp : getExpList()){
			if (exp.isRecAggregate()){
				return true;
			}
		}
		return false;
	}
	
        @Override
	public boolean isAggregate(){
		return isAggregate(name);
	}
	
        @Override
	public boolean isFunctional() {
		if (! isFunction()){
                    return false;
                }
                String str = getLabel();
		return (str.equals(Processor.UNNEST) || 
                str.equals(Processor.KGUNNEST) || 		
                str.equals(Processor.SQL) || 
		str.equals(XPATH) ||
		//str.equals(Processor.SPARQL) ||
		str.equals(Processor.EXTERN)) ;
	}
	
        @Override
	public boolean isBound(){
		if (isFunction()) {
			return getName().equalsIgnoreCase(Processor.BOUND);   
		} 
		else for (int i = 0; i < getArity(); i++) {
			if (getArg(i).isBound())
				return true;
		}
		return false;
	}
			
	
        @Override
	public  int getArity(){
		return args.size();
	}
        
        void setArgs(ArrayList<Expression> list){
            args = list;
        }
	
        @Override
	public ArrayList<Expression> getArgs(){
		return args;
	}
        	
	public void add(Expression exp) {
		args.add(exp);
	}
        
        public void add(int i, Expression exp) {
		args.add(i, exp);
	}
	
	public void setArg(int i, Expression exp){
		args.set(i, exp);
	}
	
        @Override
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
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 * Exception: do not rewrite in case of aggregate:
	 * foo(?x) as ?y
	 * sum(?y) as ?z
	 */
        @Override
	public Expression process(ASTQuery ast){
		if (isAggregate() || isFunctional()){ //(ast.isKgram() && isFunctional())){
                    return this;
                }
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
        @Override
	public void getVariables(List<String> list, boolean excludeLocal) {
		for (Expression ee : getArgs()){
			ee.getVariables(list, excludeLocal);
		}
		if (oper() == ExprType.EXIST){
			getPattern().getVariables(list, excludeLocal);
		}
	}
        
        // this = xt:fun(?x, ?y)
        List<Variable> getFunVariables(){
            ArrayList<Variable> list = new ArrayList<Variable>();
            for (Expression exp : getArgs()){
                if (exp.isVariable()){
                    list.add(exp.getVariable());
                }
            }
            return list;
        }
	
        @Override
        public String getShortName(){
            if (proc == null || proc.getShortName() == null){
                return getName();
            }
            return proc.getShortName();
        }
	
        @Override
	public Expr getExp(int i){
		return lExp.get(i);
	}
        
        @Override
        public void setExp(int i, Expr e){
            if (i < lExp.size()){
                lExp.set(i, e);
            }
            else if (i == lExp.size()){
                lExp.add(i, e);
            }
        }
        
        @Override
        public void addExp(int i, Expr e){
            lExp.add(i, e);
        }

	
	void setArguments(){
		if (lExp == null){
			lExp = new ArrayList<Expr>();
			for (Expr e : getArgs()){
				lExp.add(e);
			}
		}
	}
	
        @Override
	public int arity(){
		return lExp.size();
	}
	
	
        @Override
	public Expression getArg(){
		return exp;
	}
                      	
	public void setArg(Expression e){
		exp = e;
	}
	
        @Override
	public List<Expr> getExpList(){
		return lExp;
	}
        
        void setExpList(List<Expr> l){
            lExp = l;
        }
	
        @Override
	public ExpPattern getPattern(){
		if (proc == null){
                    return null;
                }
		return proc.getPattern();
	}
	
	public void setPattern(ExpPattern pat){
            if (proc != null){
		proc.setPattern(pat);
            }
	}
	
	void setExist(Exp exp){
		exist = exp;
	}
	
	public Exp getExist(){
		return exist;
	}
	
	// Exp
	
        @Override
	public Expression prepare(ASTQuery ast){
		if (proc != null){
                    return this;
                }
                
		//proc = new Processor(this);
                proc = processor;
		proc.type(this, ast);                
		
                int i = 0;
		for (Expression exp : getArgs()){
			exp.prepare(ast);
		}
		
                // May create a specific Processor to manage this specific term
                // and overload proc field
                // Use case: regex, external fun, etc.
		proc.prepare(this, ast);
                
                if (getArg() != null){
                    getArg().prepare(ast);
                }
                
		return this;
		
	}
        
	
        @Override
	public int type(){
		return type;
	}
        
        /**
     * @param type the type to set
     */
        public void setType(int type) {
            this.type = type;
        }
	
        @Override
	public int oper(){
		return oper;
	}
        
        @Override
        public void setOper(int n){
            oper = n;
        }

	public Processor getProcessor() {
		// TODO Auto-generated method stub
		return proc;
	}
        
        void setProcessor(Processor p){
            proc = p;
        }
	              
        @Override
        public Term copy(Variable o, Variable n) {
            Term f = null;
            if (isFunction()) {
                f = function(getName());
                f.setLongName(getLongName());
                f.setModality(getModality());
                if (getArg() != null){
                    f.setArg(getArg().copy(o, n));
                }
            } else {
                f = Term.create(getName());
            }
            for (Expression e : getArgs()) {
                Expression ee = e.copy(o, n);
                f.add(ee);
            }
            return f;
    }
        
        @Override
    void visit(ExpressionVisitor v){
         v.visit(this);
    }   

    /**
     * @return the isExport
     */
        @Override
    public boolean isPublic() {
        return false;
    }

    /**
     * @param isExport the isExport to set
     */
        @Override
    public void setPublic(boolean isExport) {
    }

        @Override
    public Term getTerm(){
        return this;
    }

    /**
     * @return the place
     */
        @Override
    public int place() {
        return place;
    }

    /**
     * @param place the place to set
     */
    public void setPlace(int place) {
        this.place = place;
    }
    
}