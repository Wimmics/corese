package fr.inria.corese.rdf4jImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.Test;

import fr.inria.corese.rdf4j.convert.datatypes.CoreseDatatypeToRdf4jValue;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseDate;
import fr.inria.corese.sparql.datatype.CoreseDateTime;
import fr.inria.corese.sparql.datatype.CoreseDay;
import fr.inria.corese.sparql.datatype.CoreseGenericInteger;
import fr.inria.corese.sparql.datatype.CoreseLiteral;
import fr.inria.corese.sparql.datatype.CoreseMonth;
import fr.inria.corese.sparql.datatype.CoreseString;
import fr.inria.corese.sparql.datatype.CoreseStringBuilder;
import fr.inria.corese.sparql.datatype.CoreseYear;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;

public class TestCoreseDatatypeToRdf4jValue {

    @Test
    public void convertUri() {
        String string_uri = "http://example.org/bob";

        // Build Corese URI
        IDatatype corese_uri = DatatypeMap.createResource(string_uri);

        // Convert Corese URI to RDF4J IRI
        Value rdf4j_iri_value = CoreseDatatypeToRdf4jValue.convert(corese_uri);

        // Checks
        assertEquals(true, rdf4j_iri_value.isIRI());
        IRI rdf4j_iri = (IRI) rdf4j_iri_value;
        assertEquals(string_uri, rdf4j_iri.stringValue());
    }

    @Test
    public void convertBNode() {
        String string_id = "_:BN_42";

        // Build Corese blank node
        IDatatype corese_blank = DatatypeMap.createBlank(string_id);

        // Convert Corese blank node to RDF4J blank node
        Value rdf4j_blank_value = CoreseDatatypeToRdf4jValue.convert(corese_blank);

        // Checks
        assertEquals(true, rdf4j_blank_value.isBNode());
        BNode rdf4j_blank = (BNode) rdf4j_blank_value;
        assertEquals(string_id, corese_blank.stringValue());
        assertEquals(string_id, rdf4j_blank.toString());
    }

    @Test
    public void convertDouble() {
        double value = 1.234;

        // Build Corese double
        IDatatype corese_double = DatatypeMap.newDouble(value);

        // Convert Corese double to RDF4J double
        Value rdf4j_double_value = CoreseDatatypeToRdf4jValue.convert(corese_double);

        // Checks
        assertEquals(rdf4j_double_value.isLiteral(), true);
        Literal rdf4j_double = (Literal) rdf4j_double_value;
        assertEquals(value, rdf4j_double.doubleValue(), 0);
        assertEquals(XSD.DOUBLE.stringValue(), rdf4j_double.getDatatype().stringValue());
        assertEquals(XSD.DOUBLE.stringValue(), corese_double.getDatatype().stringValue());
    }

    @Test
    public void convertFloat() {
        float value = 1.234f;

        // Build Corese float
        IDatatype corese_float = DatatypeMap.newFloat(value);

        // Convert Corese float to RDF4J float
        Value rdf4j_float_value = CoreseDatatypeToRdf4jValue.convert(corese_float);

        // Checks
        assertEquals(rdf4j_float_value.isLiteral(), true);
        Literal rdf4j_float = (Literal) rdf4j_float_value;
        assertEquals(value, rdf4j_float.floatValue(), 0);
        assertEquals(XSD.FLOAT, rdf4j_float.getDatatype());
        assertEquals(XSD.FLOAT.stringValue(), corese_float.getDatatype().stringValue());
    }

    @Test
    public void convertDecimal() {
        double double_value = 1.234;
        BigDecimal value = new BigDecimal(String.valueOf(double_value));

        // Build Corese decimal
        IDatatype corese_decimal = DatatypeMap.newDecimal(double_value);

        // Convert Corese decimal to RDF4J decimal
        Value rdf4j_decimal_value = CoreseDatatypeToRdf4jValue.convert(corese_decimal);

        // Checks
        assertEquals(rdf4j_decimal_value.isLiteral(), true);
        Literal rdf4j_decimal = (Literal) rdf4j_decimal_value;
        assertEquals(value, rdf4j_decimal.decimalValue());
        assertEquals(XSD.DECIMAL, rdf4j_decimal.getDatatype());
        assertEquals(XSD.DECIMAL.stringValue(), corese_decimal.getDatatype().stringValue());
    }

