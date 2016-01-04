package fr.inria.acacia.corese.triple.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    static final int RELAX   = 11;
    static final int MORE    = 12;
    static final int SERVICE = 13;
    
    private static HashMap<String, Integer> annotation;    
    private static HashMap<Integer, String> back; 
    
    HashMap<String, String> map;
    
    
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
    }
    
    static void define(String str, int type){
        annotation.put(str, type);
        back.put(type, str);
    }
    
    public Metadata(){
        map = new HashMap<String, String>();
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Metadata:");
        sb.append(NL);
        for (String m : this){
            sb.append(m);
            sb.append(" : ");
            sb.append(get(m));
            sb.append(NL);
        }
        return sb.toString();
    }
    
    public void add(String str){
        map.put(str, str);
    }
    
    public void add(String name, String str){
       map.put(name, str);
    }
    
    public boolean hasMetadata(int type){
        String str = back.get(type);
        if (str == null){
            return false;
        }
        return map.containsKey(str);
    }
    
    // add without overloading local
    void add(Metadata m){
        for (String name : m.getMap().keySet()){
            if (get(name) == null){
                add(name, m.getMap().get(name));
            }
        }
    }
       
    public HashMap<String, String> getMap(){
        return map;
    }
    
    public String get(String name){
        return map.get(name);
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
