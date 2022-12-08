package fr.inria.corese.jenaImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import fr.inria.corese.jena.convert.datatypes.JenaRdfNodeToCoreseDatatype;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.CoreseBlankNode;
import fr.inria.corese.sparql.datatype.CoreseBoolean;
import fr.inria.corese.sparql.datatype.CoreseDate;
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

public class TestJenaRdfNodeToCoreseDatatype {

    private Model model = ModelFactory.createDefaultModel();

    @Test
    public void convertIri() {
        String string_iri = "http://example.org/bob";

        // Build Jena Resource
        Resource jena_uri = ResourceFactory.createResource(string_iri);

        // Convert Jena Resource to Corese URI
        IDatatype corese_uri_value = JenaRdfNodeToCoreseDatatype.convert(jena_uri);

        // Checks
        assertEquals(true, jena_uri.isURIResource());
        assertEquals(false, jena_uri.isLiteral());
        assertEquals(false, jena_uri.isAnon());
        CoreseURI corese_uri = (CoreseURI) corese_uri_value;
        assertEquals(string_iri, corese_uri.stringValue());
    }

    @Test
    public void convertBNode() {
        String string_id = "BN_42";

        // Build Jena blank node
        RDFNode jena_blank = this.model.createResource(new AnonId(string_id));

        // Convert Jena blank node to Corese blank node
        IDatatype corese_blank_value = JenaRdfNodeToCoreseDatatype.convert(jena_blank);

        // Checks
        assertEquals(true, corese_blank_value.isBlank());
        CoreseBlankNode corese_blank = (CoreseBlankNode) corese_blank_value;
        assertEquals(string_id, corese_blank.toString());
        assertEquals(string_id, jena_blank.toString());
    }

    @Test
    public void convertDouble() {
        String string_value = "1.234";
        double value = Double.parseDouble(string_value);

        // Build Jena double
        Literal jena_double = this.model.createTypedLiteral(string_value, XSDDatatype.XSDdouble);

        // Convert Jena double to Corese double
        IDatatype corese_double_value = JenaRdfNodeToCoreseDatatype.convert(jena_double);
        CoreseDouble corese_double = (CoreseDouble) corese_double_value;

        // Checks
        assertEquals(true, corese_double.isLiteral());
        assertEquals(value, corese_double.doubleValue(), 0);
        assertEquals(XSDDatatype.XSDdouble.getURI(), corese_double.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDdouble.getURI(), jena_double.getDatatypeURI());
    }

    @Test
    public void convertFloat() {
        String string_value = "1.234";
        float value = Float.parseFloat(string_value);

        // Build Jena float
        Literal jena_float = this.model.createTypedLiteral(string_value, XSDDatatype.XSDfloat);

        // Convert Jena float to Corese float
        IDatatype corese_float_value = JenaRdfNodeToCoreseDatatype.convert(jena_float);
        CoreseDouble corese_float = (CoreseDouble) corese_float_value;

        // Checks
        assertEquals(true, corese_float.isFloat());
        assertEquals(value, corese_float.floatValue(), 0);
        assertEquals(XSDDatatype.XSDfloat.getURI(), corese_float.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDfloat.getURI(), jena_float.getDatatypeURI());
    }

    @Test
    public void convertDecimal() {
        String string_value = "1098491072963113850.7436076939614540479";
        double double_value = Double.parseDouble(string_value);
        BigDecimal value = new BigDecimal(string_value);

        // Build Jena decimal
        Literal jena_decimal = this.model.createTypedLiteral(string_value, XSDDatatype.XSDdecimal);

        // Convert Jena decimal to Corese decimal
        IDatatype corese_decimal_value = JenaRdfNodeToCoreseDatatype.convert(jena_decimal);
        CoreseDecimal corese_decimal = (CoreseDecimal) corese_decimal_value;

        // Checks
        assertEquals(true, corese_decimal.isDecimal());
        assertEquals(value, corese_decimal.decimalValue());
        assertEquals(double_value, corese_decimal.doubleValue(), 0);
        assertEquals(XSDDatatype.XSDdecimal.getURI(), corese_decimal.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDdecimal.getURI(), jena_decimal.getDatatypeURI());
    }

