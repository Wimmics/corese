package fr.inria.corese.kgram.path;

import java.util.ArrayList;
import java.util.List;


import fr.inria.corese.kgram.api.core.Regex;

/**
 * Compile Reg Exp into NDFSA
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Automaton {
	static boolean isStack = false;
	
	State in, out, current;
	// list of open transitions (target state to be completed)
	StepList pending;
	// state -> list of transitions
	Table table;
	int nbState;
	boolean isBound = false;
	
	class StepList  extends ArrayList<Step>{}
	
	class Table extends ArrayList<State> {}
	
	
	

	
	Automaton(){
		nbState = 0;
		table = new Table();
		in = create();
		current = in;
		pending = new StepList();

	}
	
	State create(){
		State st = new State(nbState++);
		table.add(st);
		return st;
	}
	
	public State getIn(){
		return in;
	}
	
	public State getOut(){
		return out;
	}
	
	public void setCurrent(State state){
		current = state;
	}
	
	public State getCurrent(){
		return current;
	}
	
	boolean isBound(){
		return isBound;
	}
	
	public void start(){
		current = in;
		for (State state : table){
			state.setLoop(-1);
			state.setCount(0);
			for (Step step : state.getSteps()){
				step.setWalk(false);
			}
		}
	}
	
	public String toString(){
		String str = "";
		str += "in: "  + in  +"\n";
		str += "out: " + out +"\n";
		for (State state : table){
			if (! state.isFinal() || state.getSteps().size()>0){
				str += state +":";
				for (Step step : state.getSteps()){
					str += " " + step;
				}
				str += "\n";
			}
		}
		return str;
	}
	
	/**
	 * Compile regex into automaton
	 */
	void compile(Regex exp){
		compute(exp, null,  false, false, false);
		if (pending.size()>0){
			// complete pending transitions to final state
			out = create();
			//System.out.println("** A1: " + out);
			for (Step step : pending){
				step.setState(out);
			}
			pending.clear();
		}
		else {
			// use case: star(Exp)
			out = current;
			//System.out.println("** A2: " + out);
		}
		out.setOut(true);
		compile();
		
		//System.out.println(this);
	}

	void compile(){
		for (State state : table){
			state.compile();
		}
	}

	/**
	 * or = true when inside an or
	 * algorithm:
	 * p1 & p2 : 
	 * s0: p1->s1 ; s1: p2->s2 
	 * p1 | p2 :
	 * s0: p1->s1 , p2->s1
	 * star(exp) :
	 * s0: exp->s0 , eps -> s1
	 */
	void compute(Regex exp, Regex star, boolean or,  boolean inverse, boolean reverse){
		
		//if (exp.getExpr()!=null) System.out.println("** A: " + exp + " " + exp.getExpr() + " " + exp.isSeq());
		
		if (exp.isReverse()){
			// ?x ^ ex:prop ?y
			// SPARQL 1.1 inverse edge
			// rec call to process ^ ^ exp
			compute(exp.getArg(0), star, or, inverse, !reverse);
			return;
		}
		else if (exp.isInverse()){
			// ?x i(ex:prop) ?y
			// deprecated corese regular and inverse edge
			inverse = true;
			exp = exp.getArg(0);
		}
		
		if (exp.isNotOrReverse()){
			// use case:   ! (^ rdf:type | rdf:type)
			// rewrite as :  (^ (! rdf:type) | ! rdf:type)
			compute(exp.translate(), star, or, inverse, reverse);
			return;
		}
		
		if (exp.isConstant() || exp.isNot()){
			if (! isStack) exp.setReverse(reverse);
			exp.setInverse(inverse);
			Step step = new Step(exp);
			// pending transitions waiting for their target state
			pending.add(step);
			// add transition with exp to current state 
			current.add(step);
		}
		else {
			// save pending transitions
			StepList psave = pending;
			// current state
			State csave = current;
			// start new pending list for this expression
			pending = new StepList();
			
			if (exp.isSeq()){

				int arity = exp.getArity(), n = 0;
				int start = 0; 
				if (reverse){
					start = arity-1;
				}
				int i = start;
				boolean ok = true;
				while (ok){
					// in case of reverse, enumerate steps  in reverse order
					// ?x ^(p1/p2) ?y
					// generate ?x ^p2/^p1 ?y
					Regex e = exp.getArg(i);	
					// csave: A & B
					compute(e, star, or,  inverse, reverse);
					n++;
					if (n < arity && pending.size() > 0){
						// csave: A->current
						current = create();
						// pending transitions point to new current
						for (Step state : pending){
							state.setState(current);
						}
						// clear pending
						pending.clear();
					}
					
					if (reverse){
						i--;
						ok = i>= 0;
					}
					else {
						i++;
						ok = i<arity;
					}
				}
				
				
				
				// add new pending to previous pending
				// current: B->? 
				psave.addAll(pending);
			}
			else if (exp.isAlt()){
				for (int i=0; i<exp.getArity(); i++){
					Regex e = exp.getArg(i);	
					// csave: A | B
					// csave: A->? ; B->?
					compute(e, star, true,  inverse, reverse);
					current = csave;
					//System.out.println("**A2: " + current);
				}
				// A->? ; B->?
				psave.addAll(pending);
			}
			else if (exp.isOpt()){
				compute(exp.getArg(0), star, true,  inverse, reverse);
				
				// create non det step for opt:
				current = csave;
				Step st2 = new Step(); //(eps);
				st2.setEpsilon(true);
				current.add(0, st2);
				psave.add(st2);
				
				// A->? ; B->?
				psave.addAll(pending);
			}
			else if (exp.isStar()){
								
				while (exp.isStar() && exp.getArg(0).isStar()){
					// star(star(p)) -> star(p)
					exp = exp.getArg(0);
				}
				
				if (true){ 
					// create non det step to enter star
					// to have an entry state
					Step st = new Step(); 
					st.setEpsilon(true);
					current.add(st);
					current = create();

					csave = current;
					st.setState(current);
					st.setEnter(true);

					if (! isUpperBound(exp)){
						// start state of a loop exp* or exp{n,}
						current.setFirst(true);
					}
					if (isBound(exp)){
						// use case: exp{n,m}
						current.set(exp);
						isBound = true;
					}
					if (exp.isPlus()){
						// use case:
						// exp+ -> exp{1,}
						// check loop 
						current.setPlus(true);
					}
				}
				
				
				// current is the entry point of the loop
				
				compute(exp.getArg(0), exp, or,  inverse, reverse);
				
				for (Step step : pending){
					// csave: A*
					// csave: A->csave
					// step loops back to entry of A*
					step.setState(csave);
					step.setLoop(true);
				}
				

				// create non det step to leave star:
				current = csave;
				Step st2 = new Step(); //eps);
				st2.setEpsilon(true);
				st2.setLeave(true);
				current.add(0, st2);
				psave.add(st2);

			}
			
			pending = psave;
		}
	}
		
	
	/**
	 * use case:
	 * (p1/p2)*
	 * exp = p1/p2
	 * return true
	 * 
	 * TODO:
	 * ((p1?/p2?)/p3)*
	 */
	boolean isStar(Regex exp, int n){
		return true;
	}
	
	boolean isBound(Regex exp){
		boolean b = exp.getMin() != -1 || exp.getMax() != -1;
		return b;
	}
	
	boolean isUpperBound(Regex exp){
		// exp {1,} ::= exp{1,Integer.MAX}
		boolean b = exp.getMax() != -1 &&  exp.getMax() < Integer.MAX_VALUE;
		return b;
	}
	
	
	/**
	 	return starting properties of automaton
	 	WARNING: 
	 	return null means all properties needed
	 */
	List<Regex> getStart(){
		ArrayList<Regex> evec = new ArrayList<Regex> ();
		Table svec = new Table();
		return getStart(getIn(), evec, svec);
	}
	
	ArrayList<Regex> getStart(State st, ArrayList<Regex> evec, Table svec){
		svec.add(st);
		for (Step step : st.getTransitions()){
			if (! step.isEpsilon()){ 
				evec.add(step.getProperty());
			}
		}
		for (Step step : st.getTransitions()){
			if (step.isEpsilon()){
				if (step.getState().isFinal()){
					// Go from input state to output state with epsilon transitions
					// i.e. without a specific property 
					// hence we need to enumerate all resources as start candidate
					// used case: ?x ex:p* ?y
					return null;
				}
				if (! svec.contains(step.getState())){
					ArrayList<Regex> el = getStart(step.getState(), evec, svec);
					if (el == null) return null;
				}
			}
		}
		return evec;
	}

}
