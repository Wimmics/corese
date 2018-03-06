package fr.inria.corese.kgram.path;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Regex;

/**
 * Regex Automaton Transition
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
class Step {
	Regex prop;
	List<State> states;
	State state;
	boolean epsilon = false,
	enter = false,
	leave = false,
	// first of a star
	loop = false,
	check = false,
	walk = false;
	
	Step(){
		states = new ArrayList<State>();
	}
	
	Step(Regex prop){
		this.prop = prop;
		states = new ArrayList<State>();
	}
	
	public String toString(){
		String title = "epsilon";
		if (prop!=null) title = prop.toString();
		return title + " -> " + states.get(0) + "; ";
	}
	
	void setEpsilon(boolean b){
		epsilon = b;
	}
	
	public boolean isEpsilon(){
		return epsilon;
	}
	
	void setEnter(boolean b){
		enter = b;
	}
	
	public boolean isEnter(){
		return enter;
	}

	
	void setLeave(boolean b){
		leave = b;
	}
	
	public boolean isLeave(){
		return leave;
	}
	
	void setLoop(boolean b){
		loop = b;
	}
	
	public boolean isLoop(){
		return loop;
	}
	
	void setCheck(boolean b){
		check = b;
	}
	
	public boolean isCheck(){
		return check;
	}
	
	void setWalk(boolean b){
		walk = b;
	}
	
	public boolean isWalk(){
		return walk;
	}
	
	void setState(State state){
		states.add(state);
		this.state = state; 
	}
	
	// constant(p) or not(constant(p))
	public Regex getProperty(){
		return prop;
	}
	
	public Regex getRegex(){
		return prop;
	}
	
	public List<State> getStates(){
		return states;
	}
	
	public State getState(){
		return state;
	}
	
}
