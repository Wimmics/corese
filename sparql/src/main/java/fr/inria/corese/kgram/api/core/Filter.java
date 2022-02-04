package fr.inria.corese.kgram.api.core;

import fr.inria.corese.sparql.triple.parser.Expression;
import java.util.List;


/**
 * Interface of Filter that contains an evaluable expression
 * Filter (and Expr) api refer to sparql.triple.parser.Expression
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public interface Filter {

	/**
	 * List of variable names contained in the filter
	 * 
	 * @return
	 */
	List<String> getVariables();
	List<String> getVariables(boolean excludeLocal);
	
	/**
	 * Evaluable expression processed by KGRAM generic Interpreter
         * Expr api refer also to sparql.triple.parser.Expression
	 * 
	 * @return
	 */
	Expr getExp();
        Expression getFilterExpression();

	/**
	 * Does filter contain a bound() function
	 * 
	 * @return
	 */
	boolean isBound();
	
	/**
	 * Is it an aggregate function such as count() min() sum()
	 * 
	 * @return
	 */
	boolean isAggregate();
	
	boolean isRecAggregate();

	boolean isFunctional();

        boolean isRecExist();

	
}
