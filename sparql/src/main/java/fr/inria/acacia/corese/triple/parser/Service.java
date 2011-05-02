package fr.inria.acacia.corese.triple.parser;

/**
 * Draft SPARQL 1.1 Service
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Service extends BasicGraphPattern {
	
	Atom uri;
	
	Service(Atom serv, Exp exp){
		super(exp);
		uri = serv;
	}
	
	public static Service create(Atom serv, Exp body){
		return  new Service(serv, body);
	}
	
	public boolean isService(){
		return true;
	}
	
	public Atom getService(){
		return uri;
	}

}
