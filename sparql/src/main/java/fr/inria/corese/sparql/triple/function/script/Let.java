package fr.inria.corese.sparql.triple.function.script;

import fr.inria.corese.kgram.api.core.ExprType;
import fr.inria.corese.sparql.api.Computer;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Processor;
import fr.inria.corese.sparql.triple.parser.Variable;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.sparql.triple.parser.ASTBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Let extends Statement {
    private boolean dynamic = false;
    private List<Expression> declaration;
    Expression body;

    public Let() {}
    
    public Let(Expression def, Expression body) {
        super(Processor.LET, def, body);
        setDeclaration(def);
        setBody(body);
    }
    
    public Let(Expression def, Expression body, boolean dyn) {
        super(Processor.LET, def, body);
        setDynamic(dyn);
        setDeclaration(def);
        setBody(body);
        if (getVariable() != null) {
            // use case: intermediate let (match(x, y)) ...
            getVariable().setDynamic(dyn);
        }
    }
    
     public Let(List<Expression> decl, Expression body, boolean dyn) {
        super(Processor.LET);
        setDynamic(dyn);
        for (Expression def : decl) {
            add(def);
            if (def.getArg(0).isVariable()) {
                def.getArg(0).getVariable().setDynamic(dyn);
            }
        }
        add(body);
        setDeclaration(decl);
        setBody(body);
    }
    
     // TODO: clean this
    @Override
    public void setArgs(ArrayList<Expression> list) {
        super.setArgs(list);
    }

    @Override
    public Let getLet() {
        return this;
    }

    /**
     * let (var = exp){ exp }
     *
     * @return
     */
    @Override
    public Variable getVariable() {
        return getVariableDefinition().getBasicArg(0).getVariable();
    }

    @Override
    public Expression getDefinition() {
        return getVariableDefinition().getBasicArg(1);
    }
    
    public Variable getVariable(Expression decl) {
        return decl.getArg(0).getVariable();
    }
    
    public Expression getDefinition(Expression decl) {
        return decl.getArg(1);
    }
    
    public void setDefinition(Expression decl, Expression exp) {
        decl.getArgs().set(1, exp);
    }

    @Override
    public Expression getBody() {
        return body;
    }
    
    public void setBody(Expression exp) {
        body = exp;
    }

    public Expression getVariableDefinition() {
        return getBasicArg(0);
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        sb.append(Processor.LET);
        sb.append(" (");
        int i = 0;
        
        for (Expression decl : getDeclaration()) {
            if (i++ > 0) {
                sb.append(", ");
            }
            decl.getArg(0).toString(sb);
            sb.append(" = ");
            decl.getArg(1).toString(sb);
        }

        sb.append(") {");
        sb.nlincr();
        getBody().toString(sb);
        sb.nldecr();
        sb.append("}");
        return sb;
    }

    @Override
    public IDatatype eval(Computer eval, Binding b, Environment env, Producer p) throws EngineException {
        b.allocation(this);
        int i = -1; // -1 because pop before current i
        for (Expression decl : getDeclaration()) {
            Expression exp = getDefinition(decl);
            IDatatype val = exp.eval(eval, b, env, p);
            if (val == null) {
                switch (exp.oper())  {
                    case ExprType.XT_GEN_GET:
                        // let (select * where {}) with no solution
                        // access to variables do not raise an error
                        // they can be trapped by bound()
                        break;
                    // pop when i >= 0 ; when i = -1 there is nothing to pop
                    default:
                        desallocate(b, i);
                        return null;
                }
            } else if (val == DatatypeMap.UNBOUND) {
                val = null;
            }
            b.setlet(this, getVariable(decl), val);
            i++;
        }
        
        boolean save = true; 
        if (isDynamic()) {
            save = b.isDynamicCapture();
            b.setDynamicCapture(isDynamic());
        }
        IDatatype res = null;
        try {
            res = getBody().eval(eval, b, env, p);
        }
        finally  {
            if (isDynamic()) {
                b.setDynamicCapture(save);
            }
        }
        
        desallocate(b, getDeclaration().size() - 1);
        
        return res;
    }
    
    
    void desallocate(Binding b, int n) {
        if (b.desallocation(this)) {
            // do nothing
        }
        else for (int i = n; i >= 0; i--) {
            Expression decl = getDeclaration().get(i);
            b.unsetlet(this, getVariable(decl), null);
        }
    }
    
    @Override
    public void tailRecursion(Function fun) {
        getBody().tailRecursion(fun);
    }

    /**
     * @return the dynamic
     */
    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * @param dynamic the dynamic to set
     */
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
   
    public void setDeclaration(List<Expression> declaration) {
        this.declaration = declaration;
    }
    
    public void setDeclaration(Expression exp) {
        ArrayList<Expression> list = new ArrayList<>();
        list.add(exp);
        setDeclaration(list);
    }
    
    public List<Expression> getDeclaration() {
        return declaration;
    }
    
    public void removeDeclaration(Expression exp) {
        getDeclaration().remove(exp);
        getArgs().remove(exp);
        if (getExpList() != null) {
            getExpList().remove(exp);
        }
    }
    
    /**
     *  Is it let (select where)
     */
    public boolean isLetQuery() {
        if (isDynamic()) {
            return false;
        }
        Expression exp = getDefinition(getDeclaration().get(0));        
        return exp.oper() == ExprType.XT_GET && exp.getArg(0).oper() == ExprType.EXIST;
    }
}
