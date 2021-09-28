package fr.inria.corese.kgram.filter;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.tool.Message;


/**
 * Filter Exp Checker
 * 
 * Check presence of patterns that are always true or always false
 * Such as:
 * ?x != ?x
 * ?x > ?y && ?x < ?y
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Checker implements ExprType {
	static final String ATRUE = "true";
	static final String AFALSE = "false";
	static final String AREWRI = "rewritable";
	public static boolean verbose = true;
	
	static final int BOOL = BOOLEAN;

	static List<Pattern> alwaysFalse, alwaysTrue, rewritable;
	

	int count = 1;
	
	Matcher matcher;
	Query query;
	
	public Checker(Query q){
		matcher = new Matcher();
		query = q;
	}
	
	/**
	 * var1=cst || var2=cst
	 */
	public boolean check(String v1, String v2, Expr ee){
		Pattern p = path(v1, v2);
		boolean b = matcher.match(p, ee);
		return b;
	}
	
	/**
	 * Check one filter 
	 */
	public boolean check(Expr ee){
		boolean b = match(ee);
		// match = true means that a false pattern matches
		// hence return false (check correctness is false)
		return ! b;
	}
	
	/**
	 * Check two filters for contradiction:
	 * ?x = ?y vs ?x != ?y
	 */
	
	public boolean check(Expr e1, Expr e2){
		// fake a AND using a pattern
		Pattern and = new Pattern(BOOL, AND, e1, e2);
		return check(and);
	}
	
	
	/**
	 * Return true if a false pattern matches
	 * return false otherwise
	 */
	boolean match(Expr ee){
		boolean bf = match(ee, alwaysFalse(), AFALSE);
		boolean bt = match(ee, alwaysTrue(), ATRUE);
		// exp that can be simplified
		boolean bw = match(ee, rewritable(), AREWRI);
		
		return bf;
	}
	
	
	boolean match(Expr ee, List<Pattern> pat, String mes){
		//System.out.println(exp1 + " " + exp2);
		boolean suc = false, b;
		
		for (Pattern p : pat){
			b = matcher.match(p, ee);
			//System.out.println(p + " " + ee + " " + b);
			if (b){
				suc = true;
				log(ee, mes);
			}
		}
		
		return suc;
	}
	
	
	void log(Expr ee, String mes){
		
		if (isPattern(ee) && ee.oper() == AND){
			query.addInfo(mes + ": " , ee.getExp(0) + " && " + ee.getExp(1));
			
			log(mes + " " + ee.getExp(0) + " && " + ee.getExp(1));
			if (mes.equals(AFALSE)){
				query.addFailure(ee.getExp(0).getFilter());
				query.addFailure(ee.getExp(1).getFilter());
			}				
		}
		else {
			query.addInfo(mes + ": ", ee);
			
			log(mes + " " + ee);
			if (mes.equals(AFALSE)){
				query.addFailure(ee.getFilter());
			}
		}
	}
	
	
	/****************************************************************
	 * 
	 * Always true 
	 * 
	 */
	
	List<Pattern> alwaysTrue(){
		if (alwaysTrue == null){
			List<Pattern> pat = new ArrayList<Pattern>();
			pat.add(patOrNotPat());
			pat.add(notPatAndNotPat());
			pat.add(eqeq());
			pat.add(le());
			pat.add(notNeq());
			pat.add(eqOrNeq());
			pat.add(leOrGe());
			pat.add(ltOrGe());

			alwaysTrue = pat;
		}
		return alwaysTrue;
	}

	// always true
	
	Pattern eqeq(){
		// EXP = EXP
		Pattern exp = pat(JOKER);
		return term(EQ, exp, exp);

	}
	
	Pattern notNeq(){
		// ! (EXP != EXP)
		Pattern exp = pat(JOKER);
		return not(term(NEQ, exp, exp));
		
	}
	
	Pattern le(){
		// EXP <= EXP
		Pattern exp = pat(JOKER);
		return term(LE, exp, exp);
		
	}
	
	
	Pattern patOrNotPat(){
		// EXP || ! EXP
		Pattern exp  = pat(JOKER);
		return or(exp, not(exp));
		
	}
	
	Pattern notPatAndNotPat(){
		// ! (EXP && ! EXP)
		Pattern exp  = pat(JOKER);
		return not(and(exp, not(exp)));
		
	}
	
	Pattern eqOrNeq(){
		//EXP1 = EXP2 || EXP1 != EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return or(term(EQ, e1, e2), term(NEQ, e1, e2));
		
	}

	Pattern leOrGe(){
		// EXP1 >= EXP2 || EXP1 <= EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return or(term(LE, e1, e2), term(GE, e1, e2));
		
	}
	
	Pattern ltOrGe(){
		// EXP1 < EXP2 || EXP1 >= EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return or(term(LT, e1, e2), term(GE, e1, e2));
		
	}
	

	/*************************************************
	 * 
	 * Always false patterns
	 * 
	 */
	
	List<Pattern> alwaysFalse(){
		if (alwaysFalse == null){
			List<Pattern> pat = new ArrayList<Pattern>();
			pat.add(neqSelf());
			pat.add(ltSelf());
			pat.add(notEqSelf());
			pat.add(notGeSelf());
			
			pat.add(patNotPat());
			pat.add(notOr());
			pat.add(eqNeq());
			pat.add(eqGt());
			pat.add(ltGt());
			pat.add(gtNotGe());
			pat.add(eqNotGe());

			alwaysFalse = pat;
		}
		return alwaysFalse;
	}
	
	
	/**
	 * ?from=cst || ?to=cst
	 */
	Pattern path(String v1, String v2){
		Pattern e1 = term(EQ, Pattern.variable(v1), constant());
		Pattern e2 = term(EQ, Pattern.variable(v2), constant());
		Pattern e3 = or(e1, e2);
		return e3;
	}
	
	// always false
	Pattern neqSelf(){
		// EXP != EXP
		Pattern exp = pat(JOKER);
		return term(NE, exp, exp);
		
	}
	
	Pattern ltSelf(){
		// EXP < EXP
		Pattern exp = pat(JOKER);
		return term(LT, exp, exp);
		
	}
	
	Pattern notEqSelf(){
		// !(EXP = EXP)
		Pattern exp = pat(JOKER);
		return not(term(EQ, exp, exp));
		
	}
	
	Pattern notGeSelf(){
		// !(EXP >= EXP)
		Pattern exp = pat(JOKER);
		return not(term(GE, exp, exp));
		
	}
	
	
	
	Pattern patNotPat(){
		// EXP && ! EXP
		Pattern exp = pat(JOKER);
		return and(exp, not(exp));
	}
	
	
	Pattern notOr(){
		// ! (EXP || ! EXP)
		Pattern exp = pat(JOKER);
		return not(or(exp, not(exp)));
	}

	
	// TODO:
	// we can have both with list of values 
	//  ?x = xpath() && ?x != xpath()
	Pattern eqNeq(){
		// EXP1 = EXP2 && EXP1 != EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		Pattern and = and(term(EQ, e1, e2), term(NE, e1, e2));
		return and;
	}
	
	
	Pattern eqGt(){
		// EXP1 = EXP2 && EXP1 > EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(EQ, e1, e2), term(GT, e1, e2));
	}
	
	Pattern ltGt(){
		// EXP1 > EXP2 && EXP1 < EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(GT, e1, e2), term(LT, e1, e2));
	}
	

	Pattern gtNotGe(){
		// EXP1 > EXP2 && ! (EXP1 >= EXP2)
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(GT, e1, e2), not(term(GE, e1, e2)));
	}

	Pattern eqNotGe(){
		// EXP1 = EXP2 && ! (EXP1 >= EXP2)
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(EQ, e1, e2), not(term(GE, e1, e2)));
	}
	
	// PRAGMA: not for simple variable
	Pattern variable(String name){
		Pattern var = Pattern.variable(name);
		Pattern pat = pat(JOKER, JOKER, var);
		pat.setRec(true);
		return pat;
	}
	
	Pattern constant(){
		return Pattern.constant();
	}
	
	
	/**********************************************************
	 * 
	 * Rewritable
	 * 
	 */
	List<Pattern> rewritable(){
		if (rewritable == null){
			List<Pattern> pat = new ArrayList<Pattern>();
			pat.add(leLt());
			pat.add(leEq());
			pat.add(leNe());
			pat.add(leGe());
			
			pat.add(leNotGe());
			pat.add(ltNotGt());
			pat.add(notNot());
			pat.add(notOrNot());
			
			pat.add(patBoolPat());
			pat.add(neqNotEq());
			pat.add(ltNotGe());
			pat.add(leNotGt());	
			rewritable = pat;
		}
		return rewritable;
	}
	
	

	boolean isPattern(Expr e){
		return e instanceof Pattern;
	}
	
	Pattern leLt(){
		// EXP1 <= EXP2 && EXP1 < EXP2 -> EXP1 = EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LE, e1, e2), term(LT, e1, e2));
		
	}
	
	Pattern leEq(){
		// EXP1 <= EXP2 && EXP1 = EXP2 -> EXP1 = EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LE, e1, e2), term(EQ, e1, e2));
		
	}
	
	Pattern leNe(){
		// EXP1 <= EXP2 && EXP1 != EXP2 -> EXP1 < EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LE, e1, e2), term(NE, e1, e2));
		
	}
	
	Pattern leGe(){
		// EXP1 <= EXP2 && EXP1 >= EXP2 -> EXP1 = EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LE, e1, e2), term(GE, e1, e2));
		
	}
	

	
	Pattern leNotGe(){
		// EXP1 <= EXP2 && !(EXP1 >= EXP2) -> EXP1 < EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LE, e1, e2), not(term(GE, e1, e2)));
		
	}
	
	Pattern ltNotGt(){
		// EXP1 < EXP2 && !(EXP1 > EXP2) -> EXP1 < EXP2
		Pattern e1 = pat(JOKER);
		Pattern e2 = pat(JOKER);
		return and(term(LT, e1, e2), not(term(GT, e1, e2)));
		
	}
	

	
	Pattern notNot(){
		// ! ! EXP
		Pattern pat = not(not(pat(JOKER)));
		return pat;
	}
	
	Pattern notOrNot(){
		// ! (! EXP1 || ! EXP2) -> EXP1 && EXP2
		Pattern e1 = not(pat(JOKER));
		Pattern e2 = not(pat(JOKER));
		Pattern pat = not(or(e1, e2));
		return pat;
	}

	
	Pattern patBoolPat(){
		// EXP && EXP
		// EXP || EXP
		Pattern exp = pat(JOKER);
		return pat(BOOL, JOKER, exp, exp);
		
	}
	
	Pattern neqNotEq(){
		// EXP1 != EXP2 && ! (EXP1 = EXP2)
		// EXP1 != EXP2 || ! (EXP1 = EXP2)
		Pattern p1 = pat(JOKER);
		Pattern p2 = pat(JOKER);
		Pattern pp1 = term(NE, p1, p2);
		Pattern pp2 = not(term(EQ, p1, p2));
		return pat(BOOL, JOKER, pp1, pp2);
		
	}
	
	
	Pattern ltNotGe(){
		// EXP1 < EXP2 && ! (EXP1 >= EXP2)
		// EXP1 < EXP2 || ! (EXP1 >= EXP2)
		Pattern p1 = pat(JOKER);
		Pattern p2 = pat(JOKER);
		Pattern pp1 = term(LT, p1, p2);
		Pattern pp2 = not(term(GE, p1, p2));
		return pat(BOOL, JOKER, pp1, pp2);
		
	}

	Pattern leNotGt(){
		// EXP1 <= EXP2 && ! (EXP1 > EXP2)
		// EXP1 <= EXP2 || ! (EXP1 > EXP2)
		Pattern p1 = pat(JOKER);
		Pattern p2 = pat(JOKER);
		Pattern pp1 = term(LE, p1, p2);
		Pattern pp2 = not(term(GT, p1, p2));
		return pat(BOOL, JOKER, pp1, pp2);
		
	}

	
	
	Pattern pat(int type){
		return new Pattern(type);
	}
	
	Pattern pat(int type, int ope, Pattern e1){
		return new Pattern(type, ope, e1);
	}
	
	Pattern pat(int type, int ope, Pattern e1, Pattern e2){
		return new Pattern(type, ope, e1, e2);
	}
	
	Pattern not(Pattern e){
		return pat(BOOLEAN, NOT, e);
	}
	
	Pattern and(Pattern e1, Pattern e2){
		return pat(BOOLEAN, AND, e1, e2);
	}
	
	Pattern or(Pattern e1, Pattern e2){
		return pat(BOOLEAN, OR, e1, e2);
	}
	
	Pattern term(int ope, Pattern e1, Pattern e2){
		return pat(TERM, ope, e1, e2);
	}
	
	Pattern fun(int ope, Pattern e1){
		return pat(FUNCTION, ope, e1);
	}
	

	
	void log(Object obj){
		if (verbose) Message.log(Message.CHECK, count++ + " " + obj);
	}
	
	/**
	 * Called on path regex 
	 */
	void test(Expr path){
		Pattern p1 = pat(JOKER);
		Pattern p2 = pat(JOKER);

		Pattern or =  fun(JOKER, or(p1, p2));
		Pattern seq = fun(MULT, term(DIV, p1, p2));

		boolean b = matcher.match(or, path);
		//if (b) System.out.println("** Check: " + path);
	}
	
	
	
	
	

}
