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
	
	public StringBuffer toString(StringBuffer sb){
		sb.append(Term.EXIST + " " + get(0));
		return sb;
	}
	
	public boolean validate(ASTQuery ast, boolean exist) {
		if (getBody().size() > 0){
			return getBody().get(0).validate(ast, true);
		}
		return true;
	}
	
}