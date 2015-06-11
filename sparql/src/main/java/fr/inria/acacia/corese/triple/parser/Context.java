/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.triple.parser;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import java.util.HashMap;

/**
 * Execution Context for SPARQL Query and Template
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2014
 *
 */
public class Context {

    public static final String NL = System.getProperty("line.separator");
    public static final String STL = NSManager.STL;
    public static final String STL_QUERY = STL + "query";
    public static final String STL_NAME = STL + "name"; // query path name
    public static final String STL_SERVICE = STL + "service";
    public static final String STL_SERVER = STL + "server";
    public static final String STL_PROFILE = STL + "profile";
    public static final String STL_TRANSFORM = STL + "transform";
    public static final String STL_URI = STL + "uri";
    public static final String STL_PROTOCOL = STL + "protocol";
    public static final String STL_AJAX = STL + "ajax";
    public static final String STL_CONTEXT = STL + "context";
    public static final String STL_DATASET = STL + "dataset";
    public static final String STL_LANG = STL + "lang";
    public static final String STL_PARAM = STL + "param";
    HashMap<String, IDatatype> table;

    public Context() {
        table = new HashMap();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : table.keySet()) {
            sb.append(key);
            sb.append(" : ");
            sb.append(table.get(key));
            sb.append(NL);
        }
        return sb.toString();
    }

    public Context set(String name, IDatatype value) {
        table.put(name, value);
        return this;
    }

    public Context set(String name, String str) {
        table.put(name, DatatypeMap.newInstance(str));
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

    public String getURI() {
        return stringValue(STL_URI);
    }
    
    public Context setProtocol(String str){
        return setURI(STL_PROTOCOL, str);
    }

    public Context setQuery(String str) {
        return set(STL_QUERY, str);
    }

    // add values clause to query
    public Context addValue(String value) {
        String squery = getQuery();
        if (getURI() == null && squery != null) {
            setQuery(squery + value);
        }
        return this;
    }

    public String getQuery() {
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

    public Context setParam(String str) {
        return setURI(STL_PARAM, str);
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

    public String stringValue(String name) {
        IDatatype dt = table.get(name);
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }
}
