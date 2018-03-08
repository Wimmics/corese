package fr.inria.corese.sparql.triple.parser;

public class Scope extends BasicGraphPattern {

	Scope(){
		
	}
	
	Scope(Exp e){
		super(e);
	}

	public static Scope create(Exp e){
		return new Scope(e);
	}
	
        @Override
	public boolean isScope(){
		return true;
	}
	
}
