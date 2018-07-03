/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.sparql.triple.function.term;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Binder;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * Stack vor LDScript variable bindings Variable have a relative index stack
 * index = level + var index level is the level of current function call in the
 * stack
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class Binding implements Binder {

    static final String NL = System.getProperty("line.separator");
    static final int UNBOUND = ExprType.UNBOUND;

    ArrayList<Expr> varList;
    ArrayList<IDatatype> valList;
    // level of the stack before function call
    // every funcall add a level
    // let add no level
    ArrayList<Integer> level;
    int currentLevel = 0, count = 0;
    Expr current;
    HashMap<String, IDatatype> globalValue;
    HashMap<String, Variable>  globalVariable;

    private boolean debug = false;

    private static Logger logger = LoggerFactory.getLogger(Binding.class);
    private boolean result;

    Binding() {
        varList = new ArrayList();
        valList = new ArrayList();
        level = new ArrayList();
        globalValue    = new HashMap<>();
        globalVariable = new HashMap<>();
    }

    public static Binding create() {
        return new Binding();
    }

    @Override
    public void clear() {
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
        if (! getGlobalVariableNames().isEmpty()) {
            sb.append("Global:").append(NL);
        }
        for (String name : getGlobalVariableNames().keySet()) {
            sb.append(name).append(" = ").append(getGlobalVariableValues().get(name)).append(NL);
        }
        return sb.toString();
    }

    public int size() {
        return varList.size();
    }

    int index() {
        return varList.size() - 1;
    }

    /**
     * A function (possibly with no arg) or a let has been called
     *
     * @return
     */
    @Override
    public boolean hasBind() {
        return varList.size() > 0 || level.size() > 0;
    }

    int getIndex(Expr var) {
        return currentLevel + var.getIndex();
    }

    public void set(Expr exp, Expr var, IDatatype val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                count++;
                allocate(exp);
                break;

            case ExprType.LET:
            case ExprType.FOR:
                if (exp.getNbVariable() > 0) {
                    allocate(exp);
                }
        }
        set(var, val);
        if (isDebug()) {
            trace(exp, val);
        }
    }

    public void unset(Expr exp, Expr var, IDatatype val) {
        switch (exp.oper()) {
            case ExprType.FUNCTION:
                // special case: walker aggregate
                desallocate(exp);
                break;

            case ExprType.LET:
            case ExprType.FOR:
                if (exp.getNbVariable() > 0) {
                    desallocate(exp);
                    return;
                }
            // else continue

            default:
                unset(var);
        }
    }

     //special case: unary function
    public void set(Function exp, Expr var, IDatatype val) {
        pushLevel();
        push(var, val);
        // allocate additional variables
        for (int j = 1; j < exp.getNbVariable(); j++) {
            push(null, null);
        }
    }
    
    public void setTailRec(Function exp, Expr var, IDatatype val) {
        valList.set(currentLevel, val);
        // unset additional variables
        for (int j = 1; j < exp.getNbVariable(); j++) {
            valList.set(currentLevel+j, null);
        }
    }
    
     //special case: binary function
     public void set(Function exp, Expr var1, IDatatype val1, Expr var2, IDatatype val2) {
        pushLevel();
        push(var1, val1);
        push(var2, val2);
        // allocate additional variables
        for (int j = 2; j < exp.getNbVariable(); j++) {
            push(null, null);
        }
    }
    
    public void unset(Function exp) {
        desallocate(exp);
    }
    
    
