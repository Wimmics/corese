package fr.inria.corese.shex.shacl;

/**
 *
 * @author Olivier Corby - Inria I3S - 2020
 */
public interface Constant {

    static final String RDF_TYPE = "a";
    static final String SH_SHAPE = "sh:NodeShape";
    static final String SH_PATH = "sh:path";
    static final String SH_NODE = "sh:node";
    static final String SH_PROPERTY = "sh:property";
    static final String SH_MINCOUNT = "sh:minCount";
    static final String SH_MAXCOUNT = "sh:maxCount";
    static final String SH_INVERSEPATH = "sh:inversePath";
    static final String SH_ALTERNATIVEPATH = "sh:alternativePath";
    static final String SH_ONE = "sh:xone";
    static final String SH_AND = "sh:and";
    static final String SH_OR = "sh:or";
    static final String SH_NOT = "sh:not";
    static final String SH_QUALIFIED_VALUE_SHAPE = "sh:qualifiedValueShape";
    static final String SH_QUALIFIED_MIN_COUNT  = "sh:qualifiedMinCount";
    static final String SH_QUALIFIED_MAX_COUNT  = "sh:qualifiedMaxCount";
    static final String SH_QUALIFIED_DISJOINT   = "sh:qualifiedValueShapesDisjoint";
    static final String SH_CLOSED = "sh:closed";
    
    static final String SH_DATATYPE = "sh:datatype";
    static final String SH_IN = "sh:in";
    static final String SH_HAS_VALUE = "sh:hasValue";
    static final String SH_MIN_EXCLUSIVE = "sh:minExclusive";
    static final String SH_MIN_INCLUSIVE = "sh:minInclusive";
    static final String SH_MAX_EXCLUSIVE = "sh:maxExclusive";
    static final String SH_MAX_INCLUSIVE = "sh:maxInclusive";
    static final String SH_NODE_KIND = "sh:nodeKind";
    static final String SH_UNDEF = "sh:Undef";
    static final String SH_BLANK = "sh:BlankNode";
    static final String SH_IRI_OR_BLANK = "sh:BlankNodeOrIRI";
    static final String SH_LITERAL = "sh:Literal";
    static final String SH_IRI = "sh:IRI";
    static final String SH_PATTERN = "sh:pattern";
    static final String SH_FLAGS = "sh:flags";
    static final String SH_MAXLENGTH = "sh:maxLength";
    static final String SH_MINLENGTH = "sh:minLength";
    static final String SH_LANGUAGE_IN = "sh:languageIn";
    
    static final String SHEX_EXTRA      = "shex:extra";
    static final String SHEX_OPTIONAL   = "shex:optional";
    static final String SHEX_COUNT      = "shex:count";
    static final String SHEX_CONSTRAINT = "shex:constraint";
    static final String SHEX_MINCOUNT   = "shex:minCount";
    static final String SHEX_MAXCOUNT   = "shex:maxCount";

}
