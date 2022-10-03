package fr.inria.corese.inteGraalImpl.convertDatatype;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
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
import fr.inria.corese.storage.inteGraal.convertDatatype.CoreseDatatypeToInteGraal;

public class TestCoreseDatatypeToInteGraal {

    private Matcher parseLiteral(String stringLiteral) {
        Pattern pattern = Pattern.compile("(.*)\\^\\^([^@]*)@?(.*)?");
        Matcher mat = pattern.matcher(stringLiteral);
        mat.find();
        return mat;
    }

    private String getLiteralValue(Term literal) {
        return this.parseLiteral(literal.getLabel()).group(1);
    }

    private String getLiteralDatatype(Term literal) {
        return this.parseLiteral(literal.getLabel()).group(2);
    }

    private String getLiteralLang(Term literal) {
        return this.parseLiteral(literal.getLabel()).group(3);
    }

    @Test
    public void convertUri() {
        String string_uri = "http://example.org/bob";

        // Build Corese URI
        IDatatype corese_uri = DatatypeMap.createResource(string_uri);

        // Convert Corese URI to Graal IRI
        Term graal_iri = CoreseDatatypeToInteGraal.convert(corese_uri);

        // Checks
        assertEquals(true, graal_iri.isConstant());
        assertEquals(corese_uri.stringValue(), graal_iri.getLabel());
    }

    @Test
    public void convertBNode() {
        String string_id = "_:BN_42";

        // Build Corese blank node
        IDatatype corese_blank = DatatypeMap.createBlank(string_id);

        // Convert Corese blank node to Graal blank node
        Term graal_blank = CoreseDatatypeToInteGraal.convert(corese_blank);

        // Checks
        assertEquals(true, graal_blank.isLiteral());
        assertEquals(corese_blank.stringValue(), this.getLiteralValue(graal_blank));
        assertEquals("blankNode", this.getLiteralDatatype(graal_blank));
    }

    @Test
    public void convertDouble() {
        double value = 1.234;

        // Build Corese double
        IDatatype corese_double = DatatypeMap.newDouble(value);

        // Convert Corese double to Graal double
        Term graal_double = CoreseDatatypeToInteGraal.convert(corese_double);
        // Checks
        assertEquals(graal_double.isLiteral(), true);
        assertEquals(corese_double.doubleValue(), Double.parseDouble(this.getLiteralValue(graal_double)), 0);
        assertEquals(corese_double.getDatatype().stringValue(), this.getLiteralDatatype(graal_double));
    }

    @Test
    public void convertFloat() {
        float value = 1.234f;

        // Build Corese float
        IDatatype corese_float = DatatypeMap.newFloat(value);

        // Convert Corese float to Graal float
        Term graal_float = CoreseDatatypeToInteGraal.convert(corese_float);

        // Checks
        assertEquals(graal_float.isLiteral(), true);
        assertEquals(corese_float.floatValue(), Float.parseFloat(this.getLiteralValue(graal_float)), 0);
        assertEquals(corese_float.getDatatype().stringValue(), this.getLiteralDatatype(graal_float));
    }

    @Test
    public void convertDecimal() {
        double double_value = 1.234;

        // Build Corese decimal
        IDatatype corese_decimal = DatatypeMap.newDecimal(double_value);

        // Convert Corese decimal to Graal decimal
        Term graal_decimal = CoreseDatatypeToInteGraal.convert(corese_decimal);

        // Checks
        assertEquals(graal_decimal.isLiteral(), true);
        assertEquals(corese_decimal.decimalValue(), new BigDecimal(this.getLiteralValue(graal_decimal)));
        assertEquals(corese_decimal.getDatatype().stringValue(), this.getLiteralDatatype(graal_decimal));
    }

    @Test
    public void convertInteger() {
        int value = 4;

        // Build Corese integer
        IDatatype corese_integer = DatatypeMap.newInteger(value);

        // Convert Corese integer to Graal integer
        Term graal_integer = CoreseDatatypeToInteGraal.convert(corese_integer);

        // Checks
        assertEquals(graal_integer.isLiteral(), true);
        assertEquals(corese_integer.intValue(), Integer.parseInt(this.getLiteralValue(graal_integer)));
        assertEquals(corese_integer.getDatatype().stringValue(), this.getLiteralDatatype(graal_integer));
    }

