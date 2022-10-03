package fr.inria.corese.storage.inteGraal.convertDatatype;

import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.boreal.model.logicalElements.api.Variable;
import fr.boreal.model.logicalElements.factory.api.PredicateFactory;
import fr.boreal.model.logicalElements.factory.api.TermFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectPredicateFactory;
import fr.boreal.model.logicalElements.factory.impl.SameObjectTermFactory;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.RDF;

public class CoreseDatatypeToInteGraal {

    private static TermFactory termFactory = SameObjectTermFactory.instance();
    private static PredicateFactory predicateFactory = SameObjectPredicateFactory.instance();

    public static Predicate convertPredicate(IDatatype corese_dataType) {
        return predicateFactory.createOrGetPredicate(corese_dataType.stringValue(), 3);
    }

    public static Variable convertVariable() {
        return termFactory.createOrGetFreshVariable();
    }

    public static Term convert(IDatatype corese_dataType) {

        String string_iri = corese_dataType.getLabel();

        switch (corese_dataType.getCode()) {

            case IDatatype.URI:
                return convertIri(corese_dataType);

            case IDatatype.BLANK:
                return convertIriBn(corese_dataType);

            case IDatatype.DOUBLE:
                return convertLiteral(string_iri, RDF.xsddouble);

            case IDatatype.FLOAT:
                return convertLiteral(string_iri, RDF.xsdfloat);

            case IDatatype.DECIMAL:
                return convertLiteral(string_iri, RDF.xsddecimal);

            case IDatatype.INTEGER:

                String uri_datatype = corese_dataType.getDatatypeURI();

                switch (uri_datatype) {
                    case RDF.xsdyear:
                        return convertLiteral(string_iri, RDF.xsdyear);

                    case RDF.xsdmonth:
                        return convertLiteral(string_iri, RDF.xsdmonth);

                    case RDF.xsdday:
                        return convertLiteral(string_iri, RDF.xsdday);

                    case RDF.xsdbyte:
                        return convertLiteral(string_iri, RDF.xsdbyte);

                    case RDF.xsdshort:
                        return convertLiteral(string_iri, RDF.xsdshort);

                    case RDF.xsdint:
                        return convertLiteral(string_iri, RDF.xsdint);

                    case RDF.xsdpositiveInteger:
                        return convertLiteral(string_iri, RDF.xsdpositiveInteger);

                    case RDF.xsdnegativeInteger:
                        return convertLiteral(string_iri, RDF.xsdnegativeInteger);

                    case RDF.xsdnonNegativeInteger:
                        return convertLiteral(string_iri, RDF.xsdnonNegativeInteger);

                    case RDF.xsdnonPositiveInteger:
                        return convertLiteral(string_iri, RDF.xsdnonPositiveInteger);

                    case RDF.xsdunsignedByte:
                        return convertLiteral(string_iri, RDF.xsdunsignedByte);

                    case RDF.xsdunsignedInt:
                        return convertLiteral(string_iri, RDF.xsdunsignedInt);

                    case RDF.xsdunsignedLong:
                        return convertLiteral(string_iri, RDF.xsdunsignedLong);

                    case RDF.xsdunsignedShort:
                        return convertLiteral(string_iri, RDF.xsdunsignedShort);

                    default:
                        return convertLiteral(string_iri, RDF.xsdinteger);
                }

            case IDatatype.BOOLEAN:
                return convertLiteral(string_iri, RDF.xsdboolean);

            case IDatatype.URI_LITERAL:
                return convertLiteral(string_iri, RDF.xsdanyURI);

            case IDatatype.XMLLITERAL:
                return convertLiteral(string_iri, RDF.XMLLITERAL);

            case IDatatype.DATE:
                return convertLiteral(string_iri, RDF.xsddate);

            case IDatatype.DATETIME:
                return convertLiteral(string_iri, RDF.xsddateTime);

            case IDatatype.LITERAL:
                return convertLangString(corese_dataType, RDF.rdflangString);

            case IDatatype.STRING:
                return convertLiteral(string_iri, RDF.xsdstring);

            default:
                return convertLiteral(string_iri, corese_dataType.getDatatypeURI());
        }
    }

    private static Term convertIri(IDatatype corese_uri) {
        String string_iri = corese_uri.getLabel();
        return termFactory.createOrGetConstant(string_iri);
    }

    private static Term convertIriBn(IDatatype corese_uri) {
        String string_iri = corese_uri.getLabel();
        return termFactory.createOrGetLiteral(string_iri + "^^" + "blankNode");
    }

    private static Term convertLiteral(String label, String datatype) {
        return termFactory.createOrGetLiteral(label + "^^" + datatype);
    }

    private static Term convertLangString(IDatatype corese_lang_string, String datatype) {
        String value = corese_lang_string.getLabel();

        if (corese_lang_string.hasLang()) {
            String lang = corese_lang_string.getLang();
            return termFactory.createOrGetLiteral(value + "^^" + datatype + "@" + lang);
        } else {
            return convertLiteral(value, RDF.xsdstring);
        }
    }
    
    
}
