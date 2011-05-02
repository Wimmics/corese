package fr.inria.acacia.corese.triple.parser;

public class Minus extends And {
	
	public static Minus create(Exp exp){
		Minus e = new Minus();
		e.add(exp);
		return e;
	}
	
	
	public boolean isMinus(){
		return true;
	}
	
	
}
