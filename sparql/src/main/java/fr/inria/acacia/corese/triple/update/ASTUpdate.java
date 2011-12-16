package fr.inria.acacia.corese.triple.update;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;

/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class ASTUpdate {
	static final String NL = System.getProperty("line.separator");
	
	List<Update> list;
	ASTQuery ast;
	
	
	ASTUpdate(){
		list = new ArrayList<Update>();
	}
	
	public static ASTUpdate create(){
		return new ASTUpdate();
	}
	
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}
	
	public StringBuffer toString(StringBuffer sb){
		for (Update ast : list){
			ast.toString(sb);
			sb.append(NL);
		}
		return sb;
	}
	
	public void set(ASTQuery a){
		ast = a;
	}
	
	ASTQuery getASTQuery(){
		return ast;
	}
	
	public void add(Update ope){
		ope.set(this);
		list.add(ope);
	}
	
	public List<Update> getUpdates(){
		return list;
	}
	
	
	
	

}
