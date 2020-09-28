package fr.inria.corese.sparql.triple.parser;

import static fr.inria.corese.sparql.triple.parser.Access.Feature.*;
import static fr.inria.corese.sparql.triple.parser.Access.Level.*;
import java.util.HashMap;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class Access {
    
    static final String NL = System.getProperty("line.separator");

    public enum Mode {
        LIBRARY, GUI, SERVER
    }
    
    // Protection Level Access Right: 
    // function has access level, user action is granted access level
    // access provided to function for action when function.level <= action.level
    // private is default level: function definition is private
    // restricted is access level for specific protected user query
    // public  is access level for protected server: LDScript use is public, the rest is private or restricted
    // super user is access level for LinkedFunction
    public enum Level     { 
        
        PUBLIC(1), RESTRICTED(2), PRIVATE(3), DENIED(4), SUPER_USER(5)  ; 
        
        private int value;
        
        public static Level DEFAULT = PRIVATE;
        public static Level USER    = PUBLIC;
        public static Level DENY    = DENIED;
        
        private Level(int n) {
            value = n;
        }
        
        int getValue() {
            return value;
        } 
        
        public Level min(Level r2) {
            if (this.getValue() <= r2.getValue()) {
                return this;
            }
            return r2;
        }
        
        boolean provide(Level l) {
            return getValue() >= l.getValue();
        }
    
    } ;
    
    public enum Feature  { 
        FUNCTION_DEFINITION, IMPORT_FUNCTION,
        LINKED_FUNCTION, LINKED_TRANSFORMATION,
        READ_WRITE, JAVA_FUNCTION,
        LD_SCRIPT, 
        // sparql query in LDScript
        SPARQL, 
        SPARQL_UPDATE, 
    }
    
    class FeatureLevel extends HashMap<Feature, Level> {
        
        FeatureLevel(Level l) {
            init(l);
        }
        
        FeatureLevel init(Level l) {
            for (Feature f : Feature.values()) {
                put(f, l);
            }
            return this;
        } 
               
    }
    
    private FeatureLevel table;
    
    private static boolean protect = false;
    
    private static Access singleton;
    
    private static Mode mode; 
    
    static {
        singleton = new Access(DEFAULT);
        setMode(Mode.LIBRARY);
    }
    
    Access() {
        this(PRIVATE);
    }
    
    Access(Level l) {
        table = new FeatureLevel(l);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mode: ").append(getMode()).append(NL);
        sb.append(table());
        return sb.toString();
    }
    
    public Access singleton() {
        return singleton;
    }
    
    
    private static FeatureLevel driver() {
        return singleton.table();
    }
    
    private FeatureLevel table() {
        return table;
    }
    
    public static void deny(Feature feature) {
        set(feature, DENY);
    }
    
    public static void authorize(Feature feature) {
        set(feature, DEFAULT);
    }
        
    public static void set(Feature feature, Level accessRight) {
        driver().put(feature, accessRight);
    }
    
    public static Level get(Feature feature) {
        return driver().get(feature);
    }
    
    // consider level value 
    public static boolean provide(Feature feature, Level level) {
        return level.provide(get(feature));
    }
    
    public static boolean provide(Feature feature) {
        return DEFAULT.provide(get(feature));
    }
    
    // tune level value if the server is protected or not 
    // if the server is not protected, public -> default
    public static boolean accept(Feature feature, Level actionLevel) {
        return getLevel(actionLevel).provide(get(feature));
    }
    
    public static boolean accept(Feature feature) {
        return DEFAULT.provide(get(feature));
    }
    
    public static boolean accept(Feature feature, Context c) {
        if (c == null) {
            return accept(feature);
        }
        return accept(feature, c.getLevel());
    }
    
    public static boolean reject(Feature feature, Context c) {
        return ! accept(feature, c);
    }
    
    public static boolean reject(Feature feature, Level actionLevel) {
        return ! accept(feature, actionLevel);
    }
    
    public static boolean reject(Feature feature) {
        return ! accept(feature);
    }
    
    
    public static Level getLevel(Level actionLevel) {        
        return actionLevel;
    }
    
    /**
     * Used by server to grant access right to server query (user query or system query)
     * user = true : user query coming from http request
     * special = true: grant RESTRICTED access level (better than PUBLIC) 
     * Return the access right granted to the query 
     */
    public static Level getQueryAccessLevel(boolean user, boolean special) {
        if (isProtect()) {
            // run in protect mode
            if (user) {
                if (special) {
                    // special case: authorize SPARQL_UPDATE (e.g. for tutorial)
                    return RESTRICTED;
                }
                else {
                    // user query has only access to PUBLIC feature
                    return USER;
                }
            }
        }
        return DEFAULT;
    }
    

    
    public static void setLinkedFeature(boolean b) {
         setLinkedFunction(b);
         setLinkedTransformation(b);
    }
        
    /**
     * Available for DEFAULT but not for PUBLIC
     */
    public static void setLinkedFunction(boolean b) {
        if (b) {
            authorize(LINKED_FUNCTION);
        } else {
            deny(LINKED_FUNCTION);
        }
    }
    
    public static void setLinkedTransformation(boolean b) {
        if (b) {
            authorize(LINKED_TRANSFORMATION);
        } else {
            deny(LINKED_TRANSFORMATION);
        }
    }
    
    // everything is forbiden to DEFAULT level
    public static void restrict() {
        singleton = new Access(DENIED);
    }
    
    
    public static Mode getMode() {
        return mode;
    }

    public static void setMode(Mode m) {
        mode = m;
        singleton.initMode();
        //System.out.println(singleton);
    }
    
    void initMode() {
        switch (mode) {
            case LIBRARY:
                initLibrary();
                break;
            case GUI:
                initGUI();
                break;
            case SERVER:
                initServer();
                break;
        }
    }
    
    void initLibrary() {
        init();
    }
    
    void initGUI() {
        init();
    }
    
    void initServer() {
        init();
    }
    
    /**
     * default mode:
     * everything is authorized as DEFAULT except some features
     * in server mode, user query is PUBLIC
     * 
     */
    void init() {
        deny(LINKED_FUNCTION);
        // external transformation may contain/import function definition
        //deny(LINKED_TRANSFORMATION);
        set(SPARQL_UPDATE, RESTRICTED);
        set(LD_SCRIPT, PUBLIC);
    }
    
    /**
     * protected mode:
     * some features are protected
     *
     */
    public static void protect() {
        setProtect(true);
        deny(READ_WRITE);
        deny(JAVA_FUNCTION);
        deny(LINKED_FUNCTION);
        // external transformation may contain/import function definition
        deny(LINKED_TRANSFORMATION);
        // IMPORT_FUNCTION is PRIVATE and user query is PUBLIC
    }
    
      /**
     * @return the protect
     */
    public static boolean isProtect() {
        return protect;
    }

    /**
     * @param aProtect the protect to set
     */
    public static void setProtect(boolean aProtect) {
        protect = aProtect;
    }
    
    
}
