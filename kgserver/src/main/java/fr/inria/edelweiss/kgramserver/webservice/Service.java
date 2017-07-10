package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.triple.parser.Context;
import fr.inria.acacia.corese.triple.parser.NSManager;
import java.util.ArrayList;
import java.util.List;


/**
 * Specification of a Profile or a Server 
 * Profile specifies st:workflow (or directly a transformation)
 * Server specifies a triplestore with a name, possibly with a st:content Workflow,
 * and possibly with a st:workflow; In the latter case, it is also considered as a profile
 * and a profile object is also created with the same URI as the server.
 * Server with name "test" is triggered with URL http://corese.inria.fr/srv/tutorial/test
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Service {
    static final String PDATA   = NSManager.STL + "data";
    static final String PSCHEMA = NSManager.STL + "schema";
    static final String PCONTEXT= NSManager.STL + "context";  
    
    // uri of this Service/Profile definition
    // subject of the description
    private String name;
    // name of the service if any (e.g. cdn for template/cdn)
    // value of st:service property
    private String service;
    private String query;
    private String transform;
    private String variable;
    private String server;
    private String lang;
    
    private Context ctx;
    
    private String[] load;
    private List<Doc> data;
    private List<Doc> schema;
    private List<Doc> context;
    

    /**
     * @return the data
     */
    public List<Doc> getData() {
        if (data == null) {
            data = new ArrayList();
        }
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<Doc> data) {
        this.data = data;
    }

    /**
     * @return the schema
     */
    public List<Doc> getSchema() {
        if (schema == null) {
            schema = new ArrayList();
        }
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(List<Doc> schema) {
        this.schema = schema;
    }

    /**
     * @return the queries
     */
    public List<Doc> getContext() {
        if (context == null) {
            context = new ArrayList();
        }
        return context;
    }

    /**
     * @param queries the queries to set
     */
    public void setContext(List<Doc> queries) {
        this.context = queries;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    boolean isRDFSEntailment() {
        return !  isOWLEntailment() && ! getSchema().isEmpty();
    }
    
    boolean isOWLEntailment(){
        if (getSchema().isEmpty()){
            return false;
        }
        for (Doc d : getSchema()){
            if (d.getUri().endsWith(".owl")){
                return true;
            }
        }
        return false;
    }

    /**
     * @return the server
     */
    public String getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * @param lang the lang to set
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * @return the service
     */
    public String getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(String service) {
        this.service = service;
    }
   
    /**
     * @return the ctx
     */
    public Context getParam() {
        return ctx;
    }

    /**
     * @param ctx the ctx to set
     */
    public void setParam(Context ctx) {
        this.ctx = ctx;
    }

    class Doc {

        private String uri;
        private String name;

        Doc(String u, String n) {
            uri = u;
            name = n;
        }
             
        /**
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * @param uri the uri to set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        
        boolean similar(Doc d){
            if (! getUri().equals(d.getUri())){
                return false;
            }
            if (getName() == null || d.getName() == null){
                return getName() == d.getName();
            }
            return  getName().equals(d.getName());
        }
    }

    Service(String name) {
        this.name = name;
    }

    Service(String t, String q, String v) {
        query = q;
        transform = t;
        variable = v;
    }

    Service(String[] l) {
        load = l;
    }

    @Override
    public String toString() {
        String s = "";
        s += "name: " + name + "\n";
        s += "transform: " + transform + "\n";
        s += "variable: " + variable + "\n";
        s += "query: " + query + "\n";
        s += "data: ";
        for (Doc d : getData()) {
            s += d.getUri() + " ";
        }
        s += "\n";
        s += "schema: ";
        for (Doc d : getSchema()) {
            s += d.getUri() + " ";
        }
        s += "\n";
        s += "queries: ";
        for (Doc d : getContext()) {
            s += d.getUri() + " ";
        }
        s += "\n";
        return s;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the transform
     */
    public String getTransform() {
        return transform;
    }

    /**
     * @param transform the transform to set
     */
    public void setTransform(String transform) {
        this.transform = transform;
    }

    /**
     * @return the variable
     */
    public String getVariable() {
        return variable;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * @return the load
     */
    public String[] getLoad() {
        return load;
    }

    /**
     * @param load the load to set
     */
    public void setLoad(String[] load) {
        this.load = load;
    }

    // prop = st:data st:schema st:queries
    // uri of doc to load
    // name of graph 
    void add(String prop, String uri, String name) {
        if (name == null){
            name = uri;
        }
        Doc d = new Doc(uri, name);
        if (prop.equals(PDATA)) {
            add(getData(),d);
        } else if (prop.equals(PSCHEMA)) {
            add(getSchema(), d);
        } else if (prop.equals(PCONTEXT)) {
            add(getContext(), d);
        }
    }
    
    void add(List<Doc> list, Doc doc){
        for (Doc d : list){
            if (d.similar(doc)){
                return;
            }
        }
        list.add(doc);
    }
}
