package fr.inria.corese.jenaImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.junit.Test;

import fr.inria.corese.jena.convert.datatypes.CoreseDatatypeToJenaRdfNode;
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

public class TestCoreseDatatypeToJenaRdfNode {

    @Test
    public void convertUri() {
        String string_uri = "http://example.org/bob";

        // Build Corese URI
        IDatatype corese_uri = DatatypeMap.createResource(string_uri);

        // Convert Corese URI to Jena IRI
        RDFNode jena_iri_value = CoreseDatatypeToJenaRdfNode.convert(corese_uri);

        // Checks
        assertEquals(true, jena_iri_value.isURIResource());
        Resource jena_iri = (Resource) jena_iri_value.asResource();
        assertEquals(string_uri, jena_iri.getURI());
    }

    @Test
    public void convertBNode() {
        String string_id = "_:BN_42";

        // Build Corese blank node
        IDatatype corese_blank = DatatypeMap.createBlank(string_id);

        // Convert Corese blank node to Jena blank node
        RDFNode jena_blank_value = CoreseDatatypeToJenaRdfNode.convert(corese_blank);

        // Checks
        assertEquals(true, jena_blank_value.isAnon());
        Resource jena_blank = (Resource) jena_blank_value.asResource();
        assertEquals(string_id, corese_blank.stringValue());
        assertEquals(string_id, jena_blank.toString());
    }

    @Test
    public void convertDouble() {
        double value = 1.234;

        // Build Corese double
        IDatatype corese_double = DatatypeMap.newDouble(value);

        // Convert Corese double to Jena double
        RDFNode jena_double_value = CoreseDatatypeToJenaRdfNode.convert(corese_double);
        // Checks
        assertEquals(jena_double_value.isLiteral(), true);
        Literal jena_double = jena_double_value.asLiteral();
        assertEquals(value, jena_double.getDouble(), 0);
        assertEquals(XSDDatatype.XSDdouble.getURI(), jena_double.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDdouble.getURI(), corese_double.getDatatype().stringValue());
    }

    @Test
    public void convertFloat() {
        float value = 1.234f;

        // Build Corese float
        IDatatype corese_float = DatatypeMap.newFloat(value);

        // Convert Corese float to Jena float
        RDFNode jena_float_value = CoreseDatatypeToJenaRdfNode.convert(corese_float);

        // Checks
        assertEquals(jena_float_value.isLiteral(), true);
        Literal jena_float = jena_float_value.asLiteral();
        assertEquals(value, jena_float.getFloat(), 0);
        assertEquals(XSDDatatype.XSDfloat.getURI(), jena_float.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDfloat.getURI(), corese_float.getDatatype().stringValue());
    }

    @Test
    public void convertDecimal() {
        double double_value = 1.234;
        BigDecimal value = new BigDecimal(String.valueOf(double_value));

        // Build Corese decimal
        IDatatype corese_decimal = DatatypeMap.newDecimal(double_value);

        // Convert Corese decimal to Jena decimal
        RDFNode jena_decimal_value = CoreseDatatypeToJenaRdfNode.convert(corese_decimal);

        // Checks
        assertEquals(jena_decimal_value.isLiteral(), true);
        Literal jena_decimal = (Literal) jena_decimal_value;
        assertEquals(value, jena_decimal.getValue());
        assertEquals(XSDDatatype.XSDdecimal.getURI(), jena_decimal.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDdecimal.getURI(), corese_decimal.getDatatype().stringValue());
    }

    @Test
    public void convertInteger() {
        int value = 4;

        // Build Corese integer
        IDatatype corese_integer = DatatypeMap.newInteger(value);

        // Convert Corese integer to Jena integer
        RDFNode jena_integer_value = CoreseDatatypeToJenaRdfNode.convert(corese_integer);

        // Checks
        assertEquals(jena_integer_value.isLiteral(), true);
        Literal jena_integer = (Literal) jena_integer_value;
        assertEquals(value, jena_integer.getValue());
        assertEquals(XSDDatatype.XSDinteger.getURI(), jena_integer.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDinteger.getURI(), corese_integer.getDatatype().stringValue());
    }

