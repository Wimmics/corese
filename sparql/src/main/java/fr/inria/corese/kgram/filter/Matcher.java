package fr.inria.corese.kgram.filter;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;


/**
 * Filter Exp Matcher
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Matcher implements ExprType {
	boolean 
		rec = false,
		matchConstant = true,
		trace = false;
	
	
	public boolean match(Pattern qe, Expr te){
		MatchBind bind =  MatchBind.create();
		return match(qe, te, bind);
	}

	boolean match(Pattern qe, Expr te, MatchBind bind){
		//System.out.println("** Matcher: " + qe + " " + te);
		rec = qe.isRec();
		matchConstant = qe.isMatchConstant();
		boolean b = process(qe, te, bind);
		//System.out.println("** Matcher: " + qe + " " + te + " " + b);
		return b;
	}
	
	

	
	/**
	 * Expr matcher
	 * 
	 * Special case:
	 * OR(EXP) with rec=true
	 * means match OR(EXP) recursively with OR(EXP1, OR(EXP2, EXP3))
	 * EXP is matched with all EXPi
	 * Pattern variable store target variable
	 * Afterwards, Pattern variable must match same target variables in all EXPi
	 * 
	 */
	boolean process(Expr qe, Expr te, MatchBind bind){
		
		if (qe.type() == ALTER){
			for (Expr ee : qe.getExpList()){
				int size = bind.size();
				boolean b = process(ee, te, bind);
				if (b) return true;
				else bind.clean(size);
			}
			return false;
		}
		
		// LT GT LE GE comparison operator match any comparison operator here
		// args will be sorted in matchTerm
		if (! matchType(qe, te))	return false;
			
		// When Pattern is already bound, recurse on its value
		switch (qe.type()){
		case CONSTANT:
			if (matchConstant && bind.hasValue(qe)){
				return process(bind.getValue(qe), te, bind);
			}
			break;
		default:
			if (bind.hasValue(qe)){
				return process(bind.getValue(qe), te, bind);
			}
		}
			

		switch (qe.type()){
		
		case VARIABLE:
		case CONSTANT:
			return matchVarConst(qe, te, bind);
						
		case FUNCTION:
		case JOKER:
			return matchBoolFunAny(qe, te, bind);			
		
		case BOOLEAN:
			if (qe.arity() == 2 && te.arity() == 2){
				return matchTerm(qe, te, bind);			
			}
			else if (qe.arity() > te.arity()){
				// BOOLEAN ANY Pat Pat vs NOT EXP
				return false;
			}
			else {
				// NOT has only one argument
				// rec = true has only argument
				return matchBoolFunAny(qe, te, bind);			
			}

		case TERM:
			//if (qe.arity() == 2 && qe.arity() == te.arity()){
				return matchTerm(qe, te, bind);	
			//}
		}
	
		return false;
	}
	
	
	
	boolean matchVarConst(Expr qe, Expr te, MatchBind bind){
		switch (qe.type()){

		case VARIABLE:
			if (qe.getLabel() != null) return qe.getLabel().equals(te.getLabel());
			else bind.setValue(qe, te);
			break;

			
		case CONSTANT:
			if (matchConstant){
				if (isBindable(qe)){
					bind.setValue(qe, te);
				}
				else {
					// target values
					return qe.equals(te);
				}
			}
		}
		
		return true;
	}
	

	
	/**
	 * BOOLEAN NOT
	 * BOOLEAN with rec = true
	 *   OR(EXP) matches recursively OR(OR(EXP1, EXP2), EXP3)
	 * FUNCTION ANY
	 */
	boolean matchBoolFunAny(Expr qe, Expr te, MatchBind bind) {
		if (qe.arity() == 0){
			// qe e.g. Pattern(ANY, ANY) with no args
			// first time: bind the pattern
			bind.setValue(qe, te);
			return true;
		}
		if (te.arity() == 0){
			// qe has arity, te has not: fail
			return false;
		}

		int size = bind.size();
		int i = 0;
		Expr qarg = qe.getExp(0);
		for (Expr targ : te.getExpList()){

			if (i < qe.arity()){
				// use pattern ith arg
				qarg = qe.getExp(i++);
			}
			else if (rec){
				// reuse pattern 0th arg
				qarg = qe.getExp(0);
			}
			else {
				// query has less arguments than target
				// and query args match target args
				// should reset bindings here
				bind.clean(size);
				return false;
			}

			//System.out.println("** M2: " + qarg + " " + targ);

			if (process(qarg, targ, bind)){
				// OK
			}
			//System.out.println("** M3: " + qarg + " " + targ);
			else if (rec){ 
				//System.out.println("** M4: " + qarg + " " + targ);
				// recursive match of Pattern qe on argument targ
				// qe = OR(EXP)
				// qarg = EXP
				// targ = OR(EXP1, EXP2)
				if (process(qe, targ, bind)){
					//System.out.println("** M5: " + qe + " " + targ);
					// OK
				}
				else {
					bind.clean(size);
					return false;
				}
			}
			else {
				bind.clean(size);
				return false;
			}
		}
		//System.out.println("** M6: " + qe + " " + te);

		if (! bind.hasValue(qe)){
			// because when rec = true, qe may have already been bound to an arg of te
			bind.setValue(qe, te);
		}
		return true;
	}
	
	
	boolean matchTerm(Expr qe, Expr te, MatchBind bind) {
		Expr fst = qe.getExp(0), snd = qe.getExp(1);
		
		if (isGL(qe.oper()) && qe.oper() != te.oper()) {
			if (qe.oper() == GL){
				// OK
			}
			else if (inverse(qe.oper(), te.oper())){
				// qe(x < y) VS te(y > x)
				// try switch qe args
				fst = qe.getExp(1);
				snd = qe.getExp(0);
			}
			else return false;
		}

		int size = bind.size();
		boolean b = 
			process(fst, te.getExp(0), bind) &&
			process(snd, te.getExp(1), bind);

		//System.out.println("** M9: " + qe + " " + te + " " + b);

		if (! b){
			bind.clean(size);

			if (symmetric(qe)){
				// try switch arguments of qe
				// switch ?x = ?y to ?y = ?x
				b = 
					process(snd, te.getExp(0), bind) &&
					process(fst, te.getExp(1), bind);	
				//System.out.println("** M10: " + qe + " " + te + " " + b);
			}
		}

		if (b) bind.setValue(qe, te);
		return b;
	}
	
	/**
	 * Is it a Pattern constant
	 */
	boolean isBindable(Expr exp){
		return exp instanceof Pattern;
	}
		
	boolean symmetric(Expr qe){
		if (qe.type() == BOOLEAN && qe.oper() == JOKER) return true;
		int t = qe.oper();
		return t == EQ || t == NE || t == PLUS || t == MULT || t == AND || t == OR;
	}
	
	boolean inverse(int t1, int t2){
		return 
			(t1 == LT && t2 == GT) ||
			(t1 == GT && t2 == LT) ||
			(t1 == GE && t2 == LE) ||
			(t1 == LE && t2 == GE) ; 
	}
	
	boolean isGL(int tt){
		return tt == GL || tt == LT || tt == LE || tt == GT || tt == GE;
	}
	
	
	/**
	 * accept comparison operator with any comparison because term matcher 
	 * will permute arguments as needed
	 */
	boolean match(int qt, int tt){
		if (qt == JOKER) return true;
		if (isGL(qt)) return isGL(tt);
		if (qt == EQNE) return tt == EQ || tt == NE;
		return qt == tt;
	}
	
	boolean matchType(Expr qe, Expr te){
		if (qe.oper() == EXIST || te.oper() == EXIST) return false;
		if (qe.type() == JOKER) return true;
		switch (qe.type()){
			case VARIABLE:
			case CONSTANT: return match(qe.type(), te.type());
			default: return match(qe.type(), te.type()) && match(qe.oper(), te.oper());
		}
	}

}
