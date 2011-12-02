package fr.inria.acacia.corese.triple.parser;

public class Query extends BasicGraphPattern {
	
	ASTQuery ast;
	
	Query(){}
	
	Query(ASTQuery a){
		ast = a;
		add(ast.getBody());
	}
	
	public StringBuffer toString(StringBuffer sb){
		sb.append(ast.toString());
		return sb;
	}
	
	public static Query create(ASTQuery a){
		return new Query(a);
	}
	
	public ASTQuery getQuery(){
		return ast;
	}
	
	public boolean isQuery(){
		return true;
	}

}
