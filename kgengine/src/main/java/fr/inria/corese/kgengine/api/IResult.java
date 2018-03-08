package fr.inria.corese.kgengine.api;

import fr.inria.corese.sparql.api.IDatatype;


/**
 * This interface provides methods to handle results<br />
 * 
 * @author Virginie Bottollier
 */

public interface IResult {
	
	public Iterable<String> getVariables();
	
	public boolean includes(IResult r);
	
	public boolean matches(IResult r);
	
	
	/**
	 * 
	 * @param variableName the name of the variable
	 * @return a IResultValue which corresponds to values of this variable in this result (note: in 
	 * case of a group, we return the first result value)
	 */
	public IResultValue getResultValue(String variableName);
	
	public IDatatype getDatatypeValue(String variableName);
	
	public void setResultValue(String variableName, IResultValue value);

	/**
	 * 
	 * @param variableName the name of the variable
	 * @return an array of IResultValue which corresponds to values of this variable in this result 
	 * (note: when results are grouped, there may be several values for one variable, that is why we 
	 * provide an array)
	 * without duplicate
	 */
	public IResultValue[] getResultValues(String variableName);

	// with duplicates
	public IResultValue[] getAllResultValues(String variableName);

	/**
	 * 
	 * @param variableName the name of the variable
	 * @return an array of String which corresponds to labels of values of this variable in this result 
	 * (note: when results are grouped, there may be several values for one variable, that is why we 
	 * provide an array)
	 */
	public String[] getStringValues(String variableName);
	
	/**
	 * 
	 * @param variableName the name of the variable
	 * @return a String which corresponds to the label of value of this variable in this result (note: 
	 * in case of a group, we return the first result value)
	 */
	public String getStringValue(String variableName);

	/**
	 * 
	 * @param variableName the name of the variable
	 * @return an array of String which corresponds the SPARQL value of this variable 
	 * in this result (note: when results are grouped, there may be several values for one 
	 * variable, that is why we provide an array)
	 */
	public String[] getSPARQLValues(String variableName);
	
	/**
	 * @return a number between 0 and 1 to measure the similarity in approximate search mode (1 means
	 * the perfect matches)
	 */
	public double getSimilarity();
	
	/**
	 * @return a boolean to indicate if the given variable has a value in the result
	 */
	public boolean isBound(String var);
	
}
