package fr.inria.corese.server.webservice;

import fr.inria.corese.core.print.LogManager;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import fr.inria.corese.sparql.triple.parser.URLParam;
import fr.inria.corese.sparql.triple.cst.LogKey;
import java.util.List;

/**
 *
 */
public class TripleStoreLog implements URLParam {
    
    private QueryProcess queryProcess;
    private Context context;
    
    TripleStoreLog (QueryProcess e, Context c) {
        setQueryProcess(e);
        setContext(c);
    }
    
     Mappings logCompile() {
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
     * Generate RDF error report, write it in /log/
     * generate an URL for report and set URL as Mappings link
     */
    void log(Mappings map) {
        if (getContext().hasAnyValue(PROVENANCE, LOG, WHY)) {
            ContextLog clog = getQueryProcess().getLog(map);
            LogManager log = new LogManager(clog);
            String uri = document(log.toString(), "log", ".ttl");
            map.addLink(uri);
            System.out.println("server report: " + uri);
            logWhy(map, clog);
        }
    }
    
    void logWhy(Mappings map, ContextLog log) {
        if (getContext().hasValue(WHY)) {
            logSelect(map, log);
            List<String> list = log.getStringList(LogKey.ENDPOINT_CALL);

            int i = 0;
            for (String name : list) {
                IDatatype qdt = log.get(name, LogKey.AST_SERVICE);
                IDatatype rdt = log.get(name, LogKey.OUTPUT);

                if (qdt != null) {
                    ASTQuery ast = (ASTQuery) qdt.getPointerObject();
                    String query = ast.toString();
                    if (rdt == null) {
                        query = String.format("# @federate <%s>\n%s", name, query);
                    }
                    String url1 = document(query, QUERY.concat(Integer.toString(i)), "");
                    map.addLink(url1);
                }
                
                if (rdt == null) {
                    // no result
                }
                else {
                    Mappings mymap = rdt.getPointerObject().getMappings();
                    // set endpoint URL as link in query results
                    // use case: GUI federated debugger
                    mymap.addLink(name);
                    ResultFormat fm = ResultFormat.create(mymap);
                    fm.setNbResult(mymap.getDisplay());
                    String url2 = document(fm.toString(), OUTPUT.concat(Integer.toString(i)), "");
                    map.addLink(url2);
                }

                i++;
            }
        }
    }
    
    void logSelect(Mappings map, ContextLog log) {
        if (log.getASTSelect() != null) {
            String query = log.getASTSelect().toString();
            query = String.format("# source selection query \n%s", query);
            String url1 = document(query, QUERY.concat(SEL), "");
            map.addLink(url1);
            
            if (log.getSelectMap() != null) {
                ResultFormat fm = ResultFormat.create(log.getSelectMap());
                String url2 = document(fm.toString(), OUTPUT, "");
                map.addLink(url2);
            }
        }
        
        if (log.getAST() != null) {
            String query = log.getAST().toString();
            query = String.format("# federated query \n%s", query);
            String url = document(query, QUERY.concat(REW), "");
            map.addLink(url);            
        }
    }
    
    void logQuery(Mappings map) {
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
    
    
    
}
