package fr.inria.corese.sparql.triple.parser.context;

import fr.inria.corese.sparql.api.IDatatype;
import static fr.inria.corese.sparql.datatype.DatatypeMap.createObject;
import static fr.inria.corese.sparql.datatype.DatatypeMap.newInstance;
import static fr.inria.corese.sparql.datatype.DatatypeMap.newResource;
import fr.inria.corese.sparql.triple.cst.LogKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * subject -> property map
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2021
 */
public class SubjectMap extends HashMap<String, PropertyMap> implements LogKey {

    PropertyMap getCreate(String sub) {
        PropertyMap pmap = get(sub);
        if (pmap == null) {
            pmap = new PropertyMap();
            put(sub, pmap);
        }
        return pmap;
    }

    public List<String> getKeys() {
        return sort(keySet());
    }

    public StringBuilder display() {       
        return display(new StringBuilder());
    }
        
    public StringBuilder display(StringBuilder sb) {
        for (String sub : getKeys()) {
            sb.append(String.format("%s\n", pretty(sub)));
            get(sub).display(sb);
            sb.append("\n");
        }       
        return sb;
    }
    
    
    public PropertyMap getPropertyMap(String subject) {
        return getCreate(subject);
    }

    public IDatatype get(String subject, String property) {
        return getPropertyMap(subject).get(property);
    }

    public void incr(String subject, String property, int value) {
        IDatatype dt = get(subject, property);
        set(subject, property, ((dt == null) ? 0 : dt.intValue()) + value);
    }

    public void set(String subject, String property, String value) {
        getPropertyMap(subject).put(property, value(value));
    }

    public void set(String subject, String property, int value) {
        getPropertyMap(subject).put(property, newInstance(value));
    }
    
    public void set(String subject, String property, double value) {
        getPropertyMap(subject).put(property, newInstance(value));
    }
    
    public void set(String subject, String property, IDatatype dt) {
        getPropertyMap(subject).put(property, dt);
    }

    public void set(String subject, String property, Object value) {
        getPropertyMap(subject).put(property, createObject(value));
    }

    // add value to list of value
    public void add(String subject, String property, String value) {
        IDatatype dt = getPropertyMap(subject).getCreateList(property);
        dt.getList().add(value(value));
    }

    public void add(String subject, String property, Object value) {
        IDatatype dt = getPropertyMap(subject).getCreateList(property);
        dt.getList().add(createObject(value));
    }

    
    IDatatype value(String str) {
        if (str == null) {
            return null;
        }
        if (str.startsWith("http://") || str.startsWith("https://")) {
            return newResource(str);
        }
        return newInstance(str);
    }

    void addDistinct(IDatatype dt, IDatatype value) {
        if (!dt.getList().contains(value)) {
            dt.getList().add(value);
        }
    }

 
    
    
    String pretty(String key) {
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return String.format("<%s>", key);
        }
        else {
            // ns: or bnode
            return key;
        }
    }
    
    ArrayList<String> sort(Collection<String> set) {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(set);
        Collections.sort(list);
        return list;
    }
}
