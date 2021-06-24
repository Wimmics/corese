package fr.inria.corese.server.webservice.message;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Create JSON message with Context and ContextLog
 * Message sent back to client as LinkedResult
 */
public class TripleStoreMessage {
    private Context context;
    private ContextLog log;
    private JSONObject json;
    
    TripleStoreMessage(Context ct, ContextLog log) {
        setContext(ct);
        setLog(log);
    }
    
     /**
     * JSON object with param=value sent back to client as Linked Result as "message"
     * return context as json object
     */
    JSONObject process() {
        setJson(getContext().json());                
        
        // select data from ContextLog
        messageLog(getLog());
        
        return getJson();        
    }
    
    /**
     * Copy data from ContextLog into json message
     * Copy endpoint exceptions
     */
    void messageLog(ContextLog log) {
        if (! log.getExceptionList().isEmpty()) {            
            JSONArray arr = new JSONArray();
            
            for (var ex : log.getExceptionList()) {
                JSONObject obj = message(ex);
                arr.put(obj);
            }
            
            getJson().put(URLParam.ERROR, arr);
        }
    }
    
    JSONObject message(EngineException e) {
        JSONObject json = new JSONObject();
        
        if (e.getURL() != null) {
            json.put(URLParam.URL, e.getURL().getServer());
        }

        if (e.getCause() instanceof ResponseProcessingException) {
            Response resp = (Response) e.getObject();
            if (resp != null) {
                json.put("statusInfo", resp.getStatusInfo().toString());
                json.put("status", resp.getStatus());
                String server = getServer(resp);
                if (server != null) {
                    json.put("server", server);
                }
            }
        }

        json.put(URLParam.MES, e.getMessage());
        //e.getAST().toString();
        return json;
    }
    
    String getServer(Response resp) {
        if (resp.getHeaderString("Server") != null) {
            return resp.getHeaderString("Server");
        }
        return resp.getHeaderString("server");
    }         

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ContextLog getLog() {
        return log;
    }

    public void setLog(ContextLog log) {
        this.log = log;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
    
}
