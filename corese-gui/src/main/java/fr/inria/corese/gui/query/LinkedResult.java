package fr.inria.corese.gui.query;

import fr.inria.corese.core.load.SPARQLResult;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

/**
 * Query Results contain link URLs with intermediate query and result
 * Load and parse document at URLs
 * Display query in new query panel and display results below
 * use case: federated service with URL parameter mode=why
 */
public class LinkedResult implements URLParam {
    private static final Logger logger = LogManager.getLogger(LinkedResult.class.getName());
    static final String NL = System.getProperty("line.separator");
    
    MainFrame frame;
    private Mappings sourceSelectionMappings;
    private ASTQuery sourceSelectionAST;
    private String serviceURL;
    private boolean displayAll = false;
    
    
    LinkedResult(MainFrame f) {
        frame = f;
    }
    
    void process(Mappings map) {
        linkedResult(map);
        //analyseSelection();
    }
  
    
    /**
     * When Mappings contains explain link
     * Parse intermediate query/result
     * Display query/result in additional query panel
     */
    void linkedResult(Mappings map) {
        String mes = map.getLink(MES);
        JSONObject jsonMessage = null;
        if (mes != null) {
            // url with message is a json message to be processed
            // actually it is the context, hence it contains URL parameters
            // we can tune result processing
            jsonMessage = processMessage(mes);
        }
        
        String log = map.getLink("log/log");
        if (log != null) {
            String g = new Service().getString(log);
            msg(log).msg(NL);
            msg(g).msg(NL);
        }
        
        String url = map.getLink(WHY);
        if (url != null) {
            // url of a json document that contains url of query/result documents
            String text = new Service().getString(url);
            JSONObject json = new JSONObject(text);
            msg(NL).msg("LinkedResult Explain Index").msg(NL);
            display(json);
            linkedResult(json);
        }
        
        if (jsonMessage != null) {
            msg(NL).msg("Message").msg(NL);
            display(jsonMessage);
        }
    }
    
    void linkedResult(JSONObject json) {
        // original query
        process(json, SRC, true);
        // source selection query
        process(json, SEL, true);
        // rewritten federated query
        process(json, REW, true);
        
        if (json.has(WORKFLOW)) {
            JSONArray arr = json.getJSONArray(WORKFLOW);
            
            for (int i = 0; i<arr.length(); i++) {
                // list of intermediate query/result
                JSONObject obj = arr.getJSONObject(i);
                boolean display = isDisplayAll() || (i+1==arr.length());
                processElement(obj, WORKFLOW, display);
            }
        }
    }
    
    void process(JSONObject json, String name, boolean display) {
        if (json.has(name)) {
            processElement(json.getJSONObject(name), name, display);
        }
    }
        
    // json: query=url;result=url            
    void processElement(JSONObject json, String name, boolean display) {
        String url = json.getString(QUERY);
        String query = new Service().getString(url);
        
        if (json.has(RESULT)) {
            String next = json.getString(RESULT);
            Mappings map = getMappings(next);
            if (map != null) {
                analyse(name, query, map);
                display(url, prepare(url, query, map), map);
            }
        }  
        else if (display) { 
            display(url, query);
        }
    }
         
    JSONObject processMessage(String url) {
        String text = new Service().getString(url);
        JSONObject json = new JSONObject(text);
        message(url, json);
        return json;
    }
    
    LinkedResult msg(String mes) {
        frame.msg(mes);
        return this;
    }
    
    // display json message
    void display(JSONObject json) {
        for (String key : json.keySet()) {
            //System.out.println(key + " = " + json.get(key));
            msg(key + " = " + json.get(key)).msg(NL);
        }
        
        if (json.has(ERROR) && json.get(ERROR) instanceof JSONArray) {            
            for (var error : json.getJSONArray(ERROR)) {
                msg(NL).msg("Server error:").msg(NL);
                display((JSONObject) error);
            }
        }
        
        if (json.has(UNDEF) && json.get(UNDEF) instanceof JSONArray) {            
            for (var undef : json.getJSONArray(UNDEF)) {
                msg(NL).msg(String.format("Undefined triple: %s", undef.toString())).msg(NL);
            }
        }
        
        if (json.has(FAIL) && json.get(FAIL) instanceof JSONObject) {    
            JSONObject obj = json.getJSONObject(FAIL);
            msg(NL);
            msg(String.format("Query fail at: %s", obj.getString(URL))).msg(NL);
            msg(obj.getString(QUERY)).msg(NL);
        }        

    }
    
    /**
     * json is a JSON representation of server Context
     * it contains URL parameters such as mode
     */
    void message(String url, JSONObject json) {
        //System.out.println("LR:");
        
        //display(json);

        if (json.has(URLParam.MODE)) {
            JSONArray arr = json.getJSONArray(URLParam.MODE);
            if (arr != null) {
                mode(arr);
            }
        }
    }
    
    /**
     * mode=all display all query even if no result
     */
    void mode(JSONArray arr) {
        for (Object mode : arr) {
            if (mode.equals(URLParam.ALL)) {
                setDisplayAll(true);
            }
        }
    }
    
