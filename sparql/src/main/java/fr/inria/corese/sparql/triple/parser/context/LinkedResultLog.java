package fr.inria.corese.sparql.triple.parser.context;

import fr.inria.corese.sparql.triple.parser.URLParam;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Store Linked Result URLs about federated query processing
 * JSON object exchanged between server and client as Linked Result
 * of Query Result.
 * 
 */
public class LinkedResultLog extends JSONObject implements URLParam {
    
    public void setLink(String key, String value) {
        put(key, value);
    }
    
    public void setLink(String key, JSONObject obj) {
        put(key, obj);
    }
    
    public void addLink(String name, JSONObject obj) {
        JSONArray arr = null; 
        if (has(name)) {
            arr = getJSONArray(name);
        }
        else {
            arr = new JSONArray();
            put(name, arr);
        }
        arr.put(obj);
    }
    
    public JSONObject create(String query) {
        JSONObject obj = new JSONObject();
        obj.put(QUERY, query);       
        return obj;
    }
    
    public JSONObject create(String query, String result) {
        JSONObject obj = new JSONObject();
        obj.put(QUERY, query);    
        if (result != null) {
            obj.put(RESULT, result);            
        }
        return obj;
    }
    
}