    @Test
    public void convertBoolean() {
        boolean value = true;

        // Build Corese boolean
        IDatatype corese_boolean = DatatypeMap.newInstance(value);

        // Convert Corese boolean to Jena boolean
        RDFNode jena_boolean_value = CoreseDatatypeToJenaRdfNode.convert(corese_boolean);

        // Checks
        assertEquals(jena_boolean_value.isLiteral(), true);
        Literal jena_boolean = (Literal) jena_boolean_value;
        assertEquals(value, jena_boolean.getBoolean());
        assertEquals(XSDDatatype.XSDboolean.getURI(), jena_boolean.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDboolean.getURI(), corese_boolean.getDatatype().stringValue());
    }

    @Test
    public void convertAnyUri() {
        String value = "http://example.org/bob";

        // Build Corese any uri
        IDatatype corese_any_uri = DatatypeMap.createLiteral(value, XSDDatatype.XSDanyURI.getURI());

        // Convert Corese any uri to Jena any uri
        RDFNode jena_any_uri_value = CoreseDatatypeToJenaRdfNode.convert(corese_any_uri);

        // Checks
        assertEquals(jena_any_uri_value.isLiteral(), true);
        Literal jena_any_uri = (Literal) jena_any_uri_value;
        assertEquals(value, jena_any_uri.getValue());
        assertEquals(XSDDatatype.XSDanyURI.getURI(), jena_any_uri.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDanyURI.getURI(), corese_any_uri.getDatatype().stringValue());

    }

    @Test
    public void convertXmlLiteral() {
        String value = "<span xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">シェイクスピアの<ruby><rbc><rb>演</rb><rb>劇</rb></rbc><rtc><rt>えん</rt><rt>げき</rt></rtc></ruby></span>";

        // Build Corese XML literal
        IDatatype corese_xml_literal = DatatypeMap.createLiteral(value, XMLLiteralType.theXMLLiteralType.getURI());

        // Convert Corese XML literal to Jena XML literal
        RDFNode jena_xml_literal_value = CoreseDatatypeToJenaRdfNode.convert(corese_xml_literal);

        // Checks
        assertEquals(jena_xml_literal_value.isLiteral(), true);
        Literal jena_xml_literal = (Literal) jena_xml_literal_value;
        assertEquals(value, jena_xml_literal.getValue());
        assertEquals(XMLLiteralType.theXMLLiteralType.getURI(), jena_xml_literal.getDatatype().getURI());
        assertEquals(XMLLiteralType.theXMLLiteralType.getURI(), corese_xml_literal.getDatatype().stringValue());
    }

    @Test
    public void convertDate() {
        String value = "2021-06-16";

        // Build Corese date
        CoreseDate corese_date = (CoreseDate) DatatypeMap.newDate(value);

        // Convert Corese date to Jena date
        RDFNode jena_date_value = CoreseDatatypeToJenaRdfNode.convert(corese_date);

        // Checks
        assertEquals(jena_date_value.isLiteral(), true);
        Literal jena_date = (Literal) jena_date_value;
        assertEquals(value, jena_date.getString());
        assertEquals(XSDDatatype.XSDdate.getURI(), jena_date.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDdate.getURI(), corese_date.getDatatype().stringValue());
    }

