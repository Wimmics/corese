package fr.inria.corese.core.print;

import fr.inria.corese.sparql.triple.parser.Context;

/**
 * Root class for Result Format
 */
public class QueryResultFormat {

    private Context context;
    
    public QueryResultFormat() {}
    
    public QueryResultFormat init(Context c) {
        setContext(c);
        return this;
    }
    
    /**
     * in service: param=sv:unselect~var   -- var name without "?"
     * context manage unselect variable list
     * if var is unselect, do not pprint var value in result
     */
    public boolean accept(String var) {
        if (var.startsWith("?") || var.startsWith("$")) {
            var = var.substring(1);
        }
        if (getContext() == null) {
            return true;
        }        
        return getContext().acceptVariable(var);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    
    
}
