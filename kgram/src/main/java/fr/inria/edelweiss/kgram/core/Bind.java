package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

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
    private static Logger logger = Logger.getLogger(Bind.class);

    Bind() {
        varList = new ArrayList();
        valList = new ArrayList();
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


    public void set(Expr exp, Expr var, Node val) {
        set(var, val);
    }

    public void set(Expr exp, List<Expr> lvar, Object[] value) {
        int i = 0;
        for (Expr var : lvar) {
            set(var, (Node) value[i++]);
        }
    }

    public void unset(Expr exp, Expr var) {
        unset(var);
    }

    public void unset(Expr exp, List<Expr> lvar) {
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
        return varList;
    } 
}
