package fr.inria.corese.sparql.triple.parser;

import java.util.List;

/**
 *
 * @author corby
 */
public class SourceExp extends And {

    Atom source;

    SourceExp() {
    }

    SourceExp(Exp exp) {
        super(exp);
    }

    SourceExp(Atom at, Exp exp) {
        super(exp);
        setSource(at);
    }

    public Atom getSource() {
        return source;
    }

    public void setSource(Atom at) {
        source = at;
    }
    
    @Override
    void getVariables(List<Variable> list) {
        super.getVariables(list);
        getSource().getVariables(list);
    }
    
    @Override
    void getVariables(VariableSort sort, List<Variable> list) {
        switch (sort) {
            case SUBSCOPE:  getVariables(list); break;
            default:  super.getVariables(sort, list); break;
        }
    }

    @Override
    public boolean validate(ASTQuery ast, boolean exist) {
        if (getSource().isVariable()) {
            ast.bind(getSource().getVariable());
            if (!exist) {
                ast.defSelect(getSource().getVariable());
            }
        }
        return super.validate(ast, exist);
    }

}
