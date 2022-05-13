package fr.inria.corese.gui.query;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.ResultMessage;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.triple.cst.LogKey;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.util.List;
import org.json.JSONObject;

/**
 * Process ContextLog local why in the same way as Linked Result why
 */
public class LocalResult {
    static final String NL = System.getProperty("line.separator");
    
    MainFrame frame;
    
    LocalResult(MainFrame f) {
        frame = f;
    }
    
    
    /**
     * Process Federated query log, display in GUI
     */
    void process(ContextLog log) {
        logSelect(log);
        logService(log);
    }
    
    /**
     * 
     */
    void message(Mappings map, Binding bind) {
        message(map, bind.getContext(), bind.getLog());
    }

    void message(Mappings map, Context c, ContextLog log) {
        JSONObject json = new ResultMessage(getGraph(),  c, log).process(map);
        frame.msg(NL);
        frame.msg("Local Message").msg(NL);
        for (String key : json.keySet()) {
            frame.msg(String.format("%s = %s", key, json.get(key))).msg(NL);
        }
    }
    
    Graph getGraph() {
        return frame.getMyCorese().getGraph();
    }

    
    void logService(ContextLog log) {
        // list of URL of service calls in order, with number
        List<String> list = log.getStringList(LogKey.ENDPOINT_CALL);
        
        for (String name : list) {
            ASTQuery ast   = log.getAST(name, LogKey.AST_SERVICE);
            Mappings mymap = log.getMappings(name, LogKey.OUTPUT);
            String query = null;
            
            if (ast != null) {
                query = ast.toString();
                
                if (mymap == null) {
                    if (name.startsWith(ProviderService.UNDEFINED_SERVICE)
                            && log.getString(LogKey.SERVICE_URL) != null) {
                        // replace undefined service by source service URL
                        name = log.getString(LogKey.SERVICE_URL);
                    }
                    query = String.format("# @federate <%s>\n%s", name, query);
                }
                
                display(name, query, mymap);
            }
        }
    }
    
       
    /**
     * Log intermediate service and results for federated endpoint
     * and mode=why
     * Generate LinkedResult for source selection query and results
     * and for query rewrite with service clause
     */
    void logSelect(ContextLog log) {
        String sourceQuery = log.getString(LogKey.SERVICE_AST);
        
        if (sourceQuery != null) {
            String sourceURL = log.getString(LogKey.SERVICE_URL);
            if (sourceURL!=null) {
                sourceQuery = String.format("# <%s>\n%s", sourceURL, sourceQuery);
            }
            sourceQuery = String.format("# source query \n%s", sourceQuery);
            display(sourceURL, sourceQuery);
        }
        
        if (log.getASTIndex()!= null) {
            String query = log.getASTIndex().toString();
            query = String.format("# source discovery query \n%s", query);                                   
            display("", query, log.getIndexMap());            
        }
        
        if (log.getASTSelect() != null) {
            String query = log.getASTSelect().toString();
            query = String.format("# source selection query \n%s", query);                                   
            display("", query, log.getSelectMap());            
        }
        
        if (log.getAST() != null) {
            String query = log.getAST().toString();
            query = String.format("# federated query \n%s", query);
            display("", query);
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
        if (map != null) {
            panel.setDisplayLink(false);
            panel.display(map);
        }
        return panel;
    }
}
