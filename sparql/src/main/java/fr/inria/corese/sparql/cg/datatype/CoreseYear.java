package fr.inria.corese.sparql.cg.datatype;

import fr.inria.corese.sparql.api.IDatatype;

public  class CoreseYear extends CoreseInteger {
	static final CoreseURI datatype=new CoreseURI(RDF.xsdyear);

	public CoreseYear(String value) {
		super(value);
	}
	
        @Override
	 public IDatatype getDatatype(){
	       return datatype;
	     }
}
