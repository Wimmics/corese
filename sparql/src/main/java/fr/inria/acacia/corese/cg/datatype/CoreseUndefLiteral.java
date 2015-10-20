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
    static final int code = UNDEF;
    static final String FUTURE = "Future";
    
    CoreseURI datatype=null ;
    private Object object;
    private boolean isFuture = false;
    static final CoreseUndefLiteral ERROR, UNBOUND;
    
    static {
        ERROR   = new CoreseUndefLiteral("Error",   IDatatype.SYSTEM);
        UNBOUND = new CoreseUndefLiteral("Unbound", IDatatype.SYSTEM);
    }

  public CoreseUndefLiteral(String value) {
      super(value);
  }
  
   public CoreseUndefLiteral(String value, String dt) {
      super(value);
      setDatatype(dt);
  }
  
  public CoreseUndefLiteral() {
      super(FUTURE);
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
  
  public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
	  if (! iod.isLiteral()) return false; 
	  check(iod);
	  boolean b =  getLabel().equals(iod.getLabel());
	  if (!b) throw failure();
	  return b;

  }

    /**
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * @return the isFuture
     */
    public boolean isFuture() {
        return isFuture;
    }

    /**
     * @param isFuture the isFuture to set
     */
    public void setFuture(boolean isFuture) {
        this.isFuture = isFuture;
    }
  

}