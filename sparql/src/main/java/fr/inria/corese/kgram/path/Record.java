package fr.inria.corese.kgram.path;

import java.util.ArrayList;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;

/**
 * Stack of Regex + Visit for loop check
 * @author Olivier Corby, Edelweiss, INRIA 2011
 * 
 */
public class Record extends ArrayList<Regex> {
	
	Visit visit;
        Node target;
	Regex exp;
	
	ArrayList<Node> stack;
	
	boolean success = false;
	
	Record(Visit v){
		visit = v;
		stack = new ArrayList<Node>();
	}
	
	Record(){}
	
	Visit getVisit(){
		return visit;
	}
        
        	
	Record push(Regex exp){
		if (exp != null){
			add(exp);
		}
		return this;
	}
	
	Record set(Regex exp){
		if (exp != null){
			set(size()-1, exp);
		}
		return this;
	}
	
	Regex pop(){
		if (size() == 0){
			System.out.println("** PP: pop empty");
			return null;
		}
		Regex exp = get(size()-1);
		remove(size()-1);
		return exp;
	}
	
	
	/*************************************************************
	 * 
	 * Stack dedicated to PARA: e1 || e2
	 * 
	 * e1 goes from start to target
	 * then e2 checks that there is a path from start to target
	 * pushStart() store start node 
	 * setTarget() store target node in a fresh new Record, 
	 * hence there is no stack for target 
	 * New Record is created for loop check specific to e2 path
	 * 
	 *************************************************************/
	
	void pushStart(Node start){
		stack.add(start);
	}
	
	Node getStart(){
		return stack.get(stack.size()-1);
	}
	
	Node popStart(){
		Node n = stack.get(stack.size()-1);
		stack.remove(stack.size()-1);
		return n;
	}
	
	void setTarget(Node n){
		target = n;
	}
	
	Node getTarget(){
		return target;
	}
	
	void setSuccess(boolean b){
		success = b;
	}
	
	boolean isSuccess(){
		return success;
	}
	

}
