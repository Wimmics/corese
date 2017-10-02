package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import java.util.List;

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
public class BindIndex extends Bind {

    BindIndex() {
        super();
    }

    @Override
    int getIndex(Expr var) {
        int index = getCurrentLevel() + var.getIndex();
        if (index == ExprType.UNBOUND) {
            return -1;
        }
        return index;
    }

    @Override
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

    @Override
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
    }

    void popLevel() {
        if (!level.isEmpty()) {
            level.remove(level.size() - 1);
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
    @Override
    public void set(Expr exp, List<Expr> lvar, Node[] value) {
        // Parameters and local variables of this function are above level 
        allocate(exp);
        int i = 0;
        for (Expr var : lvar) {
            // push parameter value
            set(var, value[i++]);
        }
    }

    @Override
    void set(Expr var, Node val) {
        int index = getIndex(var);
        varList.set(index, var);
        valList.set(index, val);
    }

    @Override
    public void unset(Expr exp, List<Expr> lvar) {
        desallocate(exp);
    }

    @Override
    void unset(Expr var) {
        valList.set(getIndex(var), null);
    }
}
