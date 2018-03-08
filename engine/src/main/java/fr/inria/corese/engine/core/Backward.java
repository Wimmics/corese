package fr.inria.corese.engine.core;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

//import fr.inria.acacia.corese.event.Event;
//import fr.inria.acacia.corese.event.EventManager;
//import fr.inria.acacia.corese.event.RuleEvent;
import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.Clause;
import fr.inria.corese.engine.model.api.ExpFilter;
import fr.inria.corese.engine.model.api.LBind;
import fr.inria.corese.engine.model.api.Query;
import fr.inria.corese.engine.model.api.Rule;
import fr.inria.corese.engine.model.api.RuleBase;
import fr.inria.corese.engine.model.core.LBindImpl;
import fr.inria.corese.engine.tool.api.EventsTreatment;

public class Backward {
	private static Logger logger = LogManager.getLogger(Backward.class);

		/**
	     * ruleBase: { construct {?a grandFather ?c} where {?a father ?b . ?b father ?c} }
	     * eventBase: {John father Mark. Mark father Pierre}
	     */
	
		//the base of rules
	    private RuleBase ruleBase;
	   // private EventManager manager;
	    private EventsTreatment sparql;
	    

	    private Stack stack;
	    
	    private boolean 
	    	// sparql query for level = 0, i.e. for initial clause
	    	// we may skip sparql query for initial clause in some case
	    	// e.g. when clause is only defined by a proof
	    	hasQuery = true,
	    	hasEvent = false, 
	    	trace = !true, 
	    	debug = !true;
	    
	    int count = 0, loop;
	   
	    public Backward(RuleBase ruleBase, EventsTreatment proc) {
			this.ruleBase = ruleBase;
			sparql = proc;
			stack = new Stack();
		}
	    
//	    void setEventManager(EventManager em){
//	    	manager = em;
//			hasEvent = manager.handle(Event.RULE);
//	    }
	
		public LBind prove(Query query, Bind bind){
			debug = query.getASTQuery().isDebug();
			if (debug){
				System.out.println("** RuleBase: " );
				for (Rule rr : ruleBase){
					System.out.println(rr);
				}
				System.out.println("** end" );
			}
			return prove(query, query.getClauses(), bind, 0, 0);
		}
		
		int getLoop(){
			return loop;
		}
		
		/**
		 * Query is the AST of the clauses to prove
		 * clauses is the list of clause to prove
		 * bind is the current partial variable binding
		 * n is the index of current clause in the clauses list
		 * level is the recursive level of backward rule calls
		 * level = 0 means the initial clause to prove 
		 * 
		 *  TODO: 
		 *  compile filters only once for all bindings
		 *  when filter fails there is no event
		 *
		 */
		public LBind prove(Query query, List<Clause> clauses, 
				Bind bind, int n, int level){
			LBind lb;
			loop++;
			if (level > count){
				count = level;
				//System.out.println(level);
			}

			if (n == clauses.size()){
				// never happens
				lb = new  LBindImpl();
				lb.add(bind);
				return lb;
			}

			if (query.isSparql()){
				// this is a pure SPARQL query, no backward here:

//				if (hasEvent){ 
//					// ASTQuery is the Abstract Syntax Tree of the query
//					RuleEvent e = 
//						RuleEvent.create(Event.RULE_SPARQL, level, n, query.getASTQuery());
//					manager.send(e);		
//				}

				lb = sparql.searchTriples(query, clauses, bind);

				return lb;
			}
			
			
			if (query.getFilters().size() > 0 ){
				// test filters that would need only current bind and no clause

				if (! checkFilters(query, null, bind, level, n)){
					// a filter is not valid : fail right now
					return new LBindImpl();
				}
			}
			
			

			//Iteration of the clauses of the query
			Clause clause = clauses.get(n);
			

//			if (hasEvent){ 
//				// announce current clause
//				RuleEvent e = 
//					RuleEvent.create(Event.RULE_PROVE, level, n, clause.getTriple());
//				manager.send(e);		
//			}

			
			// search  triples matching the clause in the RDF store 
			// and check appropriate filters

			LBind lbQuery = sparql.searchTriples(query, clause, bind);
			
			if (debug){
				System.out.println("sparql: "+ clause + " level: " + level);
				System.out.println("bind: " + bind);
				System.out.println("result: " + lbQuery);
				System.out.println("__");
			}
			
//			if (hasEvent){ 
//				// we try to prove current clause by querying the graph
//				RuleEvent e = 
//					RuleEvent.create(Event.RULE_SPARQL, level, n, clause.getTriple());
//				e.addObject(lbQuery);
//				manager.send(e);		
//			}
			
			LBind lbProve;
									
			if (clause.isGround()){
				// e.g. clause = xxx rdf:type owl:TransitiveProperty
				// no backward for this clause, search only in RDF store
				lbProve = lbQuery;
				lbQuery = new LBindImpl();
			}
			else {
				// prove clause using the backward algorithm
					lbProve = backward(clause, bind, level, n);
			}

			// now we check filters that concern current clause in backward
			if (query.getFilters().size() > 0 ){

				for (Bind bProve : lbProve){	
					
					if (checkFilters(query, clause, bProve, level, n)){
						lbQuery = lbQuery.union(bProve);
					}
				}
				
				lb = lbQuery;
			}
			else {								
				//union of the two lists of binds above, SPARQL and backward
				lb = lbQuery.union(lbProve);
			}
			
			if (debug){
				System.out.println("union: sparql: " + lbQuery);
				System.out.println("union: prove: " + lbProve);
				System.out.println("union: " + lb);
			}


//			if (hasEvent){ 
//				// we have tried to prove current clause
//				RuleEvent e = 
//					RuleEvent.create(Event.RULE_PROVED, level, n, clause.getTriple());
//				// current bindings
//				e.addObject(lb);
//				manager.send(e);		
//			}

			n++;

			if (n < clauses.size()){
				// yet another clause

				LBind lbRec = new LBindImpl();

				for (Bind b : lb){
					// for each binding, 
					// call the function prove with the rest of the query  
					LBind lbRecProve = prove (query, clauses, b, n, level);

					lbRec = lbRec.union(lbRecProve);
				}

				//recover the lasts bindings
				lb = lbRec;
			}


			return lb;
		}	

		
		boolean checkFilters(Query query, Clause clause, Bind bind, int level, int n){
			for (ExpFilter filter: query.getFilters()){

				if (filter.isCorresponding(clause, bind)){
					// filter share variables with clause: evaluate it
					if (! sparql.test(query, filter, bind, level, n)){
						return false;						
					}
				}
			}
			return true;
		}

		
		
