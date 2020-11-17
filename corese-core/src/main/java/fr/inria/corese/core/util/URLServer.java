package fr.inria.corese.core.util;

/**
 *
 * @author corby
 */
public class URLServer {
    
    String url;
    
    public URLServer(String s) {
        url = s;
    }
    
    public String getServer() {
        int index = url.indexOf("?");
        if (index == -1) {
            return url;
        }
        return url.substring(0, index);
    }
    
    public String getParameter() {
        int index = url.indexOf("?");
        if (index == -1) {
            return null;
        }
        return url.substring(index+1);
    }
    
    public String getParameter(String name) {
        String param = getParameter();
        if (param == null) {
            return null;
        }
        String[] params = param.split("&");
        for (String str : params) {
            String[] keyval = str.split("=");
            if (keyval[0].equals(name)) {
                return keyval[1];
            }
        }
        return null;
    }
    
    public boolean hasMethod() {
        return getParameter("method") != null;
    }
    
    public boolean isGET() {
        String method = getParameter("method");
        return method != null && method.equals("get");
    }
    
}
