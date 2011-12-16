package fr.inria.acacia.corese.triple.parser;

public class Forall extends BasicGraphPattern {
	
	Exp first;

	Forall(Exp e1, Exp e2){
		super(e2);
		first = e1;
	}

	public static Forall create(Exp e1, Exp e2){
		return new Forall(e1, e2);
	}
	
	public Exp getFirst(){
		return first;
	}
	
	
	public boolean isForall(){
		return true;
	}
	
}
