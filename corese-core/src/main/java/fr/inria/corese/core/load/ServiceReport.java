package fr.inria.corese.core.load;

import fr.inria.corese.compiler.parser.NodeImpl;
import static fr.inria.corese.core.api.Loader.ACCEPT;
import static fr.inria.corese.kgram.api.core.ExprType.FORMAT;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.URLParam;
import static fr.inria.corese.sparql.triple.parser.URLParam.MES;
import fr.inria.corese.sparql.triple.parser.URLServer;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

/**
 *
 */
public class ServiceReport implements URLParam {

    public URLServer getURL() {
        return url;
    }

    public void setURL(URLServer url) {
        this.url = url;
    }
    
    private String format;
    private String accept;
    private Response response;
    private URLServer url;
    private double time;
    
    /**
     * Service detail when service exception occur
     */
    Mappings serviceReport(Query q, ResponseProcessingException ex, Exception e) {
        IDatatype dt = DatatypeMap.newServiceReport(FORMAT, getFormatText(), ACCEPT, getAccept());
        
        if (ex == null) {
            dt.set(MES, e.getMessage());
        }
        else {
            dt.set(ERROR,  ex.getResponse().getStatus());
            if (ex.getResponse().getStatusInfo()!=null){
                dt.set("info", ex.getResponse().getStatusInfo().getReasonPhrase());
            }
            if (ex.getMessage()!=null && !ex.getMessage().isEmpty()){
                dt.set(MES, ex.getMessage());
            }
        }
        
        Mappings map = Mappings.create(q).recordDetail(node(), dt).setFake(true);
        completeReportHeader(map);
        return map;
    }
    
    void createParserReport(Query q, Mappings map, String str, boolean suc) {
        if (Service.isDetail(q)) {
            IDatatype detail = DatatypeMap.newServiceReport(FORMAT, getFormatText());
            map.recordDetail(node(), detail);
            if (! suc) {
                detail.set(MES, "Format not handled by local parser");
                detail.set(RESULT, str);
            }
        }
    }
    
     /**
     * Service detail when ServiceParser exception occur
     */
    Mappings parserReport(Query q, LoadException e) {
        IDatatype dt = DatatypeMap.newServiceReport(ERROR, e.getMessage());
        Mappings map = Mappings.create(q).recordDetail(node(), dt).setFake(true);
        completeReportHeader(map);
        return map;
    }
    
    void completeReport(Mappings map, String accept) {
        if (map.getDetail()!=null && accept != null) {
            // ServiceParser generated detail report, complete it here
            map.getDetail().set(ACCEPT, accept);
            completeReportHeader(map);
        }
    }
    
    
    void completeReportHeader(Mappings map) {
        IDatatype dt = map.getDetail();
        
        if (dt != null) {
            if (getResponse() != null) {
                Response resp = getResponse();
                if (resp.getHeaders().get("Server") != null) {
                    dt.set(SERVER_NAME, resp.getHeaders().get("Server"));
                }
                if (resp.getHeaders().get("Content-Length") != null) {
                    dt.set(LENGTH, resp.getHeaders().get("Content-Length"));
                }
                if (resp.getDate() != null) {
                    dt.set(DATE, resp.getDate());
                }
                if (resp.getLastModified() != null) {
                    dt.set("modified", resp.getLastModified());
                }
            }
            
            dt.set(SIZE, (map.isFake()) ? 0 : map.size());
            dt.set(TIME, getTime());
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
    
}
