package fr.inria.acacia.corese.triple.parser;

public class IfThenElse extends  BasicGraphPattern {

	Exp eif, ethen, eelse;
	
	public static IfThenElse create(Exp e1, Exp e2, Exp e3){
		IfThenElse exp = new IfThenElse();
		exp.eif = e1;
		exp.ethen = e2;
		exp.eelse = e3;
		return exp;
	}
	
	public Exp getIf(){
		return eif;
	}
	
	public Exp getThen(){
		return ethen;
	}
	
	public Exp getElse(){
		return eelse;
	}
	
	public boolean isIfThenElse(){
		return true;
	}
	
}
