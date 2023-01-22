package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.sparql.exceptions.SafetyException;
import static fr.inria.corese.sparql.triple.parser.Access.Feature.*;
import static fr.inria.corese.sparql.triple.parser.Access.Level.*;
import static fr.inria.corese.sparql.triple.parser.Access.Mode.SERVER;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Access model for features
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2019
 *
 */
public class Access {
    public static Logger logger = LoggerFactory.getLogger(Access.class);
    
    static final String NL = System.getProperty("line.separator");
    // throw exception for undefined expression, see FunctionCompiler
    // false -> SPARQL semantics
    // true  -> safety check semantics
    public static boolean UNDEFINED_EXPRESSION_EXCEPTION = false;
    // throw exception for coalesce  
    // false -> sparql semantics, coalesce trap error
    public static boolean COALESCE_EXCEPTION = false;
    
    // true -> skip access control
    public static boolean SKIP = false;

    public enum Mode {
        LIBRARY, GUI, SERVER
    }
    
    /** 
     * Protection Level Access Right: 
    * feature has access level, user action (query) has access level
    * action is granted access to feature when action.level >= feature.level 
    * PUBLIC is access level granted to user query on protected server 
    * for ex, LDScript use is PUBLIC, the rest is PRIVATE or RESTRICTED
    * RESTRICTED is access level for specific protected user query
    * PRIVATE is default level: function definition is private
    * SUPER USER is access level for LinkedFunction
    * 
    */
    public enum Level     { 
        
        PUBLIC(1), RESTRICTED(2), PRIVATE(3), DENIED(4), SUPER_USER(5)  ; 
        
        private int value;
        
        // default feature access level 
        public static Level DEFAULT = PRIVATE;
        // default user access level (by default user have access to features)
        // protected server may restrict user access level to PUBLIC
        public static Level USER_DEFAULT = DEFAULT;
        // user with weak access
        public static Level USER    = PUBLIC;
        // deny access to feature
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
        
        // does this action level provide access to feature level 
        boolean provide(Level featureLevel) {
            if (SKIP) { return true; }
            return getValue() >= featureLevel.getValue();
        }
    
    } ;
    
    public enum Feature  {  
        EVENT,
        // @import <http://myfun.org/fun.rq> 
        // [] owl:imports <http://myfun.org/fun.rq>
        // load(file.rq)
        IMPORT_FUNCTION,
        // undefined ex:fun()  -> dereference, parse and compile ex:fun
        LINKED_FUNCTION, 
        // concern all transformations/rules (predefined and user defined)
        // if action level is < DEFAULT, authorize predefined transformation/rule only
        LINKED_TRANSFORMATION, 
        LINKED_RULE,
        

        SPARQL_UPDATE, 
        SPARQL_SERVICE,
        
        // may deny whole LDScript
        LDSCRIPT, 
        // define ldscript function us:test() {}
        DEFINE_FUNCTION, 
        // sparql query in LDScript: xt:sparql, query(select where), let (select where)
        LDSCRIPT_SPARQL, 
        // xt:read 
        READ, READ_FILE, 
        // not used yet
        WRITE, 
        // xt:load xt:write in /tmp/
        READ_WRITE, 
        // write anywhere
        SUPER_WRITE,
        // not used yet
        LOAD_FILE,
        // xt:httpget
        HTTP,
        // java:fun(xt:stack())
        JAVA_FUNCTION,

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
    
    public static void setDefaultUserLevel(Level l) {
        USER_DEFAULT = l;
    }
            
    public static boolean skip(boolean b) {
        boolean save = SKIP;
        SKIP = b;
        return save;
    }
    
    // when b = false, access checking is desactivated
    public static void setActive(boolean b) {
        SKIP = !b;
    }
    
