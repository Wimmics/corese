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
 * An implementation of the xsd:literal datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public class CoreseLiteral extends CoreseStringLiteral { 
	static final String DATATYPE = RDF.rdflangString; 
	static final CoreseURI datatype=new CoreseURI(DATATYPE);
	static final int code=LITERAL;
	private IDatatype dataLang=null;

	public CoreseLiteral(String value) {
		super(value);

		dataLang = empty; // default is ""
	}
        
        public CoreseLiteral(String value, String lang) {
		this(value);
                setLang(lang);
	}

	/**
	 * Literal has no xsd:datatype
	 */
        @Override
	public IDatatype getDatatype(){
		if (dataLang == empty){
			// SPARQL requires that datatype("abc") = xsd:string
			return CoreseString.datatype;
		}
		return datatype;
	}

        @Override
	public  int getCode(){
		return code;
	}
     
    @Override
    public NodeKind getNodeKind() {
        return NodeKind.LITERAL;
    }

        @Override
	public void setLang(String lang) {
		if (lang != null){
			dataLang=intGetDataLang(lang);
                }
	}



        @Override
	public String getLang(){
		if (dataLang == null) return null;
		else return dataLang.getLabel();
	}

        @Override
	public IDatatype getDataLang(){
		return dataLang;
	}


        @Override
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
        @Override
	public boolean equalsWE(IDatatype iod) throws CoreseDatatypeException{
		switch (iod.getCode()){
		
		case STRING:  
			if (getDataLang() != empty){
				return false; 
			}
			return getLabel().equals(iod.getLabel());
			
		case LITERAL:
			boolean b1 = testLang(iod);
			if (! b1) {
                            return false;
                        } 
			return getLabel().equals(iod.getLabel());	
			
                case UNDEF: if (hasLang()){return false;} else {throw failure();}
                    
		case URI:
		case BLANK: case TRIPLE: return false;
		}
		throw failure();
	}
        
            }
