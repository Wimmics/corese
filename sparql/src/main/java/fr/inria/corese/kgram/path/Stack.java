package fr.inria.corese.kgram.path;

import java.util.ArrayList;
import java.util.List;

/**
 * Stack of epsilon transitions with path lengths.
 * Checks it does not loop.
 * use case: (p* / q*)*
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Stack {
	
	List<Step> steps;
	List<Integer> sizes;
	
	Stack(){
		steps = new ArrayList<Step>();
		sizes = new ArrayList<Integer>();
	}
	
	
	void clear(){
		steps.clear();
		sizes.clear();
	}
	
	void push(Step st, Path path){
		steps.add(st);
		sizes.add(path.size());
	}
	
	void pop(Step st, Path path){
		steps.remove(steps.size()-1);
		sizes.remove(sizes.size()-1);
	}
	
	boolean loop(Step st, Path path){
		int index = steps.lastIndexOf(st);
		if (index == -1) return false;
		if (sizes.get(index) == path.size()) return true;
		return false;
	}
	
	int size(Step st){
		int index = steps.lastIndexOf(st);
		if (index == -1) return -1;
		return sizes.get(index);
	}
	

}
