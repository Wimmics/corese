package fr.inria.corese.sparql.datatype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.CoreseDatatypeException;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 * <br>
 * An implementation of all the datatype that representation is a string: URI,
 * literals, strings for example. These classes have the same implemented
 * functions.<br>
 * It subsumes URI, Literal, xsd:string<br>
 * We can compare URI with URI, string and literal/XMLLiteral with string and
 * literal/XMLLiteral (they can be <= modulo lang)<br> e.g. : titi <=
 * toto@en<br> This class factorize util functions such as contains and plus
 * <br>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public abstract class CoreseStringableImpl extends CoreseDatatype {

    /**
     * logger from log4j
     */
    private static Logger logger = LoggerFactory.getLogger(CoreseStringableImpl.class);
    private Marker fatal = MarkerFactory.getMarker("FATAL");

    static int code = STRINGABLE;
    public static int count = 0;
    String value = "";

    public CoreseStringableImpl() {
    }

    public CoreseStringableImpl(String str) {
        setValue(str);
    }

    @Override
    public void setValue(String str) {
        this.value = str;
    }

    @Override
    public String getLabel() {
        return this.value;
    }

    /**
     * Cast a literal to a boolean may be allowed: when the value can be cast to
     * a float, double, decimal or integer, if this value is 0, then return
     * false, else return true
     */
    @Override
    public IDatatype cast(String target) {
        if (target.equals(RDF.xsdboolean)) {
            try {
                Float f =  Float.parseFloat(getLabel());
                if (f == 0) {
                    return CoreseBoolean.FALSE;
                } else if (f == 1) {
                    return CoreseBoolean.TRUE;
                } else {
                    return null;
                }
            } catch (NumberFormatException e) {
                return super.cast(target);
            }
        } else {
            return super.cast(target);
        }
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getLowerCaseLabel() {
        return getLabel().toLowerCase();
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isTrue() throws CoreseDatatypeException {
        return booleanValue();
    }

    @Override
    public boolean booleanValue() {
        return getLabel().length() > 0;
    }

    @Override
    public boolean isTrueAble() {
        return true;
    }

    @Override
    public boolean contains(IDatatype iod) {
        return getLowerCaseLabel().contains(iod.getLowerCaseLabel());
    }

    @Override
    public boolean startsWith(IDatatype iod) {
        return getLabel().startsWith(iod.getLabel());

    }

    //optimization
    public boolean contains(String label) {
        return getLowerCaseLabel().contains(label.toLowerCase());
    }

    public boolean startsWith(String label) {
        return getLabel().startsWith(label);
    }

    @Override
    public String getNormalizedLabel() {
        return getLabel();
    }

    public static String getNormalizedLabel(String label) {
        return label;
    }

    public boolean equals(String siod) {
        return getLabel().equals(siod);
    }

    int intCompare(CoreseStringableImpl icod) {
        return getLabel().compareTo(icod.getLabel());
    }

}
