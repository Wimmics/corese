package fr.inria.corese.sparql.triple.parser;

/**
 *
 */
public interface Message {
    
    public static final String BNODE_SCOPE      = "Scope error for bnode: %s in %s";
    public static final String BNODE_SCOPE1     = "Scope error for bnode: %s";
    public static final String SELECT_DUPLICATE = "Duplicate select: %s as %s ";
    public static final String PREFIX_UNDEFINED = "Undefined prefix: %s";
    public static final String ARITY_ERROR      = "Arity error: %s";
    public static final String SCOPE_ERROR      ="Scope error: %s";
    public static final String PARAMETER_DUPLICATE = "Duplicate parameter: %s in:\n%s";
    public static final String ORDER_GROUP_UNDEFINED ="OrderBy GroupBy undefined: %s";
    public static final String VALUES_ERROR      = "Values error: nb variables != nb values";
    public static final String VARIABLE_UNDEFINED = "Undefined variable: %s %s";
}
