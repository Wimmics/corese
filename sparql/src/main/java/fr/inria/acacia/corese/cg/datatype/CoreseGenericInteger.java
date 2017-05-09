package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * int short ...
 * 
 * @author corby
 *
 */
public class CoreseGenericInteger extends CoreseInteger {
	
    CoreseURI datatype;
	
	public CoreseGenericInteger(String label, String uri){
		super(label);
		setDatatype(uri);
	}
	
	public CoreseGenericInteger(String label){
		super(label);
		// by safety:
		datatype = CoreseInteger.datatype;
	}
	
	public CoreseGenericInteger(int n, String uri){
		super(n);
		setDatatype(uri);
	}
	
    @Override
	public void setDatatype(String uri){
	    datatype = getGenericDatatype(uri);
	}

    @Override
	public IDatatype getDatatype(){
		return datatype;
	}

	
}