    /**
     * Store source selection query/result
     */
    void analyse(String url, String query, Mappings map) {
        if (url.contains(URLParam.SEL)) {
            QueryProcess exec = QueryProcess.create();
            try {
                setSourceSelectionAST(exec.parse(query));
                setSourceSelectionMappings(map);
            } catch (EngineException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
    
    /**
     * Analyse source selection query result
     */
    void analyseSelection() {
        if (getSourceSelectionAST() == null || getSourceSelectionMappings() == null) {
            return;
        }
        for (Expression exp : getSourceSelectionAST().getUndefinedTriple(getSourceSelectionMappings())) {
            msg(String.format("Triple pattern not found in federation: %s", exp)).msg(NL);
        }
    }
  
    
    MyJPanelQuery display(String url, String query) {
        MyJPanelQuery panel = frame.execPlus(url, query);
        return panel;
    }
    
    /**
     * Display linked query and result in additional query panel
     */
    MyJPanelQuery display(String url, String query, Mappings map) {
        MyJPanelQuery panel = display(url, query);
        panel.setDisplayLink(false);
        panel.display(map);
        return panel;
    }
    
    Mappings getMappings(String url)  {
        try {
            String result = new Service().getString(url);
            SPARQLResult parser = SPARQLResult.create();
            Mappings map = parser.parseString(result);
            map.genericSort();
            return map;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }
    

    
    String clean(String uri) {
        return URLServer.clean(uri);
    }
    
    
    String getURL(String query) {
        if (query.startsWith("#")) {
            // query starts with: # @federate <url>
            String url = query.substring(1 + query.indexOf("<"), query.indexOf(">"));
            //url = clean(url);
            return url;
        }
        return null;
    }
    
    String prepare(String url, String query, Mappings map) {
        String uri = map.getLink();
        if (uri == null) {
            return query;
        }
        return rewriteQuery(url, clean(uri), query);
    }
 
    /**
     * url: query linked document
     * uri: endpoint
     */
    String rewriteQuery(String url, String uri, String query) {
        if (uri == null) {
            return query;
        }
        try {
            ASTQuery ast = rewrite(uri, query);
            query = String.format("%s%s", message(uri), ast.toString());           
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return query;
    }
    
    String message(String uri) {
        String message = "";
        if (uri.equals(ProviderService.UNDEFINED_SERVICE)) {
            message = String.format("#\n# %s\n#\n", "Triple pattern not found in endpoint federation");
        }
        message = String.format("%s# @federate <%s>\n", message, uri);
        return message;
    }

    
    
    /**
     * copy last query, rewrite body with service clause
     * display it in new panel
     * such that it can be executed as is
     */
    ASTQuery rewrite(String uri, String query) throws EngineException {
        QueryProcess exec = QueryProcess.create();
        ASTQuery ast = exec.parse(query);
        return rewrite(ast, uri);
    }
    
    /**
     * ast = select where {}
     * ->
     * select where { service url { ast }}
     * 
     */
    ASTQuery rewrite(ASTQuery ast, String uri) {
        ast.setLimit(10);
        Query q = Query.create(ast);
        fr.inria.corese.sparql.triple.parser.Service s = 
                ast.service(Constant.create(DatatypeMap.newResource(uri)), q);
        ASTQuery as = ASTQuery.create();
        as.setNSM(ast.getNSM());
        as.setBody(as.bgp(s));
        as.setSelectAll(true);
        return as;
    }
    
//   void complete(String url, String text) {
//        if (url.contains(URLParam.MES)) {
//            message(url, new JSONObject(text));
//        }
//    }
        
    //    void linkedResult2(Mappings map) {
//        List<String> list = map.getLinkList();
//        for (int i = 0; i < list.size(); i++) {
//            String url = list.get(i);
//            
//            if (url.contains(URLParam.LOG)) {
//                String text = new Service().getString(url);
//
//                if (url.contains(URLParam.QUERY)) {
//                    
//                    if (i + 1 < list.size()) {
//                        String next = list.get(i + 1);
//
//                        if (next.contains(URLParam.OUTPUT)) {
//                            Mappings amap = getMappings(next);
//                            
//                            if (amap != null) {
//                                analyse(url, text, amap);
//                                display(url, prepare(url, text, amap), amap);
//                            }
//                                                       
//                            i++;
//                        } else if (isDisplayAll() || url.contains(URLParam.REW) || url.contains(SRC)) {
//                            // federated query, result of rewrite query by FederateVisitor
//                            display(url, text);
//                        }
//                    }
//                    else {                      
//                        // query starts with # @federate <qurl>
//                        String uri = getURL(text);
//                        if (uri != null) {
//                            display(url, rewriteQuery(url, uri, text));
//                        }
//                    }
//                } else {
//                    // log document
//                    msg(url).msg(NL);
//                    msg(text).msg(NL);
//                    complete(url, text);                   
//                }
//            }
//            else {
//                if (getServiceURL() == null) {
//                    setServiceURL(url);
//                }
//            }
//        }
//    }
    

    public Mappings getSourceSelectionMappings() {
        return sourceSelectionMappings;
    }

    public void setSourceSelectionMappings(Mappings sourceSelectionMappings) {
        this.sourceSelectionMappings = sourceSelectionMappings;
    }

    public ASTQuery getSourceSelectionAST() {
        return sourceSelectionAST;
    }

    public void setSourceSelectionAST(ASTQuery sourceSelectionAST) {
        this.sourceSelectionAST = sourceSelectionAST;
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public boolean isDisplayAll() {
        return displayAll;
    }

    public void setDisplayAll(boolean displayAll) {
        this.displayAll = displayAll;
    }
         

    
}
