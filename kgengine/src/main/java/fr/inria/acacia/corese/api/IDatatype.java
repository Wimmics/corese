package fr.inria.acacia.corese.api;

import fr.inria.acacia.corese.cg.datatype.ICoresePolymorphDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * This is an interface for all Corese datatypes.<br />
 * 
 * @author  Olivier Corby
 * This is an interface for all xsd:datatypes: each has a normalized label and a lower case label,
 * that are comparable with an other datatype(instance). Each can also have a value space (which is
 * a string or not and so allow regular expression matching) that have an order relation.
 * <br>
 * Here are the hierarchy of datatypes in Corese:<br />
 * <pre>
 * IDatatype
 * 	ICoreseDatatype
 * 		CoreseDatatype
 * 			CoreseDate
 * 			CoreseNumber
 * 				CoreseDouble
 * 					CoreseDecimal
 * 					CoreseFloat
 * 				CoreseLong
 * 					CoreseInteger
 * 			CoreseStringableImpl
 * 				CoreseResource
 * 					CoreseBlankNode
 * 					CoreseURI
 * 				CoreseStringableLiteral
 * 					CoreseBoolean
 * 					CoreseLiteral
 * 					CoreseString
 * 						CoreseStringCast
 * 					CoreseUndefLiteral
 * 					CoreseXMLLiteral
 * </pre>
 * @author Olivier Savoie & Olivier Corby & Virginie Bottollier
 */
public interface IDatatype 
	extends ICoresePolymorphDatatype
	//, IResultValue 
	{

	/**
	 * @return true if we have a blanknode
	 */	
	public boolean isBlank();
	
	public boolean isXMLLiteral();	
	
	public boolean isArray();
	
	public IDatatype[] getValues();
	
	public IDatatype get(int n);
	
	public int size();
	
	//public Constant getConstant();
	
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
	 * @return the normalized formatted string depending on the datatype <br>
	 *         representing the value of this
	 */
	public String getNormalizedLabel();
	
	/**
	 * @return the Sparql form of the datatype
	 */
	public String toSparql();
	
	/**
	 * @return true if we have an URI
	 */
	public boolean isURI();
	
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
	 * note: pequals correponds to the SPARQL keyword "sameTerm" (if an error is produced by the function "equals", the return value will be "false")
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
	 * @return the value of this as an integer
	 * @deprecated <i>use getIntegerValue instead</i>
	 */
	public int getiValue();
	
	/**
	 * @return the value of this as a double
	 * @deprecated <i>use getDoubleValue instead</i>
	 */
	public double getdValue();
	
	/**
	 * @return the datatype of this
	 */
	public IDatatype getDatatype();
	
	// same as getDatatype but return rdfs:Literal for literal with lang
	public IDatatype getExtDatatype();
	
	// same as getExtDatatype but URI return rdfs:Resource
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
	
	public String getLowerCaseLabel();

	/**
	 * @return true if this instance class is a number
	 */
	public boolean isNumber();

	/**
	 * @return the value of this as a double
	 */
	public double getDoubleValue();
	
	/**
	 * @return the value of this as an integer
	 */
	public int getIntegerValue();
	

}
