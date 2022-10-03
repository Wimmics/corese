package fr.inria.corese.inteGraalImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.storage.inteGraal.convertDatatype.InteGraalToCoreseDatatype;

public class TestInteGraalToCoreseDatatype {

    private static Matcher parseLiteral(String stringLiteral) {
        Pattern pattern = Pattern.compile("(.*)\\^\\^([^@]*)@?(.*)?");
        Matcher mat = pattern.matcher(stringLiteral);
        mat.find();
        return mat;
    }

    private static String getLiteralValue(String literal) {
        return parseLiteral(literal).group(1);
    }

    private static String getLiteralDatatype(String literal) {
        return parseLiteral(literal).group(2);
    }

    private static String getLiteralLang(String literal) {
        return parseLiteral(literal).group(3);
    }

    // Factories
    private TermFactory termFactory = SameObjectTermFactory.instance();
    private PredicateFactory predicateFactory = SameObjectPredicateFactory.instance();

    @Test
    public void convertPredicate() {
        String string_iri = "http://example.org/bob";

        // Build Graal Resource
        Predicate graal_uri = predicateFactory.createOrGetPredicate(string_iri, 3);

        // Convert Graal Resource to Corese URI
        IDatatype corese_uri = InteGraalToCoreseDatatype.convert(graal_uri);

        // Checks
        assertEquals(graal_uri.getLabel(), corese_uri.stringValue());
    }

    @Test
    public void convertIri() {
        String string_iri = "http://example.org/bob";

        // Build Graal Resource
        Term graal_uri = termFactory.createOrGetConstant(string_iri);

        // Convert Graal Resource to Corese URI
        IDatatype corese_uri = InteGraalToCoreseDatatype.convert(graal_uri);

        // Checks
        assertEquals(graal_uri.getLabel(), corese_uri.stringValue());
    }

    @Test
    public void undefType() {
        String string_iri = "http://example.org/bob";

        // Build Graal Resource
        Term graal_uri = termFactory.createOrGetVariable(string_iri);

        // Convert Graal Resource to Corese URI
        IDatatype corese_uri = InteGraalToCoreseDatatype.convert(graal_uri);

        // Checks
        assertEquals("fr.inria.boreal#undef", corese_uri.getDatatype().stringValue());
    }

    @Test
    public void convertBNode() {
        String string_id = "BN_42^^blankNode";

        // Build Graal blank node
        Term graal_blank = termFactory.createOrGetLiteral(string_id);

        // Convert Graal blank node to Corese blank node
        IDatatype corese_blank = InteGraalToCoreseDatatype.convert(graal_blank);

        // Checks
        assertEquals(getLiteralValue(graal_blank.getLabel()), corese_blank.stringValue());
        assertEquals(true, corese_blank.isBlank());
    }

    @Test
    public void convertDouble() {
        String string_value = "1.234";

        // Build Graal double
        Term graal_double = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsddouble);

        // Convert Graal double to Corese double
        IDatatype corese_double = InteGraalToCoreseDatatype.convert(graal_double);

