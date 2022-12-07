package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * Subsume String Literal XMLLiteral UndefLiteral and Boolean In Corese they
 * compare with <= < >= > but not with = !=
 */
public class CoreseURILiteral extends CoreseStringableImpl {

    static int code = URI_LITERAL;
    static final CoreseURI datatype = new CoreseURI(RDF.xsdanyURI);

    public CoreseURILiteral() {
    }

    public CoreseURILiteral(String value) {
        super(value);

    }

    @Override
    public IDatatype getDatatype() {
        return datatype;
    }
    
    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int compare(IDatatype dt) throws CoreseDatatypeException {
        switch (dt.getCode()) {
            case URI_LITERAL:
                return getLabel().compareTo(dt.getLabel());
        }
        throw failure();
    }
    
      @Override
  public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException {
	  switch (iod.getCode()){
            case URI_LITERAL:  return getLabel().equals(iod.getLabel());	  
	  }
	  throw failure();
  }

    @Override
    public boolean less(IDatatype dt) throws CoreseDatatypeException {
        switch (dt.getCode()) {
            case URI_LITERAL:
                return getLabel().compareTo(dt.getLabel()) < 0;
        }
        throw failure();
    }

    @Override
    public boolean lessOrEqual(IDatatype dt) throws CoreseDatatypeException {
        switch (dt.getCode()) {
            case URI_LITERAL:
                return getLabel().compareTo(dt.getLabel()) <= 0;
        }
        throw failure();
    }

    @Override
    public boolean greater(IDatatype dt) throws CoreseDatatypeException {
        switch (dt.getCode()) {
            case URI_LITERAL:
                return getLabel().compareTo(dt.getLabel()) > 0;
        }
        throw failure();
    }

    @Override
    public boolean greaterOrEqual(IDatatype dt) throws CoreseDatatypeException {
        switch (dt.getCode()) {
            case URI_LITERAL:
                return getLabel().compareTo(dt.getLabel()) >= 0;
        }
        throw failure();
    }

}
