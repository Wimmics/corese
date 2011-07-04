package fr.inria.acacia.corese.triple.parser;

import java.util.Vector;

import fr.inria.acacia.corese.triple.cst.Keyword;
import fr.inria.acacia.corese.triple.cst.KeywordPP;



/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * @author Olivier Corby & Olivier Savoie
 */

public class Or extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	static int num = 0;
	
	public Or() {}
	
	public  Or (Exp e1, Exp e2){
		add(e1);
		add(e2);		
	}
	
	public static Or create(){
		return new Or();
	}
	
	public static Or create(Exp e1, Exp e2){
		if (!e1.isAnd()){
			e1 = new BasicGraphPattern(e1);
		}
		if (!e2.isAnd()){
			e2 = new BasicGraphPattern(e2);
		}
		return new Or(e1, e2);
	}
	
//	void process(ASTQuery aq){
//		aq.setQuery(this);
//	}
	
	public boolean isUnion(){
		return true;
	}
	
	/**
	 * if son is or, merge it with this or :
	 * or(or(a b) c) -> or(a b c)
	 */
	Exp distrib(){
		Exp exp;
		for (int i=0; i<size(); i++){
			exp=eget(i).distrib();
			if (exp instanceof Option)
				exp=new And(exp);
			set(i, exp);
		}
		int i=0;
		while (i < size()){
			exp=eget(i);
			if (exp instanceof Or){
				// remove sons thar are or, put there body into this or :
				getBody().remove(exp);
				addAll(exp);
			}
			else {
				i++;
			}
		}
		if (isExp()){
			return toExp();
		}
		return this;
	}
	
	/**
	 * This OR is a computable exp
	 * e.g. : ?x = a OR ?y = b
	 * Let's transform it as one Term in one triple
	 * @return
	 */
	Exp toExp(){
		Expression t=toTerm();
		Triple triple= Triple.create(t);
		return triple;
	}
	
	String getOper() {
		return Keyword.SEOR;
	}
	
	
	Exp product(Exp exp){
		return exp.sproduct(this);
	}
	
	/**
	 * sproduct should perfom arg * this (instead of this * arg)
	 * the args are permuted by product because of polymorphism weakness of java
	 */
	
	Exp sproduct(Triple exp){
		return sproduct(new And(exp));
	}
	
	Exp sproduct(Option exp){
		return sproduct(new And(exp));
	}
	
	
	/**
	 * (and A B) (or C D) -> (or (and A B C) (and A B D))
	 */
	Exp sproduct(And exp){
		Exp alt;
		for (int i=0; i<size(); i++){
			alt=eget(i); // C,D
			for (int j=exp.size()-1; j>=0; j--){
				alt.add(0, exp.eget(j));
			}
			
		}
		return this;
	}
	
	/**
	 * perfom exp or this
	 * (or A B) (or C D) -> (or (A C) (A D) (B C) (B D))
	 */
	Exp sproduct(Or exp){
		Or res=new Or();
		And prod;
		for (int i=0; i<exp.size(); i++){
			for (int j=0; j<size(); j++){
				prod=new And();
				prod.addAll(exp.eget(i));
				prod.addAll(eget(j));
				res.add(prod.simplify());
			}
		}
		return res;
	}
	
	
	/**
	 * Set  source for graph/state ?src 
	 * generate subState locally within each A union B 
	 */
	void setSource(Parser parser,  Env env, String src, boolean b) {
		Exp exp;
		for (int i = 0; i < size(); i++) {
			Env nenv = env.fork();
			exp = eget(i);
			exp.setSource(parser, nenv, src, b);
			
			if (nenv.state && nenv.vars.size() > 0){
				// we have found state ?src, generate ?src cos:subStateOf ?si
				exp.defState(parser,  nenv);
			}
		}
	}
	
	
	/**
	 * FROM NAMED uri
	 * Generate local filter for source var : var = uri
	 * vars : vector of collected source var (redefines that of Exp)
	 * named : from named uri
	 */
	void collectSource(Parser parser, Vector<String> vars, Vector<String> named) {
		Exp exp;

		for (int i = 0; i < size(); i++) {
			Vector<String> localVars = new Vector<String>();
			exp = eget(i);
			exp.collectSource(parser, localVars, named);
			
			if (localVars.size() > 0){
				// we have found source var, generate var = uri
				Exp expFrom = source(parser, localVars, named);
				exp.add(expFrom);
			}
		}
		
	}
	
	
	
	void setFromSource(Parser parser, String name, Vector<String> vars, Vector<String> from, boolean generate) {
		Exp exp;
		// allocate a new vector for potential source var in option
		name += "u"+ num++  ; 
		for (int i = 0; i < size(); i++) {
			Vector<String> localVars = new Vector<String>();
			exp = eget(i);
			// TODO : allocate a new root name / use parser
			exp.setFromSource(parser, name, localVars, from, generate);
			
			if (localVars.size() > 0){
				// we have set source var, generate var = uri
				Exp expFrom = source(parser, localVars, from);
				exp.add(expFrom);
			}
		}
	}	
	
	
	
	
	
	
	
	
	public String toSparql() {
		return toSparql(null);
	}
	
	public String toSparql(NSManager nsm) {
		String str = "";
		if (size() == 1) {
			str += eget(0).toSparql(nsm);
		} else {
			for (int i=0;i<size();i++) {
				str += KeywordPP.OPEN_BRACKET + KeywordPP.SPACE + eget(i).toSparql(nsm);
				str += KeywordPP.CLOSE_BRACKET + KeywordPP.SPACE + 
					KeywordPP.UNION + KeywordPP.SPACE + 
					KeywordPP.OPEN_BRACKET + KeywordPP.SPACE;
				i++;
				Exp e = eget(i);
				if (e != null)
					str += e.toSparql(nsm);
				str += KeywordPP.CLOSE_BRACKET + KeywordPP.SPACE_LN;				
			}
		}
		return str;
	}
	
	
	
}