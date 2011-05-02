package fr.inria.edelweiss.kgram.filter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Expr;


/**
 * Manage bindings of Matcher
 * Can bind and unbind
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Bind {
	
	Hashtable<Expr, Expr> table;
	List<Expr> stack;
	boolean success = true;
	
	Bind (){
		table = new Hashtable<Expr, Expr>();
		stack = new ArrayList<Expr>();
	}
	
	static Bind create(){
		return new Bind();
	}
	
	boolean hasValue(Expr exp){
		return table.containsKey(exp);
	}
	
	Expr getValue(Expr exp){
		return table.get(exp);
	}
	
	void setValue(Expr qe, Expr te){
		if (qe instanceof Pattern){
			//System.out.println(qe + " += " + te);
			table.put(qe, te);
			stack.add(qe);
		}
	}
	
	void reset(Expr exp){
		//System.out.println(exp + " -= " + table.get(exp));
		table.remove(exp);
		stack.remove(exp);
	}
	
	int size(){
		return stack.size();
	}
	
	/**
	 * Remove last bindings until stack.size() = size 
	 */
	void clean(int size){
		while (stack.size()>size){
			reset(stack.get(stack.size()-1));
		}
		if (table.size() != stack.size()){
			System.out.println("** ERROR: Match Bind: Stack and Table have different sizes");
		}
	}

}
