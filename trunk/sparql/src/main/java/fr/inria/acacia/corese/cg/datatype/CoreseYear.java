package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

public  class CoreseYear extends CoreseInteger {
	static final CoreseURI datatype=new CoreseURI(RDF.xsdyear);

	public CoreseYear(String value) {
		super(value);
	}
	
	 public IDatatype getDatatype(){
	       return datatype;
	     }
}
