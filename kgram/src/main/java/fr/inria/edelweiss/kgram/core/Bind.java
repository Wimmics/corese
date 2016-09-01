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
    Node value;
    ArrayList<Expr> varList;
    ArrayList<Node> valList;
    // level of the stack before function call
    // every funcall add a level
    // let add no level
    ArrayList<Integer> level;
    
    private static Logger logger = LogManager.getLogger(Bind.class);

    Bind() {
        varList = new ArrayList();
        valList = new ArrayList();
        level   = new ArrayList();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = varList.size() - 1; i >= 0; i--) {
            sb.append(varList.get(i) + " = " + valList.get(i) + NL);
        }
        return sb.toString();
    }
    
    public int size() {
        return varList.size();
    }
    
    /**
     * A function (possibly with no arg) or a let has been called
     * @return 
     */
    public boolean hasBind(){
        return varList.size() > 0 || level.size()>0;
    }

    public Node get(Expr var) {
//        System.out.println("B: " + var + " " + var.getIndex());
//        System.out.println(this);
        for (int i = varList.size() - 1; i >= 0; i--) {
            if (varList.get(i).equals(var)) {
                return valList.get(i);
            }
        }
        return null;
    }
    
    public boolean isBound(String label){
        for (int i = varList.size() - 1; i >= 0; i--) {
            if (varList.get(i).getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO; scope of variable
     */
    public void bind(Expr exp, Expr var, Node val) {
        for (int i = varList.size() - 1; i >= 0; i--) {
            if (varList.get(i).equals(var)) {
                valList.set(i, val);
            }
        }
    }
    
    public void set(Expr exp, Expr var, Node val) {
        set(var, val);
    }

    public void set(Expr exp, List<Expr> lvar, Object[] value) {
        if (exp.oper() == ExprType.FUNCTION || exp.oper() == ExprType.EQ){
            // xt:fun(?x) = exp
            // funcall
            level.add(varList.size());           
        }
        int i = 0;
        for (Expr var : lvar) {
            set(var, (Node) value[i++]);
        }
    }

    public void unset(Expr exp, Expr var) {
        unset(var);
    }

    public void unset(Expr exp, List<Expr> lvar) {
        if (exp.oper() == ExprType.FUNCTION ||exp.oper() == ExprType.EQ){
            // xt:fun(?x) = exp
            // funcall
            level.remove(level.size()-1);
        }
        for (int j = lvar.size() - 1; j >= 0; j--) {
            unset(lvar.get(j));
        }
    }

    private void set(Expr var, Node val) {
        varList.add(var);
        valList.add(val);
    }

    private void unset(Expr var) {
        varList.remove(varList.size() - 1);
        valList.remove(valList.size() - 1);
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
         int start = level.get(level.size()-1);
         int top   = varList.size();
         ArrayList<Expr> list = new ArrayList();
         for (int i = start; i<top; i++){
             list.add(varList.get(i));
         }
         return list;
     }
     
     public Mapping getMapping(Query q) {
        ArrayList<Node> lvar = new ArrayList();
        ArrayList<Node> lval = new ArrayList();
        for (Expr var : getVariables()) {
            Node node = q.getProperAndSubSelectNode(var.getLabel());
            if (node != null) {
                lvar.add(node);
                lval.add(get(var));
            }
        }
        Mapping m = Mapping.create(lvar, lval);
        return m;
    }


}
