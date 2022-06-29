package fr.inria.corese.sparql.triple.cst;

/**
 *
 */
public interface LogKey {

    static final String NS = "http://ns.inria.fr/corese/log/";
    static final String HEADER_NS = NS+"header/";
    static final String PREF = "ns:";
    static final String HEADER_PREF = "hd:";
    static final String SUBJECT = NS+"report" ;
    static final String EVALUATION_REPORT = "ns:EvaluationReport";
    static final String REPORT = "ns:ServiceReport";
    
    // initial service URL
    static final String SERVICE_URL = "ns:serviceURL";
    // initial service AST
    static final String SERVICE_AST = "ns:serviceAST"; 
    // initial service result
    static final String SERVICE_OUTPUT = "ns:serviceOutput"; 
    // list of endpoints
    static final String ENDPOINT = "ns:endpoint";
    static final String INDEX = "ns:index";
    static final String ENDPOINT_CALL = "ns:endpointCall";
    static final String ENDPOINT_NUMBER = "ns:endpointNumber";
    static final String ENDPOINT_URL    = "ns:endpointURL";
    // query that generated an exception 
    static final String QUERY = "ns:query";
    // federate query result of federate rewrite
    static final String AST = "ns:ast";
    // federate source selection
    static final String AST_SELECT = "ns:astSelect";
    static final String AST_INDEX = "ns:astIndex";
    // intermediate service call:
    static final String AST_SERVICE = "ns:astService";
    static final String INPUT_SIZE = "ns:astServiceLength";
    static final String MESSAGE = "ns:message";
    static final String DATE = "ns:date";
    static final String SERVER = "ns:server";
    static final String STATUS = "ns:status";
    static final String INFO = "ns:info";
    static final String URL = "ns:url";
    static final String URL_PARAM = "ns:urlParam";
    static final String LOG = "ns:log";
    static final String LINK = "ns:link";
    static final String INPUT_CARD = "ns:inputCard";
    static final String OUTPUT_CARD = "ns:outputCard";
    static final String OUTPUT_SIZE = "ns:outputLength";
    static final String TIME = "ns:time";
    static final String NBCALL = "ns:nbcall";
    static final String INPUT = "ns:input";
    static final String OUTPUT = "ns:output";
    static final String RESULT = OUTPUT;
    static final String RESULT_TEXT = "ns:outputText";
    static final String RESULT_SELECT = "ns:outputSelect";
    static final String RESULT_INDEX = "ns:outputIndex";
    static final String BNODE = "ns:bnode";
    static final String NL = System.getProperty("line.separator");

}
