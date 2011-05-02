package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:integer datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseInteger extends CoreseLong {
  static final CoreseURI datatype=new CoreseURI(RDF.xsdinteger);
  public static final CoreseInteger ZERO = new CoreseInteger(0);
  public static final CoreseInteger ONE =  new CoreseInteger(1);
  public static final CoreseInteger TWO =  new CoreseInteger(2);
  public static final CoreseInteger THREE = new CoreseInteger(3);
  public static final CoreseInteger FOUR = new CoreseInteger(4);
  public static final CoreseInteger FIVE = new CoreseInteger(5);
  

  public CoreseInteger(String value) {
    super(value);
  }

  public CoreseInteger(int value){
    super(value);
  }
  
  public CoreseInteger(long value){
	    super(value);
	  }

  public IDatatype getDatatype(){
       return datatype;
     }



  public String getNormalizedLabel(){
      return Long.toString(lvalue);
  }

  public static String getNormalizedLabel(String label){
	  if (label.startsWith("+")){
		  label = label.substring(1);
	  }
      return new Integer(label).toString();
  }

  public boolean isInteger(){
	  return true;
  }
  
  public IDatatype polyplus(CoreseLong iod) {
	  if (iod.isInteger()){
		  return new CoreseInteger(getlValue() + iod.getlValue());
	  }
	  return super.polyplus(iod);
  }
  
  public IDatatype polymult(CoreseLong iod) {
	  if (iod.isInteger()){
		  return new CoreseInteger(getlValue() * iod.getlValue());
	  }
	  return super.polymult(iod);
  }
  
  
  public IDatatype polyminus(CoreseLong iod) {
	  if (iod.isInteger()){
		  return new CoreseInteger(iod.getlValue() - getlValue()  );
	  }
	  return super.polyplus(iod);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}