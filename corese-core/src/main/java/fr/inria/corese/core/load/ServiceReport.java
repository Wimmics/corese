package fr.inria.corese.core.load;

import fr.inria.corese.compiler.parser.NodeImpl;
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
import java.util.List;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

/**
 *
 */
public class ServiceReport implements URLParam {
    
    private String format;
    private String accept;
    private Response response;
    private URLServer url;
    private Query query;
    private double time;
    
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
            set(dt, ERROR, ex.getResponse().getStatus());
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
        if (Service.isReport(getQuery()) && 
                (!suc || map.size()>0 || 
                (getQuery()!=null && getQuery().getGlobalQuery().getAST()
                    .hasMetadataValue(Metadata.REPORT, Metadata.EMPTY)))) {
            // if service is success but return no result:
            // @report        generate no report (and no result Mapping)
            // @report empty generate report and create a fake Mapping with report
            IDatatype dt = newReport();
            set(dt, FORMAT, getFormatText());
            map.recordReport(node(), dt);
            if (! suc) {
                set(dt, MES, "Format not handled by local parser");
                set(dt, RESULT, str);
            }
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
    
    void completeReport(Mappings map) {
        if (map.getReport()!=null && getAccept() != null) {
            // ServiceParser generated report, complete it here
            set(map.getReport(), ACCEPT, getAccept());
            completeReportHeader(map);
        }
    }
    
    // no used yet
    void complete(Mappings map, ASTQuery ast) {
        if (map.getReport() != null) {
            set(map.getReport(), "ast", ast.toString());
        }
    }
    
    void completeReportHeader(Mappings map) {
        IDatatype dt = map.getReport();
        
        if (dt != null) {
            if (getResponse() != null) {
                Response resp = getResponse();
                if (resp.getHeaders().get("Server") != null) {
                    set(dt, SERVER_NAME, resp.getHeaders().get("Server"));
                }
                if (resp.getHeaders().get("Content-Length") != null) {
                    set(dt, LENGTH, resp.getHeaders().get("Content-Length"));
                }
                if (resp.getDate() != null) {
                    set(dt, DATE, resp.getDate());
                }
                if (resp.getLastModified() != null) {
                    set(dt, "modified", resp.getLastModified());
                }
            }
            set(dt, URL, getURL().getServer());
            set(dt, SIZE, (map.isFake()) ? 0 : map.size());
            set(dt, TIME, getTime());
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
        if (hasKey(key)) {
            dt.set(key, value);
        }
    }
    
    void set(IDatatype dt, String key, int value) {
        if (hasKey(key)) {
            dt.set(key, value);
        }
    }

    void set(IDatatype dt, String key, Object value) {
        if (hasKey(key)) {
            dt.set(key, value);
        }
    }

    void set(IDatatype dt, String key, Date value) {
        if (hasKey(key)) {
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
        Metadata meta = getQuery().getGlobalQuery().getAST().getMetadata();
        if (meta == null) {
            return true;
        }
        List<String> list = meta.getValues(Metadata.REPORT);
        if (list == null) {
            return true;
        }
        // @report empty: empty is not a key
        if (list.size()==1 && list.contains(Metadata.EMPTY)) {
            return true;
        }
        return list.contains(key);
    }
            
    public Query getQuery() {
        return query;
    }

    public ServiceReport setQuery(Query query) {
        this.query = query;
        return this;
    }
}
