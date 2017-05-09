package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

public  class CoreseDay extends CoreseInteger {
	static final CoreseURI datatype=new CoreseURI(RDF.xsdday);

	public CoreseDay(String value) {
		super(value);
	}
	
	
        @Override
	 public IDatatype getDatatype(){
	       return datatype;
	     }
}
