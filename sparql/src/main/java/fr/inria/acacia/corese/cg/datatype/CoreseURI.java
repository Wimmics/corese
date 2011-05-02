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
 * An implementation of the xsd:anyURI datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseURI extends CoreseResource {
    static int  code=URI;
	//private Object object; // to store an object such as an XML document


  public CoreseURI(String value) {
      super(value);
  }

  public boolean isURI() {
	return true;
  }

  public int getCode() {
    return code;
  }
  
  public int compare(IDatatype iod) throws CoreseDatatypeException {
	  return iod.polyCompare(this);
  }
  
  public int polyCompare(CoreseURI icod) throws CoreseDatatypeException {
	  return   icod.intCompare(this);
  }

  public boolean less(IDatatype iod) throws CoreseDatatypeException {
      return iod.polymorphGreater(this);
    }

    public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
      return iod.polymorphGreaterOrEqual(this);
    }

    public boolean greater(IDatatype iod) throws CoreseDatatypeException {
      return iod.polymorphLess(this);
    }

    public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
      return iod.polymorphLessOrEqual(this);
    }

    public boolean equals(IDatatype iod) throws CoreseDatatypeException{
      return  iod.polymorphEquals(this);
    }

//    public IDatatype plus(IDatatype iod) {
//        return iod.polyplus(this);
//      }
//      
//      public IDatatype minus(IDatatype iod) {
//          return iod.polyminus(this);
//        }
//  
  
    
    public boolean polymorphEquals(CoreseURI icod) throws CoreseDatatypeException {
       // boolean b= getValue().compareTo(icod.getValue()) == 0;
        boolean b= getValue().equals(icod.getValue());

        return b;
      }


    public boolean polymorphGreaterOrEqual(CoreseURI icod) throws
        CoreseDatatypeException {
      return intCompare(icod) >= 0;
    }

  public boolean polymorphGreater(CoreseURI icod)
      throws CoreseDatatypeException {
    return intCompare(icod) > 0;
  }

  public boolean polymorphLessOrEqual(CoreseURI icod)
      throws CoreseDatatypeException {
    return intCompare(icod) <= 0;
  }

  public boolean polymorphLess(CoreseURI icod)
   throws CoreseDatatypeException {
    return intCompare(icod) < 0;
  }

 



}
