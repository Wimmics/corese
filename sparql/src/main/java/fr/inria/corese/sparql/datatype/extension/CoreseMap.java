package fr.inria.corese.sparql.datatype.extension;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.FALSE;
import static fr.inria.corese.sparql.datatype.CoreseBoolean.TRUE;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.HashMapList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONObject;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2018
 *
 */
public class CoreseMap extends CoreseExtension {

    private static final IDatatype dt = getGenericDatatype(IDatatype.MAP_DATATYPE);
    private static int count = 0;
    private static final String SEED = "_m_";  
    TreeDatatype map;
    

    public CoreseMap() {
        super(SEED + count++);
        map = new TreeDatatype();
    }
    
    @Override
    public IDatatype getDatatype() {
        return dt;
    }
      
    @Override
    public String getContent() {
        return String.format("[Map: size=%s]", map.size());
    }
    
    @Override
    public boolean isExtension() {
        return true;
    }
       
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isMap() {
        return true;
    }
    
    @Override
    public Map getNodeObject() {
        return map;
    }
    
    @Override
    public Map<IDatatype, IDatatype> getMap() {
        return map;
    }
    
    public void incr(IDatatype key) {
        IDatatype num = get(key);
        if (num == null) {
            num = DatatypeMap.newInstance(0);
        }
        set(key, num.intValue() + 1);
    }
    
    public void set(IDatatype key, int val) {
        set(key, DatatypeMap.newInstance(val));
    }

    /** 
     * @todo 
     * check IDatatype set(name,value) use newInstance instead of newResource.
     * */
    @Override
    public IDatatype set(String key, IDatatype val) {
        return set(DatatypeMap.newResource(key), val);     
    }
    
    @Override
    public IDatatype set(IDatatype key, IDatatype value) {
        map.put(key, value);
        return value;
    }
    
    @Override
    public IDatatype member(IDatatype key) {
        return  map.containsKey(key) ? TRUE : FALSE;
    }
    
    @Override
    public IDatatype has(IDatatype key) {
        return map.containsKey(key) ? TRUE : FALSE;
    }
    
    @Override
    public IDatatype get(IDatatype key) {
        return map.get(key);
    }
    
    public IDatatype keys() {
        return DatatypeMap.createList(map.keySet());
    }   
    
    public IDatatype values() {
        return DatatypeMap.createList(map.values());
    } 
    
    @Override
    public Iterator<IDatatype> iterator() {
        return getValueList().iterator();
    }
    
    @Override
    public List<IDatatype> getValueList() { 
        ArrayList<IDatatype> list = new ArrayList<>();
        for (IDatatype key : map.keySet()) {
            IDatatype pair = DatatypeMap.newList(key, map.get(key));
            list.add(pair);
        }
        return list;
    } 
    
    @Override
    public boolean isLoop() {
        return true;
    }
    
    public JSONObject toJSON() {
        return jsonCast().getNodeObject();
    }
    
    public CoreseJSON jsonCast() {
        CoreseJSON dt = new CoreseJSON(new JSONObject());

        for (IDatatype key : map.keySet()) {
            dt.set(key, map.get(key));
        }

        return dt;
    }
    
    public IDatatype toJSONDatatype() {
        return jsonCast();
    }
    
    @Override
    public Iterable<IDatatype> getLoop() {
        return getValueList();
    }
    
    class TreeDatatype extends TreeMap<IDatatype, IDatatype> {

        TreeDatatype() {
            super((IDatatype t, IDatatype t1) -> t.mapCompareTo(t1));
        }
    }

    public static IDatatype cast(HashMapList<String> map) {
        CoreseMap dt = new CoreseMap();
        for (String key : map.keySet()) {
            dt.set(DatatypeMap.newInstance(key), DatatypeMap.newStringList(map.get(key)));
        }
        return dt;
    }
    
}
