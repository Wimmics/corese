package fr.inria.corese.sparql.triple.parser;

import static fr.inria.corese.sparql.triple.parser.VariableScope.Scope.INSCOPE;
import static fr.inria.corese.sparql.triple.parser.VariableScope.Scope.SUBSCOPE;
import static fr.inria.corese.sparql.triple.parser.VariableScope.Scope.ALLSCOPE;

/**
 * Used by  TopExp getVariables(scope)
 */
public class VariableScope {

    public enum Scope {
        ALLSCOPE,
        INSCOPE,
        // Variable surely bound: left part of optional, common var in union branchs
        // former getVariables()
        SUBSCOPE
    };
    
    public static final VariableScope inscope  = inscope();
    public static final VariableScope subscope = subscope();
    public static final VariableScope allscope = allscope();
    public static final VariableScope filterscope = filterscope();
    
    private boolean excludeLocal = false;
    // from  BGP to filter and from exists to filter
    private boolean filter = false;
    // from filter to exists
    private boolean exist = true;
    
    private Scope scope;
    
    public VariableScope() {
        this(INSCOPE);
    }
         
    public VariableScope(Scope s) {
        setScope(s);
    }
    
    public static VariableScope filterscope() {
        return new VariableScope(INSCOPE).setFilter(true);
    }
    
    public static VariableScope filterscopeNotLocal() {
        return new VariableScope(INSCOPE).setFilter(true).setExcludeLocal(true);
    }
    
    public static VariableScope inscope() {
        return new VariableScope(INSCOPE);
    }
    
    public static VariableScope subscope() {
        return new VariableScope(SUBSCOPE);
    }
    
    public static VariableScope allscope() {
        return new VariableScope(ALLSCOPE);
    }
      
    /**
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public VariableScope setScope(Scope scope) {
        this.scope = scope;
        return this;
    }
    
    /**
     * @return the excludeLocal
     */
    public boolean isExcludeLocal() {
        return excludeLocal;
    }

    /**
     * @param excludeLocal the excludeLocal to set
     */
    public VariableScope setExcludeLocal(boolean excludeLocal) {
        this.excludeLocal = excludeLocal;
        return this;
    }

    /**
     * @return the traversal
     */
    public boolean isFilter() {
        return filter;
    }

    /**
     * @param traversal the traversal to set
     */
    public VariableScope setFilter(boolean traversal) {
        this.filter = traversal;
        return this;
    }
    
     /**
     * @return the exist
     */
    public boolean isExist() {
        return exist;
    }

    /**
     * @param exist the exist to set
     */
    public VariableScope setExist(boolean exist) {
        this.exist = exist;
        return this;
    }
   
    
    
}
