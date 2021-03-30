package fr.inria.corese.sparql.triple.parser;

/**
 *
 */
public interface URLParam {

    static final String MODE = "mode";
    static final String QUERY = "query";
    static final String ACCESS = "access";
    static final String FORMAT = "format";
    static final String PARAM = "param";
    static final String URI = "uri";
    static final String DEFAULT_GRAPH = "default-graph-uri";
    static final String NAMED_GRAPH = "named-graph-uri";

    // specific for service clause
    static final String LOOP = "loop";
    static final String LIMIT = "limit";
    static final String SLICE = "slice";
    static final String TIMEOUT = "timeout";
    static final String BINDING = "binding";
    static final String FOCUS = "focus";
    static final String SKIP = "skip";

    // value of mode=
    static final String TRACE = "trace";
    static final String DEBUG = "debug";
    static final String SHOW = "show";
    static final String SPARQL = "sparql";
    static final String PROVENANCE = "provenance";
    static final String FEDERATE = "federate";
    static final String FEDERATION = "federation";
    static final String SHACL = "shacl";
    static final String REQUEST = "request";
    static final String BEFORE = "before";
    static final String AFTER = "after";
    static final String SHARE = "share";
    static final String COUNT = "count";
    static final String PLATFORM = "platform";
    static final String CORESE = "corese";

    // values of binding=
    static final String VALUES = "values";
    static final String FILTER = "filter";

}
