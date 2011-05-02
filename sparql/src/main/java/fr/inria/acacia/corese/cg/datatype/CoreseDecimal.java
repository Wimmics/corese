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
	
	
	public IDatatype getDatatype(){
		return datatype;
	}
	
	public boolean isDecimal(){
		return true;
	}
	
	public IDatatype polyplus(CoreseDouble iod) {
		if (iod.isDecimal()){
			return new CoreseDecimal(getdValue() + iod.getdValue());
		} 
		else if (iod.isFloat()){
			return new CoreseFloat(getdValue() + iod.getdValue());
		}
		return super.polyplus(iod);
	}
	
	public IDatatype polyminus(CoreseDouble iod) {
		if (iod.isDecimal()){
			return new CoreseDecimal(iod.getdValue() - getdValue());
		} 
		else if (iod.isFloat()){
			return new CoreseFloat(iod.getdValue() - getdValue());
		}
		return super.polyminus(iod);
	}
	
	public IDatatype polymult(CoreseDouble iod) {
		if (iod.isDecimal()){
			return new CoreseDecimal(getdValue() * iod.getdValue());
		}
		else if (iod.isFloat()){
			return new CoreseFloat(getdValue() * iod.getdValue());
		}
		return super.polymult(iod);
	}
	
	public IDatatype polydiv(CoreseDouble iod) {
		if (iod.isDecimal()){
			return new CoreseDecimal(iod.getdValue() / getdValue());
		} 
		else if (iod.isFloat()){
			return new CoreseFloat(iod.getdValue() / getdValue());
		}
		return super.polydiv(iod);
	}
	
	
	public IDatatype polyplus(CoreseLong iod) {
		return new CoreseDecimal(getdValue() + iod.getdValue());
	}
	
	public IDatatype polyminus(CoreseLong iod) {
		return new CoreseDecimal(iod.getdValue() - getdValue());
	}
	
	
	
	
	
	
	
	
	
	
}