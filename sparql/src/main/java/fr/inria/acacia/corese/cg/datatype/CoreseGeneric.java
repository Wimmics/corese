package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * 
 * Generic datatype for other such as dayTimeDuration
 */
public class CoreseGeneric extends CoreseString {
	
    CoreseURI datatype;
	
	public CoreseGeneric(String label, String uri){
		super(label);
		setDatatype(uri);
	}
	
	public CoreseGeneric(String label){
		super(label);
	}
	
	public void setDatatype(String uri){
	    datatype = getGenericDatatype(uri);
	}

	public IDatatype getDatatype(){
		return datatype;
	}
	
	public boolean equals(IDatatype dt) throws CoreseDatatypeException {
		switch (dt.getCode()){
		case STRING: 
			if (! getDatatypeURI().equals(dt.getDatatypeURI())) throw failure();
			return getLabel().equals(dt.getLabel());
		case URI:
		case BLANK: return false;
		}
		throw failure();
	}
	
	
}