    @Test
    public void convertBoolean() {
        boolean value = true;

        // Build Corese boolean
        IDatatype corese_boolean = DatatypeMap.newInstance(value);

        // Convert Corese boolean to Graal boolean
        Term graal_boolean = CoreseDatatypeToInteGraal.convert(corese_boolean);

        // Checks
        assertEquals(graal_boolean.isLiteral(), true);
        assertEquals(corese_boolean.booleanValue(), Boolean.parseBoolean(this.getLiteralValue(graal_boolean)));
        assertEquals(corese_boolean.getDatatype().stringValue(), this.getLiteralDatatype(graal_boolean));
    }

    @Test
    public void convertAnyUri() {
        String value = "http://example.org/bob";

        // Build Corese any uri
        IDatatype corese_any_uri = DatatypeMap.createLiteral(value, RDF.xsdanyURI);

        // Convert Corese any uri to Graal any uri
        Term graal_any_uri = CoreseDatatypeToInteGraal.convert(corese_any_uri);

        // Checks
        assertEquals(graal_any_uri.isLiteral(), true);
        assertEquals(corese_any_uri.stringValue(), this.getLiteralValue(graal_any_uri));
        assertEquals(corese_any_uri.getDatatype().stringValue(), this.getLiteralDatatype(graal_any_uri));
    }

    @Test
    public void convertXmlLiteral() {
        String value = "<span xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"ja\">シェイクスピアの<ruby><rbc><rb>演</rb><rb>劇</rb></rbc><rtc><rt>えん</rt><rt>げき</rt></rtc></ruby></span>";

        // Build Corese XML literal
        IDatatype corese_xml_literal = DatatypeMap.createLiteral(value, RDF.XMLLITERAL);

        // Convert Corese XML literal to Graal XML literal
        Term graal_xml_literal = CoreseDatatypeToInteGraal.convert(corese_xml_literal);

        // Checks
        assertEquals(graal_xml_literal.isLiteral(), true);
        assertEquals(corese_xml_literal.stringValue(), this.getLiteralValue(graal_xml_literal));
        assertEquals(corese_xml_literal.getDatatype().stringValue(), this.getLiteralDatatype(graal_xml_literal));
    }

    @Test
    public void convertDate() {
        String value = "2021-06-16";

        // Build Corese date
        CoreseDate corese_date = (CoreseDate) DatatypeMap.newDate(value);

        // Convert Corese date to Graal date
        Term graal_date = CoreseDatatypeToInteGraal.convert(corese_date);

        // Checks
        assertEquals(graal_date.isLiteral(), true);
        assertEquals(corese_date.stringValue(), this.getLiteralValue(graal_date));
        assertEquals(corese_date.getDatatype().stringValue(), this.getLiteralDatatype(graal_date));
    }

    @Test
    public void convertDateTime() {
        String value = "2021-06-17T07:12:19";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to Graal date
        Term graal_date_time = CoreseDatatypeToInteGraal.convert(corese_date_time);

        // Checks
        assertEquals(graal_date_time.isLiteral(), true);
        assertEquals(corese_date_time.stringValue(), this.getLiteralValue(graal_date_time));
        assertEquals(corese_date_time.getDatatype().stringValue(), this.getLiteralDatatype(graal_date_time));
    }

    @Test
    public void convertDateTimeMilisecond() {

        String value = "2021-07-16T16:28:36.477";

        // Build Corese date and time
        CoreseDateTime corese_date_time = (CoreseDateTime) DatatypeMap.newDateTime(value);

        // Convert Corese date to Graal date
        Term graal_date_time = CoreseDatatypeToInteGraal.convert(corese_date_time);

        // Checks
        assertEquals(graal_date_time.isLiteral(), true);
        assertEquals(corese_date_time.stringValue(), this.getLiteralValue(graal_date_time));
        assertEquals(corese_date_time.getDatatype().stringValue(), this.getLiteralDatatype(graal_date_time));
    }

    @Test
    public void convertLiteralLang() {
        String value = "Un super test";
        String lang = "fr";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.createLiteral(value, RDF.LANGSTRING, lang);

        // Convert Corese literal lang to Graal literal lang
        Term graal_literal = CoreseDatatypeToInteGraal.convert(corese_literal);

        // Checks
        assertEquals(graal_literal.isLiteral(), true);
        assertEquals(corese_literal.stringValue(), this.getLiteralValue(graal_literal));
        assertEquals(corese_literal.getDatatype().stringValue(), this.getLiteralDatatype(graal_literal));
        assertEquals(corese_literal.getLang(), this.getLiteralLang(graal_literal));
    }

