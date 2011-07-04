package fr.inria.acacia.corese.triple.update;

import java.util.ArrayList;
import java.util.List;

import fr.inria.acacia.corese.triple.parser.ASTQuery;

/**
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2011
 */
public class ASTUpdate {
	
	List<Update> list;
	ASTQuery ast;
	
	
	ASTUpdate(){
		list = new ArrayList<Update>();
	}
	
	public static ASTUpdate create(){
		return new ASTUpdate();
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
