package fr.inria.corese.kgram.core;

import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Binder;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * Stack vor LDScript variable bindings
 * Variable have a relative index 
 * stack index = level + var index
 * level is the level of current function call in the stack
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Bind implements Binder {

    static final String NL = System.getProperty("line.separator");
    static final int UNBOUND = ExprType.UNBOUND;
    
    ArrayList<Expr> varList;
    ArrayList<Node> valList;
    // level of the stack before function call
    // every funcall add a level
    // let add no level
    ArrayList<Integer> level;
    int currentLevel = 0;
    Expr current;
    
    private static Logger logger = LogManager.getLogger(Bind.class);

    Bind() {
        varList = new ArrayList();
        valList = new ArrayList();
        level   = new ArrayList();
    }
    
    public static Bind create(){
         return new Bind();
    }
    
    @Override
    public void clear(){
        varList.clear();
        valList.clear();
        level.clear();
        currentLevel = 0;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Level: ").append(level).append(NL);
        for (int i = index(); i >= 0; i--) {
            sb.append("(").append(i).append(") ");
            sb.append(varList.get(i)).append(" = ").append(valList.get(i));                    
            sb.append(NL);
        }
        return sb.toString();
    }
    
    public int size() {
        return varList.size();
    }
    
    int index(){
        return varList.size() - 1;
    }
    
    /**
     * A function (possibly with no arg) or a let has been called
     * @return 
     */
    public boolean hasBind(){
        return varList.size() > 0 || level.size()>0;
    }

    
    
    int getIndex(Expr var) {
        return currentLevel + var.getIndex();
    }

    
    public void set(Expr exp, Expr var, Node val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                allocate(exp); break;
                
            case ExprType.LET:
            case ExprType.FOR:
                if (exp.getNbVariable() > 0){
                    allocate(exp); 
                }               
        }
        set(var, val);
    }

    
    public void unset(Expr exp, Expr var, Node val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                desallocate(exp);
                break;
                
            case ExprType.LET:
            case ExprType.FOR:
                if (exp.getNbVariable() > 0){
                    desallocate(exp); return;
                } 
                // else continue
                
            default: unset(var);
        }        
    }

    /**
     * Level of current function call in the stack
     * Also level of filter for/let
     */
    void pushLevel() {
        level.add(varList.size());
        currentLevel = varList.size();
    }

    void popLevel() {
        if (!level.isEmpty()) {
            level.remove(level.size() - 1);
        }
        if (level.isEmpty()){
            currentLevel = 0;
        }
        else {
           currentLevel = level.get(level.size() -1);
        }
    }
    
    void pop(){
       varList.remove(varList.size() - 1);
       valList.remove(valList.size() - 1); 
    }

    /**
     * Allocate block in the stack for local variables of exp
     */
    void allocate(Expr exp) {
        pushLevel();
        // allocate block for fun
        for (int i = 0; i < exp.getNbVariable(); i++) {
            varList.add(null);
            valList.add(null);
        }
    }

    /**
     * Desallocate block in the stack for local variables of exp
     */
    void desallocate(Expr exp) {
        for (int i = 0; i < exp.getNbVariable(); i++) {
            pop();
        }
        popLevel();
    }

    /**
     * Function call
     */
    
    public void set(Expr exp, List<Expr> lvar, Node[] value) {
        // Parameters and local variables of this function are above level 
        allocate(exp);
        int i = 0;
        for (Expr var : lvar) {
            // push parameter value
            set(var, value[i++]);
        }
    }

    
    void set(Expr var, Node val) {
        int index = getIndex(var);
        varList.set(index, var);
        valList.set(index, val);
    }

    
    public void unset(Expr exp, List<Expr> lvar) {
        desallocate(exp);
    }

    
    void unset(Expr var) {
        valList.set(getIndex(var), null);
    }
    
    /**
     * Get variable value within current function call binding environment
     * between top of stack and level
     */
    public Node get(Expr var) {    
        return valList.get(getIndex(var));
    }
                      
    
    // todo:  why not level ???
    public boolean isBound(String label){
        for (int i = index(); i >= 0; i--) {
            if (varList.get(i).getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }
    
    int getLevel(){
        return level.get(level.size() - 1);
    }
    
    int getCurrentLevel(){
        return (level.isEmpty()) ? 0 : getLevel();
    }

    /**
     * set(?x = exp)
     * ?x is already bound, assign variable
     */
    public void bind(Expr exp, Expr var, Node val) {
         valList.set(getIndex(var), val); 
    }
    
  
     public List<Expr> getVariables() {
         if (level.size() > 0){
             // funcall: return variables of this funcall (including let var)
             return getVar();
         }
         else {
             // let variables
             return varList;
         }
    }
     
     /**
      * Funcall has bound variables from level to top of stack
      * Return these variables (may be empty if function has no arg)
      * @return 
      */
     List<Expr> getVar(){
         int start = getLevel();
         int top   = varList.size();
         ArrayList<Expr> list = new ArrayList();
         for (int i = start; i<top; i++) {
             if (varList.get(i) != null && valList.get(i) != null){
                list.add(varList.get(i));
             }
         }
         return list;
     }
     
     /**
      * TODO: remove duplicates in getVariables()
      * use case:
      * function us:fun(?x){let (select ?x where {}) {}}
      * variable ?x appears twice in the stack because it is redefined in the let clause
      */     
     public Mapping getMapping(Query q) {
        ArrayList<Node> lvar = new ArrayList();
        ArrayList<Node> lval = new ArrayList();
        for (Expr var : getVariables()) {
            Node node = q.getProperAndSubSelectNode(var.getLabel());
            if (node != null && ! lvar.contains(node)) {
                lvar.add(node);
                lval.add(get(var));
            }
        }
        Mapping m = Mapping.create(lvar, lval);
        return m;
    }


}