    @Test
    public void convertInteger() {
        int value = 4;
        String string_value = String.valueOf(value);

        // Build Jena integer
        Literal jena_integer = this.model.createTypedLiteral(string_value, XSDDatatype.XSDinteger);
        System.out.println(jena_integer.getDatatypeURI());

        // Convert Jena integer to Corese integer
        IDatatype corese_integer_value = JenaRdfNodeToCoreseDatatype.convert(jena_integer);
        CoreseInteger corese_integer = (CoreseInteger) corese_integer_value;

        // Checks
        assertEquals(true, corese_integer.isInteger());
        assertEquals(value, corese_integer.intValue(), 0);
        assertEquals(XSDDatatype.XSDinteger.getURI(), corese_integer.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDinteger.getURI(), jena_integer.getDatatypeURI());
    }

    @Test
    public void convertYear() {
        int value = 2021;
        String string_value = String.valueOf(value);

        // Build Jena year
        Literal jena_year = this.model.createTypedLiteral(string_value, XSDDatatype.XSDgYear);

        // Convert Jena year to Corese year
        IDatatype corese_year_value = JenaRdfNodeToCoreseDatatype.convert(jena_year);
        CoreseYear corese_year = (CoreseYear) corese_year_value;

        // Checks
        assertEquals(true, corese_year.isDateElement());
        assertEquals(value, corese_year.intValue());
        assertEquals(XSDDatatype.XSDgYear.getURI(), corese_year.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDgYear.getURI(), jena_year.getDatatypeURI());
    }

    @Test
    public void convertMonth() {
        String string_value = "--12";
        int value = 12;

        // Build Jena month
        Literal jena_month = this.model.createTypedLiteral(string_value, XSDDatatype.XSDgMonth);

        // Convert Jena month to Corese month
        IDatatype corese_month_value = JenaRdfNodeToCoreseDatatype.convert(jena_month);
        CoreseMonth corese_month = (CoreseMonth) corese_month_value;

        // Checks
        assertEquals(true, corese_month.isDateElement());
        assertEquals(value, corese_month.intValue());
        assertEquals(XSDDatatype.XSDgMonth.getURI(), corese_month.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDgMonth.getURI(), jena_month.getDatatypeURI());
    }

    @Test
    public void convertDay() {
        String string_value = "---12";
        int value = 12;

        // Build Jena day
        Literal jena_day = this.model.createTypedLiteral(string_value, XSDDatatype.XSDgDay);

        // Convert Jena day to Corese day
        IDatatype corese_day_value = JenaRdfNodeToCoreseDatatype.convert(jena_day);
        CoreseDay corese_day = (CoreseDay) corese_day_value;

        // Checks
        assertEquals(true, corese_day.isDateElement());
        assertEquals(value, corese_day.intValue());
        assertEquals(XSDDatatype.XSDgDay.getURI(), corese_day.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDgDay.getURI(), jena_day.getDatatypeURI());
    }

    @Test
    public void convertByte() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena byte
        Literal jena_byte = this.model.createTypedLiteral(string_value, XSDDatatype.XSDbyte);

        // Convert Jena byte to Corese byte
        IDatatype corese_byte_value = JenaRdfNodeToCoreseDatatype.convert(jena_byte);
        CoreseGenericInteger corese_byte = (CoreseGenericInteger) corese_byte_value;