        // Checks
        assertEquals(getLiteralValue(graal_double.getLabel()), corese_double.stringValue());
        assertEquals(getLiteralDatatype(graal_double.getLabel()), corese_double.getDatatype().stringValue());
    }

    @Test
    public void convertFloat() {
        String string_value = "1.234";

        // Build Graal float
        Term graal_float = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdfloat);

        // Convert Graal float to Corese float
        IDatatype corese_float = InteGraalToCoreseDatatype.convert(graal_float);

        // Checks
        assertEquals(getLiteralValue(graal_float.getLabel()), corese_float.stringValue());
        assertEquals(getLiteralDatatype(graal_float.getLabel()), corese_float.getDatatype().stringValue());
    }

    @Test
    public void convertDecimal() {
        String string_value = "1098491072963113850.7436076939614540479";

        // Build Graal decimal
        Term graal_decimal = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsddecimal);

        // Convert Graal decimal to Corese decimal
        IDatatype corese_decimal = InteGraalToCoreseDatatype.convert(graal_decimal);

        // Checks
        assertEquals(getLiteralValue(graal_decimal.getLabel()), corese_decimal.stringValue());
        assertEquals(getLiteralDatatype(graal_decimal.getLabel()), corese_decimal.getDatatype().stringValue());

    }

    @Test
    public void convertInteger() {
        String string_value = "4";

        // Build Graal integer
        Term graal_integer = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdinteger);

        // Convert Graal integer to Corese integer
        IDatatype corese_integer = InteGraalToCoreseDatatype.convert(graal_integer);

        // Checks
        assertEquals(getLiteralValue(graal_integer.getLabel()), corese_integer.stringValue());
        assertEquals(getLiteralDatatype(graal_integer.getLabel()), corese_integer.getDatatype().stringValue());
    }

    @Test
    public void convertYear() {
        String string_value = "2021";

        // Build Graal year
        Term graal_year = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdyear);

        // Convert Graal year to Corese year
        IDatatype corese_year = InteGraalToCoreseDatatype.convert(graal_year);

        // Checks
        assertEquals(getLiteralValue(graal_year.getLabel()), corese_year.stringValue());
        assertEquals(getLiteralDatatype(graal_year.getLabel()), corese_year.getDatatype().stringValue());
    }

    @Test
    public void convertMonth() {
        String string_value = "12";

        // Build Graal month
        Term graal_month = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdmonth);

        // Convert Graal month to Corese month
        IDatatype corese_month = InteGraalToCoreseDatatype.convert(graal_month);

        // Checks
        assertEquals(getLiteralValue(graal_month.getLabel()), corese_month.stringValue());
        assertEquals(getLiteralDatatype(graal_month.getLabel()), corese_month.getDatatype().stringValue());
    }

    @Test
    public void convertDay() {
        String string_value = "12";

        // Build Graal day
        Term graal_day = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdday);

        // Convert Graal day to Corese day
        IDatatype corese_day = InteGraalToCoreseDatatype.convert(graal_day);

        // Checks
        assertEquals(getLiteralValue(graal_day.getLabel()), corese_day.stringValue());
        assertEquals(getLiteralDatatype(graal_day.getLabel()), corese_day.getDatatype().stringValue());
    }

    @Test
    public void convertByte() {
        String string_value = "22";

        // Build Graal byte
        Term graal_byte = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdbyte);

        // Convert Graal byte to Corese byte
        IDatatype corese_byte = InteGraalToCoreseDatatype.convert(graal_byte);

        // Checks
        assertEquals(getLiteralValue(graal_byte.getLabel()), corese_byte.stringValue());
        assertEquals(getLiteralDatatype(graal_byte.getLabel()), corese_byte.getDatatype().stringValue());
    }

    @Test
    public void convertShort() {
        String string_value = "22";

        // Build Graal short
        Term graal_short = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdshort);

        // Convert Graal short to Corese short
        IDatatype corese_short = InteGraalToCoreseDatatype.convert(graal_short);

        // Checks
        assertEquals(getLiteralValue(graal_short.getLabel()), corese_short.stringValue());
        assertEquals(getLiteralDatatype(graal_short.getLabel()), corese_short.getDatatype().stringValue());
    }

    @Test
    public void convertInt() {
        String string_value = "22";

        // Build Graal int
        Term graal_int = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdint);

        // Convert Graal int to Corese int
        IDatatype corese_int = InteGraalToCoreseDatatype.convert(graal_int);

        // Checks
        assertEquals(getLiteralValue(graal_int.getLabel()), corese_int.stringValue());
        assertEquals(getLiteralDatatype(graal_int.getLabel()), corese_int.getDatatype().stringValue());
    }

    @Test
    public void convertPositiveInteger() {
        String string_value = "22";

        // Build Graal positive integer
        Term graal_positive_integer = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdpositiveInteger);

        // Convert Graal positive integer to Corese positive integer
        IDatatype corese_positive_integer = InteGraalToCoreseDatatype.convert(graal_positive_integer);

        // Checks
        assertEquals(getLiteralValue(graal_positive_integer.getLabel()), corese_positive_integer.stringValue());
        assertEquals(getLiteralDatatype(graal_positive_integer.getLabel()),
                corese_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNegativeInteger() {
        String string_value = "-22";

        // Build Graal negative integer
        Term graal_negative_integer = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdnegativeInteger);

        // Convert Graal negative integer to Corese negative integer
        IDatatype corese_negative_integer = InteGraalToCoreseDatatype.convert(graal_negative_integer);

        // Checks
        assertEquals(getLiteralValue(graal_negative_integer.getLabel()), corese_negative_integer.stringValue());
        assertEquals(getLiteralDatatype(graal_negative_integer.getLabel()),
                corese_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonNegativeInteger() {
        String string_value = "0";

        // Build Graal non negative integer
        Term graal_non_negative_integer = termFactory.createOrGetLiteral(string_value + "^^"
                + RDF.xsdnonNegativeInteger);

        // Convert Graal non negative integer to Corese non negative integer
        IDatatype corese_non_negative_integer = InteGraalToCoreseDatatype.convert(graal_non_negative_integer);

        // Checks
        assertEquals(getLiteralValue(graal_non_negative_integer.getLabel()), corese_non_negative_integer.stringValue());
        assertEquals(getLiteralDatatype(graal_non_negative_integer.getLabel()),
                corese_non_negative_integer.getDatatype().stringValue());
    }

    @Test
    public void convertNonPositiveInteger() {
        String string_value = "0";

        // Build Graal non positive integer
        Term graal_non_positive_integer = termFactory.createOrGetLiteral(string_value + "^^" +
                RDF.xsdnonPositiveInteger);

        // Convert Graal non positive integer to Corese non positive integer
        IDatatype corese_non_positive_integer = InteGraalToCoreseDatatype.convert(graal_non_positive_integer);

        // Checks
        assertEquals(getLiteralValue(graal_non_positive_integer.getLabel()), corese_non_positive_integer.stringValue());
        assertEquals(getLiteralDatatype(graal_non_positive_integer.getLabel()),
                corese_non_positive_integer.getDatatype().stringValue());
    }

    @Test
    public void convertUnsignedByte() {
        String string_value = "22";

        // Build Graal unsigned byte
        Term graal_unsigned_byte = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdunsignedByte);

        // Convert Graal unsigned byte to Corese unsigned byte
        IDatatype corese_unsigned_byte = InteGraalToCoreseDatatype.convert(graal_unsigned_byte);

        // Checks
        assertEquals(getLiteralValue(graal_unsigned_byte.getLabel()), corese_unsigned_byte.stringValue());
        assertEquals(getLiteralDatatype(graal_unsigned_byte.getLabel()),
                corese_unsigned_byte.getDatatype().stringValue());
    }

    @Test
    public void convertUnsignedInt() {
        String string_value = "22";

        // Build Graal unsigned int
        Term graal_unsigned_int = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdunsignedByte);

        // Convert Graal unsigned int to Corese unsigned int
        IDatatype corese_unsigned_int = InteGraalToCoreseDatatype.convert(graal_unsigned_int);

        // Checks
        assertEquals(getLiteralValue(graal_unsigned_int.getLabel()), corese_unsigned_int.stringValue());
        assertEquals(getLiteralDatatype(graal_unsigned_int.getLabel()),
                corese_unsigned_int.getDatatype().stringValue());
    }

    @Test
    public void convertUnsignedLong() {
        String string_value = "22";

        // Build Graal unsigned long
        Term graal_unsigned_long = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdunsignedLong);

        // Convert Graal unsigned long to Corese unsigned long
        IDatatype corese_unsigned_long = InteGraalToCoreseDatatype.convert(graal_unsigned_long);

        // Checks
        assertEquals(getLiteralValue(graal_unsigned_long.getLabel()), corese_unsigned_long.stringValue());
        assertEquals(getLiteralDatatype(graal_unsigned_long.getLabel()),
                corese_unsigned_long.getDatatype().stringValue());
    }

    @Test
    public void convertUnsignedShort() {
        String string_value = "22";

        // Build Graal unsigned short
        Term graal_unsigned_short = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdunsignedShort);

        // Convert Graal unsigned short to Corese unsigned short
        IDatatype corese_unsigned_short = InteGraalToCoreseDatatype.convert(graal_unsigned_short);

        // Checks
        assertEquals(getLiteralValue(graal_unsigned_short.getLabel()), corese_unsigned_short.stringValue());
        assertEquals(getLiteralDatatype(graal_unsigned_short.getLabel()),
                corese_unsigned_short.getDatatype().stringValue());
    }

    @Test
    public void convertBoolean() {
        String string_value = "true";

        // Build Graal boolean
        Term graal_boolean = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdboolean);

        // Convert Graal boolean to Corese boolean
        IDatatype corese_boolean = InteGraalToCoreseDatatype.convert(graal_boolean);

        // Checks
        assertEquals(getLiteralValue(graal_boolean.getLabel()), corese_boolean.stringValue());
        assertEquals(getLiteralDatatype(graal_boolean.getLabel()), corese_boolean.getDatatype().stringValue());
    }

    @Test
    public void convertAnyUri() {
        String string_value = "http://example.org/bob";

        // Build Graal any UTI
        Term graal_any_uri = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdanyURI);

        // Convert Graal any UTI to Corese any UTI
        IDatatype corese_any_uri = InteGraalToCoreseDatatype.convert(graal_any_uri);

        // Checks
        assertEquals(getLiteralValue(graal_any_uri.getLabel()), corese_any_uri.stringValue());
        assertEquals(getLiteralDatatype(graal_any_uri.getLabel()), corese_any_uri.getDatatype().stringValue());
    }

    @Test
    public void convertXmlLiteral() {
        String string_value = "<span xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">シェイクスピアの<ruby><rbc><rb>演</rb><rb>劇</rb></rbc><rtc><rt>えん</rt><rt>げき</rt></rtc></ruby></span>";

        // Build Graal XML literal
        Term graal_xml_literal = termFactory.createOrGetLiteral(string_value + "^^" + RDF.XMLLITERAL);

        // Convert Graal XML Term to Corese XML literal
        IDatatype corese_xml_literal = InteGraalToCoreseDatatype.convert(graal_xml_literal);

        // Checks
        assertEquals(getLiteralValue(graal_xml_literal.getLabel()), corese_xml_literal.stringValue());
        assertEquals(getLiteralDatatype(graal_xml_literal.getLabel()), corese_xml_literal.getDatatype().stringValue());
    }

    @Test
    public void convertDate() {
        String string_value = "2021-06-16";

        // Build Graal date
        Term graal_date = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsddate);

        // Convert Graal date to Corese date
        IDatatype corese_date = InteGraalToCoreseDatatype.convert(graal_date);

        // Checks
        assertEquals(getLiteralValue(graal_date.getLabel()), corese_date.stringValue());
        assertEquals(getLiteralDatatype(graal_date.getLabel()), corese_date.getDatatype().stringValue());
    }

    @Test
    public void convertDateTime() {
        String string_value = "2021-06-17T07:12:19";

        // Build Graal date and time
        Term graal_date_time = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsddateTime);

        // Convert Graal date and time to Corese date and time
        IDatatype corese_date_time = InteGraalToCoreseDatatype.convert(graal_date_time);

        // Checks
        assertEquals(getLiteralValue(graal_date_time.getLabel()), corese_date_time.stringValue());
        assertEquals(getLiteralDatatype(graal_date_time.getLabel()), corese_date_time.getDatatype().stringValue());
    }

    @Test
    public void convertDateTimeMilisecond() {
        String string_value = "2021-07-16T16:28:36.477";

        // Build Graal date and time
        Term graal_date_time = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsddateTime);

        // Convert Graal date and time to Corese date and time
        IDatatype corese_date_time = InteGraalToCoreseDatatype.convert(graal_date_time);

        assertEquals(getLiteralValue(graal_date_time.getLabel()), corese_date_time.stringValue());
        assertEquals(getLiteralDatatype(graal_date_time.getLabel()), corese_date_time.getDatatype().stringValue());

    }

    @Test
    public void convertLiteralLang() {
        String string_value = "Un super test";
        String lang = "fr";

        // Build Graal Term lang
        Term graal_literal_lang = termFactory.createOrGetLiteral(string_value + "^^" + RDF.LANGSTRING + "@" + lang);

        // Convert Graal Term lang and time to Corese Term lang
        IDatatype corese_literal_lang = InteGraalToCoreseDatatype.convert(graal_literal_lang);

        // Checks
        assertEquals(getLiteralValue(graal_literal_lang.getLabel()), corese_literal_lang.stringValue());
        assertEquals(getLiteralDatatype(graal_literal_lang.getLabel()),
                corese_literal_lang.getDatatype().stringValue());
        assertEquals(getLiteralLang(graal_literal_lang.getLabel()), corese_literal_lang.getLang());
    }

    @Test
    public void convertString() {
        String string_value = "Un super test";

        // Build Graal string
        Term graal_string = termFactory.createOrGetLiteral(string_value + "^^" + RDF.xsdstring);

        // Convert Graal string and time to Corese string
        IDatatype corese_string = InteGraalToCoreseDatatype.convert(graal_string);

        // Checks
        assertEquals(getLiteralValue(graal_string.getLabel()), corese_string.stringValue());
        assertEquals(getLiteralDatatype(graal_string.getLabel()), corese_string.getDatatype().stringValue());
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build Graal undef
        Term graal_string = termFactory.createOrGetLiteral(string_value + "^^" + undef_datatype);

        // Convert Graal undef and time to Corese undef
        IDatatype corese_string = InteGraalToCoreseDatatype.convert(graal_string);

        // Checks
        assertEquals(getLiteralValue(graal_string.getLabel()), corese_string.stringValue());
        assertEquals(getLiteralDatatype(graal_string.getLabel()), corese_string.getDatatype().stringValue());
    }

}
