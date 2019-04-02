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
    
    // Protection Level Access Right: access provided to <= level
    // private is default level: function definition is private
    // public  is access level for protected server: LDScript use is public, the rest is private
    // locked is default value for LinkedFunction
    public enum Level     { 
        
        PUBLIC(1), PRIVATE(2), SUPER_USER(3) ; 
        
        private int value;
        
        public static Level DEFAULT = PRIVATE;
        
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
        FUNCTION_DEFINITION, LINKED_FUNCTION, 
        READ_WRITE_JAVA, 
        LD_SCRIPT, SPARQL_UPDATE, 
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
    
    static boolean protect = false;
    
    private static Access singleton;
    
    private static Mode mode; 
    
    static {
        singleton = new Access(PRIVATE);
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
    
    public static boolean reject(Feature feature, Level actionLevel) {
        return ! accept(feature, actionLevel);
    }
    
    public static boolean reject(Feature feature) {
        return ! accept(feature);
    }
    
    /**
     * if query is public (it comes from http sparql endpoint)
     *   if server is protect : access level is public (protect the query)
     *   else access is default (it is my query, I want all features)
     */
    public static Level getLevel(Level actionLevel) {
        if (actionLevel == PUBLIC) {
            if (protect) {
                return PUBLIC;
            }
            return DEFAULT;
        }
        return actionLevel;
    }
    
    // protect DEFAULT level
    public static void protect() {
        protect = true;
        set(READ_WRITE_JAVA, SUPER_USER);
    }
    
    public static void setLinkedFunction(boolean b) {
        if (b) {
            set(LINKED_FUNCTION, DEFAULT);
            //System.out.println(singleton);
        }
    }
    
    // everything is forbiden to DEFAULT level
    public static void restrict() {
        singleton = new Access(SUPER_USER);
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
    
    void init() {
        // everything is private except:
        set(LINKED_FUNCTION, SUPER_USER);
        set(LD_SCRIPT, PUBLIC);
    }
    
 
}
