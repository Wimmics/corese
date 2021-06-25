package fr.inria.corese.server.webservice.message;

import fr.inria.corese.core.print.LogManager;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.cst.LogKey;
import static fr.inria.corese.sparql.triple.cst.LogKey.SERVICE_AST;
import static fr.inria.corese.sparql.triple.cst.LogKey.SERVICE_OUTPUT;
import static fr.inria.corese.sparql.triple.cst.LogKey.SERVICE_URL;
import static fr.inria.corese.sparql.triple.parser.Context.URL;
import fr.inria.corese.sparql.triple.parser.context.LinkedResultLog;
import java.util.List;
import org.json.JSONObject;


/**
 *
 */
public class TripleStoreLog implements URLParam {
    
    private QueryProcess queryProcess;
    private Context context;
    // Linked Result contain URLs of query/result, sent as json object
    private LinkedResultLog json;
    
    public TripleStoreLog (QueryProcess e, Context c) {
        setQueryProcess(e);
        setContext(c);
    }
    
    @Deprecated
    public Mappings logCompile() {
        ContextLog log = getQueryProcess().getLog();
        Mappings map     = log.getSelectMap();
        ASTQuery select  = log.getASTSelect();
        ASTQuery rewrite = log.getAST();
        String uri1 = document(select.toString(),  "select");
        String uri2 = document(rewrite.toString(), "rewrite");
        map.addLink(uri1);
        map.addLink(uri2);
        return map;
    }
    
    /**
     * Generate log report, write it in /log/
     * generate an URL for report and set URL as Mappings link
     */
    public void log(Mappings map) {
        ContextLog log = getQueryProcess().getLog(map);
        complete(map, log);
        
        if (getContext().hasAnyValue(LOG)) {
            processLog(map, log);
        }
        if (getContext().hasAnyValue(COMPILE, WHY)) {
            logWhy(map, getQueryProcess().getLog(map));
        }
        if (getContext().hasValue(MES)) {
            messageContext(map, getQueryProcess().getLog(map));
        }
    }
    
    void complete(Mappings map, ContextLog clog) {
        if (getContext().get(URL) != null) {
            clog.set(SERVICE_URL, getContext().get(URL).getLabel());
        }
        clog.set(SERVICE_AST, getContext().get(QUERY).getLabel());
        clog.set(SERVICE_OUTPUT, map);
    }

    /**
     * Generate log report, write it in /log/
     */
    public void processLog(Mappings map, ContextLog clog) {        
        LogManager log = new LogManager(clog);
        String uri = document(log.toString(), "log", ".ttl");
        map.addLink(uri);
        System.out.println("server report: " + uri);
    }

    
    
    /**
     * Generate message with Context as JSON object
     * Complete with explanation such as 
     * - service exceptions
     * - why query fails
     * 
     */
    void messageContext(Mappings map, ContextLog log) {
        // basic message
        JSONObject json = new TripleStoreMessage(getContext(), log).process();
        // explanation about query failure
        messageMappings(map, json);
        
        // publish message as LinkedResult
        String url = document(json.toString(), URLParam.MES, "");
        map.addLink(url);
    }
    
    /**
     * Try to explain why the query fails 
     * add explanation json object into json message
     *
     */
    void messageMappings(Mappings map, JSONObject json) {
        if (map.isEmpty()) {
            JSONObject obj = new TripleStoreExplain(getQueryProcess(), getContext(), map).process();
            if (!obj.isEmpty()) {
                json.put(URLParam.EXPLAIN, obj);
            }
        }
    }
    
    /**
     * Log intermediate service query/results for federated endpoint
     * when mode=why
     * Generate LinkedResult for service and result if any
     */
    void logWhy(Mappings map, ContextLog log) {
        messageContext(map, log);
        setJson(new LinkedResultLog());
        logSelect(map, log);
        logService(map, log);
        messageExplain(map);
    }
    
    /**
     * Create json object with URLs of explain Linked Results
     * Return json object as Linked Result to client
     */
    void messageExplain(Mappings map) {
        String url = document(getJson().toString(), WHY, "");
        map.addLink(url);
    }
    
   
    
