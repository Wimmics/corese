package fr.inria.acacia.corese.api;

import fr.inria.acacia.corese.cg.datatype.ICoresePolymorphDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * This is an interface for all Corese datatypes.<br />
 * 
 * This is an interface for all xsd:datatypes: each has a normalized label and a lower case label,
 * that are comparable with an other datatype(instance). Each can also have a value space (which is
 * a string or not and so allow regular expression matching) that have an order relation.
 * 
 * @author Olivier Savoie & Olivier Corby & Virginie Bottollier
 */
public interface IDatatype 
	extends ICoresePolymorphDatatype
	{
	
	static final int LITERAL =	0;
	static final int STRING =	1;
	static final int XMLLITERAL=2;
	static final int NUMBER =	3;
	static final int DATE =		4;
	static final int BOOLEAN =	5;
	static final int STRINGABLE=6;
	static final int URI = 		7;
	static final int UNDEF =	8;
	static final int BLANK =	9;
	
	static final int DOUBLE =	11;
	static final int FLOAT =	12;
	static final int DECIMAL =	13;
	static final int LONG =		14;
	static final int INTEGER =	15;
	
	// Pseudo codes (target is Integer or String ...)
	static final int DAY =		21;
	static final int MONTH =	22;
	static final int YEAR =		23;
	static final int DURATION =	24;
	static final int DATETIME =	25;


	/**
	 * @return true if we have a blanknode
	 */	
	public boolean isBlank();
	
	public boolean isXMLLiteral();	
	
	public boolean isArray();
	
	public IDatatype[] getValues();
	
	public IDatatype get(int n);
	
	public int size();
		
	/**
	 * @return true if we have a literal
	 */
	public boolean isLiteral();
	
	/**
	 * Compare 2 datatypes
	 * @param dt2 another datatype
	 * @return 0 if they are equals, an int > 0 if the datatype is greater than dt2, an int < 0 if
	 * the datatype is lesser 
	 */
	public int compareTo(IDatatype dt2);

	/**
	 * Cast a value
	 * @param target the java type (ex: xsd:integer)
	 * @param javaType the CoreseDatatype that corresponds (ex: CoreseInteger)
	 * @return the datatype casted
	 */
	public IDatatype cast(IDatatype target, IDatatype javaType);

	/**
	 * @return the lang as a datatype
	 */
	public IDatatype getDataLang();


	
	/**
	 * @return the Sparql form of the datatype
	 */
	public String toSparql();
	
	/**
	 * @return true if we have an URI
	 */
	public boolean isURI();
	
	// Used by XMLLiteral to store a XML DOM 
	public void setObject(Object obj);
	
	public Object getObject();

	/***************************************************************************/
	
	/**
	 * test if this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
	 *
	 * @param iod the instance to be tested with
	 * @return this.getLowerCaseLabel() contains iod.getLowerCaseLabel()
	 */
	public boolean contains(IDatatype iod);

	/**
	 * test if this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
	 *
	 * @param iod the instance to be tested with
	 * @return this.getLowerCaseLabel() starts with iod.getLowerCaseLabel()
	 */
	public boolean startsWith(IDatatype iod);

	/**
	 * test the equality (by value) between two instances of datatype class
	 *
	 * @param iod the instance to be tested with this
	 * @return true if the param has the same runtime class and if values are equals, else false
	 * note: equals correponds to the SPARQL equals, with type checking
	 */
	public boolean equals(IDatatype iod) throws CoreseDatatypeException;

	/**
	 * test the equality (by value) between two instances of datatype class
	 *
	 * @param iod the instance to be tested with this
	 * @return true if the param has the same runtime class and if values are equals, else false
	 */
	public boolean sameTerm(IDatatype iod);

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() < this.getValue()
	 * @throws CoreseDatatypeException
	 */
	public boolean less(IDatatype iod) throws CoreseDatatypeException;

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() <= to this.getValue()
	 * @throws CoreseDatatypeException
	 */
	public boolean lessOrEqual(IDatatype iod)
			throws CoreseDatatypeException;

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() > this.getValue()
	 * @throws CoreseDatatypeException
	 */
	public boolean greater(IDatatype iod) throws CoreseDatatypeException;

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() >= to this.getValue()
	 * @throws CoreseDatatypeException
	 */
	public boolean greaterOrEqual(IDatatype iod)
			throws CoreseDatatypeException;

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() + this.getValue()
	 */
	public IDatatype plus(IDatatype iod);

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() - this.getValue()
	 */
	public IDatatype minus(IDatatype iod);

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() * this.getValue()
	 */
	public IDatatype mult(IDatatype iod);

	/**
	 * 
	 * @param iod
	 * @return iod.getValue() / this.getValue()
	 */
	public IDatatype div(IDatatype iod);

	/***************************************************************************/
	
	
	/**
	 * @return the datatype of this
	 */
	public IDatatype getDatatype();
	

	
	// same as getDatatype but URI return rdfs:Resource
	public IDatatype getIDatatype();
	
	/**
	 * @return the lang of this ('fr', 'en',...)
	 */
	public String getLang();

	/**
	 * @return the datatype of this as a URI
	 */
	public String getDatatypeURI();

	/**
	 * @return the string depending on the datatype
	 * <br>representing the value of this
	 */
	public String getLabel();
	

	/**
	 * @return true if this instance class is a number
	 */
	public boolean isNumber();

	
	public double doubleValue();
	public float  floatValue();
	public long   longValue();
	public int    intValue();
	
	
	/***************************************************/

	@Deprecated
	public double getDoubleValue();
	@Deprecated
	public int getIntegerValue();
	@Deprecated
	public int getiValue();
	@Deprecated
	public double getdValue();
	
	@Deprecated
	public String getNormalizedLabel();
	@Deprecated 
	public IDatatype getExtDatatype();
	@Deprecated
	public String getLowerCaseLabel();

}
