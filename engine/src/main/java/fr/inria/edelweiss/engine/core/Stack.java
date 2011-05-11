package fr.inria.edelweiss.engine.core;

import java.io.IOException;
import java.util.ArrayList;

import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.Clause;
import fr.inria.edelweiss.engine.model.api.Rule;

/**
 * Stack of clause/rule/binding
 * At each recursive call we check that we are not in a loop
 * i.e. not same rule on equivalent clause with same binding
 *
 */
class Stack  {
	
	ArrayList<Rule> rules 		= new ArrayList<Rule>();
	ArrayList<Clause> clauses 	= new ArrayList<Clause>();
	ArrayList<Bind> binds 		= new ArrayList<Bind>();

	void push(Rule r, Clause clause, Bind bind){
		rules.add(r);
		clauses.add(clause);
		binds.add(bind);
	}
	
	void pop(){
		if (rules.size()>0)
			rules.remove(rules.size()-1);
		if (clauses.size()>0)
			clauses.remove(clauses.size()-1);
		if (binds.size()>0)
			binds.remove(binds.size()-1);
	}
	
	
	/**
	 * If stack already contains an occurrence of same rule, same clause
	 * and same binding, we are in a loop: engine should skip this
	 */
	boolean contains(Rule rule, Clause clause, Bind bind){
		for (int i=0; i<rules.size(); i++){
			if (rules.get(i) == rule &&
				equivalent(clauses.get(i), binds.get(i), clause, bind)){
				return  true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check whether b1(c1) and b2(c2) are equivalent
	 * i.e. if the clauses are the same when variables are replaced by their value
	 * including free variables. 
	 * i.e. if there are two free variables at same place in the two clauses
	 * it is considered equivalent
	 * TODO: 
	 * we test strict equality, there is no subsumption of classes and properties
	 * 
	 */
	public boolean equivalent(Clause c1, Bind b1, Clause c2, Bind b2){
		int n = 0;
		if (c1.size()!=c2.size()) return false;
		
		for (ElementClause elem1 : c1){
			ElementClause elem2 = c2.get(n++);
			boolean equiv = true;
			Constant value1 = b1.getValue(elem1);
			Constant value2 = b2.getValue(elem2);
			if (value1 == null){ 
				if (value2 != null) equiv = false;
			}
			else if (value2 == null) equiv = false;
			else if (! value1.getDatatypeValue().sameTerm(value2.getDatatypeValue()))
				equiv = false;

			if (! equiv){
				return false;
			}
		}
		return  true;
	}
	
	
	// former test, used to check physical equality of clauses which is not enough
	boolean contains2(Rule rule, Clause clause, Bind bind){
		for (int i=0; i<rules.size(); i++){
			if (rules.get(i) == rule &&
				clauses.get(i) == clause){
				if (binds.get(i).match(clause, bind)){
					return  true;
				}
			}
		}
		return false;
	}
	
	


	
	int size(){
		return rules.size();
	}
	
	public String toString(){
		String str = "";
		for (int i=0; i<rules.size(); i++){
			str += i + ": " + rules.get(i).getID() + " " + clauses.get(i).getTriple() + " " + binds.get(i) + "\n";
		}
		return str;
	}
	
}
