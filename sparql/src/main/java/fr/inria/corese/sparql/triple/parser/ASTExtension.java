package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.query.Hierarchy;
import fr.inria.corese.kgram.filter.Extension;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.script.Function;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage extension functions 
 * Function exp must have executed exp.local() to tag
 * local variables
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ASTExtension implements Extension {

    private static ASTExtension singleton;
    public static int FUNCTION_PARAMETER_MAX = 15;
    private static Logger logger = LoggerFactory.getLogger(ASTExtension.class);
    static final String NL = System.getProperty("line.separator");
    public static final String TYPE = ExpType.TYPE_METADATA;
    //FunMap map;
    FunMap[] maps;
    private String name;
    private Object pack;
    private boolean compiled = false;

  
    public class FunMap extends HashMap<String, Function> {
        // String is metadata such as @before
        HashMap<String, Function> metadata;
        
        FunMap() {
            metadata = new HashMap<>();
        }
        
        Function getMetadata(String name) {           
           return metadata.get(name);
        }
        
        HashMap<String, Function> getMetadata() {
            return metadata;
        }
                    
        // @before -> f1 ; @after -> f2
        void setMetadata(Function exp) {
            Collection<String> list = exp.getMetadataList();
            if (list != null) {
                for (String name : list) {
                    metadata.put(name, exp);
                }
            }
        }
        
        void removeNamespace(String namespace) {
            ArrayList<String> list = new ArrayList<>();
            for (String name : keySet()) {
                if (name.startsWith(namespace)) {
                    logger.info("Remove function: " + name);
                    list.add(name);
                }
            } 
            for (String name : list) {
                remove(name);
            }
            list.clear();
            for (String meta : metadata.keySet()) {
                Function exp = metadata.get(meta);
                String name = exp.getFunction().getLabel();
                if (name.startsWith(namespace)) {
                    logger.info("Remove event: " + meta + " " + name);
                    list.add(meta);
                }
            } 
            for (String name : list) {
                metadata.remove(name);
            }
        }
    }
    
    // datatype -> Extension for methods of the datatype
    HashMap<String, ASTExtension> method;
    // Embedding extension in case of method
    private ASTExtension extension;
    private Hierarchy hierarchy;
    ArrayList<Function> funList;
    private boolean debug = false;
    
    static {
        setSingleton(new ASTExtension());
    }

    public ASTExtension() {
        maps = new FunMap[FUNCTION_PARAMETER_MAX];
        funList = new ArrayList<>();
        for (int i=0; i<maps.length; i++){
            maps[i] = new FunMap();
        }
    }
    
    public ASTExtension(String n){
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
    public void define(Function exp) {
        if (exp.getMetadataValues(TYPE) != null){
            defineMethod(exp);
        }
        else {
            defineFunction(exp);
        }
        funList.add(exp);
    }
    
    /**
     * name is a namespace
     */
    @Override
    public void removeNamespace(String name) {
        for (FunMap fm : getMaps()) {
            fm.removeNamespace(name);
        }
        
        if (getMethod() != null) {
            for (ASTExtension ext : getMethod().values()) {
                ext.removeNamespace(name);
            }
        }
    }
    
    void defineFunction(Function exp) { 
        Term fun = exp.getSignature(); 
        Function def = get(fun.getLabel(), fun.arity());
//        if  (def != null && exp != def) {
//            logger.info("Redefine function: " +fun.getLabel());
//            logger.info(def.toString());
//            logger.info(exp.toString());
//        }
        if (getMap(fun) == null) {
            logger.error("Undefined function: " + fun);
        }
        else {
            getMap(fun).put(fun.getLabel(), exp);
            getMap(fun).setMetadata(exp);
        }
    }
    
    void defineMethodFunction(Function exp) {       
        Expression fun = exp.getSignature(); 
        getMap(fun).put(fun.getLabel(), exp);
    }
    
    
    /**
     * Record function as method of datatype(s)
     */
    void defineMethod(Function exp) {   
         for (String type : exp.getMetadataValues(TYPE)){
            getCreateMethodExtension(type).defineMethodFunction(exp);
         }
    }
    
    /**
     * Return the Extension that records the methods of type    
     */
    ASTExtension getCreateMethodExtension(String type){
        ASTExtension ext = getMethod().get(type);
        if (ext == null){
            ext = new ASTExtension();
            ext.setExtension(this);
            getMethod().put(type, ext);
        }
        return ext;
    }
    
    ASTExtension getMethodExtension(String type){
        return getMethod().get(type);
    }
    
    public HashMap<String, ASTExtension> getMethod(){
        if (method == null){
            method = new HashMap<>();
        }
        return method;
    }
    
    @Override
    public boolean isMethod() {
        return getMethod().size() > 0;
    }
    
    public void add(ASTExtension ext){
        for (FunMap m : ext.getMaps()){
            for (Function e : m.values()){
                if (! isDefined(e.getSignature())){ 
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
            for (Function e : m.values()){
                e.setPublic(b);
            }
        }
    }

    @Override
    public boolean isDefined(Expr exp) {
        return getMap(exp).containsKey(exp.getLabel());
    }

    /**
     * exp: st:fac(?n) values: #[10] actual values of parameters return body of
     * fun
     * @param exp
     */
    public Function get(Expr exp, Object[] values) {
        return getMap(exp).get(exp.getLabel());
    }

    @Override
    public Function get(Expr exp) {
        Function def = getMap(exp).get(exp.getLabel());
        return def;
    }
    
    @Override
    public Function get(Expr exp, String name) {
        Function def = getMap(exp).get(name);
        return def;
    }
    
    @Override
    public Function get(String label) {
        for (int i = 0; i<maps.length; i++){
            Function exp = get(label, i);
            if (exp != null){
                return exp;
            }
        }
        return null;
    }
    
    @Override
    public Function get(String label, int n) {
        FunMap m = getMap(n);
        if (m == null){
            return null;
        }
        return m.get(label);
    }
    
    @Override
    public Function getMetadata(String metadata, int n) {
        FunMap m = getMap(n);
        if (m == null){
            return null;
        }
        Function f = m.getMetadata(metadata);
        return f;
    }
    
    /**
     * Retrieve a method with label name and param[0] (data)type 
     * There are two possible Hierarchies: DatatypeHierarchy and ClassHierarchy
     * ClassHierarchy extends DatatypeHierarchy (for literals only)
     * By default, return  function if there is no method
     */  
    @Override
    public Function getMethod(String label, IDatatype type, IDatatype[] param) {
        if (getActualHierarchy() != null && param.length > 0) {
            if (isDebug()) {
                    System.out.println("ASTExtension: " + label + " " + type);
            }
            for (String atype : getActualHierarchy().getSuperTypes(param[0],  type)) {
                ASTExtension ext = getMethodExtension(atype);
                if (isDebug()) {
                    System.out.println("ASTExtension: " + label + " " + atype + " " + ext);
                }
                if (ext != null) {
                    Function exp = ext.get(label, param.length);
                    if (isDebug()) {
                        System.out.println("ASTExtension: " + label + " " + atype + " " + exp);
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
            for (String name : m.keySet()) {
                Function exp = m.get(name);
                sb.append("# ").append(name).append(NL);
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
    public ASTExtension getExtension() {
        return extension;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(ASTExtension extension) {
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

    public List<Function> getFunList(){
        return funList;
    }
    
    public boolean isEmpty() {
        return getFunList().isEmpty();
    }

    /**
     * @return the compiled
     */
    public boolean isCompiled() {
        return compiled;
    }

    /**
     * @param compiled the compiled to set
     */
    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }
    

    @Override
    public void define(Expr exp) {
        if (exp instanceof Function) {
            define((Function) exp);
        }
    }
    
    public List<Function> getFunctionList() {
        ArrayList<Function> list = new ArrayList<>();
        for (FunMap m : getMaps()){
            for (Function e : m.values()){
                list.add(e);
            }
        }
        return list;
    }

    public static ASTExtension getSingleton() {
        return singleton;
    }

    public static void setSingleton(ASTExtension aSingleton) {
        singleton = aSingleton;
    }

}