    @Test
    public void convertInteger() {
        int value = 4;

        // Build Corese integer
        IDatatype corese_integer = DatatypeMap.newInteger(value);

        // Convert Corese integer to RDF4J integer
        Value rdf4j_integer_value = CoreseDatatypeToRdf4jValue.convert(corese_integer);

        // Checks
        assertEquals(rdf4j_integer_value.isLiteral(), true);
        Literal rdf4j_integer = (Literal) rdf4j_integer_value;
        assertEquals(value, rdf4j_integer.intValue());
        assertEquals(XSD.INTEGER, rdf4j_integer.getDatatype());
        assertEquals(XSD.INTEGER.stringValue(), corese_integer.getDatatype().stringValue());
    }

    @Test
    public void convertBoolean() {
        boolean value = true;

        // Build Corese boolean
        IDatatype corese_boolean = DatatypeMap.newInstance(value);

        // Convert Corese boolean to RDF4J boolean
        Value rdf4j_boolean_value = CoreseDatatypeToRdf4jValue.convert(corese_boolean);

        // Checks
        assertEquals(rdf4j_boolean_value.isLiteral(), true);
        Literal rdf4j_boolean = (Literal) rdf4j_boolean_value;
        assertEquals(value, rdf4j_boolean.booleanValue());
        assertEquals(XSD.BOOLEAN, rdf4j_boolean.getDatatype());
        assertEquals(XSD.BOOLEAN.stringValue(), corese_boolean.getDatatype().stringValue());
    }

    @Test
    public void convertAnyUri() {
        String value = "http://example.org/bob";

        // Build Corese any uri
        IDatatype corese_any_uri = DatatypeMap.createLiteral(value, XSD.ANYURI.stringValue());

        // Convert Corese any uri to RDF4J any uri
        Value rdf4j_any_uri_value = CoreseDatatypeToRdf4jValue.convert(corese_any_uri);

        // Checks
        assertEquals(rdf4j_any_uri_value.isLiteral(), true);
        Literal rdf4j_any_uri = (Literal) rdf4j_any_uri_value;
        assertEquals(value, rdf4j_any_uri.getLabel());
        assertEquals(XSD.ANYURI, rdf4j_any_uri.getDatatype());
        assertEquals(XSD.ANYURI.stringValue(), corese_any_uri.getDatatype().stringValue());

    }

    @Test
    public void convertXmlLiteral() {
        String value = "<span xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">シェイクスピアの<ruby><rbc><rb>演</rb><rb>劇</rb></rbc><rtc><rt>えん</rt><rt>げき</rt></rtc></ruby></span>";

        // Build Corese XML literal
        IDatatype corese_xml_literal = DatatypeMap.createLiteral(value, DatatypeMap.XMLLITERAL);

        // Convert Corese XML literal to RDF4J XML literal
        Value rdf4j_xml_literal_value = CoreseDatatypeToRdf4jValue.convert(corese_xml_literal);

        // Checks
        assertEquals(rdf4j_xml_literal_value.isLiteral(), true);
        Literal rdf4j_xml_literal = (Literal) rdf4j_xml_literal_value;
        assertEquals(value, rdf4j_xml_literal.getLabel());
        assertEquals(RDF.XMLLITERAL, rdf4j_xml_literal.getDatatype().stringValue());
        assertEquals(RDF.XMLLITERAL, corese_xml_literal.getDatatype().stringValue());
    }

    @Test
    public void convertDate() {
        String value = "2021-06-16";

        // Build Corese date
        CoreseDate corese_date = (CoreseDate) DatatypeMap.newDate(value);

        // Convert Corese date to RDF4J date
        Value rdf4j_date_value = CoreseDatatypeToRdf4jValue.convert(corese_date);

        // Checks
        assertEquals(rdf4j_date_value.isLiteral(), true);
        Literal rdf4j_date = (Literal) rdf4j_date_value;
        assertEquals(value, rdf4j_date.getLabel());
        assertEquals(XSD.DATE, rdf4j_date.getDatatype());
        assertEquals(XSD.DATE.stringValue(), corese_date.getDatatype().stringValue());
    }

    @Test
    public void convertDateTime() {
        String value = "2021-06-17T07:12:19.0";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to RDF4J date
        Value rdf4j_date_time_value = CoreseDatatypeToRdf4jValue.convert(corese_date_time);

        // Checks
        assertEquals(rdf4j_date_time_value.isLiteral(), true);
        Literal rdf4j_date_time = (Literal) rdf4j_date_time_value;
        assertEquals(value, rdf4j_date_time.getLabel());
        assertEquals(XSD.DATETIME, rdf4j_date_time.getDatatype());
        assertEquals(XSD.DATETIME.stringValue(), corese_date_time.getDatatype().stringValue());
    }

