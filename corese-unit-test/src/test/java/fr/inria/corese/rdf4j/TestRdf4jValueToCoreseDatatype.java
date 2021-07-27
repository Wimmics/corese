package fr.inria.corese.rdf4j;

import static org.junit.Assert.assertEquals;

import com.ibm.icu.math.BigDecimal;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.Test;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseBlankNode;
import fr.inria.corese.sparql.datatype.CoreseBoolean;
import fr.inria.corese.sparql.datatype.CoreseDateOld;
import fr.inria.corese.sparql.datatype.CoreseDay;
import fr.inria.corese.sparql.datatype.CoreseDecimal;
import fr.inria.corese.sparql.datatype.CoreseDouble;
import fr.inria.corese.sparql.datatype.CoreseGenericInteger;
import fr.inria.corese.sparql.datatype.CoreseInteger;
import fr.inria.corese.sparql.datatype.CoreseLiteral;
import fr.inria.corese.sparql.datatype.CoreseMonth;
import fr.inria.corese.sparql.datatype.CoreseString;
import fr.inria.corese.sparql.datatype.CoreseURI;
import fr.inria.corese.sparql.datatype.CoreseURILiteral;
import fr.inria.corese.sparql.datatype.CoreseUndefLiteral;
import fr.inria.corese.sparql.datatype.CoreseXMLLiteral;
import fr.inria.corese.sparql.datatype.CoreseYear;
import fr.inria.corese.sparql.rdf4j.Rdf4jValueToCoreseDatatype;

public class TestRdf4jValueToCoreseDatatype {

    private static ValueFactory rdf4j_factory = SimpleValueFactory.getInstance();

    @Test
    public void convertUri() {
        String string_iri = "http://example.org/bob";

        // Build RDF4J IRI
        Value rdf4j_uri = rdf4j_factory.createIRI(string_iri);

        // Convert RDF4J IRI to Corese URI
        IDatatype corese_uri_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_uri);

