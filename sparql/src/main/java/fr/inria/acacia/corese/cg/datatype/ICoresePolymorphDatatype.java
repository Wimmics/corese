package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This is an interface to add method needed to simulate method calls with polymorphism.<br>
 * This is for an internal use, it may be not public.
 * <br>
 * <br>
 * Note: methods use <b>polymorphism</b>. <br />
 * For example, to do "5(long) + 4.2(double)" we do: <br />
 * <ul>
 * 	<li>as 5 is a long, we call in CoreseLong: (5).plus(4.2) which calls (4.2).polyplus(5)</li>
 * 	<li>as 4.2 is a double, we call in CoreseDouble: (4.2).polyplus(5) which returns 9.2</li>
 * </ul>  
 * @author Olivier Savoie
 */

public interface ICoresePolymorphDatatype {
	
	int getCode();
	
	boolean semiEquals(IDatatype iod); // get rid of @ lang

	/**
	 * @return the string in lower case depending on the datatype
	 * <br>representing the value of this
	 */
	String getLowerCaseLabel();
	
	boolean hasLang();
	
	boolean isTrue() throws CoreseDatatypeException;
	
 	boolean isTrueAble();
 	
	void setBlank(boolean b);
	
	void setDatatype(String uri);
	
	void setValue(String str);
	
	void setValue(IDatatype dt);

	void setLang(String str);

	boolean compatible(IDatatype dt);

	long getlValue();
	

	
	int polyCompare(CoreseURI icod) throws CoreseDatatypeException ;

	int polyCompare(CoreseBlankNode icod) throws CoreseDatatypeException ;

	int polyCompare(CoreseLong icod) throws CoreseDatatypeException ;

	int polyCompare(CoreseDouble icod) throws CoreseDatatypeException ;

	int polyCompare(CoreseStringLiteral icod) throws CoreseDatatypeException ;

	int polyCompare(CoreseDate icod) throws CoreseDatatypeException ;

	/**
	 *  
	 * @param icod CoreseDouble
	 * @return this.getdValue() >= icod.getdValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphGreaterOrEqual(CoreseDouble icod) throws CoreseDatatypeException ;
	
	/**
	 * 
	 * @param icod CoreseDouble
	 * @return this.getdValue() > icod.getdValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphGreater(CoreseDouble icod) throws CoreseDatatypeException ;
	
	/**
	 * 
	 * @param icod CoreseDouble
	 * @return this.getdValue() <= icod.getdValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphLessOrEqual(CoreseDouble icod)throws CoreseDatatypeException ;
	
	/**
	 * 
	 * @param icod CoreseDouble
	 * @return this.getdValue() < icod.getdValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphLess(CoreseDouble icod) throws CoreseDatatypeException;
	
	/**
	 * 
	 * @param icod CoreseDouble
	 * @return this.getdValue() == icod.getdValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphEquals(CoreseDouble icod)throws CoreseDatatypeException;
	
	/**
	 * 
	 * @param iod CoreseDouble
	 * @return this.getdValue() + icod.getdValue()
	 */
	IDatatype polyplus(CoreseDouble iod);
	
	/**
	 * 
	 * @param iod CoreseDouble
	 * @return this.getdValue() - icod.getdValue()
	 */
	IDatatype polyminus(CoreseDouble iod);
	
	/**
	 * 
	 * @param iod CoreseDouble
	 * @return this.getdValue() * icod.getdValue()
	 */
	IDatatype polymult(CoreseDouble iod);
	
	/**
	 * 
	 * @param iod CoreseDouble
	 * @return this.getdValue() / icod.getdValue()
	 */
	IDatatype polydiv(CoreseDouble iod);
	

	/*---------------------------------------------------------------------------------*/
	/*---------------------------LONG--------------------------------------------------*/
	/*---------------------------------------------------------------------------------*/