        // Checks
        assertEquals(true, corese_byte.isInteger());
        assertEquals(value, corese_byte.intValue());
        assertEquals(XSDDatatype.XSDbyte.getURI(), corese_byte.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDbyte.getURI(), jena_byte.getDatatypeURI());
    }

    @Test
    public void convertShort() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena short
        Literal jena_short = this.model.createTypedLiteral(string_value, XSDDatatype.XSDshort);

        // Convert Jena short to Corese short
        IDatatype corese_short_value = JenaRdfNodeToCoreseDatatype.convert(jena_short);
        CoreseGenericInteger corese_short = (CoreseGenericInteger) corese_short_value;

        // Checks
        assertEquals(true, corese_short.isInteger());
        assertEquals(value, corese_short.intValue());
        assertEquals(XSDDatatype.XSDshort.getURI(), corese_short.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDshort.getURI(), jena_short.getDatatypeURI());
    }

    @Test
    public void convertInt() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena int
        Literal jena_int = this.model.createTypedLiteral(string_value, XSDDatatype.XSDint);

        // Convert Jena int to Corese int
        IDatatype corese_int_value = JenaRdfNodeToCoreseDatatype.convert(jena_int);
        CoreseGenericInteger corese_int = (CoreseGenericInteger) corese_int_value;

        // Checks
        assertEquals(true, corese_int.isInteger());
        assertEquals(value, corese_int.intValue());
        assertEquals(XSDDatatype.XSDint.getURI(), corese_int.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDint.getURI(), jena_int.getDatatypeURI());
    }

    @Test
    public void convertPositiveInteger() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena positive integer
        Literal jena_positive_integer = this.model.createTypedLiteral(string_value, XSDDatatype.XSDpositiveInteger);

        // Convert Jena positive integer to Corese positive integer
        IDatatype corese_positive_integer_value = JenaRdfNodeToCoreseDatatype.convert(jena_positive_integer);
        CoreseGenericInteger corese_positive_integer = (CoreseGenericInteger) corese_positive_integer_value;

        // Checks
        assertEquals(true, corese_positive_integer.isInteger());
        assertEquals(value, corese_positive_integer.intValue());
        assertEquals(XSDDatatype.XSDpositiveInteger.getURI(), corese_positive_integer.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDpositiveInteger.getURI(), jena_positive_integer.getDatatypeURI());
    }

    @Test
    public void convertNegativeInteger() {
        int value = -22;
        String string_value = String.valueOf(value);

        // Build Jena negative integer
        Literal jena_negative_integer = this.model.createTypedLiteral(string_value, XSDDatatype.XSDnegativeInteger);

        // Convert Jena negative integer to Corese negative integer
        IDatatype corese_negative_integer_value = JenaRdfNodeToCoreseDatatype.convert(jena_negative_integer);
        CoreseGenericInteger corese_negative_integer = (CoreseGenericInteger) corese_negative_integer_value;

        // Checks
        assertEquals(true, corese_negative_integer.isInteger());
        assertEquals(value, corese_negative_integer.intValue());
        assertEquals(XSDDatatype.XSDnegativeInteger.getURI(), corese_negative_integer.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDnegativeInteger.getURI(), jena_negative_integer.getDatatypeURI());
    }

    @Test
    public void convertNonNegativeInteger() {
        int value = 0;
        String string_value = String.valueOf(value);

        // Build Jena non negative integer
        Literal jena_non_negative_integer = this.model.createTypedLiteral(string_value,
                XSDDatatype.XSDnonNegativeInteger);

        // Convert Jena non negative integer to Corese non negative integer
        IDatatype corese_non_negative_integer_value = JenaRdfNodeToCoreseDatatype.convert(jena_non_negative_integer);
        CoreseGenericInteger corese_non_negative_integer = (CoreseGenericInteger) corese_non_negative_integer_value;

        // Checks
        assertEquals(true, corese_non_negative_integer.isInteger());
        assertEquals(value, corese_non_negative_integer.intValue());
        assertEquals(XSDDatatype.XSDnonNegativeInteger.getURI(),
                corese_non_negative_integer.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDnonNegativeInteger.getURI(), jena_non_negative_integer.getDatatypeURI());
    }

    @Test
    public void convertNonPositiveInteger() {
        int value = 0;
        String string_value = String.valueOf(value);

        // Build Jena non positive integer
        Literal jena_non_positive_integer = this.model.createTypedLiteral(string_value,
                XSDDatatype.XSDnonPositiveInteger);

        // Convert Jena non positive integer to Corese non positive integer
        IDatatype corese_non_positive_integer_value = JenaRdfNodeToCoreseDatatype.convert(jena_non_positive_integer);
        CoreseGenericInteger corese_non_positive_integer = (CoreseGenericInteger) corese_non_positive_integer_value;

        // Checks
        assertEquals(true, corese_non_positive_integer.isInteger());
        assertEquals(value, corese_non_positive_integer.intValue());
        assertEquals(XSDDatatype.XSDnonPositiveInteger.getURI(),
                corese_non_positive_integer.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDnonPositiveInteger.getURI(), jena_non_positive_integer.getDatatypeURI());
    }

    @Test
    public void convertUnsignedByte() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena unsigned byte
        Literal jena_unsigned_byte = this.model.createTypedLiteral(string_value, XSDDatatype.XSDunsignedByte);

        // Convert Jena unsigned byte to Corese unsigned byte
        IDatatype corese_unsigned_byte_value = JenaRdfNodeToCoreseDatatype.convert(jena_unsigned_byte);
        CoreseGenericInteger corese_unsigned_byte = (CoreseGenericInteger) corese_unsigned_byte_value;

        // Checks
        assertEquals(true, corese_unsigned_byte.isInteger());
        assertEquals(value, corese_unsigned_byte.intValue());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), corese_unsigned_byte.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), jena_unsigned_byte.getDatatypeURI());
    }

    @Test
    public void convertUnsignedInt() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena unsigned int
        Literal jena_unsigned_int = this.model.createTypedLiteral(string_value, XSDDatatype.XSDunsignedByte);

        // Convert Jena unsigned int to Corese unsigned int
        IDatatype corese_unsigned_int_value = JenaRdfNodeToCoreseDatatype.convert(jena_unsigned_int);
        CoreseGenericInteger corese_unsigned_int = (CoreseGenericInteger) corese_unsigned_int_value;

        // Checks
        assertEquals(true, corese_unsigned_int.isInteger());
        assertEquals(value, corese_unsigned_int.intValue());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), corese_unsigned_int.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDunsignedByte.getURI(), jena_unsigned_int.getDatatypeURI());
    }

    @Test
    public void convertUnsignedLong() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena unsigned long
        Literal jena_unsigned_long = this.model.createTypedLiteral(string_value, XSDDatatype.XSDunsignedLong);

        // Convert Jena unsigned long to Corese unsigned long
        IDatatype corese_unsigned_long_value = JenaRdfNodeToCoreseDatatype.convert(jena_unsigned_long);
        CoreseGenericInteger corese_unsigned_long = (CoreseGenericInteger) corese_unsigned_long_value;

        // Checks
        assertEquals(true, corese_unsigned_long.isInteger());
        assertEquals(value, corese_unsigned_long.intValue());
        assertEquals(XSDDatatype.XSDunsignedLong.getURI(), corese_unsigned_long.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDunsignedLong.getURI(), jena_unsigned_long.getDatatypeURI());
    }

    @Test
    public void convertUnsignedShort() {
        int value = 22;
        String string_value = String.valueOf(value);

        // Build Jena unsigned short
        Literal jena_unsigned_short = this.model.createTypedLiteral(string_value, XSDDatatype.XSDunsignedShort);

        // Convert Jena unsigned short to Corese unsigned short
        IDatatype corese_unsigned_short_value = JenaRdfNodeToCoreseDatatype.convert(jena_unsigned_short);
        CoreseGenericInteger corese_unsigned_short = (CoreseGenericInteger) corese_unsigned_short_value;

        // Checks
        assertEquals(true, corese_unsigned_short.isInteger());
        assertEquals(value, corese_unsigned_short.intValue());
        assertEquals(XSDDatatype.XSDunsignedShort.getURI(), corese_unsigned_short.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDunsignedShort.getURI(), jena_unsigned_short.getDatatypeURI());
    }

    @Test
    public void convertBoolean() {
        Boolean value = true;
        String string_value = String.valueOf(value);

        // Build Jena boolean
        Literal jena_boolean = this.model.createTypedLiteral(string_value, XSDDatatype.XSDboolean);

        // Convert Jena boolean to Corese boolean
        IDatatype corese_boolean_value = JenaRdfNodeToCoreseDatatype.convert(jena_boolean);
        CoreseBoolean corese_boolean = (CoreseBoolean) corese_boolean_value;

        // Checks
        assertEquals(true, corese_boolean.isBoolean());
        assertEquals(value, corese_boolean.booleanValue());
        assertEquals(XSDDatatype.XSDboolean.getURI(), corese_boolean.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDboolean.getURI(), jena_boolean.getDatatypeURI());
    }

    @Test
    public void convertAnyUri() {
        String value = "http://example.org/bob";

        // Build Jena any UTI
        Literal jena_any_uri = this.model.createTypedLiteral(value, XSDDatatype.XSDanyURI);

        // Convert Jena any UTI to Corese any UTI
        IDatatype corese_any_uri_value = JenaRdfNodeToCoreseDatatype.convert(jena_any_uri);
        CoreseURILiteral corese_any_uri = (CoreseURILiteral) corese_any_uri_value;

        // Checks
        assertEquals(value, corese_any_uri.stringValue());
        assertEquals(XSDDatatype.XSDanyURI.getURI(), corese_any_uri.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDanyURI.getURI(), jena_any_uri.getDatatypeURI());
    }

    @Test
    public void convertXmlLiteral() {
        String value = "<span xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">シェイクスピアの<ruby><rbc><rb>演</rb><rb>劇</rb></rbc><rtc><rt>えん</rt><rt>げき</rt></rtc></ruby></span>";

        // Build Jena XML literal
        Literal jena_xml_literal = this.model.createTypedLiteral(value, XMLLiteralType.theXMLLiteralType.getURI());

        // Convert Jena XML literal to Corese XML literal
        IDatatype corese_xml_literal_value = JenaRdfNodeToCoreseDatatype.convert(jena_xml_literal);
        CoreseXMLLiteral corese_xml_literal = (CoreseXMLLiteral) corese_xml_literal_value;

        // Checks
        assertEquals(value, corese_xml_literal.stringValue());
        assertEquals(XMLLiteralType.theXMLLiteralType.getURI(), corese_xml_literal.getDatatype().stringValue());
        assertEquals(XMLLiteralType.theXMLLiteralType.getURI(), jena_xml_literal.getDatatypeURI());
    }

    @Test
    public void convertDate() {
        String value = "2021-06-16";

        // Build Jena date
        Literal jena_date = this.model.createTypedLiteral(value, XSDDatatype.XSDdate);

        // Convert Jena date to Corese date
        IDatatype corese_date_value = JenaRdfNodeToCoreseDatatype.convert(jena_date);
        CoreseDate corese_date = (CoreseDate) corese_date_value;

        // Checks
        assertEquals(value, corese_date.stringValue());
        assertEquals(XSDDatatype.XSDdate.getURI(), corese_date.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDdate.getURI(), jena_date.getDatatypeURI());
    }

    @Test
    public void convertDateTime() {
        String value = "2021-06-17T07:12:19";

        // Build Jena date and time
        Literal jena_date_time = this.model.createTypedLiteral(value, XSDDatatype.XSDdateTime);

        // Convert Jena date and time to Corese date and time
        IDatatype corese_date_time_value = JenaRdfNodeToCoreseDatatype.convert(jena_date_time);
        CoreseDate corese_date_time = (CoreseDate) corese_date_time_value;

        // Checks
        assertEquals(value, corese_date_time.stringValue());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), corese_date_time.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), jena_date_time.getDatatypeURI());
    }

    @Test
    public void convertDateTimeMilisecond() {
        String value = "2021-07-16T16:28:36.477";

        // Build Jena date and time
        Literal jena_date_time = this.model.createTypedLiteral(value, XSDDatatype.XSDdateTime);

        // Convert Jena date and time to Corese date and time
        IDatatype corese_date_time_value = JenaRdfNodeToCoreseDatatype.convert(jena_date_time);

        assertEquals(value, jena_date_time.getValue().toString());
        assertEquals(value, corese_date_time_value.stringValue());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), corese_date_time_value.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDdateTime.getURI(), jena_date_time.getDatatypeURI());

    }

    @Test
    public void convertLiteralLang() {
        String value = "Un super test";
        String lang = "fr";

        // Build Jena literal lang
        Literal jena_literal_lang = this.model.createLiteral(value, lang);

        // Convert Jena literal lang and time to Corese literal lang
        IDatatype corese_literal_lang_value = JenaRdfNodeToCoreseDatatype.convert(jena_literal_lang);
        CoreseLiteral corese_literal_lang = (CoreseLiteral) corese_literal_lang_value;

        // Checks
        assertEquals(corese_literal_lang.isLiteral(), true);
        assertEquals(value, corese_literal_lang.stringValue());
        assertEquals(RDFLangString.rdfLangString.getURI(), corese_literal_lang.getDatatype().stringValue());
        assertEquals(RDFLangString.rdfLangString.getURI(), jena_literal_lang.getDatatypeURI());
        assertEquals("fr", corese_literal_lang.getLang());
    }

    @Test
    public void convertString() {
        String value = "Un super test";

        // Build Jena string
        Literal jena_string = this.model.createTypedLiteral(value, XSDDatatype.XSDstring);

        // Convert Jena string and time to Corese string
        IDatatype corese_string_value = JenaRdfNodeToCoreseDatatype.convert(jena_string);
        CoreseString corese_string = (CoreseString) corese_string_value;

        // Checks
        assertEquals(corese_string.isLiteral(), true);
        assertEquals(value, corese_string.stringValue());
        assertEquals(XSDDatatype.XSDstring.getURI(), corese_string.getDatatype().stringValue());
        assertEquals(XSDDatatype.XSDstring.getURI(), jena_string.getDatatypeURI());
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build Jena undef
        Literal jena_string = this.model.createTypedLiteral(string_value, undef_datatype);

        // Convert Jena undef and time to Corese undef
        IDatatype corese_string_value = JenaRdfNodeToCoreseDatatype.convert(jena_string);
        CoreseUndefLiteral corese_string = (CoreseUndefLiteral) corese_string_value;

        // Checks
        assertEquals(corese_string.isLiteral(), true);
        assertEquals(undef_datatype, corese_string.getDatatype().stringValue());
        assertEquals(undef_datatype, jena_string.getDatatypeURI());
    }
}