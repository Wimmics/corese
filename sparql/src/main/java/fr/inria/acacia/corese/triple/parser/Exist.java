package fr.inria.acacia.corese.triple.parser;

public class Exist extends BasicGraphPattern {
	

	Exist(Exp e){
		super(e);
	}

	public static Exist create(Exp e1){
		return new Exist(e1);
	}

	public boolean isExist(){
		return true;
	}
	
}