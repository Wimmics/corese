package fr.inria.corese.engine.model.core;

import java.util.List;

import fr.inria.acacia.corese.triple.api.ElementClause;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.Exp;
import fr.inria.acacia.corese.triple.parser.Expression;
import fr.inria.acacia.corese.triple.parser.Term;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.acacia.corese.triple.parser.Variable;
import fr.inria.corese.engine.model.api.Bind;
import fr.inria.corese.engine.model.api.Clause;
import fr.inria.corese.engine.model.api.ExpFilter;

public class ExpFilterImpl implements ExpFilter {

	private Triple filter; // a Triple that contains a filter

	
	/**
	 * constructor instantiating the attributes of the instance of the filter
	 */
	public ExpFilterImpl(Exp filter) {
		this.filter = filter.getTriple();
	}

	public Triple createFilter(Bind bind){
		//convert the filter as an expression
		Expression expression=create(bind);
		//System.out.println("** EF: " + createFilter2(bind, exp));
		return Triple.create(expression);
	}
	
	public static Triple createEQ(Variable var, Constant cst){
		Expression term = Term.create(Term.SEQ, var, cst);
		Triple triple = Triple.create(term);
		return  triple;
	}
	
	public Expression create(Bind bind){
		return createFilter(bind, filter.getExp()) ;
	}
	
	
	
	
	public Expression getExpression(){
		return filter.getExp();
	}



	/**
	 * Return a copy of the expression with variable replaced by its value
	 * in Bind
	 */
	public Expression createFilter(Bind bind, Expression exp){

		if (exp.isTerm()){
			Term term;
			
			if (exp.isFunction()){
				//case the term is a function
				term = Term.function(exp.getLabel());
			}
			else {
				term = Term.create(exp.getName());
			}
			
			for (Expression arg : exp.getArgs()){
				Expression aa = createFilter(bind, arg);
				term.add(aa);
			}
			return term;
		}
		else if (exp.isVariable() && bind.hasVariable(exp.getName())){
			Constant result = bind.getValue(exp.getName());
			return result;
		}
		
		// free variable or constant
		return exp;
	}
	
	
	void getVariables(Expression exp, Bind bind, List<Variable> vars){
		if (exp.isVariable()){
			Variable var = (Variable) exp;
			if (bind.hasVariable(var.getName())){
				if (! vars.contains(var)) vars.add(var);
			}
		}
		else if (exp.isTerm()){
			for (Expression arg : exp.getArgs()){
				getVariables(arg, bind, vars);
			}
		}
	}
	
	
	

	public boolean isCorresponding(Clause clause, Bind bind){
		return isCorresponding(clause, filter.getExp(), bind);
	}
		
	public boolean isCorresponding(Bind bind){
		return isCorresponding(null, filter.getExp(), bind);
	}
	
	
	public boolean isCorresponding(Clause clause, Expression exp, Bind bind){

		//initialize the result
		boolean result = true;

		if (exp.isTerm()){

			for (int i=0; (i<exp.getArity()) && result; i++){
				result = result && isCorresponding(clause, exp.getArg(i), bind);
			}

		}
		else if (exp.isVariable()){
			//case the expression is a variable
			if (bind.hasVariable(exp.getName())){ 	
				// variable is bound : OK
			}
			else if (clause == null){
				return false;
			}
			else {
				// variable not bound in current bind
				// does clause bind the variable ?
				result = false;

				for (ElementClause arg : clause){

					if (arg.isVariable() && arg.getName().equals(exp.getName())){
						// the clause binds the variable : OK
						//System.out.println(arg.getName() + " " + exp.getName());
						return  true;
					}
				}
			}

		}

		return result;
	}



}
