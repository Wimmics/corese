package fr.inria.corese.core.load;

import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.SERVICE_REPORT;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.URLParam;
import static fr.inria.corese.sparql.triple.parser.URLParam.MES;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.util.Date;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

/**
 * Generate service execution report as IDatatype json object
 * recorded in Mappings as additional variable ?_service_report_n 
 * ASTParser declare such variables in query AST in preprocessing phase
 * Report process when metadata @record
 * or Property SERVICE_REPORT = true
 * record subset of key value:
 * @record server url 
 * generate record when service return empty results:
 * @record empty
 */
public class ServiceReport implements URLParam {
    
    private String format;
    private String accept;
    private String location;
    private String result;
    private Response response;
    private URLServer url;
    // query is set by Service accessor to ServiceReport
    // getCreateReport(query)
    private Query query;
    private double time;
    
    
    boolean isReport() {
        return Property.booleanValue(SERVICE_REPORT) || 
                getURL().hasParameter(MODE, REPORT) ||
                (getQuery()!=null &&
                (getQuery().getAST().hasMetadata(Metadata.REPORT)
                || getGlobalAST().hasMetadata(Metadata.REPORT)));
    }
    
    ASTQuery getGlobalAST() {
        return getQuery().getGlobalQuery().getAST();
    }
    
    /**
     * Service report when service exception occur
     */
    Mappings serviceReport(ResponseProcessingException ex, Exception e) {
        IDatatype dt = newReport();
        set(dt, FORMAT, getFormatText());
        set(dt, ACCEPT, getAccept());
        
        if (ex == null) {
            set(dt, MES, e.getMessage());
        }
        else {
            //set(dt, STATUS, ex.getResponse().getStatus());
            if (ex.getResponse().getStatusInfo()!=null){
                set(dt, "info", ex.getResponse().getStatusInfo().getReasonPhrase());
            }
            if (ex.getMessage()!=null && !ex.getMessage().isEmpty()){
                set(dt, MES, ex.getMessage());
            }
        }
        
        Mappings map = Mappings.create(getQuery()).recordReport(node(), dt).setFake(true);
        completeReportHeader(map);
        return map;
    }
    
    
    /**
     * ServiceParser report without exception
     */
    void parserReport(Mappings map, String str, boolean suc) {
        if (isReport() && 
                (!suc || map.size()>0 || 
                (getQuery()!=null && 
                    getGlobalAST().hasMetadataValue(Metadata.REPORT, Metadata.EMPTY)))) {
            // if service is success but return no result:
            // @report        generate no report (and no result Mapping)
            // @report empty generate report and create a fake Mapping with report
            IDatatype dt = newReport();
            set(dt, FORMAT, getFormatText());
            
            if (! suc) {
                set(dt, MES, "Format not handled by local parser");
                set(dt, RESULT, str);
            }
            
            map.recordReport(node(), dt);
        }
    }
        
    /**
     * Service report without exception
     */
    void completeReport(Mappings map) {
        if (map.getReport()!=null) {
            // ServiceParser generated report, complete it here
            set(map.getReport(), ACCEPT, getAccept());
            completeReportHeader(map);
        }
    }
    
     /**
     * Service report when ServiceParser exception occur
     */
    Mappings parserReport(LoadException e) {
        IDatatype dt = newReport();
        set(dt, ERROR, e.getMessage());
        Mappings map = Mappings.create(getQuery()).recordReport(node(), dt).setFake(true);
        completeReportHeader(map);
        return map;
    }
    
    
    // no used yet
    void complete(Mappings map, ASTQuery ast) {
        if (map.getReport() != null) {
            set(map.getReport(), "ast", ast.toString());
        }
    }
    
    void completeReportHeader(Mappings map) {
        if (map.getReport() != null) {
            IDatatype dt = map.getReport();
            
            if (getResponse() != null) {
                Response resp = getResponse();
                set(dt, STATUS, resp.getStatus());
                set(dt, SERVER_NAME, protect(resp.getHeaderString("Server")));
                set(dt, DATE, resp.getDate());
                if (resp.getHeaderString("Content-Length") != null) {
                    set(dt, LENGTH, Integer.parseInt(resp.getHeaderString("Content-Length")));
                }

                if (getGlobalAST().hasMetadata(Metadata.HEADER)) {
                    for (String key : resp.getHeaders().keySet()) {
                        set(dt, key, resp.getHeaderString(key));
                    }
                    for (Link link : resp.getLinks()) {
                        set(dt, "link", link.toString());
                        System.out.println("SR link: " + link);
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    for (String cookie : resp.getCookies().keySet()) {
                        sb.append(resp.getCookies().get(cookie)).append("; ");
                    }
                    if (sb.length()>0) {
                        set(dt, "cookie", sb.toString());
                        sb = new StringBuilder();
                    }   
                    
                    for (String method : resp.getAllowedMethods()) {
                        sb.append(method).append("; ");
                    }
                    if (sb.length()>0) {
                        set(dt, "method", sb.toString());
                    }   
                        
                }
            }

            set(dt, URL, getURL().getServer());
            set(dt, SIZE, (map.isFake()) ? 0 : map.size());
            set(dt, TIME, getTime());
            set(dt, LOCATION, getLocation());
            
            if (getGlobalAST().hasMetadata(Metadata.DETAIL)) {
                set(dt, "result", getResult());
            }
        }
    }
    
    Node node() {       
        return node(getURL().getNumber());
    }
    
    static Node node(int n) {  
        return NodeImpl.createVariable(String.format(Binding.SERVICE_REPORT_FORMAT, n));
    }
    
    public String getFormat() {
        return format;
    }

    public String getFormatText() {
        return (getFormat()==null)?"undefined":getFormat();
    }
    
    String protect(String str) {
        return (str == null || str.isEmpty()) ? "undefined" : str;
    }
    
    public ServiceReport setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getAccept() {
        return accept;
    }

    public ServiceReport setAccept(String accept) {
        this.accept = accept;
        return this;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
    
    public URLServer getURL() {
        return url;
    }

    public void setURL(URLServer url) {
        this.url = url;
    }

    IDatatype newReport(String... param) {
        return DatatypeMap.newServiceReport(param);
    }
    
    void set(IDatatype dt, String key, String value) {
        if (hasKey(key) && value!=null) {
            dt.set(key, value);
        }
    }
    
    void set(IDatatype dt, String key, int value) {
        if (hasKey(key)) {
            dt.set(key, value);
        }
    }
    
    void set(IDatatype dt, String key, double value) {
        if (hasKey(key)) {
            dt.set(key, value);
        }
    }

    void set(IDatatype dt, String key, Object value) {
        if (hasKey(key) && value!=null) {
            dt.set(key, value);
        }
    }

    void set(IDatatype dt, String key, Date value) {
        if (hasKey(key) && value!=null) {
            dt.set(key, value);
        }
    }
    
    /**
     * @report server -> record server only
     */
    boolean hasKey(String key) {
        if (getQuery() == null) {
            return true;
        }
        return getQuery().getGlobalQuery().getAST().hasReportKey(key);
    }
            
    public Query getQuery() {
        return query;
    }

    public ServiceReport setQuery(Query query) {
        this.query = query;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
