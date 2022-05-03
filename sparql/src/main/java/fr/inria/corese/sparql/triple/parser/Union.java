package fr.inria.corese.sparql.triple.parser;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.triple.cst.Keyword;
import fr.inria.corese.sparql.triple.cst.KeywordPP;

/**
 * <p>
 * Title: Corese</p>
 * <p>
 * Description: A Semantic Search Engine</p>
 * <p>
 * Copyright: Copyright INRIA (c) 2007</p>
 * <p>
 * Company: INRIA</p>
 * <p>
 * Project: Acacia</p>
 *
 * @author Olivier Corby & Olivier Savoie
 */
public class Union extends Binary {

    /**
     * Use to keep the class version, to be consistent with the interface
     * Serializable.java
     */
    private static final long serialVersionUID = 1L;
    static int num = 0;

    public Union() {
    }

    public Union(Exp e1, Exp e2) {
        add(e1);
        add(e2);
    }

    public static Union create() {
        return new Union();
    }

    public static Union create(Exp e1, Exp e2) {
        return new Union(e1, e2);
    }

    @Override
    public boolean isUnion() {
        return true;
    }

    @Override
    public Union getUnion() {
        return this;
    }

    void basicVariables(VariableScope sort, List<Variable> list) {
        if (size() > 1) {
            List<Variable> left  = get(0).getVariables(sort);
            List<Variable> right = get(1).getVariables(sort);

            for (Variable var : left) {
                if (right.contains(var)) {
                    add(var, list);
                }
            }
        }
    }
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        switch (sort.getScope()) {
            case SUBSCOPE:  basicVariables(sort, list); break;
            default:  super.getVariables(sort, list); break;
        }
    }
    
    String getOper() {
        return Keyword.SEOR;
    }

    @Override
    public ASTBuffer toString(ASTBuffer sb) {
        get(0).toString(sb);
        sb.nl().append(KeywordPP.UNION).append(" ");
        get(1).pretty(sb);
        return sb;
    }


    /**
     * Each branch of union binds its variable (in parallel)
     */
    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        boolean ok = true;

        List<Variable> list = ast.getStack();
        List<List<Variable>> ll = new ArrayList();

        for (Exp exp : getBody()) {
            ast.newStack();
            boolean b = exp.validate(ast, exist);
            if (!b) {
                ok = false;
            }
            ll.add(ast.getStack());
        }

        ast.setStack(list);

        for (List<Variable> l : ll) {
            ast.addStack(l);
        }
        return ok;
    }

}
