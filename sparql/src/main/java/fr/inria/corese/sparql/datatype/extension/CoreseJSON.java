package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.api.IDatatypeList;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.FALSE;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.TRUE;
import fr.inria.corese.sparql.datatype.CoreseDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.NSManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;
import org.json.JSONPointerException;

/**
 *
 * @author corby
 */
public class CoreseJSON extends CoreseExtension {
    
    private static final IDatatype dt = getGenericDatatype(IDatatype.JSON_DATATYPE);
    private static int count = 0;
    private static final String SEED = "_j_"; 
    
    JSONObject json;
    
    
    public CoreseJSON() {
        super(SEED + count++);       
    }
    
    public CoreseJSON(JSONObject json) {
        this();
        this.json = json;
    }
    
    public CoreseJSON(String str) {
        this(new JSONObject(str));        
    }
    
    @Override
    public boolean isJSON() {
        return true;
    }
    
    @Override
    public IDatatype getDatatype() {
        return dt;
    }
    
    @Override
    public JSONObject getNodeObject() {
        return json;
    }
    
    @Override
    public String getContent() {
        return json.toString();
    }
    
    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        for (IDatatype list : this) {
            sb.append(list.get(0)).append(" = ").append(list.get(1)).append(NL);
        }
        return sb.toString();
    }
    
    @Override
    public int size() {
        return json.length();
    }
    
    @Override
    public IDatatype has(IDatatype key) {
        return json.has(key.getLabel()) ? TRUE : FALSE;
    }
    
    @Override
    public IDatatype get(IDatatype key) {
        try {
            Object val = json.get(key.getLabel());
            if (val == null) {
                return null;
            }
            return cast(val);
        }
        catch (JSONException e) {
            return null;
        }
    }
        
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        set(key.getLabel(), value);
        return value;
    }
    
    @Override
    public IDatatype set(String key, IDatatype value) {
        switch (value.getCode()) {
            case INTEGER: json.put(key, value.intValue()); break;
            case LONG:    json.put(key, value.longValue()); break;
            case DOUBLE:  json.put(key, value.doubleValue()); break;
            case FLOAT:   json.put(key, value.floatValue()); break;
            case DECIMAL: json.put(key, value.decimalValue()); break;
            case BOOLEAN: json.put(key, value.booleanValue()); break;
            
            case UNDEF: 
                switch (value.getDatatypeURI()) {
                    case LIST_DATATYPE: json.put(key, toJSONArray(value)); break;
                    case JSON_DATATYPE: json.put(key, (JSONObject) value.getNodeObject()); break;
                    case MAPPINGS_DATATYPE: 
                        json.put(key, value.getNodeObject()); 
                        break;
                }
                break;
            
            default: json.put(key, value.stringValue()); break;
        }
        return value;
    }
     
    /**
     */
    @Override
    public IDatatype path(IDatatype path) {
        if (path.getLabel().startsWith("/")) {
            return jsonPath(path);
        }
        else {
            return myPath(path);
        }
    }
    
    public IDatatype jsonPath(IDatatype path) {
        JSONPointer jp = new JSONPointer(path.getLabel());
        try {
            Object res = jp.queryFrom(getNodeObject());
            return cast(res);
        }
        catch (JSONPointerException e) {
            CoreseDatatype.logger.error(e.getMessage());
            return defaultPath();
        }
    }
       
    /**
     * Path that walk through json array and compute a list of results
     */    
    IDatatype myPath(IDatatype path) {
        if (! path.isList()) {
            path = DatatypeMap.split(path, "/");
        }
        return path(path, 0);
    }
        
    IDatatype defaultPath() {
        return DatatypeMap.newList();
    }
        
    @Override
    public IDatatype path(IDatatype path, int i) {
        IDatatype obj, res = null;
        
        if (i < path.size()) {
            IDatatype slot = path.get(i);
                       
            if (has(slot).booleanValue()) {
                obj = get(slot);
                res = path(obj, path, i+1);
            } else {
                res = defaultPath();
            }
        }
        else {
            // path is finished
            res = this;
        }
        
        if (res.isList()) {
            return res;
        }
        else {
            return DatatypeMap.newList(res);
        }
    }
    
    /**
     * path = (p1 (p2 p3)* p4)
     * exp  = (p2 p3)*
     */
    void star(IDatatype path, int i) {
        IDatatype exp = path.get(i);
        // 1) skip exp
        path(path, i+1);
        
        // 2) 
        // path' = (p1 exp exp* p4)
        
    }
    
    /**
     * obj is result of preceding path step
     */
    IDatatype path(IDatatype obj, IDatatype path, int i) {
        if (i >= path.size()) {
            // end of path 
            return obj;
        } else if (obj.isList()) {
            // compute path for each element of the list
            IDatatypeList list = DatatypeMap.newList();
            
            for (IDatatype elem : obj) {
                IDatatype res = path(elem, path, i);
                if (res.isList()) {
                    list = list.append(res);
                }
                else {
                    list.add(res);
                }
            }
            return list;
        } else if (obj.isJSON()) {
            return obj.path(path, i);
        } 
        // path continues but obj is not json
        return defaultPath();
    }

      
    JSONArray toJSONArray(IDatatype list) {
        JSONArray arr = new JSONArray();
        int i = 0;
        for (IDatatype value : list) {
            switch (value.getCode()) {
                case INTEGER:
                    arr.put(i, value.intValue());
                    break;
                case LONG:
                    arr.put(i, value.longValue());
                    break;
                case DOUBLE:
                    arr.put(i, value.doubleValue());
                    break;
                case FLOAT:
                    arr.put(i, value.floatValue());
                    break;
                case DECIMAL:
                    arr.put(i, value.decimalValue());
                    break;
                case BOOLEAN:
                    arr.put(i, value.booleanValue());
                    break;

                case UNDEF:
                    switch (value.getDatatypeURI()) {
                        case LIST_DATATYPE:
                            arr.put(i, toJSONArray(value));
                            break;
                        case JSON_DATATYPE:
                            arr.put(i, (JSONObject) value.getNodeObject());
                            break;
                        case MAPPINGS_DATATYPE:
                            arr.put(i,  value.getNodeObject());
                            break;    
                    }
                    break;

                default:
                    arr.put(i, value.stringValue());
                    break;
            }
            i++;
        }
        return arr;
    }
    
    @Override
    public IDatatype keys() {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (String key : json.keySet()) {
            list.add(DatatypeMap.newInstance(key));
        }
        return DatatypeMap.newList(list);
    }
    
    @Override
    public Iterator<IDatatype> iterator() {
        return getValueList().iterator();
    }
    
    @Override
    public List<IDatatype> getValueList() { 
        ArrayList<IDatatype> list = new ArrayList<>();
        for (String key : json.keySet()) {
            Object obj = json.get(key);
            IDatatype value = cast(obj);
            IDatatype pair = DatatypeMap.newList(DatatypeMap.newInstance(key), value);
            list.add(pair);
        }
        return list;
    } 
    
    
    IDatatype cast(Object obj) {
        if (obj instanceof JSONArray) {
            return cast((JSONArray) obj );
        } else if (obj instanceof JSONObject) {
            return new CoreseJSON((JSONObject) obj);
        } else if (obj instanceof String) {
            String str = (String) obj;
            if (isURI(str)) {
                return DatatypeMap.newResource(str);
            }
            else {
                return DatatypeMap.newInstance(str);
            }
        }
        return DatatypeMap.castObject(obj);
    }
    
    boolean isURI(String str) {
        try {
            URI uri = new URI(str);
            return uri.isAbsolute();
        } catch (URISyntaxException ex) {
            return false;
        }
    }
    
    IDatatype cast(JSONArray ar) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (Object obj : ar) {
            list.add(cast(obj));
        }
        return DatatypeMap.newList(list);
    }

    
    @Override
    public boolean isLoop() {
        return true;
    }
    
    @Override
    public Iterable<IDatatype> getLoop() {
        return getValueList();
    }
    
    @Override
    public IDatatype duplicate() {
        JSONObject myjson = new JSONObject();
        for (String key : json.keySet()) {
            Object obj = json.get(key);
            myjson.put(key, obj);
        }
        return new CoreseJSON(myjson);
    }
    
}