		boolean isPropertyVariable(Clause clause, Bind bind){
			ElementClause property = clause.get(Clause.PROPERTY);
			if (property.isVariable() &&
					! bind.hasVariable(property.getName())){
				return true;
			}
			else return false;
		}


        /**
         * clause: John grandFather ?y
         * bind: {}
         * case rule = construct {?a grandFather ?c} where {?a father ?b . ?b father ?c} 
         * rule.match(clause,bind) -> true 
         * bind.get(rule.getHead(), clause) -> {?a = John}
         * lb1.rename(rule.getHead(), clause) : rename ?c into ?y in the list of binds lb1 
         */

        public LBind backward(Clause clause, Bind bind, int level, int n){
        	
            LBind lb1 = null, lb = new LBindImpl();
            
            //iteration of the base of rules
            for (Rule rule : ruleBase){
           	
            	// draft to prevent loop 
            	if (stack.contains(rule, clause, bind)){
            		continue;
            	}
            	

            	// get  clauses of  rule matching the clause of the query
            	List<Clause> list = rule.match(clause, bind);

            	for (Clause clauseRule : list){
            		
                	// rule match the clause,  considering the bind
            		
//            		if (hasEvent){
//                		RuleEvent e = 
//                			RuleEvent.create(Event.RULE_BACK, level, n, clause.getTriple());
//                		e.addObject(rule.getRuleInstance());
//                		e.addObject(bind);
//                		manager.send(e);
//                	}
                	           		           		
                	// call the method prove with the premises of the rule 
                	// if clause variables are bound
            		// generate a new bind were bindings are attached to the
            		// rule clause variables 
            		// (rename clause variables into rule variables)
            		
            		//if (debug) System.out.println("back unify");
            		
            		Bind unified =  bind.unify(clauseRule, clause);
            		
            		if (debug){
//            			System.out.println("back unify: " + clauseRule);
//            			System.out.println("back unify: " + clause);
//            			System.out.println("back unify: " + unified);
            		}

            		if (unified != null){
                   		stack.push(rule, clause, clauseRule, bind);

                   		if (debug){
                   			//System.out.println(level + ": " + rule.getID() + " " + clauseRule.getTriple() + " " + clause + " " + unified);
                   			System.out.println("prove: " + clause );
                   			System.out.println("with:  " + clauseRule.getTriple() + " " + unified);
                   			System.out.println(stack);
                   			watch();
                   		}
                   	 
            			lb1 = prove(rule.getBody(), rule.getBody().getClauses(), 
            					unified, 0, level+1);

            			stack.pop();

            			/*	
            			 *	result : list of binds renaming the variables of the binds to the variables of the clause
            			 *	corresponding to these variables in the rule's conclusion, for example : rename ?c into ?y	
            			 *			
            			 * TODO: 
            			 * clause = ?x R ?x ; clauseRule = ?y R ?z
            			 * check that ?y = ?z in the bind
            			 * 
            			 */
            			
            			if (debug){
                   			System.out.println("back result: " + lb1);
            			}
            			
            			lb = lb.union(lb1.rename(clauseRule, clause, bind));
            			
            			if (debug){
                   			System.out.println(clauseRule);
                 			System.out.println(clause);
                			System.out.println("back union: " + lb);
            			}
            			
            		}
                  
                }
            }
            return lb;
        }
        
        void watch(){
        	try {
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
      
}

