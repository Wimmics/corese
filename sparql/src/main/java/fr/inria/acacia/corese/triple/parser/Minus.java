package fr.inria.acacia.corese.triple.parser;

import java.util.List;

import fr.inria.acacia.corese.triple.cst.KeywordPP;

public class Minus extends And {
	
	public static Minus create(Exp exp){
		Minus e = new Minus();
		e.add(exp);
		return e;
	}
	
	
        @Override
	public boolean isMinus(){
		return true;
	}
	
        @Override
	public StringBuffer toString(StringBuffer sb){
		sb.append(get(0));
		sb.append(" " + KeywordPP.MINUS + " ");
		sb.append(get(1));
		return sb;
	}
	
	
        @Override
	public boolean validate(ASTQuery ast, boolean exist) {
		boolean b1 = getBody().get(0).validate(ast, exist);
		
		List<Variable> list = ast.getStack();
		ast.newStack();
		boolean b2 = getBody().get(1).validate(ast, true);
		ast.setStack(list);
		
		return b1 && b2;
	}
	
	
}