    @Test
    public void convertDateTimeMilisecond() {

        String value = "2021-07-16T16:28:36.477";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to RDF4J date
        Value rdf4j_date_time_value = CoreseDatatypeToRdf4jValue.convert(corese_date_time);

        assertEquals(value, corese_date_time.getLabel());
        Literal rdf4j_date_time = (Literal) rdf4j_date_time_value;
        assertEquals(value, rdf4j_date_time.stringValue());
        assertEquals(XSD.DATETIME, rdf4j_date_time.getDatatype());
        assertEquals(XSD.DATETIME.stringValue(), corese_date_time.getDatatype().stringValue());
    }

    @Test
    public void convertLiteralLang() {
        String value = "Un super test";
        String lang = "fr";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.createLiteral(value, RDF.rdflangString, lang);

        // Convert Corese literal lang to RDF4J literal lang
        Value rdf4j_literal_value = CoreseDatatypeToRdf4jValue.convert(corese_literal);
        // Checks
        assertEquals(rdf4j_literal_value.isLiteral(), true);
        Literal rdf4j_literal = (Literal) rdf4j_literal_value;
        assertEquals(value, rdf4j_literal.getLabel());
        assertEquals(RDF.LANGSTRING, rdf4j_literal.getDatatype().stringValue());
        assertEquals(RDF.LANGSTRING, corese_literal.getDatatype().stringValue());
        assertEquals(Optional.of("fr"), rdf4j_literal.getLanguage());
    }

    @Test
    public void convertLiteralLangXhithoutLang() {
        String value = "Un super test";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.newInstance(value, RDF.rdflangString);

        // Convert Corese literal lang to RDF4J literal lang
        Value rdf4j_literal_value = CoreseDatatypeToRdf4jValue.convert(corese_literal);

        // Checks
        assertEquals(rdf4j_literal_value.isLiteral(), true);
        Literal rdf4j_literal = (Literal) rdf4j_literal_value;
        assertEquals(value, rdf4j_literal.getLabel());
        assertEquals(XSD.STRING, rdf4j_literal.getDatatype());
        assertEquals(XSD.STRING.stringValue(), corese_literal.getDatatype().stringValue());
        assertEquals(Optional.empty(), rdf4j_literal.getLanguage());
    }

    @Test
    public void convertString() {
        String value = "Un super test";

        // Build Corese string
        CoreseString corese_string = (CoreseString) DatatypeMap.newStringBuilder(value);

        // Convert Corese string to RDF4J string
        Value rdf4j_string_value = CoreseDatatypeToRdf4jValue.convert(corese_string);

        // Checks
        assertEquals(rdf4j_string_value.isLiteral(), true);
        Literal rdf4j_string = (Literal) rdf4j_string_value;
        assertEquals(value, rdf4j_string.getLabel());
        assertEquals(XSD.STRING, rdf4j_string.getDatatype());
        assertEquals(XSD.STRING.stringValue(), corese_string.getDatatype().stringValue());
        assertEquals(Optional.empty(), rdf4j_string.getLanguage());
    }

    @Test
    public void convertStringBuilder() {
        String value = "Un super test";

        // Build Corese string builder
        CoreseStringBuilder corese_string_builder = (CoreseStringBuilder) DatatypeMap.newStringBuilder(value);

        // Convert Corese string builder to RDF4J string builder
        Value rdf4j_string_builder_value = CoreseDatatypeToRdf4jValue.convert(corese_string_builder);

        // Checks
        assertEquals(rdf4j_string_builder_value.isLiteral(), true);
        Literal rdf4j_string_builder = (Literal) rdf4j_string_builder_value;
        assertEquals(value, rdf4j_string_builder.getLabel());
        assertEquals(XSD.STRING, rdf4j_string_builder.getDatatype());
        assertEquals(XSD.STRING.stringValue(), corese_string_builder.getDatatype().stringValue());
        assertEquals(Optional.empty(), rdf4j_string_builder.getLanguage());
    }

