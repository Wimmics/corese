package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * 
 * Generic datatype for other such as dayTimeDuration
 */
public class CoreseGeneric extends CoreseString {
	
    IDatatype datatype;
	
	public CoreseGeneric(String label, String uri){
		super(label);
		setDatatype(uri);
	}
	
	public CoreseGeneric(String label){
		super(label);
	}
	
    @Override
	public void setDatatype(String uri){
	    datatype = getGenericDatatype(uri);
	}

    @Override
	public IDatatype getDatatype(){
		return datatype;
	}
	
    @Override
	public boolean equalsWE(IDatatype dt) throws CoreseDatatypeException {
		switch (dt.getCode()){
		case STRING: 
			if (! getDatatypeURI().equals(dt.getDatatypeURI())) throw failure();
			return getLabel().equals(dt.getLabel());
		case URI:
		case BLANK: case TRIPLE: return false;
		}
		throw failure();
	}
	
	
}
