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
 * Root of URI and BlankNode
 * <br>
 */

public class CoreseResource extends CoreseStringableImpl {
    static int  code=URI;
    static final CoreseURI datatype=new CoreseURI(RDF.RDFSRESOURCE);

  public CoreseResource(String value) {
      super(value);

  }

// URI and Blank have no lang, hence return null
    @Override
  public IDatatype getDataLang() {
       return null;
     }

    @Override
     public boolean isTrue() throws CoreseDatatypeException {
         throw failure();
       }

    @Override
       public boolean isTrueAble() {
         return false;
       }
       
    @Override
    public boolean booleanValue() {
        return false;
    }

    @Override
       public boolean isLiteral() {
    	   return false;
       }
       
       /**
        * SPARQL fails because URI have no datatype
        */
    @Override
       public IDatatype getDatatype(){
    	   return null;
       }
       
//    @Override
//       public IDatatype getIDatatype(){
//   		 return datatype;
//       }
       
    @Override
       public  int getCode(){
    	   return code;
       }
       



}
