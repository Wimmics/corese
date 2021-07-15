package fr.inria.corese.sparql.datatype;

import java.math.BigDecimal;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;

/**
 * <p>
 * Title: Corese
 * </p>
 * <p>
 * Description: A Semantic Search Engine
 * </p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007
 * </p>
 * <p>
 * Company: INRIA
 * </p>
 * <p>
 * Project: Acacia
 * </p>
 * <br>
 * An implementation of the xsd:decimal datatype used by Corese <br>
 * 
 * @author Olivier Savoie
 */
public class CoreseDecimal extends CoreseDouble {
	static final CoreseURI datatype = new CoreseURI(RDF.xsddecimal);
	static final int code = DECIMAL;
	protected BigDecimal bdValue;

	CoreseDecimal() {
	}

	public CoreseDecimal(String value) throws CoreseDatatypeException {
		super(value);
		this.bdValue = new BigDecimal(value);
		// no exponent in decimal:
		if (value.indexOf("e") != -1 || value.indexOf("E") != -1) {
			throw new CoreseDatatypeException("Decimal", value);
		}
	}

	public CoreseDecimal(double value) {
		super(value);
		this.bdValue = new BigDecimal(value);
	}

	public CoreseDecimal(BigDecimal value) {

		// initialise double
		this.dvalue = value.doubleValue();
		setLabel(getNormalizedLabel());
		
		// initialise decimal
		this.bdValue = value;
		this.setLabel(this.bdValue.toString());
	}

	public static CoreseDecimal create(double value) {
		CoreseDecimal dt = new CoreseDecimal();
		dt.setValue(value);
		return dt;
	}

	public static CoreseDecimal create(BigDecimal value) {
		CoreseDecimal dt = new CoreseDecimal();
		dt.setValue(value);
		return dt;
	}

	@Override
	public void setValue(double value) {
		super.setValue(value);
		this.bdValue = new BigDecimal(value);
	}

	public void setValue(BigDecimal value) {
		this.dvalue = value.doubleValue();
		this.bdValue = value;
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public boolean isDecimal() {
		return true;
	}

	public BigDecimal decimalValue() {
		return this.bdValue;
	}

}