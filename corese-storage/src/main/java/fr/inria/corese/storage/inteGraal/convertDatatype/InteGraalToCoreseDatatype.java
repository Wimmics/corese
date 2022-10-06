package fr.inria.corese.storage.inteGraal.convertDatatype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.boreal.model.logicalElements.api.Literal;
import fr.boreal.model.logicalElements.api.Predicate;
import fr.boreal.model.logicalElements.api.Term;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.datatype.RDF;

public class InteGraalToCoreseDatatype {

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

    /**
     * Convert InteGraal Predicate to equivalent Corese datatype
     * 
     * @param graal_predicate the InteGraal Predicate to convert
     * @return Equivalent Corese datatype
     */
    public static IDatatype convert(Predicate graal_predicate) {
        return DatatypeMap.createResource(graal_predicate.getLabel());
    }

    /**
     * Convert InteGraal Term to equivalent Corese datatype
     * 
     * @param graal_term the InteGraal Term to convert
     * @return Equivalent Corese datatype
     */
    public static IDatatype convert(Term graal_term) {

        if (graal_term.isConstant()) {
            return DatatypeMap.createResource(graal_term.getLabel());
        } else if (graal_term.isLiteral()) {
            Literal<?> graal_lit = (Literal<?>) graal_term;
            Object graal_value = graal_lit.getValue();

            if (graal_value instanceof String) {
                String graal_value_string = (String) graal_value;

                // If string is not in good format (...^^...@..)
                if (!graal_value_string.contains("^^")) {
                    return DatatypeMap.createLiteral(graal_value.toString(), RDF.xsdstring);
                }

                String value = getLiteralValue(graal_value_string);
                String datatype = getLiteralDatatype(graal_value_string);
                String lang = getLiteralLang(graal_value_string);

                if (datatype.equals("blankNode")) {
                    return DatatypeMap.createBlank(value);
                }

                // If string is not in good format (...^^...@..)
                if (datatype.equals("")) {
                    return DatatypeMap.createLiteral(graal_value.toString(), RDF.xsdstring);
                }

                if (lang.equals("")) {
                    return DatatypeMap.createLiteral(value, datatype);
                } else {
                    return DatatypeMap.createLiteral(value, datatype, lang);

                }

            } else if (graal_value instanceof Integer) {
                return DatatypeMap.createLiteral(graal_value.toString(), RDF.xsdinteger);
            } else {
                return DatatypeMap.createUndef(graal_term.getLabel(), "fr.inria.boreal#undef");
            }
        }
         else {
            return DatatypeMap.createUndef(graal_term.getLabel(), "fr.inria.boreal#undef");
        }
    }
}
