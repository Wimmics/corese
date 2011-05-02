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
 * An implementation of the xsd:string datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseString extends CoreseStringLiteral { //CoreseStringableImpl{
  static int code=STRING;
  static final CoreseURI datatype=new CoreseURI(RDF.xsdstring);


  public CoreseString() {}

  public CoreseString(String value) {
      super(value);

  }
  
  public static CoreseString create(String str){
	  return new CoreseString(str);
  }

  public IDatatype getDatatype(){
       return datatype;
     }

  public int getCode() {
    return code;
  }

 
  public boolean equals(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polymorphEquals(this);
  }
  
  public boolean polymorphEquals(CoreseString icod) throws CoreseDatatypeException {
	  boolean res =   getValue().compareTo(icod.getValue()) == 0;
	  return res;
  }
  
  public boolean polymorphEquals(CoreseLiteral icod) throws CoreseDatatypeException {
	 return icod.polymorphEquals(this);
  }
  


}