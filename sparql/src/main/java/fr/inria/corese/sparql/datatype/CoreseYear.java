package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;

public class CoreseYear extends CoreseDateElement {
	static final CoreseURI datatype = new CoreseURI(RDF.xsdyear);

	public CoreseYear(String value) {
		super(value);
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}
}