//    public void set(Function exp, Expr var, IDatatype val) {
//        allocate(exp);
//        set(var, val);
//    }
//
    
    /**
     * Level of current function call in the stack Also level of filter for/let
     */
    void pushLevel() {
        currentLevel = varList.size();
        level.add(currentLevel);
    }

    void popLevel() {
        if (!level.isEmpty()) {
            level.remove(level.size() - 1);
        }
        if (level.isEmpty()) {
            currentLevel = 0;
        } else {
            currentLevel = level.get(level.size() - 1);
        }
    }

    void pop() {
        varList.remove(varList.size() - 1);
        valList.remove(valList.size() - 1);
    }

    /**
     * Allocate block in the stack for local variables of exp
     */
    public void allocate(Expr exp) {
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
    public void desallocate(Expr exp) {
        for (int i = 0; i < exp.getNbVariable(); i++) {
            pop();
        }
        popLevel();
    }

    /**
     * Function call
     */
    public void set(Expr exp, List<Expr> lvar, IDatatype[] value) {
        count++;
        pushLevel();
        int i = 0;
        for (Expr var : lvar) {
            push(var, value[i++]);
        } 
        // allocate additional variables:
        for (int j = i; j<exp.getNbVariable(); j++){
            push(null, null);
        }
    }

    void push(Expr var, IDatatype val) {
        varList.add(var);
        valList.add(val); 
    }
    
    void set(Expr var, IDatatype val) {
        int index = getIndex(var);
        varList.set(index, var);
        valList.set(index, val);
    }

    @Override
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
    @Override
    public IDatatype get(Expr var) {
        switch (var.getIndex()) {
            case UNBOUND: 
                IDatatype dt = globalValue.get(var.getLabel());
                if (dt == null) {
                    logger.warn("Variable unbound: " + var);
                }
                return dt;
            default:      return valList.get(getIndex(var));
        }
    }

    void trace(Expr exp, IDatatype[] value) {
        System.out.print(pretty(count) + " " + pretty(level.size()) + " ");
        System.out.print((exp.getFunction() == null) ? exp.getDefinition() : exp.getFunction() + " ");
        for (IDatatype dt : value) {
            System.out.print(" " + dt);
        }
        System.out.println();
    }

    String pretty(int n) {
        return "(" + ((n < 10) ? ("0" + n) : n) + ")";
    }

    void trace(Expr exp, IDatatype value) {
        IDatatype[] aa = new IDatatype[1];
        aa[0] = value;
        trace(exp, aa);
    }

    // todo:  why not level ???
    @Override
    public boolean isBound(String label) {
        for (int i = index(); i >= 0; i--) {
            if (varList.get(i).getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    int getLevel() {
        return level.get(level.size() - 1);
    }

    int getCurrentLevel() {
        return (level.isEmpty()) ? 0 : getLevel();
    }
    
    public IDatatype getVariable(String name) {
        return getGlobalVariableValues().get(name);
    }
   
    public Binding setVariable(String var, IDatatype val) {
        return bind(new Variable(var), val);
    }
    
    
    public Binding bind(Variable var, IDatatype val) {
        bind(null, var, val);
        return this;
    }

    /**
     * set(?x = exp) ?x is already bound, assign variable
     */
    public void bind(Expr exp, Expr var, IDatatype val) {
        switch (var.getIndex()) {
            case UNBOUND:
                globalValue.put(var.getLabel(), val); 
                globalVariable.put(var.getLabel(), (Variable) var);
                break;
            default: valList.set(getIndex(var), val);
        }       
    }

    @Override
    public List<Expr> getVariables() {
        if (level.size() > 0) {
            // funcall: return variables of this funcall (including let var)
            return getVar();
        } else {
            // let variables
            return varList;
        }
    }

    /**
     * Funcall has bound variables from level to top of stack Return these
     * variables (may be empty if function has no arg)
     *
     * @return
     */
    List<Expr> getVar() {
        int start = getLevel();
        int top = varList.size();
        ArrayList<Expr> list = new ArrayList();
        for (int i = start; i < top; i++) {
            if (varList.get(i) != null && valList.get(i) != null) {
                list.add(varList.get(i));
            }
        }
        for (Variable var : globalVariable.values()) {
            if (! list.contains(var)) {
                list.add(var);
            }
        }
        return list;
    }
    
    @Override
    public void share(Binder b) {
        share((Binding) b);
    }
    
    void share(Binding b) {
        setGlobalVariableNames(b.getGlobalVariableNames());
        setGlobalVariableValues(b.getGlobalVariableValues());
    }
    
    public HashMap<String, Variable> getGlobalVariableNames() {
        return globalVariable;
    }
    
    public HashMap<String, IDatatype> getGlobalVariableValues() {
        return globalValue;
    }
      
    void setGlobalVariableNames(HashMap<String, Variable> m) {
        globalVariable = m;
    }
    
    void setGlobalVariableValues(HashMap<String, IDatatype> m) {
        globalValue = m;
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

    public int getCount() {
        return count;
    }

    @Override
    public void bind(Expr exp, Expr var, Node val) {
        bind(exp, var, (IDatatype) val);
    }

    @Override
    public void set(Expr exp, Expr var, Node val) {
        set(exp, var, (IDatatype) val);
    }

    @Override
    public void set(Expr exp, List<Expr> lvar, Node[] value) {
        set(exp, lvar, (IDatatype[]) value);
    }

    @Override
    public void unset(Expr exp, Expr var, Node value) {
        unset(exp, var, (IDatatype) value);
    }

    /**
     * return(dt) LDScript function set boolean result field to true
     *
     * @param dt
     * @return
     */
    public IDatatype result(IDatatype dt) {
        result = true;
        return dt;
    }

    /**
     * sequence and for loop LDScript statements check whether intermediate
     * evaluation is a return(dt); if true, evaluation resumes
     *
     * @return the result
     */
    public boolean isResult() {
        return result;
    }

    /**
     * LDScript function call return dt and set boolean result field to false
     *
     * @param dt
     * @return
     */
    public IDatatype resultValue(IDatatype dt) {
        result = false;
        return dt;
    }

}
