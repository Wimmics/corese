package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public JSONObject getObject() {
        return json;
    }
    
    @Override
    public String getContent() {
        return json.toString();
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
    
    void set(String key, IDatatype value) {
        switch (value.getCode()) {
            case INTEGER: json.put(key, value.intValue()); break;
            case LONG:    json.put(key, value.longValue()); break;
            case DOUBLE:  json.put(key, value.doubleValue()); break;
            case FLOAT:   json.put(key, value.floatValue()); break;
            case DECIMAL: json.put(key, value.doubleValue()); break;
            case BOOLEAN: json.put(key, value.booleanValue()); break;
            
            case UNDEF:
                switch (value.getDatatypeURI()) {
                    case LIST_DATATYPE: json.put(key, toJSONArray(value)); break;
                    case JSON_DATATYPE: json.put(key, (JSONObject) value.getObject()); break;
                }
                break;
            
            default: json.put(key, value.stringValue()); break;
        }
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
                    arr.put(i, value.doubleValue());
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
                            arr.put(i, (JSONObject) value.getObject());
                            break;
                    }

                default:
                    arr.put(i, value.stringValue());
                    break;
            }
            i++;
        }
        return arr;
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
            if (str.startsWith("http://") || str.startsWith("https://")) {
                return DatatypeMap.newResource(str);
            }
        }
        return DatatypeMap.castObject(obj);
    }
    
    IDatatype cast(JSONArray ar) {
        ArrayList<IDatatype> list = new ArrayList<>();
        for (int i=0; i<ar.length(); i++) {
            list.add(cast(ar.get(i)));
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
    
}
