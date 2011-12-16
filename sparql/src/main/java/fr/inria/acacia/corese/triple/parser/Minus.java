package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.triple.cst.KeywordPP;

public class Minus extends And {
	
	public static Minus create(Exp exp){
		Minus e = new Minus();
		e.add(exp);
		return e;
	}
	
	
	public boolean isMinus(){
		return true;
	}
	
	public StringBuffer toString(StringBuffer sb){
		sb.append(get(0));
		sb.append(" " + KeywordPP.MINUS + " ");
		sb.append(get(1));
		return sb;
	}
	
	
}
