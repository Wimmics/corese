/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Pointerable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Execution Context for SPARQL Query and Template
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Context extends ASTObject {

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
    public static final String STL_CONTEXT  = STL + "context";    // query named graph (for tutorial) 
    public static final String STL_DATASET  = STL + "dataset";    // dataset named graph
    public static final String STL_EXPORT   = STL + "export";   
    public static final String STL_IMPORT   = STL + "import";   
    public static final String STL_PARAM    = STL + "param";   
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
    
    
    HashMap<String, IDatatype> table;
    static  HashMap<String, Boolean> sexport;
    HashMap<String, Boolean> export;
    private HashMap<String, Context> context;
    NSManager nsm;
    
    private boolean userQuery = false;
   
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

    @Override
    public String toString() {        
        StringBuilder sb = new StringBuilder();
        String[] keys = new String[table.size()];
        table.keySet().toArray(keys);
        Arrays.sort(keys);
        for (String key : keys) {
            sb.append(getNSM().toPrefix(key, true));
            sb.append(" : ");
            sb.append(table.get(key));
            sb.append(NL);
        }
        return sb.toString();
    }
    
    NSManager getNSM(){
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
            sb.append("  Profile: ").append(getNSM().toPrefix(getProfile()));
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
    
    Context getContext(String name){
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
    
    public IDatatype cget(IDatatype name, IDatatype slot){
        return getContext(name).get(slot.getLabel());
    }
    
    public IDatatype cset(IDatatype name, IDatatype slot, IDatatype value){
        getContext(name).set(slot.getLabel(), value);
        return value;
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
        export.put(name, true);
        return this;
    }
    public Context exportName(String name, IDatatype value) {
        return export(NSManager.STL+name, value);
    }

    public Context set(String name, String str) {
        table.put(name, DatatypeMap.newInstance(str));
        return this;
    }
    
     public Context set(String name, int n) {
        table.put(name, DatatypeMap.newInstance(n));
        return this;
    }
     
     public Context set(String name, boolean n) {
        table.put(name, DatatypeMap.newInstance(n));
        return this;
    }

    public Context setURI(String name, String str) {
        table.put(name, DatatypeMap.newResource(str));
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
    
    public boolean hasValue(String name, String value) {
        IDatatype dt = table.get(name);
        return dt != null && dt.getLabel().equals(value);
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
    public int pointerType(){
        return Pointerable.CONTEXT_POINTER;
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
  
}
