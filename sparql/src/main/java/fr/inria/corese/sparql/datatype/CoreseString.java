package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

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

public class CoreseString extends CoreseStringLiteral { 
  static int code=STRING;
  static final CoreseURI datatype=new CoreseURI(RDF.xsdstring);


  public CoreseString() {}

  public CoreseString(String value) {
      super(value);

  }
  
  public static CoreseString create(String str){
	  return new CoreseString(str);
  }

  @Override
  public IDatatype getDatatype(){
       return datatype;
     }

  @Override
  public int getCode() {
    return code;
  }

 
  @Override
  public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
	  case STRING:  return getLabel().equals(iod.getLabel());
	  case LITERAL: return iod.equalsWE(this);
              
          //case UNDEF: 
	  case URI:
	  case BLANK: case TRIPLE: return false;
	  }
	  throw failure();
  }
  

}