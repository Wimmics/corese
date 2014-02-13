package fr.inria.acacia.corese.triple.parser;

/**
 * 
 * Implement array for function arg
 * Use case: filter(member(?x, [1, 2 ,3]))
 * 
 * @author corby
 *
 */
public class Array extends Constant {
	ExpressionList list;
	
	Array(ExpressionList list){
		super("array[" + list.toString() +"]");
		this.list = list;
	}
	
	public ExpressionList getList(){
		return list;
	}
	
	public boolean isArray(){
		return true;
	}

}
