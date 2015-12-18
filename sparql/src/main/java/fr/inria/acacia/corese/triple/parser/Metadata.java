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
    public static final String SERVICE = "@service";
 
    static final int UNDEFINED  = -1;
    static final int TEST   = 0;
    static final int DEBUG  = 1;
    static final int TRACE  = 2;
    static final int PUBLIC = 3;
    
    private static HashMap<String, Integer> annotation;    
    ArrayList<String> list;
    HashMap<String, String> map;
    
    
     static {
        initAnnotate();
    }
    
    static void initAnnotate(){
        annotation = new HashMap();
        annotation.put("@debug", DEBUG);
        annotation.put("@trace", TRACE);
        annotation.put("@test",  TEST);
        annotation.put("@export", PUBLIC);      
        annotation.put("@public", PUBLIC);      
    }
    
    public Metadata(){
        list = new ArrayList<String>();
        map = new HashMap<String, String>();
    }
    
    public void add(String str){
        list.add(str);
    }
    
    public void add(String name, String str){
       map.put(name, str);
    }
    
    // add without overloading local
    void add(Metadata m){
        for (String name : m.getMap().keySet()){
            if (get(name) == null){
                add(name, m.getMap().get(name));
            }
        }
    }
    
    public ArrayList<String> getList(){
        return list;
    }
    
    public HashMap<String, String> getMap(){
        return map;
    }
    
    public String get(String name){
        return map.get(name);
    }
    
    public Iterator<String> iterator(){
            return list.iterator();
    }
    
    int type(String a){
        Integer i = annotation.get(a);
        if (i == null){
            i = UNDEFINED;
        }
        return i;
    }

}
