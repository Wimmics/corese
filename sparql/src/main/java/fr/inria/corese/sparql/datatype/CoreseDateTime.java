package fr.inria.corese.sparql.datatype;

import javax.xml.datatype.DatatypeConfigurationException;

import fr.inria.corese.sparql.api.IDatatype;

public class CoreseDateTime extends CoreseDate {
	static int code = DATETIME;

	static final CoreseURI datatype = new CoreseURI(RDF.xsddateTime);

	public CoreseDateTime(String label) throws DatatypeConfigurationException {
		super(label);
	}

	public CoreseDateTime() throws DatatypeConfigurationException {
		super();
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	@Override
	public int getCode() {
		return code;
	}

}
