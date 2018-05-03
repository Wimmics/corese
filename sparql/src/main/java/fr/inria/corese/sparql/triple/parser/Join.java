package fr.inria.corese.sparql.triple.parser;

public class Join extends And {
	
	public static Join create(Exp e1, Exp e2){
		Join e = new Join();
		e.add(e1);
		e.add(e2);
		return e;
	}
	
	
        @Override
	public boolean isJoin(){
		return true;
	}
	
        @Override
	public ASTBuffer toString(ASTBuffer sb){
		sb.append(get(0));
		//sb.append(" " + KeywordPP.JOIN + " ");
		sb.append(get(1));
		return sb;
	}
	
	
}
