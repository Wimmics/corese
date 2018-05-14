package fr.inria.corese.kgram.filter;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Hierarchy;
import java.util.HashMap;

/**
 * Manage extension functions 
 * Expr exp must have executed exp.local() to tag
 * local variables
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {

    static final String NL = System.getProperty("line.separator");
    public static final String TYPE = ExpType.TYPE_METADATA;
    //FunMap map;
    FunMap[] maps;
    private String name;
    private Object pack;
    
    public class FunMap extends HashMap<String, Expr> {}
    // datatype -> Extension for methods of the datatype
    HashMap<String, Extension> method;
    // Embedding extension in case of method
    private Extension extension;
    private Hierarchy hierarchy;
    private boolean debug = false;

    public Extension() {
        maps = new FunMap[11];
        for (int i=0; i<maps.length; i++){
            maps[i] = new FunMap();
        }
    }
    
    public Extension(String n){
        this();
        name = n;
    }
    
    FunMap getMap(Expr exp){
        return getMap(exp.arity());
    }
    
    FunMap getMap(int n){
         if (n >= maps.length){
            return null;
        }
        return maps[n];
    }
    
    public FunMap[] getMaps(){
        return maps;
    }

    /**
     *
     * exp: st:fac(?x) = if (?x = 1, 1, ?x * st:fac(?x - 1))
     */
    public void define(Expr exp) {
        if (exp.getMetadataValues(TYPE) != null){
            defineMethod(exp);
        }
        else {
            defineFunction(exp);
        }
    }
    
    void defineFunction(Expr exp) {       
        Expr fun = exp.getFunction(); 
        getMap(fun).put(fun.getLabel(), exp);
    }
    
    /**
     * Record function as method of datatype(s)
     */
    void defineMethod(Expr exp) {   
         for (String type : exp.getMetadataValues(TYPE)){
            getCreateMethodExtension(type).defineFunction(exp);
         }
    }
    
    /**
     * Return the Extension that records the methods of type    
     */
    Extension getCreateMethodExtension(String type){
        Extension ext = getMethod().get(type);
        if (ext == null){
            ext = new Extension();
            ext.setExtension(this);
            getMethod().put(type, ext);
        }
        return ext;
    }
    
    Extension getMethodExtension(String type){
        return getMethod().get(type);
    }
    
    public HashMap<String, Extension> getMethod(){
        if (method == null){
            method = new HashMap<>();
        }
        return method;
    }
    
    public boolean isMethod() {
        return getMethod().size() > 0;
    }
    
    public void add(Extension ext){
        for (FunMap m : ext.getMaps()){
            for (Expr e : m.values()){
                if (! isDefined(e.getFunction())){ 
                    define(e);
                }
            }
        }
    }
    
    /**
     * Use case: Transformation st:profile exports its functions to transformation
     * They are declared as public
     * Hence Interpreter isPublic() is OK.
     */
     public void setPublic(boolean b){
        for (FunMap m : getMaps()){
            for (Expr e : m.values()){
                e.setPublic(b);
            }
        }
    }

    public boolean isDefined(Expr exp) {
        return getMap(exp).containsKey(exp.getLabel());
    }

    /**
     * exp: st:fac(?n) values: #[10] actual values of parameters return body of
     * fun
     */
    public Expr get(Expr exp, Object[] values) {
        return getMap(exp).get(exp.getLabel());
    }

    public Expr get(Expr exp) {
        Expr def = getMap(exp).get(exp.getLabel());
        return def;
    }
    
    public Expr get(Expr exp, String name) {
        Expr def = getMap(exp).get(name);
        return def;
    }
    
    public Expr get(String label) {
        for (int i = 0; i<maps.length; i++){
            Expr exp = get(label, i);
            if (exp != null){
                return exp;
            }
        }
        return null;
    }
    
    public Expr get(String label, int n) {
        FunMap m = getMap(n);
        if (m == null){
            return null;
        }
        return m.get(label);
    }
    
    /**
     * Retrieve a method with label name and param[0] (data)type 
     * There are two possible Hierarchies: DatatypeHierarchy and ClassHierarchy
     * ClassHierarchy extends DatatypeHierarchy (for literals only)
     * By default, return  function if there is no method
     */    
    public Expr getMethod(String label, Object type, Object[] param) {
        if (getActualHierarchy() != null && param.length > 0) {
            for (String atype : getActualHierarchy().getSuperTypes((DatatypeValue) param[0], (DatatypeValue) type)) {
                Extension ext = getMethodExtension(atype);
                if (isDebug()) {
                    System.out.println("Ext: " + atype + " " + ext);
                }
                if (ext != null) {
                    Expr exp = ext.get(label, param.length);
                    if (isDebug()) {
                        System.out.println("Ext: " + atype + " " + exp);
                    }
                    if (exp != null) {
                        return exp;
                    }
                }
            }
        }
        return get(label, param.length);
    }
  
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("extension: ");
        sb.append(NL);
        for (FunMap m : getMaps()){
            for (Expr exp : m.values()) {
                sb.append(exp);
                sb.append(NL);
                sb.append(NL);
            }
        }
        return sb.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the pack
     */
    public Object getPackage() {
        return pack;
    }

    /**
     * @param pack the pack to set
     */
    public void setPackage(Object pack) {
        this.pack = pack;
    }
    
    /**
     * @return the extension
     */
    public Extension getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(Extension extension) {
        this.extension = extension;
    }
    
      /**
     * @return the hierarchy
     */
    public Hierarchy getActualHierarchy() {
        if (getExtension() != null){
            return getExtension().getHierarchy();
        }
        return getHierarchy();
    }

    
     /**
     * @return the hierarchy
     */
    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    /**
     * @param hierarchy the hierarchy to set
     */ 
    public void setHierarchy(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }
    
     /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }


    
}
