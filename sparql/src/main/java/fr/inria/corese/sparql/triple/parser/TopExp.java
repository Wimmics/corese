package fr.inria.corese.sparql.triple.parser;

import fr.inria.corese.sparql.triple.api.ASTVisitable;
import fr.inria.corese.sparql.triple.api.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

/**
 * Root of SPARQL Expression (filter) and Exp (triple, option, ...)
 *
 * @author corby
 *
 */
public class TopExp implements ASTVisitable {

    /**
     * @return the generated
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * @param generated the generated to set
     */
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }
    private boolean generated = false;

    @Override
    public String toString() {
        ASTBuffer sb = new ASTBuffer();
        toString(sb);
        return sb.toString();
    }

    public StringBuffer toString(StringBuffer sb) {
        return sb;
    }

    public ASTBuffer toString(ASTBuffer sb) {
        return sb;
    }

    public String toSparql() {
        return toString();
    }

    public StringBuffer toJava(StringBuffer sb) {
        return toString(sb);
    }

    @Override
    public void accept(ASTVisitor visitor) {

    }

    public List<Variable> getSubscopeVariables() {
        return getVariables(VariableScope.subscope);
    }

    public List<Variable> getInscopeVariables() {
        return getVariables(VariableScope.inscope);
    }

    public List<Variable> getAllVariables() {
        return getVariables(VariableScope.allscope);
    }

    public List<Variable> getVariables(VariableScope scope) {
        List<Variable> list = new ArrayList<>();
        getVariables(scope, list);
        return list;
    }

    void getVariables(VariableScope scope, List<Variable> list) {
    }

}
