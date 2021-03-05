package fr.inria.corese.core.util;

import fr.inria.corese.kgram.api.core.Node;
import java.util.List;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author corby
 */
public class URLServer {
    
    // whole URL: http://corese.inria.fr/sparql?param=value
    private String url;
    // server part of URL: http://corese.inria.fr/sparql
    private String server;
    // param=value&...
    private String param;
    // map of param = value
    private MultivaluedMap<String, String> map;
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
            setMap(map(getParam()));
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
    
    /**
     * MultivaluedMap because some parameters may appear several times
     */
    MultivaluedMap<String, String> map(String param) {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
        String[] params = param.split("&");
        for (String str : params) {
            System.out.println("URL param: " + str);
            String[] keyval = str.split("=");
            if (keyval.length>=2)   {
                map.add(keyval[0], keyval[1]);
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
    public MultivaluedMap<String, String> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(MultivaluedMap<String, String> map) {
        this.map = map;
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
