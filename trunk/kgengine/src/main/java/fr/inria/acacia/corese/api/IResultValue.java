package fr.inria.acacia.corese.api;



/**
 * This interface provides methods to handle values of results<br />
 * @author Virginie Bottollier
 */
public interface IResultValue {

	/**
	 * @return the string of this result value (an URI for a resource, a literal string for 
	 * a literal or a blank node id for a blank node)
	 */
	public String getStringValue();
	
	/**
	 * @return the datatype URI of this result value
	 */
	public String getDatatypeURI();
	
	public IDatatype getDatatypeValue();

	/**
	 * @return the lang of this result value if any, else null
	 */
	public String getLang();

	/**
	 * @return if this result value is a blank node
	 */
	public boolean isBlank();
	
	/**
	 * @return if this result value is a URI
	 */
	public boolean isURI();
	
	/**
	 * @return if this result value is a literal
	 */
	public boolean isLiteral();
	
	/**
	 * @return if this result value is a number
	 */
	public boolean isNumber();
	
	/**
	 * @return the integer value of this result value if it is an integer; else -1 
	 */
	public int getIntegerValue();
	
	/**
	 * @return the double value of this result value if it is a double, a float, a decimal or an integer; else -1
	 */
	public double getDoubleValue();
	
	public IPath getPath();
	
	public boolean isPath();
	
	public boolean isArray();
	
}
