package fr.inria.corese.sparql.triple.parser;

import java.util.List;

import fr.inria.corese.sparql.triple.cst.KeywordPP;

public class Minus extends Binary {

    Minus() {
    }

    Minus(Exp e1, Exp e2) {
        add(e1);
        add(e2);
    }

    public static Minus create(Exp exp) {
        Minus e = new Minus();
        e.add(exp);
        return e;
    }

    @Override
    public boolean isMinus() {
        return true;
    }

    @Override
    public Minus getMinus() {
        return this;
    }

//    @Override
//    public ASTBuffer toString(ASTBuffer sb) {
//        sb.append("{ ").incr();
//        get(0).display(sb);
//        sb.decr().nl().append("} ").append(KeywordPP.MINUS).append(" ");
//        get(1).pretty(sb);
//        return sb;
//    }
    
    @Override
    public ASTBuffer toString(ASTBuffer sb) {        
        toString(eget(0), sb);
        sb.nl().append(KeywordPP.MINUS)
                .append(KeywordPP.SPACE);
        eget(1).pretty(sb);
        return sb;
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        boolean b1 = getBody().get(0).validate(ast, exist);

        List<Variable> list = ast.getStack();
        ast.newStack();
        boolean b2 = getBody().get(1).validate(ast, true);
        ast.setStack(list);

        return b1 && b2;
    }

    void basicVariables(VariableScope sort, List<Variable> list) {
        if (size() > 0) {
            get(0).getVariables(sort, list);
        }
    }

    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        switch (sort.getScope()) {
            case SUBSCOPE:
            case INSCOPE:
                basicVariables(sort, list);
                break;
            case ALLSCOPE:
                super.getVariables(sort, list);
                break;
        }
    }

}
