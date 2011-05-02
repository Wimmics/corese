package fr.inria.acacia.corese.triple.parser;

public class Negation extends BasicGraphPattern {
	
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