    @Test
    public void convertYear() {
        int int_value = 2021;
        String value = String.valueOf(int_value);

        // Build Corese year
        CoreseYear corese_year = (CoreseYear) DatatypeMap.createLiteral(value, RDF.xsdyear);

        // Convert Corese year to RDF4J year
        Value rdf4j_year_value = CoreseDatatypeToRdf4jValue.convert(corese_year);

        // Checks
        assertEquals(rdf4j_year_value.isLiteral(), true);
        Literal rdf4j_year = (Literal) rdf4j_year_value;
        assertEquals(value, rdf4j_year.getLabel());
        assertEquals(int_value, rdf4j_year.intValue());
        assertEquals(RDF.xsdyear, rdf4j_year.getDatatype().stringValue());
        assertEquals(RDF.xsdyear, corese_year.getDatatype().stringValue());
    }

    @Test
    public void convertMonth() {
        int int_value = 6;
        String string_value = String.valueOf(int_value);

        // Build Corese month
        CoreseMonth corese_month = (CoreseMonth) DatatypeMap.createLiteral(string_value, RDF.xsdmonth);

        // Convert Corese month to RDF4J month
        Value rdf4j_month_value = CoreseDatatypeToRdf4jValue.convert(corese_month);

        // Checks
        assertEquals(rdf4j_month_value.isLiteral(), true);
        Literal rdf4j_month = (Literal) rdf4j_month_value;
        assertEquals(string_value, rdf4j_month.getLabel());
        assertEquals(RDF.xsdmonth, rdf4j_month.getDatatype().stringValue());
        assertEquals(RDF.xsdmonth, corese_month.getDatatype().stringValue());
    }

    @Test
    public void convertDay() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese day
        CoreseDay corese_day = (CoreseDay) DatatypeMap.createLiteral(string_value, RDF.xsdday);

        // Convert Corese day to RDF4J day
        Value rdf4j_day_value = CoreseDatatypeToRdf4jValue.convert(corese_day);

