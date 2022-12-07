package fr.inria.corese.rdf4j.convert.datatypes;

import java.util.Optional;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;

/**
 * Factory to create Corese datatype from RDF4J value
 */
public class Rdf4jValueToCoreseDatatype {

    /**
     * Convert RDF4J value to equivalent Corese datatype
     * 
     * @param rdf4j_value the RDF4J value to convert
     * @return Equivalent Corese datatype
     */
    public static IDatatype convert(Value rdf4j_value) {

        if (rdf4j_value.isIRI()) {
            IRI rdf4j_iri = (IRI) rdf4j_value;
            return convertUri(rdf4j_iri);
        } else if (rdf4j_value.isBNode()) {
            BNode rdf4j_bnode = (BNode) rdf4j_value;
            return convertBlank(rdf4j_bnode);
        } else {
            Literal rdf4j_literal = (Literal) rdf4j_value;
            String label = rdf4j_literal.getLabel();

            switch (rdf4j_literal.getDatatype().stringValue()) {

                case RDF.xsddouble:
                    return convertLiteral(label, RDF.xsddouble);

                case RDF.xsdfloat:
                    return convertLiteral(label, RDF.xsdfloat);

                case RDF.xsddecimal:
                    return convertLiteral(label, RDF.xsddecimal);

                case RDF.xsdinteger:
                    return convertLiteral(label, RDF.xsdinteger);

                case RDF.xsdyear:
                    return convertLiteral(label, RDF.xsdyear);

                case RDF.xsdmonth:
                    return convertLiteral(label, RDF.xsdmonth);

                case RDF.xsdday:
                    return convertLiteral(label, RDF.xsdday);

                case RDF.xsdbyte:
                    return convertLiteral(label, RDF.xsdbyte);

                case RDF.xsdshort:
                    return convertLiteral(label, RDF.xsdshort);

                case RDF.xsdint:
                    return convertLiteral(label, RDF.xsdint);

                case RDF.xsdpositiveInteger:
                    return convertLiteral(label, RDF.xsdpositiveInteger);

                case RDF.xsdnegativeInteger:
                    return convertLiteral(label, RDF.xsdnegativeInteger);

                case RDF.xsdnonNegativeInteger:
                    return convertLiteral(label, RDF.xsdnonNegativeInteger);

                case RDF.xsdnonPositiveInteger:
                    return convertLiteral(label, RDF.xsdnonPositiveInteger);

                case RDF.xsdunsignedByte:
                    return convertLiteral(label, RDF.xsdunsignedByte);

                case RDF.xsdunsignedInt:
                    return convertLiteral(label, RDF.xsdunsignedInt);

                case RDF.xsdunsignedLong:
                    return convertLiteral(label, RDF.xsdunsignedLong);

                case RDF.xsdunsignedShort:
                    return convertLiteral(label, RDF.xsdunsignedShort);

                case RDF.xsdboolean:
                    return convertLiteral(label, RDF.xsdboolean);

                case RDF.xsdanyURI:
                    return convertLiteral(label, RDF.xsdanyURI);

                case RDF.XMLLITERAL:
                    return convertLiteral(label, RDF.XMLLITERAL);

                case RDF.xsddate:
                    return convertLiteral(label, RDF.xsddate);

                case RDF.xsddateTime:
                    return convertLiteral(label, RDF.xsddateTime);

                case RDF.rdflangString:
                    return convertLiteral(label, RDF.rdflangString, rdf4j_literal.getLanguage());

                case RDF.xsdstring:
                    return convertLiteral(label, RDF.xsdstring);

                default:
                    return convertLiteral(label, rdf4j_literal.getDatatype().stringValue());
            }
        }
    }

    /**
     * Convert RDF4J IRI to equivalent Corese URI
     * 
     * @param corese_uri the RDF4J IRI to convert
     * @return equivalent Corese URI
     */
    private static IDatatype convertUri(IRI rdf4j_iri) {
        String value = rdf4j_iri.stringValue();
        return DatatypeMap.createResource(value);
    }

    /**
     * Convert RDF4J blank node BNode to equivalent Corese blank node
     * 
     * @param rdf4j_bnode the RDF4J blank node BNode to convert
     * @return equivalent Corese blank node
     */
    private static IDatatype convertBlank(BNode rdf4j_bnode) {
        String value = "_:" + rdf4j_bnode.getID();
        return DatatypeMap.createBlank(value);
    }

    /**
     * Convert RDF4J literal to equivalent Corese IDatatype
     * 
     * @param label         RDF4J literal value to convert
     * @param datatype      RDF4J literal datatype url
     * @param optional_lang RDF4J literal languge
     * @return equivalent Corese IDatatype
     */
    private static IDatatype convertLiteral(String label, String datatype, Optional<String> optional_lang) {
        if (optional_lang.isPresent()) {
            String lang = optional_lang.get();
            return DatatypeMap.createLiteral(label, datatype, lang);
        } else {
            return DatatypeMap.createLiteral(label, datatype);
        }
    }

    /**
     * Convert RDF4J literal to equivalent Corese IDatatype
     * 
     * @param label    RDF4J literal value to convert
     * @param datatype RDF4J literal datatype url
     * @return equivalent Corese IDatatype
     */
    private static IDatatype convertLiteral(String label, String datatype) {
        return convertLiteral(label, datatype, Optional.empty());
    }

}