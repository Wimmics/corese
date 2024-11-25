package fr.inria.corese.jena.convert.datatypes;

import java.util.Optional;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;

public class JenaRdfNodeToCoreseDatatype {

    private static Model jena_factory = ModelFactory.createDefaultModel();

    public static IDatatype convert(Node jena_rdf_node) {
        return JenaRdfNodeToCoreseDatatype.convert(jena_factory.asRDFNode(jena_rdf_node));
    }

    /**
     * Convert Jena RDFNode to equivalent Corese datatype
     * 
     * @param jena_rdf_node the Jena RDFNode to convert
     * @return Equivalent Corese datatype
     */
    public static IDatatype convert(RDFNode jena_rdf_node) {

        if (jena_rdf_node.isURIResource()) {
            Resource jena_ressource = jena_rdf_node.asResource();
            return convertUri(jena_ressource);
        } else if (jena_rdf_node.isAnon()) {
            Resource jena_anon = jena_rdf_node.asResource();
            return convertBlank(jena_anon);
        } else {
            Literal jena_literal = jena_rdf_node.asLiteral();
            String label = jena_literal.getString();

            switch (jena_literal.getDatatypeURI()) {

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
                    return convertLiteral(label.replace("--", ""), RDF.xsdmonth);

                case RDF.xsdday:
                    return convertLiteral(label.replace("---", ""), RDF.xsdday);

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
                return convertLiteral(label, RDF.rdflangString, Optional.of(jena_literal.getLanguage()));

                case RDF.xsdstring:
                    return convertLiteral(label, RDF.xsdstring);

                default:
                    return convertLiteral(label, jena_literal.getDatatypeURI());
            }
        }
    }

    /**
     * Convert Jena Resource to equivalent Corese URI
     * 
     * @param corese_uri the Jena Resource to convert
     * @return equivalent Corese URI
     */
    private static IDatatype convertUri(Resource jena_ressource) {
        String value = jena_ressource.getURI();
        return DatatypeMap.createResource(value);
    }

    /**
     * Convert Jena blank node Resource to equivalent Corese blank node
     * 
     * @param jena_anon the Jena blank node Resource to convert
     * @return equivalent Corese blank node
     */
    private static IDatatype convertBlank(Resource jena_anon) {
        String id = jena_anon.getId().toString();
        return DatatypeMap.createBlank(id);
    }

    /**
     * Convert Jena literal to equivalent Corese IDatatype
     * 
     * @param label         Jena literal value to convert
     * @param datatype      Jena literal datatype url
     * @param optional_lang Jena literal languge
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
     * Convert Jena literal to equivalent Corese IDatatype
     * 
     * @param label    Jena literal value to convert
     * @param datatype Jena literal datatype url
     * @return equivalent Corese IDatatype
     */
    private static IDatatype convertLiteral(String label, String datatype) {
        return convertLiteral(label, datatype, Optional.empty());
    }
}
