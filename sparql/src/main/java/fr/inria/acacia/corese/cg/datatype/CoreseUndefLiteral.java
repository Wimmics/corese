package fr.inria.acacia.corese.cg.datatype;

import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * This is used for unknown literals that carry their own datatype
 * <br>
 * @author Olivier Corby
 */

public class CoreseUndefLiteral extends CoreseStringLiteral {
    CoreseURI datatype=null ;
    static final int code = UNDEF;

  public CoreseUndefLiteral(String value) {
      super(value);
  }

  public void setDatatype(String uri) {
    datatype = getGenericDatatype(uri);
  }

  public int getCode() {
   return code;
 }


  public IDatatype getDatatype(){
    return datatype;
  }

  public boolean isTrue() throws CoreseDatatypeException {
      throw failure();
    }

    public boolean isTrueAble() {
      return false;
    }
	
  void check(IDatatype iod) throws CoreseDatatypeException {
      if (getDatatype() != iod.getDatatype()){
      	throw failure(); 
      }
  }
  
  public boolean equals(IDatatype iod) throws CoreseDatatypeException {
	  if (! iod.isLiteral()) return false; 
	  check(iod);
	  boolean b =  getLabel().equals(iod.getLabel());
	  if (!b) throw failure();
	  return b;

  }
  

}