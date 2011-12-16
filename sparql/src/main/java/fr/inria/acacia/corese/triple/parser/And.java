package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.cst.Keyword;


/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * Implement the group graph pattern
 * 
 * @author Olivier Corby 
 */

public class And extends Exp {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
	public And() {
	}
	
	public And(Exp exp){
		add(exp);
	}
	
	public And(Exp exp1, Exp exp2){
		add(exp1);
		add(exp2);
	}
	
	public static And create(Exp exp1){
		return new And(exp1);
	}
	
	public static And create(Exp exp1, Exp exp2){
		return new And(exp1, exp2);
	}
	
	public boolean isAnd(){
		return true;
	}
	
	/**
	 * (and t1 t2 (or (and t3) (and t4)))
	 */
	
//	Exp copy(){
//		And tmp= new And();
//		for (int i=0; i<size(); i++){
//			tmp.add(eget(i).copy());
//		}
//		return tmp;
//	}
	
	/**
	 * If there is a negation, need also a positive relation (not an expression)
	 * @return
	 */
//	boolean validate() {
//		Exp exp;
//		for (int i = 0; i < size(); i++) {
//			exp = (Exp) get(i);
//			if (! exp.validate()) {
//				return false;
//			}
//		}
//		return true;
//	}
//	
	boolean validateNegation() {
		Triple triple;
		boolean negation=false;
		for (int i = 0; i < size(); i++) {
			triple = (Triple) get(i);
			if (! triple.isExp()){
				return true;
			}
		}
		return ! negation;
	}
	
	
//	void process(ASTQuery aq){
//		Exp exp=new Or();
//		exp.add(this);
//		aq.setQuery(exp);
//	}
	
	
	/**
	 *  Recursive distribution of AND over OR
	 */
//	Exp distrib(){
//		Exp exp;
//		boolean triple=true;
//		for (int i=0; i<size(); i++){
//			// recurse distrib on sons
//			exp=eget(i).distrib();
//			set(i, exp);
//			if (! (exp.isTriple()))
//				triple=false;
//		}
//		if (triple){
//			return this;
//		}
//		else {
//			Exp res=product();
//			return res;
//		}
//	}
	
	String getOper() {
		return Keyword.SEAND;
	}
	
	
	/**
	 * and( or(a b)  c) -> or(and(a c) and(b c))
	 */
//	Exp product(){
//		Exp exp=eget(0);
//		if (exp instanceof Triple)
//			exp=new And(exp);
//		for (int i=1; i<size(); i++){
//			exp=exp.product(eget(i));
//		}
//		return exp;
//	}
//	
//	Exp product(Exp exp){
//		Exp res= exp.sproduct(this);
//		return res;
//	}
//	
//	/**
//	 * sproduct conceptually perfom arg * this (instead of this * arg)
//	 * the args are permuted by product because of polymorphism weakness of java
//	 */
//	
//	/**
//	 * A (and B C) -> (and A B C)
//	 */
//	Exp sproduct(Triple t){
//		//add(t);
//		add(0, t);
//		return this;
//	}
//	
//	
//	Exp sproduct(Option t){
//		//add(t);
//		add(0, t);
//		return this;
//	}
//	
//	
//	/**
//	 * (and A B) (and C D) -> (and A B C D)
//	 */
//	Exp sproduct(And exp){
//		exp.addAll(this);
//		return exp;
//	}
//	
//	/**
//	 * (or A B) (and C D) -> (or (and A C D) (and B C D))
//	 */
//	Exp sproduct(Or exp){
//		Exp alt;
//		for (int i=0; i<exp.size(); i++){
//			alt=exp.eget(i);
//			alt.addAll(this);
//		}
//		return exp;
//	}
//	
//	
//	Exp simplify(){
//		boolean simple=true;
//		for (int i=0; i<size(); i++){
//			if (eget(i) instanceof And){
//				simple=false;
//			}
//		}
//		if (simple)
//			return this;
//		And exp=new And();
//		for (int i=0; i<size(); i++){
//			if (eget(i) instanceof And){
//				exp.addAll(eget(i));
//			}
//			else exp.add(eget(i));
//		}
//		return exp;
//	}
	
}