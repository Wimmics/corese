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
 * An implementation of the xsd:literal datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseLiteral extends CoreseStringLiteral { //CoreseStringableImpl{
	static final CoreseURI datatype=new CoreseURI(RDF.RDFSLITERAL);
	static final int code=LITERAL;
	private CoreseString dataLang=null;

	public CoreseLiteral(String value) {
		super(value);

		dataLang = empty; // default is ""
	}

	/**
	 * Literal has no xsd:datatype
	 */
	public IDatatype getDatatype(){
		return null;
	}

	public IDatatype getExtDatatype(){
		return datatype;
	}


	public  int getCode(){
		return code;
	}

	public void setLang(String lang) {
		//lang=str;
		if (lang != null)
			dataLang=intGetDataLang(lang);
	}



	public String getLang(){
		if (dataLang == null) return null;
		else return dataLang.getValue();
	}

	public IDatatype getDataLang(){
		return dataLang;
	}


	public boolean hasLang(){
		return dataLang != null && dataLang != empty;
	}

	boolean testLang(IDatatype iod) {
		IDatatype dataLang2 = iod.getDataLang();
		if (dataLang != null) {
			if (dataLang2 == null || dataLang != dataLang2 ){
				return false;
			}
		}
		else if (dataLang2 != null){
			return false;
		}
		return true;
	}

	/**
	 * Literals are equal if their languages are equal
	 * not equal to URI 
	 * literal x string throw failure (?)
	 *
	 */
	public boolean equals(IDatatype iod) throws CoreseDatatypeException{
		boolean b2=  iod.polymorphEquals(this);
		return b2;
	}

	public boolean polymorphEquals(CoreseLiteral icod) throws CoreseDatatypeException {
		boolean b1= testLang(icod);
		if (! b1) throw failure(); //return false;
		boolean b2 = getValue().compareTo(icod.getValue()) == 0;
		return b2;
	}
	
	public boolean polymorphEquals(CoreseString icod) throws CoreseDatatypeException {
		if (getDataLang() != empty){
			throw failure();
		}
		return getValue().equals(icod.getValue());
	}


}