    @Test
    public void convertDateTime() {
        String value = "2021-06-17T07:12:19";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to Jena date
        RDFNode jena_date_time_value = CoreseDatatypeToJenaRdfNode.convert(corese_date_time);

        // Checks
        assertEquals(jena_date_time_value.isLiteral(), true);
        Literal jena_date_time = (Literal) jena_date_time_value;
        assertEquals(value, jena_date_time.getString());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), jena_date_time.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), corese_date_time.getDatatype().stringValue());
    }

    @Test
    public void convertDateTimeMilisecond() {

        String value = "2021-07-16T16:28:36.477";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to Jena date
        RDFNode jena_date_time_value = CoreseDatatypeToJenaRdfNode.convert(corese_date_time);

        assertEquals(value, corese_date_time.getLabel());
        Literal jena_date_time = (Literal) jena_date_time_value;
        assertEquals(value, jena_date_time.getString());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), jena_date_time.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), corese_date_time.getDatatype().stringValue());

    }

    @Test
    public void convertLiteralLang() {
        String value = "Un super test";
        String lang = "fr";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.createLiteral(value,
                RDFLangString.rdfLangString.getURI(), lang);

        // Convert Corese literal lang to Jena literal lang
        RDFNode jena_literal_value = CoreseDatatypeToJenaRdfNode.convert(corese_literal);
        // Checks
        assertEquals(jena_literal_value.isLiteral(), true);
        Literal jena_literal = (Literal) jena_literal_value;
        assertEquals(value, jena_literal.getValue());
        assertEquals(RDFLangString.rdfLangString.getURI(), jena_literal.getDatatype().getURI());
        assertEquals(RDFLangString.rdfLangString.getURI(), corese_literal.getDatatype().stringValue());
        assertEquals("fr", jena_literal.getLanguage());
    }

    @Test
    public void convertLiteralLangXhithoutLang() {
        String value = "Un super test";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.newInstance(value,
                RDFLangString.rdfLangString.getURI());

        // Convert Corese literal lang to Jena literal lang
        RDFNode jena_literal_value = CoreseDatatypeToJenaRdfNode.convert(corese_literal);

        // Checks
        assertEquals(jena_literal_value.isLiteral(), true);
        Literal jena_literal = (Literal) jena_literal_value;
        assertEquals(value, jena_literal.getValue());
        assertEquals(XSDDatatype.XSDstring.getURI(), jena_literal.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDstring.getURI(), corese_literal.getDatatype().stringValue());
        assertEquals("", jena_literal.getLanguage());
    }

    @Test
    public void convertString() {
        String value = "Un super test";

        // Build Corese string
        CoreseString corese_string = (CoreseString) DatatypeMap.newStringBuilder(value);

        // Convert Corese string to Jena string
        RDFNode jena_string_value = CoreseDatatypeToJenaRdfNode.convert(corese_string);

        // Checks
        assertEquals(jena_string_value.isLiteral(), true);
        Literal jena_string = (Literal) jena_string_value;
        assertEquals(value, jena_string.getValue());
        assertEquals(XSDDatatype.XSDstring.getURI(), jena_string.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDstring.getURI(), corese_string.getDatatype().stringValue());
        assertEquals("", jena_string.getLanguage());
    }

    @Test
    public void convertStringBuilder() {
        String value = "Un super test";

        // Build Corese string builder
        CoreseStringBuilder corese_string_builder = (CoreseStringBuilder) DatatypeMap.newStringBuilder(value);

        // Convert Corese string builder to Jena string builder
        RDFNode jena_string_builder_value = CoreseDatatypeToJenaRdfNode.convert(corese_string_builder);

        // Checks
        assertEquals(jena_string_builder_value.isLiteral(), true);
        Literal jena_string_builder = (Literal) jena_string_builder_value;
        assertEquals(value, jena_string_builder.getValue());
        assertEquals(XSDDatatype.XSDstring.getURI(), jena_string_builder.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDstring.getURI(), corese_string_builder.getDatatype().stringValue());
        assertEquals("", jena_string_builder.getLanguage());
    }

    @Test
    public void convertYear() {
        int int_value = 2021;
        String value = String.valueOf(int_value);

        // Build Corese year
        CoreseYear corese_year = (CoreseYear) DatatypeMap.createLiteral(value, XSDDatatype.XSDgYear.getURI());

        // Convert Corese year to Jena year
        RDFNode jena_year_value = CoreseDatatypeToJenaRdfNode.convert(corese_year);

        // Checks
        assertEquals(jena_year_value.isLiteral(), true);
        Literal jena_year = (Literal) jena_year_value;
        assertEquals(value, jena_year.getString());
        assertEquals(XSDDatatype.XSDgYear.getURI(), jena_year.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDgYear.getURI(), corese_year.getDatatype().stringValue());
    }

    @Test
    public void convertMonth() {
        int int_value = 6;
        String string_value = String.valueOf(int_value);

        // Build Corese month
        CoreseMonth corese_month = (CoreseMonth) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDgMonth.getURI());

        // Convert Corese month to Jena month
        RDFNode jena_month_value = CoreseDatatypeToJenaRdfNode.convert(corese_month);

        // Checks
        assertEquals(jena_month_value.isLiteral(), true);
        Literal jena_month = (Literal) jena_month_value;
        assertEquals("--0" + string_value, jena_month.getString());
        assertEquals(XSDDatatype.XSDgMonth.getURI(), jena_month.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDgMonth.getURI(), corese_month.getDatatype().stringValue());
    }

    @Test
    public void convertDay() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese day
        CoreseDay corese_day = (CoreseDay) DatatypeMap.createLiteral(string_value, XSDDatatype.XSDgDay.getURI());

        // Convert Corese day to Jena day
        RDFNode jena_day_value = CoreseDatatypeToJenaRdfNode.convert(corese_day);

        // Checks
        assertEquals(jena_day_value.isLiteral(), true);
        Literal jena_day = (Literal) jena_day_value;
        assertEquals("---" + string_value, jena_day.getString());
        assertEquals(XSDDatatype.XSDgDay.getURI(), jena_day.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDgDay.getURI(), corese_day.getDatatype().stringValue());
    }

    @Test
    public void convertByte() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese byte
        CoreseGenericInteger corese_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDbyte.getURI());

        // Convert Corese byte to Jena byte
        RDFNode jena_byte_value = CoreseDatatypeToJenaRdfNode.convert(corese_byte);

        // Checks
        assertEquals(jena_byte_value.isLiteral(), true);
        Literal jena_byte = (Literal) jena_byte_value;
        assertEquals(string_value, jena_byte.getString());
        assertEquals(int_value, jena_byte.getInt());
        assertEquals(XSDDatatype.XSDbyte.getURI(), jena_byte.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDbyte.getURI(), corese_byte.getDatatype().stringValue());
    }

    @Test
    public void convertShort() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese short
        CoreseGenericInteger corese_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDshort.getURI());

        // Convert Corese short to Jena short
        RDFNode jena_short_value = CoreseDatatypeToJenaRdfNode.convert(corese_short);

        // Checks
        assertEquals(jena_short_value.isLiteral(), true);
        Literal jena_short = (Literal) jena_short_value;
        assertEquals(string_value, jena_short.getString());
        assertEquals(int_value, jena_short.getInt());
        assertEquals(int_value, jena_short.getShort());
        assertEquals(XSDDatatype.XSDshort.getURI(), jena_short.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDshort.getURI(), corese_short.getDatatype().stringValue());
    }

    @Test
    public void convertInt() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese int
        CoreseGenericInteger corese_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDint.getURI());

        // Convert Corese int to Jena int
        RDFNode jena_int_value = CoreseDatatypeToJenaRdfNode.convert(corese_int);

        // Checks
        assertEquals(jena_int_value.isLiteral(), true);
        Literal jena_int = (Literal) jena_int_value;
        assertEquals(string_value, jena_int.getString());
        assertEquals(int_value, jena_int.getInt());
        assertEquals(XSDDatatype.XSDint.getURI(), jena_int.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDint.getURI(), corese_int.getDatatype().stringValue());
    }

    @Test
    public void convertPositiveInteger() {
        int positive_integer_value = 22;
        String string_value = String.valueOf(positive_integer_value);

        // Build Corese positive integer
        CoreseGenericInteger corese_positive_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDpositiveInteger.getURI());

        // Convert Corese positive integer to Jena positive integer
        RDFNode jena_positive_integer_value = CoreseDatatypeToJenaRdfNode.convert(corese_positive_integer);

        // Checks
        assertEquals(jena_positive_integer_value.isLiteral(), true);
        Literal jena_positive_integer = (Literal) jena_positive_integer_value;
        assertEquals(string_value, jena_positive_integer.getString());
        assertEquals(positive_integer_value, jena_positive_integer.getInt());
        assertEquals(XSDDatatype.XSDpositiveInteger.getURI(), jena_positive_integer.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDpositiveInteger.getURI(), corese_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNegativeInteger() {
        int negative_integer_value = -22;
        String string_value = String.valueOf(negative_integer_value);

        // Build Corese negative integer
        CoreseGenericInteger corese_negative_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDnegativeInteger.getURI());

        // Convert Corese negative integer to Jena negative integer
        RDFNode jena_negative_integer_value = CoreseDatatypeToJenaRdfNode.convert(corese_negative_integer);

        // Checks
        assertEquals(jena_negative_integer_value.isLiteral(), true);
        Literal jena_negative_integer = (Literal) jena_negative_integer_value;
        assertEquals(string_value, jena_negative_integer.getString());
        assertEquals(negative_integer_value, jena_negative_integer.getInt());
        assertEquals(XSDDatatype.XSDnegativeInteger.getURI(), jena_negative_integer.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDnegativeInteger.getURI(), corese_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonNegativeInteger() {
        int non_negative_integer_value = 0;
        String string_value = String.valueOf(non_negative_integer_value);

        // Build Corese non negative integer
        CoreseGenericInteger corese_non_negative_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, XSDDatatype.XSDnonNegativeInteger.getURI());

        // Convert Corese non negative integer to Jena non negative integer
        RDFNode jena_non_negative_integer_value = CoreseDatatypeToJenaRdfNode.convert(corese_non_negative_integer);

        // Checks
        assertEquals(jena_non_negative_integer_value.isLiteral(), true);
        Literal jena_non_negative_integer = (Literal) jena_non_negative_integer_value;
        assertEquals(string_value, jena_non_negative_integer.getString());
        assertEquals(non_negative_integer_value, jena_non_negative_integer.getInt());
        assertEquals(XSDDatatype.XSDnonNegativeInteger.getURI(), jena_non_negative_integer.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDnonNegativeInteger.getURI(),
                corese_non_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonPositiveInteger() {
        int non_positive_integer_value = 0;
        String string_value = String.valueOf(non_positive_integer_value);

        // Build Corese non positive integer
        CoreseGenericInteger corese_non_positive_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, XSDDatatype.XSDnonPositiveInteger.getURI());

        // Convert Corese non positive integer to Jena non positive integer
        RDFNode jena_non_positive_integer_value = CoreseDatatypeToJenaRdfNode.convert(corese_non_positive_integer);

        // Checks
        assertEquals(jena_non_positive_integer_value.isLiteral(), true);
        Literal jena_non_positive_integer = (Literal) jena_non_positive_integer_value;
        assertEquals(string_value, jena_non_positive_integer.getString());
        assertEquals(non_positive_integer_value, jena_non_positive_integer.getInt());
        assertEquals(XSDDatatype.XSDnonPositiveInteger.getURI(), jena_non_positive_integer.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDnonPositiveInteger.getURI(),
                corese_non_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedByte() {
        int unsigned_byte_int = 22;
        String string_value = String.valueOf(unsigned_byte_int);
        byte byte_value = Byte.valueOf(string_value);

        // Build Corese unsigned byte
        CoreseGenericInteger corese_unsigned_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDunsignedByte.getURI());

        // Convert Corese unsigned byte to Jena unsigned byte
        RDFNode jena_unsigned_byte_value = CoreseDatatypeToJenaRdfNode.convert(corese_unsigned_byte);

        // Checks
        assertEquals(jena_unsigned_byte_value.isLiteral(), true);
        Literal jena_unsigned_byte = (Literal) jena_unsigned_byte_value;
        assertEquals(string_value, jena_unsigned_byte.getString());
        assertEquals(unsigned_byte_int, jena_unsigned_byte.getInt());
        assertEquals(byte_value, jena_unsigned_byte.getByte());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), jena_unsigned_byte.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), corese_unsigned_byte.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedInt() {
        int unsigned_int_value = 22;
        String string_value = String.valueOf(unsigned_int_value);

        // Build Corese unsigned int
        CoreseGenericInteger corese_unsigned_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDunsignedInt.getURI());

        // Convert Corese unsigned int to Jena unsigned int
        RDFNode jena_unsigned_int_value = CoreseDatatypeToJenaRdfNode.convert(corese_unsigned_int);

        // Checks
        assertEquals(jena_unsigned_int_value.isLiteral(), true);
        Literal jena_unsigned_int = (Literal) jena_unsigned_int_value;
        assertEquals(string_value, jena_unsigned_int.getString());
        assertEquals(unsigned_int_value, jena_unsigned_int.getInt());
        assertEquals(XSDDatatype.XSDunsignedInt.getURI(), jena_unsigned_int.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDunsignedInt.getURI(), corese_unsigned_int.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedLong() {
        int unsigned_long_value = 22;
        String string_value = String.valueOf(unsigned_long_value);

        // Build Corese unsigned long
        CoreseGenericInteger corese_unsigned_long = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDunsignedLong.getURI());

        // Convert Corese unsigned long to Jena unsigned long
        RDFNode jena_unsigned_long_value = CoreseDatatypeToJenaRdfNode.convert(corese_unsigned_long);

        // Checks
        assertEquals(jena_unsigned_long_value.isLiteral(), true);
        Literal jena_unsigned_long = (Literal) jena_unsigned_long_value;
        assertEquals(string_value, jena_unsigned_long.getString());
        assertEquals(unsigned_long_value, jena_unsigned_long.getLong());
        assertEquals(XSDDatatype.XSDunsignedLong.getURI(), jena_unsigned_long.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDunsignedLong.getURI(), corese_unsigned_long.getDatatype().stringValue());
    }

    @Test
    public void convertUsignedShort() {
        int unsigned_short_value = 22;
        String string_value = String.valueOf(unsigned_short_value);

        // Build Corese unsigned short
        CoreseGenericInteger corese_unsigned_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                XSDDatatype.XSDunsignedShort.getURI());

        // Convert Corese unsigned short to Jena unsigned short
        RDFNode jena_unsigned_short_value = CoreseDatatypeToJenaRdfNode.convert(corese_unsigned_short);

        // Checks
        assertEquals(jena_unsigned_short_value.isLiteral(), true);
        Literal jena_unsigned_short = (Literal) jena_unsigned_short_value;
        assertEquals(string_value, jena_unsigned_short.getString());
        assertEquals(unsigned_short_value, jena_unsigned_short.getShort());
        assertEquals(XSDDatatype.XSDunsignedShort.getURI(), jena_unsigned_short.getDatatype().getURI());
        assertEquals(XSDDatatype.XSDunsignedShort.getURI(), corese_unsigned_short.getDatatype().stringValue());
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build Corese undef
        IDatatype corese_undef = DatatypeMap.createUndef(string_value, undef_datatype);

        // Convert Corese undef to Jena undef
        RDFNode jena_undef_value = CoreseDatatypeToJenaRdfNode.convert(corese_undef);

        // Checks
        assertEquals(jena_undef_value.isLiteral(), true);
        Literal jena_undef = (Literal) jena_undef_value;
        assertEquals(string_value, jena_undef.getString());
        assertEquals(undef_datatype, jena_undef.getDatatype().getURI());
        assertEquals(undef_datatype, corese_undef.getDatatype().stringValue());
    }
}
