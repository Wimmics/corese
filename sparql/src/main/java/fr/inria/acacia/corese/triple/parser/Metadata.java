package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Metadata implements Iterable<String> {
    static final String NL = System.getProperty("line.separator");
    static final int UNDEFINED  = -1;
    
    static final int TEST   = 0;
    static final int DEBUG  = 1;
    static final int TRACE  = 2;
    static final int PUBLIC = 3;
    public static final int IMPORT = 4;
    
    // Query
    public static final int RELAX   = 11;
    static final int MORE    = 12;
    public static final int SERVICE = 13;
    public static final int DISPLAY = 14;
    
    static final String PREF = NSManager.KGRAM;
    public static final String DISPLAY_TURTLE   = PREF + "turtle";
    public static final String DISPLAY_JSON_LD  = PREF + "jsonld";
    public static final String DISPLAY_RDF_XML  = PREF + "rdfxml";
    
    public static final String DISPLAY_JSON     = PREF + "json";
    public static final String DISPLAY_XML      = PREF + "xml";
    public static final String DISPLAY_RDF      = PREF + "rdf";
    
    private static HashMap<String, Integer> annotation;    
    private static HashMap<Integer, String> back; 
    
    HashMap<String, String> map;
    HashMap<String, List<String>> value;
    
    
     static {
        initAnnotate();
    }
    
    static void initAnnotate(){
        annotation = new HashMap();
        back = new HashMap();
        define("@debug",    DEBUG);
        define("@trace",    TRACE);
        define("@test",     TEST);
        define("@export",   PUBLIC);      
        define("@public",   PUBLIC);      
        define("@more",     MORE);      
        define("@relax",    RELAX);      
        define("@service",  SERVICE);      
        define("@import",   IMPORT);      
        define("@display",  DISPLAY);      
    }
    
    static void define(String str, int type){
        annotation.put(str, type);
        back.put(type, str);
    }
    
    public Metadata(){
        map   = new HashMap<String, String>();
        value = new HashMap();               
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Metadata:");
        sb.append(NL);
        for (String m : this){
            sb.append(m);
            List<String> list = getValues(m);
            if (list != null && ! list.isEmpty()){
                sb.append(" : ");
                sb.append(getValues(m));
            }
            sb.append(NL);
        }
        return sb.toString();
    }
       
    public void add(String str){
        map.put(str, str);
    }
    
    public void add(String name, String val){
       //map.put(name, val);
       add(name);
       List<String> list = value.get(name);
       if (list == null){
           list = new ArrayList<String>();
           value.put(name, list);
       }
       if (! list.contains(val)){
           list.add(val);
       }
    }
    
    public void add(String name, Constant val){
        add(name, val.getLongName());
    }
    
    public boolean hasMetadata(int type){
        String str = back.get(type);
        if (str == null){
            return false;
        }
        return hasMetadata(str);
    }
    
    public boolean hasMetadata(String name){
       return map.containsKey(name); 
    }
    
    // add without overloading local
    void add(Metadata m){
        for (String name : m){
            if (! hasMetadata(name)){
                //add(name, m.getMap().get(name));
                add(name);
                if (m.value.containsKey(name)){
                    value.put(name, m.getValues(name));
                }
            }
        }
    }
       
    public HashMap<String, String> getMap(){
        return map;
    }
    
//    public String get(String name){
//        return map.get(name);
//    }
    
    public String getValue(int type){
        return getValue(back.get(type)); 
    }
    
     public String getValue(String name){
         if (name == null){
             return null;
         }
        List<String> list = getValues(name);
        if (list == null || list.isEmpty()){
            return null;
        }
        return list.get(0);
    }
     
      public List<String> getValues(int type){
          return getValues(back.get(type));
      }
     
      public List<String> getValues(String name){
         if (name == null){
             return null;
         }
         List<String> val = value.get(name);
         if (val == null){
             return null;
         }
         return val;
    }
    
    public Iterator<String> iterator(){
            return map.keySet().iterator();
    }
    
    int type(String a){
        Integer i = annotation.get(a);
        if (i == null){
            i = UNDEFINED;
        }
        return i;
    }

}
