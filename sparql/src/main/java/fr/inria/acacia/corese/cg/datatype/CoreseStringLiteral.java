package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.storage.api.IStorage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * Subsume String Literal XMLLiteral UndefLiteral and Boolean
 * In Corese they compare with <= < >= >  but not with = !=
 */

public class CoreseStringLiteral extends CoreseStringableImpl{
  static int code=STRING;
  private static Logger logger = LogManager.getLogger(CoreseStringLiteral.class);
  
  private IStorage manager ;
  private int id;
        
  public CoreseStringLiteral() {}

  public CoreseStringLiteral(String value) {
      super(value);

  }
   
  @Override
  public int compare(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
	  case LITERAL:
	  case STRING:
	  case BOOLEAN:
	  case XMLLITERAL:
		  return getLabel().compareTo(iod.getLabel());
	  }
	  throw failure();
  }
  

  @Override
  public boolean less(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
	  case LITERAL:
	  case STRING:
	  case BOOLEAN:
	  case XMLLITERAL:
	  case UNDEF:
		  return getLabel().compareTo(iod.getLabel()) < 0;
	  }
	  throw failure();
  }
  
  @Override
  public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
	  switch (iod.getCode()){
	  case LITERAL:
	  case STRING:
	  case BOOLEAN:
	  case XMLLITERAL:
	  case UNDEF:
		  return getLabel().compareTo(iod.getLabel()) <= 0;
	  }
	  throw failure();
  }
  
  @Override
  public boolean greater(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
	  case LITERAL:
	  case STRING:
	  case BOOLEAN:
	  case XMLLITERAL:
	  case UNDEF:
		  return getLabel().compareTo(iod.getLabel()) > 0;
	  }
	  throw failure();
  }
  
  @Override
  public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
	  case LITERAL:
	  case STRING:
	  case BOOLEAN:
	  case XMLLITERAL:
	  case UNDEF:
		  return getLabel().compareTo(iod.getLabel()) >= 0;
	  }
	  throw failure();
  }
  
        @Override
        public void setValue(String str, int nid, IStorage mgr){
            if(str == null || str.isEmpty()) return;
            if(mgr == null){
                this.setValue(str);
                return;
            }
            
            this.setManager(mgr);
            this.id = nid;
            manager.write(this.id, str);
            this.value = "";
        }

        @Override
        public String getLabel(){
            if(manager == null){
               	return value;
            }else{
                String s = manager.read(this.id);
                if(s == null){
                    logger.error("Read string ["+id+"] from file error!");
                    return value;
                }
                return s;
            }
	}

    /**
     * @return the manager
     */
    public IStorage getManager() {
        return manager;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(IStorage manager) {
        this.manager = manager;
    }
}