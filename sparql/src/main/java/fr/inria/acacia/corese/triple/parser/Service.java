package fr.inria.acacia.corese.triple.parser;

/**
 * Draft SPARQL 1.1 Service
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Service extends And {
	
	Atom uri;
	boolean silent;
	
	Service(Atom serv, Exp exp, boolean b){
		super(exp);
		uri = serv;
		silent = b;
	}
	
	public static Service create(Atom serv, Exp body, boolean b){
		return  new Service(serv, body, b);
	}
	
	public static Service create(Atom serv, Exp body){
		return  new Service(serv, body, false);
	}
	
    public StringBuffer toString(StringBuffer sb) {
    	sb.append(Term.SERVICE);
    	sb.append(" ");
    	uri.toString(sb);
    	sb.append(" ");
    	return super.toString(sb);
    }
    
    public boolean isSilent(){
		return silent;
	}
	
	public boolean isService(){
		return true;
	}
	
	public Atom getService(){
		return uri;
	}
	
	  public boolean validate(ASTQuery ast, boolean exist){
		  if (uri.isVariable()){
			  ast.bind(uri.getVariable());
			  if (! exist){
				  ast.defSelect(uri.getVariable());
			  }
		  }
		  return super.validate(ast, exist);
	  }


}
