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

public class CoreseLiteral extends CoreseStringLiteral { 
	static final String DATATYPE = RDF.rdflangString; 
	static final CoreseURI datatype=new CoreseURI(DATATYPE);
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
		if (dataLang == empty){
			// SPARQL requires that datatype("abc") = xsd:string
			return CoreseString.datatype;
		}
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
		else return dataLang.getLabel();
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
		switch (iod.getCode()){
		
		case STRING:  
			if (getDataLang() != empty){
				throw failure();
			}
			return getLabel().equals(iod.getLabel());
			
		case LITERAL:
			boolean b1 = testLang(iod);
			if (! b1) throw failure(); //return false;
			return getLabel().equals(iod.getLabel());	
			
		case URI:
		case BLANK: return false;
		}
		throw failure();
	}

}