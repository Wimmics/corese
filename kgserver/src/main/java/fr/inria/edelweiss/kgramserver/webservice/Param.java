
package fr.inria.edelweiss.kgramserver.webservice;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.triple.parser.Context;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Param {
    private String service;
    private String server;
    private String profile;    
    private String transform;
    private String uri;
    private String name;
    private String query;
    private String value;
    private String load;
    
    private IDatatype title;
    private List<String> from;
    private List<String>  named;
    private boolean protect = false;
    private boolean isUserQuery = false;
    private Context context;
    
    Param(String s, String p, String t, String u, String n, String q){
        service = s;
        profile = p;
        transform = t;
        uri = u;
        name = n;
        query = q;
    }
    
    public String toString(){
        String str = "";
        str += "profile: "   + getValue(profile) + "\n";
        str += "transform: " + getValue(transform) + "\n" ;
        return str;
    }
    
    String getValue(String str) {
        if (str == null) {
            return "";
        }
        return str;
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
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(String profile) {
        this.profile = profile;
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
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the from
     */
    public List<String> getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(List<String> from) {
        this.from = from;
    }

    /**
     * @return the named
     */
    public List<String> getNamed() {
        return named;
    }

    /**
     * @param named the named to set
     */
    public void setNamed(List<String> named) {
        this.named = named;
    }
    
    public void setDataset(List<String> from, List<String> named){
        setFrom(from);
        setNamed(named);
    }

    void setProtect(boolean b) {
        protect = true;
    }

    boolean isProtect() {
        return protect;
    }

    /**
     * @return the load
     */
    public String getLoad() {
        return load;
    }

    /**
     * @param load the load to set
     */
    public void setLoad(String load) {
        this.load = load;
    }

    /**
     * @return the isUserQuery
     */
    public boolean isUserQuery() {
        return isUserQuery;
    }

    /**
     * @param isUserQuery the isUserQuery to set
     */
    public void setUserQuery(boolean isUserQuery) {
        this.isUserQuery = isUserQuery;
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
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

}
