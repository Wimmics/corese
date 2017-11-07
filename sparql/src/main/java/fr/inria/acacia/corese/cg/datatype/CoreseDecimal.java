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
 * An implementation of the xsd:decimal datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseDecimal extends CoreseDouble {
	static final CoreseURI datatype=new CoreseURI(RDF.xsddecimal);
	static final int code = DECIMAL;
        
        CoreseDecimal() {}
	
	public CoreseDecimal(String value) throws  CoreseDatatypeException {
		super(value);
		// no exponent in decimal:
		if (value.indexOf("e")!=-1 || value.indexOf("E")!=-1){
			throw new CoreseDatatypeException("Decimal", value);
		}
	}
	
	public CoreseDecimal(double value) {
		super(value);
	}
        
        public static CoreseDecimal create(double value) {
            CoreseDecimal dt = new CoreseDecimal();
            dt.setValue(value);
            return dt;
	}
	
	
        @Override
	public IDatatype getDatatype(){
		return datatype;
	}
	
        @Override
	 public int getCode(){
			return code;
		}
	
        @Override
	public boolean isDecimal(){
		return true;
	}
	
}