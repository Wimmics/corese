package fr.inria.corese.rdf4j.convert.datatypes;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;

/**
 * Factory to create RDF4J value from Corese datatype
 */
public class CoreseDatatypeToRdf4jValue {

    private static ValueFactory rdf4j_factory = SimpleValueFactory.getInstance();

    /**
     * Convert Corese datatype to equivalent RDF4J value
     * 
     * @param corese_datatype the Corese datatype to convert
     * @return RDF4J value equivalent
     */
    public static Value convert(IDatatype corese_datatype) {

        switch (corese_datatype.getCode()) {

            case IDatatype.URI:
                return convertIri(corese_datatype);

            case IDatatype.BLANK:
                return convertBNode(corese_datatype);

            case IDatatype.DOUBLE:
                return convertLiteral(corese_datatype, XSD.DOUBLE);

            case IDatatype.FLOAT:
                return convertLiteral(corese_datatype, XSD.FLOAT);

            case IDatatype.DECIMAL:
                return convertLiteral(corese_datatype, XSD.DECIMAL);

            case IDatatype.INTEGER:
                String uri_datatype = corese_datatype.getDatatype().stringValue();

                switch (uri_datatype) {
                    case RDF.xsdyear:
                        return convertLiteral(corese_datatype, XSD.GYEAR);

                    case RDF.xsdmonth:
                        return convertLiteral(corese_datatype, XSD.GMONTH);

                    case RDF.xsdday:
                        return convertLiteral(corese_datatype, XSD.GDAY);

                    case RDF.xsdbyte:
                        return convertLiteral(corese_datatype, XSD.BYTE);

                    case RDF.xsdshort:
                        return convertLiteral(corese_datatype, XSD.SHORT);

                    case RDF.xsdint:
                        return convertLiteral(corese_datatype, XSD.INT);

                    case RDF.xsdpositiveInteger:
                        return convertLiteral(corese_datatype, XSD.POSITIVE_INTEGER);

                    case RDF.xsdnegativeInteger:
                        return convertLiteral(corese_datatype, XSD.NEGATIVE_INTEGER);

                    case RDF.xsdnonNegativeInteger:
                        return convertLiteral(corese_datatype, XSD.NON_NEGATIVE_INTEGER);

                    case RDF.xsdnonPositiveInteger:
                        return convertLiteral(corese_datatype, XSD.NON_POSITIVE_INTEGER);

                    case RDF.xsdunsignedByte:
                        return convertLiteral(corese_datatype, XSD.UNSIGNED_BYTE);

                    case RDF.xsdunsignedInt:
                        return convertLiteral(corese_datatype, XSD.UNSIGNED_INT);

                    case RDF.xsdunsignedLong:
                        return convertLiteral(corese_datatype, XSD.UNSIGNED_LONG);

                    case RDF.xsdunsignedShort:
                        return convertLiteral(corese_datatype, XSD.UNSIGNED_SHORT);

                    default:
                        return convertLiteral(corese_datatype, XSD.INTEGER);
                }

            case IDatatype.BOOLEAN:
                return convertLiteral(corese_datatype, XSD.BOOLEAN);

            case IDatatype.URI_LITERAL:
                return convertLiteral(corese_datatype, XSD.ANYURI);

            case IDatatype.XMLLITERAL:
                return convertLiteral(corese_datatype, org.eclipse.rdf4j.model.vocabulary.RDF.XMLLITERAL);

            case IDatatype.DATE:
                return convertLiteral(corese_datatype, XSD.DATE);

            case IDatatype.DATETIME:
                return convertLiteral(corese_datatype, XSD.DATETIME);

            case IDatatype.LITERAL:
                return convertLangString(corese_datatype);

            case IDatatype.STRING:
                return convertLiteral(corese_datatype, XSD.STRING);

            default:
                return convertUndefLiteral(corese_datatype, corese_datatype.getDatatypeURI());
        }
    }

    /**
     * Convert Corese datatype to equivalent RDF4J Literal
     * 
     * @param corese_datatype the Corese datatype to convert
     * @return RDF4J Literal equivalent
     */
    public static Literal convertLiteral(IDatatype corese_datatype) {
        return (Literal) CoreseDatatypeToRdf4jValue.convert(corese_datatype);
    }

    /**
     * Convert Corese URI to equivalent RDF4J IRI
     * 
     * @param corese_uri the Corese URI to convert
     * @return equivalent RDF4J IRI
     */
    public static IRI convertIri(IDatatype corese_uri) {
        String string_iri = corese_uri.getLabel();
        return rdf4j_factory.createIRI(string_iri);
    }

    /**
     * Convert Corese blank node to equivalent RDF4J blank node BNode
     * 
     * @param corese_blank the Corese blank node to convert
     * @return equivalent RDF4J blank node BNode
     */
    public static BNode convertBNode(IDatatype corese_blank) {
        String id = corese_blank.stringValue();
        id = id.replace("_:", "");
        return rdf4j_factory.createBNode(id);
    }

    /**
     * Convert Corese Literal to equivalent RDF4J Literal
     * 
     * @param corese_literal the Corese Literal to convert
     * @param datatype       Literal datatype
     * @return equivalent RDF4J Literal
     */
    private static Literal convertLiteral(IDatatype corese_literal, IRI datatype) {
        String value = corese_literal.getLabel();
        return rdf4j_factory.createLiteral(value, datatype);
    }

    /**
     * Convert Corese lang string Literal to equivalent RDF4J lang string Literal
     * 
     * @param corese_lang_string the Corese lang string Literal to convert
     * @return equivalent RDF4J lang string
     */
    private static Literal convertLangString(IDatatype corese_lang_string) {
        String value = corese_lang_string.getLabel();

        if (corese_lang_string.hasLang()) {
            String lang = corese_lang_string.getLang();
            return rdf4j_factory.createLiteral(value, lang);
        } else {
            return rdf4j_factory.createLiteral(value);
        }
    }

    /**
     * Convert Corese undef Literal to equivalent RDF4J Literal
     * 
     * @param corese_undef_literal the Corese Literal to convert
     * @param dataType             datatype url
     * @return equivalent RDF4J Literal
     */
    private static Literal convertUndefLiteral(IDatatype corese_undef_literal, String dataType) {
        String value = corese_undef_literal.getLabel();
        return rdf4j_factory.createLiteral(value, rdf4j_factory.createIRI(dataType));
    }
}