    @Test
    public void convertLiteralLang2() {
        String value = "Un super^^ test";
        String lang = "fr";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.createLiteral(value, RDF.LANGSTRING, lang);

        // Convert Corese literal lang to Graal literal lang
        Term graal_literal = CoreseDatatypeToInteGraal.convert(corese_literal);

        // Checks
        assertEquals(graal_literal.isLiteral(), true);
        assertEquals(corese_literal.stringValue(), this.getLiteralValue(graal_literal));
        assertEquals(corese_literal.getDatatype().stringValue(), this.getLiteralDatatype(graal_literal));
        assertEquals(corese_literal.getLang(), this.getLiteralLang(graal_literal));
    }

    @Test
    public void convertLiteralLangWhithoutLang() {
        String value = "Un super test";

        // Build Corese literal lang
        CoreseLiteral corese_literal = (CoreseLiteral) DatatypeMap.createLiteral(value, RDF.LANGSTRING);

        // Convert Corese literal lang to Graal literal lang
        Term graal_literal = CoreseDatatypeToInteGraal.convert(corese_literal);

        // Checks
        assertEquals(graal_literal.isLiteral(), true);
        assertEquals(corese_literal.stringValue(), this.getLiteralValue(graal_literal));
        assertEquals(corese_literal.getDatatype().stringValue(), this.getLiteralDatatype(graal_literal));
        assertEquals(corese_literal.getLang(), this.getLiteralLang(graal_literal));
    }

    @Test
    public void convertString() {
        String value = "Un super test";

        // Build Corese string
        CoreseString corese_string = (CoreseString) DatatypeMap.newStringBuilder(value);

        // Convert Corese string to Graal string
        Term graal_string = CoreseDatatypeToInteGraal.convert(corese_string);

        // Checks
        assertEquals(graal_string.isLiteral(), true);
        assertEquals(corese_string.stringValue(), this.getLiteralValue(graal_string));
        assertEquals(corese_string.getDatatype().stringValue(), this.getLiteralDatatype(graal_string));
    }

    @Test
    public void convertStringBuilder() {
        String value = "Un super test";

        // Build Corese string builder
        CoreseStringBuilder corese_string_builder = (CoreseStringBuilder) DatatypeMap.newStringBuilder(value);

        // Convert Corese string builder to Graal string builder
        Term graal_string_builder = CoreseDatatypeToInteGraal.convert(corese_string_builder);

        // Checks
        assertEquals(graal_string_builder.isLiteral(), true);
        assertEquals(corese_string_builder.stringValue(), this.getLiteralValue(graal_string_builder));
        assertEquals(corese_string_builder.getDatatype().stringValue(), this.getLiteralDatatype(graal_string_builder));
    }

    @Test
    public void convertYear() {
        int int_value = 2021;
        String value = String.valueOf(int_value);

        // Build Corese year
        CoreseYear corese_year = (CoreseYear) DatatypeMap.createLiteral(value, RDF.xsdyear);

        // Convert Corese year to Graal year
        Term graal_year = CoreseDatatypeToInteGraal.convert(corese_year);

        // Checks
        assertEquals(graal_year.isLiteral(), true);
        assertEquals(corese_year.stringValue(), this.getLiteralValue(graal_year));
        assertEquals(corese_year.getDatatype().stringValue(), this.getLiteralDatatype(graal_year));
    }

    @Test
    public void convertMonth() {
        int int_value = 6;
        String string_value = String.valueOf(int_value);

        // Build Corese month
        CoreseMonth corese_month = (CoreseMonth) DatatypeMap.createLiteral(string_value, RDF.xsdmonth);

        // Convert Corese month to Graal month
        Term graal_month = CoreseDatatypeToInteGraal.convert(corese_month);

        // Checks
        assertEquals(graal_month.isLiteral(), true);
        assertEquals(corese_month.stringValue(), this.getLiteralValue(graal_month));
        assertEquals(corese_month.getDatatype().stringValue(), this.getLiteralDatatype(graal_month));
    }

    @Test
    public void convertDay() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese day
        CoreseDay corese_day = (CoreseDay) DatatypeMap.createLiteral(string_value, RDF.xsdday);

        // Convert Corese day to Graal day
        Term graal_day = CoreseDatatypeToInteGraal.convert(corese_day);

