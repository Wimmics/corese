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
		return iod.polyCompare(this);
	}
	
	public int polyCompare(CoreseBlankNode icod) throws CoreseDatatypeException {
		return   icod.intCompare(this);
	}
	
	public boolean less(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphGreater(this);
	}
	
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException{
		return iod.polymorphGreaterOrEqual(this);
	}
	
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphLess(this);
	}
	
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphLessOrEqual(this);
	}
	
	public boolean equals(IDatatype iod) throws CoreseDatatypeException{
		return  iod.polymorphEquals(this);
	}
	
	public boolean polymorphEquals(CoreseBlankNode icod) throws CoreseDatatypeException {
		boolean b= intCompare(icod) == 0;
		return b;
	}
	
	
	public boolean polymorphGreaterOrEqual(CoreseBlankNode icod) throws
	CoreseDatatypeException {
		return intCompare(icod) >= 0;
	}
	
	public boolean polymorphGreater(CoreseBlankNode icod)
	throws CoreseDatatypeException {
		return intCompare(icod) > 0;
	}
	
	public boolean polymorphLessOrEqual(CoreseBlankNode icod)
	throws CoreseDatatypeException {
		return intCompare(icod) <= 0;
	}
	
	public boolean polymorphLess(CoreseBlankNode icod)
	throws CoreseDatatypeException {
		return intCompare(icod) < 0;
	}
	
	
	
}
