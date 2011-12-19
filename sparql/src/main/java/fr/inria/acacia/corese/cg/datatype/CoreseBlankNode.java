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
 * An implementation of the xsd:anyURI datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseBlankNode extends CoreseResource {
	static int  code=BLANK;
	
	
	public CoreseBlankNode(String value) {
		super(value);
	}
	
	public int getCode() {
		return code;
	}
	
	public boolean isBlank() {
		return true;
	}
	
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel());
		}
		throw failure();
		//return iod.polyCompare(this);
	}
		
	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) < 0;
		}
		throw failure();
	}
	
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) <= 0;
		}
		throw failure();
	}
	
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) > 0;
		}
		throw failure();
	}
	
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		switch (iod.getCode()){
		case BLANK: return getLabel().compareTo(iod.getLabel()) >= 0;
		}
		throw failure();
	}
	
	public boolean equals(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		case BLANK: return getLabel().equals(iod.getLabel()) ;
		}
		return false;
	}
	

	
}