        // Checks
        assertEquals(graal_day.isLiteral(), true);
        assertEquals(corese_day.stringValue(), this.getLiteralValue(graal_day));
        assertEquals(corese_day.getDatatype().stringValue(), this.getLiteralDatatype(graal_day));
    }

    @Test
    public void convertByte() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese byte
        CoreseGenericInteger corese_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdbyte);

        // Convert Corese byte to Graal byte
        Term graal_byte = CoreseDatatypeToInteGraal.convert(corese_byte);

        // Checks
        assertEquals(graal_byte.isLiteral(), true);
        assertEquals(corese_byte.stringValue(), this.getLiteralValue(graal_byte));
        assertEquals(corese_byte.getDatatype().stringValue(), this.getLiteralDatatype(graal_byte));
    }

    @Test
    public void convertShort() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese short
        CoreseGenericInteger corese_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdshort);

        // Convert Corese short to Graal short
        Term graal_short = CoreseDatatypeToInteGraal.convert(corese_short);

        // Checks
        assertEquals(graal_short.isLiteral(), true);
        assertEquals(corese_short.stringValue(), this.getLiteralValue(graal_short));
        assertEquals(corese_short.getDatatype().stringValue(), this.getLiteralDatatype(graal_short));
    }

    @Test
    public void convertInt() {
        int int_value = 22;
        String string_value = String.valueOf(int_value);

        // Build Corese int
        CoreseGenericInteger corese_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdint);

        // Convert Corese int to Graal int
        Term graal_int = CoreseDatatypeToInteGraal.convert(corese_int);

        // Checks
        assertEquals(graal_int.isLiteral(), true);
        assertEquals(corese_int.stringValue(), this.getLiteralValue(graal_int));
        assertEquals(corese_int.getDatatype().stringValue(), this.getLiteralDatatype(graal_int));
    }

    @Test
    public void convertPositiveInteger() {
        int positive_integer_value = 22;
        String string_value = String.valueOf(positive_integer_value);

        // Build Corese positive integer
        CoreseGenericInteger corese_positive_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdpositiveInteger);

        // Convert Corese positive integer to Graal positive integer
        Term graal_positive_integer = CoreseDatatypeToInteGraal.convert(corese_positive_integer);

        // Checks
        assertEquals(graal_positive_integer.isLiteral(), true);
        assertEquals(corese_positive_integer.stringValue(), this.getLiteralValue(graal_positive_integer));
        assertEquals(corese_positive_integer.getDatatype().stringValue(),
                this.getLiteralDatatype(graal_positive_integer));
    }

    @Test
    public void convertNegativeInteger() {
        int negative_integer_value = -22;
        String string_value = String.valueOf(negative_integer_value);

        // Build Corese negative integer
        CoreseGenericInteger corese_negative_integer = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdnegativeInteger);

        // Convert Corese negative integer to Graal negative integer
        Term graal_negative_integer = CoreseDatatypeToInteGraal.convert(corese_negative_integer);

        // Checks
        assertEquals(graal_negative_integer.isLiteral(), true);
        assertEquals(corese_negative_integer.stringValue(), this.getLiteralValue(graal_negative_integer));
        assertEquals(corese_negative_integer.getDatatype().stringValue(),
                this.getLiteralDatatype(graal_negative_integer));
    }

    @Test
    public void convertNonNegativeInteger() {
        int non_negative_integer_value = 0;
        String string_value = String.valueOf(non_negative_integer_value);

        // Build Corese non negative integer
        CoreseGenericInteger corese_non_negative_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, RDF.xsdnonNegativeInteger);

        // Convert Corese non negative integer to Graal non negative integer
        Term graal_non_negative_integer = CoreseDatatypeToInteGraal.convert(corese_non_negative_integer);

        // Checks
        assertEquals(graal_non_negative_integer.isLiteral(), true);
        assertEquals(corese_non_negative_integer.stringValue(), this.getLiteralValue(graal_non_negative_integer));
        assertEquals(corese_non_negative_integer.getDatatype().stringValue(),
                this.getLiteralDatatype(graal_non_negative_integer));
    }

    @Test
    public void convertNonPositiveInteger() {
        int non_positive_integer_value = 0;
        String string_value = String.valueOf(non_positive_integer_value);

        // Build Corese non positive integer
        CoreseGenericInteger corese_non_positive_integer = (CoreseGenericInteger) DatatypeMap
                .createLiteral(string_value, RDF.xsdnonPositiveInteger);

        // Convert Corese non positive integer to Graal non positive integer
        Term graal_non_positive_integer = CoreseDatatypeToInteGraal.convert(corese_non_positive_integer);

        // Checks
        assertEquals(graal_non_positive_integer.isLiteral(), true);
        assertEquals(corese_non_positive_integer.stringValue(), this.getLiteralValue(graal_non_positive_integer));
        assertEquals(corese_non_positive_integer.getDatatype().stringValue(),
                this.getLiteralDatatype(graal_non_positive_integer));
    }

    @Test
    public void convertUsignedByte() {
        int unsigned_byte_int = 22;
        String string_value = String.valueOf(unsigned_byte_int);

        // Build Corese unsigned byte
        CoreseGenericInteger corese_unsigned_byte = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedByte);

        // Convert Corese unsigned byte to Graal unsigned byte
        Term graal_unsigned_byte = CoreseDatatypeToInteGraal.convert(corese_unsigned_byte);

        // Checks
        assertEquals(graal_unsigned_byte.isLiteral(), true);
        assertEquals(corese_unsigned_byte.stringValue(), this.getLiteralValue(graal_unsigned_byte));
        assertEquals(corese_unsigned_byte.getDatatype().stringValue(), this.getLiteralDatatype(graal_unsigned_byte));
    }

    @Test
    public void convertUsignedInt() {
        int unsigned_int_value = 22;
        String string_value = String.valueOf(unsigned_int_value);

        // Build Corese unsigned int
        CoreseGenericInteger corese_unsigned_int = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedInt);

        // Convert Corese unsigned int to Graal unsigned int
        Term graal_unsigned_int = CoreseDatatypeToInteGraal.convert(corese_unsigned_int);

        // Checks
        assertEquals(graal_unsigned_int.isLiteral(), true);
        assertEquals(corese_unsigned_int.stringValue(), this.getLiteralValue(graal_unsigned_int));
        assertEquals(corese_unsigned_int.getDatatype().stringValue(), this.getLiteralDatatype(graal_unsigned_int));
    }

    @Test
    public void convertUsignedLong() {
        int unsigned_long_value = 22;
        String string_value = String.valueOf(unsigned_long_value);

        // Build Corese unsigned long
        CoreseGenericInteger corese_unsigned_long = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedLong);

        // Convert Corese unsigned long to Graal unsigned long
        Term graal_unsigned_long = CoreseDatatypeToInteGraal.convert(corese_unsigned_long);

        // Checks
        assertEquals(graal_unsigned_long.isLiteral(), true);
        assertEquals(corese_unsigned_long.stringValue(), this.getLiteralValue(graal_unsigned_long));
        assertEquals(corese_unsigned_long.getDatatype().stringValue(), this.getLiteralDatatype(graal_unsigned_long));
    }

    @Test
    public void convertUsignedShort() {
        int unsigned_short_value = 22;
        String string_value = String.valueOf(unsigned_short_value);

        // Build Corese unsigned short
        CoreseGenericInteger corese_unsigned_short = (CoreseGenericInteger) DatatypeMap.createLiteral(string_value,
                RDF.xsdunsignedShort);

        // Convert Corese unsigned short to Graal unsigned short
        Term graal_unsigned_short = CoreseDatatypeToInteGraal.convert(corese_unsigned_short);

        // Checks
        assertEquals(graal_unsigned_short.isLiteral(), true);
        assertEquals(corese_unsigned_short.stringValue(), this.getLiteralValue(graal_unsigned_short));
        assertEquals(corese_unsigned_short.getDatatype().stringValue(), this.getLiteralDatatype(graal_unsigned_short));
    }

    @Test
    public void convertUndefLiteral() {
        int value = 22;
        String string_value = String.valueOf(value);
        String undef_datatype = "https://inria/corese/datatype#newTypeOfNumber";

        // Build Corese undef
        IDatatype corese_undef = DatatypeMap.createUndef(string_value, undef_datatype);

        // Convert Corese undef to Graal undef
        Term graal_undef = CoreseDatatypeToInteGraal.convert(corese_undef);

        // Checks
        assertEquals(graal_undef.isLiteral(), true);
        assertEquals(corese_undef.stringValue(), this.getLiteralValue(graal_undef));
        assertEquals(corese_undef.getDatatype().stringValue(), this.getLiteralDatatype(graal_undef));
    }

    @Test
    public void convertPredicate() {
        String value = "p";

        // Build Corese undef
        IDatatype corese_undef = DatatypeMap.createResource(value);

        // Convert Corese undef to Graal undef
        Predicate graal_undef = CoreseDatatypeToInteGraal.convertPredicate(corese_undef);

        // Checks
        assertEquals(corese_undef.stringValue(), graal_undef.getLabel());
        assertEquals(3, graal_undef.getArity());
    }
}