    public static boolean isActive() {
        return ! SKIP;
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
    
    public static Level setValue(Feature feature, Level accessRight) {
        Level level = get(feature);
        driver().put(feature, accessRight);
        return level;
    }
   
    public static Level get(Feature feature) {
        return driver().get(feature);
    }
      
    public static boolean provide(Feature feature) {
        return accept(feature, USER_DEFAULT);
    }
    
    // tune level value if the server is protected or not 
    // if the server is not protected, public -> default
    public static boolean accept(Feature feature, Level actionLevel) {
        return actionLevel.provide(get(feature));
    }
    
    public static boolean accept(Feature feature) {
        return USER_DEFAULT.provide(get(feature));
    }
    
    public static boolean reject(Feature feature, Level actionLevel) {
        return ! accept(feature, actionLevel);
    }
    
    /**
     * feature = LINKED_TRANSFORMATION uri=st:turtle
     * feature = IMPORT_FUNCTION       uri=ex:myfun.rq
     * check uri: 
     * action level means user query level (action = query)
     * action level >= DEFAULT -> if accept is empty, every namespace is authorized
     * action level < DEFAULT  -> access to explicitely authorized namespace only
     * hint: LINKED_TRANSFORMATION is public and there is a list
     * of authorized namespace in server myprofile.ttl
     * st:access st:namespace uri1, uri2 .
    */
    public static boolean accept(Feature feature, Level actionLevel, String uri) {
//        logger.info(feature + " " + actionLevel + " " + accept(feature, actionLevel));
//        logger.info(uri + " " + acceptNamespace(feature, actionLevel, uri));
       return accept(feature, actionLevel) && acceptNamespace(feature, actionLevel, uri);
    }
    
    public static boolean accept(Feature feature, Level actionLevel, String uri, boolean acceptWhenEmpty) {
//        logger.info(feature + " " + actionLevel + " " + accept(feature, actionLevel));
//        logger.info(uri + " " + acceptNamespace(feature, actionLevel, uri));
       return accept(feature, actionLevel) && acceptNamespace(feature, actionLevel, uri, acceptWhenEmpty);
    }
    
    public static boolean reject(Feature feature, Level actionLevel, String uri) {
        return ! accept(feature, actionLevel, uri);
    }
    
    public static void check(Feature feature, Level actionLevel, String uri, String mes) throws SafetyException {
        if (reject(feature, actionLevel, uri)) {
            throw new SafetyException(mes + ": " + uri);
        }
    }
    
    public static void check(Feature feature, Level actionLevel, String uri, String mes, boolean acceptWhenEmpty) throws SafetyException {
        if (!Access.accept(feature, actionLevel, uri, acceptWhenEmpty)) {
            throw new SafetyException(mes);
        }
    }
    
    public static void check(Feature feature, Level actionLevel, String mes) throws SafetyException {
        if (Access.reject(feature, actionLevel)) {
            throw new SafetyException(mes);
        }
    }
        
    public static boolean acceptNamespace(Feature feature, Level actionLevel, String uri) {
        // action level >= DEFAULT -> if accept is empty, every namespace is authorized
        return acceptNamespace(feature, actionLevel, uri, actionLevel.provide(DEFAULT));
    }
    
    // if acceptWhenEmpty==true,  accept uri when accept list is empty
    // if acceptWhenEmpty==false, do not accept uri when accept list is empty
    public static boolean acceptNamespace(Feature feature, Level actionLevel, String uri, boolean acceptWhenEmpty) {
        if (SKIP || actionLevel.provide(SUPER_USER)) {
            return !AccessNamespace.forbidden(uri);
        } else {
            return accept(uri, acceptWhenEmpty);
        }
    }
    
    // used by server
    public static List<String> selectNamespace(Feature feature, Level level, List<String> list) {
        ArrayList<String> alist = new ArrayList<>();
        for (String uri : list) {
            if (acceptNamespace(feature, level, uri)) {
                alist.add(uri);
            }
        }
        return alist;
    }
    
    static boolean accept(String uri, boolean resultWhenEmptyAccept) {
        return NSManager.isPredefinedNamespace(uri) || AccessNamespace.access(uri, resultWhenEmptyAccept);
    }
    
    public static void define(String ns, boolean b) {
        AccessNamespace.define(ns, b);
    }
    
    public static void define(Feature feature, Level accessRight) {
        set(feature, accessRight); 
    }
    
    static boolean  isFile(String ns) {
        return ns.startsWith("/");
    }
    
    static String toFile(String ns) {
        return "file://" + ns;
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
     * Return access level granted to query 
     */
    public static Level getQueryAccessLevel(boolean user) {
        return getQueryAccessLevel(user, false);
    }
    
    /**
     * return access level granted to user query
     * user query may have access key set by parameter access=key
     */
    public static Level getQueryAccessLevel(boolean user, boolean hasKey) {
        if (isProtect()) {
            // run in protect mode
            if (user) {
                // user query
                if (hasKey) {
                    // user has key: authorize SPARQL_UPDATE 
                    return RESTRICTED; 
                }
                else {
                    // user query has only access to PUBLIC feature
                    return USER;
                }
            }
        }
        return USER_DEFAULT;
    }
    

    
    public static void setLinkedFeature(boolean b) {
         setLinkedFunction(b);
         setLinkedTransformation(b);
//         setLinkedRule(b);
    }
    
    public static void setReadFile(boolean b) {
         setFeature(READ_FILE, b);
    }
    
    public static void setFeature(Feature feature, boolean b) {
        if (b) {
            authorize(feature);
        } else {
            deny(feature);
        }
    }
        
    /**
     * Available for DEFAULT but not for PUBLIC
     */
    public static void setLinkedFunction(boolean b) {
        setFeature(LINKED_FUNCTION, b);
    }
    
    public static void setLinkedTransformation(boolean b) {
        setFeature(LINKED_TRANSFORMATION, b);
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
    
    public static boolean isServerMode() {
        return mode == SERVER;
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
        deny(READ_WRITE);
        deny(WRITE);
        deny(SUPER_WRITE);
        deny(READ_FILE);
        deny(LOAD_FILE);
        deny(JAVA_FUNCTION);
        // user query on protected server have USER access level
        // user query with parameter access=key 
        // is granted RESTRICTED access level instead of USER level
        // features below require RESTRICTED access level instead of PRIVATE level
        // hence they are accessible for user query with access key
        set(SPARQL_UPDATE, RESTRICTED);
        set(SPARQL_SERVICE, RESTRICTED);
        // draft test for st:logger
        set(LDSCRIPT_SPARQL, RESTRICTED);
        set(DEFINE_FUNCTION, RESTRICTED);
        set(READ, RESTRICTED);
    }
    
    /**
     * default mode:
     * everything is authorized as DEFAULT except some features
     * in server mode, user query is PUBLIC
     * 
     */
    void init() {
        deny(LINKED_FUNCTION);
        // xt:read st:format cannot read the file system
        // use case: server mode
        deny(READ_FILE);
        set(LDSCRIPT, PUBLIC);
        // authorize server for query + transform when transform is authorized
        set(LINKED_TRANSFORMATION, PUBLIC);
    }
    
    /**
     * protected mode:
     * some features are protected
     *
     */
    public static void protect() {
        setProtect(true);
        deny(READ_WRITE);
        deny(WRITE);
        deny(SUPER_WRITE);
        deny(HTTP);
        deny(JAVA_FUNCTION);
        deny(LINKED_FUNCTION);
        // other features are PRIVATE and user query is PUBLIC
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
    
    public static Level getLevel(Mapping m) {
        return getLevel(m, USER_DEFAULT);
    }

    public static Level getLevel(Mapping m, Level level) {
        if (m == null || m.getBind() == null) {
            return level;
        }
        return ((fr.inria.corese.sparql.triple.function.term.Binding)m.getBind()).getAccessLevel();
    }
    
    
    
}
