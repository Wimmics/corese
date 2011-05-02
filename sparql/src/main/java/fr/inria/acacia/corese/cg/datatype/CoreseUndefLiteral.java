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
    static final Hashtable<String, CoreseURI> hdt = new Hashtable<String, CoreseURI>(); // datatype name -> CoreseURI datatype
    static final int code = UNDEF;

  public CoreseUndefLiteral(String value) {
      super(value);
  }

  public void setDatatype(String uri) {
    CoreseURI dt = (CoreseURI) hdt.get(uri);
    if (dt == null){
      dt = new CoreseURI(uri);
      hdt.put(uri, dt);
    }
    datatype = dt;
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
	  return iod.polymorphEquals(this);
  }
  
  public boolean polymorphEquals(CoreseUndefLiteral icod) throws CoreseDatatypeException {
	  check(icod);
	  boolean b =  getValue().compareTo(icod.getValue()) == 0;
	  // if same undef datatype but different labels
	  // cannot conclude they differ because we know nothing about 
	  // this datatype !!!
	  //if (!b && Corese.SPARQLCompliant) throw failure();
	  if (!b) throw failure();
	  return b;
	  
  }




}