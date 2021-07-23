package fr.inria.corese.sparql.datatype;

import javax.xml.datatype.DatatypeConfigurationException;

import fr.inria.corese.sparql.api.IDatatype;

public class CoreseDateTimeNew extends CoreseDateNew {
	static int code = DATETIME;

	static final CoreseURI datatype = new CoreseURI(RDF.xsddateTime);

	public CoreseDateTimeNew(String label) throws DatatypeConfigurationException {
		super(label);
	}

	public CoreseDateTimeNew() throws DatatypeConfigurationException {
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
