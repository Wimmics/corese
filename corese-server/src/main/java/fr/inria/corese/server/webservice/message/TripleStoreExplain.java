package fr.inria.corese.server.webservice.message;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphDistance;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.URLParam;
import org.json.JSONObject;

/**
 * Explain why query fail
 * Search in the graph if there exist property and resource URI 
 * similar to those in the query
 * Return explanation in json object, ready for Linked Result message
 */
public class TripleStoreExplain {
       
    private QueryProcess queryProcess;
    private JSONObject json;
    private Mappings map;
    private Query query;
    private ASTQuery ast;
    private Context context;
    
    TripleStoreExplain(QueryProcess exec, Context context, Mappings map) {
        setQueryProcess(exec);
        setMappings(map);
        setQuery(map.getQuery());
        setAst(getQuery().getAST());
        setContext(context);
    }
    
    
    JSONObject distance() {
        int distance = GraphDistance.DISTANCE;
        
        if (getContext().hasValue(URLParam.DISTANCE)) {
            // URL parameter sv:distance=n
            IDatatype dt = getContext().getFirst(URLParam.DISTANCE);
            distance = Integer.valueOf(dt.getLabel());
        }
        
        JSONObject obj = getGraph().match(getAst(), distance);                        
        return obj;
    }
      
    Graph getGraph() {
        return getQueryProcess().getGraph();
    }


    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public Mappings getMappings() {
        return map;
    }

    public void setMappings(Mappings map) {
        this.map = map;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public ASTQuery getAst() {
        return ast;
    }

    public void setAst(ASTQuery ast) {
        this.ast = ast;
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
