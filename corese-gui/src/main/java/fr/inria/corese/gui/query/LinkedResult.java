package fr.inria.corese.gui.query;

import fr.inria.corese.core.load.SPARQLResult;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.query.ProviderService;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Bind;
import fr.inria.corese.sparql.triple.parser.Binding;
import fr.inria.corese.sparql.triple.parser.Constant;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Query;
import fr.inria.corese.sparql.triple.parser.URLParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Query Results contain link URLs with intermediate query and result
 * Load and parse document at URLs
 * Display query in new query panel and display results below
 * use case: federated service with URL parameter mode=why
 */
public class LinkedResult {
    private static final Logger logger = LogManager.getLogger(LinkedResult.class.getName());
    static final String NL = System.getProperty("line.separator");
    
    MainFrame frame;
    private Mappings sourceSelectionMappings;
    private ASTQuery sourceSelectionAST;
    
    
    LinkedResult(MainFrame f) {
        frame = f;
    }
    
    void process(Mappings map) {
        linkedResult(map);
        analyseSelection();
    }

    
    
    
    /**
     * When Mappings contains link
     * Consider link of federated query and result
     * Parse result
     * Display query and result in additional query panel
     */
    void linkedResult(Mappings map) {
        List<String> list = map.getLinkList();

        for (int i = 0; i < list.size(); i++) {
            String url = list.get(i);
            
            if (url.contains(URLParam.LOG)) {
                String text = new Service().getString(url);

                if (url.contains(URLParam.QUERY)) {
                    
                    if (i + 1 < list.size()) {
                        String next = list.get(i + 1);

                        if (next.contains(URLParam.OUTPUT)) {
                            Mappings amap = getMappings(next);
                            
                            if (amap != null) {
                                analyse(url, text, amap);
                                display(url, prepare(url, text, amap), amap);
                            }
                                                       
                            i++;
                        } else if (url.contains(URLParam.REW)) {
                            // federated query, result of rewrite query by FederateVisitor
                            display(url, text);
                        }
                    }
                    else {                      
                        // query starts with # @federate <qurl>
                        String uri = getURL(text);
                        if (uri != null) {
                            display(url, rewriteQuery(url, uri, text));
                        }
                    }
                } else {
                    // log document
                    frame.msg(url).msg(NL);
                    frame.msg(text).msg(NL);
                }
            }
        }
    }
    
    
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
     * Analyse source selection Mappings
     */
    void analyseSelection() {
        if (getSourceSelectionAST() == null || getSourceSelectionMappings() == null) {
            return;
        }

        HashMap<String, Integer> res = getSourceSelectionMappings().countBooleanValue();
        Exp body = getServiceBody(getSourceSelectionAST());
        
        if (body != null) {
            for (Exp exp : body.getBody()) {
                if (exp.isBind()) {
                    Binding b = exp.getBind();
                    Integer count = res.get(b.getVariable().getLabel());
                    if (count != null && count == 0) {
                        frame.msg(String.format("Triple pattern not found in federation: %s", b.getFilter())).msg(NL);
                    }
                }
            }
        }
    }

    /**
     * return bgp(bind exists{} as ?b)
     */
    Exp getServiceBody(ASTQuery ast) {
        for (Exp exp : ast.getBody().getBody()) {
            if (exp.isService()) {
                return exp.getService().getBodyExp();
            }
        }
        return null;
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
        panel.display(map, frame);
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
        return uri.substring(0, uri.lastIndexOf("."));
    }
    
    
    String getURL(String query) {
        if (query.startsWith("#")) {
            // query starts with: # @federate <url>
            String url = query.substring(1 + query.indexOf("<"), query.indexOf(">"));
            url = clean(url);
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
         

    
}
