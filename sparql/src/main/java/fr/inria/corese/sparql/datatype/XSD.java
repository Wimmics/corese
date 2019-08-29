package fr.inria.corese.sparql.datatype;

import fr.inria.corese.sparql.triple.parser.NSManager;

/**
 *
 * @author corby
 */
public interface XSD {

    static final String XSD = NSManager.XSD;
    static final String XSI = NSManager.XSI;
    static final String xsdboolean = XSD + "boolean";
    static final String xsdinteger = XSD + "integer";
    static final String xsdlong = XSD + "long";
    static final String xsdint = XSD + "int";
    static final String xsdshort = XSD + "short";
    static final String xsdbyte = XSD + "byte";
    static final String xsddecimal = XSD + "decimal";
    static final String xsdfloat = XSD + "float";
    static final String xsddouble = XSD + "double";
    static final String xsdnonNegativeInteger = XSD + "nonNegativeInteger";
    static final String xsdnonPositiveInteger = XSD + "nonPositiveInteger";
    static final String xsdpositiveInteger = XSD + "positiveInteger";
    static final String xsdnegativeInteger = XSD + "negativeInteger";
    static final String xsdunsignedLong = XSD + "unsignedLong";
    static final String xsdunsignedInt = XSD + "unsignedInt";
    static final String xsdunsignedShort = XSD + "unsignedShort";
    static final String xsdunsignedByte = XSD + "unsignedByte";
    static final String xsdduration = XSD + "duration";
    static final String xsddaytimeduration = XSD + "dayTimeDuration";
    static final String xsddate = XSD + "date";
    static final String xsddateTime = XSD + "dateTime";
    static final String xsdday = XSD + "gDay";
    static final String xsdmonth = XSD + "gMonth";
    static final String xsdyear = XSD + "gYear";
}
