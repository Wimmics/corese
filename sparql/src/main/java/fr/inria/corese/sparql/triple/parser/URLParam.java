package fr.inria.corese.sparql.triple.parser;

/**
 *
 */
public interface URLParam {
    
    static final String SEPARATOR = "~";
    static final String CLIENT = "cn:";
    static final String SERVER = "sv:";

    static final String MODE = "mode";
    static final String QUERY = "query";
    static final String ACCESS = "access";
    static final String FORMAT = "format";
    static final String DECODE = "decode";
    static final String PARAM = "param";
    static final String URI = "uri";
    static final String DEFAULT_GRAPH = "default-graph-uri";
    static final String NAMED_GRAPH = "named-graph-uri";

    // specific for service clause
    static final String LOOP = "loop";
    static final String LIMIT = "limit";
    static final String SLICE = "slice";
    static final String TIMEOUT = "timeout";
    static final String TIME = "time";
    static final String BINDING = "binding";
    static final String FOCUS = "focus";
    static final String SKIP = "skip";
    static final String HEADER = "header";

    // value of mode=
    static final String DISPLAY = "display";
    static final String TRACE = "trace";
    static final String DEBUG = "debug";
    static final String SHOW = "show";
    static final String TRAP = "trap";
    static final String SPARQL = "sparql";
    static final String PROVENANCE = "provenance";
    static final String DETAIL = "detail";
    static final String LOG = "log";
    static final String NBVAR = "nbvar";
    static final String NBRESULT = "nboutput";
    static final String NBINPUT = "nbinput";
    static final String RESULT = "result";
    static final String RESULT_TEXT = "text";
    static final String FEDERATE = "federate";
    static final String FEDERATION = "federation";
    static final String SHACL = "shacl";
    static final String REQUEST = "request";
    static final String BEFORE = "before";
    static final String AFTER = "after";
    static final String SHARE = "share";
    static final String EXPORT = "export";
    static final String COUNT = "count";
    static final String PLATFORM = "platform";
    static final String CORESE = "corese";
    static final String CONSTRUCT = "construct";
    static final String WRAPPER = "wrapper";
    static final String ACCEPT = "accept";
    static final String REJECT = "reject";
    static final String DOCUMENT = "document";
    static final String LINK = "link";
    static final String TRANSFORM = "transform";
    static final String PARSE = "parse";
    static final String COMPILE = "compile";

    // values of binding=
    static final String VALUES = "values";
    static final String FILTER = "filter";

}
