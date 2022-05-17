package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.CONTEXT;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/**
 * Execution Context for SPARQL Query and Template
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Context extends ASTObject implements URLParam {

    public static final String NL = System.getProperty("line.separator");
    public static final String STL = NSManager.STL;
    public static final String STL_QUERY    = STL + "query";
    public static final String STL_NAME     = STL + "name"; // query path name
    public static final String STL_SERVICE  = STL + "service"; // /srv/template/
    public static final String STL_SERVER   = STL + "server";  //  http://corese.inria.fr
    public static final String STL_PROFILE  = STL + "profile"; // st:dbpedia
    public static final String STL_SERVER_PROFILE = STL + "definition"; // profile.ttl graph
    public static final String STL_TRANSFORM = STL + "transform"; // st:navlab
    public static final String STL_LOD_PROFILE = STL + "lodprofile"; 
    public static final String STL_URI      = STL + "uri";        // focus resource URI
    public static final String STL_PROTOCOL = STL + "protocol";   // st:ajax
    public static final String STL_AJAX     = STL + "ajax";
    public static final String STL_CONTEXT  = STL + "context";    // query named graph for tutorial 
    public static final String STL_CONTEXT_LIST  = STL + "contextlist";    //  context graph list for tutorial 
    public static final String STL_DATASET  = STL + "dataset";    // dataset named graph
    public static final String STL_EXPORT   = STL + "export";   
    public static final String STL_EXPORT_LIST   = STL + "exportlist";   
    public static final String STL_IMPORT   = STL + "import";   
    public static final String STL_PARAM    = STL + "param";   
    public static final String STL_ARG      = STL + "arg";   
    public static final String STL_FORMAT   = STL + "format";   
    public static final String STL_LANG     = STL + "lang";
    public static final String STL_TITLE    = STL + "title";
    public static final String STL_VALUES   = STL + "values";
    public static final String STL_FILTER   = STL + "filter";
    public static final String STL_BIND     = STL + "bind";
    public static final String STL_MODE     = STL + "mode";
    public static final String STL_CLEAN    = STL + "clean";
    public static final String STL_WORKFLOW = STL + "workflow";
    public static final String STL_DEBUG    = STL + "debug";
    public static final String STL_LOOP     = STL + "loop";
    public static final String STL_INDEX    = STL + "index";
    public static final String STL_TEST     = STL + "test";
    public static final String STL_GRAPH    = STL + "graph";
    public static final String STL_GRAPH_LIST    = STL + "graphs";
    public static final String STL_TRANSFORMATION_LIST    = STL + "transformations";
    public static final String STL_SOLUTION = STL + "solution";
    public static final String STL_VALUE    = STL + "value";
    public static final String STL_VISITOR  = STL + "visitor";
    public static final String STL_DEFAULT  = STL + "default";
    public static final String STL_REMOTE_HOST  = STL + "remoteHost";
    public static final String STL_PATTERN        = STL + "pattern";
    public static final String STL_PATTERN_PARAM  = STL + "patternparam";
    public static final String STL_PATTERN_VALUE  = STL + "patternvalue";
    public static final String STL_PATTERN_OPTION = STL + "patternoption";
    public static final String STL_PROCESS_QUERY  = STL + "processquery";
    public static final String STL_METADATA       = STL + "metadata";
    public static final String STL_PREFIX         = STL + "prefix";
    public static final String STL_MAPPINGS       = STL + "mappings";
    public static final String STL_RESTRICTED     = STL + "restricted";
    public static final String STL_HIERARCHY      = STL + "hierarchy";
    public static final String STL_LINK           = STL + "link";
   
    public static final String URL = "url";   
    
    
    HashMap<String, IDatatype> table;
    static  HashMap<String, Boolean> sexport;
    HashMap<String, Boolean> export;
    private HashMap<String, Context> context;
    NSManager nsm;
    private ContextLog contextLog;
    private ASTQuery ast;
    
    private Binding bind;
    private AccessRight accessRight;
    Access.Level level = Access.Level.USER_DEFAULT;
    private boolean debug;
    
    private boolean userQuery = false;
    private String key;
    private boolean federateIndex = false;
    private boolean discovery = false;
    private boolean selection = false;
    
   static {
       sexport = new HashMap();
       sexport.put(STL_DATASET, true);
       sexport.put(STL_PROTOCOL, true);
       sexport.put(STL_SERVICE, true);
   }

    public Context() {
        table = new HashMap();
        export = new HashMap();
    }
    
    public static Context create() {
        return new Context();
    }
    
    public Context(Access.Level level) {
        this();
        setLevel(level);
    }
    
    public Context(AccessRight access) {
        this();
        setAccessRight(access);
    }

    @Override
    public String toString() {        
        StringBuilder sb = new StringBuilder();
        String[] keys = new String[table.size()];
        table.keySet().toArray(keys);
        Arrays.sort(keys);
        for (String key : keys) {           
            sb.append( nsm().toPrefix(key, true));
            sb.append(" : ");
            sb.append((table.get(key) == null) ? "null" : table.get(key));
            sb.append(NL);
        }
        if (isUserQuery()) {
            sb.append("user query: true").append(NL);
        }
        sb.append("level: ").append(getLevel()).append(NL);
        if (getContextLog() != null){
            sb.append(getContextLog().toString());
            sb.append(NL);
        }
        return sb.toString();
    }
    
    public NSManager  nsm(){
        if (nsm == null){
            nsm = NSManager.create();
        }
        return nsm;
    }
    
    public String trace() {
        StringBuilder sb = new StringBuilder();
        if (get(STL_URI) != null) {
            sb.append(String.format("URI: %s Date: %s", get(STL_URI).getLabel(), new Date()));
        }
        if (getService() != null) {
            sb.append("  Service: ").append(getService());
        }
        if (getProfile() != null) {
            sb.append("  Profile: ").append( nsm().toPrefix(getProfile()));
        }
        if (get(STL_REMOTE_HOST) != null) {
            sb.append("  Host: ").append(get(STL_REMOTE_HOST).getLabel());
        }
        return sb.toString();
    }
        
    public Collection<String> keys(){
        return table.keySet();
    }
    
    public Context copy(){
        Context c = new Context();
        c.copy(this);
        return c;
    }
    
     public void complete(Context source) {
        IDatatype export = source.get(Context.STL_EXPORT);
        if (export != null && export.booleanValue()) {
            copy(source);
        } else {
            // dataset, protocol
            include(source);
        }
        if (source.getNamedContext() != null){
            setNamedContext(source.getNamedContext());
        }
     }
    
    public void copy(Context source){
        for (String str : source.keys()){
            if (source.export(str)){
               export(str, source.get(str)); 
            }
            else {
                set(str, source.get(str));
            }
        }
    }
    
    
    // this include source
    public void include(Context source){
        sinclude(source);
        source.export(this);
    }
     
    // this include source, static property
      public void sinclude(Context source){
        for (String str : sexport.keySet()){
            if (sexport.get(str) && source.get(str) != null){
                set(str, source.get(str));
            }
        }       
    }
     
     // export this to target, dynamic property
     void export(Context target){
       for (String str : export.keySet()){
            if (export(str)){
                target.export(str, get(str));
            }
        }
     }
           
    boolean export(String str){        
        return export.get(str) != null && export.get(str) && get(str) != null;
    }
    
    public Context getContext(IDatatype name){
        return getContext(name.getLabel());
    }
    
    public Context getContext(String name){
        if (getNamedContext() == null){
            setNamedContext(new HashMap<String, Context>());
        }
        Context c = getNamedContext().get(name);
        if (c == null){
            c = new Context();
            getNamedContext().put(name, c);
        }
        return c;
    }
    
    public IDatatype getDatatypeValue(){
        return DatatypeMap.createObject(this);
    }
    
    public IDatatype getNamedContextList() {
        ArrayList<IDatatype> list = new ArrayList<>();
        if (getNamedContext() != null) {
            for (String name : getNamedContext().keySet()) {
                list.add(DatatypeMap.newResourceOrLiteral(name));
            }
        }
        return DatatypeMap.newInstance(list);
    }
    
    public IDatatype getNamedContextDatatypeValue(String name) {
        if (getNamedContext() == null || getContext(name) == null) {
            return null;
        }
        Context c = getContext(name);
        return c.getDatatypeValue();
    }
    
    public IDatatype cget(IDatatype name, IDatatype slot){
        return getContext(name).get(slot);
    }
    
    public IDatatype cset(IDatatype name, IDatatype slot, IDatatype value){
        getContext(name).set(slot, value);
        return value;
    }
    
    // remove named context
    public IDatatype cremove(IDatatype name) {
        if (getNamedContext() != null){    
            getNamedContext().remove(name.getLabel());
        }
        return DatatypeMap.TRUE;
    }
    
    public Context set(IDatatype name, IDatatype value) {
        return set(name.getLabel(), value);
    }
    
    public Context set(String name, IDatatype value) {
        if (value == null){
            table.remove(name);
        }
        else {
            table.put(name, value);
        }
        return this;
    }
    
     public Context setName(String name, IDatatype value) {
        return set(NSManager.STL+name, value);
    }
    
    public Context export(String name, IDatatype value) {
        table.put(name, value);
        defineExport(name);
        return this;
    }
    
    void defineExport(String name) {
        export.put(name, true);
    }
    
    public void init() {
        defineExport();
    }
    
    public void defineExport() {
        if (hasValue(STL_EXPORT_LIST)) {
            for (IDatatype def : get(STL_EXPORT_LIST).getValueList()) {
                defineExport(def.getLabel());
            }
        }
    }

    
    public Context exportName(String name, IDatatype value) {
        return export(NSManager.STL+name, value);
    }

    public Context set(String name, String str) {
        if (str == null) {
            table.remove(name);
        }
        else {
            set(name, DatatypeMap.newInstance(str));
        }
        return this;
    }
    
    public Context set(String name, List<String> list) {
        set(name, DatatypeMap.newStringList(list));
        return this;
    }
    
    public Context add(String name, String value) {
        return add(name, DatatypeMap.newInstance(value));
    }
    
    public Context add(String name, IDatatype value) {
         return add(name, value, false);
    }
    
    public Context add(String name, IDatatype value, boolean duplicate) {
        IDatatype list = get(name);
        if (list == null) {
            list = DatatypeMap.newList();
            set(name, list);
        }
        if (!duplicate && list.contains(value)) {
            return this;
        }
        list.getList().add(value);
        return this;
    }
    
    /**
     * URL param=property~skos:broader
     * key = property ; value = skos:broader
     */
    public void decode(String key, String value) {
        if (value.contains(";")) {
            for (String val : value.split(";")) {
                basicDecode(key, val);
            }
        }
        else {
            basicDecode(key, value);
        }
    }
    
    public void basicDecode(String key, String value) {        
        switch (key) {
            case PROPERTY: 
                add(STL_HIERARCHY, DatatypeMap.newResource(nsm().toNamespace(value)));
                break;
            case PREFIX:
                String pr = value.substring(0, value.indexOf(":"));
                String ns = value.substring(value.indexOf(":")+1);
                 nsm().defPrefix(pr, ns);
                break;
        }
    }
    
    public boolean acceptVariable(String var) {
        if (hasValue(UNSELECT)) {
            return ! getStringList(UNSELECT).contains(var);
        }
        return true;
    }
        
    public Context set(String name, int n) {
        set(name, DatatypeMap.newInstance(n));
        return this;
    }
     
     public Context set(String name, boolean n) {
        set(name, DatatypeMap.newInstance(n));
        return this;
    }

    public Context setURI(String name, String str) {
        set(name, DatatypeMap.newResource(str));
        return this;
    }

    public Context setTransform(String str) {
        return setURI(STL_TRANSFORM, str);
    }

    public String getTransform() {
        return stringValue(STL_TRANSFORM);
    }

    public Context setProfile(String str) {
        return setURI(STL_PROFILE, str);
    }

    public String getProfile() {
        return stringValue(STL_PROFILE);
    }

    public Context setURI(String str) {
        return setURI(STL_URI, str);
    }
    
     public Context setMode(String str) {
        return setURI(STL_MODE, str);
    }
     
    public Context setParam(String str) {
        return set(STL_PARAM, str);
    }
    
    public Context setFormat(String str) {
        return setURI(STL_FORMAT, str);
    }

    public String getURI() {
        return stringValue(STL_URI);
    }
    
    public Context setProtocol(String str){
        return setURI(STL_PROTOCOL, str);
    }

    public Context setQueryString(String str) {
        return set(STL_QUERY, str);
    }

    // add values clause to query
    public Context addValue(String value) {
        String squery = getQueryString();
        if (getURI() == null && squery != null) {
            setQueryString(squery + value);
        }
        return this;
    }

    public String getQueryString() {
        return stringValue(STL_QUERY);
    }

    public Context setName(String str) {
        return setURI(STL_NAME, str);
    }

    public String getName() {
        return stringValue(STL_NAME);
    }

    public Context setService(String str) {
        return set(STL_SERVICE, str);
    }
    
    public Context setServer(String str) {
        return setURI(STL_SERVER, str);
    }
    
    public Context setRemoteHost(String remoteHost) {
         return set(STL_REMOTE_HOST, remoteHost);
    }
    
    public Context setTitle(String str) {
        return setURI(STL_TITLE, str);
    }
    
    public Context setTitle(IDatatype dt) {
        return set(STL_TITLE, dt);
    }
    
    public String getService() {
        return stringValue(STL_SERVICE);
    }
    
    public Context setLang(String str) {
        return set(STL_LANG, str);
    }

    public String getLang() {
        return stringValue(STL_LANG);
    }


    public IDatatype get(String name) {
        return table.get(name);
    }
    
    public List<String> getStringList(String name) {
        IDatatype dt = get(name);
        if (dt == null) {
            return null;
        }
        return DatatypeMap.toStringList(dt);
    }
    
    /**
     * 
     * mode -> ((key value) (key value))
     */
    public IDatatype getValueInList(String mode, String name) {
        IDatatype dt = get(mode);
        if (dt != null && dt.isList()) {
            for (IDatatype pair : dt) {
                if (pair.isList() && pair.size()>=2 &&
                        pair.get(0).getLabel().equals(name)) {
                    return pair.get(1);
                }
            }
        }
        return null;
    }

    public IDatatype getFirst(String name) {
        IDatatype dt = table.get(name);
        if (dt == null) {
            return null;
        }
        if (dt.isList()) {
            if (dt.size() == 0) {
                return null;
            }
            return dt.get(0);
        }
        return dt;
    }
    
    public IDatatype get(IDatatype name) {
        return table.get(name.getLabel());
    }
    
    public boolean isList(String name) {
        switch (name) {
            case STL_PATTERN_VALUE:
            case STL_PATTERN_OPTION: return true;
        }
        return false;
    }
    
    
    public boolean hasValue(String name, String value) {
        IDatatype dt = table.get(name);
        return dt != null && dt.getLabel().equals(value);
    }
    
    public boolean hasValue(String name, IDatatype value) {
        IDatatype dt = table.get(name);
        return dt != null && dt.equals(value);
    }
    
    public boolean hasValue(String name) {
        return get(name) != null;
    }
    
    public boolean hasEveryValue(String... name) {
        for (String key : name) {
            if (! hasValue(key)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasAnyValue(String... name) {
        for (String key : name) {
            if (hasValue(key)) {
                return true;
            }
        }
        return false;
    }
    
    public IDatatype getName(String name) {
        return get(NSManager.STL+name);
    }

    public String stringValue(String name) {
        IDatatype dt = table.get(name);
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    /**
     * @return the userQuery
     */
    public boolean isUserQuery() {
        return userQuery;
    }
    
    public Access.Level getLevel() {
//        if (isUserQuery()) {
//            return Access.Level.PUBLIC;
//        }
        return level;
    }
    
    public Context setLevel(Access.Level l) {
        level = l;
        return this;
    }

    /**
     * @param userQuery the userQuery to set
     */
    public void setUserQuery(boolean userQuery) {
        this.userQuery = userQuery;
    }

    public void setServerProfile(IDatatype obj) {
        set(STL_SERVER_PROFILE, obj);
    }
    
    @Override
    public PointerType pointerType(){
        return CONTEXT;
    }
       
    @Override
    public IDatatype getList(){
        ArrayList<IDatatype> list = new  ArrayList<IDatatype>();
        for (String key : table.keySet()){
            IDatatype val = table.get(key);
            if (val != null){
                IDatatype dt   = DatatypeMap.createResource(key);
                IDatatype pair = DatatypeMap.createList(dt, val);
                list.add(pair);
            }
        }
        return DatatypeMap.createList(list);
    }
    
     @Override
    public String getDatatypeLabel() {
       return String.format("[Context: size=%s]", size());
    }

    /**
     * @return the context
     */
    public HashMap<String, Context> getNamedContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setNamedContext(HashMap<String, Context> context) {
        this.context = context;
    }         

    /**
     * @return the bind
     */
    public Binding getBind() {
        return bind;
    }

    /**
     * @param bind the bind to set
     */
    public Context setBind(Binding bind) {
        this.bind = bind;
        return this;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the accessRight
     */
    public AccessRight getAccessRight() {
        return accessRight;
    }

    /**
     * @param accessRight the accessRight to set
     */
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }
    
    /**
     * Service log manager
     */
    public ContextLog getLog() {
        if (getContextLog() == null) {
            setContextLog(new ContextLog());
        }
        return getContextLog();
    }

    /**
     * @return the contextLog
     */
    public ContextLog getContextLog() {
        return contextLog;
    }

    /**
     * @param contextLog the contextLog to set
     */
    public void setContextLog(ContextLog contextLog) {
        this.contextLog = contextLog;
    }
    
    public boolean accept(String uri) {
        if (hasValue(REJECT)) {
            for (IDatatype dt : get(REJECT)) {
                if (uri.contains(dt.getLabel())) {
                    return false;
                }
            }
        }

        if (hasValue(ACCEPT)) {
            for (IDatatype dt : get(ACCEPT)) {
                if (uri.contains(dt.getLabel())) {
                    return true;
                }
            }
            return false;
        }
        
        return true;
    }
    
    

    public String tune(String uri) {
        return uri;
    }
    
    /**
     * Complete Service URL parameters with data from context
     * Use case: federated query with sv:nboutput=100
     */   
    public URLServer tune(URLServer url) {
        if (hasValue(MODE)) {
            for (IDatatype mode : get(MODE)) {
                switch (mode.getLabel()) {
                    case DEBUG:
                    case TRACE:
                        url.getMap().add(MODE, mode.getLabel());
                        break;
                }
            }
        }
        
        if (hasValue(EXPORT)) {
            // defined by URLServer decode
            // share parameters such as timeout=1000
            for (IDatatype key : get(EXPORT)) {
                url.getMap().add(key.getLabel(), getFirst(key.getLabel()).getLabel());
            }
        }
        
        if (hasValue(URI)) {
            for (IDatatype dt : get(URI)) {
                url.getMap().add(URI, dt.getLabel());
            }
        }
        return url;
    }
    
    // source selection inherit some context parameters
    public Context inherit(Context ct) {
        if (ct == null) {
            return this;
        }
        if (ct.hasValue(EXPORT)) {
            for (IDatatype key : ct.get(EXPORT)) {
                if (key.getLabel().equals(TIMEOUT)) {
                    set(TIMEOUT, ct.get(TIMEOUT));
                    add(EXPORT, key);
                }
            }
        }
        return this;
    }
    
    
    /**
     * mode=demo 
     * get parameter value list of demo (from global Context from urlprofile.ttl)
     * get value of parameter name in parameter value list
     */
    public String getDefaultValue(List<String> modeList, String name) {
        for (String mode : modeList) {
            // get value of parameter name in parameter list of mode
            IDatatype dt = getValueInList(mode, name);
            if (dt != null) {
                return dt.getLabel();
            }
        }
        // try default mode * (use case: query parameter is required before any context & default processing) 
        IDatatype dt = getValueInList(STAR, name);
        if (dt != null) {
            return dt.getLabel();
        }
        return null;
    }
    
     /**
     * Get parameter values associated to endpoint URL or to mode in server global Context gc
     * Complete current context with such parameter values
     * two use case, with name is url or name is mode:
     * a) url = http://corese.inria.fr/psparql          -> name = url
     * b) url = http://corese.inria.fr/sparql?mode=demo -> name = demo
     * 
     * @fixit: may loop when mode recursively refers to mode.
     */
    public void context(Context gc, String name) {
        // Consult Profile Context to get predefined parameters associated to name
        // name is url or mode
        IDatatype dt = gc.get(name);
        if (dt != null) {
            // dt is list of (key value value)
            for (IDatatype pair : dt) {
                String key = pair.get(0).getLabel();
                
                for (IDatatype val : pair.getList().rest()) {
                    if (key.equals(MODE) || key.equals(PARAM)) {
                        mode(gc, key, val.getLabel());
                    } else {
                        add(key, val);
                    }
                }
            }
        }
    }
    
    /**
     * name:  param | mode
     * value: debug | trace.
     */
    public void mode(Context gc, String name, String value) {
        if (value.contains(";")) {
            for (String val : value.split(";")) {
                basicMode(gc, name, val);
            }
        }
        else {
            basicMode(gc, name, value);
        }
    }
    
    

    void basicMode(Context gc, String name, String value) {
        // mode = value
        add(name, value);

        switch (name) {
            case MODE:
                switch (value) {
                    case DEBUG:
                        setDebug(true);
                    // continue

                    default:
                        // get definition of mode=value if any
                        // defined in urlprofile.ttl
                        context(gc, value);
                        set(value, true);
                        break;
                }
                break;

            case PARAM:
                // decode param=key~val;val
                URLServer.decode(this, value);
                break;
        }
    }
    
    /**
     * name = st:all
     * return (st:xml st:json) 
     * from urlprofile.ttl transformation st:equivalent definition
     * PRAGMA: this Context is server global Context
     */
    public void prepare(String name, List<String> list) {
        name = nsm().toNamespace(name);
        List<String> alist = getStringList(name);
        if (alist == null) {
            list.add(name);
        } else {
            list.addAll(alist);
        }
    }
    
    String complete(String uri, String key, String val) {
        if (uri.contains("?")) {
            uri = String.format("%s&%s=%s", uri, key, val);
        } else {
            uri = String.format("%s?%s=%s", uri, key, val);
        }
        return uri;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCreateKey() {
        if (getKey() == null) {
            setKey(UUID.randomUUID().toString());
        }
        return getKey();
    }
    
    public HashMap<String, IDatatype> getHashMap() {
        return table;
    }
    
    public Collection<String> keySet() {
        return getHashMap().keySet();
    }
    
    public JSONObject json() {
        JSONObject json = new JSONObject();
        
        for (String key : keySet()) {
            IDatatype dt = get(key);
            DatatypeMap.set(json, key, dt);
        }
        
        return json;
    }

    public ASTQuery getAST() {
        return ast;
    }

    public Context setAST(ASTQuery ast) {
        this.ast = ast;
        return this;
    }

    public boolean isFederateIndex() {
        return federateIndex;
    }

    public Context setFederateIndex(boolean federateIndex) {
        this.federateIndex = federateIndex;
        return this;
    }

    public boolean isDiscovery() {
        return discovery;
    }

    public Context setDiscovery(boolean discovery) {
        this.discovery = discovery;
        return this;
    }

    public boolean isSelection() {
        return selection;
    }

    public Context setSelection(boolean selection) {
        this.selection = selection;
        return this;
    }
    
}