        // Checks
        assertEquals(rdf4j_day_value.isLiteral(), true);
        Literal rdf4j_day = (Literal) rdf4j_day_value;
        assertEquals(string_value, rdf4j_day.getLabel());
        assertEquals(int_value, rdf4j_day.intValue());
        assertEquals(RDF.xsdday, rdf4j_day.getDatatype().stringValue());
        assertEquals(RDF.xsdday, corese_day.getDatatype().stringValue());
    }

    @Test
    public void convertByte() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese byte
        CoreseGenericInteger corese_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value, RDF.xsdbyte);

        // Convert Corese byte to RDF4J byte
        Value rdf4j_byte_value = CoreseDatatypeToRdf4jValue.convert(corese_byte);

        // Checks
        assertEquals(rdf4j_byte_value.isLiteral(), true);
        Literal rdf4j_byte = (Literal) rdf4j_byte_value;
        assertEquals(string_value, rdf4j_byte.getLabel());
        assertEquals(int_value, rdf4j_byte.intValue());
        assertEquals(RDF.xsdbyte, rdf4j_byte.getDatatype().stringValue());
        assertEquals(RDF.xsdbyte, corese_byte.getDatatype().stringValue());
    }

    @Test
    public void convertShort() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese short
        CoreseGenericInteger corese_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdshort);

        // Convert Corese short to RDF4J short
        Value rdf4j_short_value = CoreseDatatypeToRdf4jValue.convert(corese_short);

        // Checks
        assertEquals(rdf4j_short_value.isLiteral(), true);
        Literal rdf4j_short = (Literal) rdf4j_short_value;
        assertEquals(string_value, rdf4j_short.getLabel());
        assertEquals(int_value, rdf4j_short.intValue());
        assertEquals(RDF.xsdshort, rdf4j_short.getDatatype().stringValue());
        assertEquals(RDF.xsdshort, corese_short.getDatatype().stringValue());
    }

    @Test
    public void convertInt() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese int
        CoreseGenericInteger corese_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value, RDF.xsdint);

        // Convert Corese int to RDF4J int
        Value rdf4j_int_value = CoreseDatatypeToRdf4jValue.convert(corese_int);

        // Checks
        assertEquals(rdf4j_int_value.isLiteral(), true);
        Literal rdf4j_int = (Literal) rdf4j_int_value;
        assertEquals(string_value, rdf4j_int.getLabel());
        assertEquals(int_value, rdf4j_int.intValue());
        assertEquals(RDF.xsdint, rdf4j_int.getDatatype().stringValue());
        assertEquals(RDF.xsdint, corese_int.getDatatype().stringValue());
    }

    @Test
    public void convertPositiveInteger() {
        int positive_integer_value = 22;
        String string_value = String.valueOf(positive_integer_value);

        // Build Corese positive integer
        CoreseGenericInteger corese_positive_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdpositiveInteger);

        // Convert Corese positive integer to RDF4J positive integer
        Value rdf4j_positive_integer_value = CoreseDatatypeToRdf4jValue.convert(corese_positive_integer);

        // Checks
        assertEquals(rdf4j_positive_integer_value.isLiteral(), true);
        Literal rdf4j_positive_integer = (Literal) rdf4j_positive_integer_value;
        assertEquals(string_value, rdf4j_positive_integer.getLabel());
        assertEquals(positive_integer_value, rdf4j_positive_integer.intValue());
        assertEquals(RDF.xsdpositiveInteger, rdf4j_positive_integer.getDatatype().stringValue());
        assertEquals(RDF.xsdpositiveInteger, corese_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNegativeInteger() {
        int negative_integer_value = -22;
        String string_value = String.valueOf(negative_integer_value);

        // Build Corese negative integer
        CoreseGenericInteger corese_negative_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdnegativeInteger);

        // Convert Corese negative integer to RDF4J negative integer
        Value rdf4j_negative_integer_value = CoreseDatatypeToRdf4jValue.convert(corese_negative_integer);

        // Checks
        assertEquals(rdf4j_negative_integer_value.isLiteral(), true);
        Literal rdf4j_negative_integer = (Literal) rdf4j_negative_integer_value;
        assertEquals(string_value, rdf4j_negative_integer.getLabel());
        assertEquals(negative_integer_value, rdf4j_negative_integer.intValue());
        assertEquals(RDF.xsdnegativeInteger, rdf4j_negative_integer.getDatatype().stringValue());
        assertEquals(RDF.xsdnegativeInteger, corese_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonNegativeInteger() {
        int non_negative_integer_value = 0;
        String string_value = String.valueOf(non_negative_integer_value);

        // Build Corese non negative integer
        CoreseGenericInteger corese_non_negative_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, RDF.xsdnonNegativeInteger);

        // Convert Corese non negative integer to RDF4J non negative integer
        Value rdf4j_non_negative_integer_value = CoreseDatatypeToRdf4jValue.convert(corese_non_negative_integer);

        // Checks
        assertEquals(rdf4j_non_negative_integer_value.isLiteral(), true);
        Literal rdf4j_non_negative_integer = (Literal) rdf4j_non_negative_integer_value;
        assertEquals(string_value, rdf4j_non_negative_integer.getLabel());
        assertEquals(non_negative_integer_value, rdf4j_non_negative_integer.intValue());
        assertEquals(RDF.xsdnonNegativeInteger, rdf4j_non_negative_integer.getDatatype().stringValue());
        assertEquals(RDF.xsdnonNegativeInteger, corese_non_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonPositiveInteger() {
        int non_positive_integer_value = 0;
        String string_value = String.valueOf(non_positive_integer_value);

        // Build Corese non positive integer
        CoreseGenericInteger corese_non_positive_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, RDF.xsdnonPositiveInteger);

        // Convert Corese non positive integer to RDF4J non positive integer
        Value rdf4j_non_positive_integer_value = CoreseDatatypeToRdf4jValue.convert(corese_non_positive_integer);

        // Checks
        assertEquals(rdf4j_non_positive_integer_value.isLiteral(), true);
        Literal rdf4j_non_positive_integer = (Literal) rdf4j_non_positive_integer_value;
        assertEquals(string_value, rdf4j_non_positive_integer.getLabel());
        assertEquals(non_positive_integer_value, rdf4j_non_positive_integer.intValue());
        assertEquals(RDF.xsdnonPositiveInteger, rdf4j_non_positive_integer.getDatatype().stringValue());
        assertEquals(RDF.xsdnonPositiveInteger, corese_non_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedByte() {
        int unsigned_byte_int = 22;
        String string_value = String.valueOf(unsigned_byte_int);

        // Build Corese unsigned byte
        CoreseGenericInteger corese_unsigned_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedByte);

        // Convert Corese unsigned byte to RDF4J unsigned byte
        Value rdf4j_unsigned_byte_value = CoreseDatatypeToRdf4jValue.convert(corese_unsigned_byte);

        // Checks
        assertEquals(rdf4j_unsigned_byte_value.isLiteral(), true);
        Literal rdf4j_unsigned_byte = (Literal) rdf4j_unsigned_byte_value;
        assertEquals(string_value, rdf4j_unsigned_byte.getLabel());
        assertEquals(unsigned_byte_int, rdf4j_unsigned_byte.intValue());
        assertEquals(RDF.xsdunsignedByte, rdf4j_unsigned_byte.getDatatype().stringValue());
        assertEquals(RDF.xsdunsignedByte, corese_unsigned_byte.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedInt() {
        int unsigned_int_value = 22;
        String string_value = String.valueOf(unsigned_int_value);

        // Build Corese unsigned int
        CoreseGenericInteger corese_unsigned_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedInt);

        // Convert Corese unsigned int to RDF4J unsigned int
        Value rdf4j_unsigned_int_value = CoreseDatatypeToRdf4jValue.convert(corese_unsigned_int);

        // Checks
        assertEquals(rdf4j_unsigned_int_value.isLiteral(), true);
        Literal rdf4j_unsigned_int = (Literal) rdf4j_unsigned_int_value;
        assertEquals(string_value, rdf4j_unsigned_int.getLabel());
        assertEquals(unsigned_int_value, rdf4j_unsigned_int.intValue());
        assertEquals(RDF.xsdunsignedInt, rdf4j_unsigned_int.getDatatype().stringValue());
        assertEquals(RDF.xsdunsignedInt, corese_unsigned_int.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedLong() {
        int unsigned_long_value = 22;
        String string_value = String.valueOf(unsigned_long_value);

        // Build Corese unsigned long
        CoreseGenericInteger corese_unsigned_long = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedLong);

        // Convert Corese unsigned long to RDF4J unsigned long
        Value rdf4j_unsigned_long_value = CoreseDatatypeToRdf4jValue.convert(corese_unsigned_long);

        // Checks
        assertEquals(rdf4j_unsigned_long_value.isLiteral(), true);
        Literal rdf4j_unsigned_long = (Literal) rdf4j_unsigned_long_value;
        assertEquals(string_value, rdf4j_unsigned_long.getLabel());
        assertEquals(unsigned_long_value, rdf4j_unsigned_long.longValue());
        assertEquals(RDF.xsdunsignedLong, rdf4j_unsigned_long.getDatatype().stringValue());
        assertEquals(RDF.xsdunsignedLong, corese_unsigned_long.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedShort() {
        int unsigned_short_value = 22;
        String string_value = String.valueOf(unsigned_short_value);

        // Build Corese unsigned short
        CoreseGenericInteger corese_unsigned_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedShort);

        // Convert Corese unsigned short to RDF4J unsigned short
        Value rdf4j_unsigned_short_value = CoreseDatatypeToRdf4jValue.convert(corese_unsigned_short);

        // Checks
        assertEquals(rdf4j_unsigned_short_value.isLiteral(), true);
        Literal rdf4j_unsigned_short = (Literal) rdf4j_unsigned_short_value;
        assertEquals(string_value, rdf4j_unsigned_short.getLabel());
        assertEquals(unsigned_short_value, rdf4j_unsigned_short.shortValue());
        assertEquals(RDF.xsdunsignedShort, rdf4j_unsigned_short.getDatatype().stringValue());
        assertEquals(RDF.xsdunsignedShort, corese_unsigned_short.getDatatype().stringValue());
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build Corese undef
        IDatatype corese_undef = DatatypeMap.createUndef(string_value, undef_datatype);

        // Convert Corese undef to RDF4J undef
        Value rdf4j_undef_value = CoreseDatatypeToRdf4jValue.convert(corese_undef);

        // Checks
        assertEquals(rdf4j_undef_value.isLiteral(), true);
        Literal rdf4j_undef = (Literal) rdf4j_undef_value;
        assertEquals(string_value, rdf4j_undef.getLabel());
        assertEquals(value, rdf4j_undef.shortValue());
        assertEquals(undef_datatype, rdf4j_undef.getDatatype().stringValue());
        assertEquals(undef_datatype, corese_undef.getDatatype().stringValue());
    }

    @Test(expected = ClassCastException.class)
    public void wrongConvert() {
        String string_iri = "http://example.org/bob";

        // Build Corese URI
        IDatatype corese_uri = DatatypeMap.createResource(string_iri);

        // Convert Corese URI to RDF4J IRI
        CoreseDatatypeToRdf4jValue.convertLiteral(corese_uri);
    }
}
