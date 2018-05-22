package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Metadata extends ASTObject 
    implements Iterable<String> {
    static final String NL = System.getProperty("line.separator");
    static final int UNDEFINED  = -1;
    
    public static final int TEST   = 0;
    public static final int DEBUG  = 1;
    public static final int TRACE  = 2;
    public static final int PUBLIC = 3;
    public static final int IMPORT = 4;
    
    // Query
    public static final int RELAX   = 11;
    public static final int MORE    = 12;
    public static final int SERVICE = 13;
    public static final int DISPLAY = 14;
    public static final int BIND    = 15;
    public static final int TYPE    = 16;
    public static final int COMPILE = 17;
    public static final int SKIP    = 18;
    public static final int PATH    = 19;
    public static final int ENCODING= 20;
    public static final int DB      = 21;
    public static final int DB_FACTORY = 22;
    public static final int BGP     = 23;
    public static final int BOUNCE  = 24;
    public static final int SPARQL10  = 25;
    
    public static final int EVENT   = 50;
    public static final int BEFORE  = 51;
    public static final int AFTER   = 52;
    public static final int PRODUCE = 53;
    public static final int RESULT  = 54;
    public static final int STATEMENT= 55;

    // uncertainty triple metadata
    public static final int METADATA  = 50;
    public static final int VISITOR   = 51;
    public static final int LOCAL     = 52;
      
    static final String PREF = NSManager.KGRAM;
    public static final String DISPLAY_TURTLE   = PREF + "turtle";
    public static final String DISPLAY_JSON_LD  = PREF + "jsonld";
    public static final String DISPLAY_RDF_XML  = PREF + "rdfxml";
    
    public static final String DISPLAY_JSON     = PREF + "json";
    public static final String DISPLAY_XML      = PREF + "xml";
    public static final String DISPLAY_RDF      = PREF + "rdf";
    
    public static final String RELAX_URI        = PREF + "uri";
    public static final String RELAX_PROPERTY   = PREF + "property";
    public static final String RELAX_LITERAL    = PREF + "literal";
    
    public static final String PROBE            = PREF + "probe";
    public static final String VERBOSE          = PREF + "verbose";
    public static final String SELECT           = PREF + "select";
    public static final String GROUP            = PREF + "group";
    public static final String SIMPLIFY         = PREF + "simplify";
    public static final String EXIST            = PREF + "exist";
    public static final String SKIP_STR         = PREF + "skip";
    
    public static final String DISTRIBUTE_NAMED     = PREF + "distributeNamed";
    public static final String DISTRIBUTE_DEFAULT   = PREF + "distributeDefault";
    public static final String REWRITE_NAMED        = PREF + "rewriteNamed";
    
    public static final String META_BEFORE   = "@before";
    public static final String META_AFTER    = "@after";
    public static final String META_PRODUCE  = "@produce";
    public static final String META_RESULT   = "@result";
    public static final String META_STATEMENT= "@statement";
    public static final String META_CANDIDATE= "@candidate";
        
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
        define("@local",    LOCAL);      
        define("@bounce",   BOUNCE);      
        define("@sparqlzero", SPARQL10);      
        define("@encoding", ENCODING);      
        define("@bind",     BIND);      
        define("@import",   IMPORT);      
        define("@display",  DISPLAY);      
        define("@type",     TYPE);      
        define("@compile",  COMPILE);      
        define("@path",     PATH);      
        define("@skip",     SKIP);      
        define("@db",       DB);      
        define("@dbfactory",DB_FACTORY);      
        define("@bgp",      BGP); 
        define("@metadata", METADATA);      
        define("@visitor",  VISITOR); 
        
        define("@event",    EVENT);  
        define(META_BEFORE, BEFORE);  
        define(META_AFTER,  AFTER);  
        define(META_PRODUCE,PRODUCE);  
        define(META_RESULT, RESULT);         
        define(META_STATEMENT, STATEMENT);  
    }
    
    static void define(String str, int type){
        annotation.put(str, type);
        back.put(type, str);
    }
    
    public Metadata(){
        map   = new HashMap<String, String>();
        value = new HashMap();               
    }
    
    @Override
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
    
    public Metadata add(int type){
        String name = name(type);
        if (name != null){
            add(name);
        }
        return this;
    }
    
    public Metadata add(int type, String value){
        String name = name(type);
        if (name != null){
            add(name, value);
        }
        return  this;
    }
    
    public void add(String name, String val){
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
        String str = name(type);
        if (str == null){
            return false;
        }
        return hasMetadata(str);
    }
    
    public boolean hasMetadata(String name){
       return map.containsKey(name); 
    }
    
    // add without overloading local
    public void add(Metadata m){
        for (String name : m){
            if (! hasMetadata(name)){
                add(name);
                if (m.getValues(name) != null){
                    value.put(name, m.getValues(name));
                }
            }
        }
    }
       
    public HashMap<String, String> getMap(){
        return map;
    }
       
    public String getValue(int type){
        return getValue(name(type)); 
    }
    
    public String getStringValue(int type){
        String value = getValue(type);
        if (value == null){
            return null;
        }
        return NSManager.nstrip(value);
    }
    
    public boolean hasValue(int meta, String value){
        String str = getValue(meta);
        return str != null && str.equals(value);
    }
    
     public boolean hasValues(int meta, String value) {
        List<String> list = getValues(meta);
        if (list == null) {
            return false;
        }
        for (String str : list){
            if (str.equals(value)){
                return true;
            }
        }
        return false;
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
          return getValues(name(type));
      }
     
      public List<String> getValues(String name){
         if (name == null){
             return null;
         }
         return  value.get(name);
    }
    
    @Override
    public Iterator<String> iterator(){
            return map.keySet().iterator();
    }
    
    public Collection<String> getMetadataList(){
            return map.keySet();
    }
    
    public int type(String name){
        Integer i = annotation.get(name);
        if (i == null){
            i = UNDEFINED;
        }
        return i;
    }
    
    String name(int type){
        return  back.get(type);       
    }
    
    
     @Override
    public int pointerType() {
        return METADATA_POINTER;
    } 
 
   
    @Override
    public IDatatype getList(){
         ArrayList<IDatatype> list = new ArrayList<IDatatype>();
         for (String key : map.keySet()){
             IDatatype name = DatatypeMap.newLiteral(key);
             list.add(name);
         }
         return DatatypeMap.createList(list);
    }
    
    
    @Override
    public String getDatatypeLabel() {
       return String.format("[Metadata: size=%s]", size());
    }
    
  

}
