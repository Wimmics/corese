package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Local variable bindings
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Bind {

    static final String NL = System.getProperty("line.separator");
    ArrayList<Expr> varList;
    ArrayList<Node> valList;
    // level of the stack before function call
    // every funcall add a level
    // let add no level
    ArrayList<Integer> level;
    Expr current;
    
    private static Logger logger = LogManager.getLogger(Bind.class);

    Bind() {
        varList = new ArrayList();
        valList = new ArrayList();
        level   = new ArrayList();
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

    /**
     * Get variable value within current function call binding environment
     * between top of stack and level
     */
    public Node get(Expr var) {
        int i = getIndex(var);
        if (i == -1) return null;
        return valList.get(i);
    }
    
    int getIndex(Expr var){
        return computeIndex(var);        
    }
    
    int getIndex1(Expr var){
        int index = var.getIndex();
        if (index == ExprType.UNBOUND){           
            return -1;
        }
        return index;
    }
    
    int computeIndex(Expr var){
       
        int end = getCurrentLevel();                   
        for (int i = index(); i >= end; i--) {
            if (varList.get(i).equals(var)) {  
                if (var.getDefinition() != null && var.getDefinition() != varList.get(i)){                   
                    return -1;
                }
                 if (var.getDefinition() == null){
            System.out.println("**********************************");
            System.out.println(current);
            System.out.println(var);
            System.out.println("**********************************");
        }
                return i;
            }
        }
        return -1;
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
        int i = getIndex(var);
        if (i != -1){
           valList.set(i, val); 
        }
    }
    
    /**
     * let (?x = exp)
     */
    public void set(Expr exp, Expr var, Node val) {
        set(var, val);
    }

    /**
     * us:fun(?x, ?y)
     */
    public void set(Expr exp, List<Expr> lvar, Object[] value) {
        if (exp.oper() == ExprType.FUNCTION) {
            // Parameters and local variables of this function are above level 
            level.add(varList.size()); 
            current = exp;
        }
        int i = 0;
        for (Expr var : lvar) {
            set(var, (Node) value[i++]);
        }
    }
    
    /**
     * define parameter/local variable and set its value
     */
    private void set(Expr var, Node val) {
        varList.add(var);
        valList.add(val);
    }


    public void unset(Expr exp, Expr var) {
        unset(var);
    }

    public void unset(Expr exp, List<Expr> lvar) {
        if (exp.oper() == ExprType.FUNCTION){ // ||exp.oper() == ExprType.EQ){
           if (! level.isEmpty()) {
               level.remove(level.size()-1);
           }
           else {
               System.out.println("Bind: \n" + exp);
               System.out.println(this);
           }
        }
        for (int j = lvar.size() - 1; j >= 0; j--) {
            unset(lvar.get(j));
        }
    }


    private void unset(Expr var) {
        if (! varList.isEmpty()){
            varList.remove(index());
            valList.remove(valList.size() - 1);           
        }
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
         for (int i = start; i<top; i++){
             list.add(varList.get(i));
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
