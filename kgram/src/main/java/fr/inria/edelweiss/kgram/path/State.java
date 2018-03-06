package fr.inria.corese.kgram.path;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Regex;

/**
 * Regex Automaton State
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
class State {
	int num;
	boolean
	// final state
	end = false, 
	// start state of a loop exp*
	first = false,
	check = false;
	List<Step> list;
	Step[] steps;
	int loop = -1, count = 0;
	Regex exp;
	
	State(int n){
		num = n;
		list = new ArrayList<Step>();
	}
	
	void add(Step step){
		list.add(step);
	}
	
	void add(int i, Step step){
		list.add(i, step);
	}
	
	void set(Regex e){
		exp = e;
	}
	
	Regex getRegex(){
		return exp;
	}
	
	boolean isBound(){
		return exp != null;
	}
	
	int getCount(){
		return count;
	}
	
	void setCount(int n){
		count = n;
	}
	
	void incCount(int n){
		count += n;
	}
	
	boolean endLoop(){
		if (isBound()){
			if (getCount() >= getMax()){
				return true;
			}
		}
		return false;
	}
	
	int getMin(){
		return getRegex().getMin();
	}
	
	int getMax(){
		return getRegex().getMax();
	}
	
	public List<Step> getSteps(){
		return list;
	}
	
	void setOut(boolean b){
		end = b;
	}
	
	public boolean isFinal(){
		return end;
	}
	
	void setFirst(boolean b){
		first = b;
	}
	
	boolean isFirst(){
		return first;
	}
	
	/**
	 * exp+ need check loop at once
	 */
	void setPlus(boolean b){
		check = b;
	}
	
	boolean isPlus(){
		// check = true for exp+ wich is compiled as exp{1,}
		//return  isFirst() ;//&& isCheckLoop();
		return check;
	}
	
	/**
	 * This state implements a loop such as:
	 * exp* exp+ exp{}
	 * return true if we must prevent a loop on visited nodes
	 * in case of exp{2,} we do not need to check the loop for the first two steps
	 */
	boolean isCheckLoop(){
		return   (! isBound() || // exp*
				  getCount() >= getMin() || // exp{2,}
				  check); // exp+
	}
	
	void setLoop(int n){
		loop = n;
	}
	
	public int getLoop(){
		return loop;
	}
	
	void compile(){
		steps = new Step[list.size()];
		int i = 0;
		for (Step s : list){
			steps[i++] = s;
		}
	}
	
	Step[] getTransitions(){
		return steps;
	}
	
	public String toString(){
		return "st" + num ;
	}
}