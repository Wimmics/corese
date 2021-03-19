package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.Node;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author corby
 */
public class URLServer {
    
    public static final String MODE = "mode";
    public static final String PROV = "provenance";
    
    // whole URL: http://corese.inria.fr/sparql?param=value
    private String url;
    // server part of URL: http://corese.inria.fr/sparql
    private String server;
    // param=value&...
    private String param;
    private HashMapList<String> amap;
    // service Node for service clause
    private Node node;
    
    
    
    public URLServer(String s) {
        url = s;
        init();
    }
    
    // service clause
    public URLServer(Node node) {
        this(node.getLabel());
        setNode(node);
    }
    
    @Override
    public String toString() {
        return getURL();
    }
    
    void init() {
        setServer(server(getURL()));
        setParam(parameter(getURL()));
        if (getParam()!=null) {
            setMap(hashmap(getParam()));
        }
    }
    
    
    String server(String url) {
        int index = url.indexOf("?");
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }
    

    
    String parameter(String url) {
//        try {
//            URLDecoder.decode(url, StandardCharsets.UTF_8.toString());
//        } catch (UnsupportedEncodingException ex) {
        //}
        int index = url.indexOf("?");
        if (index == -1) {
            return null;
        }
        return url.substring(index+1);
    }
    
    public String getParameter(String name) {
        if (getMap() == null) {
            return null;
        }
        return getMap().getFirst(name);
    }
    
    public boolean hasParameter() {
        return getMap() != null && !getMap().isEmpty();
    }
    
    public boolean hasParameter(String name, String value) {
        List<String> list = getParameterList(name);
        if (list == null) {
            return false;
        }
        return list.contains(value);
    }
    
    public List<String> getParameterList(String name) {
        if (getMap() == null) {
            return null;
        }
        return getMap().get(name);
    }
    
    public int intValue(String name) {
        if (getMap() == null) {
            return -1;
        }
        String value = getMap().getFirst(name);
        if (value == null) {
            return -1;
        }
        try { return Integer.valueOf(value); }
        catch (Exception ex) { return -1;}
    }
    
    
    HashMapList hashmap(String param) {
        HashMapList<String> map = new HashMapList();
        String[] params = param.split("&");
        for (String str : params) {
            //System.out.println("URL param: " + str);
            String[] keyval = str.split("=");
            if (keyval.length>=2)   {
                String key = keyval[0];
                String val = keyval[1];
                List<String> list = map.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(key, list);
                }
                if (! list.contains(val)) {
                    list.add(val);
                }
            }
        }
        return map;
    }
    
    public boolean hasMethod() {
        return getParameter("method") != null;
    }
    
    public boolean isGET() {
        String method = getParameter("method");
        return method != null && method.equals("get");
    }

    /**
     * @return the param
     */
    public String getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(String param) {
        this.param = param;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String s) {
        server = s;
    }

    /**
     * @return the map
     */
    public HashMapList<String> getMap() {
        return amap;
    }

    /**
     * @param map the map to set
     */
    public void setMap(HashMapList<String> map) {
        this.amap = map;
    }

    /**
     * @return the url
     */
    public String getURL() {
        return url;
    }
    
    /**
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(Node node) {
        this.node = node;
    }
    
}
