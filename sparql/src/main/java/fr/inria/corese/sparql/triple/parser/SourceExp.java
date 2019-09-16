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
    
    // SUBSCOPE used to record source 
    // other did not
    
    @Override
    void getVariables(VariableScope sort, List<Variable> list) {
        super.getVariables(sort, list);
        getSource().getVariables(sort, list);
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
