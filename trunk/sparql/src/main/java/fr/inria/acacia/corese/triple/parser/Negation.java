package fr.inria.acacia.corese.triple.parser;

public class Negation extends And {
	
	Negation(Exp e){
		super(e);
	}

	public static Negation create(Exp e){
		return new Negation(e);
	}
	
	public boolean isNegation(){
		return true;
	}
	
}
