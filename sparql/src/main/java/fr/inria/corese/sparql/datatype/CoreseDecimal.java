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
        //this.checkNoExposant(value);
        this.setValue(clean(value));
    }
    
    String clean(String value) {
        if (value.startsWith("-.")) {
            return "-0"+value.substring(1);
        }
        return value;
    }

    private void checkNoExposant(String value) throws CoreseDatatypeException {
        // no exponent in decimal:
        if (value.indexOf("e") != -1 || value.indexOf("E") != -1) {
            throw new CoreseDatatypeException("Decimal", value);
        }
    }

    public CoreseDecimal(double value) {
        this.setValue(value);
    }

    public CoreseDecimal(BigDecimal value) {
        this.setValue(value);
    }

    public CoreseDecimal(int value) {
        this.setValue(value);
    }

    public static CoreseDecimal create(double value) {
        return new CoreseDecimal(value);
    }

    public static CoreseDecimal create(BigDecimal value) {
        return new CoreseDecimal(value);
    }

    public static CoreseDecimal create(int value) {
        return new CoreseDecimal(value);
    }

    public static CoreseDecimal create(String value) throws CoreseDatatypeException {
        return new CoreseDecimal(value);
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);
        this.bdValue = BigDecimal.valueOf(value);
        this.setLabel(String.valueOf(bdValue));
    }

    @Override
    public void setValue(BigDecimal value) {
        super.setValue(value.doubleValue());
        this.bdValue = value;
        this.setLabel(String.valueOf(bdValue));
    }

    @Override
    public void setValue(String value) throws CoreseDatatypeException {
        this.checkNoExposant(value);
        super.setValue(Double.parseDouble(value));
        this.bdValue = new BigDecimal(value);
        this.setLabel(String.valueOf(value));
    }

    @Override
    public void setValue(int value) {
        super.setValue(Double.valueOf(value));
        this.bdValue = new BigDecimal(value);
        this.setLabel(String.valueOf(bdValue));
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

    @Override
    public BigDecimal decimalValue() {
        return this.bdValue;
    }

}
