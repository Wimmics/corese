
package wsimport.KgramWS;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the wsimport.KgramWS package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Query_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "query");
    private final static QName _SetEndpoint_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "setEndpoint");
    private final static QName _RunRuleResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "runRuleResponse");
    private final static QName _RunRule_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "runRule");
    private final static QName _LoadResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "loadResponse");
    private final static QName _InitEngineResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "initEngineResponse");
    private final static QName _SetEndpointResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "setEndpointResponse");
    private final static QName _InitEngineFromSQLResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "initEngineFromSQLResponse");
    private final static QName _UploadRDF_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "uploadRDF");
    private final static QName _GetEndpointResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "getEndpointResponse");
    private final static QName _QueryResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "queryResponse");
    private final static QName _GetEndpoint_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "getEndpoint");
    private final static QName _UploadRDFResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "uploadRDFResponse");
    private final static QName _InitEngine_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "initEngine");
    private final static QName _GetEdgesResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "getEdgesResponse");
    private final static QName _LoadRDFResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "loadRDFResponse");
    private final static QName _LoadRDF_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "loadRDF");
    private final static QName _InitEngineFromSQL_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "initEngineFromSQL");
    private final static QName _Reset_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "reset");
    private final static QName _Load_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "load");
    private final static QName _ResetResponse_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "resetResponse");
    private final static QName _GetEdges_QNAME = new QName("http://webservice.kgramserver.edelweiss.inria.fr/", "getEdges");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: wsimport.KgramWS
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetEdgesResponse }
     * 
     */
    public GetEdgesResponse createGetEdgesResponse() {
        return new GetEdgesResponse();
    }

    /**
     * Create an instance of {@link Query }
     * 
     */
    public Query createQuery() {
        return new Query();
    }

    /**
     * Create an instance of {@link QueryResponse }
     * 
     */
    public QueryResponse createQueryResponse() {
        return new QueryResponse();
    }

    /**
     * Create an instance of {@link GetEndpointResponse }
     * 
     */
    public GetEndpointResponse createGetEndpointResponse() {
        return new GetEndpointResponse();
    }

    /**
     * Create an instance of {@link GetEdges }
     * 
     */
    public GetEdges createGetEdges() {
        return new GetEdges();
    }

    /**
     * Create an instance of {@link LoadResponse }
     * 
     */
    public LoadResponse createLoadResponse() {
        return new LoadResponse();
    }

    /**
     * Create an instance of {@link ResetResponse }
     * 
     */
    public ResetResponse createResetResponse() {
        return new ResetResponse();
    }

    /**
     * Create an instance of {@link SetEndpointResponse }
     * 
     */
    public SetEndpointResponse createSetEndpointResponse() {
        return new SetEndpointResponse();
    }

    /**
     * Create an instance of {@link InitEngineFromSQLResponse }
     * 
     */
    public InitEngineFromSQLResponse createInitEngineFromSQLResponse() {
        return new InitEngineFromSQLResponse();
    }

    /**
     * Create an instance of {@link UploadRDFResponse }
     * 
     */
    public UploadRDFResponse createUploadRDFResponse() {
        return new UploadRDFResponse();
    }

    /**
     * Create an instance of {@link Load }
     * 
     */
    public Load createLoad() {
        return new Load();
    }

    /**
     * Create an instance of {@link RunRule }
     * 
     */
    public RunRule createRunRule() {
        return new RunRule();
    }

    /**
     * Create an instance of {@link Reset }
     * 
     */
    public Reset createReset() {
        return new Reset();
    }

    /**
     * Create an instance of {@link InitEngine }
     * 
     */
    public InitEngine createInitEngine() {
        return new InitEngine();
    }

    /**
     * Create an instance of {@link InitEngineFromSQL }
     * 
     */
    public InitEngineFromSQL createInitEngineFromSQL() {
        return new InitEngineFromSQL();
    }

    /**
     * Create an instance of {@link UploadRDF }
     * 
     */
    public UploadRDF createUploadRDF() {
        return new UploadRDF();
    }

    /**
     * Create an instance of {@link LoadRDFResponse }
     * 
     */
    public LoadRDFResponse createLoadRDFResponse() {
        return new LoadRDFResponse();
    }

    /**
     * Create an instance of {@link SetEndpoint }
     * 
     */
    public SetEndpoint createSetEndpoint() {
        return new SetEndpoint();
    }

    /**
     * Create an instance of {@link InitEngineResponse }
     * 
     */
    public InitEngineResponse createInitEngineResponse() {
        return new InitEngineResponse();
    }

    /**
     * Create an instance of {@link RunRuleResponse }
     * 
     */
    public RunRuleResponse createRunRuleResponse() {
        return new RunRuleResponse();
    }

    /**
     * Create an instance of {@link GetEndpoint }
     * 
     */
    public GetEndpoint createGetEndpoint() {
        return new GetEndpoint();
    }

    /**
     * Create an instance of {@link LoadRDF }
     * 
     */
    public LoadRDF createLoadRDF() {
        return new LoadRDF();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Query }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "query")
    public JAXBElement<Query> createQuery(Query value) {
        return new JAXBElement<Query>(_Query_QNAME, Query.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetEndpoint }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "setEndpoint")
    public JAXBElement<SetEndpoint> createSetEndpoint(SetEndpoint value) {
        return new JAXBElement<SetEndpoint>(_SetEndpoint_QNAME, SetEndpoint.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunRuleResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "runRuleResponse")
    public JAXBElement<RunRuleResponse> createRunRuleResponse(RunRuleResponse value) {
        return new JAXBElement<RunRuleResponse>(_RunRuleResponse_QNAME, RunRuleResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RunRule }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "runRule")
    public JAXBElement<RunRule> createRunRule(RunRule value) {
        return new JAXBElement<RunRule>(_RunRule_QNAME, RunRule.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "loadResponse")
    public JAXBElement<LoadResponse> createLoadResponse(LoadResponse value) {
        return new JAXBElement<LoadResponse>(_LoadResponse_QNAME, LoadResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitEngineResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "initEngineResponse")
    public JAXBElement<InitEngineResponse> createInitEngineResponse(InitEngineResponse value) {
        return new JAXBElement<InitEngineResponse>(_InitEngineResponse_QNAME, InitEngineResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SetEndpointResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "setEndpointResponse")
    public JAXBElement<SetEndpointResponse> createSetEndpointResponse(SetEndpointResponse value) {
        return new JAXBElement<SetEndpointResponse>(_SetEndpointResponse_QNAME, SetEndpointResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitEngineFromSQLResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "initEngineFromSQLResponse")
    public JAXBElement<InitEngineFromSQLResponse> createInitEngineFromSQLResponse(InitEngineFromSQLResponse value) {
        return new JAXBElement<InitEngineFromSQLResponse>(_InitEngineFromSQLResponse_QNAME, InitEngineFromSQLResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadRDF }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "uploadRDF")
    public JAXBElement<UploadRDF> createUploadRDF(UploadRDF value) {
        return new JAXBElement<UploadRDF>(_UploadRDF_QNAME, UploadRDF.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEndpointResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "getEndpointResponse")
    public JAXBElement<GetEndpointResponse> createGetEndpointResponse(GetEndpointResponse value) {
        return new JAXBElement<GetEndpointResponse>(_GetEndpointResponse_QNAME, GetEndpointResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "queryResponse")
    public JAXBElement<QueryResponse> createQueryResponse(QueryResponse value) {
        return new JAXBElement<QueryResponse>(_QueryResponse_QNAME, QueryResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEndpoint }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "getEndpoint")
    public JAXBElement<GetEndpoint> createGetEndpoint(GetEndpoint value) {
        return new JAXBElement<GetEndpoint>(_GetEndpoint_QNAME, GetEndpoint.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UploadRDFResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "uploadRDFResponse")
    public JAXBElement<UploadRDFResponse> createUploadRDFResponse(UploadRDFResponse value) {
        return new JAXBElement<UploadRDFResponse>(_UploadRDFResponse_QNAME, UploadRDFResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitEngine }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "initEngine")
    public JAXBElement<InitEngine> createInitEngine(InitEngine value) {
        return new JAXBElement<InitEngine>(_InitEngine_QNAME, InitEngine.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEdgesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "getEdgesResponse")
    public JAXBElement<GetEdgesResponse> createGetEdgesResponse(GetEdgesResponse value) {
        return new JAXBElement<GetEdgesResponse>(_GetEdgesResponse_QNAME, GetEdgesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadRDFResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "loadRDFResponse")
    public JAXBElement<LoadRDFResponse> createLoadRDFResponse(LoadRDFResponse value) {
        return new JAXBElement<LoadRDFResponse>(_LoadRDFResponse_QNAME, LoadRDFResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoadRDF }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "loadRDF")
    public JAXBElement<LoadRDF> createLoadRDF(LoadRDF value) {
        return new JAXBElement<LoadRDF>(_LoadRDF_QNAME, LoadRDF.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InitEngineFromSQL }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "initEngineFromSQL")
    public JAXBElement<InitEngineFromSQL> createInitEngineFromSQL(InitEngineFromSQL value) {
        return new JAXBElement<InitEngineFromSQL>(_InitEngineFromSQL_QNAME, InitEngineFromSQL.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Reset }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "reset")
    public JAXBElement<Reset> createReset(Reset value) {
        return new JAXBElement<Reset>(_Reset_QNAME, Reset.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Load }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "load")
    public JAXBElement<Load> createLoad(Load value) {
        return new JAXBElement<Load>(_Load_QNAME, Load.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "resetResponse")
    public JAXBElement<ResetResponse> createResetResponse(ResetResponse value) {
        return new JAXBElement<ResetResponse>(_ResetResponse_QNAME, ResetResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEdges }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://webservice.kgramserver.edelweiss.inria.fr/", name = "getEdges")
    public JAXBElement<GetEdges> createGetEdges(GetEdges value) {
        return new JAXBElement<GetEdges>(_GetEdges_QNAME, GetEdges.class, null, value);
    }

}
