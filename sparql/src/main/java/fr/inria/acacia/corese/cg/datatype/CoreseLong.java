package fr.inria.acacia.corese.cg.datatype;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:long datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseLong extends CoreseNumber {
	
	/** logger from log4j */
	private static Logger logger = Logger.getLogger(CoreseLong.class);
	
  static final CoreseURI datatype=new CoreseURI(RDF.xsdlong);
  long lvalue;

  // TODO: fix it
  public CoreseLong(String value) {
	  super(value.startsWith("+")?value.substring(1):value);
	  lvalue = Long.parseLong(value.startsWith("+")?value.substring(1):value);
  }
  

  public CoreseLong(long value) {
    super(value);
    lvalue = value;
 }

 public IDatatype getDatatype(){
      return datatype;
    }


  public long getlValue(){
    return lvalue;
  }
  
  public int getiValue(){
	    return (int)lvalue;
  }
  
  public boolean isStringable(){return false;}

  public boolean isOrdered(){ return true;}

  public boolean isRegExpable(){return false;}
  
  
  public int compare(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polyCompare(this);
  }
  
  public int polyCompare(CoreseDouble icod) throws CoreseDatatypeException {
	  double d1 = icod.getdValue();
	  return (d1 < dvalue) ? -1 : (d1 == dvalue ? 0 : 1);
  }
  
  public int polyCompare(CoreseLong icod) throws CoreseDatatypeException {
	  long d1 = icod.getlValue();
	  return (d1 < lvalue) ? -1 : (d1 == lvalue ? 0 : 1);
  }

  

  public boolean less(IDatatype iod) throws CoreseDatatypeException {
     return iod.polymorphGreater(this);
   }

   public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
     return iod.polymorphGreaterOrEqual(this);
   }

   public boolean greater(IDatatype iod) throws CoreseDatatypeException {
     return iod.polymorphLess(this);
   }

   public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
     return iod.polymorphLessOrEqual(this);
   }

   public boolean equals(IDatatype iod) throws CoreseDatatypeException{
	   return iod.polymorphEquals(this);
   }



   public IDatatype plus(IDatatype iod) {
        return iod.polyplus(this);
      }

      public IDatatype minus(IDatatype iod) {
        return iod.polyminus(this);
      }

      public IDatatype mult(IDatatype iod) {
        return iod.polymult(this);
      }

      public IDatatype div(IDatatype iod) {
    	return iod.polydiv(this);
      }



  public String getNormalizedLabel(){
    return Long.toString(lvalue);
  }

  public static String getNormalizedLabel(String label){
	  if (label.startsWith("+")){
		  label = label.substring(1);
	  }
      return new Long(label).toString();
  }

  public String getLowerCaseLabel(){
	  return Long.toString(lvalue);
  }

  public boolean polymorphGreaterOrEqual(CoreseDouble icod){
	  return dvalue >= icod.getdValue();
  }
  
  public boolean polymorphGreater(CoreseDouble icod){
	  return dvalue > icod.getdValue();
  }
  
  public boolean polymorphLessOrEqual(CoreseDouble icod){
	  return dvalue <= icod.getdValue();
  }
  
  public boolean polymorphLess(CoreseDouble icod){
	  return dvalue < icod.getdValue();
  }
  
  public boolean polymorphEquals(CoreseDouble icod){
	  return dvalue == icod.getdValue();
  }
  
  



  public boolean polymorphGreaterOrEqual(CoreseLong icod){
	  return lvalue >= icod.getlValue();
	  
  }
  
  public boolean polymorphGreater(CoreseLong icod){
	  return lvalue > icod.getlValue();
  }
  
  public boolean polymorphLessOrEqual(CoreseLong icod){
	  return lvalue <= icod.getlValue();
  }
  public boolean polymorphLess(CoreseLong icod){
	  return lvalue < icod.getlValue();
  }
  
  public boolean polymorphEquals(CoreseLong icod){
	  return lvalue == icod.getlValue();
  }



  
  public IDatatype polyplus(CoreseDouble iod) {
	  if (iod.isDecimal()){
		  return new CoreseDecimal(getdValue() + iod.getdValue());
	  }
	  if (iod.isFloat()){
		  return new CoreseFloat(getdValue() + iod.getdValue());
	  }
	  return new CoreseDouble(getdValue() + iod.getdValue());
  }
  
  public IDatatype polyminus(CoreseDouble iod) {
	  if (iod.isDecimal()){
		  return new CoreseDecimal(getdValue() - iod.getdValue());
	  }
	  if (iod.isFloat()){
		  return new CoreseFloat(getdValue() - iod.getdValue());
	  }
	  return new CoreseDouble(iod.getdValue() - getdValue());
  }

 public IDatatype polymult(CoreseDouble iod) {
   return new CoreseDouble(getdValue() * iod.getdValue());
 }

 /**
  * double/long : double
  * decimal/long : decimal
  */
 public IDatatype polydiv(CoreseDouble iod) {
	 if (iod.isDecimal()){
		 return new CoreseDecimal(iod.getdValue() / getdValue()); 
	 }
	 else if (iod.isFloat()){
		 return new CoreseFloat(iod.getdValue() / getdValue()); 
	 }
	 return new CoreseDouble(iod.getdValue() / getdValue());
 }

 

 public IDatatype polyplus(CoreseLong iod) {
   return new CoreseLong(getlValue() + iod.getlValue());
  }

  public IDatatype polyminus(CoreseLong iod) {
    return new CoreseLong(iod.getlValue() - getlValue());
  }

  public IDatatype polymult(CoreseLong iod) {
    return new CoreseLong(getlValue() * iod.getlValue());
  }

  public IDatatype polydiv(CoreseLong iod) {
	  try {
		  if (getdValue() == 0.0) {
			  return new CoreseDecimal(iod.getlValue()/0);
		  } 
		  else {
			  double d = iod.getdValue() / getdValue();
//			  double dd = Math.floor(d);
//			  if (dd == d){
//				  if (dd < Integer.MAX_VALUE){
//					  return new CoreseInteger((int)d) ;
//				  }
//				  else {
//					  return new CoreseLong((long)d) ;
//				  }
//			  }
			  return new CoreseDecimal(d);
		  }
	  } 
	  catch (java.lang.ArithmeticException ae) {
		  return null;
	  }
  }




}