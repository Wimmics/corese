package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

public  class CoreseMonth extends CoreseInteger {
	static final CoreseURI datatype=new CoreseURI(RDF.xsdmonth);

	public CoreseMonth(String value) {
		super(value);
	}
	
        @Override
	 public IDatatype getDatatype(){
	       return datatype;
	     }
}