    void addLink(Mappings map, String url) {
        //map.addLink(url);
    }
    
    
    void logService(Mappings map, ContextLog log) {
        // list of URL of service calls in order, with number
        List<String> list = log.getStringList(LogKey.ENDPOINT_CALL);

        int i = 0;
        for (String name : list) {
            IDatatype qdt = log.get(name, LogKey.AST_SERVICE);
            IDatatype rdt = log.get(name, LogKey.OUTPUT);
            String query = null, result = null;
            
            if (qdt != null) {
                ASTQuery ast = (ASTQuery) qdt.getPointerObject();
                query = ast.toString();
                
                if (rdt == null) {
                    if (name.startsWith(ProviderService.UNDEFINED_SERVICE)
                            && log.getString(SERVICE_URL) != null) {
                        // replace undefined service by source service URL
                        name = log.getString(SERVICE_URL);
                    }
                    query = String.format("# @federate <%s>\n%s", name, query);
                }
                
            }

            if (rdt == null) {
                // no result
            } else {
                Mappings mymap = rdt.getPointerObject().getMappings();
                // set endpoint URL as link in query results
                // use case: GUI federated debugger
                mymap.addLink(name);
                ResultFormat fm = ResultFormat.create(mymap);
                fm.setNbResult(mymap.getDisplay());
                result = fm.toString();
                
            }

            String url1 = document(query, QUERY.concat(Integer.toString(i)), "");
            String url2 = null;
            addLink(map, url1);
            if (result != null) {
                url2 = document(result, OUTPUT.concat(Integer.toString(i)), "");
                addLink(map, url2);
            }
            
            getJson().addLink(WORKFLOW, create(url1, url2));
            
            i++;
        }
    }
    
    JSONObject create(String query) {
        return getJson().create(query);
    }
    
    JSONObject create(String query, String result) {
        return getJson().create(query, result);        
    }
    
       
    /**
     * Log intermediate service and results for federated endpoint
     * and mode=why
     * Generate LinkedResult for source selection query and results
     * and for query rewrite with service clause
     */
    void logSelect(Mappings map, ContextLog log) {
        String sourceQuery = log.getString(LogKey.SERVICE_AST);
        
        if (sourceQuery != null) {
            String sourceURL = log.getString(LogKey.SERVICE_URL);
            if (sourceURL!=null) {
                // first link is source service URL
                map.addLink(sourceURL);
                sourceQuery = String.format("# <%s>\n%s", sourceURL, sourceQuery);
            }
            sourceQuery = String.format("# source query \n%s", sourceQuery);
            String url = document(sourceQuery, QUERY.concat(SRC), "");
            addLink(map, url);
            getJson().setLink(SRC, create(url));
        }
        
        if (log.getASTSelect() != null) {
            String query = log.getASTSelect().toString();
            query = String.format("# source selection query \n%s", query);
            String result = null;
                                   
            if (log.getSelectMap() != null) {
                ResultFormat fm = ResultFormat.create(log.getSelectMap());
                result = fm.toString();               
            }
            
            String url1 = document(query, QUERY.concat(SEL), "");
            String url2 = null;
            addLink(map, url1);
            if (result != null) {
                url2 = document(result, OUTPUT, "");
                addLink(map, url2);
            }
            
            getJson().setLink(SEL, create(url1, url2));

        }
        
        if (log.getAST() != null) {
            String query = log.getAST().toString();
            query = String.format("# federated query \n%s", query);
            String url = document(query, QUERY.concat(REW), "");
            addLink(map, url); 
            getJson().setLink(REW, create(url));
        }
    }
    
    public void logQuery(Mappings map) {
        if (getContext().hasValue(LOG_QUERY)) {
            String uri = document(map.getQuery().getAST().toString(), "query");
            map.addLink(uri);
        }
    }
    
    /**
     * Save content as document in HTTP server, return URL for this document 
     */
    
    String document(String str, String name) {
        return document(getContext(), str, name, "");
    }

    String document(String str, String name, String ext) {
        return document(getContext(), str, name, "");
    }
    
    String document(Context ct, String str, String name) {
        return document(ct, str, name, "");
    }

    String document(Context ct, String str, String name, String ext) {
        LinkedResult lr = new LinkedResult(name, ext, ct.getCreateKey());
        lr.write(str);
        return lr.getURL();
    }

    public QueryProcess getQueryProcess() {
        return queryProcess;
    }

    public void setQueryProcess(QueryProcess queryProcess) {
        this.queryProcess = queryProcess;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public LinkedResultLog getJson() {
        return json;
    }

    public void setJson(LinkedResultLog json) {
        this.json = json;
    }
    
    
    
}