        // Checks
        assertEquals(true, corese_uri_value.isURI());
        CoreseURI corese_uri = (CoreseURI) corese_uri_value;
        assertEquals(string_iri, corese_uri.stringValue());
    }

    @Test
    public void convertBNode() {
        String string_id = "BN_42";

        // Build RDF4J blank node
        Value rdf4j_blank = rdf4j_factory.createBNode(string_id);

        // Convert RDF4J blank node to Corese blank node
        IDatatype corese_blank_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_blank);

        // Checks
        assertEquals(true, corese_blank_value.isBlank());
        CoreseBlankNode corese_blank = (CoreseBlankNode) corese_blank_value;
        assertEquals(string_id, corese_blank.getID());
    }

    @Test
    public void convertDouble() {
        double value = 1.234;

        // Build RDF4J double
        Literal rdf4j_double = rdf4j_factory.createLiteral(value);

        // Convert RDF4J double to Corese double
        IDatatype corese_double_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_double);
        CoreseDouble corese_double = (CoreseDouble) corese_double_value;

        // Checks
        assertEquals(true, corese_double.isLiteral());
        assertEquals(value, corese_double.doubleValue(), 0);
        assertEquals(XSD.DOUBLE.stringValue(), corese_double.getDatatype().stringValue());
        assertEquals(XSD.DOUBLE, rdf4j_double.getDatatype());
    }

    @Test
    public void convertFloat() {
        float value = 1.234f;

        // Build RDF4J float
        Literal rdf4j_float = rdf4j_factory.createLiteral(value);

        // Convert RDF4J float to Corese float
        IDatatype corese_float_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_float);
        CoreseDouble corese_float = (CoreseDouble) corese_float_value;

        // Checks
        assertEquals(true, corese_float.isFloat());
        assertEquals(value, corese_float.floatValue(), 0);
        assertEquals(XSD.FLOAT.stringValue(), corese_float.getDatatype().stringValue());
        assertEquals(XSD.FLOAT, rdf4j_float.getDatatype());
    }

    @Test
    public void convertDecimal() {
        double double_value = 1.234;
        BigDecimal value = new BigDecimal(String.valueOf(double_value));

        // Build RDF4J decimal
        Literal rdf4j_decimal = rdf4j_factory.createLiteral(String.valueOf(value), XSD.DECIMAL);

        // Convert RDF4J decimal to Corese decimal
        IDatatype corese_decimal_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_decimal);
        CoreseDecimal corese_decimal = (CoreseDecimal) corese_decimal_value;

        // Checks
        assertEquals(true, corese_decimal.isDecimal());
        assertEquals(double_value, corese_decimal.doubleValue(), 0);
        assertEquals(XSD.DECIMAL.stringValue(), corese_decimal.getDatatype().stringValue());
        assertEquals(XSD.DECIMAL, rdf4j_decimal.getDatatype());
    }

    @Test
    public void convertInteger() {
        int value = 4;
        String string_value = String.valueOf(value);

        // Build RDF4J integer
        Literal rdf4j_integer = rdf4j_factory.createLiteral(string_value, XSD.INTEGER);

        // Convert RDF4J integer to Corese integer
        IDatatype corese_integer_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_integer);
        CoreseInteger corese_integer = (CoreseInteger) corese_integer_value;

        // Checks
        assertEquals(true, corese_integer.isInteger());
        assertEquals(value, corese_integer.getIntegerValue(), 0);
        assertEquals(XSD.INTEGER.stringValue(), corese_integer.getDatatype().stringValue());
        assertEquals(XSD.INTEGER, rdf4j_integer.getDatatype());
    }

    @Test
    public void convertYear() {
        int value = 2021;
        String string_value = String.valueOf(value);

        // Build RDF4J year
        Literal rdf4j_year = rdf4j_factory.createLiteral(string_value, XSD.GYEAR);

        // Convert RDF4J year to Corese year
        IDatatype corese_year_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_year);
        CoreseYear corese_year = (CoreseYear) corese_year_value;

        // Checks
        assertEquals(true, corese_year.isDateElement());
        assertEquals(value, corese_year.getIntegerValue());
        assertEquals(XSD.GYEAR.stringValue(), corese_year.getDatatype().stringValue());
        assertEquals(XSD.GYEAR, rdf4j_year.getDatatype());
    }

    @Test
    public void convertMonth() {
        int value = 6;
        String string_value = String.valueOf(value);

        // Build RDF4J month
        Literal rdf4j_month = rdf4j_factory.createLiteral(string_value, XSD.GMONTH);

        // Convert RDF4J month to Corese month
        IDatatype corese_month_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_month);
        CoreseMonth corese_month = (CoreseMonth) corese_month_value;

        // Checks
        assertEquals(true, corese_month.isDateElement());
        assertEquals(value, corese_month.getIntegerValue());
        assertEquals(XSD.GMONTH.stringValue(), corese_month.getDatatype().stringValue());
        assertEquals(XSD.GMONTH, rdf4j_month.getDatatype());
    }

    @Test
    public void convertDay() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J day
        Literal rdf4j_day = rdf4j_factory.createLiteral(string_value, XSD.GDAY);

        // Convert RDF4J day to Corese day
        IDatatype corese_day_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_day);
        CoreseDay corese_day = (CoreseDay) corese_day_value;

        // Checks
        assertEquals(true, corese_day.isDateElement());
        assertEquals(value, corese_day.getIntegerValue());
        assertEquals(XSD.GDAY.stringValue(), corese_day.getDatatype().stringValue());
        assertEquals(XSD.GDAY, rdf4j_day.getDatatype());
    }

    @Test
    public void convertByte() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J byte
        Literal rdf4j_byte = rdf4j_factory.createLiteral(string_value, XSD.BYTE);

        // Convert RDF4J byte to Corese byte
        IDatatype corese_byte_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_byte);
        CoreseGenericInteger corese_byte = (CoreseGenericInteger) corese_byte_value;

        // Checks
        assertEquals(true, corese_byte.isInteger());
        assertEquals(value, corese_byte.getIntegerValue());
        assertEquals(XSD.BYTE.stringValue(), corese_byte.getDatatype().stringValue());
        assertEquals(XSD.BYTE, rdf4j_byte.getDatatype());
    }

    @Test
    public void convertShort() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J short
        Literal rdf4j_short = rdf4j_factory.createLiteral(string_value, XSD.SHORT);

        // Convert RDF4J short to Corese short
        IDatatype corese_short_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_short);
        CoreseGenericInteger corese_short = (CoreseGenericInteger) corese_short_value;

        // Checks
        assertEquals(true, corese_short.isInteger());
        assertEquals(value, corese_short.getIntegerValue());
        assertEquals(XSD.SHORT.stringValue(), corese_short.getDatatype().stringValue());
        assertEquals(XSD.SHORT, rdf4j_short.getDatatype());
    }

    @Test
    public void convertInt() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J int
        Literal rdf4j_int = rdf4j_factory.createLiteral(string_value, XSD.INT);

        // Convert RDF4J int to Corese int
        IDatatype corese_int_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_int);
        CoreseGenericInteger corese_int = (CoreseGenericInteger) corese_int_value;

        // Checks
        assertEquals(true, corese_int.isInteger());
        assertEquals(value, corese_int.getIntegerValue());
        assertEquals(XSD.INT.stringValue(), corese_int.getDatatype().stringValue());
        assertEquals(XSD.INT, rdf4j_int.getDatatype());
    }

    @Test
    public void convertPositiveInteger() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J positive integer
        Literal rdf4j_positive_integer = rdf4j_factory.createLiteral(string_value, XSD.POSITIVE_INTEGER);

        // Convert RDF4J positive integer to Corese positive integer
        IDatatype corese_positive_integer_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_positive_integer);
        CoreseGenericInteger corese_positive_integer = (CoreseGenericInteger) corese_positive_integer_value;

        // Checks
        assertEquals(true, corese_positive_integer.isInteger());
        assertEquals(value, corese_positive_integer.getIntegerValue());
        assertEquals(XSD.POSITIVE_INTEGER.stringValue(), corese_positive_integer.getDatatype().stringValue());
        assertEquals(XSD.POSITIVE_INTEGER, rdf4j_positive_integer.getDatatype());
    }

    @Test
    public void convertNegativeInteger() {
        int value = -22;
        String string_value = String.valueOf(value);

        // Build RDF4J negative integer
        Literal rdf4j_negative_integer = rdf4j_factory.createLiteral(string_value, XSD.NEGATIVE_INTEGER);

        // Convert RDF4J negative integer to Corese negative integer
        IDatatype corese_negative_integer_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_negative_integer);
        CoreseGenericInteger corese_negative_integer = (CoreseGenericInteger) corese_negative_integer_value;

        // Checks
        assertEquals(true, corese_negative_integer.isInteger());
        assertEquals(value, corese_negative_integer.getIntegerValue());
        assertEquals(XSD.NEGATIVE_INTEGER.stringValue(), corese_negative_integer.getDatatype().stringValue());
        assertEquals(XSD.NEGATIVE_INTEGER, rdf4j_negative_integer.getDatatype());
    }

    @Test
    public void convertNonNegativeInteger() {
        int value = 0;
        String string_value = String.valueOf(value);

        // Build RDF4J non negative integer
        Literal rdf4j_non_negative_integer = rdf4j_factory.createLiteral(string_value, XSD.NON_NEGATIVE_INTEGER);

        // Convert RDF4J non negative integer to Corese non negative integer
        IDatatype corese_non_negative_integer_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_non_negative_integer);
        CoreseGenericInteger corese_non_negative_integer = (CoreseGenericInteger) corese_non_negative_integer_value;

        // Checks
        assertEquals(true, corese_non_negative_integer.isInteger());
        assertEquals(value, corese_non_negative_integer.getIntegerValue());
        assertEquals(XSD.NON_NEGATIVE_INTEGER.stringValue(), corese_non_negative_integer.getDatatype().stringValue());
        assertEquals(XSD.NON_NEGATIVE_INTEGER, rdf4j_non_negative_integer.getDatatype());
    }

    @Test
    public void convertNonPositiveInteger() {
        int value = 0;
        String string_value = String.valueOf(value);

        // Build RDF4J non positive integer
        Literal rdf4j_non_positive_integer = rdf4j_factory.createLiteral(string_value, XSD.NON_POSITIVE_INTEGER);

        // Convert RDF4J non positive integer to Corese non positive integer
        IDatatype corese_non_positive_integer_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_non_positive_integer);
        CoreseGenericInteger corese_non_positive_integer = (CoreseGenericInteger) corese_non_positive_integer_value;

        // Checks
        assertEquals(true, corese_non_positive_integer.isInteger());
        assertEquals(value, corese_non_positive_integer.getIntegerValue());
        assertEquals(XSD.NON_POSITIVE_INTEGER.stringValue(), corese_non_positive_integer.getDatatype().stringValue());
        assertEquals(XSD.NON_POSITIVE_INTEGER, rdf4j_non_positive_integer.getDatatype());
    }

    @Test
    public void convertUnsignedByte() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J unsigned byte
        Literal rdf4j_unsigned_byte = rdf4j_factory.createLiteral(string_value, XSD.UNSIGNED_BYTE);

        // Convert RDF4J unsigned byte to Corese unsigned byte
        IDatatype corese_unsigned_byte_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_unsigned_byte);
        CoreseGenericInteger corese_unsigned_byte = (CoreseGenericInteger) corese_unsigned_byte_value;

        // Checks
        assertEquals(true, corese_unsigned_byte.isInteger());
        assertEquals(value, corese_unsigned_byte.getIntegerValue());
        assertEquals(XSD.UNSIGNED_BYTE.stringValue(), corese_unsigned_byte.getDatatype().stringValue());
        assertEquals(XSD.UNSIGNED_BYTE, rdf4j_unsigned_byte.getDatatype());
    }

    @Test
    public void convertUnsignedInt() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J unsigned int
        Literal rdf4j_unsigned_int = rdf4j_factory.createLiteral(string_value, XSD.UNSIGNED_INT);

        // Convert RDF4J unsigned int to Corese unsigned int
        IDatatype corese_unsigned_int_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_unsigned_int);
        CoreseGenericInteger corese_unsigned_int = (CoreseGenericInteger) corese_unsigned_int_value;

        // Checks
        assertEquals(true, corese_unsigned_int.isInteger());
        assertEquals(value, corese_unsigned_int.getIntegerValue());
        assertEquals(XSD.UNSIGNED_INT.stringValue(), corese_unsigned_int.getDatatype().stringValue());
        assertEquals(XSD.UNSIGNED_INT, rdf4j_unsigned_int.getDatatype());
    }

    @Test
    public void convertUnsignedLong() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J unsigned long
        Literal rdf4j_unsigned_long = rdf4j_factory.createLiteral(string_value, XSD.UNSIGNED_LONG);

        // Convert RDF4J unsigned long to Corese unsigned long
        IDatatype corese_unsigned_long_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_unsigned_long);
        CoreseGenericInteger corese_unsigned_long = (CoreseGenericInteger) corese_unsigned_long_value;

        // Checks
        assertEquals(true, corese_unsigned_long.isInteger());
        assertEquals(value, corese_unsigned_long.getIntegerValue());
        assertEquals(XSD.UNSIGNED_LONG.stringValue(), corese_unsigned_long.getDatatype().stringValue());
        assertEquals(XSD.UNSIGNED_LONG, rdf4j_unsigned_long.getDatatype());
    }

    @Test
    public void convertUnsignedShort() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build RDF4J unsigned short
        Literal rdf4j_unsigned_short = rdf4j_factory.createLiteral(string_value, XSD.UNSIGNED_SHORT);

        // Convert RDF4J unsigned short to Corese unsigned short
        IDatatype corese_unsigned_short_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_unsigned_short);
        CoreseGenericInteger corese_unsigned_short = (CoreseGenericInteger) corese_unsigned_short_value;

        // Checks
        assertEquals(true, corese_unsigned_short.isInteger());
        assertEquals(value, corese_unsigned_short.getIntegerValue());
        assertEquals(XSD.UNSIGNED_SHORT.stringValue(), corese_unsigned_short.getDatatype().stringValue());
        assertEquals(XSD.UNSIGNED_SHORT, rdf4j_unsigned_short.getDatatype());
    }

    @Test
    public void convertBoolean() {
        Boolean value = true;
        String string_value = String.valueOf(value);

        // Build RDF4J boolean
        Literal rdf4j_boolean = rdf4j_factory.createLiteral(string_value, XSD.BOOLEAN);

        // Convert RDF4J boolean to Corese boolean
        IDatatype corese_boolean_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_boolean);
        CoreseBoolean corese_boolean = (CoreseBoolean) corese_boolean_value;

        // Checks
        assertEquals(true, corese_boolean.isBoolean());
        assertEquals(value, corese_boolean.booleanValue());
        assertEquals(XSD.BOOLEAN.stringValue(), corese_boolean.getDatatype().stringValue());
        assertEquals(XSD.BOOLEAN, rdf4j_boolean.getDatatype());
    }

    @Test
    public void convertAnyUri() {
        String value = "http://example.org/bob";

        // Build RDF4J any UTI
        Literal rdf4j_any_uri = rdf4j_factory.createLiteral(value, XSD.ANYURI);

        // Convert RDF4J any UTI to Corese any UTI
        IDatatype corese_any_uri_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_any_uri);
        CoreseURILiteral corese_any_uri = (CoreseURILiteral) corese_any_uri_value;

        // Checks
        assertEquals(value, corese_any_uri.getStringValue());
        assertEquals(XSD.ANYURI.stringValue(), corese_any_uri.getDatatype().stringValue());
        assertEquals(XSD.ANYURI, rdf4j_any_uri.getDatatype());
    }

    @Test
    public void convertXmlLiteral() {
        String value = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></xs:schema>";

        // Build RDF4J XML literal
        Literal rdf4j_xml_literal = rdf4j_factory.createLiteral(value, RDF.XMLLITERAL);

        // Convert RDF4J XML literal to Corese XML literal
        IDatatype corese_xml_literal_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_xml_literal);
        CoreseXMLLiteral corese_xml_literal = (CoreseXMLLiteral) corese_xml_literal_value;

        // Checks
        assertEquals(value, corese_xml_literal.getStringValue());
        assertEquals(RDF.XMLLITERAL.stringValue(), corese_xml_literal.getDatatype().stringValue());
        assertEquals(RDF.XMLLITERAL, rdf4j_xml_literal.getDatatype());
    }

    @Test
    public void convertDate() {
        String value = "2021-06-16";

        // Build RDF4J date
        Literal rdf4j_date = rdf4j_factory.createLiteral(value, XSD.DATE);

        // Convert RDF4J date to Corese date
        IDatatype corese_date_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_date);
        CoreseDateOld corese_date = (CoreseDateOld) corese_date_value;

        // Checks
        assertEquals(value, corese_date.getStringValue());
        assertEquals(XSD.DATE.stringValue(), corese_date.getDatatype().stringValue());
        assertEquals(XSD.DATE, rdf4j_date.getDatatype());
    }

    @Test
    public void convertDateTime() {
        String value = "2021-06-17T07:12:19.0";

        // Build RDF4J date and time
        Literal rdf4j_date_time = rdf4j_factory.createLiteral(value, XSD.DATETIME);

        // Convert RDF4J date and time to Corese date and time
        IDatatype corese_date_time_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_date_time);
        CoreseDateOld corese_date_time = (CoreseDateOld) corese_date_time_value;

        // Checks
        assertEquals(value, corese_date_time.getStringValue());
        assertEquals(XSD.DATETIME.stringValue(), corese_date_time.getDatatype().stringValue());
        assertEquals(XSD.DATETIME, rdf4j_date_time.getDatatype());
    }

    @Test
    public void convertDateTimeMilisecond() {
        String value = "2021-07-16T16:28:36.477";

        // Build RDF4J date and time
        Literal rdf4j_date_time = rdf4j_factory.createLiteral(value, XSD.DATETIME);

        // Convert RDF4J date and time to Corese date and time
        IDatatype corese_date_time_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_date_time);

        assertEquals(value, rdf4j_date_time.getLabel());
        assertEquals(value, corese_date_time_value.stringValue());
    }

    @Test
    public void convertLiteralLang() {
        String value = "Un super test";
        String lang = "fr";

        // Build RDF4J literal lang
        Literal rdf4j_literal_lang = rdf4j_factory.createLiteral(value, lang);

        // Convert RDF4J literal lang and time to Corese literal lang
        IDatatype corese_literal_lang_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_literal_lang);
        CoreseLiteral corese_literal_lang = (CoreseLiteral) corese_literal_lang_value;

        // Checks
        assertEquals(corese_literal_lang.isLiteral(), true);
        assertEquals(value, corese_literal_lang.getStringValue());
        assertEquals(RDF.LANGSTRING.stringValue(), corese_literal_lang.getDatatype().stringValue());
        assertEquals(RDF.LANGSTRING, rdf4j_literal_lang.getDatatype());
        assertEquals("fr", corese_literal_lang.getLang());
    }

    @Test
    public void convertString() {
        String value = "Un super test";

        // Build RDF4J string
        Literal rdf4j_string = rdf4j_factory.createLiteral(value, XSD.STRING);

        // Convert RDF4J string and time to Corese string
        IDatatype corese_string_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_string);
        CoreseString corese_string = (CoreseString) corese_string_value;

        // Checks
        assertEquals(corese_string.isLiteral(), true);
        assertEquals(value, corese_string.getStringValue());
        assertEquals(XSD.STRING.stringValue(), corese_string.getDatatype().stringValue());
        assertEquals(XSD.STRING, rdf4j_string.getDatatype());
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build RDF4J undef
        Literal rdf4j_string = rdf4j_factory.createLiteral(string_value, rdf4j_factory.createIRI(undef_datatype));

        // Convert RDF4J undef and time to Corese undef
        IDatatype corese_string_value = Rdf4jValueToCoreseDatatype.convert(rdf4j_string);
        CoreseUndefLiteral corese_string = (CoreseUndefLiteral) corese_string_value;

        // Checks
        assertEquals(corese_string.isLiteral(), true);
        // assertEquals(value, corese_string.getIntegerValue());
        assertEquals(undef_datatype, corese_string.getDatatype().stringValue());
        assertEquals(undef_datatype, rdf4j_string.getDatatype().stringValue());
    }
}
