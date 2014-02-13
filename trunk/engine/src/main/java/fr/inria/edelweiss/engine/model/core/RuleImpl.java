package fr.inria.edelweiss.engine.model.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.edelweiss.engine.model.api.Bind;
import fr.inria.edelweiss.engine.model.api.Clause;
import fr.inria.edelweiss.engine.model.api.Query;
import fr.inria.edelweiss.engine.model.api.Rule;

public class RuleImpl implements Rule {
	private static Logger logger = Logger.getLogger(RuleImpl.class);
	private static int count = 0;

	private ASTQuery ruleInstance;
	private Query body;
	private List<Clause> head;
	
	private int id = 0;
	
	public RuleImpl(){
		id = count++;
	}
	
	public int getID(){
		return id;
	}

	public ASTQuery getRuleInstance() {
		return ruleInstance;
	}

	public void setRuleInstance(ASTQuery ruleInstance) {
		this.ruleInstance = ruleInstance;
	}
	
	public String toString(){
		return ruleInstance.toString();
	}
	
	/**
     * rule = construct {?a grandFather ?c} where {?a father ?b . ?b father ?c}
     * RETURNS the query (set of clauses) : ?a father ?b . ?b father ?c
     */
	public Query getBody() {
		if (body == null){
			//create the query containing the body of the rule
			body = new QueryImpl(ruleInstance);
		}
		return body;
	}

	/**
     * rule = construct {?a grandFather ?c} where {?a father ?b . ?b father ?c}
     * RETURNS the clause : ?a grandFather ?c
     */
	public List<Clause> getHead() {
		if (head == null){

			//recover the head of the rule as an expression (which is a set of expressions) 
			Exp headRule=ruleInstance.getHead();

			//list to contain the clauses of the conclusion of the rule
			head = new ArrayList<Clause>();

			getTriples(headRule, head);
		}
		return head;
	}
	
	
	void getTriples(Exp head, List<Clause> list){
		for (Exp exp : head.getBody()){
			if (exp.isTriple()){
				//create the clause which contain the head of the rule
				list.add(ClauseImpl.conclusion(ruleInstance, exp));
			}
			else if (exp.isRDFList()){
				getTriples(exp, list);
;			}
			else {
				logger.error("** Parsing, not a triple: " + exp.getClass().getName());
				logger.error("** " + exp);
			}
		}
	}
	
	
	/**
     * clause: John grandFather ?y
     * bind: {}
     * case rule = construct {?a grandFather ?c} where {?a father ?b . ?b father ?c}
     * RETURNS : ?a grandFather ?c
     */
	public List<Clause> match(Clause clause, Bind bind) { 
		
		ArrayList<Clause> list = new ArrayList<Clause>() ;
		
		//iterate the list of clauses of the conclusion of the rule
		for (Clause clauseRule : getHead()){
			if (bind.match(clauseRule, clause)){
				list.add(clauseRule);
			}
		}

		return list;
	}
	

}
