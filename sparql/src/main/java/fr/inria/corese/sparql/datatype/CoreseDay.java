package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.api.IDatatype;

public class CoreseDay extends CoreseDateElement {
	static final CoreseURI datatype = new CoreseURI(RDF.xsdday);

	public CoreseDay(String value) {
		super(value);
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}
}
