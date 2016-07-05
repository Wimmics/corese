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
    @Override
  public void setDatatype(String uri) {
    datatype = getGenericDatatype(uri);
  }

    @Override
  public int getCode() {
   return code;
 }


    @Override
  public IDatatype getDatatype(){
    return datatype;
  }

    @Override
  public boolean isTrue() throws CoreseDatatypeException {
      throw failure();
    }

    @Override
    public boolean isTrueAble() {
      return false;
    }
	
  void check(IDatatype iod) throws CoreseDatatypeException {
      if (iod.getCode() == UNDEF && getDatatype() != iod.getDatatype()){
      	throw failure(); 
      }
  }
  
    @Override
    public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
        if (!iod.isLiteral()) {
            return false;
        }
        check(iod);
        if (getDatatype() != iod.getDatatype()) {
            return false;
        }
        boolean b = getLabel().equals(iod.getLabel());
        if (!b) {
            throw failure();
        }
        return b;

    }

    /**
     * @return the object
     */
    @Override
    public Object getObject() {
        return object;
    }

    /**
     * @param object the object to set
     */
    @Override
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * @return the isFuture
     */
    @Override
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