package fr.inria.corese.jena.convert.datatypes;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;

public class CoreseDatatypeToJenaRdfNode {

    private static Model jena_factory = ModelFactory.createDefaultModel();

    /**
     * Convert Corese datatype to equivalent Jena RDFNode
     * 
     * @param corese_datatype the Corese datatype to convert
     * @return Jena RDFNode equivalent
     */
    public static RDFNode convert(IDatatype corese_datatype) {

        String string_iri = corese_datatype.getLabel();

        switch (corese_datatype.getCode()) {

            case IDatatype.URI:
                return convertIri(corese_datatype);

            case IDatatype.BLANK:
                return convertBNode(corese_datatype);

            case IDatatype.DOUBLE:
                return convertLiteral(string_iri, XSDDatatype.XSDdouble.getURI());

            case IDatatype.FLOAT:
                return convertLiteral(string_iri, XSDDatatype.XSDfloat.getURI());

            case IDatatype.DECIMAL:
                return convertLiteral(string_iri, XSDDatatype.XSDdecimal.getURI());

            case IDatatype.INTEGER:

                String uri_datatype = corese_datatype.getDatatypeURI();

                switch (uri_datatype) {
                    case RDF.xsdyear:
                        return convertLiteral(string_iri, XSDDatatype.XSDgYear.getURI());

                    case RDF.xsdmonth:

                        if (string_iri.length() == 1) {
                            string_iri = "0" + string_iri;
                        }

                        if (!string_iri.startsWith("--")) {
                            string_iri = "--" + string_iri;
                        }
                        System.out.println(string_iri);
                        return convertLiteral(string_iri, XSDDatatype.XSDgMonth.getURI());

                    case RDF.xsdday:

                        if (!string_iri.startsWith("---")) {
                            string_iri = "---" + string_iri;
                        }
                        return convertLiteral(string_iri, XSDDatatype.XSDgDay.getURI());

                    case RDF.xsdbyte:
                        return convertLiteral(string_iri, XSDDatatype.XSDbyte.getURI());

                    case RDF.xsdshort:
                        return convertLiteral(string_iri, XSDDatatype.XSDshort.getURI());

                    case RDF.xsdint:
                        return convertLiteral(string_iri, XSDDatatype.XSDint.getURI());

                    case RDF.xsdpositiveInteger:
                        return convertLiteral(string_iri, XSDDatatype.XSDpositiveInteger.getURI());

                    case RDF.xsdnegativeInteger:
                        return convertLiteral(string_iri, XSDDatatype.XSDnegativeInteger.getURI());

                    case RDF.xsdnonNegativeInteger:
                        return convertLiteral(string_iri, XSDDatatype.XSDnonNegativeInteger.getURI());

                    case RDF.xsdnonPositiveInteger:
                        return convertLiteral(string_iri, XSDDatatype.XSDnonPositiveInteger.getURI());

                    case RDF.xsdunsignedByte:
                        return convertLiteral(string_iri, XSDDatatype.XSDunsignedByte.getURI());

                    case RDF.xsdunsignedInt:
                        return convertLiteral(string_iri, XSDDatatype.XSDunsignedInt.getURI());

                    case RDF.xsdunsignedLong:
                        return convertLiteral(string_iri, XSDDatatype.XSDunsignedLong.getURI());

                    case RDF.xsdunsignedShort:
                        return convertLiteral(string_iri, XSDDatatype.XSDunsignedShort.getURI());

                    default:
                        return convertLiteral(string_iri, XSDDatatype.XSDinteger.getURI());
                }

            case IDatatype.BOOLEAN:
                return convertLiteral(string_iri, XSDDatatype.XSDboolean.getURI());

            case IDatatype.URI_LITERAL:
                return convertLiteral(string_iri, XSDDatatype.XSDanyURI.getURI());

            case IDatatype.XMLLITERAL:
                return convertLiteral(string_iri, XMLLiteralType.theXMLLiteralType.getURI());

            case IDatatype.DATE:
                return convertLiteral(string_iri, XSDDatatype.XSDdate.getURI());

            case IDatatype.DATETIME:
                return convertLiteral(string_iri, XSDDatatype.XSDdateTime.getURI());

            case IDatatype.LITERAL:
                return convertLangString(corese_datatype);

            case IDatatype.STRING:
                return convertLiteral(string_iri, XSDDatatype.XSDstring.getURI());

            default:
                return convertLiteral(string_iri, corese_datatype.getDatatypeURI());
        }
    }

    /**
     * Convert Corese URI to equivalent Jena RDFNode
     * 
     * @param corese_uri the Corese URI to convert
     * @return equivalent Jena RDFNode
     */
    private static RDFNode convertIri(IDatatype corese_uri) {
        String string_iri = corese_uri.getLabel();
        return jena_factory.createResource(string_iri);
    }

    /**
     * Convert Corese blank node to equivalent Jena blank node
     * 
     * @param corese_blank the Corese blank node to convert
     * @return equivalent Jena blank node
     */
    private static Resource convertBNode(IDatatype corese_blank) {
        String id = corese_blank.stringValue();
        return jena_factory.createResource(new AnonId(id));
    }

    /**
     * Convert Corese Literal to equivalent Jena Literal
     * 
     * @param corese_literal the Corese Literal to convert
     * @param datatype       Literal datatype
     * @return equivalent Jena Literal
     */
    private static Literal convertLiteral(String label, String datatype) {
        return jena_factory.createTypedLiteral(label, datatype);
    }

    /**
     * Convert Corese lang string Literal to equivalent Jena lang string Literal
     * 
     * @param corese_lang_string the Corese lang string Literal to convert
     * @return equivalent Jena lang string
     */
    private static Literal convertLangString(IDatatype corese_lang_string) {
        String value = corese_lang_string.getLabel();

        if (corese_lang_string.hasLang()) {
            String lang = corese_lang_string.getLang();
            return jena_factory.createLiteral(value, lang);
        } else {
            return jena_factory.createLiteral(value);
        }
    }
}
