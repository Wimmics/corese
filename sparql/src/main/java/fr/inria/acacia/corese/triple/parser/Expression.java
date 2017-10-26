package fr.inria.acacia.corese.triple.parser;

import fr.inria.corese.triple.function.script.Let;
import fr.inria.corese.triple.function.script.ForLoop;
import fr.inria.acacia.corese.api.Computer;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.api.ASTVisitor;
import fr.inria.acacia.corese.triple.api.ExpressionVisitor;
import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.corese.compiler.java.JavaCompiler;
import fr.inria.corese.triple.function.term.Binding;
import fr.inria.edelweiss.kgram.api.core.DatatypeValue;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import fr.inria.edelweiss.kgram.api.core.Regex;
import fr.inria.edelweiss.kgram.api.core.TripleStore;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Evaluator;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * The root class of the expressions of the query language: Atom, Variable, Constant, Term
 * <br>
 * @author Olivier Corby
 */

public class Expression extends TopExp 
implements Regex, Filter, Expr, Pointerable  {
	public static final int STDFILTER = 0;
	public static final int ENDFILTER = 1;
	public static final int POSFILTER = 2;
	public static final int BOUND = 4;
	
	static ArrayList<Expr> empty = new ArrayList<Expr>(0);
        int retype = Regex.UNDEF;
	
	boolean isSystem = false;
        boolean isReverse = false;
	
	String name, longName;
	Expression exp;
        private Expression expression;
        private ASTQuery ast;

	public Expression(){}
	
	public Expression(String str) {
		name=str;
	}
	
        @Override
	public  int getArity(){
		return -1;
	}
	
	public ArrayList<Expression> getArgs(){
		return null;
	}
	
        @Override
	public Expression getArg(int i){
		return null;
	}
	
        /**
         * Every filter/select/bind exp is compiled
         */
	public Expression compile(ASTQuery ast){
            prepare(ast);
            local(ast);
            return this;
	}
        
        /**
         * Declare local variables, assign index
         * arg of function define(f(?x) = exp) 
         * arg of let(?x = exp)
         * arg of map(xt:fun(?x), exp)
         * @param ast 
         */
        void local(ASTQuery ast){
            ExpressionVisitorVariable vis = new ExpressionVisitorVariable(ast);
            vis.start(this);
        }
        
        Expression prepare(ASTQuery ast){
		return this;
	}
	
	public Expression and(Expression e2){
		if (e2 == null){
			return this;
		}
		else {
			return  Term.create(Keyword.SEAND, this, e2);
		}
	}
	
	public Expression star(){
		return Term.function(Term.STAR, this);
	}
        
        @Override
        public String getShortName(){
            return name;
        }
	
        @Override
	public String getName(){
		return name;
	}
        
        @Override
        public int getNbVariable() {
            return 0;
        }
        
        public void setNbVariable(int n){
            
        }
	
        @Override
	public String getLongName(){
		return longName;
	}
	
	public void setLongName(String name){
		longName = name;
	}
	
	public void setExpr(Expression exp){
		this.exp = exp;
	}
	
        @Override
	public Expression getExpr(){
		return exp;
	}
	
	public String getKey(){
		return toString();
	}
	
	public void setName(String str){
		name=str;
	}
	
        @Override
	public boolean isSystem(){
		return isSystem;
	}
	
	public void setSystem(boolean b){
		isSystem = b;
	}
        
        @Override
        public boolean isTrace() {
            return false;
        }
        
        @Override
        public boolean isDebug() {
            return false;
        }
        
        @Override
        public boolean isTester(){
            return false;
        }
        
        @Override
        public boolean isPublic(){
		return false;
	}
	
        @Override
	public void setPublic(boolean b){
	}
        	
	public boolean isArray(){
		return false;
	}
	
	boolean isAtom(){
		return false;
	}
	
        @Override
	public boolean isConstant(){
		return false;
	}

        @Override
	public boolean isVariable(){
		return false;
	}
	
	public boolean isSimpleVariable(){
		return false;
	}
	
	// blank as variable in sparql query
	public boolean isBlankNode(){
		return false;
	}
	
	public boolean isTerm(){
		return false;
	}
        
        public boolean isStatement(){
		return false;
	}
	
	public boolean isTerm(String oper){
		return false;
	}
	
	public boolean isFunction(){
		return false;
	}
               
        @Override
        public boolean isFuncall(){
		return false;
	}
	
	public boolean isFunction(String str){
		return false;
	}
        
        @Override
        public boolean isRecExist(){
            return false;
        }
		
	Bind validate(Bind env){
		return env;
	}
	
	public boolean validate(ASTQuery ast) {
		return true;
	}

	
	public String getLang(){
		return null;
	}
	
	public String getDatatype(){
		return null;
	}
	
	public String getSrcDatatype() {
		return null;
	}
	
	public boolean isAnd(){
		return false;
	}
	
        @Override
	public boolean isSeq(){
		return false;
	}
	
        @Override
	public boolean isAlt(){
		return false;
	}
	
	public boolean isOr(){
		return false;
	}
	
        @Override
	public boolean isPara(){
		return false;
	}
	
        @Override
	public boolean isNot(){
		return false;
	}
	
	public void setWeight(String w){
	}
	
        @Override
	public int getWeight(){
		return -1;
	}
	
        @Override
	public boolean isInverse(){
		return false;
	}
	
        @Override
	public void setInverse(boolean b){
		//isInverse = b;
	}
	
        @Override
	public boolean isReverse(){
		return isReverse;
	}
	
        @Override
	public void setReverse(boolean b){
		isReverse = b;
	}
	
        @Override
	public Expression translate(){
		return this;
	}
	
        @Override
	public boolean isNotOrReverse(){
		return false;
	}
	
        @Override
	public int getMin(){
		return -1;
	}

        @Override
	public int getMax(){
		return -1;
	}
	
	// include isPlus()
        @Override
	public boolean isCounter(){
		return false;
	}
	
	boolean isOrVarEqCst(Variable var){
		return false;
	}
		
        @Override
	public boolean isStar(){
		return false;
	}
	
        @Override
	public boolean isOpt(){
		return false;
	}
	
	public boolean isTest(){
		return false;
	}
	
	public boolean isCheck(){
		return false;
	}
	
	public boolean isFinal(){
		return false;
	}
	
        @Override
	public Expression reverse(){
		return this;
	}
	
        @Override
	public Expression transform(){
		return transform(false);
	}
	
	public Expression transform(boolean isReverse){
		return this;
	}
	
        @Override
	public int regLength(){
		return 0;
	}
	
	public int length(){
		return 0;
	}
	
        @Override
	public boolean isPlus(){
		return false;
	}
	
	public boolean  isType (ASTQuery ast, int type){
		return false;
	}
	
	public boolean  isType (ASTQuery ast, Variable var, int type){
		return false;
	}
	
	public boolean isVisited(){
		return false;
	}
	
	public void setVisited(boolean b){
	}
	
	public boolean isPath(){
		return false;
	}
	
        @Override
	public boolean isBound(){
		return false;
	}
        
        public Term getTerm(){
            return null;
        }
        	
        @Override
	public Variable getVariable(){
		return null;
	}
	
	public Constant getConstant(){
		return null;
	}
	
	public Atom getAtom(){
		return null;
	}
	
	// get:gui::?name
	public Variable getIntVariable() {
        return null;
    }
	
	
        @Override
	public String toRegex() {
		String str = toString();
		if (isReverse()){
			str = Term.SREV + str;
		}
		return str;
	}
        
       public void toJava(JavaCompiler jc) {
            jc.toJava(this);
        }
	
	/**
	 * Translate some terms like :
	 * different(?x ?y ?z) -> (?x != ?y && ?y != ?z && ?x != ?z)
	 */
	public Expression process(){
		return  this;
	}
	
	/**
	 * use case: select fun(?x) as ?y
	 * rewrite occurrences of ?y as fun(?x)
	 */
	public Expression process(ASTQuery ast){
		return  this;
	}
	
	public Expression rewrite(){
		return this;
	}
				
	
	/*************************************************************
	 * 
	 * KGRAM Filter & Exp
	 * 
	 */
	
        @Override
	public Filter getFilter(){
		return this;
	}

	
        @Override
	public Expr getExp() {		
		return this;
	}

	
        @Override
	public List<String> getVariables() {		
            return getVariables(false);
	}
        
        @Override
      public List<String> getVariables(boolean excludeLocal) {
        List<String> list = new ArrayList<String>();
        getVariables(list, excludeLocal);
        return list;
      }
               	
	public void getVariables(List<String> list, boolean excludeLocal) {
	}

        public List<Constant> getConstants() {	
            ArrayList<Constant> l = new ArrayList<Constant>();
            getConstants(l);
            return l;
	}
        
        void getConstants(List<Constant> l) {		           
	}
	
        @Override
	public int arity() {
		
		return 0;
	}
               
        @Override
	public String getLabel() {
		
		if (longName!=null) return longName;
		return name;
	}

	
        @Override
	public Object getValue() {
		return null;
	}
        
        @Override
        public IDatatype getDatatypeValue(){
            return null;
        }

	
        @Override
	public boolean isAggregate() {
		
		return false;
	}
	
        @Override
	public boolean isExist(){
		if (oper() == ExprType.EXIST){
			return true;
		}
		else {
			for (Expr ee : getExpList()){
				if (ee.isExist()){
					return true;
				}
			}
		}
		return false;
	}
	
	
        @Override
	public boolean isRecAggregate() {
		
		return false;
	}
	
        @Override
	public boolean isFunctional() {
		
		return false;
	}

	
        @Override
	public int oper() {
		
		return -1;
	}
        
        @Override
        public void setOper(int n){
            
        }

	
        @Override
	public int type() {
		return ExprType.UNDEF;
	}
        
        @Override
    public boolean match(int t) {
        switch (t) {
            case ExprType.JOKER:
                return true;
            case ExprType.EQ_SAME:
                return match(ExprType.EQ, ExprType.SAMETERM);
            case ExprType.BETWEEN:
                return match(ExprType.MORE, ExprType.LESS);
            case ExprType.MORE:
                return match(ExprType.GT, ExprType.GE);
            case ExprType.LESS:
                return match(ExprType.LT, ExprType.LE);   
            case ExprType.KIND:
                return match(ExprType.ISURI) || match(ExprType.ISBLANK, ExprType.ISLITERAL);
            case ExprType.BIPREDICATE:
                return isBipredicate();
            case ExprType.TINKERPOP:
            case ExprType.TINKERPOP_RESTRICT:
                return isTinkerpop(t);
            default:
                return oper() == t;
        }
    }
        
    boolean match(int t1, int t2) {
            return match(t1) || match(t2);
    }
    
    boolean isBipredicate(){
        switch (oper()){
            case ExprType.CONTAINS:
            case ExprType.REGEX:
            case ExprType.STARTS:
            case ExprType.ENDS :
                return true;
        }
        return false;
    }

    /**
     * SPARQL filters available in Tinkerpop
     */
    public boolean isTinkerpop(int t){
        switch (type()){
            case ExprType.BOOLEAN: return true;
        }
        
        switch (oper()){
            case ExprType.EQ:
            case ExprType.NEQ:
            case ExprType.SAMETERM:
                
            case ExprType.LE:
            case ExprType.LT:
            case ExprType.GE:
            case ExprType.GT: 
                
            case ExprType.CONTAINS:
            case ExprType.STARTS:
            case ExprType.ENDS:
            case ExprType.REGEX:
                
            case ExprType.IN:
               return true; 
                
            case ExprType.ISURI:
            case ExprType.ISBLANK:
            case ExprType.ISLITERAL:
                   return t == ExprType.TINKERPOP;            
        }
        return false;
    }
        
	
        @Override
	public int retype() {
		return retype;
	}
	
	void setretype(int n){
		retype = n;
	}
	
	public int getretype() {
		if (isConstant())return Regex.LABEL;
		if (isNot())	 return Regex.NOT;
		if (isSeq())	 return Regex.SEQ;
		if (isPara())	 return Regex.PARA;
		if (isAlt())	 return Regex.ALT;
		if (isPlus())	 return Regex.PLUS;
		if (isCounter()) return Regex.COUNT;
		if (isStar()) 	 return Regex.STAR;
		if (isOpt()) 	 return Regex.OPTION;
		if (isReverse()) return Regex.REVERSE;
		if (isTest())	 return Regex.TEST;
		if (isCheck())	 return Regex.CHECK;

		return Regex.UNDEF;
	}

	
        @Override
	public List<Expr> getExpList() {
		return empty;
	}
	
        @Override
	public Expr getExp(int i){
		return null;
	}
        
        @Override
        public void setExp(int i, Expr e){
            
        }
        
        @Override
        public void addExp(int i, Expr e){
            
        }
	
        @Override
	public int getIndex() {
		return -1;
	}

	
        @Override
	public void setIndex(int index) {		
	}

	
        @Override
	public void setArg(Expr exp) {
		
	}
	
        @Override
	public Expression getArg(){
		return null;
	}
	
        @Override
	public Object getPattern(){
		return null;
	}

	public void setDistinct(boolean b) {
	}
	
        @Override
	public boolean isDistinct() {
		return false;
	}
	
	public void setShort(boolean b) {
	}
	
        @Override
	public boolean isShort() {
		return false;
	}
	
        @Override
	public String getModality() {		
		return null;
	}
        
        @Override
        public void setModality(String mod){
            
        }
        
        
        @Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
        
        public Expression copy(Variable o, Variable n){
            return this;
        }
        
        public void visit(ExpressionVisitor v){
            
        }
        
        @Override
    public Expr getDefine() {
        return null;
    }

 
        @Override
    public void setDefine(Expr define) {
        
    }
    
          @Override
    public Term getFunction(){
        return null;
    }
          
        @Override
    public List<String> getMetadataValues(String name) {
        return null;
    }
    
        @Override
    public Expression getBody(){
        return null;
    }
        
        @Override
    public Expression getDefinition(){
        return null;
    } 
        
        @Override
    public DatatypeValue[] getArguments(int n) {
        return null;
    }
        
     public Let getLet(){
         return null;
     } 
     
     public ForLoop getFor(){
         return null;
     }
	
    @Override
    public int subtype() {
        return ExprType.UNDEF;
    }
    
        @Override
    public void setSubtype(int t) {
    }

    /**
     * @return the ancestor
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * @param ancestor the ancestor to set
     */
    public void setExpression(Expression ancestor) {
        this.expression = ancestor;
    }
    
    public boolean hasExpression(){
        return expression != null;
    }
    
    public ASTQuery getAST(){
        return ast;
    }
    
    public void setAST(ASTQuery ast){
        this.ast = ast;
    }
    
    public boolean hasAST(){
        return ast != null;
    }

    @Override
    public int pointerType() {
        return Pointerable.EXPRESSION_POINTER;
    }
    
    
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p){
        return null;
    }
    
    public IDatatype eval(Computer eval, Environment env, Producer p) {
        return eval(eval, env, p, new IDatatype[0]);
    }

    public IDatatype eval(Computer eval, Environment env, Producer p, IDatatype[] param) {
        return null;
    }
    
  
    @Override
    public Mappings getMappings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mapping getMapping() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity getEntity() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query getQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TripleStore getTripleStore() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValue(String var, int n) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterable getLoop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
     
}