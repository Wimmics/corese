package fr.inria.corese.kgram.api.core;

import java.util.List;


/**
 * Interface of Filter that contains an evaluable expression
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
	 * 
	 * @return
	 */
	Expr getExp();

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
