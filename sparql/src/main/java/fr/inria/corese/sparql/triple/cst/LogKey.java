package fr.inria.corese.sparql.triple.cst;

/**
 *
 */
public interface LogKey {

    static final String NS = "http://ns.inria.fr/corese/log/";
    static final String PREF = "ns:";
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
    static final String ENDPOINT_CALL = "ns:endpointCall";
    // query that generated an exception 
    static final String QUERY = "ns:query";
    // federate query result of federate rewrite
    static final String AST = "ns:ast";
    // federate source selection
    static final String AST_SELECT = "ns:astSelect";
    // intermediate service call:
    static final String AST_SERVICE = "ns:astService";
    static final String INPUT_SIZE = "ns:astServiceLength";
    static final String MESSAGE = "ns:message";
    static final String DATE = "ns:date";
    static final String SERVER = "ns:server";
    static final String STATUS = "ns:status";
    static final String INFO = "ns:info";
    static final String URL = "ns:url";
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
    static final String BNODE = "ns:bnode";
    static final String NL = System.getProperty("line.separator");

}