	/**
	 * 
	 * @param icod CoreseLong
	 * @return this.getlValue() >= icod.getlValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphGreaterOrEqual(CoreseLong icod) throws CoreseDatatypeException ;
	
	/**
	 * 
	 * @param icod CoreseLong
	 * @return this.getlValue() > icod.getlValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphGreater(CoreseLong icod) throws CoreseDatatypeException ;

	/**
	 * 
	 * @param icod CoreseLong
	 * @return this.getlValue() <= icod.getlValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphLessOrEqual(CoreseLong icod) throws CoreseDatatypeException;
	
	/**
	 * 
	 * @param icod CoreseLong
	 * @return this.getlValue() < icod.getlValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphLess(CoreseLong icod) throws CoreseDatatypeException;
	
	/**
	 * 
	 * @param icod CoreseLong
	 * @return this.getlValue() == icod.getlValue()
	 * @throws CoreseDatatypeException
	 */
	boolean polymorphEquals(CoreseLong icod)throws CoreseDatatypeException;

	/**
	 * 
	 * @param iod CoreseLong
	 * @return this.getlValue() + icod.getlValue()
	 */
	IDatatype polyplus(CoreseLong iod);
	
	/**
	 * 
	 * @param iod CoreseLong
	 * @return this.getlValue() - icod.getlValue()
	 */
	IDatatype polyminus(CoreseLong iod);

	/**
	 * 
	 * @param iod CoreseLong
	 * @return this.getlValue() * icod.getlValue()
	 */
	IDatatype polymult(CoreseLong iod);
	
	/**
	 * 
	 * @param iod CoreseLong
	 * @return this.getlValue() / icod.getlValue()
	 */
	IDatatype polydiv(CoreseLong iod);


	/*---------------------------------------------------------------------------------*/
	/*---------------------------STRINGABLE--------------------------------------------*/
	/*---------------------------------------------------------------------------------*/

	boolean polymorphEquals(CoreseUndefLiteral icod)throws CoreseDatatypeException;

	boolean polymorphEquals(CoreseString icod)throws CoreseDatatypeException;
  
	boolean polymorphEquals(CoreseBoolean icod)throws CoreseDatatypeException;
  
	boolean polymorphEquals(CoreseLiteral icod)throws CoreseDatatypeException;
  
	boolean polymorphEquals(CoreseXMLLiteral icod)throws CoreseDatatypeException;
	
  
	boolean polymorphGreaterOrEqual(CoreseStringLiteral icod) throws CoreseDatatypeException;

	boolean polymorphGreater(CoreseStringLiteral icod) throws CoreseDatatypeException ;

	boolean polymorphLessOrEqual(CoreseStringLiteral icod) throws CoreseDatatypeException;
 
	boolean polymorphLess(CoreseStringLiteral icod) throws CoreseDatatypeException;
	

	IDatatype polyplus(CoreseStringableImpl icod);

	IDatatype polyminus(CoreseStringableImpl icod);
  
	/*---------------------------URI---------------------------*/
  
	boolean polymorphGreaterOrEqual(CoreseURI icod) throws CoreseDatatypeException;

	boolean polymorphGreater(CoreseURI icod) throws CoreseDatatypeException ;
  
	boolean polymorphLessOrEqual(CoreseURI icod) throws CoreseDatatypeException;
  
	boolean polymorphLess(CoreseURI icod) throws CoreseDatatypeException;
  
	boolean polymorphEquals(CoreseURI icod)throws CoreseDatatypeException;
  
	/*---------------------------BlankNode---------------------------*/
  
	boolean polymorphGreaterOrEqual(CoreseBlankNode icod) throws CoreseDatatypeException;

	boolean polymorphGreater(CoreseBlankNode icod) throws CoreseDatatypeException ;
  
	boolean polymorphLessOrEqual(CoreseBlankNode icod) throws CoreseDatatypeException;
  
	boolean polymorphLess(CoreseBlankNode icod) throws CoreseDatatypeException;
  
	boolean polymorphEquals(CoreseBlankNode icod)throws CoreseDatatypeException;
   

	/*---------------------------DATE---------------------------*/

	boolean polymorphGreaterOrEqual(CoreseDate icod) throws CoreseDatatypeException;

	boolean polymorphGreater(CoreseDate icod) throws CoreseDatatypeException ;

	boolean polymorphLessOrEqual(CoreseDate icod) throws CoreseDatatypeException;
	
	boolean polymorphLess(CoreseDate icod) throws CoreseDatatypeException;

	boolean polymorphEquals(CoreseDate icod) throws CoreseDatatypeException;

}