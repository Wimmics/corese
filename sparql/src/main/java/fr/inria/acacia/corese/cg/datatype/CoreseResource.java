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
 * Root of URI and BlankNode
 * <br>
 */

public class CoreseResource extends CoreseStringableImpl {
    static int  code=URI;
    static final CoreseURI datatype=new CoreseURI(RDF.RDFSRESOURCE);

  //boolean generic=false; // if blank node URI

  public CoreseResource(String value) {
      super(value);

  }

// URI and Blank have no lang, hence return null
  public IDatatype getDataLang() {
       return null;
     }

     public boolean isTrue() throws CoreseDatatypeException {
         throw failure();
       }

       public boolean isTrueAble() {
         return false;
       }
       
  

       public boolean isLiteral() {
    	   return false;
       }
       
       /**
        * SPARQL fails because URI have no datatype
        */
       public IDatatype getDatatype(){
    	   return null;
       }
       
       public IDatatype getIDatatype(){
   		 return datatype;
       }
       
       public  int getCode(){
    	   return code;
       }
       



}
