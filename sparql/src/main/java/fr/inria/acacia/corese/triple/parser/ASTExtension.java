package fr.inria.acacia.corese.triple.parser;


import fr.inria.corese.compiler.java.JavaCompiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Manage extension functions 
 * Expression exp must have executed exp.local() to tag
 * local variables
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class ASTExtension {
    private static Logger logger = LogManager.getLogger(ASTExtension.class);	

    static final String NL = System.getProperty("line.separator");
    //FunMap map;
    ASTFunMap[] maps;
    private String name;
    private Constant pack;
    ArrayList<Function> funList;
    
    public class ASTFunMap extends HashMap<String, Function> {}

    public ASTExtension() {
        //map = new FunMap();
        maps = new ASTFunMap[11];
        for (int i=0; i<maps.length; i++){
            maps[i] = new ASTFunMap();
        }
        funList = new ArrayList();
    }
    
    public ASTExtension(String n){
        this();
        name = n;
    }
    
    ASTFunMap getMap(Expression exp){
        return getMap(exp.getArgs().size());
    }
    
    ASTFunMap getMap(int n){
         if (n >= maps.length){
            return null;
        }
        return maps[n];
    }
    
    public ASTFunMap[] getMaps(){
        return maps;
    }
    
    public boolean isEmpty(){
        for (ASTFunMap m : getMaps()){
            if (! m.isEmpty()){
                return false;
            }
        }
        return true;
    }
    
    /**
     * exp = function (ex:name(?x) = exp )
     * @param exp 
     */
    void defineFunction(Function exp){
        funList.add(exp);
        define(exp);
    }
    
    public List<Function> getFunList(){
        return funList;
    }
    
    /**
     *
     * exp: function(st:fac(?x) = if (?x = 1, 1, ?x * st:fac(?x - 1)))
     */
    public void define(Function exp) {
        Expression fun = exp.getFunction(); //exp.getArg(0);
        ASTFunMap fm = getMap(fun);
        if (fm == null){
            logger.error("To many args: " + exp);
            return;
        }
        fm.put(fun.getLabel(), exp);
    }
    
    public void add(ASTExtension ext){
        for (ASTFunMap m : ext.getMaps()){
            for (Function e : m.values()){
                if (! isDefined(e.getFunction())){
                    define(e);
                }
            }
        }
    }

    public boolean isDefined(Expression exp) {
        return getMap(exp).containsKey(exp.getLabel());
    }

    /**
     * exp: st:fac(?n) values: #[10] actual values of parameters return body of
     * fun
     */
    public Expression get(Expression exp, Object[] values) {
        return getMap(exp).get(exp.getLabel());
    }

    public Expression get(Expression exp) {
        Expression def = getMap(exp).get(exp.getLabel());
        return def;
    }
    
    public Expression get(String label) {
        for (int i = 0; i<maps.length; i++){
            Expression exp = get(label, i);
            if (exp != null){
                return exp;
            }
        }
        return null;
    }
    
    public Expression get(String label, int n) {
        ASTFunMap m = getMap(n);
        if (m == null){
            return null;
        }
        return m.get(label);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("extension: ");
        sb.append(NL);
        for (ASTFunMap m : getMaps()){
            for (Expression exp : m.values()) {
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
    public Constant getPackage() {
        return pack;
    }

    /**
     * @param pack the pack to set
     */
    public void setPackage(Constant pack) {
        this.pack = pack;
    }
}

