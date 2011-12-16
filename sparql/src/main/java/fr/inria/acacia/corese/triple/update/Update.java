package fr.inria.acacia.corese.triple.update;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.NSManager;
import fr.inria.acacia.corese.triple.parser.Statement;

/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class Update  extends Statement {
	
	static final String[] NAME = 
	{"load", "clear", "drop", "create", "add", "move", "copy",
	 "insert", "delete", "composite"};

	
	public static final int LOAD 	= 0;
	public static final int CLEAR 	= 1;
	public static final int DROP 	= 2;
	public static final int CREATE  = 3;
	public static final int ADD 	= 4;
	public static final int MOVE 	= 5;
	public static final int COPY 	= 6;
	
	public static final int INSERT 		= 7;
	public static final int DELETE 		= 8;
	public static final int COMPOSITE 	= 9;

	
	int type;
	ASTUpdate astu;
	
	String title(){
		return NAME[type];
	}
	
	public int type(){
		return type;
	}
	
	void set(ASTUpdate a){
		astu = a;
	}
	
	public ASTUpdate getASTUpdate(){
		return astu;
	}
	
	public ASTQuery getASTQuery(){
		return astu.getASTQuery();
	}
	
	public NSManager getNSM(){
		return getASTQuery().getNSM();
	}
	
	public String expand(String name){
		if (name == null) return null;
		return getNSM().toNamespaceB(name);
	}
	
	public boolean isComposite(){
		return this instanceof Composite;
	}
	
	public boolean isBasic(){
		return this instanceof Basic;
	}
	
	public Composite getComposite(){
		return (Composite) this;
	}

	public Basic getBasic(){
		return (Basic) this;
	}
}
