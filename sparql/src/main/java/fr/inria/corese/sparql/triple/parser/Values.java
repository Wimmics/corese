package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.Walker;
import java.util.List;
import java.util.ArrayList;

import fr.inria.corese.sparql.triple.cst.KeywordPP;

/**
 *
 * @author corby
 *
 */
public class Values extends Exp {

    private List<Variable> lvar;
    private List<List<Constant>> lval;
    private Expression exp;
    Binding bind;
    private boolean moved = false;

    Values(List<Variable> var, List<List<Constant>> lval) {

    }

    Values() {
        lval = new ArrayList();
    }

    public static Values create(List<Variable> var, List<Constant> val) {
        Values vv = new Values();
        vv.setVariables(var);
        vv.addValues(val);
        return vv;
    }
    
    public static Values create(List<Variable> var) {
        List<Constant> val = new ArrayList<>();
        for (Variable v : var) {
            val.add(null);
        }
        Values vv = new Values();
        vv.setVariables(var);
        vv.addValues(val);
        return vv;
    }
    
    public static Values create(List<Variable> var, Expression exp) {
        return create(var).setExp(exp);
    }

    public static Values create(Variable var, List<Constant> val) {
        Values vv = new Values();

        ArrayList<Variable> varList = new ArrayList<>();
        varList.add(var);
        vv.setVariables(varList);

        for (Constant cst : val) {
            ArrayList<Constant> list = new ArrayList<>();
            list.add(cst);
            vv.addValues(list);
        }

        return vv;
    }

    public static Values create(Variable var, Constant val) {
        Values vv = new Values();
        ArrayList<Variable> lvar = new ArrayList<>();
        lvar.add(var);
        ArrayList<Constant> lval = new ArrayList<>();
        lval.add(val);
        vv.setVariables(lvar);
        vv.addValues(lval);
        return vv;
    }

    public static Values create() {
        return new Values();
    }

    @Override
    public Values copy() {
        return this;
    }
    
    public boolean isDefined() {
        for (List<Constant> list : getValues()) {
            for (Constant cst : list) {
                if (cst != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Values getValuesExp() {
        return this;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        String SPACE = " ";
        String NL = ASTQuery.NL;

        sb.append(KeywordPP.BINDINGS);
        sb.append(SPACE);

        sb.append(KeywordPP.OPEN_PAREN);
        for (Atom var : getVariables()) {
            sb.append(var.getName());
            sb.append(SPACE);
        }
        sb.append(KeywordPP.CLOSE_PAREN);

        sb.append(KeywordPP.OPEN_BRACKET);
        if (exp == null) {
            sb.incr();
            for (List<Constant> list : getValues()) {
                sb.nl();
                sb.append(KeywordPP.OPEN_PAREN);

                for (Constant value : list) {
                    if (value == null) {
                        sb.append(KeywordPP.UNDEF);
                    } else {
                        value.toString(sb);
                    }
                    sb.append(SPACE);
                }
                sb.append(KeywordPP.CLOSE_PAREN);
            }
            sb.nldecr();

        } else {
            sb.append(SPACE);
            exp.toString(sb);
            sb.append(SPACE);
        }
        sb.append(KeywordPP.CLOSE_BRACKET);
        return sb;
    }

    void getVariables(List<Variable> list) {
        for (Variable var : getVarList()) {
            add(var, list);
        }
    }
    
    // this variable list includedIn varList   
    public boolean isBound(List<Variable> varList) {
        for (Variable var : getVarList()) {
            if (!varList.contains(var)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        getVariables(list);
    }

    @Override
    public boolean isValues() {
        return true;
    }

    public void setVariables(List<Variable> lvar) {
        this.lvar = lvar;
    }
    
    public void setVariable(Variable var) {
        ArrayList<Variable> l = new ArrayList<>();
        l.add(var);
        setVariables(l);
    }
   
    public List<Variable> getVarList() {
        return lvar;
    }

    void setValues(List<List<Constant>> lval) {
        this.lval = lval;
    }

    public void addValues(List<Constant> l) {
        lval.add(l);
    }

    public void addExp(Expression exp) {
        setExp(exp);
    }

    public List<List<Constant>> getValues() {
        return lval;
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        for (Variable var : getVariables()) {
            ast.bind(var);
            if (!exist) {
                ast.defSelect(var);
            }
        }
        return true;
    }
    
   @Override
   public void walk(Walker walker) {
       walker.enter(this);
       if (hasExpression()) {
            getExpression().walk(walker);
       }  
       walker.leave(this);
   }

  
    public boolean isMoved() {
        return moved;
    }

 
    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    
    public Expression getExp() {
        return exp;
    }
    
    public Expression getExpression() {
        return exp;
    }
    
    public Values setExp(Expression exp) {
        this.exp = exp;
        return this;
    }

    public boolean hasExpression() {
        return exp != null;
    }

   
    @Override
    public Binding getBind() {
        return bind;
    }

    
    public void setBind(Binding bind) {
        this.bind = bind;
    }

}
